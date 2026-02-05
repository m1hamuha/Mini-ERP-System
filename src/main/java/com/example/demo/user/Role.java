package com.example.demo.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Role entity representing user roles with permissions.
 */
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
    @Column(unique = true, nullable = false)
    private String name;

    @NotBlank(message = "Role description is required")
    @Column(nullable = false)
    private String description;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    /**
     * Common role names as constants.
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_MANAGER = "ROLE_MANAGER";
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_GUEST = "ROLE_GUEST";

    /**
     * Check if this role is a default system role.
     * 
     * @return true if this is a default role, false otherwise
     */
    public boolean isDefaultRole() {
        return isDefault;
    }

    /**
     * Check if this role has admin privileges.
     * 
     * @return true if this role is ADMIN, false otherwise
     */
    public boolean isAdminRole() {
        return ROLE_ADMIN.equals(this.name);
    }

    /**
     * Check if this role has manager privileges.
     * 
     * @return true if this role is MANAGER, false otherwise
     */
    public boolean isManagerRole() {
        return ROLE_MANAGER.equals(this.name);
    }

    /**
     * Check if this role has user privileges.
     * 
     * @return true if this role is USER, false otherwise
     */
    public boolean isUserRole() {
        return ROLE_USER.equals(this.name);
    }
}