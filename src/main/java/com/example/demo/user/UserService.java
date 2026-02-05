package com.example.demo.user;

import com.example.demo.user.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public User registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .phoneNumber(registerRequest.getPhoneNumber())
                .isActive(true)
                .isLocked(false)
                .failedLoginAttempts(0)
                .roles(List.of(getDefaultRole()))
                .build();

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User updateUser(Long userId, UpdateUserRequest updateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updateRequest.getFirstName() != null) {
            user.setFirstName(updateRequest.getFirstName());
        }
        if (updateRequest.getLastName() != null) {
            user.setLastName(updateRequest.getLastName());
        }
        if (updateRequest.getPhoneNumber() != null) {
            user.setPhoneNumber(updateRequest.getPhoneNumber());
        }
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateRequest.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(updateRequest.getEmail());
        }

        return userRepository.save(user);
    }

    public void updatePassword(Long userId, UpdatePasswordRequest passwordRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(passwordRequest.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
        userRepository.save(user);
    }

    public User updateUserRoles(Long userId, List<Long> roleIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Role> roles = roleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new RuntimeException("One or more roles not found");
        }

        user.setRoles(roles);
        return userRepository.save(user);
    }

    public User setUserLockStatus(Long userId, boolean isLocked) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setLocked(isLocked);
        if (!isLocked) {
            user.setFailedLoginAttempts(0);
        }

        return userRepository.save(user);
    }

    public User setUserActiveStatus(Long userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(isActive);
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(userId);
    }

    public void recordSuccessfulLogin(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.resetFailedLoginAttempts();
        user.updateLastLogin();
        userRepository.save(user);
    }

    public void recordFailedLoginAttempt(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.increaseFailedLoginAttempts();
        userRepository.save(user);
    }

    private Role getDefaultRole() {
        return roleRepository.findByName(Role.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default role not found"));
    }

    public void initializeDefaultRoles() {
        if (!roleRepository.existsByName(Role.ROLE_ADMIN)) {
            Role adminRole = Role.builder()
                    .name(Role.ROLE_ADMIN)
                    .description("Administrator with full access")
                    .isDefault(true)
                    .build();
            roleRepository.save(adminRole);
        }

        if (!roleRepository.existsByName(Role.ROLE_MANAGER)) {
            Role managerRole = Role.builder()
                    .name(Role.ROLE_MANAGER)
                    .description("Manager with limited administrative access")
                    .isDefault(true)
                    .build();
            roleRepository.save(managerRole);
        }

        if (!roleRepository.existsByName(Role.ROLE_USER)) {
            Role userRole = Role.builder()
                    .name(Role.ROLE_USER)
                    .description("Regular user with basic access")
                    .isDefault(true)
                    .build();
            roleRepository.save(userRole);
        }

        if (!roleRepository.existsByName(Role.ROLE_GUEST)) {
            Role guestRole = Role.builder()
                    .name(Role.ROLE_GUEST)
                    .description("Guest with read-only access")
                    .isDefault(true)
                    .build();
            roleRepository.save(guestRole);
        }
    }

    public void createInitialAdminUser() {
        if (userRepository.count() == 0) {
            Role adminRole = roleRepository.findByName(Role.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            User adminUser = User.builder()
                    .username("admin")
                    .email("admin@altenburg-erp.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("System")
                    .lastName("Administrator")
                    .phoneNumber("+49123456789")
                    .isActive(true)
                    .isLocked(false)
                    .failedLoginAttempts(0)
                    .roles(List.of(adminRole))
                    .build();

            userRepository.save(adminUser);
        }
    }
}
