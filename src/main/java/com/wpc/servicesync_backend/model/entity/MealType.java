package com.wpc.servicesync_backend.model.entity;

public enum MealType {
    BREAKFAST("Breakfast", "🌅", "06:00 - 09:00"),
    LUNCH("Lunch", "☀️", "11:30 - 14:00"),
    SUPPER("Supper", "🌙", "17:00 - 19:30"),
    BEVERAGES("Beverages", "☕", "All Day");

    private final String displayName;
    private final String icon;
    private final String timeRange;

    MealType(String displayName, String icon, String timeRange) {
        this.displayName = displayName;
        this.icon = icon;
        this.timeRange = timeRange;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getTimeRange() {
        return timeRange;
    }

    public String getDisplayNameWithIcon() {
        return icon + " " + displayName;
    }

    public String getLowercaseDisplayName() {
        return displayName.toLowerCase();
    }
}