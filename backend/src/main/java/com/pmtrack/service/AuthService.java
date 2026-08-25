package com.pmtrack.service;

import com.pmtrack.dto.AuthDto;
import com.pmtrack.model.Role;
import com.pmtrack.model.User;
import com.pmtrack.repository.UserRepository;
import com.pmtrack.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditService auditService;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository,
                       PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider, AuditService auditService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.auditService = auditService;
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getUsername()));

        auditService.logAction(user, "USER_LOGIN", "User", user.getId(), null, null, "User logged in successfully");

        return new AuthDto.AuthResponse(jwt, new AuthDto.UserProfileDto(user));
    }

    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
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

        String jwt = tokenProvider.generateTokenFromUsername(savedUser.getUsername());

        auditService.logAction(savedUser, "USER_REGISTER", "User", savedUser.getId(), null, savedUser.getRole().name(), "New user self-registered");

        return new AuthDto.AuthResponse(jwt, new AuthDto.UserProfileDto(savedUser));
    }
}
