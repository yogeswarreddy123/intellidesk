package com.intellidesk.service.impl;

import com.intellidesk.dto.TicketCreateDto;
import com.intellidesk.dto.TicketDto;
import com.intellidesk.dto.TicketUpdateDto;
import com.intellidesk.entity.*;
import com.intellidesk.exception.BadRequestException;
import com.intellidesk.exception.ResourceNotFoundException;
import com.intellidesk.exception.UnauthorizedException;
import com.intellidesk.repository.*;
import com.intellidesk.security.UserPrincipal;
import com.intellidesk.service.AuditLogService;
import com.intellidesk.service.TicketService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final CategoryRepository categoryRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final AuditLogService auditLogService;

    public TicketServiceImpl(TicketRepository ticketRepository,
                             CategoryRepository categoryRepository,
                             DepartmentRepository departmentRepository,
                             UserRepository userRepository,
                             CommentRepository commentRepository,
                             AuditLogService auditLogService) {
        this.ticketRepository = ticketRepository;
        this.categoryRepository = categoryRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.auditLogService = auditLogService;
    }

    private synchronized String generateNextTicketNumber() {
        Long maxId = ticketRepository.findMaxId();
        long nextNum = (maxId == null ? 1 : maxId + 1);
        return String.format("INC-%05d", 10000 + nextNum);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal)) {
            throw new UnauthorizedException("User not authenticated");
        }
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));
    }

    @Override
    public TicketDto createTicket(TicketCreateDto createDto) {
        User currentUser = getCurrentUser();

        Category category = categoryRepository.findById(createDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", createDto.getCategoryId()));

        Department department = null;
        if (createDto.getDepartmentId() != null) {
            department = departmentRepository.findById(createDto.getDepartmentId())
                    .orElse(null);
        } else if (currentUser.getDepartment() != null) {
            department = currentUser.getDepartment();
        }

        Ticket ticket = new Ticket();
        ticket.setTicketNumber(generateNextTicketNumber());
        ticket.setTitle(createDto.getTitle().trim());
        ticket.setDescription(createDto.getDescription().trim());
        ticket.setCategory(category);
        ticket.setDepartment(department);
        ticket.setCreatedBy(currentUser);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(createDto.getPriority() != null ? createDto.getPriority() : Priority.MEDIUM);

        Ticket saved = ticketRepository.save(ticket);

        auditLogService.log(currentUser, "CREATE_TICKET", "TICKET", saved.getTicketNumber(),
                "Created ticket " + saved.getTicketNumber() + ": " + saved.getTitle());

        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDto getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        User currentUser = getCurrentUser();
        // Check access: employees can only view tickets they created
        if (currentUser.getRole() == Role.EMPLOYEE && !ticket.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to view this ticket");
        }

        return mapToDto(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDto getTicketByNumber(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "ticketNumber", ticketNumber));

        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.EMPLOYEE && !ticket.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to view this ticket");
        }

        return mapToDto(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketDto> getAllTickets(String search, TicketStatus status, Priority priority,
                                         Long categoryId, Long assignedToId, Pageable pageable) {
        User currentUser = getCurrentUser();

        Specification<Ticket> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // If employee, restrict strictly to own tickets
            if (currentUser.getRole() == Role.EMPLOYEE) {
                predicates.add(cb.equal(root.get("createdBy"), currentUser));
            }

            if (StringUtils.hasText(search)) {
                String searchLike = "%" + search.trim().toLowerCase() + "%";
                Predicate numMatch = cb.like(cb.lower(root.get("ticketNumber")), searchLike);
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), searchLike);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), searchLike);
                predicates.add(cb.or(numMatch, titleMatch, descMatch));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (assignedToId != null) {
                predicates.add(cb.equal(root.get("assignedTo").get("id"), assignedToId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return ticketRepository.findAll(spec, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketDto> getMyTickets(Pageable pageable) {
        User currentUser = getCurrentUser();
        return ticketRepository.findByCreatedBy(currentUser, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketDto> getAssignedTickets(Pageable pageable) {
        User currentUser = getCurrentUser();
        return ticketRepository.findByAssignedTo(currentUser, pageable).map(this::mapToDto);
    }

    @Override
    public TicketDto updateTicket(Long id, TicketUpdateDto updateDto) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.EMPLOYEE && !ticket.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to update this ticket");
        }

        if (StringUtils.hasText(updateDto.getTitle())) {
            ticket.setTitle(updateDto.getTitle().trim());
        }

        if (StringUtils.hasText(updateDto.getDescription())) {
            ticket.setDescription(updateDto.getDescription().trim());
        }

        if (updateDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(updateDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", updateDto.getCategoryId()));
            ticket.setCategory(category);
        }

        if (updateDto.getPriority() != null) {
            ticket.setPriority(updateDto.getPriority());
        }

        // Only agent/admin can reassign or change status through general update
        if (currentUser.getRole() != Role.EMPLOYEE) {
            if (updateDto.getStatus() != null) {
                ticket.setStatus(updateDto.getStatus());
            }
            if (updateDto.getAssignedToId() != null) {
                User agent = userRepository.findById(updateDto.getAssignedToId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", updateDto.getAssignedToId()));
                ticket.setAssignedTo(agent);
            }
        }

        if (updateDto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(updateDto.getDepartmentId())
                    .orElse(null);
            ticket.setDepartment(department);
        }

        Ticket saved = ticketRepository.save(ticket);
        auditLogService.log(currentUser, "UPDATE_TICKET", "TICKET", saved.getTicketNumber(),
                "Updated ticket: " + saved.getTicketNumber());

        return mapToDto(saved);
    }

    @Override
    public TicketDto updateStatus(Long id, TicketStatus status) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        User currentUser = getCurrentUser();
        ticket.setStatus(status);

        Ticket saved = ticketRepository.save(ticket);
        auditLogService.log(currentUser, "CHANGE_STATUS", "TICKET", saved.getTicketNumber(),
                "Changed status of " + saved.getTicketNumber() + " to " + status);

        return mapToDto(saved);
    }

    @Override
    public TicketDto updatePriority(Long id, Priority priority) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        User currentUser = getCurrentUser();
        ticket.setPriority(priority);

        Ticket saved = ticketRepository.save(ticket);
        auditLogService.log(currentUser, "CHANGE_PRIORITY", "TICKET", saved.getTicketNumber(),
                "Changed priority of " + saved.getTicketNumber() + " to " + priority);

        return mapToDto(saved);
    }

    @Override
    public TicketDto assignTicket(Long id, Long agentId) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent", "id", agentId));

        if (agent.getRole() == Role.EMPLOYEE) {
            throw new BadRequestException("Tickets cannot be assigned to employees");
        }

        ticket.setAssignedTo(agent);
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }

        Ticket saved = ticketRepository.save(ticket);
        auditLogService.log("ASSIGN_TICKET", "TICKET", saved.getTicketNumber(),
                "Assigned ticket " + saved.getTicketNumber() + " to " + agent.getFullName());

        return mapToDto(saved);
    }

    @Override
    public TicketDto resolveTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolvedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);
        auditLogService.log("RESOLVE_TICKET", "TICKET", saved.getTicketNumber(),
                "Resolved ticket " + saved.getTicketNumber());

        return mapToDto(saved);
    }

    @Override
    public TicketDto closeTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        User currentUser = getCurrentUser();
        // Employee can close if they created it; otherwise agent/admin
        if (currentUser.getRole() == Role.EMPLOYEE && !ticket.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to close this ticket");
        }

        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setClosedAt(LocalDateTime.now());
        if (ticket.getResolvedAt() == null) {
            ticket.setResolvedAt(LocalDateTime.now());
        }

        Ticket saved = ticketRepository.save(ticket);
        auditLogService.log(currentUser, "CLOSE_TICKET", "TICKET", saved.getTicketNumber(),
                "Closed ticket " + saved.getTicketNumber());

        return mapToDto(saved);
    }

    @Override
    public void deleteTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        ticketRepository.delete(ticket);
        auditLogService.log("DELETE_TICKET", "TICKET", ticket.getTicketNumber(),
                "Deleted ticket " + ticket.getTicketNumber());
    }

    private TicketDto mapToDto(Ticket ticket) {
        TicketDto dto = new TicketDto();
        dto.setId(ticket.getId());
        dto.setTicketNumber(ticket.getTicketNumber());
        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());
        dto.setStatus(ticket.getStatus());
        dto.setPriority(ticket.getPriority());

        if (ticket.getCategory() != null) {
            dto.setCategoryId(ticket.getCategory().getId());
            dto.setCategoryName(ticket.getCategory().getName());
        }

        if (ticket.getCreatedBy() != null) {
            dto.setCreatedById(ticket.getCreatedBy().getId());
            dto.setCreatedByName(ticket.getCreatedBy().getFullName());
            dto.setCreatedByEmail(ticket.getCreatedBy().getEmail());
        }

        if (ticket.getAssignedTo() != null) {
            dto.setAssignedToId(ticket.getAssignedTo().getId());
            dto.setAssignedToName(ticket.getAssignedTo().getFullName());
            dto.setAssignedToEmail(ticket.getAssignedTo().getEmail());
        }

        if (ticket.getDepartment() != null) {
            dto.setDepartmentId(ticket.getDepartment().getId());
            dto.setDepartmentName(ticket.getDepartment().getName());
        }

        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
        dto.setResolvedAt(ticket.getResolvedAt());
        dto.setClosedAt(ticket.getClosedAt());

        long count = commentRepository.countByTicketId(ticket.getId());
        dto.setCommentCount(count);

        return dto;
    }
}
