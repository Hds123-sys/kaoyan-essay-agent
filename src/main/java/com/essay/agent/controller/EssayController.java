package com.essay.agent.controller;

import com.essay.agent.common.constant.ErrorCodeConstants;
import com.essay.agent.common.context.SessionContext;
import com.essay.agent.common.exception.BusinessException;
import com.essay.agent.common.util.LanguageDetector;
import com.essay.agent.common.util.SensitiveWordFilter;
import com.essay.agent.common.util.WordCounter;
import com.essay.agent.model.EssayType;
import com.essay.agent.model.ReferenceResult;
import com.essay.agent.entity.EssayRecord;
import com.essay.agent.model.dto.request.ReCorrectRequest;
import com.essay.agent.model.dto.request.EssayReferenceRequest;
import com.essay.agent.model.dto.request.EssayCorrectRequest;
import com.essay.agent.model.dto.response.ApiResponse;
import com.essay.agent.model.dto.response.EssayCorrectResponse;
import com.essay.agent.service.AgentDispatcher;
import com.essay.agent.service.HistoryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/essay")
public class EssayController {

    @Autowired
    private AgentDispatcher agentDispatcher;

    @Autowired
    private HistoryService historyService;

    @PostMapping("/correct")
    public ApiResponse<EssayCorrectResponse> correct(@Valid @RequestBody EssayCorrectRequest request) {
        String sessionId = SessionContext.getSessionId();
        if (sessionId == null) {
            throw new BusinessException(ErrorCodeConstants.SESSION_INVALID, "会话无效");
        }

        String userEssay = request.getUserEssay().trim();

        if (userEssay.length() < 10) {
            throw new BusinessException(ErrorCodeConstants.ESSAY_EMPTY, "作文内容过短，请至少输入10个字符");
        }

        if (!LanguageDetector.isEnglishEssay(userEssay)) {
            throw new BusinessException(ErrorCodeConstants.NON_ENGLISH, "检测到非英文内容占比过高，请输入英文作文");
        }

        int wordCount = WordCounter.count(userEssay);
        if (wordCount > 800) {
            throw new BusinessException(ErrorCodeConstants.ESSAY_TOO_LONG, "作文过长，请控制在800词以内");
        }

        if (SensitiveWordFilter.containsSensitiveWord(userEssay)) {
            throw new BusinessException(ErrorCodeConstants.SENSITIVE_CONTENT, "检测到敏感内容，请修改后重试");
        }

        EssayType essayType = request.getEssayType();
        if (essayType == null) {
            essayType = (EssayType) SessionContext.get("currentEssayType");
            if (essayType == null) {
                throw new BusinessException(ErrorCodeConstants.ESSAY_TYPE_REQUIRED, "请指定作文类型");
            }
        }

        SessionContext.set("currentEssayType", essayType);

        request.setEssayType(essayType);
        request.setSessionId(sessionId);
        if (request.getIsHeavilyEdited() == null) {
            request.setIsHeavilyEdited(false);
        }

        EssayCorrectResponse response = agentDispatcher.correct(request);

        Map<String, Object> meta = new HashMap<>();
        meta.put("templateId", response.getTemplateId());
        meta.put("templateVersion", response.getTemplateVersion());
        meta.put("summaryDegraded", response.isSummaryDegraded());
        meta.put("iterationCount", response.getIterationCount());

        log.info("Essay correction completed successfully. sessionId={}, wordCount={}, essayType={}",
                sessionId, wordCount, essayType);

        return ApiResponse.success(response, meta);
    }

    @PostMapping("/reference")
    public ApiResponse<ReferenceResult> reference(@Valid @RequestBody EssayReferenceRequest request) {
        String sessionId = SessionContext.getSessionId();
        if (sessionId == null) {
            throw new BusinessException(ErrorCodeConstants.SESSION_INVALID, "会话无效");
        }

        String topic = request.getTopic().trim();

        if (topic.length() < 5) {
            throw new BusinessException(ErrorCodeConstants.ESSAY_EMPTY, "题目内容过短，请至少输入5个字符");
        }

        if (request.getEssayType() == null) {
            throw new BusinessException(ErrorCodeConstants.ESSAY_TYPE_REQUIRED, "作文类型不能为空");
        }

        request.setSessionId(sessionId);

        ReferenceResult result = agentDispatcher.generateReference(request);

        log.info("Reference generation completed successfully. sessionId={}, essayType={}, degraded={}",
                sessionId, request.getEssayType(), result.isDegraded());

        return ApiResponse.success(result);
    }

    @PostMapping("/re-correct")
    public ApiResponse<EssayCorrectResponse> reCorrect(@Valid @RequestBody ReCorrectRequest request) {
        String sessionId = SessionContext.getSessionId();
        if (sessionId == null) {
            throw new BusinessException(ErrorCodeConstants.SESSION_INVALID, "会话无效");
        }

        EssayRecord originalRecord = historyService.getById(request.getRecordId());
        if (originalRecord == null) {
            throw new BusinessException(ErrorCodeConstants.SESSION_INVALID, "原记录不存在");
        }

        if (!originalRecord.getSessionId().equals(sessionId)) {
            throw new BusinessException(ErrorCodeConstants.UNAUTHORIZED_ACCESS, "越权访问");
        }

        EssayCorrectRequest correctRequest = new EssayCorrectRequest();
        correctRequest.setSessionId(sessionId);
        correctRequest.setTopic(request.getTopic() != null ? request.getTopic() : originalRecord.getTopic());
        correctRequest.setUserEssay(request.getUserEssay() != null ? request.getUserEssay() : originalRecord.getUserEssay());
        correctRequest.setEssayType(request.getEssayType() != null ? request.getEssayType() : EssayType.valueOf(originalRecord.getEssayType()));
        correctRequest.setImageUrl(null);
        correctRequest.setIsHeavilyEdited(true);

        EssayCorrectResponse response = agentDispatcher.correct(correctRequest);

        Map<String, Object> meta = new HashMap<>();
        meta.put("originalRecordId", request.getRecordId());
        meta.put("isReCorrect", true);

        log.info("Re-correction completed. sessionId={}, originalRecordId={}, newResult={}",
                sessionId, request.getRecordId(), response.getResult().isDegraded() ? "degraded" : "success");

        return ApiResponse.success(response, meta);
    }
}