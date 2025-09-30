package com.example.carrental.controller;

import com.example.carrental.model.EmployeeModel;
import com.example.carrental.services.EmployeeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<EmployeeModel> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/{id}")
    public EmployeeModel getEmployeeById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
    }

    @PostMapping
    public EmployeeModel createEmployee(@RequestBody EmployeeModel employee) {
        return employeeService.createEmployee(employee);
    }

    @PutMapping("/{id}")
    public EmployeeModel updateEmployee(@PathVariable Long id, @RequestBody EmployeeModel employeeDetails) {
        return employeeService.updateEmployee(id, employeeDetails);
    }

    @DeleteMapping("/{id}")
    public String deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return "Empleado eliminado con éxito";
    }
}
