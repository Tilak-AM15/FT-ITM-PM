package com.pmtrack.controller;

import com.pmtrack.dto.AuthDto;
import com.pmtrack.model.User;
import com.pmtrack.security.UserPrincipal;
import com.pmtrack.service.AuthService;
import com.pmtrack.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user login, registration, and session info")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(summary = "Log in with username and password")
    public ResponseEntity<AuthDto.AuthResponse> login(@Valid @RequestBody AuthDto.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Register a user by an administrator")
    public ResponseEntity<AuthDto.AuthResponse> register(@Valid @RequestBody AuthDto.RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current logged-in user profile")
    public ResponseEntity<AuthDto.UserProfileDto> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(userService.getUserProfileById(userPrincipal.getId()));
    }
}
