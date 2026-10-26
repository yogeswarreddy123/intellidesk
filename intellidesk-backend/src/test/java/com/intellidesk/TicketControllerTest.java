package com.intellidesk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellidesk.dto.AuthRequest;
import com.intellidesk.dto.TicketCreateDto;
import com.intellidesk.entity.Category;
import com.intellidesk.entity.Priority;
import com.intellidesk.repository.CategoryRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    private String adminToken;
    private String employeeToken;
    private Long categoryId;

    @BeforeEach
    public void setup() throws Exception {
        // Admin Token
        AuthRequest adminAuth = new AuthRequest("admin@intellidesk.com", "admin123");
        MvcResult adminResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminAuth)))
                .andExpect(status().isOk())
                .andReturn();
        adminToken = JsonPath.read(adminResult.getResponse().getContentAsString(), "$.data.token");

        // Employee Token
        AuthRequest empAuth = new AuthRequest("employee@intellidesk.com", "employee123");
        MvcResult empResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empAuth)))
                .andExpect(status().isOk())
                .andReturn();
        employeeToken = JsonPath.read(empResult.getResponse().getContentAsString(), "$.data.token");

        Category cat = categoryRepository.findAll().get(0);
        categoryId = cat.getId();
    }

    @Test
    public void testCreateTicketAndGetTicket() throws Exception {
        TicketCreateDto createDto = new TicketCreateDto();
        createDto.setTitle("Outlook client crashes on startup");
        createDto.setDescription("Every time I start Outlook, it crashes with error 0x80004005.");
        createDto.setCategoryId(categoryId);
        createDto.setPriority(Priority.HIGH);

        MvcResult createResult = mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ticketNumber").isNotEmpty())
                .andExpect(jsonPath("$.data.title").value("Outlook client crashes on startup"))
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andReturn();

        Integer ticketId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.id");

        // Get by ID
        mockMvc.perform(get("/api/tickets/" + ticketId)
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(ticketId));
    }

    @Test
    public void testAgentUpdateStatus() throws Exception {
        Map<String, String> statusUpdate = new HashMap<>();
        statusUpdate.put("status", "IN_PROGRESS");

        mockMvc.perform(put("/api/tickets/1/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    public void testAuthorization_EmployeeCannotAccessAdminUsers() throws Exception {
        // Employee attempting to access Admin-only /api/users should receive 403 Forbidden
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testValidation_EmptyTitleRejected() throws Exception {
        TicketCreateDto invalidDto = new TicketCreateDto();
        invalidDto.setTitle(""); // empty
        invalidDto.setDescription("Valid description");
        invalidDto.setCategoryId(categoryId);

        mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
