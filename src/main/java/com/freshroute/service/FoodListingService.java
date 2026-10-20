package com.freshroute.service;

import com.freshroute.dto.FoodListingDto;
import com.freshroute.entity.FoodListing;
import com.freshroute.entity.User;
import com.freshroute.enums.FoodCategory;
import com.freshroute.enums.ListingStatus;
import com.freshroute.repository.FoodListingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FoodListingService {

    private final FoodListingRepository foodListingRepository;

    public FoodListingService(FoodListingRepository foodListingRepository) {
        this.foodListingRepository = foodListingRepository;
    }

    @Transactional
    public FoodListing createListing(FoodListingDto dto, User donor) {
        if (dto.getPickupDeadline() == null || dto.getPickupDeadline().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Pickup deadline must be in the future.");
        }
        if (dto.getTotalQuantity() == null || dto.getTotalQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be a positive number.");
        }
        if (Boolean.FALSE.equals(dto.getSafetyDeclarationAccepted())) {
            throw new IllegalArgumentException("Food safety declaration must be accepted.");
        }

        FoodListing listing = new FoodListing();
        listing.setDonor(donor);
        listing.setTitle(dto.getTitle().trim());
        listing.setCategory(dto.getCategory());
        listing.setDescription(dto.getDescription().trim());
        listing.setTotalQuantity(dto.getTotalQuantity());
        listing.setAvailableQuantity(dto.getTotalQuantity());
        listing.setUnit(dto.getUnit());
        listing.setPreparationTime(dto.getPreparationTime() != null ? dto.getPreparationTime() : LocalDateTime.now());
        listing.setPickupDeadline(dto.getPickupDeadline());
        listing.setPickupAddress(dto.getPickupAddress().trim());
        listing.setDonorContact(dto.getDonorContact() != null ? dto.getDonorContact().trim() : donor.getPhone());
        listing.setAllergens(dto.getAllergens() != null ? dto.getAllergens().trim() : null);
        listing.setStorageRequirement(dto.getStorageRequirement() != null ? dto.getStorageRequirement().trim() : null);
        listing.setSafetyDeclarationAccepted(true);
        listing.setStatus(ListingStatus.AVAILABLE);

        // Assign realistic simulated distance if not given
        if (dto.getSimulatedDistanceKm() != null && dto.getSimulatedDistanceKm() > 0) {
            listing.setSimulatedDistanceKm(dto.getSimulatedDistanceKm());
        } else {
            listing.setSimulatedDistanceKm(Math.round((1.0 + Math.random() * 8.0) * 10.0) / 10.0);
        }

        return foodListingRepository.save(listing);
    }

    @Transactional
    public FoodListing updateListing(Long id, FoodListingDto dto, User currentUser) {
        FoodListing listing = foodListingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + id));

        // Authorization check: Only owner or admin can update
        if (!listing.getDonor().getId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            throw new AccessDeniedException("You are not authorized to edit this food listing.");
        }

        if (dto.getPickupDeadline() == null || dto.getPickupDeadline().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Pickup deadline must be in the future.");
        }

        listing.setTitle(dto.getTitle().trim());
        listing.setCategory(dto.getCategory());
        listing.setDescription(dto.getDescription().trim());

        // Update quantity adjust available quantity proportionally if changed
        int diff = dto.getTotalQuantity() - listing.getTotalQuantity();
        listing.setTotalQuantity(dto.getTotalQuantity());
        listing.setAvailableQuantity(Math.max(0, listing.getAvailableQuantity() + diff));
        if (listing.getAvailableQuantity() > 0 && listing.getStatus() == ListingStatus.RESERVED) {
            listing.setStatus(ListingStatus.AVAILABLE);
        }

        listing.setUnit(dto.getUnit());
        listing.setPreparationTime(dto.getPreparationTime());
        listing.setPickupDeadline(dto.getPickupDeadline());
        listing.setPickupAddress(dto.getPickupAddress().trim());
        listing.setDonorContact(dto.getDonorContact());
        listing.setAllergens(dto.getAllergens());
        listing.setStorageRequirement(dto.getStorageRequirement());
        listing.setUpdatedAt(LocalDateTime.now());

        return foodListingRepository.save(listing);
    }

    @Transactional
    public void deleteListing(Long id, User currentUser) {
        FoodListing listing = foodListingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + id));

        if (!listing.getDonor().getId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            throw new AccessDeniedException("You are not authorized to delete this food listing.");
        }

        foodListingRepository.delete(listing);
    }

    public Optional<FoodListing> findById(Long id) {
        return foodListingRepository.findById(id);
    }

    public List<FoodListing> getAvailableMarketplaceListings(String query, FoodCategory category, String sortBy) {
        // Exclude expired listings on the fly
        LocalDateTime now = LocalDateTime.now();
        String cleanQuery = (query != null && !query.trim().isBlank()) ? query.trim() : null;
        String cleanSort = (sortBy != null && !sortBy.trim().isBlank()) ? sortBy.trim() : "urgency";
        return foodListingRepository.searchMarketplace(now, cleanQuery, category, cleanSort);
    }

    public List<FoodListing> getFeaturedAvailableListings() {
        return foodListingRepository.findTop6ByStatusAndPickupDeadlineAfterAndAvailableQuantityGreaterThanOrderByPickupDeadlineAsc(
                ListingStatus.AVAILABLE, LocalDateTime.now(), 0);
    }

    public List<FoodListing> getListingsByDonor(User donor) {
        return foodListingRepository.findByDonorOrderByCreatedAtDesc(donor);
    }

    public List<FoodListing> getListingsByDonorAndStatus(User donor, ListingStatus status) {
        return foodListingRepository.findByDonorAndStatusOrderByCreatedAtDesc(donor, status);
    }

    public List<FoodListing> getAllListings() {
        return foodListingRepository.findAll();
    }

    /**
     * Automatic expiry checking: Excludes expired listings and updates status to EXPIRED.
     * Scheduled every 5 minutes and also called when needed.
     */
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void checkAndExpireListings() {
        LocalDateTime now = LocalDateTime.now();
        List<FoodListing> expiredListings = foodListingRepository.findByPickupDeadlineBeforeAndStatus(now, ListingStatus.AVAILABLE);
        for (FoodListing listing : expiredListings) {
            listing.setStatus(ListingStatus.EXPIRED);
            listing.setUpdatedAt(now);
            foodListingRepository.save(listing);
        }
    }

    public List<FoodListing> getExpiredListings() {
        return foodListingRepository.findByPickupDeadlineBeforeAndStatus(LocalDateTime.now(), ListingStatus.EXPIRED);
    }
}
