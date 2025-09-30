package com.example.carrental.mapper;

import com.example.carrental.dto.ClientRequestDTO;
import com.example.carrental.dto.ClientResponseDTO;
import com.example.carrental.model.ClientModel;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

    private final PasswordEncoder passwordEncoder;

    public ClientMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public ClientModel toEntity(ClientRequestDTO dto) {
        ClientModel client = new ClientModel();
        client.setNombre(dto.getNombre());
        client.setEmail(dto.getEmail());
        client.setPassword(passwordEncoder.encode(dto.getPassword()));
        client.setLicenciaConducir(dto.getLicenciaConducir());
        return client;
    }

    public ClientResponseDTO toResponseDTO(ClientModel client) {
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setId(client.getId());
        dto.setNombre(client.getNombre());
        dto.setEmail(client.getEmail());
        dto.setLicenciaConducir(client.getLicenciaConducir());
        return dto;
    }

    public void updateEntity(ClientModel client, ClientRequestDTO dto) {
        client.setNombre(dto.getNombre());
        client.setEmail(dto.getEmail());
        client.setLicenciaConducir(dto.getLicenciaConducir());

        // Only update password if provided
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            client.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
    }
}