package com.pmtrack.controller;

import com.pmtrack.dto.AuthDto;
import com.pmtrack.model.Role;
import com.pmtrack.model.User;
import com.pmtrack.security.UserPrincipal;
import com.pmtrack.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management and directory endpoints")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','PROJECT_MANAGER','TEAM_LEAD')")
    @Operation(summary = "Get active users for project/resource assignment")
    public ResponseEntity<List<AuthDto.UserProfileDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getActiveUsers());
    }

    @GetMapping("/by-role/{role}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','PROJECT_MANAGER','TEAM_LEAD')")
    @Operation(summary = "Get users by specific role")
    public ResponseEntity<List<AuthDto.UserProfileDto>> getUsersByRole(@PathVariable Role role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<AuthDto.UserProfileDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserProfileById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Create user by Administrator")
    public ResponseEntity<AuthDto.UserProfileDto> createUser(
            @Valid @RequestBody AuthDto.RegisterRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        User creator = userService.getUserEntityById(currentUser.getId());
        return ResponseEntity.ok(userService.createUser(request, creator));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Update user details")
    public ResponseEntity<AuthDto.UserProfileDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AuthDto.RegisterRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        User modifier = userService.getUserEntityById(currentUser.getId());
        return ResponseEntity.ok(userService.updateUser(id, request, modifier));
    }
}
