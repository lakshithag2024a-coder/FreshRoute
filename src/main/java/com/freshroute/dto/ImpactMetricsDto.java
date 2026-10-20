package com.freshroute.dto;

import java.util.HashMap;
import java.util.Map;

public class ImpactMetricsDto {

    private long totalListings;
    private long currentlyAvailable;
    private long successfulPickups;
    private long totalQuantityCollected;
    private long expiredOrUnclaimed;
    private double estimatedFoodWasteAvoidedKg;
    private double estimatedCo2EmissionsAvoidedKg;
    private double activeSuccessRatePercent;

    // Charts data
    private Map<String, Long> statusCounts = new HashMap<>();
    private Map<String, Long> categoryQuantities = new HashMap<>();
    private Map<String, Long> pickupsByMonth = new HashMap<>();

    public ImpactMetricsDto() {}

    public long getTotalListings() { return totalListings; }
    public void setTotalListings(long totalListings) { this.totalListings = totalListings; }

    public long getCurrentlyAvailable() { return currentlyAvailable; }
    public void setCurrentlyAvailable(long currentlyAvailable) { this.currentlyAvailable = currentlyAvailable; }

    public long getSuccessfulPickups() { return successfulPickups; }
    public void setSuccessfulPickups(long successfulPickups) { this.successfulPickups = successfulPickups; }

    public long getTotalQuantityCollected() { return totalQuantityCollected; }
    public void setTotalQuantityCollected(long totalQuantityCollected) { this.totalQuantityCollected = totalQuantityCollected; }

    public long getExpiredOrUnclaimed() { return expiredOrUnclaimed; }
    public void setExpiredOrUnclaimed(long expiredOrUnclaimed) { this.expiredOrUnclaimed = expiredOrUnclaimed; }

    public double getEstimatedFoodWasteAvoidedKg() { return estimatedFoodWasteAvoidedKg; }
    public void setEstimatedFoodWasteAvoidedKg(double estimatedFoodWasteAvoidedKg) { this.estimatedFoodWasteAvoidedKg = estimatedFoodWasteAvoidedKg; }

    public double getEstimatedCo2EmissionsAvoidedKg() { return estimatedCo2EmissionsAvoidedKg; }
    public void setEstimatedCo2EmissionsAvoidedKg(double estimatedCo2EmissionsAvoidedKg) { this.estimatedCo2EmissionsAvoidedKg = estimatedCo2EmissionsAvoidedKg; }

    public double getActiveSuccessRatePercent() { return activeSuccessRatePercent; }
    public void setActiveSuccessRatePercent(double activeSuccessRatePercent) { this.activeSuccessRatePercent = activeSuccessRatePercent; }

    public Map<String, Long> getStatusCounts() { return statusCounts; }
    public void setStatusCounts(Map<String, Long> statusCounts) { this.statusCounts = statusCounts; }

    public Map<String, Long> getCategoryQuantities() { return categoryQuantities; }
    public void setCategoryQuantities(Map<String, Long> categoryQuantities) { this.categoryQuantities = categoryQuantities; }

    public Map<String, Long> getPickupsByMonth() { return pickupsByMonth; }
    public void setPickupsByMonth(Map<String, Long> pickupsByMonth) { this.pickupsByMonth = pickupsByMonth; }
}
