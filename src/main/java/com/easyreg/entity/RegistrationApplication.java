package com.easyreg.entity;

import com.easyreg.enums.ApprovalStage;
import com.easyreg.enums.RegistrationStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * RegistrationApplication entity representing a student's semester registration request.
 * Contains academic details, fee details, clearances from all departments, and workflow state.
 */
@Entity
@Table(name = "registration_applications")
public class RegistrationApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String applicationNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false, length = 30)
    private String academicYear; // e.g., "2025-2026"

    @Column(nullable = false)
    private Integer targetSemester; // e.g., 4 or 5

    @Column(nullable = false, length = 100)
    private String department;

    private Double cgpa;

    private Integer backlogsCount = 0;

    // Transport Requirement
    private Boolean transportOpted = false;

    @Column(length = 100)
    private String busRoute;

    @Column(length = 100)
    private String busStop;

    // Tuition / Semester Fee Info
    @Column(length = 80)
    private String feeReceiptNumber;

    private Double feeAmountPaid;

    private LocalDate feePaymentDate;

    // Transport Fee Info (when applicable)
    @Column(length = 80)
    private String transportFeeReceipt;

    private Double transportFeeAmount;

    // Registered Courses (stored as readable course list / JSON)
    @Column(columnDefinition = "TEXT")
    private String selectedSubjects;

    // Overall Workflow Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 35)
    private RegistrationStatus status = RegistrationStatus.PENDING_ACCOUNTS;

    // 1. Accounts Department Clearance
    private Boolean accountsCleared = false;
    private LocalDateTime accountsClearedAt;
    @Column(length = 100)
    private String accountsApprovedBy;
    @Column(length = 255)
    private String accountsRemarks;

    // 2. Transport Department Clearance (Required only if transportOpted == true)
    private Boolean transportCleared = false;
    private LocalDateTime transportClearedAt;
    @Column(length = 100)
    private String transportApprovedBy;
    @Column(length = 255)
    private String transportRemarks;

    // 3. Laboratory Assistant Clearance
    private Boolean labCleared = false;
    private LocalDateTime labClearedAt;
    @Column(length = 100)
    private String labApprovedBy;
    @Column(length = 255)
    private String labRemarks;

    // 4. HOD Final Approval
    private Boolean hodApproved = false;
    private LocalDateTime hodApprovedAt;
    @Column(length = 100)
    private String hodApprovedBy;
    @Column(length = 255)
    private String hodRemarks;

    // Rejection details if rejected at any point
    @Column(length = 255)
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ApprovalStage rejectionStage;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public RegistrationApplication() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.submittedAt == null) {
            this.submittedAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = RegistrationStatus.PENDING_ACCOUNTS;
        }
        if (this.backlogsCount == null) {
            this.backlogsCount = 0;
        }
        if (this.transportOpted == null) {
            this.transportOpted = false;
        }
        if (this.accountsCleared == null) this.accountsCleared = false;
        if (this.transportCleared == null) this.transportCleared = false;
        if (this.labCleared == null) this.labCleared = false;
        if (this.hodApproved == null) this.hodApproved = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper logic to check if all departmental prerequisite clearances are ready for HOD
    public boolean isReadyForHod() {
        boolean accOk = Boolean.TRUE.equals(this.accountsCleared);
        boolean transOk = !Boolean.TRUE.equals(this.transportOpted) || Boolean.TRUE.equals(this.transportCleared);
        boolean labOk = Boolean.TRUE.equals(this.labCleared);
        return accOk && transOk && labOk;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public Integer getTargetSemester() {
        return targetSemester;
    }

    public void setTargetSemester(Integer targetSemester) {
        this.targetSemester = targetSemester;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Double getCgpa() {
        return cgpa;
    }

    public void setCgpa(Double cgpa) {
        this.cgpa = cgpa;
    }

    public Integer getBacklogsCount() {
        return backlogsCount;
    }

    public void setBacklogsCount(Integer backlogsCount) {
        this.backlogsCount = backlogsCount;
    }

    public Boolean getTransportOpted() {
        return transportOpted;
    }

    public void setTransportOpted(Boolean transportOpted) {
        this.transportOpted = transportOpted;
    }

    public String getBusRoute() {
        return busRoute;
    }

    public void setBusRoute(String busRoute) {
        this.busRoute = busRoute;
    }

    public String getBusStop() {
        return busStop;
    }

    public void setBusStop(String busStop) {
        this.busStop = busStop;
    }

    public String getFeeReceiptNumber() {
        return feeReceiptNumber;
    }

    public void setFeeReceiptNumber(String feeReceiptNumber) {
        this.feeReceiptNumber = feeReceiptNumber;
    }

    public Double getFeeAmountPaid() {
        return feeAmountPaid;
    }

    public void setFeeAmountPaid(Double feeAmountPaid) {
        this.feeAmountPaid = feeAmountPaid;
    }

    public LocalDate getFeePaymentDate() {
        return feePaymentDate;
    }

    public void setFeePaymentDate(LocalDate feePaymentDate) {
        this.feePaymentDate = feePaymentDate;
    }

    public String getTransportFeeReceipt() {
        return transportFeeReceipt;
    }

    public void setTransportFeeReceipt(String transportFeeReceipt) {
        this.transportFeeReceipt = transportFeeReceipt;
    }

    public Double getTransportFeeAmount() {
        return transportFeeAmount;
    }

    public void setTransportFeeAmount(Double transportFeeAmount) {
        this.transportFeeAmount = transportFeeAmount;
    }

    public String getSelectedSubjects() {
        return selectedSubjects;
    }

    public void setSelectedSubjects(String selectedSubjects) {
        this.selectedSubjects = selectedSubjects;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public Boolean getAccountsCleared() {
        return accountsCleared;
    }

    public void setAccountsCleared(Boolean accountsCleared) {
        this.accountsCleared = accountsCleared;
    }

    public LocalDateTime getAccountsClearedAt() {
        return accountsClearedAt;
    }

    public void setAccountsClearedAt(LocalDateTime accountsClearedAt) {
        this.accountsClearedAt = accountsClearedAt;
    }

    public String getAccountsApprovedBy() {
        return accountsApprovedBy;
    }

    public void setAccountsApprovedBy(String accountsApprovedBy) {
        this.accountsApprovedBy = accountsApprovedBy;
    }

    public String getAccountsRemarks() {
        return accountsRemarks;
    }

    public void setAccountsRemarks(String accountsRemarks) {
        this.accountsRemarks = accountsRemarks;
    }

    public Boolean getTransportCleared() {
        return transportCleared;
    }

    public void setTransportCleared(Boolean transportCleared) {
        this.transportCleared = transportCleared;
    }

    public LocalDateTime getTransportClearedAt() {
        return transportClearedAt;
    }

    public void setTransportClearedAt(LocalDateTime transportClearedAt) {
        this.transportClearedAt = transportClearedAt;
    }

    public String getTransportApprovedBy() {
        return transportApprovedBy;
    }

    public void setTransportApprovedBy(String transportApprovedBy) {
        this.transportApprovedBy = transportApprovedBy;
    }

    public String getTransportRemarks() {
        return transportRemarks;
    }

    public void setTransportRemarks(String transportRemarks) {
        this.transportRemarks = transportRemarks;
    }

    public Boolean getLabCleared() {
        return labCleared;
    }

    public void setLabCleared(Boolean labCleared) {
        this.labCleared = labCleared;
    }

    public LocalDateTime getLabClearedAt() {
        return labClearedAt;
    }

    public void setLabClearedAt(LocalDateTime labClearedAt) {
        this.labClearedAt = labClearedAt;
    }

    public String getLabApprovedBy() {
        return labApprovedBy;
    }

    public void setLabApprovedBy(String labApprovedBy) {
        this.labApprovedBy = labApprovedBy;
    }

    public String getLabRemarks() {
        return labRemarks;
    }

    public void setLabRemarks(String labRemarks) {
        this.labRemarks = labRemarks;
    }

    public Boolean getHodApproved() {
        return hodApproved;
    }

    public void setHodApproved(Boolean hodApproved) {
        this.hodApproved = hodApproved;
    }

    public LocalDateTime getHodApprovedAt() {
        return hodApprovedAt;
    }

    public void setHodApprovedAt(LocalDateTime hodApprovedAt) {
        this.hodApprovedAt = hodApprovedAt;
    }

    public String getHodApprovedBy() {
        return hodApprovedBy;
    }

    public void setHodApprovedBy(String hodApprovedBy) {
        this.hodApprovedBy = hodApprovedBy;
    }

    public String getHodRemarks() {
        return hodRemarks;
    }

    public void setHodRemarks(String hodRemarks) {
        this.hodRemarks = hodRemarks;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public ApprovalStage getRejectionStage() {
        return rejectionStage;
    }

    public void setRejectionStage(ApprovalStage rejectionStage) {
        this.rejectionStage = rejectionStage;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
