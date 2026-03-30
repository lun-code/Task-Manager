package com.example.task_manager_api.service;

import com.example.task_manager_api.dto.user.LoginUserDto;
import com.example.task_manager_api.dto.user.RegisterUserDto;
import com.example.task_manager_api.entity.User;
import com.example.task_manager_api.exception.DataConflictException;
import com.example.task_manager_api.exception.ResourceNotFoundException;
import com.example.task_manager_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    public User signup(RegisterUserDto input) {
        User user = User.builder()
                .fullName(input.fullName())
                .email(input.email())
                .password(passwordEncoder.encode(input.password()))
                .build();

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new DataConflictException("Email already in use");
        }
    }

    public User authenticate(LoginUserDto input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.email(),
                        input.password()
                )
        );

        return userRepository.findByEmail(input.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}