package cz.cvut.fel.nss.userservice.service;

import cz.cvut.fel.nss.userservice.dto.user.AuthResponse;
import cz.cvut.fel.nss.userservice.dto.user.LoginRequest;
import cz.cvut.fel.nss.userservice.dto.user.RegisterRequest;
import cz.cvut.fel.nss.userservice.dto.user.UserUpdateRequest;
import cz.cvut.fel.nss.userservice.dto.user.PasswordChangeRequest;
import cz.cvut.fel.nss.commonshared.exception.BadRequestException;
import cz.cvut.fel.nss.commonshared.exception.ResourceNotFoundException;
import cz.cvut.fel.nss.userservice.repository.UserRepository;
import cz.cvut.fel.nss.userservice.dto.user.UserResponse;
import cz.cvut.fel.nss.userservice.entity.User;
import cz.cvut.fel.nss.userservice.enums.UserRole;
import cz.cvut.fel.nss.commonshared.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }
        if (userRepository.existsByLogin(request.getLogin())) {
            throw new BadRequestException("Login is already in use!");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setLogin(request.getLogin());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER);

        User savedUser = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token, mapToUserResponse(savedUser));
    }

    public AuthResponse loginUser(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found!"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token, mapToUserResponse(user));
    }

    public UserResponse updateUserProfile(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use by another account!");
        }
        if (!user.getLogin().equalsIgnoreCase(request.getLogin()) && userRepository.existsByLogin(request.getLogin())) {
            throw new BadRequestException("Login is already in use by another account!");
        }

        user.setEmail(request.getEmail());
        user.setLogin(request.getLogin());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Incorrect old password!");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private UserResponse mapToUserResponse(User user) {
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
