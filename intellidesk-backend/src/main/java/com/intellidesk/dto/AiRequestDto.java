package com.intellidesk.dto;

import jakarta.validation.constraints.NotBlank;

public class AiRequestDto {

    @NotBlank(message = "Message cannot be empty")
    private String message;

    public AiRequestDto() {
    }

    public AiRequestDto(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
