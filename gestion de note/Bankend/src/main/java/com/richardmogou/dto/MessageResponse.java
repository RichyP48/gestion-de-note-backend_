package com.richardmogou.dto; // Standard package

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Lombok for getters/setters
@NoArgsConstructor // Lombok for no-args constructor
@AllArgsConstructor // Lombok for constructor with all args
public class MessageResponse {
    private String message;
}