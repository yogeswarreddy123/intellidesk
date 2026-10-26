package com.intellidesk.dto;

import com.intellidesk.entity.Priority;

public class ClassificationResponseDto {

    private String suggestedCategory;
    private Long categoryId;
    private Priority suggestedPriority;
    private double confidence;
    private String reasoning;

    public ClassificationResponseDto() {
    }

    public ClassificationResponseDto(String suggestedCategory, Long categoryId, Priority suggestedPriority, double confidence, String reasoning) {
        this.suggestedCategory = suggestedCategory;
        this.categoryId = categoryId;
        this.suggestedPriority = suggestedPriority;
        this.confidence = confidence;
        this.reasoning = reasoning;
    }

    public String getSuggestedCategory() {
        return suggestedCategory;
    }

    public void setSuggestedCategory(String suggestedCategory) {
        this.suggestedCategory = suggestedCategory;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Priority getSuggestedPriority() {
        return suggestedPriority;
    }

    public void setSuggestedPriority(Priority suggestedPriority) {
        this.suggestedPriority = suggestedPriority;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }
}
