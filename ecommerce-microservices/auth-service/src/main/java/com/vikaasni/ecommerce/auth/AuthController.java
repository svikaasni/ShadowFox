package com.vikaasni.ecommerce.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDtos.AuthResponse register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        if (users.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        
        // Auto-elevate to ADMIN if email is shopkeeper@sweet.com or contains admin
        String emailLower = request.email().toLowerCase();
        if (emailLower.equals("shopkeeper@sweet.com") || emailLower.contains("admin")) {
            user.setRole(User.Role.ADMIN);
        }
        
        user = users.save(user);
        return response(user);
    }

    @PostMapping("/login")
    public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        User user = users.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        
        // Auto-elevate to ADMIN if already registered as customer
        String emailLower = user.getEmail().toLowerCase();
        if (user.getRole() == User.Role.CUSTOMER && (emailLower.equals("shopkeeper@sweet.com") || emailLower.contains("admin"))) {
            user.setRole(User.Role.ADMIN);
            user = users.save(user);
        }
        
        return response(user);
    }

    private AuthDtos.AuthResponse response(User user) {
        return new AuthDtos.AuthResponse(jwtService.generate(user), user.getId(), user.getName(),
                user.getEmail(), user.getRole().name());
    }
}
