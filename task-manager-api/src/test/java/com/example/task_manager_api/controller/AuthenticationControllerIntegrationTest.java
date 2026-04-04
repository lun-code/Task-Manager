package com.example.task_manager_api.controller;

import com.example.task_manager_api.BaseIntegrationTest;
import com.example.task_manager_api.dto.user.LoginUserDto;
import com.example.task_manager_api.dto.user.RegisterUserDto;
import com.example.task_manager_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    // ========================
    // POST /api/auth/signup
    // ========================

    @Test
    void signup_shouldReturn201AndPersistUser() throws Exception {
        RegisterUserDto dto = new RegisterUserDto("test@test.com", "password123", "Test User");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"))
                .andExpect(jsonPath("$.id").isNumber());

        assertThat(userRepository.findByEmail("test@test.com")).isPresent();
    }

    @Test
    void signup_shouldReturn409_whenEmailAlreadyExists() throws Exception {
        RegisterUserDto dto = new RegisterUserDto("test@test.com", "password123", "Test User");

        // Primer registro
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // Segundo registro con el mismo email
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void signup_shouldReturn400_whenEmailIsInvalid() throws Exception {
        RegisterUserDto dto = new RegisterUserDto("not-an-email", "password123", "Test User");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ========================
    // POST /api/auth/login
    // ========================

    @Test
    void login_shouldReturn200AndToken() throws Exception {
        // Primero registramos el usuario
        RegisterUserDto registerDto = new RegisterUserDto("test@test.com", "password123", "Test User");
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated());

        // Luego hacemos login
        LoginUserDto loginDto = new LoginUserDto("test@test.com", "password123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    void login_shouldReturn401_whenWrongPassword() throws Exception {
        RegisterUserDto registerDto = new RegisterUserDto("test@test.com", "password123", "Test User");
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated());

        LoginUserDto loginDto = new LoginUserDto("test@test.com", "wrong-password");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized());
    }

    // ========================
    // GET /api/auth/me
    // ========================

    @Test
    void me_shouldReturn200WithUserData() throws Exception {
        // Registramos y hacemos login para obtener el token
        RegisterUserDto registerDto = new RegisterUserDto("test@test.com", "password123", "Test User");
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginUserDto("test@test.com", "password123"))))
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("token").asText();

        // Usamos el token para acceder a /me
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"));
    }

    @Test
    void me_shouldReturn403_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isForbidden());
    }
}