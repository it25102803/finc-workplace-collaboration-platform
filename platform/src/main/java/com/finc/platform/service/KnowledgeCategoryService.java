package com.finc.platform.service;

import java.util.List;
import java.util.NoSuchElementException;

import com.finc.platform.entity.ArticleStatus;
import com.finc.platform.entity.KnowledgeArticle;
import com.finc.platform.entity.KnowledgeCategory;
import com.finc.platform.repository.KnowledgeArticleRepository;
import com.finc.platform.repository.KnowledgeCategoryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KnowledgeCategoryService {

    private final KnowledgeCategoryRepository categoryRepository;
    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgePermissions permissions;

    public KnowledgeCategoryService(KnowledgeCategoryRepository categoryRepository,
                                    KnowledgeArticleRepository articleRepository,
                                    KnowledgePermissions permissions) {
        this.categoryRepository = categoryRepository;
        this.articleRepository = articleRepository;
        this.permissions = permissions;
    }

    @Transactional
    public KnowledgeCategory create(String name, String description, Long actorId) {
        permissions.requireEditor(actorId);
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }
        if (categoryRepository.existsByNameIgnoreCase(name.trim())) {
            throw new IllegalArgumentException("A category with this name already exists");
        }
        return categoryRepository.save(new KnowledgeCategory(name.trim(), description));
    }

    @Transactional(readOnly = true)
    public List<KnowledgeCategory> listAll() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<KnowledgeArticle> getPublishedArticles(Long categoryId) {
        getOrThrow(categoryId);
        return articleRepository.findByCategory_IdAndStatus(categoryId, ArticleStatus.PUBLISHED);
    }

    @Transactional(readOnly = true)
    public KnowledgeCategory getOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException("Category not found: " + categoryId));
    }
}
