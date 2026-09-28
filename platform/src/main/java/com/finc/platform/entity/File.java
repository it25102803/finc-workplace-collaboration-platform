package com.finc.platform.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor

public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fileId;

    private String fileName;
    private String storedName;
    private String mimeType;

    @Enumerated(EnumType.STRING)
    private FileType fileType;

    private Long fileSizeBytes;
    private int currentVersion;
    private String storagePath;
    private boolean isDeleted;
    private LocalDateTime uploadedAt = LocalDateTime.now();

    // stores: Folder (1) *composes* File (0..*)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "folder_id", nullable = false)
    private Folder folder;

    // uploads: User (1) --- File (0..*)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    // version history: File (1) *composes* FileVersion (1..*)
    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileVersion> versions = new ArrayList<>();

    // attaches: KnowledgeArticle (0..*) --- File (0..*)
    @ManyToMany(mappedBy = "attachedFiles")
    private List<KnowledgeArticle> articles = new ArrayList<>();

    public File(String fileName, String mimeType, FileType fileType, Folder folder, User uploadedBy) {
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.fileType = fileType;
        this.folder = folder;
        this.uploadedBy = uploadedBy;
        this.currentVersion = 0;
    }

    public void rename(String name) {
        this.fileName = name;
    }

    public void move(Folder target) {
        this.folder = target;
    }

    public void markDeleted() {
        this.isDeleted = true;
    }


}
