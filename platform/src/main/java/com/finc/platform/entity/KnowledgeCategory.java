package com.finc.platform.entity;

<<<<<<< HEAD
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Getter
@Setter
@NoArgsConstructor

=======
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "knowledge_categories")
@Getter
@Setter
@NoArgsConstructor
>>>>>>> 313b911 (Fix User entity JPA mapping and complete working Auth UI)
public class KnowledgeCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    private String name;
<<<<<<< HEAD
    private String description;
    private int displayOrder;

    // parent category: KnowledgeCategory (0..1) --- KnowledgeCategory (0..*), aggregation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private KnowledgeCategory parentCategory;

    @OneToMany(mappedBy = "parentCategory")
    private List<KnowledgeCategory> subCategories = new ArrayList<>();

    // groups: KnowledgeCategory (1) --- KnowledgeArticle (0..*), aggregation
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<KnowledgeArticle> articles = new ArrayList<>();

    public KnowledgeCategory(String name, String description, int displayOrder, KnowledgeCategory parentCategory) {
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
        this.parentCategory = parentCategory;
    }

    public void addArticle(KnowledgeArticle article) {
        articles.add(article);
        article.setCategory(this);
    }

}
=======

    public KnowledgeCategory(String name) {
        this.name = name;
    }
}
>>>>>>> 313b911 (Fix User entity JPA mapping and complete working Auth UI)
