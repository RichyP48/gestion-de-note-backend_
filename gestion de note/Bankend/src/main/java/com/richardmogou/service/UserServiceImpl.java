package com.richardmogou.service; // Standard package

import com.richardmogou.dto.UserCreateRequestDto;
import com.richardmogou.dto.UserDto;
import com.richardmogou.dto.UserUpdateRequestDto;

import com.richardmogou.model.*;
import com.richardmogou.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- Helper Method for Mapping ---
    private UserDto mapToDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole()
                // user.isEnabled() // Add if enabled status is included in UserDto
        );
    }

    // --- Service Method Implementations ---

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAllUsers(Role role) {
        List<User> users;
        if (role != null) {
            // Filter by role - Note: This might be inefficient for large datasets.
            // Consider adding a dedicated repository method like findByRole(Role role) if needed.
            users = userRepository.findAll().stream()
                    .filter(user -> user.getRole() == role)
                    .collect(Collectors.toList());
        } else {
            users = userRepository.findAll();
        }
        return users.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findUserById(Long id) {
        return userRepository.findById(id).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findUserByUsername(String username) {
        return userRepository.findByUsername(username).map(this::mapToDto);
    }

    @Override
    @Transactional
    public UserDto createUser(UserCreateRequestDto userCreateDto) {
        // Check for existing username/email
        if (userRepository.existsByUsername(userCreateDto.getUsername())) {
            throw new IllegalArgumentException("Error: Username is already taken!");
        }
        if (userRepository.existsByEmail(userCreateDto.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        // Encode password
        String encodedPassword = passwordEncoder.encode(userCreateDto.getPassword());

        // Create the appropriate user type based on role
        User newUser;
        switch (userCreateDto.getRole()) {
            case STUDENT:
                newUser = new Student(
                        userCreateDto.getUsername(),
                        encodedPassword,
                        userCreateDto.getFirstName(),
                        userCreateDto.getLastName(),
                        userCreateDto.getEmail()
                );
                break;
            case TEACHER:
                newUser = new Teacher(
                        userCreateDto.getUsername(),
                        encodedPassword,
                        userCreateDto.getFirstName(),
                        userCreateDto.getLastName(),
                        userCreateDto.getEmail()
                );
                break;
            case ADMIN:
                newUser = new Admin(
                        userCreateDto.getUsername(),
                        encodedPassword,
                        userCreateDto.getFirstName(),
                        userCreateDto.getLastName(),
                        userCreateDto.getEmail()
                );
                break;
            default:
                // Should not happen if validation is correct, but handle defensively
                throw new IllegalArgumentException("Error: Invalid role specified.");
        }

        User savedUser = userRepository.save(newUser);
        return mapToDto(savedUser);
    }

    @Override
    @Transactional
    public Optional<UserDto> updateUser(Long id, UserUpdateRequestDto userUpdateDto) {
        Optional<User> existingUserOptional = userRepository.findById(id);
        if (existingUserOptional.isEmpty()) {
            return Optional.empty(); // User not found
        }

        User existingUser = existingUserOptional.get();

        // Check for email conflict if email is being changed
        if (!existingUser.getEmail().equalsIgnoreCase(userUpdateDto.getEmail())) {
            if (userRepository.existsByEmail(userUpdateDto.getEmail())) {
                throw new IllegalArgumentException("Error: Email is already in use by another account!");
            }
            existingUser.setEmail(userUpdateDto.getEmail());
        }

        // Update other fields
        existingUser.setFirstName(userUpdateDto.getFirstName());
        existingUser.setLastName(userUpdateDto.getLastName());

        // Update enabled status if included in DTO
        // if (userUpdateDto.getEnabled() != null) {
        //     existingUser.setEnabled(userUpdateDto.getEnabled());
        // }

        User updatedUser = userRepository.save(existingUser);
        return Optional.of(mapToDto(updatedUser));
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            // Consider adding checks: e.g., cannot delete currently logged-in admin?
            // Cannot delete users with associated data (grades, etc.)?
            // For now, simple deletion.
            userRepository.deleteById(id);
            return true;
        }
        return false; // User not found
    }
    
    @Override
    @Transactional
    public boolean changePassword(Long id, String currentPassword, String newPassword) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            return false; // User not found
        }
        
        User user = userOptional.get();
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false; // Current password is incorrect
        }
        
        // Encode and set new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return true;
    }
}