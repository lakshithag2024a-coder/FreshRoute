package com.freshroute.dto;

import com.freshroute.enums.FoodCategory;

public class MatchingCriteriaDto {

    private Integer requestedQuantity = 5;
    private FoodCategory category;
    private Double maxDistanceKm = 25.0;
    private String urgencyPreference = "BALANCED"; // URGENT_FIRST, BALANCED, CAPACITY_FIRST

    public MatchingCriteriaDto() {}

    public Integer getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(Integer requestedQuantity) { this.requestedQuantity = requestedQuantity; }

    public FoodCategory getCategory() { return category; }
    public void setCategory(FoodCategory category) { this.category = category; }

    public Double getMaxDistanceKm() { return maxDistanceKm; }
    public void setMaxDistanceKm(Double maxDistanceKm) { this.maxDistanceKm = maxDistanceKm; }

    public String getUrgencyPreference() { return urgencyPreference; }
    public void setUrgencyPreference(String urgencyPreference) { this.urgencyPreference = urgencyPreference; }
}
