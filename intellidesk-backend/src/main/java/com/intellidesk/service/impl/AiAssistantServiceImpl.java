package com.intellidesk.service.impl;

import com.intellidesk.dto.AiRequestDto;
import com.intellidesk.dto.AiResponseDto;
import com.intellidesk.dto.ClassificationRequestDto;
import com.intellidesk.dto.ClassificationResponseDto;
import com.intellidesk.dto.KnowledgeArticleDto;
import com.intellidesk.entity.KnowledgeArticle;
import com.intellidesk.repository.KnowledgeArticleRepository;
import com.intellidesk.service.AiAssistantService;
import com.intellidesk.service.TicketClassificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiAssistantServiceImpl implements AiAssistantService {

    private static final Logger logger = LoggerFactory.getLogger(AiAssistantServiceImpl.class);

    private final KnowledgeArticleRepository articleRepository;
    private final TicketClassificationService classificationService;

    @Value("${intellidesk.ai.api-key:}")
    private String apiKey;

    @Value("${intellidesk.ai.enabled:false}")
    private boolean externalAiEnabled;

    public AiAssistantServiceImpl(KnowledgeArticleRepository articleRepository,
                                  TicketClassificationService classificationService) {
        this.articleRepository = articleRepository;
        this.classificationService = classificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public AiResponseDto askAssistant(AiRequestDto request) {
        String query = request.getMessage().trim();
        logger.info("Processing AI Assistant query: {}", query);

        // 1. Classify the user issue to determine likely category & priority
        ClassificationResponseDto classification = classificationService.classifyTicket(
                new ClassificationRequestDto(query, query)
        );

        // 2. Search knowledge base
        List<KnowledgeArticle> matchedArticles = articleRepository.searchArticles(query);
        List<KnowledgeArticleDto> articleDtos = new ArrayList<>();
        for (KnowledgeArticle article : matchedArticles) {
            KnowledgeArticleDto dto = new KnowledgeArticleDto();
            dto.setId(article.getId());
            dto.setTitle(article.getTitle());
            dto.setContent(article.getContent());
            if (article.getCategory() != null) {
                dto.setCategoryId(article.getCategory().getId());
                dto.setCategoryName(article.getCategory().getName());
            }
            dto.setKeywords(article.getKeywords());
            articleDtos.add(dto);
        }

        // 3. Synthesize smart response
        StringBuilder responseBuilder = new StringBuilder();

        if (!matchedArticles.isEmpty()) {
            KnowledgeArticle topArticle = matchedArticles.get(0);
            responseBuilder.append("I found relevant information in our knowledge base for **")
                    .append(topArticle.getTitle()).append("**:\n\n");

            // Extract main recommendations or steps
            String content = topArticle.getContent();
            responseBuilder.append(content).append("\n\n");
            responseBuilder.append("---\n");
            responseBuilder.append("Did this solve your problem?\n");
            responseBuilder.append("If not, you can click **Create Ticket** below and I will auto-fill the details for you.");
        } else {
            // Contextual Fallback Troubleshooting Engine
            String lower = query.toLowerCase();
            if (lower.contains("vpn")) {
                responseBuilder.append("This appears to be a **VPN connectivity issue**.\n\n")
                        .append("Try these standard troubleshooting steps:\n")
                        .append("1. Verify your local internet connection is active.\n")
                        .append("2. Close and restart the VPN client application completely.\n")
                        .append("3. Verify your multi-factor authentication (MFA) push notification or OTP.\n")
                        .append("4. Switch network connection (e.g. from Wi-Fi to wired hotspot) and retry.\n\n")
                        .append("If the connection still fails, please create an IT support ticket below.");
            } else if (lower.contains("wifi") || lower.contains("wi-fi") || lower.contains("internet")) {
                responseBuilder.append("This appears to be an **Office Wi-Fi / Network issue**.\n\n")
                        .append("Recommended steps:\n")
                        .append("1. Toggle your Wi-Fi adapter OFF and back ON.\n")
                        .append("2. Ensure you are connected to the official corporate SSID (`IntelliDesk-Corp`).\n")
                        .append("3. Forget the network and re-enter your corporate domain credentials.\n")
                        .append("4. Restart your workstation if the adapter remains disconnected.\n\n")
                        .append("Still facing issues? You can open a ticket for our Network Engineering team.");
            } else if (lower.contains("password") || lower.contains("login") || lower.contains("locked")) {
                responseBuilder.append("This appears to be an **Authentication / Password request**.\n\n")
                        .append("Quick self-service steps:\n")
                        .append("1. Visit the company Self-Service Password Reset (SSPR) portal.\n")
                        .append("2. If your account is locked due to multiple attempts, wait 15 minutes for auto-unlock.\n")
                        .append("3. Make sure CAPS LOCK is disabled.\n\n")
                        .append("If you need immediate manual unlock, submit a ticket below.");
            } else if (lower.contains("printer") || lower.contains("print")) {
                responseBuilder.append("This appears to be a **Printer issue**.\n\n")
                        .append("Suggested steps:\n")
                        .append("1. Check if the printer display shows paper jam or offline status.\n")
                        .append("2. Clear any stuck jobs in your local print queue.\n")
                        .append("3. Remove and re-add the printer via Settings > Printers & Scanners.\n\n")
                        .append("If hardware service is required, please log a ticket.");
            } else {
                responseBuilder.append("Thank you for reaching out to **IntelliDesk AI**.\n\n")
                        .append("I analyzed your request: *\"").append(query).append("\"*\n\n")
                        .append("General diagnostic suggestions:\n")
                        .append("1. Save all pending work and perform a system reboot.\n")
                        .append("2. Check if recent software updates or patches were installed.\n")
                        .append("3. Verify whether colleagues in your department are experiencing the same behavior.\n\n")
                        .append("I have classified your issue under **")
                        .append(classification.getSuggestedCategory())
                        .append("** with **")
                        .append(classification.getSuggestedPriority())
                        .append("** priority. Click below to submit this ticket directly to IT Support!");
            }
        }

        AiResponseDto responseDto = new AiResponseDto();
        responseDto.setResponse(responseBuilder.toString());
        responseDto.setSuggestedCategory(classification.getSuggestedCategory());
        responseDto.setSuggestedCategoryId(classification.getCategoryId());
        responseDto.setSuggestedPriority(classification.getSuggestedPriority());
        responseDto.setSuggestedArticles(articleDtos);
        responseDto.setCanCreateTicket(true);

        return responseDto;
    }
}
