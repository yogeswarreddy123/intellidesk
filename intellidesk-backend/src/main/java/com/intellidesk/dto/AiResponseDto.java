package com.intellidesk.dto;

import com.intellidesk.entity.Priority;

import java.util.ArrayList;
import java.util.List;

public class AiResponseDto {

    private String response;
    private String suggestedCategory;
    private Long suggestedCategoryId;
    private Priority suggestedPriority;
    private List<KnowledgeArticleDto> suggestedArticles = new ArrayList<>();
    private boolean canCreateTicket = true;

    public AiResponseDto() {
    }

    public AiResponseDto(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getSuggestedCategory() {
        return suggestedCategory;
    }

    public void setSuggestedCategory(String suggestedCategory) {
        this.suggestedCategory = suggestedCategory;
    }

    public Long getSuggestedCategoryId() {
        return suggestedCategoryId;
    }

    public void setSuggestedCategoryId(Long suggestedCategoryId) {
        this.suggestedCategoryId = suggestedCategoryId;
    }

    public Priority getSuggestedPriority() {
        return suggestedPriority;
    }

    public void setSuggestedPriority(Priority suggestedPriority) {
        this.suggestedPriority = suggestedPriority;
    }

    public List<KnowledgeArticleDto> getSuggestedArticles() {
        return suggestedArticles;
    }

    public void setSuggestedArticles(List<KnowledgeArticleDto> suggestedArticles) {
        this.suggestedArticles = suggestedArticles;
    }

    public boolean isCanCreateTicket() {
        return canCreateTicket;
    }

    public void setCanCreateTicket(boolean canCreateTicket) {
        this.canCreateTicket = canCreateTicket;
    }
}
