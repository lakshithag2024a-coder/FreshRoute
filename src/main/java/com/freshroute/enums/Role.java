package com.freshroute.enums;

public enum Role {
    ROLE_DONOR("Donor / Business"),
    ROLE_RECIPIENT("Recipient / Community"),
    ROLE_ADMIN("Administrator");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
