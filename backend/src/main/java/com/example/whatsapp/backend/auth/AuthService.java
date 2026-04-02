package com.example.whatsapp.backend.auth;

import com.example.whatsapp.backend.auth.dto.AuthRequest;
import com.example.whatsapp.backend.auth.dto.AuthResponse;
import com.example.whatsapp.backend.auth.dto.RegisterRequest;
import com.example.whatsapp.backend.config.JwtService;
import com.example.whatsapp.backend.user.UserEntity;
import com.example.whatsapp.backend.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByPhoneNumber(request.phoneNumber()).isPresent()) {
            throw new ResponseStatusException(BAD_REQUEST, "Phone number already registered");
        }

        UserEntity user = new UserEntity();
        user.setName(request.name());
        user.setPhoneNumber(request.phoneNumber());
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        UserEntity saved = userRepository.save(user);
        String token = jwtService.generate(saved.getId(), saved.getPhoneNumber());

        return new AuthResponse(saved.getId(), saved.getName(), saved.getPhoneNumber(), token);
    }

    public AuthResponse login(AuthRequest request) {
        UserEntity user = userRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generate(user.getId(), user.getPhoneNumber());
        return new AuthResponse(user.getId(), user.getName(), user.getPhoneNumber(), token);
    }
}

