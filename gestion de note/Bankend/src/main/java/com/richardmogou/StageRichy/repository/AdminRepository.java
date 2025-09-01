package com.richardmogou.StageRichy.repository; // Standard package

import com.richardmogou.StageRichy.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    // Find an admin by their username (inherited from User)
    Optional<Admin> findByUsername(String username);

    // Find an admin by their email (inherited from User)
    Optional<Admin> findByEmail(String email);

    // Add other admin-specific query methods here if needed
}