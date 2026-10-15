package com.easyreg.service;

import com.easyreg.dto.ApplicationRequestDto;
import com.easyreg.entity.RegistrationApplication;
import com.easyreg.entity.User;
import com.easyreg.enums.RegistrationStatus;
import com.easyreg.enums.Role;
import com.easyreg.exception.BusinessException;
import com.easyreg.repository.RegistrationApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.easyreg.entity.ApprovalAuditLog;
import com.easyreg.enums.ApprovalStage;
import com.easyreg.repository.ApprovalAuditLogRepository;
import java.util.List;
import java.util.Random;

@Service
public class RegistrationService {

    private final RegistrationApplicationRepository applicationRepository;
    private final ApprovalAuditLogRepository auditLogRepository;

    public RegistrationService(RegistrationApplicationRepository applicationRepository,
                               ApprovalAuditLogRepository auditLogRepository) {
        this.applicationRepository = applicationRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Submits a new semester registration application for a student.
     */
    @Transactional
    public RegistrationApplication submitApplication(User student, ApplicationRequestDto req) {
        if (student.getRole() != Role.STUDENT) {
            throw new BusinessException("Only enrolled students can submit semester registration requests.");
        }

        // Prevent duplicate submissions for the same academic year and semester unless previously rejected
        boolean exists = applicationRepository.existsByStudentAndAcademicYearAndTargetSemesterAndStatusNot(
                student, req.getAcademicYear().trim(), req.getTargetSemester(), RegistrationStatus.REJECTED);
        if (exists) {
            throw new BusinessException("You already have an active or approved registration application for Semester "
                    + req.getTargetSemester() + " (" + req.getAcademicYear() + ").");
        }

        RegistrationApplication app = new RegistrationApplication();

        // Generate unique readable application reference
        String appNum = generateApplicationNumber(req.getTargetSemester());
        app.setApplicationNumber(appNum);
        app.setStudent(student);
        app.setAcademicYear(req.getAcademicYear().trim());
        app.setTargetSemester(req.getTargetSemester());
        app.setDepartment(req.getDepartment() != null ? req.getDepartment().trim() : student.getDepartment());
        app.setCgpa(req.getCgpa());
        app.setBacklogsCount(req.getBacklogsCount() != null ? req.getBacklogsCount() : 0);

        // Transport Option
        boolean transportOpted = Boolean.TRUE.equals(req.getTransportOpted());
        app.setTransportOpted(transportOpted);
        if (transportOpted) {
            app.setBusRoute(req.getBusRoute());
            app.setBusStop(req.getBusStop());
            app.setTransportFeeReceipt(req.getTransportFeeReceipt());
            app.setTransportFeeAmount(req.getTransportFeeAmount() != null ? req.getTransportFeeAmount() : 0.0);
        }

        // Academic / Tuition Fee Info
        app.setFeeReceiptNumber(req.getFeeReceiptNumber().trim());
        app.setFeeAmountPaid(req.getFeeAmountPaid());
        app.setFeePaymentDate(req.getFeePaymentDate() != null ? req.getFeePaymentDate() : LocalDate.now());

        // Registered Courses List
        app.setSelectedSubjects(req.getSelectedSubjects().trim());

        // Initial workflow stage: submitted applications start at Accounts for tuition fee clearance
        app.setStatus(RegistrationStatus.PENDING_ACCOUNTS);
        app.setSubmittedAt(LocalDateTime.now());

        return applicationRepository.save(app);
    }

    /**
     * Resubmits a previously rejected application with corrected/updated information.
     */
    @Transactional
    public RegistrationApplication resubmitApplication(User student, Long applicationId, ApplicationRequestDto req) {
        if (student.getRole() != Role.STUDENT) {
            throw new BusinessException("Only enrolled students can submit semester registration requests.");
        }

        RegistrationApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("Application not found with ID: " + applicationId));

        if (!app.getStudent().getId().equals(student.getId())) {
            throw new BusinessException("You are not authorized to resubmit this application.");
        }

        if (app.getStatus() != RegistrationStatus.REJECTED) {
            throw new BusinessException("Only rejected applications can be resubmitted. Current status: " 
                    + app.getStatus().getDescription());
        }

        // Update academic & student details
        app.setAcademicYear(req.getAcademicYear().trim());
        app.setTargetSemester(req.getTargetSemester());
        app.setDepartment(req.getDepartment() != null ? req.getDepartment().trim() : student.getDepartment());
        app.setCgpa(req.getCgpa());
        app.setBacklogsCount(req.getBacklogsCount() != null ? req.getBacklogsCount() : 0);

        // Update transport details
        boolean transportOpted = Boolean.TRUE.equals(req.getTransportOpted());
        app.setTransportOpted(transportOpted);
        if (transportOpted) {
            app.setBusRoute(req.getBusRoute());
            app.setBusStop(req.getBusStop());
            app.setTransportFeeReceipt(req.getTransportFeeReceipt());
            app.setTransportFeeAmount(req.getTransportFeeAmount() != null ? req.getTransportFeeAmount() : 0.0);
        } else {
            app.setBusRoute(null);
            app.setBusStop(null);
            app.setTransportFeeReceipt(null);
            app.setTransportFeeAmount(0.0);
        }

        // Update tuition fee info
        app.setFeeReceiptNumber(req.getFeeReceiptNumber().trim());
        app.setFeeAmountPaid(req.getFeeAmountPaid());
        app.setFeePaymentDate(req.getFeePaymentDate() != null ? req.getFeePaymentDate() : LocalDate.now());

        // Update selected courses
        app.setSelectedSubjects(req.getSelectedSubjects().trim());

        RegistrationStatus previousStatus = app.getStatus();

        // Reset clearance flags and rejection details back to initial clearance stage (Accounts)
        app.setStatus(RegistrationStatus.PENDING_ACCOUNTS);
        app.setRejectionReason(null);
        app.setRejectionStage(null);

        app.setAccountsCleared(false);
        app.setAccountsClearedAt(null);
        app.setAccountsApprovedBy(null);
        app.setAccountsRemarks(null);

        app.setTransportCleared(false);
        app.setTransportClearedAt(null);
        app.setTransportApprovedBy(null);
        app.setTransportRemarks(null);

        app.setLabCleared(false);
        app.setLabClearedAt(null);
        app.setLabApprovedBy(null);
        app.setLabRemarks(null);

        app.setHodApproved(false);
        app.setHodApprovedAt(null);
        app.setHodApprovedBy(null);
        app.setHodRemarks(null);

        app.setCompletedAt(null);
        app.setSubmittedAt(LocalDateTime.now());

        RegistrationApplication savedApp = applicationRepository.save(app);

        // Record audit trail entry for resubmission
        ApprovalAuditLog auditLog = new ApprovalAuditLog(
                savedApp,
                student,
                ApprovalStage.ACCOUNTS,
                "RESUBMITTED",
                previousStatus,
                RegistrationStatus.PENDING_ACCOUNTS,
                "Application details updated and resubmitted by student for clearance."
        );
        auditLogRepository.save(auditLog);

        return savedApp;
    }

    public List<RegistrationApplication> getStudentApplications(User student) {
        return applicationRepository.findByStudentOrderBySubmittedAtDesc(student);
    }

    public RegistrationApplication getApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Application not found with ID: " + id));
    }

    public RegistrationApplication getApplicationByNumber(String appNum) {
        return applicationRepository.findByApplicationNumber(appNum.trim())
                .orElseThrow(() -> new BusinessException("Application not found with number: " + appNum));
    }

    private String generateApplicationNumber(int semester) {
        int year = LocalDate.now().getYear();
        int randomDigits = 1000 + new Random().nextInt(9000);
        return "REG-" + year + "-S" + semester + "-" + randomDigits;
    }
}
