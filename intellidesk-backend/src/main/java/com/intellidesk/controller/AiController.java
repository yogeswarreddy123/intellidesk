package com.intellidesk.controller;

import com.intellidesk.dto.*;
import com.intellidesk.service.AiAssistantService;
import com.intellidesk.service.TicketClassificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiAssistantService aiAssistantService;
    private final TicketClassificationService classificationService;

    public AiController(AiAssistantService aiAssistantService,
                        TicketClassificationService classificationService) {
        this.aiAssistantService = aiAssistantService;
        this.classificationService = classificationService;
    }

    @PostMapping("/ask")
    public ResponseEntity<ApiResponse<AiResponseDto>> askAssistant(@Valid @RequestBody AiRequestDto request) {
        AiResponseDto response = aiAssistantService.askAssistant(request);
        return ResponseEntity.ok(ApiResponse.success("AI response generated", response));
    }

    @PostMapping("/classify-ticket")
    public ResponseEntity<ApiResponse<ClassificationResponseDto>> classifyTicket(
            @Valid @RequestBody ClassificationRequestDto request) {
        ClassificationResponseDto classification = classificationService.classifyTicket(request);
        return ResponseEntity.ok(ApiResponse.success("AI suggestion generated", classification));
    }
}
