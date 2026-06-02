package com.enjot.quickcommerce.service;

import com.enjot.quickcommerce.domain.Role;
import com.enjot.quickcommerce.domain.User;
import com.enjot.quickcommerce.exception.EmailAlreadyUsedException;
import com.enjot.quickcommerce.repository.UserRepository;
import com.enjot.quickcommerce.security.JwtService;
import com.enjot.quickcommerce.web.dto.AuthResponse;
import com.enjot.quickcommerce.web.dto.LoginRequest;
import com.enjot.quickcommerce.web.dto.RegisterRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository users,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (users.existsByEmail(request.email())) {
            throw new EmailAlreadyUsedException(request.email());
        }
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        user.setDateOfBirth(request.dateOfBirth());
        users.save(user);
        return token(user.getEmail(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = users.findByEmail(request.email()).orElseThrow();
        return token(user.getEmail(), user.getRole());
    }

    private AuthResponse token(String email, Role role) {
        return AuthResponse.bearer(jwtService.generateToken(email, role.name()), email, role.name());
    }
}
