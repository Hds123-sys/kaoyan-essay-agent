package com.essay.agent.service;

import com.essay.agent.entity.EssayRecord;
import org.springframework.data.domain.Page;

public interface HistoryService {
    EssayRecord saveRecord(EssayRecord record);
    EssayRecord getById(Long id);
    Page<EssayRecord> pageBySessionId(String sessionId, int page, int size);
}