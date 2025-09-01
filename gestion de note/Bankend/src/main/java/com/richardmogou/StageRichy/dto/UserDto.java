package com.richardmogou.StageRichy.dto; // Standard package

import com.richardmogou.StageRichy.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    // Add other common fields if needed, e.g., isEnabled() status from UserDetails
    // private boolean enabled;

}