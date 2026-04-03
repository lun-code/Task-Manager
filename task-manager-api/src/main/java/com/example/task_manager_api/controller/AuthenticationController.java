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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}