// Usuario.java
package com.example.carrental.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name ="users")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String email;
    private String password;
}