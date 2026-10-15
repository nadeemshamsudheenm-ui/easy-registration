package com.easyreg;

import com.easyreg.dto.ApplicationRequestDto;
import com.easyreg.entity.ApprovalAuditLog;
import com.easyreg.entity.RegistrationApplication;
import com.easyreg.entity.User;
import com.easyreg.enums.ApprovalStage;
import com.easyreg.enums.RegistrationStatus;
import com.easyreg.enums.Role;
import com.easyreg.exception.BusinessException;
import com.easyreg.repository.ApprovalAuditLogRepository;
import com.easyreg.repository.RegistrationApplicationRepository;
import com.easyreg.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegistrationResubmitTest {

    @Mock
    private RegistrationApplicationRepository applicationRepository;

    @Mock
    private ApprovalAuditLogRepository auditLogRepository;

    @InjectMocks
    private RegistrationService registrationService;

    private User student;
    private RegistrationApplication existingApp;

    @BeforeEach
    void setUp() {
        student = new User("2024CS101", "password123", "Test Student", "student@college.edu", "9876543210", Role.STUDENT);
        student.setId(10L);
        student.setDepartment("Computer Science & Engineering");

        existingApp = new RegistrationApplication();
        existingApp.setId(100L);
        existingApp.setApplicationNumber("REG-2026-S4-1001");
        existingApp.setStudent(student);
        existingApp.setAcademicYear("2025-2026");
        existingApp.setTargetSemester(4);
        existingApp.setDepartment("Computer Science & Engineering");
        existingApp.setCgpa(8.5);
        existingApp.setFeeReceiptNumber("OLD-RCPT-1");
        existingApp.setFeeAmountPaid(45000.0);
        existingApp.setSelectedSubjects("CS401, CS402");
        existingApp.setStatus(RegistrationStatus.REJECTED);
        existingApp.setRejectionStage(ApprovalStage.ACCOUNTS);
        existingApp.setRejectionReason("Tuition fee receipt number invalid");
    }

    @Test
    @DisplayName("Successfully resubmit a rejected application with updated fee details")
    void testResubmitRejectedApplicationSuccess() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(existingApp));
        when(applicationRepository.save(any(RegistrationApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationRequestDto updateDto = new ApplicationRequestDto();
        updateDto.setAcademicYear("2025-2026");
        updateDto.setTargetSemester(4);
        updateDto.setDepartment("Computer Science & Engineering");
        updateDto.setCgpa(8.5);
        updateDto.setBacklogsCount(0);
        updateDto.setFeeReceiptNumber("NEW-CORRECTED-RCPT-2026");
        updateDto.setFeeAmountPaid(45000.0);
        updateDto.setFeePaymentDate(LocalDate.now());
        updateDto.setSelectedSubjects("CS401, CS402");
        updateDto.setTransportOpted(false);

        RegistrationApplication resubmitted = registrationService.resubmitApplication(student, 100L, updateDto);

        assertNotNull(resubmitted);
        assertEquals(RegistrationStatus.PENDING_ACCOUNTS, resubmitted.getStatus());
        assertNull(resubmitted.getRejectionReason());
        assertNull(resubmitted.getRejectionStage());
        assertEquals("NEW-CORRECTED-RCPT-2026", resubmitted.getFeeReceiptNumber());
        assertFalse(resubmitted.getAccountsCleared());

        // Verify audit log created
        ArgumentCaptor<ApprovalAuditLog> logCaptor = ArgumentCaptor.forClass(ApprovalAuditLog.class);
        verify(auditLogRepository, times(1)).save(logCaptor.capture());
        ApprovalAuditLog capturedLog = logCaptor.getValue();
        assertEquals("RESUBMITTED", capturedLog.getDecision());
        assertEquals(RegistrationStatus.REJECTED, capturedLog.getPreviousStatus());
        assertEquals(RegistrationStatus.PENDING_ACCOUNTS, capturedLog.getNewStatus());
    }

    @Test
    @DisplayName("Resubmit fails if application is not in REJECTED status")
    void testResubmitFailsIfNotRejected() {
        existingApp.setStatus(RegistrationStatus.PENDING_ACCOUNTS);
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(existingApp));

        ApplicationRequestDto updateDto = new ApplicationRequestDto();
        updateDto.setAcademicYear("2025-2026");
        updateDto.setTargetSemester(4);
        updateDto.setDepartment("Computer Science & Engineering");
        updateDto.setCgpa(8.5);
        updateDto.setFeeReceiptNumber("RCPT-123");
        updateDto.setFeeAmountPaid(45000.0);
        updateDto.setSelectedSubjects("CS401");

        BusinessException ex = assertThrows(BusinessException.class, () -> 
                registrationService.resubmitApplication(student, 100L, updateDto)
        );
        assertTrue(ex.getMessage().contains("Only rejected applications can be resubmitted"));
    }

    @Test
    @DisplayName("Resubmit fails if another student tries to resubmit")
    void testResubmitFailsForUnauthorizedStudent() {
        User otherStudent = new User("2024CS102", "password", "Other", "other@college.edu", "111", Role.STUDENT);
        otherStudent.setId(999L);
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(existingApp));

        ApplicationRequestDto updateDto = new ApplicationRequestDto();
        updateDto.setAcademicYear("2025-2026");
        updateDto.setTargetSemester(4);
        updateDto.setDepartment("Computer Science & Engineering");
        updateDto.setCgpa(8.5);
        updateDto.setFeeReceiptNumber("RCPT-123");
        updateDto.setFeeAmountPaid(45000.0);
        updateDto.setSelectedSubjects("CS401");

        BusinessException ex = assertThrows(BusinessException.class, () -> 
                registrationService.resubmitApplication(otherStudent, 100L, updateDto)
        );
        assertTrue(ex.getMessage().contains("You are not authorized to resubmit this application"));
    }
}
