package com.finc.platform.dto;

import com.finc.platform.entity.KnowledgeCategory;

public record KnowledgeCategoryResponse(Long categoryId, String name, String description) {

    public static KnowledgeCategoryResponse from(KnowledgeCategory c) {
        return new KnowledgeCategoryResponse(c.getId(), c.getName(), c.getDescription());
    }
}
