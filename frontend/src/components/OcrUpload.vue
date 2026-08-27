<template>
  <div class="ocr-upload-card">
    <el-upload
      ref="uploadRef"
      class="ocr-upload"
      drag
      :auto-upload="false"
      :on-change="handleFileChange"
      :before-upload="beforeUpload"
      :limit="1"
      accept="image/jpeg,image/jpg,image/png"
      :on-exceed="handleExceed"
      :on-remove="handleRemove"
    >
      <el-icon class="el-icon--upload"><upload-filled /></el-icon>
      <div class="el-upload__text">
        拖拽文件到此处或<em>点击上传</em>
      </div>
      <template #tip>
        <div class="el-upload__tip">
          支持 JPG、PNG、JPEG 格式，单张图片不超过 10MB
        </div>
      </template>
    </el-upload>

    <div v-if="!ocrResult && !loading" class="upload-actions">
      <el-button
        type="primary"
        @click="handleOcr"
        :disabled="!selectedFile"
        style="width: 100%;"
      >
        开始识别
      </el-button>
    </div>

    <div v-if="loading" class="loading-section">
      <el-skeleton :rows="8" animated />
      <div class="loading-text">正在识别图片，请稍候...</div>
    </div>

    <div v-if="ocrResult" class="ocr-result">
      <el-divider content-position="left">识别结果</el-divider>

      <!-- 统计信息 -->
      <el-row :gutter="20" class="stats-section">
        <el-col :span="8">
          <el-statistic title="识别字数" :value="ocrResult.ocr_text?.length || 0" />
        </el-col>
        <el-col :span="8">
          <el-statistic title="平均置信度" :value="ocrResult.average_confidence" :precision="2" suffix="%" />
        </el-col>
        <el-col :span="8">
          <el-statistic title="低置信度单词" :value="lowConfidenceWords.length" />
        </el-col>
      </el-row>

      <!-- 警告提示 -->
      <el-alert
        v-if="ocrResult.warning"
        type="warning"
        :closable="false"
        show-icon
        style="margin: 15px 0;"
      >
        {{ ocrResult.warning }}
      </el-alert>

      <!-- 图片预览 -->
      <div v-if="ocrResult.image_url" class="image-preview">
        <el-image
          :src="ocrResult.image_url"
          :preview-src-list="[ocrResult.image_url]"
          fit="contain"
          style="max-height: 200px;"
        />
      </div>

      <!-- 可编辑文本框 -->
      <div class="text-editor">
        <div class="editor-header">
          <span>识别文本（可编辑）</span>
          <el-tag v-if="isHeavilyEdited" type="danger" size="small">
            大幅修改 (>30%)
          </el-tag>
        </div>
        <el-input
          v-model="editableText"
          type="textarea"
          :rows="10"
          placeholder="识别的文本内容"
          @input="handleTextChange"
        />
      </div>

      <!-- 低置信度单词标签云 -->
      <div v-if="lowConfidenceWords.length > 0" class="low-confidence-section">
        <el-alert
          type="error"
          :closable="false"
          show-icon
          style="margin-bottom: 10px;"
        >
          以下单词识别置信度较低，请重点核对
        </el-alert>
        <div class="word-cloud">
          <el-tag
            v-for="word in lowConfidenceWords"
            :key="word.word"
            type="danger"
            effect="plain"
            style="margin-right: 5px; margin-bottom: 5px;"
          >
            {{ word.word }}
          </el-tag>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="result-actions">
        <el-button type="success" @click="handleConfirm" :icon="Check">
          确认使用此文本
        </el-button>
        <el-button @click="handleReOcr" :icon="Refresh">
          重新识别
        </el-button>
        <el-button @click="handleClear" :icon="Delete" type="danger">
          清除
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { UploadFilled, Check, Refresh, Delete } from '@element-plus/icons-vue'
import { upload } from '@/api/ocr'

const emit = defineEmits(['confirm', 'heavily-edited'])

const uploadRef = ref(null)
const loading = ref(false)
const selectedFile = ref(null)
const ocrResult = ref(null)
const editableText = ref('')
const originalOcrText = ref('')
const isHeavilyEdited = ref(false)

