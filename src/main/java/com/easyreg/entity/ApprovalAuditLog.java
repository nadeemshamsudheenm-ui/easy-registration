package com.easyreg.entity;

import com.easyreg.enums.ApprovalStage;
import com.easyreg.enums.RegistrationStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Immutable audit trail of every review, clearance, or rejection action performed by staff.
 */
@Entity
@Table(name = "approval_audit_logs")
public class ApprovalAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "application_id", nullable = false)
    private RegistrationApplication application;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "action_by_user_id", nullable = false)
    private User actionBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApprovalStage stage;

    @Column(nullable = false, length = 20)
    private String decision; // "APPROVED", "REJECTED"

    @Enumerated(EnumType.STRING)
    @Column(length = 35)
    private RegistrationStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 35)
    private RegistrationStatus newStatus;

    @Column(length = 500)
    private String remarks;

    @Column(nullable = false)
    private LocalDateTime actionTimestamp;

    public ApprovalAuditLog() {
    }

    public ApprovalAuditLog(RegistrationApplication application, User actionBy, ApprovalStage stage,
                            String decision, RegistrationStatus previousStatus, RegistrationStatus newStatus,
                            String remarks) {
        this.application = application;
        this.actionBy = actionBy;
        this.stage = stage;
        this.decision = decision;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.remarks = remarks;
        this.actionTimestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RegistrationApplication getApplication() {
        return application;
    }

    public void setApplication(RegistrationApplication application) {
        this.application = application;
    }

    public User getActionBy() {
        return actionBy;
    }

    public void setActionBy(User actionBy) {
        this.actionBy = actionBy;
    }

    public ApprovalStage getStage() {
        return stage;
    }

    public void setStage(ApprovalStage stage) {
        this.stage = stage;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public RegistrationStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(RegistrationStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public RegistrationStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(RegistrationStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getActionTimestamp() {
        return actionTimestamp;
    }

    public void setActionTimestamp(LocalDateTime actionTimestamp) {
        this.actionTimestamp = actionTimestamp;
    }
}
