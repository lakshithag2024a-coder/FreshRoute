package com.freshroute.enums;

public enum FoodCategory {
    COOKED_MEALS("Cooked Meals", "fa-utensils", "Hot/chilled prepared dishes, curries, rice, pasta"),
    BAKERY("Bakery Items", "fa-bread-slice", "Breads, buns, bagels, croissants, pastries"),
    FRUITS_AND_VEGETABLES("Fruits & Vegetables", "fa-apple-alt", "Fresh produce, whole fruits, raw vegetables"),
    PACKAGED_FOOD("Packaged Food", "fa-box", "Sealed dry goods, snacks, canned products"),
    DAIRY_AND_BEVERAGES("Dairy & Beverages", "fa-wine-bottle", "Milk, yogurt, cheeses, fruit juices"),
    OTHER("Other", "fa-cube", "Miscellaneous surplus food items");

    private final String displayName;
    private final String iconClass;
    private final String description;

    FoodCategory(String displayName, String iconClass, String description) {
        this.displayName = displayName;
        this.iconClass = iconClass;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconClass() {
        return iconClass;
    }

    public String getDescription() {
        return description;
    }
}
