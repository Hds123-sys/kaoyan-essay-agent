<template>
  <el-card class="chat-history-card" shadow="never">
    <div class="chat-container" ref="chatContainer">
      <div
        v-for="(message, index) in messages"
        :key="index"
        :class="['message', message.type]"
      >
        <div class="message-header">
          <span class="message-role">
            <el-icon v-if="message.type === 'user'"><User /></el-icon>
            <el-icon v-else-if="message.type === 'assistant'"><ChatDotRound /></el-icon>
            <el-icon v-else><Bell /></el-icon>
            {{ message.role === 'user' ? '学生' : message.role === 'assistant' ? '批改系统' : '系统' }}
          </span>
          <span class="message-time">{{ message.time }}</span>
        </div>

        <div class="message-content">
          <!-- 用户消息：显示作文内容 -->
          <div v-if="message.type === 'user'" class="user-essay">
            <div class="essay-type-tag">
              <el-tag size="small" type="primary">{{ getEssayTypeLabel(message.essayType) }}</el-tag>
            </div>
            <div class="essay-text">{{ message.content }}</div>
          </div>

          <!-- 助手消息：显示批改结果 -->
          <div v-else-if="message.type === 'assistant'" class="assistant-response">
            <div v-if="message.result" class="result-summary">
              <div class="score-badge">
                <span class="score-label">得分</span>
                <span class="score-value">{{ message.result.total_score }}</span>
              </div>
              <div class="meta-info">
                <el-tag v-if="message.meta?.template_version" size="small" type="info">
                  模板 v{{ message.meta.template_version }}
                </el-tag>
                <el-tag v-if="message.meta?.summary_degraded" size="small" type="warning">
                  降级
                </el-tag>
              </div>
            </div>
            <div class="response-text">{{ message.content }}</div>
          </div>

          <!-- 系统消息 -->
          <div v-else-if="message.type === 'system'" class="system-message">
            <el-alert :type="getSystemMessageType(message.content)" :closable="false">
              {{ message.content }}
            </el-alert>
          </div>
        </div>
      </div>

      <el-empty v-if="messages.length === 0" description="暂无对话记录" :image-size="100" />
    </div>

    <div class="chat-actions">
      <el-button
        @click="handleExport"
        :disabled="messages.length === 0"
        :icon="Download"
        size="small"
      >
        导出对话
      </el-button>
      <el-button
        @click="handleClear"
        :disabled="messages.length === 0"
        type="danger"
        :icon="Delete"
        size="small"
      >
        清空历史
      </el-button>
    </div>
  </el-card>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { User, ChatDotRound, Bell, Download, Delete } from '@element-plus/icons-vue'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['clear', 'export'])

const chatContainer = ref(null)

// 监听消息变化，自动滚动到底部
watch(() => props.messages.length, () => {
  nextTick(() => {
    scrollToBottom()
  })
}, { immediate: true })

// 滚动到底部
const scrollToBottom = () => {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

// 获取作文类型标签
const getEssayTypeLabel = (type) => {
  const labels = {
    'EN1_PICTURE': '英语一图画',
    'EN2_CHART': '英语二图表',
    'LETTER': '应用文'
  }
  return labels[type] || type
}

// 获取系统消息类型
const getSystemMessageType = (content) => {
  if (content.includes('失败') || content.includes('错误')) {
    return 'error'
  } else if (content.includes('成功') || content.includes('完成')) {
    return 'success'
  } else {
    return 'info'
  }
}

// 清空历史
const handleClear = () => {
  ElMessageBox.confirm('确定要清空对话历史吗？此操作不可恢复。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    emit('clear')
    ElMessage.success('对话历史已清空')
  }).catch(() => {})
}

// 导出对话
const handleExport = () => {
  const exportData = props.messages.map(msg => ({
    role: msg.role,
    time: msg.time,
    type: msg.type,
    content: msg.content,
    essayType: msg.essayType,
    result: msg.result,
    meta: msg.meta
  }))

  const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `chat-history-${Date.now()}.json`
  link.click()
  URL.revokeObjectURL(url)

  ElMessage.success('对话历史已导出')
  emit('export', exportData)
}
</script>

<style scoped>
.chat-history-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  max-height: 600px;
  display: flex;
  flex-direction: column;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  max-height: 500px;
}

.message {
  margin-bottom: 20px;
  padding: 15px;
  border-radius: 8px;
  transition: all 0.3s;
}

.message.user {
  background: linear-gradient(135deg, #e8f3ff 0%, #f0f9ff 100%);
  margin-left: 30px;
  border-left: 3px solid #409eff;
}

.message.assistant {
  background: linear-gradient(135deg, #f0f9ff 0%, #f5f7fa 100%);
  margin-right: 30px;
  border-left: 3px solid #67c23a;
}

.message.system {
  background-color: #fef0f0;
  border-radius: 6px;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 12px;
  color: #909399;
}

.message-role {
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 4px;
}

.message.user .message-role {
  color: #409eff;
}

.message.assistant .message-role {
  color: #67c23a;
}

.message.system .message-role {
  color: #f56c6c;
}

.user-essay .essay-type-tag {
  margin-bottom: 8px;
}

.essay-text {
  padding: 12px;
  background-color: rgba(255, 255, 255, 0.6);
  border-radius: 4px;
  line-height: 1.6;
  white-space: pre-wrap;
  color: #303133;
}

.assistant-response .result-summary {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
  padding: 10px;
  background: linear-gradient(135deg, #67c23a 0%, #5daf34 100%);
  border-radius: 6px;
  color: white;
}

.score-badge {
  display: flex;
  align-items: baseline;
  gap: 5px;
}

.score-label {
  font-size: 14px;
  opacity: 0.9;
}

.score-value {
  font-size: 28px;
  font-weight: bold;
}

.meta-info {
  display: flex;
  gap: 5px;
}

.meta-info .el-tag {
  background-color: rgba(255, 255, 255, 0.2);
  border-color: transparent;
  color: white;
}

.response-text {
  padding: 12px;
  background-color: rgba(255, 255, 255, 0.6);
  border-radius: 4px;
  line-height: 1.6;
  color: #303133;
}

.chat-actions {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #e4e7ed;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 自定义滚动条样式 */
.chat-container::-webkit-scrollbar {
  width: 6px;
}

.chat-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.chat-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chat-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>