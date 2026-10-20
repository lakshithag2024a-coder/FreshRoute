package com.freshroute.enums;

public enum FoodUnit {
    PORTIONS("Portions / Meals"),
    KG("Kilograms (kg)"),
    BOXES("Boxes / Packs"),
    ITEMS("Individual Items"),
    LITERS("Liters (L)");

    private final String displayName;

    FoodUnit(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
