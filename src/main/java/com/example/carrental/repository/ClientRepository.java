package com.example.carrental.repository;
import com.example.carrental.model.ClientModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ClientRepository extends JpaRepository<ClientModel, Long> {
}
