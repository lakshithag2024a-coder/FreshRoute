package com.freshroute.service;

import com.freshroute.dto.MatchingCriteriaDto;
import com.freshroute.dto.SmartMatchResultDto;
import com.freshroute.entity.FoodListing;
import com.freshroute.enums.ListingStatus;
import com.freshroute.repository.FoodListingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SmartMatchingService {

    private final FoodListingRepository foodListingRepository;

    @Value("${freshroute.matching.urgency-weight:0.40}")
    private double defaultUrgencyWeight;

    @Value("${freshroute.matching.quantity-weight:0.35}")
    private double defaultQuantityWeight;

    @Value("${freshroute.matching.distance-weight:0.25}")
    private double defaultDistanceWeight;

    @Value("${freshroute.matching.max-distance-km:25.0}")
    private double configuredMaxDistanceKm;

    public SmartMatchingService(FoodListingRepository foodListingRepository) {
        this.foodListingRepository = foodListingRepository;
    }

    /**
     * Executes the deterministic rule-based matching algorithm to rank food listings
     * based on Urgency, Quantity Match, and Transit Feasibility.
     */
    public List<SmartMatchResultDto> findMatches(MatchingCriteriaDto criteria) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Exclude expired and unavailable listings
        List<FoodListing> eligibleListings = foodListingRepository
                .findByStatusAndPickupDeadlineAfterAndAvailableQuantityGreaterThanOrderByPickupDeadlineAsc(
                        ListingStatus.AVAILABLE, now, 0);

        // Filter by category if requested
        if (criteria.getCategory() != null) {
            eligibleListings = eligibleListings.stream()
                    .filter(listing -> listing.getCategory() == criteria.getCategory())
                    .toList();
        }

        int requestedQty = (criteria.getRequestedQuantity() != null && criteria.getRequestedQuantity() > 0)
                ? criteria.getRequestedQuantity() : 5;
        double maxDist = (criteria.getMaxDistanceKm() != null && criteria.getMaxDistanceKm() > 0)
                ? criteria.getMaxDistanceKm() : configuredMaxDistanceKm;

        // Determine weights dynamically if preference is specified
        double uWeight = defaultUrgencyWeight;
        double qWeight = defaultQuantityWeight;
        double dWeight = defaultDistanceWeight;

        if ("URGENT_FIRST".equalsIgnoreCase(criteria.getUrgencyPreference())) {
            uWeight = 0.60;
            qWeight = 0.25;
            dWeight = 0.15;
        } else if ("CAPACITY_FIRST".equalsIgnoreCase(criteria.getUrgencyPreference())) {
            uWeight = 0.25;
            qWeight = 0.55;
            dWeight = 0.20;
        } else if ("DISTANCE_FIRST".equalsIgnoreCase(criteria.getUrgencyPreference())) {
            uWeight = 0.25;
            qWeight = 0.25;
            dWeight = 0.50;
        }

        List<SmartMatchResultDto> results = new ArrayList<>();

        for (FoodListing listing : eligibleListings) {
            List<String> reasons = new ArrayList<>();

            // 1. Calculate Urgency Score (0 - 100)
            // Listings expiring sooner receive higher urgency score
            Duration duration = Duration.between(now, listing.getPickupDeadline());
            long minutesLeft = duration.toMinutes();
            double urgencyScore;

            if (minutesLeft <= 120) { // <= 2 hours
                urgencyScore = 100.0;
                reasons.add("⚡ Critical Urgency: Pickup deadline in less than 2 hours!");
            } else if (minutesLeft <= 360) { // 2 - 6 hours
                urgencyScore = 85.0 + (360 - minutesLeft) * (15.0 / 240.0);
                reasons.add("⏰ Approaching Deadline: Within 6 hours of expiry");
            } else if (minutesLeft <= 1440) { // 6 - 24 hours (Today)
                urgencyScore = 50.0 + (1440 - minutesLeft) * (35.0 / 1080.0);
                reasons.add("📅 Same-Day Pickup: Available today");
            } else { // > 24 hours
                urgencyScore = Math.max(15.0, 50.0 - (minutesLeft - 1440) * (35.0 / 4320.0));
                reasons.add("⏳ Extended Window: Flexible pickup schedule");
            }

            // 2. Calculate Quantity Compatibility Score (0 - 100)
            // Prefer listings that can satisfy the requested quantity without excessive overflow
            int available = listing.getAvailableQuantity();
            double quantityScore;

            if (available == requestedQty) {
                quantityScore = 100.0;
                reasons.add("🎯 Perfect Quantity Match: Exactly " + requestedQty + " " + listing.getUnit().getDisplayName());
            } else if (available > requestedQty) {
                double surplusRatio = (double) available / requestedQty;
                if (surplusRatio <= 2.0) {
                    quantityScore = 95.0;
                    reasons.add("✅ Full Demand Satisfied: Can provide all " + requestedQty + " units (+ surplus available)");
                } else {
                    quantityScore = Math.max(65.0, 95.0 - (surplusRatio - 2.0) * 5.0);
                    reasons.add("📦 High Volume Batch: " + available + " units available (bulk allocation)");
                }
            } else { // available < requestedQty (partial fulfillment)
                double fulfillmentRatio = (double) available / requestedQty;
                quantityScore = Math.max(25.0, fulfillmentRatio * 80.0);
                reasons.add("🧩 Partial Fulfillment: Covers " + available + " of " + requestedQty + " requested units (" + 
                        (int)(fulfillmentRatio * 100) + "%)");
            }

            // 3. Calculate Distance / Proximity Score (0 - 100)
            double distance = listing.getSimulatedDistanceKm() != null ? listing.getSimulatedDistanceKm() : 4.0;
            double distanceScore;

            if (distance <= 3.0) {
                distanceScore = 100.0;
                reasons.add("📍 Close Proximity: Only " + distance + " km away (quick low-emission collection)");
            } else if (distance <= maxDist) {
                distanceScore = Math.max(20.0, 100.0 - ((distance - 3.0) / (maxDist - 3.0)) * 75.0);
                reasons.add("🚗 Accessible Location: " + distance + " km away (within " + maxDist + " km radius)");
            } else {
                distanceScore = Math.max(5.0, 20.0 - (distance - maxDist) * 2.0);
                reasons.add("⚠️ Extended Transit: " + distance + " km (beyond preferred " + maxDist + " km)");
            }

            // Category bonus or highlight
            reasons.add("🏷️ Category: " + listing.getCategory().getDisplayName());

            // 4. Calculate Weighted Composite Match Score
            double totalScore = (urgencyScore * uWeight) + (quantityScore * qWeight) + (distanceScore * dWeight);
            totalScore = Math.min(100.0, Math.max(0.0, totalScore));

            results.add(new SmartMatchResultDto(listing, totalScore, urgencyScore, quantityScore, distanceScore, reasons));
        }

        // Sort by total match score descending (best matches first)
        results.sort(Comparator.comparingDouble(SmartMatchResultDto::getMatchScore).reversed());

        return results;
    }

    public double getDefaultUrgencyWeight() { return defaultUrgencyWeight; }
    public double getDefaultQuantityWeight() { return defaultQuantityWeight; }
    public double getDefaultDistanceWeight() { return defaultDistanceWeight; }
    public double getConfiguredMaxDistanceKm() { return configuredMaxDistanceKm; }
}
