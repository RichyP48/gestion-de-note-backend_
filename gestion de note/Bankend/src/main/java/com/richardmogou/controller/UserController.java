package com.richardmogou.controller;

import com.richardmogou.dto.MessageResponse;
import com.richardmogou.dto.PasswordChangeRequestDto;
import com.richardmogou.dto.UserDto;
import com.richardmogou.dto.UserUpdateRequestDto;
import com.richardmogou.model.User;
import com.richardmogou.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Profile", description = "Operations for managing user profiles")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * Helper method to get the current authenticated user's ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            User userPrincipal = (User) authentication.getPrincipal();
            return userPrincipal.getId();
        }
        return null;
    }

    @Operation(summary = "Get current user profile", description = "Returns the profile information of the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
    })
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Error: Not authenticated"));
        }

        logger.info("Fetching profile for user ID: {}", userId);
        
        Optional<UserDto> userDto = userService.findUserById(userId);
        if (userDto.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Error: User not found"));
        }
        
        return ResponseEntity.ok(userDto.get());
    }

    @Operation(summary = "Get user profile by ID", description = "Returns the profile information of a user by their ID. Only accessible by admins or the user themselves.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - not authorized to view this profile",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<?> getUserProfile(
            @Parameter(description = "ID of the user to get profile for", required = true)
            @PathVariable Long id) {
        
        logger.info("Fetching profile for user ID: {}", id);
        
        Optional<UserDto> userDto = userService.findUserById(id);
        if (userDto.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Error: User not found"));
        }
        
        return ResponseEntity.ok(userDto.get());
    }

    @Operation(summary = "Update user profile", description = "Updates the profile information of the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - not authorized to update this profile",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
    })
    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<?> updateUserProfile(
            @Parameter(description = "ID of the user to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated user information", required = true)
            @Valid @RequestBody UserUpdateRequestDto updateDto) {
        
        logger.info("Updating profile for user ID: {}", id);
        
        try {
            Optional<UserDto> updatedUser = userService.updateUser(id, updateDto);
            if (updatedUser.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("Error: User not found"));
            }
            
            return ResponseEntity.ok(updatedUser.get());
        } catch (IllegalArgumentException e) {
            logger.warn("Error updating user: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error updating user: " + e.getMessage()));
        }
    }

    @Operation(summary = "Change password", description = "Changes the password of the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data or passwords don't match",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - not authorized to change this user's password",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
    })
    @PostMapping("/{id}/change-password")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<?> changePassword(
            @Parameter(description = "ID of the user to change password for", required = true)
            @PathVariable Long id,
            @Parameter(description = "Password change details", required = true)
            @Valid @RequestBody PasswordChangeRequestDto passwordChangeDto) {
        
        logger.info("Changing password for user ID: {}", id);
        
        // Validate that new password and confirm password match
        if (!passwordChangeDto.getNewPassword().equals(passwordChangeDto.getConfirmPassword())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Error: New password and confirm password do not match"));
        }
        
        boolean success = userService.changePassword(
                id, 
                passwordChangeDto.getCurrentPassword(), 
                passwordChangeDto.getNewPassword()
        );
        
        if (!success) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Error: Current password is incorrect or user not found"));
        }
        
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }
}
