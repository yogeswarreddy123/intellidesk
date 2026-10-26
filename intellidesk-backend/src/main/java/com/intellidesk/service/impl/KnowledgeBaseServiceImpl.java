package com.intellidesk.service.impl;

import com.intellidesk.dto.KnowledgeArticleCreateDto;
import com.intellidesk.dto.KnowledgeArticleDto;
import com.intellidesk.entity.Category;
import com.intellidesk.entity.KnowledgeArticle;
import com.intellidesk.entity.User;
import com.intellidesk.exception.ResourceNotFoundException;
import com.intellidesk.exception.UnauthorizedException;
import com.intellidesk.repository.CategoryRepository;
import com.intellidesk.repository.KnowledgeArticleRepository;
import com.intellidesk.repository.UserRepository;
import com.intellidesk.security.UserPrincipal;
import com.intellidesk.service.AuditLogService;
import com.intellidesk.service.KnowledgeBaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private final KnowledgeArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public KnowledgeBaseServiceImpl(KnowledgeArticleRepository articleRepository,
                                    CategoryRepository categoryRepository,
                                    UserRepository userRepository,
                                    AuditLogService auditLogService) {
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KnowledgeArticleDto> getAllPublishedArticles(Pageable pageable) {
        return articleRepository.findByPublishedTrue(pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KnowledgeArticleDto> getAllArticlesForAdmin(String search, Pageable pageable) {
        if (StringUtils.hasText(search)) {
            return articleRepository.searchArticlesAdmin(search.trim(), pageable).map(this::mapToDto);
        }
        return articleRepository.findAll(pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KnowledgeArticleDto> searchArticles(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return articleRepository.findByPublishedTrue(Pageable.ofSize(10)).getContent()
                    .stream().map(this::mapToDto).collect(Collectors.toList());
        }
        return articleRepository.searchArticles(keyword.trim())
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeArticleDto getArticleById(Long id) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KnowledgeArticle", "id", id));
        return mapToDto(article);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KnowledgeArticleDto> getArticlesByCategory(Long categoryId) {
        return articleRepository.findByPublishedTrueAndCategoryId(categoryId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public KnowledgeArticleDto createArticle(KnowledgeArticleCreateDto dto) {
        User currentUser = getCurrentUser();
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));

        KnowledgeArticle article = new KnowledgeArticle();
        article.setTitle(dto.getTitle().trim());
        article.setContent(dto.getContent().trim());
        article.setCategory(category);
        article.setKeywords(dto.getKeywords());
        article.setPublished(dto.getPublished() != null ? dto.getPublished() : true);
        article.setCreatedBy(currentUser);

        KnowledgeArticle saved = articleRepository.save(article);

        auditLogService.log("CREATE_ARTICLE", "KNOWLEDGE_ARTICLE", String.valueOf(saved.getId()),
                "Created knowledge article: " + saved.getTitle());

        return mapToDto(saved);
    }

    @Override
    public KnowledgeArticleDto updateArticle(Long id, KnowledgeArticleCreateDto dto) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KnowledgeArticle", "id", id));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));

        article.setTitle(dto.getTitle().trim());
        article.setContent(dto.getContent().trim());
        article.setCategory(category);
        article.setKeywords(dto.getKeywords());
        if (dto.getPublished() != null) {
            article.setPublished(dto.getPublished());
        }

        KnowledgeArticle updated = articleRepository.save(article);

        auditLogService.log("UPDATE_ARTICLE", "KNOWLEDGE_ARTICLE", String.valueOf(updated.getId()),
                "Updated knowledge article: " + updated.getTitle());

        return mapToDto(updated);
    }

    @Override
    public void deleteArticle(Long id) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KnowledgeArticle", "id", id));

        articleRepository.delete(article);

        auditLogService.log("DELETE_ARTICLE", "KNOWLEDGE_ARTICLE", String.valueOf(id),
                "Deleted knowledge article: " + article.getTitle());
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal)) {
            throw new UnauthorizedException("User not authenticated");
        }
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));
    }

    private KnowledgeArticleDto mapToDto(KnowledgeArticle article) {
        KnowledgeArticleDto dto = new KnowledgeArticleDto();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setContent(article.getContent());
        if (article.getCategory() != null) {
            dto.setCategoryId(article.getCategory().getId());
            dto.setCategoryName(article.getCategory().getName());
        }
        dto.setKeywords(article.getKeywords());
        if (article.getCreatedBy() != null) {
            dto.setCreatedById(article.getCreatedBy().getId());
            dto.setCreatedByName(article.getCreatedBy().getFullName());
        }
        dto.setCreatedAt(article.getCreatedAt());
        dto.setUpdatedAt(article.getUpdatedAt());
        dto.setPublished(article.isPublished());
        return dto;
    }
}
