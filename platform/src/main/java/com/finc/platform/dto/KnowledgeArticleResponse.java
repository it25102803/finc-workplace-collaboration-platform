package com.finc.platform.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.finc.platform.entity.ArticleStatus;
import com.finc.platform.entity.ArticleType;
import com.finc.platform.entity.File;
import com.finc.platform.entity.KnowledgeArticle;

public record KnowledgeArticleResponse(Long articleId, String title, String content, ArticleType articleType,
                                       Set<String> tags, ArticleStatus status, int viewCount,
                                       LocalDateTime publishedAt, LocalDateTime updatedAt,
                                       Long categoryId, String categoryName,
                                       Long authorId, String authorName, List<Long> attachedFileIds) {

    public static KnowledgeArticleResponse from(KnowledgeArticle a) {
        return new KnowledgeArticleResponse(
                a.getArticleId(), a.getTitle(), a.getContent(), a.getArticleType(),
                Set.copyOf(a.getTags()), a.getStatus(), a.getViewCount(),
                a.getPublishedAt(), a.getUpdatedAt(),
                a.getCategory().getId(), a.getCategory().getName(),
                a.getAuthor().getId(), a.getAuthor().getUsername(),
                a.getAttachedFiles().stream().map(File::getFileId).toList());
    }
}
