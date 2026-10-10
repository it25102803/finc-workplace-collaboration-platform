package com.finc.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long departmentId;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    private Long storageLimitMb;

    private Long storageUsedMb;

    private LocalDateTime createdAt;

    // Custom Constructor 1: Single String argument (Fixes DataInitializer compilation error)
    public Department(String name) {
        this.name = name;
    }

    // Custom Constructor 2: Name and Description
    public Department(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}