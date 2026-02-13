package com.example.demo.controller;

import com.example.demo.dto.PagedResponse;
import com.example.demo.user.Role;
import com.example.demo.user.User;
import com.example.demo.user.UserService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setId(1L);
        userRole.setName("ROLE_USER");
        userRole.setDescription("Regular user");

        adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName("ROLE_ADMIN");
        adminRole.setDescription("Administrator");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPhoneNumber("+1234567890");
        testUser.setActive(true);
        testUser.setLocked(false);
        testUser.setRoles(Arrays.asList(userRole));
    }

    @Test
    void getAllUsers_shouldReturnPagedResponse() {
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 20), 1);
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        ResponseEntity<PagedResponse<UserResponse>> response = userController.getAllUsers(0, 20, "id", "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(1, response.getBody().getTotalElements());
        verify(userService, times(1)).getAllUsers(any(Pageable.class));
    }

    @Test
    void getUserById_shouldReturnUserWhenExists() {
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));

        ResponseEntity<UserResponse> response = userController.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getUsername());
        assertEquals("test@example.com", response.getBody().getEmail());
    }

    @Test
    void getUserById_shouldThrowExceptionWhenNotExists() {
        when(userService.getUserById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            userController.getUserById(99L);
        });
    }

    @Test
    void updateUser_shouldUpdateSuccessfully() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Updated");
        request.setLastName("Name");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setEmail("test@example.com");
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("Name");
        updatedUser.setActive(true);
        updatedUser.setLocked(false);
        updatedUser.setRoles(Arrays.asList(userRole));

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(updatedUser);

        ResponseEntity<UserResponse> response = userController.updateUser(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated", response.getBody().getFirstName());
        assertEquals("Name", response.getBody().getLastName());
    }

    @Test
    void deleteUser_shouldReturnNoContent() {
        doNothing().when(userService).deleteUser(1L);

        ResponseEntity<Void> response = userController.deleteUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void updateUserRoles_shouldUpdateSuccessfully() {
        List<Long> roleIds = Arrays.asList(1L, 2L);

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setRoles(Arrays.asList(userRole, adminRole));

        when(userService.updateUserRoles(1L, roleIds)).thenReturn(updatedUser);

        ResponseEntity<UserResponse> response = userController.updateUserRoles(1L, roleIds);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getRoles().size());
    }

    @Test
    void updateUserStatus_shouldActivateUser() {
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setActive(true);

        when(userService.setUserActiveStatus(1L, true)).thenReturn(updatedUser);

        ResponseEntity<UserResponse> response = userController.updateUserStatus(1L, true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isActive());
    }

    @Test
    void lockUser_shouldLockUser() {
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setLocked(true);

        when(userService.setUserLockStatus(1L, true)).thenReturn(updatedUser);

        ResponseEntity<UserResponse> response = userController.lockUser(1L, true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isLocked());
    }

    @Test
    void updatePassword_shouldReturnOk() {
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setCurrentPassword("oldpass");
        request.setNewPassword("newpass123");
        request.setConfirmPassword("newpass123");

        doNothing().when(userService).updatePassword(eq(1L), any(UpdatePasswordRequest.class));

        ResponseEntity<Void> response = userController.updatePassword(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).updatePassword(eq(1L), any(UpdatePasswordRequest.class));
    }

    @Test
    void getAllUsers_withDescendingSort_shouldWork() {
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 20), 1);
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        ResponseEntity<PagedResponse<UserResponse>> response = userController.getAllUsers(0, 20, "username", "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).getAllUsers(any(Pageable.class));
    }

    @Test
    void updateUserRoles_shouldReturnRolesInResponse() {
        List<Long> roleIds = Arrays.asList(2L);

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setEmail("test@example.com");
        updatedUser.setRoles(Arrays.asList(adminRole));

        when(userService.updateUserRoles(1L, roleIds)).thenReturn(updatedUser);

        ResponseEntity<UserResponse> response = userController.updateUserRoles(1L, roleIds);

        assertNotNull(response.getBody());
        assertTrue(response.getBody().getRoles().contains("ROLE_ADMIN"));
    }
}
