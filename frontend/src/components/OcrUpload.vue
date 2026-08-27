<template>
  <el-card class="ocr-upload-card">
    <template #header>
      <span>图片OCR识别</span>
    </template>

    <el-upload
      class="ocr-upload"
      drag
      :auto-upload="false"
      :on-change="handleFileChange"
      :limit="1"
      accept="image/*"
    >
      <el-icon class="el-icon--upload"><upload-filled /></el-icon>
      <div class="el-upload__text">
        拖拽文件到此处或<em>点击上传</em>
      </div>
      <template #tip>
        <div class="el-upload__tip">
          支持 JPG、PNG 等常见图片格式
        </div>
      </template>
    </el-upload>

    <el-button
      type="primary"
      @click="handleOcr"
      :loading="loading"
      :disabled="!file"
      style="margin-top: 20px; width: 100%;"
    >
      开始识别
    </el-button>

    <div v-if="ocrResult" class="ocr-result">
      <h4>识别结果</h4>
      <el-alert type="success" :closable="false" show-icon style="margin-bottom: 15px;">
        语言检测：{{ ocrResult.language }}
      </el-alert>

      <el-input
        v-model="editableText"
        type="textarea"
        :rows="10"
        placeholder="识别的文本内容"
        @input="handleTextChange"
      />

      <div class="result-actions">
        <el-button type="success" @click="handleConfirm">
          确认使用此文本
        </el-button>
        <el-button @click="handleReOcr">
          重新识别
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ocr as ocrApi } from '@/api/ocr'

const emit = defineEmits(['confirm'])

const loading = ref(false)
const file = ref(null)
const ocrResult = ref(null)
const editableText = ref('')

const handleFileChange = (uploadFile) => {
  file.value = uploadFile.raw
}

const handleOcr = async () => {
  if (!file.value) {
    ElMessage.warning('请先选择图片')
    return
  }

  loading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file.value)

    // 假设后端支持文件上传，如果需要URL上传可以修改
    // 这里先模拟上传后的URL
    const imageUrl = URL.createObjectURL(file.value)
    ocrResult.value = await ocrApi(imageUrl)
    editableText.value = ocrResult.value.text
    ElMessage.success('OCR识别完成')
  } catch (error) {
    console.error('OCR识别失败', error)
  } finally {
    loading.value = false
  }
}

const handleTextChange = () => {
  // 文本编辑时的处理
}

const handleConfirm = () => {
  emit('confirm', editableText.value)
}

const handleReOcr = () => {
  ocrResult.value = null
  editableText.value = ''
  handleOcr()
}
</script>

<style scoped>
.ocr-upload-card {
  border-radius: 8px;
}

.ocr-upload {
  margin-bottom: 20px;
}

.ocr-result {
  margin-top: 20px;
}

h4 {
  margin-bottom: 15px;
  color: #303133;
}

.result-actions {
  margin-top: 15px;
  display: flex;
  gap: 10px;
}
</style>