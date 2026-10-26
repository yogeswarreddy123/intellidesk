package com.intellidesk.service;

import com.intellidesk.dto.KnowledgeArticleCreateDto;
import com.intellidesk.dto.KnowledgeArticleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface KnowledgeBaseService {

    Page<KnowledgeArticleDto> getAllPublishedArticles(Pageable pageable);

    Page<KnowledgeArticleDto> getAllArticlesForAdmin(String search, Pageable pageable);

    List<KnowledgeArticleDto> searchArticles(String keyword);

    KnowledgeArticleDto getArticleById(Long id);

    List<KnowledgeArticleDto> getArticlesByCategory(Long categoryId);

    KnowledgeArticleDto createArticle(KnowledgeArticleCreateDto dto);

    KnowledgeArticleDto updateArticle(Long id, KnowledgeArticleCreateDto dto);

    void deleteArticle(Long id);
}
