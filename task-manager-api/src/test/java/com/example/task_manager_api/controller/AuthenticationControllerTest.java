package com.example.task_manager_api.controller;

import com.example.task_manager_api.config.TestSecurityConfig;
import com.example.task_manager_api.dto.user.LoginUserDto;
import com.example.task_manager_api.dto.user.RegisterUserDto;
import com.example.task_manager_api.entity.User;
import com.example.task_manager_api.exception.DataConflictException;
import com.example.task_manager_api.service.AuthenticationService;
import com.example.task_manager_api.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@Import(TestSecurityConfig.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtService jwtService;

    // ========================
    // POST /api/auth/signup
    // ========================

    @Test
    void signup_shouldReturn201_whenValid() throws Exception {
        // GIVEN
        RegisterUserDto dto = new RegisterUserDto("test@test.com", "password123", "Test User");
        User saved = User.builder()
                .id(1L)
                .email("test@test.com")
                .fullName("Test User")
                .createdAt(LocalDateTime.now())
                .build();
        when(authenticationService.signup(any(RegisterUserDto.class))).thenReturn(saved);

        // WHEN + THEN
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void signup_shouldReturn400_whenEmailIsBlank() throws Exception {
        // GIVEN
        RegisterUserDto dto = new RegisterUserDto("", "password123", "Test User");

        // WHEN + THEN
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_shouldReturn400_whenPasswordTooShort() throws Exception {
        // GIVEN
        RegisterUserDto dto = new RegisterUserDto("test@test.com", "123", "Test User");

        // WHEN + THEN
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_shouldReturn409_whenEmailAlreadyExists() throws Exception {
        // GIVEN
        RegisterUserDto dto = new RegisterUserDto("test@test.com", "password123", "Test User");
        when(authenticationService.signup(any(RegisterUserDto.class)))
                .thenThrow(new DataConflictException("Email already in use"));

        // WHEN + THEN
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    // ========================
    // POST /api/auth/login
    // ========================

    @Test
    void login_shouldReturn200_whenValidCredentials() throws Exception {
        // GIVEN
        LoginUserDto dto = new LoginUserDto("test@test.com", "password123");
        User user = User.builder().id(1L).email("test@test.com").build();
        when(authenticationService.authenticate(any(LoginUserDto.class))).thenReturn(user);
        when(jwtService.generateToken(any())).thenReturn("mocked-jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(3600000L);

        // WHEN + THEN
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"))
                .andExpect(jsonPath("$.expiresIn").value(3600000));
    }

    @Test
    void login_shouldReturn401_whenBadCredentials() throws Exception {
        // GIVEN
        LoginUserDto dto = new LoginUserDto("test@test.com", "wrong-password");
        when(authenticationService.authenticate(any(LoginUserDto.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // WHEN + THEN
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}