package com.intellidesk.service.impl;

import com.intellidesk.dto.CommentCreateDto;
import com.intellidesk.dto.CommentDto;
import com.intellidesk.entity.Comment;
import com.intellidesk.entity.Role;
import com.intellidesk.entity.Ticket;
import com.intellidesk.entity.User;
import com.intellidesk.exception.ResourceNotFoundException;
import com.intellidesk.exception.UnauthorizedException;
import com.intellidesk.repository.CommentRepository;
import com.intellidesk.repository.TicketRepository;
import com.intellidesk.repository.UserRepository;
import com.intellidesk.security.UserPrincipal;
import com.intellidesk.service.AuditLogService;
import com.intellidesk.service.CommentService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public CommentServiceImpl(CommentRepository commentRepository,
                              TicketRepository ticketRepository,
                              UserRepository userRepository,
                              AuditLogService auditLogService) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
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
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsForTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.EMPLOYEE && !ticket.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to view comments for this ticket");
        }

        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(Long ticketId, CommentCreateDto createDto) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.EMPLOYEE && !ticket.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to add comments to this ticket");
        }

        Comment comment = new Comment(createDto.getMessage().trim(), ticket, currentUser);
        Comment saved = commentRepository.save(comment);

        auditLogService.log(currentUser, "ADD_COMMENT", "TICKET", ticket.getTicketNumber(),
                "Added comment to ticket: " + ticket.getTicketNumber());

        return mapToDto(saved);
    }

    private CommentDto mapToDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setMessage(comment.getMessage());
        dto.setTicketId(comment.getTicket().getId());
        dto.setUserId(comment.getUser().getId());
        dto.setUserName(comment.getUser().getFullName());
        dto.setUserEmail(comment.getUser().getEmail());
        dto.setUserRole(comment.getUser().getRole());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}
