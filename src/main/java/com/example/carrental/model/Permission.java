package com.example.carrental.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String resource;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // Many-to-Many relationship with Role (mapped by Role entity)
    @ManyToMany(mappedBy = "permissions")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    public Permission(String name, String resource, String action, String description) {
        this.name = name;
        this.resource = resource;
        this.action = action;
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission)) return false;
        Permission that = (Permission) o;
        return name != null && name.equals(that.getName());
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", resource='" + resource + '\'' +
                ", action='" + action + '\'' +
                '}';
    }
}

// Common permission constants
class PermissionConstants {
    // Resources
    public static final String VEHICLES = "VEHICLES";
    public static final String CLIENTS = "CLIENTS";
    public static final String USERS = "USERS";
    public static final String REPORTS = "REPORTS";
    public static final String ADMIN = "ADMIN";

    // Actions
    public static final String READ = "READ";
    public static final String CREATE = "CREATE";
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";
    public static final String MANAGE = "MANAGE";

    // Common permission names
    public static final String READ_VEHICLES = "READ_VEHICLES";
    public static final String CREATE_VEHICLES = "CREATE_VEHICLES";
    public static final String UPDATE_VEHICLES = "UPDATE_VEHICLES";
    public static final String DELETE_VEHICLES = "DELETE_VEHICLES";
    public static final String MANAGE_VEHICLES = "MANAGE_VEHICLES";

    public static final String READ_CLIENTS = "READ_CLIENTS";
    public static final String CREATE_CLIENTS = "CREATE_CLIENTS";
    public static final String UPDATE_CLIENTS = "UPDATE_CLIENTS";
    public static final String DELETE_CLIENTS = "DELETE_CLIENTS";
    public static final String MANAGE_CLIENTS = "MANAGE_CLIENTS";

    public static final String READ_USERS = "READ_USERS";
    public static final String CREATE_USERS = "CREATE_USERS";
    public static final String UPDATE_USERS = "UPDATE_USERS";
    public static final String DELETE_USERS = "DELETE_USERS";
    public static final String MANAGE_USERS = "MANAGE_USERS";

    public static final String READ_REPORTS = "READ_REPORTS";
    public static final String MANAGE_ADMIN = "MANAGE_ADMIN";
}