<template>
  <el-card class="result-card">
    <template #header>
      <div class="card-header">
        <span>批改结果</span>
        <el-tag :type="getScoreType(result.totalScore)" size="large">
          总分：{{ result.totalScore }}
        </el-tag>
      </div>
    </template>

    <div v-if="result.breakdown" class="breakdown-section">
      <h4>得分详情</h4>
      <el-row :gutter="20">
        <el-col :span="6" v-for="(score, key) in result.breakdown" :key="key">
          <div class="score-item">
            <span class="score-label">{{ getScoreLabel(key) }}</span>
            <span class="score-value">{{ score }}</span>
          </div>
        </el-col>
      </el-row>
    </div>

    <div v-if="result.errors && result.errors.length > 0" class="errors-section">
      <h4>错误标注</h4>
      <el-timeline>
        <el-timeline-item v-for="(error, index) in result.errors" :key="index" placement="top">
          <el-card shadow="hover">
            <div class="error-item">
              <div class="error-type">
                <el-tag size="small">{{ error.type }}</el-tag>
              </div>
              <div class="error-content">
                <p><strong>原文：</strong>{{ error.original }}</p>
                <p><strong>修改：</strong>{{ error.corrected }}</p>
                <p><strong>原因：</strong>{{ error.reason }}</p>
              </div>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </div>

    <div v-if="result.weaknesses" class="weaknesses-section">
      <h4>整体不足与修改建议</h4>
      <el-alert type="info" :closable="false" show-icon>
        {{ result.weaknesses }}
      </el-alert>
    </div>

    <div v-if="result.polishedEssay" class="polished-section">
      <h4>润色优化后的全文</h4>
      <el-card shadow="never" class="polished-content">
        {{ result.polishedEssay }}
      </el-card>
    </div>

    <div v-if="result.advancedPhrases && result.advancedPhrases.length > 0" class="phrases-section">
      <h4>高级表达</h4>
      <div class="phrases-tags">
        <el-tag
          v-for="(phrase, index) in result.advancedPhrases"
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
const props = defineProps({
  result: {
    type: Object,
    required: true
  }
})

const getScoreType = (score) => {
  if (score >= 16) return 'success'
  if (score >= 12) return 'warning'
  return 'danger'
}

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
</script>

<style scoped>
.result-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.breakdown-section,
.errors-section,
.weaknesses-section,
.polished-section,
.phrases-section {
  margin-top: 20px;
}

h4 {
  margin-bottom: 15px;
  color: #303133;
  font-weight: 600;
}

.score-item {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 6px;
  text-align: center;
}

.score-label {
  display: block;
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.score-value {
  display: block;
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.error-item {
  padding: 10px;
}

.error-type {
  margin-bottom: 8px;
}

.error-content p {
  margin: 6px 0;
  line-height: 1.5;
}

.polished-content {
  background-color: #f5f7fa;
  padding: 15px;
  line-height: 1.8;
  border-radius: 4px;
  white-space: pre-wrap;
}

.phrases-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>