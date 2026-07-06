package com.justeat.service;

import com.justeat.dto.LoginRequest;
import com.justeat.dto.RegisterRequest;
import com.justeat.exception.InvalidCredentialsException;
import com.justeat.exception.UserAlreadyExistsException;
import com.justeat.model.Role;
import com.justeat.model.User;
import com.justeat.repository.UserRepository;
import com.justeat.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User("jane", "jane@example.com", "hashed-password", Role.CUSTOMER);
        existingUser.setId(1L);
    }

    // Test 1: Login with correct credentials -> JWT token returned
    @Test
    void login_withCorrectCredentials_returnsJwtToken() {
        LoginRequest request = new LoginRequest();
        request.setUsername("jane");
        request.setPassword("Password1!");

        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("Password1!", "hashed-password")).thenReturn(true);
        when(jwtUtil.generateToken("jane", "CUSTOMER", 1L)).thenReturn("mock-jwt-token");

        var response = authService.login(request);

        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("jane", response.getUsername());
    }

    // Test 2: Login with wrong password -> exception thrown
    @Test
    void login_withWrongPassword_throwsInvalidCredentialsException() {
        LoginRequest request = new LoginRequest();
        request.setUsername("jane");
        request.setPassword("wrong-password");

        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    // Test 3: Register with duplicate email -> UserAlreadyExistsException thrown
    @Test
    void register_withDuplicateEmail_throwsUserAlreadyExistsException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("jane@example.com");
        request.setPassword("Password1!");
        request.setRole("CUSTOMER");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }
}
