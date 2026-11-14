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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KnowledgeArticleService {

    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgeCategoryService categoryService;
    private final FileRepository fileRepository;
    private final KnowledgePermissions permissions;
    private final FileAccess fileAccess;

    public KnowledgeArticleService(KnowledgeArticleRepository articleRepository,
                                   KnowledgeCategoryService categoryService,
                                   FileRepository fileRepository,
                                   KnowledgePermissions permissions,
                                   FileAccess fileAccess) {
        this.articleRepository = articleRepository;
        this.categoryService = categoryService;
        this.fileRepository = fileRepository;
        this.permissions = permissions;
        this.fileAccess = fileAccess;
    }

    @Transactional
    public KnowledgeArticle create(CreateArticleRequest request, Long actorId) {
        User author = permissions.requireEditor(actorId);

        if (isBlank(request.title()) || isBlank(request.content()) || request.articleType() == null
                || request.categoryId() == null) {
            throw new IllegalArgumentException(
                    "Required fields missing: title, content, article type and category are required");
        }

        KnowledgeCategory category = categoryService.getOrThrow(request.categoryId());
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
    public KnowledgeArticle publish(Long articleId, Long actorId) {
        permissions.requireEditor(actorId);
        KnowledgeArticle article = getOrThrow(articleId);
        article.publish();
        return article;
    }

    @Transactional
    public KnowledgeArticle archive(Long articleId, Long actorId) {
        permissions.requireEditor(actorId);
        KnowledgeArticle article = getOrThrow(articleId);
        article.archive();
        return article;
    }

    @Transactional
    public KnowledgeArticle attachFile(Long articleId, Long fileId, Long actorId) {
        permissions.requireEditor(actorId);
        KnowledgeArticle article = getOrThrow(articleId);
        File file = fileRepository.findById(fileId)
                .filter(f -> !f.isDeleted())
                .orElseThrow(() -> new NoSuchElementException("File not found: " + fileId));
        // Attaching publishes the file to every reader, so it must be a file you can open yourself
        fileAccess.requireReadFile(file, fileAccess.actor(actorId));
        boolean alreadyAttached = article.getAttachedFiles().stream()
                .anyMatch(f -> f.getFileId().equals(fileId));
        if (!alreadyAttached) {
            article.attachFile(file);
        }
        return article;
    }

    @Transactional
    // Published articles are open to everyone; drafts and archived articles only to KB editors
    public KnowledgeArticle view(Long articleId, Long actorId) {
        KnowledgeArticle article = getOrThrow(articleId);
        if (article.getStatus() != ArticleStatus.PUBLISHED) {
            permissions.requireEditor(actorId);
        }
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
