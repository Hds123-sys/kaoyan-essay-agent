-- 批改记录表
CREATE TABLE IF NOT EXISTS `essay_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `session_id` VARCHAR(36) NOT NULL COMMENT '会话 ID',
    `essay_type` VARCHAR(20) NOT NULL COMMENT '作文类型枚举',
    `topic` TEXT COMMENT '题目',
    `user_essay` TEXT NOT NULL COMMENT '学生原文',
    `result_json` TEXT COMMENT '批改结果 JSON',
    `image_url` VARCHAR(255) COMMENT '原始图片地址',
    `template_id` VARCHAR(64) COMMENT '本次使用的模板 ID',
    `template_version` VARCHAR(20) COMMENT '模板版本号',
    `user_disputed` TINYINT DEFAULT 0 COMMENT '争议标注',
    `is_heavily_edited` TINYINT DEFAULT 0 COMMENT '重度编辑标记',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_session_created` (`session_id`, `created_at` DESC),
    INDEX `idx_template_version` (`template_id`, `template_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='批改记录表';

-- Prompt 模板表
CREATE TABLE IF NOT EXISTS `prompt_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `version` VARCHAR(20) NOT NULL COMMENT '版本号',
    `content` TEXT NOT NULL COMMENT '模板内容',
    `essay_type` VARCHAR(20) COMMENT '适用作文类型',
    `temperature` DECIMAL(3,2) COMMENT '默认温度',
    `enabled` TINYINT DEFAULT 1 COMMENT '是否启用',
    UNIQUE KEY `uk_name_version` (`template_name`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Prompt 模板表';