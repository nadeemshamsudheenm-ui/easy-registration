package com.easyreg.enums;

/**
 * Individual approval clearance stages.
 */
public enum ApprovalStage {
    ACCOUNTS("Accounts Clearance"),
    TRANSPORT("Transport Clearance"),
    LAB("Laboratory Clearance"),
    HOD("HOD Final Sign-off");

    private final String title;

    ApprovalStage(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
