<template>
  <el-card class="result-card" shadow="never">
    <div class="score-header">
      <div class="total-score">
        <span class="score-label">总分</span>
        <span class="score-value" :class="getScoreClass(result.total_score)">
          {{ result.total_score }}
        </span>
        <span class="score-unit">/ {{ getMaxScore() }}</span>
      </div>
      <el-tag v-if="result.degraded" type="warning" size="small">
        降级结果
      </el-tag>
    </div>

    <!-- 分项得分 -->
    <div v-if="result.breakdown" class="breakdown-section">
      <h4>分项得分</h4>
      <el-row :gutter="12">
        <el-col
          :span="6"
          v-for="(score, key) in result.breakdown"
          :key="key"
        >
          <div class="score-item">
            <span class="score-label">{{ getScoreLabel(key) }}</span>
            <span class="score-value">{{ score }}</span>
            <span class="score-unit">/ 5</span>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 错误标注 -->
    <div v-if="result.errors && result.errors.length > 0" class="errors-section">
      <h4>错误标注（{{ result.errors.length }}处）</h4>
      <el-collapse accordion>
        <el-collapse-item
          v-for="(error, index) in result.errors"
          :key="index"
          :title="`${index + 1}. ${error.original}`"
        >
          <div class="error-item">
            <el-tag :type="getErrorType(error.type)" size="small" class="error-type-tag">
              {{ error.type }}
            </el-tag>
            <div class="error-detail">
              <p><strong>原文：</strong><span class="error-text">{{ error.original }}</span></p>
              <p><strong>修改：</strong><span class="corrected-text">{{ error.corrected }}</span></p>
              <p><strong>原因：</strong>{{ error.reason }}</p>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>

    <!-- 整体建议 -->
    <div v-if="result.weaknesses" class="weaknesses-section">
      <h4>整体不足与修改建议</h4>
      <el-alert type="info" :closable="false" show-icon>
        {{ result.weaknesses }}
      </el-alert>
    </div>

    <!-- 润色全文 -->
    <div v-if="result.polished_essay" class="polished-section">
      <div class="section-header">
        <h4>润色优化后的全文</h4>
        <el-button
          size="small"
          type="primary"
          @click="handleCopyPolished"
          :icon="CopyDocument"
        >
          复制
        </el-button>
      </div>
      <div class="polished-content">{{ result.polished_essay }}</div>
    </div>

    <!-- 高级表达 -->
    <div v-if="result.advanced_phrases && result.advanced_phrases.length > 0" class="phrases-section">
      <h4>可复用高级表达</h4>
      <div class="phrases-tags">
        <el-tag
          v-for="(phrase, index) in result.advanced_phrases"
          :key="index"
          type="success"
          effect="dark"
          style="margin-right: 8px; margin-bottom: 8px;"
        >
          {{ phrase }}
        </el-tag>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { CopyDocument } from '@element-plus/icons-vue'

const props = defineProps({
  result: {
    type: Object,
    required: true
  }
})

// 获取总分对应的CSS类
const getScoreClass = (score) => {
  const maxScore = getMaxScore()
  if (score >= maxScore * 0.8) return 'excellent'
  if (score >= maxScore * 0.6) return 'good'
  if (score >= maxScore * 0.4) return 'medium'
  return 'poor'
}

// 获取总分最大值
const getMaxScore = () => {
  // 大作文20分，小作文10分
  return props.result.total_score > 10 ? 20 : 10
}

// 获取分项标签
const getScoreLabel = (key) => {
  const labels = {
    content: '内容完整性',
    language: '语言准确性',
    vocabulary: '词汇多样性',
    structure: '句式丰富度',
    format: '格式规范',
    appropriacy: '交际得体',
    completeness: '信息完整'
  }
  return labels[key] || key
}

// 获取错误类型标签
const getErrorType = (type) => {
  const types = {
    '语法': 'danger',
    '用词': 'warning',
    '拼写': 'info'
  }
  return types[type] || 'info'
}

// 复制润色全文
const handleCopyPolished = () => {
  if (!props.result.polished_essay) return
  navigator.clipboard.writeText(props.result.polished_essay).then(() => {
    ElMessage.success('润色全文已复制')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}
</script>

<style scoped>
.result-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.score-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  border-radius: 8px 8px 0 0;
  margin-bottom: 20px;
}

.total-score {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.total-score .score-label {
  color: rgba(255, 255, 255, 0.8);
  font-size: 16px;
}

.total-score .score-value {
  font-size: 42px;
  font-weight: bold;
  color: #fff;
}

.total-score .score-value.excellent {
  color: #67c23a;
}

.total-score .score-value.good {
  color: #e6a23c;
}

.total-score .score-value.medium {
  color: #f56c6c;
}

.total-score .score-value.poor {
  color: #ff4d4f;
}

.total-score .score-unit {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.breakdown-section,
.errors-section,
.weaknesses-section,
.polished-section,
.phrases-section {
  margin: 20px 0;
}

h4 {
  margin-bottom: 15px;
  color: #303133;
  font-weight: 600;
  font-size: 16px;
}

.score-item {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 6px;
  text-align: center;
  transition: all 0.3s;
}

.score-item:hover {
  background-color: #e8f3ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.score-label {
  display: block;
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}

.score-value {
  display: block;
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
}

.score-unit {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.error-item {
  padding: 10px;
}

.error-type-tag {
  margin-bottom: 10px;
}

.error-detail p {
  margin: 8px 0;
  line-height: 1.6;
}

.error-text {
  color: #f56c6c;
  text-decoration: line-through;
}

.corrected-text {
  color: #67c23a;
  font-weight: bold;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.section-header h4 {
  margin: 0;
}

.polished-content {
  background-color: #f9f9f9;
  padding: 20px;
  line-height: 1.8;
  border-radius: 6px;
  white-space: pre-wrap;
  font-family: 'Georgia', 'Times New Roman', serif;
  font-size: 15px;
}

.phrases-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>