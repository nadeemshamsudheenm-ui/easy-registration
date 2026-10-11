package com.easyreg.enums;

/**
 * Status of the semester registration workflow.
 */
public enum RegistrationStatus {
    DRAFT("Draft / Not Submitted", 0),
    PENDING_ACCOUNTS("Pending Accounts Fee Clearance", 1),
    PENDING_TRANSPORT("Pending Transport Clearance", 2),
    PENDING_LAB("Pending Laboratory Clearance", 3),
    PENDING_HOD("Pending HOD Final Approval", 4),
    APPROVED("Approved & Registration Completed", 5),
    REJECTED("Registration Rejected", -1);

    private final String description;
    private final int stageStep;

    RegistrationStatus(String description, int stageStep) {
        this.description = description;
        this.stageStep = stageStep;
    }

    public String getDescription() {
        return description;
    }

    public int getStageStep() {
        return stageStep;
    }
}
