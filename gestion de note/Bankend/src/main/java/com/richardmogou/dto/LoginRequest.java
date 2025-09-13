package com.richardmogou.dto; // Standard package

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data // Lombok for getters/setters
public class LoginRequest {

    // @NotBlank(message = "Username cannot be blank")
    // private String username;
    @NotBlank(message = "Username or email ")
    private String username;
    @NotBlank(message = "Password cannot be blank")
    private String password;
}