<template>
  <el-card class="chat-history-card">
    <template #header>
      <div class="card-header">
        <span>对话历史</span>
        <el-button size="small" @click="handleClear" type="danger">
          清空历史
        </el-button>
      </div>
    </template>

    <div class="chat-container" ref="chatContainer">
      <div
        v-for="(message, index) in messages"
        :key="index"
        :class="['message', message.type]"
      >
        <div class="message-header">
          <span class="message-role">{{ message.role }}</span>
          <span class="message-time">{{ message.time }}</span>
        </div>
        <div class="message-content">
          <div v-if="message.type === 'user'" class="user-essay">
            <h5>作文内容：</h5>
            <p>{{ message.content }}</p>
          </div>
          <div v-else-if="message.type === 'assistant'" class="assistant-response">
            <div v-if="message.result">
              <ResultCard :result="message.result" />
            </div>
            <div v-else>
              <p>{{ message.content }}</p>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-if="messages.length === 0" description="暂无对话记录" />
    </div>

    <div class="chat-actions">
      <el-button @click="handleExport" :disabled="messages.length === 0">
        导出对话
      </el-button>
    </div>
  </el-card>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import ResultCard from './ResultCard.vue'

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
})

const scrollToBottom = () => {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

const handleClear = () => {
  ElMessageBox.confirm('确定要清空对话历史吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    emit('clear')
    ElMessage.success('对话历史已清空')
  }).catch(() => {})
}

const handleExport = () => {
  const exportData = props.messages.map(msg => ({
    role: msg.role,
    time: msg.time,
    type: msg.type,
    content: msg.content,
    result: msg.result
  }))

  const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' })
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
  border-radius: 8px;
  max-height: 800px;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  max-height: 600px;
}

.message {
  margin-bottom: 20px;
  padding: 15px;
  border-radius: 8px;
}

.message.user {
  background-color: #e8f3ff;
  margin-left: 40px;
}

.message.assistant {
  background-color: #f5f7fa;
  margin-right: 40px;
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
}

.message-role::before {
  content: '● ';
  margin-right: 5px;
}

.message.user .message-role {
  color: #409eff;
}

.message.assistant .message-role {
  color: #67c23a;
}

.user-essay h5 {
  margin: 8px 0;
  color: #303133;
}

.user-essay p {
  margin: 8px 0;
  line-height: 1.6;
  white-space: pre-wrap;
}

.assistant-response {
  line-height: 1.6;
}

.chat-actions {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>