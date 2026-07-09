package cz.cvut.fel.nss.userservice.controller;

import cz.cvut.fel.nss.userservice.dto.user.AuthResponse;
import cz.cvut.fel.nss.userservice.dto.user.LoginRequest;
import cz.cvut.fel.nss.userservice.dto.user.RegisterRequest;
import cz.cvut.fel.nss.commonshared.payload.ApiResponse;
import cz.cvut.fel.nss.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authData = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("SUCCESS", "User successfully registered", authData));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authData = userService.loginUser(request);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Login successful", authData));
    }
}