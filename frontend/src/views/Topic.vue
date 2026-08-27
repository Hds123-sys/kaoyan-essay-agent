<template>
  <div class="topic-page">
    <el-card>
      <h2>随机出题</h2>
      <el-form :model="formData" label-width="100px">
        <el-form-item label="作文类型">
          <el-select v-model="formData.essayType" placeholder="选择作文类型">
            <el-option label="英语一图画作文" value="EN1_PICTURE" />
            <el-option label="英语一图表作文" value="EN1_CHART" />
            <el-option label="英语二图表作文" value="EN2_CHART" />
            <el-option label="小作文-书信" value="LETTER" />
            <el-option label="小作文-通知" value="NOTICE" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="formData.difficulty" placeholder="选择难度">
            <el-option label="简单" value="简单" />
            <el-option label="中等" value="中等" />
            <el-option label="困难" value="困难" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="formData.keywords" placeholder="输入关键词，多个关键词用逗号分隔" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleGenerate" :loading="loading">
            生成题目
          </el-button>
        </el-form-item>
      </el-form>

      <div v-if="topics.length > 0" class="topics-section">
        <h3>生成的题目</h3>
        <el-card v-for="(topic, index) in topics" :key="index" class="topic-card">
          <h4>题目 {{ index + 1 }}</h4>
          <p><strong>题目描述：</strong>{{ topic.topicDescription }}</p>
          <p><strong>写作要求：</strong>{{ topic.writingRequirements }}</p>
          <p><strong>词数要求：</strong>{{ topic.wordCount }}</p>
          <p><strong>难度：</strong>{{ topic.difficulty }}</p>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { generateTopic } from '@/api/topic'

const loading = ref(false)
const formData = ref({
  essayType: 'EN1_PICTURE',
  difficulty: '中等',
  keywords: ''
})

const topics = ref([])

const handleGenerate = async () => {
  loading.value = true
  try {
    const result = await generateTopic(formData.value.essayType, formData.value.difficulty, formData.value.keywords)
    topics.value = result
    ElMessage.success(`成功生成 ${result.length} 个题目`)
  } catch (error) {
    console.error('生成题目失败', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.topic-page {
  max-width: 1200px;
  margin: 0 auto;
}

h2 {
  margin-bottom: 20px;
  color: #303133;
}

.topics-section {
  margin-top: 20px;
}

.topics-section h3 {
  margin-bottom: 15px;
  color: #303133;
}

.topic-card {
  margin-bottom: 15px;
}

.topic-card h4 {
  margin: 10px 0;
  color: #409eff;
}

.topic-card p {
  margin: 8px 0;
  line-height: 1.6;
}
</style>