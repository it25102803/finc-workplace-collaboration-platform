package com.finc.platform.controller;

import java.util.List;
import java.util.NoSuchElementException;

import com.finc.platform.dto.CreateArticleRequest;
import com.finc.platform.dto.KnowledgeArticleResponse;
import com.finc.platform.service.KnowledgeArticleService;

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
@RequestMapping("/api/kb/articles")
public class KnowledgeArticleController {

    private final KnowledgeArticleService articleService;

    public KnowledgeArticleController(KnowledgeArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping
    public ResponseEntity<KnowledgeArticleResponse> create(@RequestBody CreateArticleRequest request,
                                                           @RequestParam(required = false) Long actorId) {
        return ResponseEntity.ok(KnowledgeArticleResponse.from(articleService.create(request, actorId)));
    }

    @GetMapping
    public ResponseEntity<List<KnowledgeArticleResponse>> listPublished() {
        return ResponseEntity.ok(articleService.listPublished().stream().map(KnowledgeArticleResponse::from).toList());
    }

    @GetMapping("/search")
    public ResponseEntity<List<KnowledgeArticleResponse>> search(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(articleService.search(q).stream().map(KnowledgeArticleResponse::from).toList());
    }

    @GetMapping("/{articleId}")
    public ResponseEntity<KnowledgeArticleResponse> view(@PathVariable Long articleId) {
        return ResponseEntity.ok(KnowledgeArticleResponse.from(articleService.view(articleId)));
    }

    @PostMapping("/{articleId}/publish")
    public ResponseEntity<KnowledgeArticleResponse> publish(@PathVariable Long articleId,
                                                            @RequestParam(required = false) Long actorId) {
        return ResponseEntity.ok(KnowledgeArticleResponse.from(articleService.publish(articleId, actorId)));
    }

    @PostMapping("/{articleId}/archive")
    public ResponseEntity<KnowledgeArticleResponse> archive(@PathVariable Long articleId,
                                                            @RequestParam(required = false) Long actorId) {
        return ResponseEntity.ok(KnowledgeArticleResponse.from(articleService.archive(articleId, actorId)));
    }

    @PostMapping("/{articleId}/attachments")
    public ResponseEntity<KnowledgeArticleResponse> attachFile(@PathVariable Long articleId,
                                                               @RequestParam Long fileId,
                                                               @RequestParam(required = false) Long actorId) {
        return ResponseEntity.ok(KnowledgeArticleResponse.from(articleService.attachFile(articleId, fileId, actorId)));
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
