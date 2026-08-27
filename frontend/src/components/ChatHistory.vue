<template>
  <div class="chat-history-card">
    <div class="chat-header">
      <span>多轮对话历史</span>
      <el-button
        size="small"
        type="danger"
        @click="handleClearContext"
        :icon="Delete"
      >
        清空上下文
      </el-button>
    </div>

    <div class="chat-container" ref="chatContainer">
      <div
        v-for="(message, index) in messages"
        :key="index"
        :class="['message', message.type]"
      >
        <!-- 用户消息（靠右） -->
        <div v-if="message.type === 'user'" class="user-message">
          <div class="message-bubble user-bubble">
            <div class="message-meta">
              <el-tag size="small" type="primary" class="essay-type-tag">
                {{ getEssayTypeLabel(message.essayType) }}
              </el-tag>
              <span class="message-time">{{ message.time }}</span>
            </div>
            <div class="message-content">
              <div class="essay-summary">
                {{ getEssaySummary(message.content) }}
              </div>
              <div class="essay-full" v-if="expandedMessages.has(index)">
                {{ message.content }}
              </div>
              <el-button
                v-if="message.content.length > 50"
                size="small"
                text
                type="primary"
                @click="toggleExpand(index)"
              >
                {{ expandedMessages.has(index) ? '收起' : '展开全文' }}
              </el-button>
            </div>
          </div>
        </div>

        <!-- Agent消息（靠左） -->
        <div v-else-if="message.type === 'assistant'" class="assistant-message">
          <div class="message-bubble assistant-bubble">
            <div class="message-meta">
              <div class="score-info" v-if="message.result">
                <span class="score-badge">得分：{{ message.result.total_score }}</span>
                <el-tag v-if="message.meta?.template_version" size="small" type="info" style="margin-left: 8px;">
                  v{{ message.meta.template_version }}
                </el-tag>
              </div>
              <span class="message-time">{{ message.time }}</span>
            </div>
            <div class="message-content">
              {{ message.content }}
            </div>
            <div class="message-actions">
              <el-button
                size="small"
                type="primary"
                @click="handleViewResult(index)"
                :icon="View"
              >
                查看完整结果
              </el-button>
            </div>
          </div>
        </div>

        <!-- 系统消息（居中） -->
        <div v-else-if="message.type === 'system'" class="system-message">
          <el-alert :type="getSystemMessageType(message.content)" :closable="false">
            {{ message.content }}
          </el-alert>
        </div>
      </div>

      <el-empty v-if="messages.length === 0" description="暂无对话记录" :image-size="80" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue'
import { Delete, View } from '@element-plus/icons-vue'
import { clearContext } from '@/api/session'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['clear', 'view-result'])

const chatContainer = ref(null)
const expandedMessages = ref(new Set())

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

// 获取作文摘要（前50字）
const getEssaySummary = (content) => {
  if (!content) return ''
  if (content.length <= 50) return content
  return content.substring(0, 50) + '...'
}

// 切换展开/收起
const toggleExpand = (index) => {
  if (expandedMessages.value.has(index)) {
    expandedMessages.value.delete(index)
  } else {
    expandedMessages.value.add(index)
  }
  expandedMessages.value = new Set(expandedMessages.value)
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

// 查看完整结果
const handleViewResult = (index) => {
  emit('view-result', props.messages[index])
}

// 清空上下文
const handleClearContext = async () => {
  ElMessageBox.confirm(
    '确定要清空当前会话的上下文吗？这会清除对话历史和词汇表，但不影响已保存的历史记录。',
    '清空上下文',
    {
      confirmButtonText: '确定清空',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await clearContext()
      emit('clear')
      ElMessage.success('上下文已清空')
    } catch (error) {
      console.error('清空上下文失败', error)
      ElMessage.error('清空上下文失败: ' + (error.message || '未知错误'))
    }
  }).catch(() => {})
}
</script>

<style scoped>
.chat-history-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #e4e7ed;
  background-color: #f5f7fa;
  border-radius: 8px 8px 0 0;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  max-height: 500px;
  background: linear-gradient(135deg, #fafafa 0%, #f5f7fa 100%);
}

.message {
  margin-bottom: 15px;
}

/* 用户消息靠右 */
.user-message {
  display: flex;
  justify-content: flex-end;
}

.user-bubble {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
  max-width: 70%;
  border-radius: 18px 18px 4px 18px;
}

/* Agent消息靠左 */
.assistant-message {
  display: flex;
  justify-content: flex-start;
}

.assistant-bubble {
  background: linear-gradient(135deg, #f0f9ff 0%, #e1f3ff 100%);
  color: #303133;
  max-width: 70%;
  border-radius: 18px 18px 18px 4px;
  border: 1px solid #b3d8ff;
}

/* 系统消息居中 */
.system-message {
  display: flex;
  justify-content: center;
  margin: 20px 0;
}

.message-bubble {
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.message-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
}

.essay-type-tag {
  background-color: rgba(255, 255, 255, 0.2);
  border-color: transparent;
  color: white;
}

.score-info {
  display: flex;
  align-items: center;
}

.score-badge {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: bold;
}

.message-time {
  opacity: 0.7;
  font-size: 11px;
}

.message-content {
  line-height: 1.6;
  margin-bottom: 8px;
}

.essay-summary {
  font-size: 14px;
}

.essay-full {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  white-space: pre-wrap;
  line-height: 1.6;
}

.message-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.message-actions :deep(.el-button) {
  color: white;
  background-color: rgba(255, 255, 255, 0.2);
  border-color: transparent;
}

.message-actions :deep(.el-button:hover) {
  background-color: rgba(255, 255, 255, 0.3);
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