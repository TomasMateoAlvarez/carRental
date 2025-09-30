package com.example.carrental.controller;

import com.example.carrental.dto.ClientRequestDTO;
import com.example.carrental.dto.ClientResponseDTO;
import com.example.carrental.services.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public ResponseEntity<List<ClientResponseDTO>> getAllClients() {
        List<ClientResponseDTO> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> getClientById(@PathVariable Long id) {
        ClientResponseDTO client = clientService.getClientById(id);
        return ResponseEntity.ok(client);
    }

    @PostMapping
    public ResponseEntity<ClientResponseDTO> createClient(@Valid @RequestBody ClientRequestDTO clientDTO) {
        ClientResponseDTO createdClient = clientService.createClient(clientDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdClient);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> updateClient(@PathVariable Long id, @Valid @RequestBody ClientRequestDTO clientDTO) {
        ClientResponseDTO updatedClient = clientService.updateClient(id, clientDTO);
        return ResponseEntity.ok(updatedClient);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
}
