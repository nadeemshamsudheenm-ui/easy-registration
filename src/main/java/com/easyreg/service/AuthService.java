package com.easyreg.service;

import com.easyreg.dto.LoginRequest;
import com.easyreg.dto.RegisterRequest;
import com.easyreg.dto.UserSummaryDto;
import com.easyreg.entity.User;
import com.easyreg.enums.Role;
import com.easyreg.exception.BusinessException;
import com.easyreg.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
public class AuthService {

    public static final String SESSION_USER_KEY = "LOGGED_IN_USER";

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Authenticates user against credentials and selected role.
     */
    public User login(LoginRequest request, HttpSession session) {
        User user = userRepository.findByUsername(request.getUsername().trim())
                .orElseThrow(() -> new BusinessException("User not found with ID/Register number: " + request.getUsername()));

        if (!user.getPassword().equals(request.getPassword().trim())) {
            throw new BusinessException("Invalid password. Please check your credentials.");
        }

        if (user.getRole() != request.getRole()) {
            throw new BusinessException("Invalid role selected. Account is registered as: " + user.getRole().getDisplayName());
        }

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new BusinessException("Your account is currently disabled. Please contact the administrator.");
        }

        // Store user in session
        session.setAttribute(SESSION_USER_KEY, user);
        return user;
    }

    /**
     * Registers a new student account.
     */
    @Transactional
    public User registerStudent(RegisterRequest req) {
        String regNo = req.getUsername().trim();

        if (userRepository.existsByUsername(regNo)) {
            throw new BusinessException("A student with Register Number '" + regNo + "' is already registered.");
        }

        User student = new User();
        student.setUsername(regNo);
        student.setPassword(req.getPassword().trim());
        student.setFullName(req.getFullName().trim());
        student.setEmail(req.getEmail().trim());
        student.setPhone(req.getPhone().trim());
        student.setRole(Role.STUDENT);
        student.setDepartment(req.getDepartment().trim());
        student.setBatchYear(req.getBatchYear() != null ? req.getBatchYear().trim() : "2024-2028");
        student.setCurrentSemester(req.getCurrentSemester());
        student.setRollNumber(req.getRollNumber() != null ? req.getRollNumber().trim() : regNo);
        student.setTransportOpted(Boolean.TRUE.equals(req.getTransportOpted()));
        student.setBusRoute(req.getBusRoute());
        student.setActive(true);

        return userRepository.save(student);
    }

    /**
     * Retrieves current logged in user from session.
     */
    public User getCurrentUser(HttpSession session) {
        Object userObj = session.getAttribute(SESSION_USER_KEY);
        if (userObj instanceof User user) {
            // refresh entity from database to have current state
            return userRepository.findById(user.getId()).orElse(null);
        }
        return null;
    }

    /**
     * Requires user to be logged in.
     */
    public User requireCurrentUser(HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            throw new BusinessException("Authentication required. Please log in to proceed.");
        }
        return user;
    }

    /**
     * Validates that the logged-in user possesses one of the authorized roles.
     */
    public User requireRole(HttpSession session, Role... allowedRoles) {
        User user = requireCurrentUser(session);
        boolean authorized = Arrays.stream(allowedRoles).anyMatch(r -> r == user.getRole());
        if (!authorized) {
            throw new BusinessException("Access denied. You do not have permissions for this action.");
        }
        return user;
    }

    /**
     * Invalidates the active session.
     */
    public void logout(HttpSession session) {
        session.removeAttribute(SESSION_USER_KEY);
        session.invalidate();
    }
}
