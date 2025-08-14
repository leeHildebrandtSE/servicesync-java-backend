package com.wpc.servicesync_backend.model.entity;

public enum QRLocationType {
    KITCHEN_EXIT("Kitchen Exit", "KITCHEN_"),
    WARD_ARRIVAL("Ward Arrival", "WARD_"),
    NURSE_STATION("Nurse Station", "NURSE_");

    private final String displayName;
    private final String prefix;

    QRLocationType(String displayName, String prefix) {
        this.displayName = displayName;
        this.prefix = prefix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPrefix() {
        return prefix;
    }
}