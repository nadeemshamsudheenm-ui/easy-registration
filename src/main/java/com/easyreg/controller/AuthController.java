package com.easyreg.controller;

import com.easyreg.dto.ApiResponse;
import com.easyreg.dto.LoginRequest;
import com.easyreg.dto.RegisterRequest;
import com.easyreg.dto.UserSummaryDto;
import com.easyreg.entity.User;
import com.easyreg.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserSummaryDto>> login(@Valid @RequestBody LoginRequest request,
                                                             HttpSession session) {
        User user = authService.login(request, session);
        return ResponseEntity.ok(ApiResponse.ok("Login successful. Welcome " + user.getFullName(),
                UserSummaryDto.fromEntity(user)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserSummaryDto>> register(@Valid @RequestBody RegisterRequest request) {
        User student = authService.registerStudent(request);
        return ResponseEntity.ok(ApiResponse.ok("Registration successful! You can now log in with your Register Number.",
                UserSummaryDto.fromEntity(student)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserSummaryDto>> getCurrentUser(HttpSession session) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.error("Not authenticated"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Authenticated", UserSummaryDto.fromEntity(user)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully."));
    }
}
