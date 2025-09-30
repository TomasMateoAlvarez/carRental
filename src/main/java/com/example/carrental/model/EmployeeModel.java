package com.example.carrental.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeModel extends UserModel {

    private String puesto;
    private String name;
}
