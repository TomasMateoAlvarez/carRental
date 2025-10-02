package com.example.carrental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

    private String token;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn; // seconds

    // User info
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private List<String> roles;
    private List<String> permissions;

    public static AuthResponseDTO success(String token, String refreshToken, Long expiresIn,
                                        Long userId, String username, String email,
                                        String firstName, String lastName,
                                        List<String> roles, List<String> permissions) {
        return AuthResponseDTO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .userId(userId)
                .username(username)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .fullName(firstName + " " + lastName)
                .roles(roles)
                .permissions(permissions)
                .build();
    }
}