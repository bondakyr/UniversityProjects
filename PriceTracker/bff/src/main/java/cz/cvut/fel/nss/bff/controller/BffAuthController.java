package cz.cvut.fel.nss.bff.controller;

import cz.cvut.fel.nss.bff.client.UserServiceClient;
import cz.cvut.fel.nss.bff.dto.LoginRequest;
import cz.cvut.fel.nss.bff.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registration and login (proxied to user-service)")
public class BffAuthController {

    private final UserServiceClient userServiceClient;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        return userServiceClient.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Log in and receive a JWT (data.token in the response)")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        return userServiceClient.login(request);
    }
}