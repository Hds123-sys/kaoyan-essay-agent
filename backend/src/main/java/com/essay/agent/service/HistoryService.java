package com.essay.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.essay.agent.common.context.SessionContext;
import com.essay.agent.common.exception.BusinessException;
import com.essay.agent.common.util.WordCounter;
import com.essay.agent.mapper.EssayRecordMapper;
import com.essay.agent.model.dto.response.HistoryResponse;
import com.essay.agent.model.entity.EssayRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HistoryService {

    @Autowired
    private EssayRecordMapper essayRecordMapper;

    public List<HistoryResponse> getHistory(int page, int size) {
        String sessionId = SessionContext.get();

        QueryWrapper<EssayRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("session_id", sessionId)
                .orderByDesc("created_at")
                .last("LIMIT " + size + " OFFSET " + (page - 1) * size);

        List<EssayRecord> records = essayRecordMapper.selectList(queryWrapper);

        return records.stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    public HistoryResponse getDetail(Long id) {
        String sessionId = SessionContext.get();

        EssayRecord record = essayRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(404, "记录不存在");
        }

        if (!sessionId.equals(record.getSessionId())) {
            throw new BusinessException(403, "无权访问该记录");
        }

        return toHistoryResponse(record);
    }

    private HistoryResponse toHistoryResponse(EssayRecord record) {
        String summary = generateSummary(record.getUserEssay());

        return HistoryResponse.builder()
                .id(record.getId())
                .essayType(record.getEssayType())
                .topic(record.getTopic())
                .userEssay(record.getUserEssay())
                .imageUrl(record.getImageUrl())
                .templateVersion(record.getTemplateVersion())
                .createdAt(record.getCreatedAt())
                .summary(summary)
                .build();
    }

    private String generateSummary(String essay) {
        if (essay == null || essay.trim().isEmpty()) {
            return "";
        }

        String firstSentence = essay.split("[.!?]")[0].trim();
        if (firstSentence.length() > 50) {
            return firstSentence.substring(0, 50) + "...";
        }
        return firstSentence;
    }
}