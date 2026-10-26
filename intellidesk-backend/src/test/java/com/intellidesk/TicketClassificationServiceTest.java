package com.intellidesk;

import com.intellidesk.dto.ClassificationRequestDto;
import com.intellidesk.dto.ClassificationResponseDto;
import com.intellidesk.entity.Priority;
import com.intellidesk.service.TicketClassificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class TicketClassificationServiceTest {

    @Autowired
    private TicketClassificationService classificationService;

    @Test
    public void testClassifyNetworkIssue() {
        ClassificationRequestDto request = new ClassificationRequestDto(
                "My laptop cannot connect to office Wi-Fi",
                "Wi-Fi keeps dropping"
        );

        ClassificationResponseDto result = classificationService.classifyTicket(request);
        assertNotNull(result);
        assertEquals("Network", result.getSuggestedCategory());
        assertEquals(Priority.MEDIUM, result.getSuggestedPriority());
    }

    @Test
    public void testClassifyProductionOutage() {
        ClassificationRequestDto request = new ClassificationRequestDto(
                "Production application is down for all users",
                "Outage affecting customer portal"
        );

        ClassificationResponseDto result = classificationService.classifyTicket(request);
        assertNotNull(result);
        assertEquals("Application", result.getSuggestedCategory());
        assertEquals(Priority.CRITICAL, result.getSuggestedPriority());
    }
}
