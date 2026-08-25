package com.pmtrack.dto;

import com.pmtrack.model.Role;
import com.pmtrack.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDto {

    public static class LoginRequest {
        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;

        public LoginRequest() {}
        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterRequest {
        @NotBlank
        @Size(min = 3, max = 50)
        private String username;

        @NotBlank
        @Email
        private String email;

        @NotBlank
        @Size(min = 6, max = 100)
        private String password;

        @NotBlank
        private String fullName;

        private Role role = Role.EMPLOYEE;
        private String department;
        private String designation;
        private Double hourlyRate = 50.0;

        public RegisterRequest() {}

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }
        public Double getHourlyRate() { return hourlyRate; }
        public void setHourlyRate(Double hourlyRate) { this.hourlyRate = hourlyRate; }
    }

    public static class AuthResponse {
        private String token;
        private String tokenType = "Bearer";
        private UserProfileDto user;

        public AuthResponse() {}
        public AuthResponse(String token, UserProfileDto user) {
            this.token = token;
            this.user = user;
        }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }
        public UserProfileDto getUser() { return user; }
        public void setUser(UserProfileDto user) { this.user = user; }
    }

    public static class UserProfileDto {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private Role role;
        private String department;
        private String designation;
        private String avatarUrl;
        private Double hourlyRate;
        private boolean active;

        public UserProfileDto() {}
        public UserProfileDto(User user) {
            if (user != null) {
                this.id = user.getId();
                this.username = user.getUsername();
                this.email = user.getEmail();
                this.fullName = user.getFullName();
                this.role = user.getRole();
                this.department = user.getDepartment();
                this.designation = user.getDesignation();
                this.avatarUrl = user.getAvatarUrl();
                this.hourlyRate = user.getHourlyRate();
                this.active = user.isActive();
            }
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
        public Double getHourlyRate() { return hourlyRate; }
        public void setHourlyRate(Double hourlyRate) { this.hourlyRate = hourlyRate; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }
}
