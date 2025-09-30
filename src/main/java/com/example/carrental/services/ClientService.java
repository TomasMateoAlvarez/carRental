package com.example.carrental.services;

import com.example.carrental.dto.ClientRequestDTO;
import com.example.carrental.dto.ClientResponseDTO;
import com.example.carrental.exception.ResourceNotFoundException;
import com.example.carrental.mapper.ClientMapper;
import com.example.carrental.model.ClientModel;
import com.example.carrental.repository.ClientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClientService {
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    public ClientService(ClientRepository clientRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
    }

    public List<ClientResponseDTO> getAllClients() {
        log.info("Retrieving all clients");
        List<ClientResponseDTO> clients = clientRepository.findAll()
                .stream()
                .map(clientMapper::toResponseDTO)
                .collect(Collectors.toList());
        log.info("Found {} clients", clients.size());
        return clients;
    }

    public ClientResponseDTO getClientById(Long id) {
        log.info("Retrieving client with ID: {}", id);
        ClientModel client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
        return clientMapper.toResponseDTO(client);
    }

    public ClientResponseDTO createClient(ClientRequestDTO clientDTO) {
        log.info("Creating new client with email: {}", clientDTO.getEmail());
        ClientModel client = clientMapper.toEntity(clientDTO);
        ClientModel savedClient = clientRepository.save(client);
        log.info("Client created successfully with ID: {}", savedClient.getId());
        return clientMapper.toResponseDTO(savedClient);
    }

    public ClientResponseDTO updateClient(Long id, ClientRequestDTO clientDTO) {
        log.info("Updating client with ID: {}", id);
        ClientModel existingClient = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));

        clientMapper.updateEntity(existingClient, clientDTO);
        ClientModel updatedClient = clientRepository.save(existingClient);
        log.info("Client updated successfully with ID: {}", updatedClient.getId());
        return clientMapper.toResponseDTO(updatedClient);
    }

    public void deleteClient(Long id) {
        log.info("Deleting client with ID: {}", id);
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente", id);
        }
        clientRepository.deleteById(id);
        log.info("Client deleted successfully with ID: {}", id);
    }
}
