package com.example.task_manager_api.controller;

import com.example.task_manager_api.dto.user.LoginResponse;
import com.example.task_manager_api.dto.user.LoginUserDto;
import com.example.task_manager_api.dto.user.RegisterUserDto;
import com.example.task_manager_api.dto.user.UserResponseDTO;
import com.example.task_manager_api.entity.User;
import com.example.task_manager_api.service.AuthenticationService;
import com.example.task_manager_api.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private final JwtService jwtService;

    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);

        UserResponseDTO response = new UserResponseDTO(
                registeredUser.getId(),
                registeredUser.getFullName(),
                registeredUser.getEmail(),
                registeredUser.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@Valid @RequestBody LoginUserDto loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponse loginResponse = new LoginResponse(jwtToken, jwtService.getExpirationTime());

        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        UserResponseDTO response = new UserResponseDTO(
                currentUser.getId(),
                currentUser.getFullName(),
                currentUser.getEmail(),
                currentUser.getCreatedAt()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> verifyAccount(@RequestParam String token) {
        authenticationService.verifyAccount(token);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "https://task-manager-nto7.vercel.app/verified")
                .build();
    }
}