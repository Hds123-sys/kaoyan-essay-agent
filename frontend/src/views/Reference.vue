<template>
  <div class="reference-page">
    <el-card shadow="hover">
      <template #header>
        <span>范文生成</span>
      </template>

      <el-form :model="formData" label-width="100px" :rules="rules" ref="formRef">
        <el-form-item label="作文类型" prop="essayType">
          <el-select v-model="formData.essayType" placeholder="选择作文类型" style="width: 100%;">
            <el-option label="英语一图画作文" value="EN1_PICTURE" />
            <el-option label="英语二图表作文" value="EN2_CHART" />
            <el-option label="应用文/小作文" value="LETTER" />
          </el-select>
        </el-form-item>

        <el-form-item label="题目" prop="topic">
          <el-input
            v-model="formData.topic"
            type="textarea"
            :rows="6"
            placeholder="输入作文题目（不少于5个字符）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            @click="handleGenerate"
            :loading="loading"
            :disabled="!canGenerate"
            style="width: 100%;"
          >
            {{ loading ? '生成中...' : '生成范文' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="referenceResult" shadow="hover" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>参考范文</span>
          <el-tag type="success">{{ referenceResult.word_count }} 词</el-tag>
        </div>
      </template>

      <div class="reference-content">
        <el-input
          v-model="referenceResult.reference_essay"
          type="textarea"
          :rows="15"
          readonly
          class="essay-textarea"
        />
      </div>

      <div v-if="referenceResult.highlights && referenceResult.highlights.length > 0" class="highlights-section">
        <el-divider content-position="left">亮点表达</el-divider>
        <div class="highlights-tags">
          <el-tag
            v-for="(highlight, index) in referenceResult.highlights"
            :key="index"
            type="success"
            effect="dark"
            closable
            @close="handleRemoveHighlight(index)"
            style="margin-right: 8px; margin-bottom: 8px;"
          >
            {{ highlight }}
          </el-tag>
        </div>
      </div>

      <div class="result-actions">
        <el-button
          type="primary"
          @click="handleCopyEssay"
          :icon="CopyDocument"
        >
          复制全文
        </el-button>
        <el-button
          type="success"
          @click="handleCopyHighlights"
          :icon="DocumentCopy"
          :disabled="!referenceResult.highlights?.length"
        >
          复制亮点表达
        </el-button>
        <el-button
          @click="handleDownload"
          :icon="Download"
        >
          下载文本
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
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { getReference } from '@/api/essay'
import { CopyDocument, DocumentCopy, Download, Refresh } from '@element-plus/icons-vue'

const formRef = ref(null)
const loading = ref(false)
const formData = ref({
  essayType: 'EN1_PICTURE',
  topic: ''
})

const referenceResult = ref(null)

// 表单验证规则
const rules = {
  essayType: [
    { required: true, message: '请选择作文类型', trigger: 'change' }
  ],
  topic: [
    { required: true, message: '请输入题目', trigger: 'blur' },
    { min: 5, message: '题目不能少于5个字符', trigger: 'blur' }
  ]
}

// 是否可以生成
const canGenerate = computed(() => {
  return formData.value.essayType && formData.value.topic?.trim().length >= 5
})

// 生成范文
const handleGenerate = async () => {
  if (!canGenerate.value) {
    if (!formData.value.essayType) {
      ElMessage.warning('请选择作文类型')
    } else if (formData.value.topic?.trim().length < 5) {
      ElMessage.warning('题目不能少于5个字符')
    }
    return
  }

  loading.value = true
  try {
    const result = await getReference(formData.value.topic.trim(), formData.value.essayType)
    referenceResult.value = result
    ElMessage.success('范文生成成功')
  } catch (error) {
    console.error('生成范文失败', error)
    ElMessage.error('生成范文失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 复制全文
const handleCopyEssay = () => {
  if (!referenceResult.value?.reference_essay) return

  navigator.clipboard.writeText(referenceResult.value.reference_essay).then(() => {
    ElMessage.success('范文已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

// 复制亮点表达
const handleCopyHighlights = () => {
  if (!referenceResult.value?.highlights?.length) return

  const highlightsText = referenceResult.value.highlights.join('\n')
  navigator.clipboard.writeText(highlightsText).then(() => {
    ElMessage.success('亮点表达已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

// 下载文本
const handleDownload = () => {
  if (!referenceResult.value?.reference_essay) return

  const content = `题目：${formData.value.topic}\n\n范文：\n${referenceResult.value.reference_essay}\n\n词数：${referenceResult.value.word_count}\n\n亮点表达：\n${referenceResult.value.highlights?.join('、') || '无'}`

  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `reference-essay-${Date.now()}.txt`
  link.click()
  URL.revokeObjectURL(url)

  ElMessage.success('文件下载成功')
}

// 重新生成
const handleRegenerate = () => {
  referenceResult.value = null
  handleGenerate()
}

// 移除某个亮点表达
const handleRemoveHighlight = (index) => {
  if (referenceResult.value?.highlights) {
    referenceResult.value.highlights.splice(index, 1)
  }
}
</script>

<style scoped>
.reference-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.reference-content {
  margin-bottom: 20px;
}

.essay-textarea :deep(textarea) {
  font-family: 'Georgia', 'Times New Roman', serif;
  line-height: 1.8;
  font-size: 16px;
  background-color: #f9f9f9;
}

.highlights-section {
  margin: 20px 0;
}

.highlights-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.result-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
}
</style>