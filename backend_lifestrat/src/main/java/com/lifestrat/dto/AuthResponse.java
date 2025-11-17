package com.lifestratproj.app.dto;

public class AuthResponse {
    private String message;
    private Long userId;
    private String username;
    private String email;
    private String token;

    public AuthResponse() {}
    
    public AuthResponse(String message, Long userId, String username, String email, String token) {
        this.message = message;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.token = token;
    }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}