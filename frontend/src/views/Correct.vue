<template>
  <div class="correct-page">
    <el-row :gutter="20">
      <!-- 左侧：输入区 -->
      <el-col :span="12" class="input-section">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>作文输入</span>
              <el-tag v-if="currentMeta" type="info" size="small">
                第{{ currentMeta.iteration_count }}轮对话
              </el-tag>
            </div>
          </template>

          <el-form :model="formData" label-width="100px">
            <el-form-item label="作文类型">
              <el-select v-model="formData.essayType" placeholder="选择作文类型" style="width: 100%;">
                <el-option label="英语一图画作文" value="EN1_PICTURE" />
                <el-option label="英语二图表作文" value="EN2_CHART" />
                <el-option label="应用文/小作文" value="LETTER" />
              </el-select>
            </el-form-item>

            <el-form-item label="题目">
              <el-input
                v-model="formData.topic"
                type="textarea"
                :rows="2"
                placeholder="会话中有题目可不填"
              />
            </el-form-item>

            <!-- 输入方式切换 -->
            <el-form-item label="输入方式">
              <el-radio-group v-model="inputMode">
                <el-radio-button value="text">文本输入</el-radio-button>
                <el-radio-button value="image">图片上传</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <!-- 文本输入 -->
            <el-form-item v-if="inputMode === 'text'" label="作文内容">
              <el-input
                v-model="formData.userEssay"
                type="textarea"
                :rows="15"
                placeholder="粘贴你的作文..."
                @input="handleTextChange"
              />
              <div class="word-count">
                <span :class="{ 'exceed': isWordCountExceed }">
                  {{ wordCount }} 词
                </span>
              </div>
            </el-form-item>

            <!-- 图片上传 -->
            <el-form-item v-if="inputMode === 'image'" label="图片上传">
              <OcrUpload
                @confirm="handleOcrConfirm"
                @heavily-edited="handleHeavilyEdited"
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                @click="handleSubmit"
                :loading="loading"
                :disabled="!canSubmit"
                style="width: 100%;"
              >
                {{ loading ? '批改中...' : '开始批改' }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：结果展示区 -->
      <el-col :span="12" class="result-section">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>批改结果</span>
              <div v-if="result" class="result-meta">
                <el-tag v-if="result.degraded" type="warning" size="small" style="margin-right: 8px;">
                  降级结果
                </el-tag>
                <el-tag v-if="currentMeta?.template_version" type="info" size="small">
                  v{{ currentMeta.template_version }}
                </el-tag>
              </div>
            </div>
          </template>

          <el-tabs v-model="resultTab" type="card">
            <!-- 当前结果Tab -->
            <el-tab-pane label="当前结果" name="current">
              <div v-if="!result" class="empty-result">
                <el-empty description="提交作文后将显示批改结果">
                  <el-icon :size="60"><Document /></el-icon>
                </el-empty>
              </div>

              <div v-else class="result-content">
                <ResultCard :result="result" />

                <!-- 重新批改按钮 -->
                <div class="result-actions">
                  <el-button
                    type="warning"
                    @click="handleReCorrect"
                    :loading="reCorrectLoading"
                    :disabled="!lastRecordId"
                  >
                    重新批改
                  </el-button>
                  <el-button type="primary" @click="handleExport">
                    导出报告
                  </el-button>
                </div>

                <!-- Meta信息展示 -->
                <el-divider />
                <div v-if="currentMeta" class="meta-info">
                  <el-descriptions :column="1" size="small" border>
                    <el-descriptions-item label="模板ID">
                      {{ currentMeta.template_id || 'N/A' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="模板版本">
                      {{ currentMeta.template_version || 'N/A' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="摘要降级">
                      <el-tag :type="currentMeta.summary_degraded ? 'warning' : 'success'" size="small">
                        {{ currentMeta.summary_degraded ? '是' : '否' }}
                      </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="迭代轮次">
                      {{ currentMeta.iteration_count || 0 }}
                    </el-descriptions-item>
                  </el-descriptions>
                </div>
              </div>
            </el-tab-pane>

            <!-- 对话历史Tab -->
            <el-tab-pane label="对话历史" name="history">
              <ChatHistory
                :messages="chatStore.messages"
                @view-result="handleViewResult"
                @clear="handleClearChat"
              />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { correct } from '@/api/essay'
import { useChatStore } from '@/stores/chat'
import { sanitizeMarkdown } from '@/api/report'
import { generateMarkdownReport, downloadMarkdownReport, generateReportFilename } from '@/utils/reportGenerator'
import ResultCard from '@/components/ResultCard.vue'
import OcrUpload from '@/components/OcrUpload.vue'
import ChatHistory from '@/components/ChatHistory.vue'

const route = useRoute()
const router = useRouter()
const chatStore = useChatStore()

const loading = ref(false)
const reCorrectLoading = ref(false)
const inputMode = ref('text')
const resultTab = ref('current') // 结果Tab切换
const formData = ref({
  essayType: 'EN1_PICTURE',
  topic: '',
  userEssay: '',
  imageUrl: '',
  isHeavilyEdited: false
})

const result = ref(null)
const currentMeta = ref(null)
const lastRecordId = ref(null)

// 词数统计
const wordCount = computed(() => {
  if (!formData.value.userEssay) return 0
  return formData.value.userEssay.trim().split(/\s+/).filter(word => word.length > 0).length
})

// 是否超过词数限制
const isWordCountExceed = computed(() => {
  return wordCount.value > 800
})

// 是否可以提交
const canSubmit = computed(() => {
  const text = formData.value.userEssay?.trim() || ''
  return text.length >= 10 && wordCount.value <= 800 && !isWordCountExceed.value
})

// 监听路由参数（从出题页面跳转过来时）
watch(() => route.query, (query) => {
  if (query.topic) {
    formData.value.topic = query.topic
  }
  if (query.essayType) {
    formData.value.essayType = query.essayType
  }
}, { immediate: true })

// 文本输入变化
const handleTextChange = () => {
  formData.value.imageUrl = ''
  formData.value.isHeavilyEdited = false
}

// OCR确认
const handleOcrConfirm = (text, imageUrl) => {
  formData.value.userEssay = text
  formData.value.imageUrl = imageUrl
  inputMode.value = 'text' // 切换到文本模式显示结果
}

// 重度编辑
const handleHeavilyEdited = (isHeavilyEdited) => {
  formData.value.isHeavilyEdited = isHeavilyEdited
}

// 提交批改
const handleSubmit = async () => {
  if (!canSubmit.value) {
    if (wordCount.value > 800) {
      ElMessage.warning('作文不能超过800词')
    } else if (formData.value.userEssay?.trim().length < 10) {
      ElMessage.warning('作文内容不能少于10个字符')
    }
    return
  }

  loading.value = true
  try {
    // 保存用户消息到对话历史
    chatStore.addUserMessage(formData.value.userEssay, formData.value.essayType)

    const response = await correct({
      userEssay: formData.value.userEssay,
      essayType: formData.value.essayType,
      topic: formData.value.topic || null,
      imageUrl: formData.value.imageUrl,
      isHeavilyEdited: formData.value.isHeavilyEdited
    })

    result.value = response
    lastRecordId.value = response.id || null

    // 保存助手消息到对话历史
    chatStore.addAssistantMessage(response, response.meta || {})

    if (response.meta) {
      currentMeta.value = response.meta
    }

    ElMessage.success('批改完成')

  } catch (error) {
    console.error('批改失败', error)
    chatStore.addSystemMessage('批改失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 重新批改
const handleReCorrect = async () => {
  if (!lastRecordId.value) {
    ElMessage.warning('没有可重新批改的记录')
    return
  }

  reCorrectLoading.value = true
  try {
    const response = await correct({
      recordId: lastRecordId.value,
      userEssay: formData.value.userEssay,
      essayType: formData.value.essayType,
      topic: formData.value.topic
    })

    result.value = response
    lastRecordId.value = response.id || null
    chatStore.addAssistantMessage(response, response.meta || {})

    if (response.meta) {
      currentMeta.value = response.meta
    }

    ElMessage.success('重新批改完成')

  } catch (error) {
    console.error('重新批改失败', error)
    chatStore.addSystemMessage('重新批改失败: ' + (error.message || '未知错误'))
  } finally {
    reCorrectLoading.value = false
  }
}

// 导出结果
const handleExport = () => {
  if (!result.value) return

  // 生成Markdown报告
  const markdown = generateMarkdownReport(
    result.value,
    formData.value.topic,
    formData.value.userEssay,
    formData.value.essayType
  )

  // 净化Markdown文本
  const sanitizedMarkdown = sanitizeMarkdown(markdown)

  // 生成带时间戳的文件名
  const filename = generateReportFilename()

  // 下载Markdown报告
  downloadMarkdownReport(sanitizedMarkdown, filename)

  ElMessage.success('报告已导出')
}

// 清空对话历史
const handleClearChat = () => {
  ElMessageBox.confirm('确定要清空对话历史吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    chatStore.clearMessages()
    result.value = null
    currentMeta.value = null
    lastRecordId.value = null
    ElMessage.success('对话历史已清空')
  }).catch(() => {})
}

// 查看历史结果
const handleViewResult = (message) => {
  if (message.result) {
    result.value = message.result
    currentMeta.value = message.meta || null
    resultTab.value = 'current'
  }
}
</script>

<style scoped>
.correct-page {
  padding: 20px;
  height: 100%;
  overflow-y: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.input-section,
.result-section {
  height: 100%;
}

.empty-result {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.word-count {
  text-align: right;
  margin-top: 5px;
  font-size: 14px;
  color: #909399;
}

.word-count .exceed {
  color: #f56c6c;
  font-weight: bold;
}

.result-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.meta-info {
  margin-top: 15px;
}

.meta-info :deep(.el-descriptions__label) {
  width: 100px;
}

.result-content {
  max-height: 600px;
  overflow-y: auto;
}
</style>