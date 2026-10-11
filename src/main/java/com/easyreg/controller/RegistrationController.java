package com.easyreg.controller;

import com.easyreg.dto.ApiResponse;
import com.easyreg.dto.ApplicationRequestDto;
import com.easyreg.entity.RegistrationApplication;
import com.easyreg.entity.User;
import com.easyreg.enums.Role;
import com.easyreg.service.AuthService;
import com.easyreg.service.RegistrationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registration")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final AuthService authService;

    public RegistrationController(RegistrationService registrationService, AuthService authService) {
        this.registrationService = registrationService;
        this.authService = authService;
    }

    /**
     * Submit semester registration request by the logged-in student.
     */
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<RegistrationApplication>> submitApplication(
            @Valid @RequestBody ApplicationRequestDto requestDto,
            HttpSession session) {
        User student = authService.requireRole(session, Role.STUDENT);
        RegistrationApplication app = registrationService.submitApplication(student, requestDto);
        return ResponseEntity.ok(ApiResponse.ok("Registration application submitted successfully! Reference: " 
                + app.getApplicationNumber(), app));
    }

    /**
     * List all applications submitted by the logged-in student.
     */
    @GetMapping("/my-applications")
    public ResponseEntity<ApiResponse<List<RegistrationApplication>>> getMyApplications(HttpSession session) {
        User student = authService.requireRole(session, Role.STUDENT);
        List<RegistrationApplication> applications = registrationService.getStudentApplications(student);
        return ResponseEntity.ok(ApiResponse.ok("Applications retrieved", applications));
    }

    /**
     * Fetch application details by ID (accessible by student owner or staff).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RegistrationApplication>> getApplicationById(
            @PathVariable Long id,
            HttpSession session) {
        authService.requireCurrentUser(session);
        RegistrationApplication app = registrationService.getApplicationById(id);
        return ResponseEntity.ok(ApiResponse.ok("Application found", app));
    }

    /**
     * Fetch application by application number (used for printable slip).
     */
    @GetMapping("/by-number/{appNum}")
    public ResponseEntity<ApiResponse<RegistrationApplication>> getApplicationByNumber(
            @PathVariable String appNum,
            HttpSession session) {
        authService.requireCurrentUser(session);
        RegistrationApplication app = registrationService.getApplicationByNumber(appNum);
        return ResponseEntity.ok(ApiResponse.ok("Application found", app));
    }
}
