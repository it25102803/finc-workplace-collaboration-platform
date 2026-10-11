package com.finc.platform.repository;

import com.finc.platform.entity.KnowledgeCategory;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeCategoryRepository extends JpaRepository<KnowledgeCategory, Long> {
    boolean existsByNameIgnoreCase(String name);
}
