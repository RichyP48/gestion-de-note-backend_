package com.richardmogou.controller; // Standard package

import com.richardmogou.dto.MessageResponse;
import com.richardmogou.dto.UserCreateRequestDto;
import com.richardmogou.dto.UserDto;
import com.richardmogou.dto.UserUpdateRequestDto;
import com.richardmogou.model.Role;
import com.richardmogou.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600) // Adjust for production
@RestController
@RequestMapping("/api/admin/users") // Base path for user admin operations
@PreAuthorize("hasRole('ADMIN')") // Secure all endpoints for ADMIN role
public class AdminUserController {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);

    @Autowired
    private UserService userService;

    // GET /api/admin/users - Retrieve all users (optional filter by role)
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers(@RequestParam(required = false) Role role) {
        List<UserDto> users = userService.findAllUsers(role);
        return ResponseEntity.ok(users);
    }

    // GET /api/admin/users/{id} - Retrieve a single user by ID
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return userService.findUserById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id);
                });
    }

     // GET /api/admin/users/username/{username} - Retrieve a single user by username
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        return userService.findUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> {
                     logger.warn("User not found with username: {}", username);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with username: " + username);
                });
    }

    // POST /api/admin/users - Create a new user
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateRequestDto userCreateDto) {
        try {
            UserDto createdUser = userService.createUser(userCreateDto);
            logger.info("Created user: {}", createdUser.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to create user, conflict: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT) // Use 409 for username/email conflicts
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating user: {}", userCreateDto.getUsername(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error creating user: " + e.getMessage()));
        }
    }

    // PUT /api/admin/users/{id} - Update an existing user
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequestDto userUpdateDto) {
         try {
            return userService.updateUser(id, userUpdateDto)
                    .map(updatedUser -> {
                        logger.info("Updated user with ID: {}", id);
                        return ResponseEntity.ok(updatedUser);
                    })
                    .orElseThrow(() -> {
                         logger.warn("User not found for update with ID: {}", id);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id);
                    });
        } catch (IllegalArgumentException e) {
             logger.warn("Failed to update user {}, conflict: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT) // Use 409 for email conflicts
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
             logger.error("Error updating user with ID: {}", id, e);
             return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error updating user: " + e.getMessage()));
        }
    }

    // DELETE /api/admin/users/{id} - Delete a user
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long id) {
        // Add check to prevent self-deletion?
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // User currentUser = (User) auth.getPrincipal();
        // if (currentUser.getId().equals(id)) {
        //     logger.warn("Admin user {} attempted to delete their own account.", currentUser.getUsername());
        //     return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse("Error: Cannot delete your own account."));
        // }

        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            logger.info("Deleted user with ID: {}", id);
            return ResponseEntity.ok(new MessageResponse("User deleted successfully!"));
        } else {
            logger.warn("User not found for deletion with ID: {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id);
        }
    }
}