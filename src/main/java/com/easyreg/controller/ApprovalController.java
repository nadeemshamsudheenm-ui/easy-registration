package com.easyreg.controller;

import com.easyreg.dto.ApiResponse;
import com.easyreg.dto.ApprovalActionDto;
import com.easyreg.dto.DashboardStatsDto;
import com.easyreg.entity.ApprovalAuditLog;
import com.easyreg.entity.RegistrationApplication;
import com.easyreg.entity.User;
import com.easyreg.enums.Role;
import com.easyreg.service.ApprovalWorkflowService;
import com.easyreg.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    private final ApprovalWorkflowService workflowService;
    private final AuthService authService;

    public ApprovalController(ApprovalWorkflowService workflowService, AuthService authService) {
        this.workflowService = workflowService;
        this.authService = authService;
    }

    /**
     * Retrieve applications waiting for verification at the staff member's department.
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<RegistrationApplication>>> getPendingApplications(HttpSession session) {
        User staff = authService.requireRole(session, 
                Role.ACCOUNTS, Role.TRANSPORT, Role.LAB_ASSISTANT, Role.HOD);
        List<RegistrationApplication> pendingList = workflowService.getPendingApplicationsForRole(staff);
        return ResponseEntity.ok(ApiResponse.ok("Pending applications retrieved", pendingList));
    }

    /**
     * Staff action: Approve or Reject application, automatically forwarding to next stage.
     */
    @PostMapping("/action")
    public ResponseEntity<ApiResponse<RegistrationApplication>> processAction(
            @Valid @RequestBody ApprovalActionDto actionDto,
            HttpSession session) {
        User staff = authService.requireRole(session, 
                Role.ACCOUNTS, Role.TRANSPORT, Role.LAB_ASSISTANT, Role.HOD);
        RegistrationApplication updatedApp = workflowService.processApproval(staff, actionDto);
        String msg = "APPROVE".equalsIgnoreCase(actionDto.getDecision())
                ? "Application approved and forwarded to next stage (" + updatedApp.getStatus().getDescription() + ")."
                : "Application rejected.";
        return ResponseEntity.ok(ApiResponse.ok(msg, updatedApp));
    }

    /**
     * Get review audit history for an application.
     */
    @GetMapping("/audit/{appId}")
    public ResponseEntity<ApiResponse<List<ApprovalAuditLog>>> getAuditTrail(
            @PathVariable Long appId,
            HttpSession session) {
        authService.requireCurrentUser(session);
        List<ApprovalAuditLog> logs = workflowService.getAuditTrail(appId);
        return ResponseEntity.ok(ApiResponse.ok("Audit logs retrieved", logs));
    }

    /**
     * Get aggregate statistics across all departments.
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getStats(HttpSession session) {
        authService.requireCurrentUser(session);
        DashboardStatsDto stats = workflowService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.ok("Statistics retrieved", stats));
    }
}
