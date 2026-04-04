package com.example.task_manager_api.service;

import com.example.task_manager_api.dto.user.LoginUserDto;
import com.example.task_manager_api.dto.user.RegisterUserDto;
import com.example.task_manager_api.entity.User;
import com.example.task_manager_api.exception.DataConflictException;
import com.example.task_manager_api.exception.ResourceNotFoundException;
import com.example.task_manager_api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    // ========================
    // signup
    // ========================

    @Test
    void signup_shouldReturnSavedUser() {
        // GIVEN
        RegisterUserDto dto = new RegisterUserDto("test@test.com", "password123", "Test User");
        User saved = User.builder()
                .id(1L)
                .email("test@test.com")
                .fullName("Test User")
                .password("encoded-password")
                .build();
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        // WHEN
        User result = authenticationService.signup(dto);

        // THEN
        assertThat(result.getEmail()).isEqualTo("test@test.com");
        assertThat(result.getFullName()).isEqualTo("Test User");
    }

    @Test
    void signup_shouldThrowException_whenEmailAlreadyExists() {
        // GIVEN
        RegisterUserDto dto = new RegisterUserDto("test@test.com", "password123", "Test User");
        when(passwordEncoder.encode(any())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class)))
                .thenThrow(DataIntegrityViolationException.class);

        // THEN
        assertThatThrownBy(() -> authenticationService.signup(dto))
                .isInstanceOf(DataConflictException.class)
                .hasMessageContaining("Email already in use");
    }

    // ========================
    // authenticate
    // ========================

    @Test
    void authenticate_shouldReturnUser_whenValidCredentials() {
        // GIVEN
        LoginUserDto dto = new LoginUserDto("test@test.com", "password123");
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        // WHEN
        User result = authenticationService.authenticate(dto);

        // THEN
        assertThat(result.getEmail()).isEqualTo("test@test.com");
        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    void authenticate_shouldThrowException_whenUserNotFound() {
        // GIVEN
        LoginUserDto dto = new LoginUserDto("noexiste@test.com", "password123");
        when(userRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        // THEN
        assertThatThrownBy(() -> authenticationService.authenticate(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void authenticate_shouldThrowException_whenBadCredentials() {
        // GIVEN
        LoginUserDto dto = new LoginUserDto("test@test.com", "wrong-password");
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        // THEN
        assertThatThrownBy(() -> authenticationService.authenticate(dto))
                .isInstanceOf(BadCredentialsException.class);
    }
}