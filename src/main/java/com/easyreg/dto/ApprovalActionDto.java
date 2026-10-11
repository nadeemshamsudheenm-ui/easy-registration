package com.easyreg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ApprovalActionDto {

    @NotNull(message = "Application ID is required")
    private Long applicationId;

    @NotBlank(message = "Decision is required (APPROVE or REJECT)")
    private String decision; // "APPROVE" or "REJECT"

    private String remarks;

    public ApprovalActionDto() {}

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
