package com.finc.platform.dto;

import java.util.Set;

import com.finc.platform.entity.ArticleType;

public record CreateArticleRequest(String title, String content, ArticleType articleType, Set<String> tags,
                                   Long categoryId, Long authorId, Boolean publish) {}
