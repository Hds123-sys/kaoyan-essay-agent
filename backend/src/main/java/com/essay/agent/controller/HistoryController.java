package com.essay.agent.controller;

import com.essay.agent.common.context.SessionContext;
import com.essay.agent.common.exception.BusinessException;
import com.essay.agent.model.dto.response.ApiResponse;
import com.essay.agent.model.dto.response.HistoryResponse;
import com.essay.agent.service.HistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    @GetMapping
    public ApiResponse<List<HistoryResponse>> getHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        String sessionId = SessionContext.get();
        log.info("Get history request - sessionId: {}, page: {}, size: {}", sessionId, page, size);

        if (page < 1 || size < 1 || size > 100) {
            throw new BusinessException(400, "分页参数不合法");
        }

        List<HistoryResponse> result = historyService.getHistory(page, size);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<HistoryResponse> getDetail(@PathVariable Long id) {
        String sessionId = SessionContext.get();
        log.info("Get history detail request - sessionId: {}, id: {}", sessionId, id);

        HistoryResponse result = historyService.getDetail(id);
        return ApiResponse.success(result);
    }
}