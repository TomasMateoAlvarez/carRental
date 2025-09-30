package com.example.carrental.dto;

import lombok.Data;

@Data
public class ClientResponseDTO {
    private Long id;
    private String nombre;
    private String email;
    private String licenciaConducir;
    // Note: password is excluded for security
}