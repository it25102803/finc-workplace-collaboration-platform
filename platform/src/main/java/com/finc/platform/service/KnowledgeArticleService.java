package com.finc.platform.service;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;

import com.finc.platform.dto.CreateArticleRequest;
import com.finc.platform.entity.ArticleStatus;
import com.finc.platform.entity.File;
import com.finc.platform.entity.KnowledgeArticle;
import com.finc.platform.entity.KnowledgeCategory;
import com.finc.platform.entity.User;
import com.finc.platform.repository.FileRepository;
import com.finc.platform.repository.KnowledgeArticleRepository;
import com.finc.platform.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KnowledgeArticleService {

    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgeCategoryService categoryService;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;

    public KnowledgeArticleService(KnowledgeArticleRepository articleRepository,
                                   KnowledgeCategoryService categoryService,
                                   UserRepository userRepository,
                                   FileRepository fileRepository) {
        this.articleRepository = articleRepository;
        this.categoryService = categoryService;
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
    }

    @Transactional
    public KnowledgeArticle create(CreateArticleRequest request) {
        if (isBlank(request.title()) || isBlank(request.content()) || request.articleType() == null
                || request.categoryId() == null || request.authorId() == null) {
            throw new IllegalArgumentException(
                    "Required fields missing: title, content, article type, category and author are required");
        }

        KnowledgeCategory category = categoryService.getOrThrow(request.categoryId());
        User author = userRepository.findById(request.authorId())
                .orElseThrow(() -> new NoSuchElementException("User not found: " + request.authorId()));

        KnowledgeArticle article = new KnowledgeArticle(request.title().trim(), request.content().trim(),
                request.articleType(), category, author);
        if (request.tags() != null) {
            article.setTags(new HashSet<>(request.tags()));
        }
        if (Boolean.TRUE.equals(request.publish())) {
            article.publish();
        }
        return articleRepository.save(article);
    }

    @Transactional
    public KnowledgeArticle publish(Long articleId) {
        KnowledgeArticle article = getOrThrow(articleId);
        article.publish();
        return article;
    }

    @Transactional
    public KnowledgeArticle archive(Long articleId) {
        KnowledgeArticle article = getOrThrow(articleId);
        article.archive();
        return article;
    }

    @Transactional
    public KnowledgeArticle attachFile(Long articleId, Long fileId) {
        KnowledgeArticle article = getOrThrow(articleId);
        File file = fileRepository.findById(fileId)
                .filter(f -> !f.isDeleted())
                .orElseThrow(() -> new NoSuchElementException("File not found: " + fileId));
        boolean alreadyAttached = article.getAttachedFiles().stream()
                .anyMatch(f -> f.getFileId().equals(fileId));
        if (!alreadyAttached) {
            article.attachFile(file);
        }
        return article;
    }

    @Transactional
    public KnowledgeArticle view(Long articleId) {
        KnowledgeArticle article = getOrThrow(articleId);
        article.incrementViewCount();
        return article;
    }

    @Transactional(readOnly = true)
    public List<KnowledgeArticle> listPublished() {
        return articleRepository.findByStatus(ArticleStatus.PUBLISHED);
    }

    @Transactional(readOnly = true)
    public List<KnowledgeArticle> search(String query) {
        if (isBlank(query)) {
            return listPublished();
        }
        return articleRepository.search(query.trim(), ArticleStatus.PUBLISHED);
    }

    @Transactional(readOnly = true)
    public KnowledgeArticle getOrThrow(Long articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new NoSuchElementException("Article not found: " + articleId));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
