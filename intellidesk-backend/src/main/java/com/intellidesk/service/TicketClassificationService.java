package com.intellidesk.service;

import com.intellidesk.dto.ClassificationRequestDto;
import com.intellidesk.dto.ClassificationResponseDto;

public interface TicketClassificationService {

    ClassificationResponseDto classifyTicket(ClassificationRequestDto request);
}
