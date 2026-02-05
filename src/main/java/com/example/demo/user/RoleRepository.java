package com.example.demo.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Role entity operations.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find a role by its name.
     * 
     * @param name the name of the role to find
     * @return Optional containing the role if found
     */
    Optional<Role> findByName(String name);

    /**
     * Check if a role exists by its name.
     * 
     * @param name the name of the role to check
     * @return true if the role exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Find all default roles.
     * 
     * @return List of default roles
     */
    List<Role> findByIsDefaultTrue();
}