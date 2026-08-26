package com.essay.agent.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("essay_record")
public class EssayRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("session_id")
    private String sessionId;

    @TableField("essay_type")
    private String essayType;

    private String topic;

    @TableField("user_essay")
    private String userEssay;

    @TableField("result_json")
    private String resultJson;

    @TableField("image_url")
    private String imageUrl;

    @TableField("template_id")
    private String templateId;

    @TableField("template_version")
    private String templateVersion;

    @TableField("user_disputed")
    private Integer userDisputed;

    @TableField("is_heavily_edited")
    private Integer isHeavilyEdited;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

}