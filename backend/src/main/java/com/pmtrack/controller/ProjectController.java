package com.pmtrack.controller;

import com.pmtrack.dto.ProjectDto;
import com.pmtrack.model.Milestone;
import com.pmtrack.model.ProjectRisk;
import com.pmtrack.model.User;
import com.pmtrack.security.UserPrincipal;
import com.pmtrack.service.ProjectService;
import com.pmtrack.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Project management lifecycle endpoints")
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;

    public ProjectController(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get all projects accessible to the current user")
    public ResponseEntity<List<ProjectDto.ProjectResponse>> getAllProjects(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User currentUser = userService.getUserEntityById(userPrincipal.getId());
        return ResponseEntity.ok(projectService.getAllProjects(currentUser));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project details by ID")
    public ResponseEntity<ProjectDto.ProjectResponse> getProjectById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User currentUser = userService.getUserEntityById(userPrincipal.getId());
        return ResponseEntity.ok(projectService.getProjectById(id, currentUser));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Create a new project")
    public ResponseEntity<ProjectDto.ProjectResponse> createProject(
            @Valid @RequestBody ProjectDto.ProjectRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User creator = userService.getUserEntityById(userPrincipal.getId());
        return ResponseEntity.ok(projectService.createProject(request, creator));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Update an existing project")
    public ResponseEntity<ProjectDto.ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectDto.ProjectRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User modifier = userService.getUserEntityById(userPrincipal.getId());
        return ResponseEntity.ok(projectService.updateProject(id, request, modifier));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Add team member to project")
    public ResponseEntity<?> addMember(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        String roleInProject = payload.containsKey("roleInProject") ? payload.get("roleInProject").toString() : "Developer";
        Integer allocation = payload.containsKey("allocationPercentage") ? Integer.valueOf(payload.get("allocationPercentage").toString()) : 100;

        User actor = userService.getUserEntityById(userPrincipal.getId());
        projectService.addMember(id, userId, roleInProject, allocation, actor);
        return ResponseEntity.ok(Map.of("message", "Member added successfully"));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Remove team member from project")
    public ResponseEntity<?> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User actor = userService.getUserEntityById(userPrincipal.getId());
        projectService.removeMember(id, userId, actor);
        return ResponseEntity.ok(Map.of("message", "Member removed successfully"));
    }

    @GetMapping("/{id}/milestones")
    @Operation(summary = "Get project milestones")
    public ResponseEntity<List<Milestone>> getMilestones(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User actor = userService.getUserEntityById(userPrincipal.getId());
        return ResponseEntity.ok(projectService.getMilestones(id, actor));
    }

    @PostMapping("/{id}/milestones")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Add project milestone")
    public ResponseEntity<Milestone> addMilestone(
            @PathVariable Long id,
            @RequestBody ProjectDto.MilestoneDto dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User actor = userService.getUserEntityById(userPrincipal.getId());
        return ResponseEntity.ok(projectService.addMilestone(id, dto, actor));
    }

    @GetMapping("/{id}/risks")
    @Operation(summary = "Get project risks and issues")
    public ResponseEntity<List<ProjectRisk>> getRisks(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User actor = userService.getUserEntityById(userPrincipal.getId());
        return ResponseEntity.ok(projectService.getRisks(id, actor));
    }

    @PostMapping("/{id}/risks")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Add risk/issue to project")
    public ResponseEntity<ProjectRisk> addRisk(
            @PathVariable Long id,
            @RequestBody ProjectDto.RiskDto dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User actor = userService.getUserEntityById(userPrincipal.getId());
        return ResponseEntity.ok(projectService.addRisk(id, dto, actor));
    }
}
