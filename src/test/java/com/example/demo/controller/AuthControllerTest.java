package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RefreshTokenRequest;
import com.example.demo.security.JwtService;
import com.example.demo.user.Role;
import com.example.demo.user.User;
import com.example.demo.user.UserService;
import com.example.demo.user.dto.RegisterRequest;
import com.example.demo.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setId(1L);
        userRole.setName("ROLE_USER");
        userRole.setDescription("Regular user");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encoded_password");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setActive(true);
        testUser.setLocked(false);
        testUser.setRoles(Arrays.asList(userRole));
    }

    @Test
    void register_shouldCreateUserSuccessfully() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");
        request.setPhoneNumber("+1234567890");

        User createdUser = new User();
        createdUser.setId(2L);
        createdUser.setUsername("newuser");
        createdUser.setEmail("new@example.com");
        createdUser.setFirstName("New");
        createdUser.setLastName("User");
        createdUser.setPhoneNumber("+1234567890");
        createdUser.setActive(true);
        createdUser.setLocked(false);
        createdUser.setRoles(Arrays.asList(userRole));

        when(userService.registerUser(any(RegisterRequest.class))).thenReturn(createdUser);

        ResponseEntity<UserResponse> response = authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("newuser", response.getBody().getUsername());
        assertEquals("new@example.com", response.getBody().getEmail());
        verify(userService, times(1)).registerUser(any(RegisterRequest.class));
    }

    @Test
    void login_shouldAuthenticateSuccessfully() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.getAuthorities()).thenReturn(
                (java.util.Collection) Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(testUser)).thenReturn("access_token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("refresh_token");
        when(jwtService.getExpiration()).thenReturn(3600000L);
        doNothing().when(userService).recordSuccessfulLogin("testuser");

        ResponseEntity<LoginResponse> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("access_token", response.getBody().getAccessToken());
        assertEquals("Bearer", response.getBody().getTokenType());
        assertEquals("testuser", response.getBody().getUsername());
        assertTrue(response.getBody().getRoles().contains("ROLE_USER"));
    }

    @Test
    void login_shouldThrowExceptionWhenCredentialsInvalid() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> {
            authController.login(request);
        });
    }

    @Test
    void refresh_shouldReturnNewTokensWhenValid() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid_refresh_token");

        when(jwtService.extractUsername("valid_refresh_token")).thenReturn("testuser");
        when(userService.loadUserByUsername("testuser")).thenReturn(testUser);
        when(jwtService.isTokenValid("valid_refresh_token", testUser)).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn("new_access_token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("new_refresh_token");
        when(jwtService.getExpiration()).thenReturn(3600000L);

        ResponseEntity<LoginResponse> response = authController.refresh(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("new_access_token", response.getBody().getAccessToken());
    }

    @Test
    void refresh_shouldReturnUnauthorizedWhenTokenInvalid() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid_refresh_token");

        when(jwtService.extractUsername("invalid_refresh_token")).thenReturn("testuser");
        when(userService.loadUserByUsername("testuser")).thenReturn(testUser);
        when(jwtService.isTokenValid("invalid_refresh_token", testUser)).thenReturn(false);

        ResponseEntity<LoginResponse> response = authController.refresh(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getCurrentUser_shouldReturnUserInfo() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUser);

        ResponseEntity<UserResponse> response = authController.getCurrentUser(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getUsername());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertEquals("Test", response.getBody().getFirstName());
        assertEquals("User", response.getBody().getLastName());
    }

    @Test
    void register_shouldIncludeRolesInResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("adminuser");
        request.setEmail("admin@example.com");
        request.setPassword("adminpass123");
        request.setFirstName("Admin");
        request.setLastName("User");

        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName("ROLE_ADMIN");

        User adminUser = new User();
        adminUser.setId(3L);
        adminUser.setUsername("adminuser");
        adminUser.setEmail("admin@example.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setActive(true);
        adminUser.setRoles(Arrays.asList(adminRole));

        when(userService.registerUser(any(RegisterRequest.class))).thenReturn(adminUser);

        ResponseEntity<UserResponse> response = authController.register(request);

        assertNotNull(response.getBody());
        assertTrue(response.getBody().getRoles().contains("ROLE_ADMIN"));
    }

    @Test
    void login_shouldIncludeAllRolesInResponse() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        testUser.setRoles(Arrays.asList(userRole, adminRole));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.getAuthorities()).thenReturn(
                (java.util.Collection) Arrays.asList(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                )
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(testUser)).thenReturn("access_token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("refresh_token");
        when(jwtService.getExpiration()).thenReturn(3600000L);
        doNothing().when(userService).recordSuccessfulLogin("testuser");

        ResponseEntity<LoginResponse> response = authController.login(request);

        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getRoles().size());
        assertTrue(response.getBody().getRoles().contains("ROLE_USER"));
        assertTrue(response.getBody().getRoles().contains("ROLE_ADMIN"));
    }

    @Test
    void getCurrentUser_shouldReturnCompleteUserDetails() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUser);

        ResponseEntity<UserResponse> response = authController.getCurrentUser(authentication);

        UserResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(1L, body.getId());
        assertEquals("testuser", body.getUsername());
        assertEquals("Test User", body.getFullName());
        assertNotNull(body.getRoles());
        assertFalse(body.isLocked());
        assertTrue(body.isActive());
    }
}
