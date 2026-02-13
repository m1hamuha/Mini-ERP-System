package com.example.demo.user;

import com.example.demo.user.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder()
                .id(1L)
                .name(Role.ROLE_USER)
                .description("Regular user")
                .build();

        adminRole = Role.builder()
                .id(2L)
                .name(Role.ROLE_ADMIN)
                .description("Administrator")
                .build();

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encoded_password")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+1234567890")
                .isActive(true)
                .isLocked(false)
                .failedLoginAttempts(0)
                .roles(Arrays.asList(userRole))
                .build();
    }

    @Test
    void loadUserByUsername_shouldReturnUserWhenExists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserDetails result = userService.loadUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_shouldThrowExceptionWhenNotExists() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("unknown");
        });
    }

    @Test
    void loadUserByEmail_shouldReturnUserWhenExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        UserDetails result = userService.loadUserByEmail("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", ((User) result).getEmail());
    }

    @Test
    void registerUser_shouldRegisterSuccessfully() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("password123")
                .firstName("New")
                .lastName("User")
                .phoneNumber("+0987654321")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(roleRepository.findByName(Role.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(2L);
            return savedUser;
        });

        User result = userService.registerUser(request);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("encoded_password", result.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_shouldThrowExceptionWhenUsernameExists() {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("new@example.com")
                .password("password123")
                .firstName("New")
                .lastName("User")
                .build();

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            userService.registerUser(request);
        });
    }

    @Test
    void registerUser_shouldThrowExceptionWhenEmailExists() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("test@example.com")
                .password("password123")
                .firstName("New")
                .lastName("User")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            userService.registerUser(request);
        });
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));

        List<User> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
    }

    @Test
    void getAllUsers_withPagination_shouldReturnPagedUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(Arrays.asList(testUser), pageable, 1);
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<User> result = userService.getAllUsers(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("testuser", result.getContent().get(0).getUsername());
    }

    @Test
    void getUserById_shouldReturnUserWhenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void getUserById_shouldReturnEmptyWhenNotExists() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void updateUser_shouldUpdateSuccessfully() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName("Updated")
                .lastName("Name")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateUser(1L, request);

        assertNotNull(result);
        assertEquals("Updated", result.getFirstName());
        assertEquals("Name", result.getLastName());
    }

    @Test
    void updateUser_shouldUpdateEmailWhenChanged() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .email("newemail@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateUser(1L, request);

        assertNotNull(result);
    }

    @Test
    void updateUser_shouldThrowExceptionWhenEmailExists() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .email("existing@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            userService.updateUser(1L, request);
        });
    }

    @Test
    void updatePassword_shouldUpdateSuccessfully() {
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .currentPassword("oldpassword")
                .newPassword("newpassword123")
                .confirmPassword("newpassword123")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldpassword", "encoded_password")).thenReturn(true);
        when(passwordEncoder.encode("newpassword123")).thenReturn("new_encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        assertDoesNotThrow(() -> userService.updatePassword(1L, request));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updatePassword_shouldThrowExceptionWhenCurrentPasswordIncorrect() {
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .currentPassword("wrongpassword")
                .newPassword("newpassword123")
                .confirmPassword("newpassword123")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encoded_password")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            userService.updatePassword(1L, request);
        });
    }

    @Test
    void updateUserRoles_shouldUpdateSuccessfully() {
        List<Long> roleIds = Arrays.asList(1L, 2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findAllById(roleIds)).thenReturn(Arrays.asList(userRole, adminRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateUserRoles(1L, roleIds);

        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserRoles_shouldThrowExceptionWhenRoleNotFound() {
        List<Long> roleIds = Arrays.asList(1L, 99L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findAllById(roleIds)).thenReturn(Arrays.asList(userRole));

        assertThrows(RuntimeException.class, () -> {
            userService.updateUserRoles(1L, roleIds);
        });
    }

    @Test
    void setUserLockStatus_shouldLockUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.setUserLockStatus(1L, true);

        assertTrue(result.isLocked());
    }

    @Test
    void setUserLockStatus_shouldUnlockUser() {
        testUser.setLocked(true);
        testUser.setFailedLoginAttempts(5);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.setUserLockStatus(1L, false);

        assertFalse(result.isLocked());
        assertEquals(0, result.getFailedLoginAttempts());
    }

    @Test
    void setUserActiveStatus_shouldDeactivateUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.setUserActiveStatus(1L, false);

        assertFalse(result.isActive());
    }

    @Test
    void deleteUser_shouldDeleteSuccessfully() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_shouldThrowExceptionWhenNotExists() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            userService.deleteUser(99L);
        });
    }

    @Test
    void recordSuccessfulLogin_shouldResetAttemptsAndUpdateLastLogin() {
        testUser.setFailedLoginAttempts(3);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.recordSuccessfulLogin("testuser");

        assertEquals(0, testUser.getFailedLoginAttempts());
        assertNotNull(testUser.getLastLogin());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void recordFailedLoginAttempt_shouldIncreaseAttempts() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.recordFailedLoginAttempt("testuser");

        assertEquals(1, testUser.getFailedLoginAttempts());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void recordFailedLoginAttempt_shouldLockUserAfter5Attempts() {
        testUser.setFailedLoginAttempts(4);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.recordFailedLoginAttempt("testuser");

        assertEquals(5, testUser.getFailedLoginAttempts());
        assertTrue(testUser.isLocked());
    }

    @Test
    void initializeDefaultRoles_shouldCreateAllDefaultRoles() {
        when(roleRepository.existsByName(Role.ROLE_ADMIN)).thenReturn(false);
        when(roleRepository.existsByName(Role.ROLE_MANAGER)).thenReturn(false);
        when(roleRepository.existsByName(Role.ROLE_USER)).thenReturn(false);
        when(roleRepository.existsByName(Role.ROLE_GUEST)).thenReturn(false);

        userService.initializeDefaultRoles();

        verify(roleRepository, times(4)).save(any(Role.class));
    }

    @Test
    void initializeDefaultRoles_shouldNotCreateExistingRoles() {
        when(roleRepository.existsByName(Role.ROLE_ADMIN)).thenReturn(true);
        when(roleRepository.existsByName(Role.ROLE_MANAGER)).thenReturn(true);
        when(roleRepository.existsByName(Role.ROLE_USER)).thenReturn(true);
        when(roleRepository.existsByName(Role.ROLE_GUEST)).thenReturn(true);

        userService.initializeDefaultRoles();

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void createInitialAdminUser_shouldCreateAdminWhenNoUsers() {
        when(userRepository.count()).thenReturn(0L);
        when(roleRepository.findByName(Role.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(passwordEncoder.encode("admin123")).thenReturn("encoded_admin_pass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.createInitialAdminUser();

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createInitialAdminUser_shouldNotCreateWhenUsersExist() {
        when(userRepository.count()).thenReturn(1L);

        userService.createInitialAdminUser();

        verify(userRepository, never()).save(any(User.class));
    }
}
