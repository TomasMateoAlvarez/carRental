package com.example.carrental.services;

import com.example.carrental.model.EmployeeModel;
import com.example.carrental.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // Obtener todos los empleados
    public List<EmployeeModel> getAllEmployees() {
        return employeeRepository.findAll();
    }

    // Buscar empleado por ID
    public Optional<EmployeeModel> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    // Crear nuevo empleado
    public EmployeeModel createEmployee(EmployeeModel employee) {
        return employeeRepository.save(employee);
    }

    // Actualizar empleado
    public EmployeeModel updateEmployee(Long id, EmployeeModel employeeDetails) {
        return employeeRepository.findById(id).map(employee -> {
            employee.setName(employeeDetails.getName());
            return employeeRepository.save(employee);
        }).orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
    }

    // Eliminar empleado
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
}
