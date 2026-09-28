package com.finc.platform.entity;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor


public class FileVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long versionId;

    private int versionNumber;
    private String storagePath;
    private Long fileSizeBytes;
    private String changeNote;
    private LocalDateTime createdAt = LocalDateTime.now();

    // version history: File (1) *composes* FileVersion (1..*)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    // saves: User (1) --- FileVersion (0..*)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    public FileVersion(File file, int versionNumber, String storagePath, Long fileSizeBytes,
                       String changeNote, User createdBy) {
        this.file = file;
        this.versionNumber = versionNumber;
        this.storagePath = storagePath;
        this.fileSizeBytes = fileSizeBytes;
        this.changeNote = changeNote;
        this.createdBy = createdBy;
    }

}
