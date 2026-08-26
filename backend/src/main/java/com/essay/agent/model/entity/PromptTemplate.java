package com.essay.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("prompt_template")
public class PromptTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("template_name")
    private String templateName;

    private String version;

    private String content;

    @TableField("essay_type")
    private String essayType;

    private BigDecimal temperature;

    private Integer enabled;

}