package com.intellidesk.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentCreateDto {

    @NotBlank(message = "Comment message cannot be empty")
    private String message;

    public CommentCreateDto() {
    }

    public CommentCreateDto(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
