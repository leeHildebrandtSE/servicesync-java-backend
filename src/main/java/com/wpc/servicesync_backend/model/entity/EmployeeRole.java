package com.wpc.servicesync_backend.model.entity;

public enum EmployeeRole {
    HOSTESS("Hostess", "👩‍⚕️"),
    NURSE("Nurse", "👨‍⚕️"),
    SUPERVISOR("Supervisor", "👔"),
    ADMIN("Administrator", "🛡️");

    private final String displayName;
    private final String icon;

    EmployeeRole(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getDisplayNameWithIcon() {
        return icon + " " + displayName;
    }
}