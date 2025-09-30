// EmployeeRepository.java
package com.example.carrental.repository;

import com.example.carrental.model.EmployeeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeModel, Long> {}
