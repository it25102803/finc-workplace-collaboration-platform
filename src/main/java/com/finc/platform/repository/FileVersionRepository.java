package com.finc.platform.repository;
import java.util.List;
import com.finc.platform.entity.FileVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileVersionRepository extends JpaRepository<FileVersion, Long> {

    List<FileVersion> findByFile_FileIdOrderByVersionNumberDesc(Long fileId);
}
