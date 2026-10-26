package com.intellidesk.controller;

import com.intellidesk.dto.ApiResponse;
import com.intellidesk.dto.KnowledgeArticleCreateDto;
import com.intellidesk.dto.KnowledgeArticleDto;
import com.intellidesk.service.KnowledgeBaseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<KnowledgeArticleDto>>> getAllArticles(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<KnowledgeArticleDto> articles = knowledgeBaseService.getAllArticlesForAdmin(search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Knowledge articles retrieved", articles));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<KnowledgeArticleDto>>> searchArticles(
            @RequestParam(name = "keyword", required = false) String keyword) {
        List<KnowledgeArticleDto> articles = knowledgeBaseService.searchArticles(keyword);
        return ResponseEntity.ok(ApiResponse.success("Search results retrieved", articles));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KnowledgeArticleDto>> getArticleById(@PathVariable("id") Long id) {
        KnowledgeArticleDto article = knowledgeBaseService.getArticleById(id);
        return ResponseEntity.ok(ApiResponse.success("Article retrieved", article));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<KnowledgeArticleDto>>> getArticlesByCategory(
            @PathVariable("categoryId") Long categoryId) {
        List<KnowledgeArticleDto> articles = knowledgeBaseService.getArticlesByCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Category articles retrieved", articles));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('IT_AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<KnowledgeArticleDto>> createArticle(
            @Valid @RequestBody KnowledgeArticleCreateDto dto) {
        KnowledgeArticleDto created = knowledgeBaseService.createArticle(dto);
        return new ResponseEntity<>(ApiResponse.success("Article created", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('IT_AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<KnowledgeArticleDto>> updateArticle(
            @PathVariable("id") Long id,
            @Valid @RequestBody KnowledgeArticleCreateDto dto) {
        KnowledgeArticleDto updated = knowledgeBaseService.updateArticle(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Article updated", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('IT_AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable("id") Long id) {
        knowledgeBaseService.deleteArticle(id);
        return ResponseEntity.ok(ApiResponse.success("Article deleted"));
    }
}
