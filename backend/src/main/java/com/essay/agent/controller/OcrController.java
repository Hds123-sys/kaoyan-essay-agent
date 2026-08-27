package com.essay.agent.controller;

import com.essay.agent.common.context.SessionContext;
import com.essay.agent.common.exception.BusinessException;
import com.essay.agent.model.dto.request.OcrRequest;
import com.essay.agent.model.dto.response.ApiResponse;
import com.essay.agent.model.dto.response.OcrResponse;
import com.essay.agent.service.OcrService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/ocr")
public class OcrController {

    @Autowired
    private OcrService ocrService;

    @PostMapping
    public ApiResponse<OcrResponse> ocr(@Valid @RequestBody OcrRequest request) {
        String sessionId = SessionContext.get();
        log.info("OCR request - sessionId: {}, imageUrl: {}", sessionId, request.getImageUrl());

        if (request.getImageUrl() == null || request.getImageUrl().trim().isEmpty()) {
            throw new BusinessException(400, "图片URL不能为空");
        }

        try {
            OcrResponse result = ocrService.ocr(request.getImageUrl());
            log.info("OCR completed - sessionId: {}, textLength: {}, language: {}",
                    sessionId, result.getText().length(), result.getLanguage());
            return ApiResponse.success(result);

        } catch (BusinessException e) {
            log.error("OCR failed - sessionId: {}, error: {}", sessionId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("OCR error - sessionId: {}", sessionId, e);
            throw new BusinessException(500, "OCR识别失败: " + e.getMessage());
        }
    }
}