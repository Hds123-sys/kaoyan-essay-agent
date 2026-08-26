package com.essay.agent.model.enums;

import lombok.Getter;

@Getter
public enum EssayType {

    EN1_PICTURE("英语一图画大作文", "英语一图画大作文，20分满分。侧重图画描述准确性和寓意提炼深度。第一段描述图画，第二段阐释寓意，第三段给出建议/评论。"),
    EN2_CHART("英语二图表大作文", "英语二图表大作文，15分满分。侧重图表数据分析和趋势归纳能力。第一段描述图表数据，第二段分析原因，第三段给出预测/建议。"),
    LETTER("应用文（书信/通知）", "应用文（书信/通知），10分满分。侧重格式规范、交际得体性、信息完整性。词数要求100词左右。");

    private final String description;

    private final String scoringCriteria;

    EssayType(String description, String scoringCriteria) {
        this.description = description;
        this.scoringCriteria = scoringCriteria;
    }

}