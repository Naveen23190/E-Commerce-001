package com.example.ecommerce.controller;

import com.example.ecommerce.model.AppUser;
import com.example.ecommerce.repo.UserRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users; private final PasswordEncoder encoder;
    public AuthController(UserRepository users, PasswordEncoder encoder){this.users=users;this.encoder=encoder;}

    public record RegisterRequest(@NotBlank String username, @NotBlank String password) {}
    public record UserResponse(Long id, String username, String role) {}

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody RegisterRequest request) {
        if (request.username().length() > 80 || request.password().length() < 8)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username must be <= 80 characters and password at least 8 characters");
        if (users.existsByUsername(request.username()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        AppUser user = users.save(new AppUser(request.username(), encoder.encode(request.password()), "CUSTOMER"));
        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }
}
