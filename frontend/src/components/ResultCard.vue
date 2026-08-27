<template>
  <el-card class="result-card" shadow="never">
    <!-- 降级结果警告 -->
    <el-alert
      v-if="result.degraded"
      type="warning"
      :closable="false"
      show-icon
      style="margin-bottom: 20px;"
    >
      本次为降级结果，格式解析失败，仅供参考
    </el-alert>

    <div class="score-header">
      <div class="total-score">
        <span class="score-label">总分</span>
        <span class="score-value" :class="getScoreClass(result.total_score)">
          {{ result.total_score }}
        </span>
        <span class="score-unit">/ {{ getMaxScore() }}</span>
      </div>
    </div>

    <!-- 分项得分 -->
    <div v-if="result.breakdown" class="breakdown-section">
      <h4>分项得分</h4>
      <el-space direction="vertical" :size="12" style="width: 100%;">
        <div v-for="(score, key) in result.breakdown" :key="key" class="score-breakdown-item">
          <div class="score-breakdown-header">
            <span class="score-label">{{ getScoreLabel(key) }}</span>
            <span class="score-value">{{ score }} / 5</span>
          </div>
          <el-progress :percentage="(score / 5) * 100" :color="getProgressColor(score)" />
        </div>
      </el-space>
    </div>

    <!-- 错误标注 -->
    <div v-if="result.errors && result.errors.length > 0" class="errors-section">
      <h4>错误标注（{{ result.errors.length }}处）</h4>
      <el-table :data="result.errors" stripe border style="width: 100%">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="original" label="原文" min-width="150">
          <template #default="scope">
            <span class="error-text">{{ scope.row.original }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="corrected" label="修改后" min-width="150">
          <template #default="scope">
            <span class="corrected-text">{{ scope.row.corrected }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="错误原因" min-width="200" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="getErrorType(scope.row.type)" size="small">
              {{ scope.row.type }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
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

// 获取进度条颜色
const getProgressColor = (score) => {
  if (score >= 4) return '#67c23a'
  if (score >= 3) return '#e6a23c'
  if (score >= 2) return '#f56c6c'
  return '#ff4d4f'
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

.score-breakdown-item {
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 6px;
}

.score-breakdown-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.error-text {
  color: #f56c6c;
  text-decoration: line-through;
  font-weight: 500;
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

@media (max-width: 768px) {
  .score-header {
    padding: 15px;
  }

  .total-score .score-value {
    font-size: 32px;
  }

  .polished-content {
    padding: 15px;
    font-size: 14px;
  }

  .el-table {
    font-size: 12px;
  }

  .score-breakdown-item {
    padding: 10px;
  }
}
</style>