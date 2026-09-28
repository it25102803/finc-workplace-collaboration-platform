package com.finc.platform.entity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;

import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor


public class KnowledgeArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long articleId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private ArticleType articleType;

    @ElementCollection
    @CollectionTable(name = "knowledge_article_tags", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private ArticleStatus status = ArticleStatus.DRAFT;

    private int viewCount;
    private LocalDateTime publishedAt;
    private LocalDateTime updatedAt = LocalDateTime.now();

    // groups: KnowledgeCategory (1) --- KnowledgeArticle (0..*)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private KnowledgeCategory category;

    // authors: User (1) --- KnowledgeArticle (0..*)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // attaches: KnowledgeArticle (0..*) --- File (0..*)
    @ManyToMany
    @JoinTable(
            name = "knowledge_article_attachments",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "file_id"))
    private List<File> attachedFiles = new ArrayList<>();

    public KnowledgeArticle(String title, String content, ArticleType articleType,
                            KnowledgeCategory category, User author) {
        this.title = title;
        this.content = content;
        this.articleType = articleType;
        this.category = category;
        this.author = author;
    }

    public void publish() {
        this.status = ArticleStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void archive() {
        this.status = ArticleStatus.ARCHIVED;
    }

    public void attachFile(File file) {
        attachedFiles.add(file);
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

}


