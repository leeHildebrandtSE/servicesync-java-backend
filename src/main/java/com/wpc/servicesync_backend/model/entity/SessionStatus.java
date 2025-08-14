package com.wpc.servicesync_backend.model.entity;

public enum SessionStatus {
    ACTIVE("Active"),
    IN_TRANSIT("In Transit"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    SessionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}