package com.finc.platform.repository;

import java.util.List;

import com.finc.platform.entity.ArticleStatus;
import com.finc.platform.entity.KnowledgeArticle;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    List<KnowledgeArticle> findByStatus(ArticleStatus status);

    List<KnowledgeArticle> findByCategory_IdAndStatus(Long categoryId, ArticleStatus status);

    @Query("select a from KnowledgeArticle a where a.status = :status and "
            + "(lower(a.title) like lower(concat('%', :q, '%')) or lower(a.content) like lower(concat('%', :q, '%')))")
    List<KnowledgeArticle> search(@Param("q") String q, @Param("status") ArticleStatus status);
}
