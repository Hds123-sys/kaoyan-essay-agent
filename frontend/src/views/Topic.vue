<template>
  <div class="topic-page">
    <el-card shadow="hover">
      <template #header>
        <span>随机出题</span>
      </template>

      <el-form :model="formData" label-width="100px">
        <el-form-item label="作文类型">
          <el-select v-model="formData.essayType" placeholder="选择作文类型" style="width: 100%;">
            <el-option label="英语一图画作文" value="EN1_PICTURE" />
            <el-option label="英语二图表作文" value="EN2_CHART" />
            <el-option label="应用文/小作文" value="LETTER" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            @click="handleGenerate"
            :loading="loading"
            style="width: 100%;"
          >
            {{ loading ? '生成中...' : '生成题目' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div v-if="topicResult" class="topic-result">
        <el-divider content-position="left">生成的题目</el-divider>

        <el-descriptions :column="1" border>
          <el-descriptions-item label="题目描述">
            <div class="topic-description">{{ topicResult.topic_description }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="写作要求">
            <div class="writing-requirements">{{ topicResult.writing_requirements }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="词数要求">
            {{ topicResult.word_count }} 词
          </el-descriptions-item>
          <el-descriptions-item label="难度">
            <el-tag :type="getDifficultyType(topicResult.difficulty)">
              {{ topicResult.difficulty }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div class="result-actions">
          <el-button
            type="success"
            @click="handleGoToWrite"
            :icon="Edit"
          >
            去写作文
          </el-button>
          <el-button
            @click="handleCopyTopic"
            :icon="CopyDocument"
          >
            复制题目
          </el-button>
          <el-button
            type="warning"
            @click="handleRegenerate"
            :loading="loading"
            :icon="Refresh"
          >
            重新生成
          </el-button>
        </div>
      </div>

      <el-empty v-else description="点击生成按钮获取模拟题目" />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { generateTopic } from '@/api/topic'
import { Edit, CopyDocument, Refresh } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const formData = ref({
  essayType: 'EN1_PICTURE'
})

const topicResult = ref(null)

// 生成题目
const handleGenerate = async () => {
  loading.value = true
  try {
    const result = await generateTopic(formData.value.essayType)
    topicResult.value = result
    ElMessage.success('题目生成成功')
  } catch (error) {
    console.error('生成题目失败', error)
    ElMessage.error('生成题目失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 去写作文（跳转到批改页面）
const handleGoToWrite = () => {
  if (!topicResult.value) return

  const fullTopic = `题目描述：${topicResult.value.topic_description}\n写作要求：${topicResult.value.writing_requirements}`

  router.push({
    path: '/correct',
    query: {
      topic: fullTopic,
      essayType: formData.value.essayType
    }
  })
}

// 复制题目
const handleCopyTopic = () => {
  if (!topicResult.value) return

  const fullTopic = `题目描述：${topicResult.value.topic_description}\n写作要求：${topicResult.value.writing_requirements}`

  navigator.clipboard.writeText(fullTopic).then(() => {
    ElMessage.success('题目已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

// 重新生成
const handleRegenerate = () => {
  topicResult.value = null
  handleGenerate()
}

// 获取难度对应的标签类型
const getDifficultyType = (difficulty) => {
  const types = {
    '简单': 'success',
    '中等': 'warning',
    '困难': 'danger'
  }
  return types[difficulty] || 'info'
}
</script>

<style scoped>
.topic-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.topic-result {
  margin-top: 20px;
}

.topic-description,
.writing-requirements {
  line-height: 1.8;
  white-space: pre-wrap;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.result-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  justify-content: center;
}
</style>