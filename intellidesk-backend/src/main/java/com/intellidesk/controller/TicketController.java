package com.intellidesk.controller;

import com.intellidesk.dto.ApiResponse;
import com.intellidesk.dto.TicketCreateDto;
import com.intellidesk.dto.TicketDto;
import com.intellidesk.dto.TicketUpdateDto;
import com.intellidesk.entity.Priority;
import com.intellidesk.entity.TicketStatus;
import com.intellidesk.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TicketDto>>> getAllTickets(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "status", required = false) TicketStatus status,
            @RequestParam(name = "priority", required = false) Priority priority,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "assignedToId", required = false) Long assignedToId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "direction", defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TicketDto> tickets = ticketService.getAllTickets(search, status, priority, categoryId, assignedToId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Tickets retrieved", tickets));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<TicketDto>>> getMyTickets(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TicketDto> tickets = ticketService.getMyTickets(pageable);
        return ResponseEntity.ok(ApiResponse.success("My tickets retrieved", tickets));
    }

    @GetMapping("/assigned")
    @PreAuthorize("hasAnyRole('IT_AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<TicketDto>>> getAssignedTickets(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TicketDto> tickets = ticketService.getAssignedTickets(pageable);
        return ResponseEntity.ok(ApiResponse.success("Assigned tickets retrieved", tickets));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketDto>> getTicketById(@PathVariable("id") Long id) {
        TicketDto ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ApiResponse.success("Ticket retrieved", ticket));
    }

    @GetMapping("/number/{ticketNumber}")
    public ResponseEntity<ApiResponse<TicketDto>> getTicketByNumber(@PathVariable("ticketNumber") String ticketNumber) {
        TicketDto ticket = ticketService.getTicketByNumber(ticketNumber);
        return ResponseEntity.ok(ApiResponse.success("Ticket retrieved", ticket));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TicketDto>> createTicket(@Valid @RequestBody TicketCreateDto createDto) {
        TicketDto ticket = ticketService.createTicket(createDto);
        return new ResponseEntity<>(ApiResponse.success("Ticket created successfully", ticket), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketDto>> updateTicket(
            @PathVariable("id") Long id,
            @RequestBody TicketUpdateDto updateDto) {
        TicketDto ticket = ticketService.updateTicket(id, updateDto);
        return ResponseEntity.ok(ApiResponse.success("Ticket updated successfully", ticket));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('IT_AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<TicketDto>> updateStatus(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> payload) {
        TicketStatus status = TicketStatus.valueOf(payload.get("status").trim().toUpperCase());
        TicketDto ticket = ticketService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Ticket status updated", ticket));
    }

    @PutMapping("/{id}/priority")
    @PreAuthorize("hasAnyRole('IT_AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<TicketDto>> updatePriority(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> payload) {
        Priority priority = Priority.valueOf(payload.get("priority").trim().toUpperCase());
        TicketDto ticket = ticketService.updatePriority(id, priority);
        return ResponseEntity.ok(ApiResponse.success("Ticket priority updated", ticket));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('IT_AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<TicketDto>> assignTicket(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Long> payload) {
        Long agentId = payload.get("agentId");
        TicketDto ticket = ticketService.assignTicket(id, agentId);
        return ResponseEntity.ok(ApiResponse.success("Ticket assigned successfully", ticket));
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('IT_AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<TicketDto>> resolveTicket(@PathVariable("id") Long id) {
        TicketDto ticket = ticketService.resolveTicket(id);
        return ResponseEntity.ok(ApiResponse.success("Ticket marked as resolved", ticket));
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<ApiResponse<TicketDto>> closeTicket(@PathVariable("id") Long id) {
        TicketDto ticket = ticketService.closeTicket(id);
        return ResponseEntity.ok(ApiResponse.success("Ticket closed successfully", ticket));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTicket(@PathVariable("id") Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.ok(ApiResponse.success("Ticket deleted successfully"));
    }
}
