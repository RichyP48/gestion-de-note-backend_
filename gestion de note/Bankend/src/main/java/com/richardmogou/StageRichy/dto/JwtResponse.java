package com.richardmogou.StageRichy.dto; // Standard package

import lombok.Data;

import java.util.List;

@Data // Lombok for getters/setters
public class JwtResponse {
    private String token;
    private String type = "Bearer"; // Standard JWT type
    private Long id;
    private String username;
    private String email;
    private List<String> roles;

    public JwtResponse(String accessToken, Long id, String username, String email, List<String> roles) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}