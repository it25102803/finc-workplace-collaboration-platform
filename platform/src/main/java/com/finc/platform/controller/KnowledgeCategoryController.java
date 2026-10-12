package com.finc.platform.controller;

import java.util.List;
import java.util.NoSuchElementException;

import com.finc.platform.dto.CreateCategoryRequest;
import com.finc.platform.dto.KnowledgeArticleResponse;
import com.finc.platform.dto.KnowledgeCategoryResponse;
import com.finc.platform.service.KnowledgeCategoryService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kb/categories")
public class KnowledgeCategoryController {

    private final KnowledgeCategoryService categoryService;

    public KnowledgeCategoryController(KnowledgeCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<KnowledgeCategoryResponse> create(@RequestBody CreateCategoryRequest request,
                                                            @RequestParam(required = false) Long actorId) {
        return ResponseEntity.ok(KnowledgeCategoryResponse.from(
                categoryService.create(request.name(), request.description(), actorId)));
    }

    @GetMapping
    public ResponseEntity<List<KnowledgeCategoryResponse>> listAll() {
        return ResponseEntity.ok(categoryService.listAll().stream().map(KnowledgeCategoryResponse::from).toList());
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<KnowledgeCategoryResponse> get(@PathVariable Long categoryId) {
        return ResponseEntity.ok(KnowledgeCategoryResponse.from(categoryService.getOrThrow(categoryId)));
    }

    @GetMapping("/{categoryId}/articles")
    public ResponseEntity<List<KnowledgeArticleResponse>> getArticles(@PathVariable Long categoryId) {
        return ResponseEntity.ok(categoryService.getPublishedArticles(categoryId).stream()
                .map(KnowledgeArticleResponse::from).toList());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> notFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<String> forbidden(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
}
