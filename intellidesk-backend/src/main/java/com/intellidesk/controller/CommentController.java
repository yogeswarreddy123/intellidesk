package com.intellidesk.controller;

import com.intellidesk.dto.ApiResponse;
import com.intellidesk.dto.CommentCreateDto;
import com.intellidesk.dto.CommentDto;
import com.intellidesk.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentDto>>> getComments(@PathVariable("ticketId") Long ticketId) {
        List<CommentDto> comments = commentService.getCommentsForTicket(ticketId);
        return ResponseEntity.ok(ApiResponse.success("Comments retrieved", comments));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommentDto>> addComment(
            @PathVariable("ticketId") Long ticketId,
            @Valid @RequestBody CommentCreateDto createDto) {
        CommentDto comment = commentService.addComment(ticketId, createDto);
        return new ResponseEntity<>(ApiResponse.success("Comment added", comment), HttpStatus.CREATED);
    }
}
