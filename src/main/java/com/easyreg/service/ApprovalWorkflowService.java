package com.easyreg.service;

import com.easyreg.dto.ApprovalActionDto;
import com.easyreg.dto.DashboardStatsDto;
import com.easyreg.entity.ApprovalAuditLog;
import com.easyreg.entity.RegistrationApplication;
import com.easyreg.entity.User;
import com.easyreg.enums.ApprovalStage;
import com.easyreg.enums.RegistrationStatus;
import com.easyreg.enums.Role;
import com.easyreg.exception.BusinessException;
import com.easyreg.repository.ApprovalAuditLogRepository;
import com.easyreg.repository.RegistrationApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class ApprovalWorkflowService {

    private final RegistrationApplicationRepository applicationRepository;
    private final ApprovalAuditLogRepository auditLogRepository;

    public ApprovalWorkflowService(RegistrationApplicationRepository applicationRepository,
                                  ApprovalAuditLogRepository auditLogRepository) {
        this.applicationRepository = applicationRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Retrieves applications awaiting action by the logged-in staff member based on their role.
     */
    public List<RegistrationApplication> getPendingApplicationsForRole(User staffUser) {
        Role role = staffUser.getRole();

        return switch (role) {
            case ACCOUNTS -> applicationRepository.findByStatusOrderBySubmittedAtAsc(RegistrationStatus.PENDING_ACCOUNTS);
            case TRANSPORT -> applicationRepository.findByStatusOrderBySubmittedAtAsc(RegistrationStatus.PENDING_TRANSPORT);
            case LAB_ASSISTANT -> applicationRepository.findByStatusOrderBySubmittedAtAsc(RegistrationStatus.PENDING_LAB);
            case HOD -> {
                // If HOD has a specific department, filter by department; otherwise return all pending HOD
                if (staffUser.getDepartment() != null && !staffUser.getDepartment().isBlank()) {
                    List<RegistrationApplication> deptApps = applicationRepository
                            .findByDepartmentAndStatusOrderBySubmittedAtAsc(staffUser.getDepartment(), RegistrationStatus.PENDING_HOD);
                    // If no apps found specifically matching department string, fallback to all pending HOD
                    yield deptApps.isEmpty() 
                            ? applicationRepository.findByStatusOrderBySubmittedAtAsc(RegistrationStatus.PENDING_HOD)
                            : deptApps;
                }
                yield applicationRepository.findByStatusOrderBySubmittedAtAsc(RegistrationStatus.PENDING_HOD);
            }
            default -> Collections.emptyList();
        };
    }

    /**
     * Processes review decisions (Approval / Rejection) and automatically advances the workflow.
     */
    @Transactional
    public RegistrationApplication processApproval(User staffUser, ApprovalActionDto actionDto) {
        RegistrationApplication app = applicationRepository.findById(actionDto.getApplicationId())
                .orElseThrow(() -> new BusinessException("Application not found with ID: " + actionDto.getApplicationId()));

        boolean isApprove = "APPROVE".equalsIgnoreCase(actionDto.getDecision().trim());
        boolean isReject = "REJECT".equalsIgnoreCase(actionDto.getDecision().trim());

        if (!isApprove && !isReject) {
            throw new BusinessException("Invalid decision. Must be 'APPROVE' or 'REJECT'.");
        }

        RegistrationStatus previousStatus = app.getStatus();
        RegistrationStatus newStatus;
        ApprovalStage stage;

        String remarks = (actionDto.getRemarks() != null && !actionDto.getRemarks().isBlank())
                ? actionDto.getRemarks().trim()
                : (isApprove ? "Verified and approved." : "Rejected by department.");

        // Enforce role-based workflow stage integrity
        switch (staffUser.getRole()) {
            case ACCOUNTS -> {
                if (app.getStatus() != RegistrationStatus.PENDING_ACCOUNTS) {
                    throw new BusinessException("This application is not pending Accounts clearance. Current status: " 
                            + app.getStatus().getDescription());
                }
                stage = ApprovalStage.ACCOUNTS;

                if (isApprove) {
                    app.setAccountsCleared(true);
                    app.setAccountsClearedAt(LocalDateTime.now());
                    app.setAccountsApprovedBy(staffUser.getFullName());
                    app.setAccountsRemarks(remarks);

                    // Automatic forwarding:
                    // If transport was opted, send to Transport; otherwise skip directly to Lab clearance!
                    if (Boolean.TRUE.equals(app.getTransportOpted())) {
                        newStatus = RegistrationStatus.PENDING_TRANSPORT;
                    } else {
                        newStatus = RegistrationStatus.PENDING_LAB;
                    }
                } else {
                    newStatus = RegistrationStatus.REJECTED;
                    app.setRejectionStage(ApprovalStage.ACCOUNTS);
                    app.setRejectionReason(remarks);
                }
            }

            case TRANSPORT -> {
                if (app.getStatus() != RegistrationStatus.PENDING_TRANSPORT) {
                    throw new BusinessException("This application is not pending Transport clearance. Current status: " 
                            + app.getStatus().getDescription());
                }
                stage = ApprovalStage.TRANSPORT;

                if (isApprove) {
                    app.setTransportCleared(true);
                    app.setTransportClearedAt(LocalDateTime.now());
                    app.setTransportApprovedBy(staffUser.getFullName());
                    app.setTransportRemarks(remarks);

                    // Automatic forwarding to Lab
                    newStatus = RegistrationStatus.PENDING_LAB;
                } else {
                    newStatus = RegistrationStatus.REJECTED;
                    app.setRejectionStage(ApprovalStage.TRANSPORT);
                    app.setRejectionReason(remarks);
                }
            }

            case LAB_ASSISTANT -> {
                if (app.getStatus() != RegistrationStatus.PENDING_LAB) {
                    throw new BusinessException("This application is not pending Laboratory clearance. Current status: " 
                            + app.getStatus().getDescription());
                }
                stage = ApprovalStage.LAB;

                if (isApprove) {
                    app.setLabCleared(true);
                    app.setLabClearedAt(LocalDateTime.now());
                    app.setLabApprovedBy(staffUser.getFullName());
                    app.setLabRemarks(remarks);

                    // Automatic forwarding to HOD for final sign-off
                    newStatus = RegistrationStatus.PENDING_HOD;
                } else {
                    newStatus = RegistrationStatus.REJECTED;
                    app.setRejectionStage(ApprovalStage.LAB);
                    app.setRejectionReason(remarks);
                }
            }

            case HOD -> {
                if (app.getStatus() != RegistrationStatus.PENDING_HOD) {
                    throw new BusinessException("This application is not ready for HOD approval. Current status: " 
                            + app.getStatus().getDescription());
                }
                if (!app.isReadyForHod()) {
                    throw new BusinessException("Cannot approve: prerequisite departmental clearances are incomplete.");
                }
                stage = ApprovalStage.HOD;

                if (isApprove) {
                    app.setHodApproved(true);
                    app.setHodApprovedAt(LocalDateTime.now());
                    app.setHodApprovedBy(staffUser.getFullName());
                    app.setHodRemarks(remarks);
                    app.setCompletedAt(LocalDateTime.now());

                    // Final state: APPROVED
                    newStatus = RegistrationStatus.APPROVED;
                } else {
                    newStatus = RegistrationStatus.REJECTED;
                    app.setRejectionStage(ApprovalStage.HOD);
                    app.setRejectionReason(remarks);
                }
            }

            default -> throw new BusinessException("You do not have permissions to perform approval clearances.");
        }

        // Apply new status
        app.setStatus(newStatus);
        RegistrationApplication savedApp = applicationRepository.save(app);

        // Record audit trail entry
        ApprovalAuditLog auditLog = new ApprovalAuditLog(
                savedApp,
                staffUser,
                stage,
                isApprove ? "APPROVED" : "REJECTED",
                previousStatus,
                newStatus,
                remarks
        );
        auditLogRepository.save(auditLog);

        return savedApp;
    }

    /**
     * Retrieves audit history for an application.
     */
    public List<ApprovalAuditLog> getAuditTrail(Long applicationId) {
        RegistrationApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("Application not found"));
        return auditLogRepository.findByApplicationOrderByActionTimestampDesc(app);
    }

    /**
     * Dashboard statistics across departments.
     */
    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setTotalApplications(applicationRepository.count());
        stats.setPendingAccounts(applicationRepository.countByStatus(RegistrationStatus.PENDING_ACCOUNTS));
        stats.setPendingTransport(applicationRepository.countByStatus(RegistrationStatus.PENDING_TRANSPORT));
        stats.setPendingLab(applicationRepository.countByStatus(RegistrationStatus.PENDING_LAB));
        stats.setPendingHod(applicationRepository.countByStatus(RegistrationStatus.PENDING_HOD));
        stats.setTotalApproved(applicationRepository.countApprovedApplications());
        stats.setTotalRejected(applicationRepository.countRejectedApplications());
        return stats;
    }
}
