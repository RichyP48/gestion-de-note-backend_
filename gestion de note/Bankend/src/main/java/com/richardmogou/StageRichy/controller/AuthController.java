package com.richardmogou.StageRichy.controller; // Standard package

import com.richardmogou.StageRichy.dto.JwtResponse;
import com.richardmogou.StageRichy.dto.LoginRequest;
import com.richardmogou.StageRichy.model.User;
import com.richardmogou.StageRichy.security.JwtUtils;
import com.richardmogou.StageRichy.service.UserDetailsServiceImpl;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600) 
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    
    
    @PostMapping("/signin")
public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    String identifier = loginRequest.getUsername();
    String password = loginRequest.getPassword();

    // Vérifie si l'identifiant ressemble à un email
    boolean isEmail = identifier.contains("@");

    // Charge l'utilisateur selon le type d'identifiant
    UserDetails userDetails;
    if (isEmail) {
        userDetails = userDetailsService.loadUserByEmail(identifier);
    } else {
        userDetails = userDetailsService.loadUserByUsername(identifier);
    }

    // Authentifie avec Spring Security
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(userDetails.getUsername(), password));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    String jwt = jwtUtils.generateJwtToken(authentication);
    User user = (User) authentication.getPrincipal();

    List<String> roles = user.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());

    return ResponseEntity.ok(new JwtResponse(jwt,
                                             user.getId(),
                                             user.getUsername(),
                                             user.getEmail(),
                                             roles));
}


    // Placeholder for signup endpoint (can be implemented later)
    /*
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        // ... implementation for user registration ...
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
    */
}