package com.essay.agent.controller;

import com.essay.agent.common.constant.ErrorCodeConstants;
import com.essay.agent.common.context.SessionContext;
import com.essay.agent.common.exception.BusinessException;
import com.essay.agent.entity.EssayRecord;
import com.essay.agent.model.dto.response.ApiResponse;
import com.essay.agent.model.dto.response.PageResult;
import com.essay.agent.service.HistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    @GetMapping
    public ApiResponse<PageResult<EssayRecord>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        String sessionId = SessionContext.getSessionId();
        Page<EssayRecord> result = historyService.pageBySessionId(sessionId, page, size);

        log.info("History list retrieved. sessionId={}, page={}, size={}, total={}",
                sessionId, page, size, result.getTotalElements());

        return ApiResponse.success(PageResult.of(result));
    }

    @GetMapping("/{id}")
    public ApiResponse<EssayRecord> detail(@PathVariable Long id) {
        String currentSessionId = SessionContext.getSessionId();

        EssayRecord record = historyService.getById(id);
        if (record == null) {
            throw new BusinessException(ErrorCodeConstants.SESSION_INVALID, "记录不存在");
        }

        if (!record.getSessionId().equals(currentSessionId)) {
            throw new BusinessException(ErrorCodeConstants.UNAUTHORIZED_ACCESS, "越权访问");
        }

        log.info("History detail retrieved. sessionId={}, recordId={}", currentSessionId, id);

        return ApiResponse.success(record);
    }
}