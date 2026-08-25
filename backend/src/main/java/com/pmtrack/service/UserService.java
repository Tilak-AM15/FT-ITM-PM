package com.pmtrack.service;

import com.pmtrack.dto.AuthDto;
import com.pmtrack.model.Role;
import com.pmtrack.model.User;
import com.pmtrack.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<AuthDto.UserProfileDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(AuthDto.UserProfileDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuthDto.UserProfileDto> getActiveUsers() {
        return userRepository.findByActiveTrue().stream()
                .map(AuthDto.UserProfileDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuthDto.UserProfileDto> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(AuthDto.UserProfileDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public User getUserEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    @Transactional(readOnly = true)
    public AuthDto.UserProfileDto getUserProfileById(Long id) {
        return new AuthDto.UserProfileDto(getUserEntityById(id));
    }

    @Transactional
    public AuthDto.UserProfileDto createUser(AuthDto.RegisterRequest request, User creator) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use: " + request.getEmail());
        }

        if (request.getRole() == Role.SUPER_ADMIN && creator.getRole() != Role.SUPER_ADMIN) {
            throw new RuntimeException("Only a Super Admin can create another Super Admin.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(request.getRole() != null ? request.getRole() : Role.EMPLOYEE);
        user.setDepartment(request.getDepartment());
        user.setDesignation(request.getDesignation());
        user.setHourlyRate(request.getHourlyRate() != null ? request.getHourlyRate() : 50.0);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        auditService.logAction(
                creator,
                "USER_CREATED",
                "User",
                savedUser.getId(),
                null,
                savedUser.getUsername() + " (" + savedUser.getRole() + ")",
                "New user registered"
        );

        return new AuthDto.UserProfileDto(savedUser);
    }

    @Transactional
    public AuthDto.UserProfileDto updateUser(Long id, AuthDto.RegisterRequest request, User modifier) {
        User user = getUserEntityById(id);
        String oldRole = user.getRole().name();
        if (request.getRole() == Role.SUPER_ADMIN && modifier.getRole() != Role.SUPER_ADMIN) {
            throw new RuntimeException("Only a Super Admin can assign the Super Admin role.");
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        user.setDepartment(request.getDepartment());
        user.setDesignation(request.getDesignation());
        if (request.getHourlyRate() != null) {
            user.setHourlyRate(request.getHourlyRate());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User savedUser = userRepository.save(user);

        auditService.logAction(
                modifier,
                "USER_UPDATED",
                "User",
                savedUser.getId(),
                oldRole,
                savedUser.getRole().name(),
                "User profile updated"
        );

        return new AuthDto.UserProfileDto(savedUser);
    }
}
