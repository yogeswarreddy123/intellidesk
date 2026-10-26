package com.intellidesk.service;

import com.intellidesk.dto.CommentCreateDto;
import com.intellidesk.dto.CommentDto;

import java.util.List;

public interface CommentService {

    List<CommentDto> getCommentsForTicket(Long ticketId);

    CommentDto addComment(Long ticketId, CommentCreateDto createDto);
}
