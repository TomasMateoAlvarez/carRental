package com.example.carrental.services;

import com.example.carrental.dto.AuthResponseDTO;
import com.example.carrental.dto.LoginRequestDTO;
import com.example.carrental.dto.RegisterRequestDTO;
import com.example.carrental.model.Role;
import com.example.carrental.model.User;
import com.example.carrental.repository.RoleRepository;
import com.example.carrental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponseDTO login(LoginRequestDTO request) {
        log.info("Attempting login for user: {}", request.getUsername());

        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Get user details
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update last login
        user.updateLastLogin();
        user = userRepository.save(user);

        // Extract roles and permissions
        List<String> roles = user.getRoleNames();
        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .distinct()
                .collect(Collectors.toList());

        // Generate tokens
        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        Long expiresIn = jwtService.getExpirationTime() / 1000; // Convert to seconds

        log.info("Login successful for user: {}", user.getUsername());

        return AuthResponseDTO.success(
                token, refreshToken, expiresIn,
                user.getId(), user.getUsername(), user.getEmail(),
                user.getFirstName(), user.getLastName(),
                roles, permissions
        );
    }

    public AuthResponseDTO register(RegisterRequestDTO request) {
        log.info("Attempting registration for user: {}", request.getUsername());

        // Check if user already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .isActive(true)
                .isLocked(false)
                .failedLoginAttempts(0)
                .build();

        // Assign default role (USER/CLIENT)
        Role defaultRole = roleRepository.findByName("USER")
                .orElseGet(() -> createDefaultRole());
        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);
        user.setRoles(roles);

        // Save user
        user = userRepository.save(user);

        log.info("Registration successful for user: {}", user.getUsername());

        // Auto-login after registration
        return login(new LoginRequestDTO(request.getUsername(), request.getPassword()));
    }

    public AuthResponseDTO refreshToken(String refreshToken) {
        log.info("Attempting token refresh");

        String username = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // Generate new tokens
        String newToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        Long expiresIn = jwtService.getExpirationTime() / 1000;

        // Extract roles and permissions
        List<String> roles = user.getRoleNames();
        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .distinct()
                .collect(Collectors.toList());

        log.info("Token refresh successful for user: {}", user.getUsername());

        return AuthResponseDTO.success(
                newToken, newRefreshToken, expiresIn,
                user.getId(), user.getUsername(), user.getEmail(),
                user.getFirstName(), user.getLastName(),
                roles, permissions
        );
    }

    private Role createDefaultRole() {
        Role userRole = Role.builder()
                .name("USER")
                .description("Default user role")
                .build();
        return roleRepository.save(userRole);
    }
}