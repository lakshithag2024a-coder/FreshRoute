package com.freshroute.dto;

import com.freshroute.entity.FoodListing;
import java.util.ArrayList;
import java.util.List;

public class SmartMatchResultDto {

    private FoodListing listing;
    private double matchScore; // 0.0 to 100.0
    private double urgencyScore; // 0.0 to 100.0
    private double quantityScore; // 0.0 to 100.0
    private double distanceScore; // 0.0 to 100.0
    private List<String> matchReasons = new ArrayList<>();

    public SmartMatchResultDto() {}

    public SmartMatchResultDto(FoodListing listing, double matchScore, double urgencyScore, 
                              double quantityScore, double distanceScore, List<String> matchReasons) {
        this.listing = listing;
        this.matchScore = Math.round(matchScore * 10.0) / 10.0;
        this.urgencyScore = Math.round(urgencyScore * 10.0) / 10.0;
        this.quantityScore = Math.round(quantityScore * 10.0) / 10.0;
        this.distanceScore = Math.round(distanceScore * 10.0) / 10.0;
        this.matchReasons = matchReasons;
    }

    public String getScoreBadgeClass() {
        if (matchScore >= 80) return "bg-success text-white";
        if (matchScore >= 60) return "bg-primary text-white";
        if (matchScore >= 40) return "bg-warning text-dark";
        return "bg-secondary text-white";
    }

    public String getScoreGrade() {
        if (matchScore >= 85) return "Exceptional Match";
        if (matchScore >= 70) return "High Compatibility";
        if (matchScore >= 50) return "Moderate Fit";
        return "Low Priority";
    }

    public FoodListing getListing() { return listing; }
    public void setListing(FoodListing listing) { this.listing = listing; }

    public double getMatchScore() { return matchScore; }
    public void setMatchScore(double matchScore) { this.matchScore = matchScore; }

    public double getUrgencyScore() { return urgencyScore; }
    public void setUrgencyScore(double urgencyScore) { this.urgencyScore = urgencyScore; }

    public double getQuantityScore() { return quantityScore; }
    public void setQuantityScore(double quantityScore) { this.quantityScore = quantityScore; }

    public double getDistanceScore() { return distanceScore; }
    public void setDistanceScore(double distanceScore) { this.distanceScore = distanceScore; }

    public List<String> getMatchReasons() { return matchReasons; }
    public void setMatchReasons(List<String> matchReasons) { this.matchReasons = matchReasons; }
}