// 低置信度单词列表（<0.6）
const lowConfidenceWords = computed(() => {
  if (!ocrResult.value?.words) return []
  return ocrResult.value.words.filter(word => word.confidence < 0.6)
})

// 上传前校验
const beforeUpload = (file) => {
  const isValidType = ['image/jpeg', 'image/jpg', 'image/png'].includes(file.type)
  const isValidSize = file.size / 1024 / 1024 <= 10

  if (!isValidType) {
    ElMessage.error('只能上传 JPG、PNG、JPEG 格式的图片！')
    return false
  }
  if (!isValidSize) {
    ElMessage.error('图片大小不能超过 10MB！')
    return false
  }
  return true
}

// 文件选择变化
const handleFileChange = (uploadFile) => {
  selectedFile.value = uploadFile.raw
  ocrResult.value = null
  editableText.value = ''
}

// 超出文件数量限制
const handleExceed = (files) => {
  uploadRef.value?.clearFiles()
  const file = files[0]
  uploadRef.value?.handleStart(file)
}

// 移除文件
const handleRemove = () => {
  selectedFile.value = null
  ocrResult.value = null
  editableText.value = ''
}

// 开始OCR识别
const handleOcr = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择图片')
    return
  }

  loading.value = true
  try {
    const result = await upload(selectedFile.value)
    ocrResult.value = result
    editableText.value = result.ocr_text || ''
    originalOcrText.value = result.ocr_text || ''
    isHeavilyEdited.value = false

    ElMessage.success('OCR识别完成')
  } catch (error) {
    console.error('OCR识别失败', error)
  } finally {
    loading.value = false
  }
}

// 文本编辑变化
const handleTextChange = () => {
  if (!originalOcrText.value) {
    return
  }

  // 计算编辑比例（简单实现：对比长度差异）
  const originalLength = originalOcrText.value.length
  const currentLength = editableText.value.length

  if (originalLength === 0) {
    isHeavilyEdited.value = false
    return
  }

  const lengthDiff = Math.abs(currentLength - originalLength)
  const editRatio = lengthDiff / originalLength

  // 超过30%变化视为重度编辑
  isHeavilyEdited.value = editRatio > 0.3

  // emit重度编辑事件
  if (isHeavilyEdited.value) {
    emit('heavily-edited', true)
  }
}

// 确认使用文本
const handleConfirm = () => {
  if (!editableText.value?.trim()) {
    ElMessage.warning('请确认识别的文本内容')
    return
  }

  emit('confirm', editableText.value, ocrResult.value?.image_url)

  // 发送重度编辑标记
  if (isHeavilyEdited.value) {
    emit('heavily-edited', true)
  }

  ElMessage.success('已确认使用识别文本')
}

// 重新识别
const handleReOcr = () => {
  handleOcr()
}

// 清除所有内容
const handleClear = () => {
  uploadRef.value?.clearFiles()
  selectedFile.value = null
  ocrResult.value = null
  editableText.value = ''
  originalOcrText.value = ''
  isHeavilyEdited.value = false
}

// 监听editableText变化，实时计算编辑比例
watch(editableText, () => {
  handleTextChange()
})
</script>

<style scoped>
.ocr-upload-card {
  border-radius: 8px;
}

.ocr-upload {
  margin-bottom: 15px;
}

.ocr-upload :deep(.el-upload-dragger) {
  padding: 20px;
}

.upload-actions {
  margin-top: 15px;
}

.loading-section {
  padding: 20px;
}

.loading-text {
  text-align: center;
  color: #909399;
  margin-top: 15px;
  font-size: 14px;
}

.ocr-result {
  margin-top: 20px;
}

.stats-section {
  margin: 15px 0;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f3ff 100%);
  padding: 15px;
  border-radius: 8px;
}

.stats-section :deep(.el-statistic__number) {
  color: #409eff;
}

.image-preview {
  margin: 15px 0;
  text-align: center;
}

.text-editor {
  margin: 15px 0;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-weight: bold;
  color: #303133;
}

.low-confidence-section {
  margin: 15px 0;
  padding: 15px;
  background-color: #fef0f0;
  border-radius: 8px;
}

.word-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-top: 10px;
}

.result-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  justify-content: center;
  flex-wrap: wrap;
}
</style>