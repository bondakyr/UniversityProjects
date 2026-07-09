package cz.cvut.fel.nss.userservice.controller;

import cz.cvut.fel.nss.commonshared.exception.ResourceNotFoundException;
import cz.cvut.fel.nss.userservice.dto.user.UserResponse;
import cz.cvut.fel.nss.userservice.entity.User;
import cz.cvut.fel.nss.userservice.repository.UserRepository;
import cz.cvut.fel.nss.userservice.security.InternalTokenValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cz.cvut.fel.nss.userservice.dto.user.UserUpdateRequest;
import cz.cvut.fel.nss.userservice.dto.user.PasswordChangeRequest;
import cz.cvut.fel.nss.userservice.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final InternalTokenValidator internalTokenValidator;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(
            @RequestHeader("X-Internal-Token") String internalToken,
            @PathVariable Long id) {
        internalTokenValidator.requireValid(internalToken);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(toResponse(user));
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserResponse> getByEmail(
            @RequestHeader("X-Internal-Token") String internalToken,
            @PathVariable String email) {
        internalTokenValidator.requireValid(internalToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(userId, request);
        return ResponseEntity.noContent().build();
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .login(user.getLogin())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }
}
