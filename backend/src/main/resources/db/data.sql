-- 随机出题模板
INSERT INTO `prompt_template` (`template_name`, `version`, `content`, `essay_type`, `temperature`, `enabled`)
VALUES (
    'topic_generate',
    '1.0',
    '你是考研英语命题专家。请生成一道【{{essay_type}}】模拟考题。

要求：
1. 不要使用历年真题原题，生成全新的模拟题目
2. 题目内容符合考研英语命题风格和难度
3. 词数要求：{{word_count}}
4. 输出严格为JSON格式，不要包含任何额外文字

JSON格式：
{
  "topic_description": "题目描述（图画/图表/书信场景的文字描述）",
  "writing_requirements": "写作要求（如：描述图画、阐释寓意、给出建议）",
  "word_count": 160,
  "difficulty": "中等"
}',
    'ALL',
    1.00,
    1
);

-- 范文生成模板
INSERT INTO `prompt_template` (`template_name`, `version`, `content`, `essay_type`, `temperature`, `enabled`)
VALUES (
    'essay_reference',
    '1.0',
    '你是考研英语写作满分选手。请根据以下题目，写一篇符合考研评分标准的参考范文。

作文类型：{{essay_type}}
题目：{{topic}}

要求：
1. 范文符合考研英语评分标准，词汇和句式有一定多样性
2. 词数符合该类型要求
3. 输出严格为JSON格式

JSON格式：
{
  "reference_essay": "范文全文",
  "word_count": 180,
  "highlights": ["亮点表达1", "亮点表达2", "亮点表达3"]
}',
    'ALL',
    0.60,
    1
);

-- 大作文批改模板
INSERT INTO `prompt_template` (`template_name`, `version`, `content`, `essay_type`, `temperature`, `enabled`)
VALUES (
    'essay_correct_major',
    '1.0',
    '你是考研英语阅卷组资深专家。请按照考研英语【{{essay_type}}】的官方评分标准批改以下作文。

评分标准（{{scoring_criteria}}）：
- 内容完整性（0-5分）：是否切题、内容是否完整
- 语言准确性（0-5分）：语法错误、用词准确性
- 词汇多样性（0-5分）：词汇丰富度、搭配准确性
- 句式丰富度（0-5分）：句型变化、复杂句使用

作文题目：{{topic}}
学生原文：
{{user_essay}}

{{#history_summary}}
历史对话摘要：{{history_summary}}
{{/history_summary}}

请输出严格的JSON格式，不要包含任何额外文字：
{
  "total_score": 15,
  "breakdown": {
    "content": 4,
    "language": 3,
    "vocabulary": 4,
    "structure": 4
  },
  "errors": [
    {
      "original": "原文错误片段",
      "corrected": "修改后",
      "reason": "错误原因",
      "type": "语法"
    }
  ],
  "weaknesses": "整体不足与修改建议",
  "polished_essay": "润色优化后的全文",
  "advanced_phrases": ["高级表达1", "高级表达2"]
}',
    'EN1_PICTURE',
    0.30,
    1
);

-- 小作文批改模板
INSERT INTO `prompt_template` (`template_name`, `version`, `content`, `essay_type`, `temperature`, `enabled`)
VALUES (
    'essay_correct_letter',
    '1.0',
    '你是考研英语应用文阅卷专家。请按照考研英语小作文（书信/通知）的官方评分标准批改以下作文。

评分标准：
- 格式规范（0-5分）：书信格式、称呼、落款是否正确
- 交际得体（0-5分）：语气是否恰当、信息传达是否有效
- 信息完整（0-5分）：要点是否覆盖
- 语言准确（0-5分）：语法、用词、拼写

作文题目：{{topic}}
学生原文：
{{user_essay}}

{{#history_summary}}
历史对话摘要：{{history_summary}}
{{/history_summary}}

请输出严格的JSON格式，不要包含任何额外文字：
{
  "total_score": 13,
  "breakdown": {
    "format": 4,
    "appropriacy": 3,
    "completeness": 3,
    "language": 3
  },
  "errors": [
    {
      "original": "原文错误片段",
      "corrected": "修改后",
      "reason": "错误原因",
      "type": "语法"
    }
  ],
  "weaknesses": "整体不足与修改建议",
  "polished_essay": "润色优化后的全文",
  "advanced_phrases": ["高级表达1", "高级表达2"]
}',
    'LETTER',
    0.30,
    1
);