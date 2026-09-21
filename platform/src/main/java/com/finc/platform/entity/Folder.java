package com.finc.platform.entity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor

public class Folder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long folderId;

    private String name;
    private String path;
    private boolean isShared;
    private LocalDateTime createdAt = LocalDateTime.now();

    // owns: User (1) --- Folder (0..*)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // repository of: Department (0..1) --- Folder (0..*)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    // parent / sub-folder: Folder (0..1) --- Folder (0..*), aggregation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_folder_id")
    private Folder parentFolder;

    @OneToMany(mappedBy = "parentFolder")
    private List<Folder> subFolders = new ArrayList<>();

    // stores: Folder (1) *composes* File (0..*)
    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<File> files = new ArrayList<>();

    public Folder(String name, String path, User owner, Department department, Folder parentFolder) {
        this.name = name;
        this.path = path;
        this.owner = owner;
        this.department = department;
        this.parentFolder = parentFolder;
    }

    public void rename(String name) {
        this.name = name;
    }

}
