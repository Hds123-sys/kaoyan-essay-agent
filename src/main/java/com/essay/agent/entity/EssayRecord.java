package com.essay.agent.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "essay_records")
public class EssayRecord {
    @Id
    private String id;
    private String sessionId;
    private String essayType;
    private String topic;
    private String userEssay;
    private Integer totalScore;
    private String polishedEssay;
    private String weaknesses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}