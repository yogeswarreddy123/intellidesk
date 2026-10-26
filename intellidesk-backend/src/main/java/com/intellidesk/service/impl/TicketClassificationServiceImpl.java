package com.intellidesk.service.impl;

import com.intellidesk.dto.ClassificationRequestDto;
import com.intellidesk.dto.ClassificationResponseDto;
import com.intellidesk.entity.Category;
import com.intellidesk.entity.Priority;
import com.intellidesk.repository.CategoryRepository;
import com.intellidesk.service.TicketClassificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TicketClassificationServiceImpl implements TicketClassificationService {

    private final CategoryRepository categoryRepository;

    public TicketClassificationServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ClassificationResponseDto classifyTicket(ClassificationRequestDto request) {
        String text = (request.getTitle() + " " + (request.getDescription() != null ? request.getDescription() : ""))
                .toLowerCase();

        // 1. Determine Priority
        Priority priority = Priority.MEDIUM;
        String priorityReason = "Standard urgency detected.";

        if (containsAny(text, "production down", "prod down", "down for all", "company-wide", "outage",
                "ransomware", "security breach", "emergency", "critical", "all users unable", "data leak")) {
            priority = Priority.CRITICAL;
            priorityReason = "Detected critical keywords indicating system outage or severe business impact.";
        } else if (containsAny(text, "urgent", "blocker", "cannot work", "deadline", "executive", "vip", "major",
                "high priority", "severely impacted", "production issue", "payment failed")) {
            priority = Priority.HIGH;
            priorityReason = "Detected high-urgency keywords affecting productivity or immediate operations.";
        } else if (containsAny(text, "minor", "how to", "question", "inquiry", "enhancement", "cosmetic",
                "typo", "low priority", "when possible", "whenever", "information")) {
            priority = Priority.LOW;
            priorityReason = "Detected inquiry or low-impact general request.";
        }

        // 2. Determine Category
        String categoryName = "Software";
        String categoryReason = "Defaulted based on general software classification.";

        if (containsAny(text, "wifi", "wi-fi", "vpn", "dns", "internet", "ethernet", "router", "switch",
                "network", "ping", "packet loss", "ip address", "firewall", "gateway")) {
            categoryName = "Network";
            categoryReason = "Detected network and connectivity indicators.";
        } else if (containsAny(text, "laptop", "monitor", "keyboard", "mouse", "screen", "charger", "printer",
                "docking station", "hardware", "fan", "battery", "cables", "headset", "webcam")) {
            categoryName = "Hardware";
            categoryReason = "Detected physical equipment or peripheral keywords.";
        } else if (containsAny(text, "email", "outlook", "inbox", "mailbox", "smtp", "exchange", "calendar invite",
                "mail delivery")) {
            categoryName = "Email";
            categoryReason = "Detected messaging or email service keywords.";
        } else if (containsAny(text, "password", "reset", "login", "unlock", "access", "permission",
                "account locked", "mfa", "2fa", "credentials", "authenticate", "sso")) {
            categoryName = "Access";
            categoryReason = "Detected user authentication or account permission keywords.";
        } else if (containsAny(text, "virus", "malware", "phishing", "hacked", "suspicious", "breach",
                "vulnerability", "unauthorized", "compromised", "threat")) {
            categoryName = "Security";
            categoryReason = "Detected cybersecurity and threat indicators.";
        } else if (containsAny(text, "production application", "crm", "erp", "portal", "microservice",
                "api error", "500 internal", "app crash", "prod app", "web app", "service is down")) {
            categoryName = "Application";
            categoryReason = "Detected enterprise application and web service keywords.";
        } else if (containsAny(text, "database", "mysql", "postgres", "sql", "query", "mongodb",
                "oracle", "deadlock", "table", "schema", "db server")) {
            categoryName = "Database";
            categoryReason = "Detected database and SQL query indicators.";
        }

        // Match with database Category entity
        Long categoryId = null;
        Optional<Category> matchedCategory = categoryRepository.findByName(categoryName);
        if (matchedCategory.isPresent()) {
            categoryId = matchedCategory.get().getId();
            categoryName = matchedCategory.get().getName();
        } else {
            // Find first available category if exact name not found
            List<Category> allCategories = categoryRepository.findAll();
            if (!allCategories.isEmpty()) {
                categoryId = allCategories.get(0).getId();
                categoryName = allCategories.get(0).getName();
            }
        }

        double confidence = 0.90;
        String fullReason = String.format("Suggested %s (Confidence: 90%%). Reason: %s %s",
                categoryName, categoryReason, priorityReason);

        return new ClassificationResponseDto(categoryName, categoryId, priority, confidence, fullReason);
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) {
                return true;
            }
        }
        return false;
    }
}
