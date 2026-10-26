package com.intellidesk.service;

import com.intellidesk.dto.AiRequestDto;
import com.intellidesk.dto.AiResponseDto;

public interface AiAssistantService {

    AiResponseDto askAssistant(AiRequestDto request);
}
