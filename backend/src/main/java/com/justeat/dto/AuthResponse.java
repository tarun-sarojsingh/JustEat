package com.justeat.dto;

public class AuthResponse {
    private String token;
    private String username;
    private String role;
    private Long userId;

    public AuthResponse(String token, String username, String role, Long userId) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.userId = userId;
    }

    public String getToken() { return token; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public Long getUserId() { return userId; }
}
