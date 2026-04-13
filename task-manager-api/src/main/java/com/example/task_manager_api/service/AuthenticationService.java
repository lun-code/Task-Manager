package com.example.task_manager_api.service;

import com.example.task_manager_api.dto.user.LoginUserDto;
import com.example.task_manager_api.dto.user.RegisterUserDto;
import com.example.task_manager_api.entity.User;
import com.example.task_manager_api.exception.DataConflictException;
import com.example.task_manager_api.exception.ResourceNotFoundException;
import com.example.task_manager_api.repository.UserRepository;
import com.resend.core.exception.ResendException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final EmailService emailService;

    public User signup(RegisterUserDto input) {
        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .fullName(input.fullName())
                .email(input.email())
                .password(passwordEncoder.encode(input.password()))
                .verified(false)
                .verificationToken(verificationToken)
                .build();

        try {
            User savedUser = userRepository.save(user);
            emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken);
            return savedUser;
        } catch (DataIntegrityViolationException e) {
            throw new DataConflictException("Email already in use");
        }
    }

    public User authenticate(LoginUserDto input) {
        User user = userRepository.findByEmail(input.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isVerified()) {
            throw new BadCredentialsException("Account not verified. Please check your email.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.email(),
                        input.password()
                )
        );

        return user;
    }

    public void verifyAccount(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid verification token"));

        user.setVerified(true);
        user.setVerificationToken(null);

        userRepository.save(user);
    }
}