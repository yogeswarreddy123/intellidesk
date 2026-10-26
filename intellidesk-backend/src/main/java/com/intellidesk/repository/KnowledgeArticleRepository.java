package com.intellidesk.repository;

import com.intellidesk.entity.KnowledgeArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    Page<KnowledgeArticle> findByPublishedTrue(Pageable pageable);

    List<KnowledgeArticle> findByPublishedTrueAndCategoryId(Long categoryId);

    @Query("SELECT k FROM KnowledgeArticle k WHERE k.published = true AND (" +
           "LOWER(k.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(k.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(k.keywords) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<KnowledgeArticle> searchArticles(@Param("keyword") String keyword);

    @Query("SELECT k FROM KnowledgeArticle k WHERE " +
           "LOWER(k.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(k.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(k.keywords) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<KnowledgeArticle> searchArticlesAdmin(@Param("keyword") String keyword, Pageable pageable);

    long countByPublishedTrue();
}
