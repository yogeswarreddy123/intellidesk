package com.intellidesk.service;

import com.intellidesk.dto.AuditLogDto;
import com.intellidesk.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuditLogService {

    void log(User user, String action, String entityType, String entityId, String description);

    void log(String action, String entityType, String entityId, String description);

    Page<AuditLogDto> getAllLogs(Pageable pageable);

    List<AuditLogDto> getRecentLogs();

    List<AuditLogDto> getLogsForEntity(String entityType, String entityId);
}
