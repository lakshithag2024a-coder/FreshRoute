package com.freshroute.enums;

public enum ListingStatus {
    AVAILABLE("Available", "bg-success text-white"),
    RESERVED("Reserved", "bg-warning text-dark"),
    COLLECTED("Collected", "bg-info text-white"),
    EXPIRED("Expired", "bg-secondary text-white"),
    CANCELLED("Cancelled", "bg-danger text-white");

    private final String displayName;
    private final String badgeClass;

    ListingStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
