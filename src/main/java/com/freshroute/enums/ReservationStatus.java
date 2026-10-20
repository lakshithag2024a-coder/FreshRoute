package com.freshroute.enums;

public enum ReservationStatus {
    RESERVED("Reserved (Pending Confirmation)", "badge-status-reserved"),
    CONFIRMED("Confirmed (Ready for Pickup)", "badge-status-confirmed"),
    COLLECTED("Collected (Completed)", "badge-status-collected"),
    CANCELLED("Cancelled", "badge-status-cancelled"),
    EXPIRED("Expired", "badge-status-expired");

    private final String displayName;
    private final String badgeClass;

    ReservationStatus(String displayName, String badgeClass) {
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
