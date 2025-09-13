package com.richardmogou.service; // Standard package

import com.richardmogou.dto.UserCreateRequestDto;
import com.richardmogou.dto.UserDto;
import com.richardmogou.dto.UserUpdateRequestDto;
import com.richardmogou.model.Role;

import java.util.List;
import java.util.Optional;

public interface UserService {

    /**
     * Retrieves all users, optionally filtered by role.
     * @param role Optional role to filter by. If null, returns all users.
     * @return A list of User DTOs.
     */
    List<UserDto> findAllUsers(Role role);

    /**
     * Finds a user by their ID.
     * @param id The ID of the user.
     * @return An Optional containing the User DTO if found, otherwise empty.
     */
    Optional<UserDto> findUserById(Long id);

    /**
     * Finds a user by their username.
     * @param username The username of the user.
     * @return An Optional containing the User DTO if found, otherwise empty.
     */
    Optional<UserDto> findUserByUsername(String username);

    /**
     * Creates a new user (Student, Teacher, or Admin).
     * The password will be encoded before saving.
     * @param userCreateDto DTO containing the details for the new user.
     * @return The created User DTO.
     * @throws IllegalArgumentException if username or email already exists.
     */
    UserDto createUser(UserCreateRequestDto userCreateDto);

    /**
     * Updates an existing user's details (excluding username and password).
     * @param id The ID of the user to update.
     * @param userUpdateDto DTO containing the updated details.
     * @return An Optional containing the updated User DTO if found and updated, otherwise empty.
     * @throws IllegalArgumentException if the updated email conflicts with another user.
     */
    Optional<UserDto> updateUser(Long id, UserUpdateRequestDto userUpdateDto);

    /**
     * Deletes a user by their ID.
     * @param id The ID of the user to delete.
     * @return true if the user was found and deleted, false otherwise.
     */
    boolean deleteUser(Long id);

    /**
     * Changes a user's password after verifying the current password.
     * @param id The ID of the user.
     * @param currentPassword The user's current password (for verification).
     * @param newPassword The new password to set.
     * @return true if the password was successfully changed, false if the user was not found or current password is incorrect.
     */
    boolean changePassword(Long id, String currentPassword, String newPassword);

    // Consider adding methods for enabling/disabling users if needed later.
}