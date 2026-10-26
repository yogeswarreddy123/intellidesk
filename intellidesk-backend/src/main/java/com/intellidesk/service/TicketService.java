package com.intellidesk.service;

import com.intellidesk.dto.TicketCreateDto;
import com.intellidesk.dto.TicketDto;
import com.intellidesk.dto.TicketUpdateDto;
import com.intellidesk.entity.Priority;
import com.intellidesk.entity.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TicketService {

    TicketDto createTicket(TicketCreateDto createDto);

    TicketDto getTicketById(Long id);

    TicketDto getTicketByNumber(String ticketNumber);

    Page<TicketDto> getAllTickets(String search, TicketStatus status, Priority priority,
                                  Long categoryId, Long assignedToId, Pageable pageable);

    Page<TicketDto> getMyTickets(Pageable pageable);

    Page<TicketDto> getAssignedTickets(Pageable pageable);

    TicketDto updateTicket(Long id, TicketUpdateDto updateDto);

    TicketDto updateStatus(Long id, TicketStatus status);

    TicketDto updatePriority(Long id, Priority priority);

    TicketDto assignTicket(Long id, Long agentId);

    TicketDto resolveTicket(Long id);

    TicketDto closeTicket(Long id);

    void deleteTicket(Long id);
}
