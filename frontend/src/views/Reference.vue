<template>
  <div class="reference-page">
    <el-card>
      <h2>范文生成</h2>
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
        <el-form-item label="题目">
          <el-input v-model="formData.topic" type="textarea" :rows="4" placeholder="输入作文题目" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleGenerate" :loading="loading">
            生成范文
          </el-button>
        </el-form-item>
      </el-form>

      <div v-if="reference" class="reference-section">
        <h3>参考范文</h3>
        <el-card>
          <div class="reference-content">{{ reference.referenceEssay }}</div>
          <div class="reference-info">
            <p><strong>词数：</strong>{{ reference.wordCount }}</p>
            <p><strong>亮点表达：</strong></p>
            <el-tag v-for="(highlight, index) in reference.highlights" :key="index" style="margin-right: 8px; margin-bottom: 8px;">
              {{ highlight }}
            </el-tag>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getReference } from '@/api/essay'

const loading = ref(false)
const formData = ref({
  essayType: 'EN1_PICTURE',
  topic: ''
})

const reference = ref(null)

const handleGenerate = async () => {
  if (!formData.value.topic) {
    ElMessage.warning('请输入作文题目')
    return
  }

  loading.value = true
  try {
    reference.value = await getReference(formData.value)
    ElMessage.success('范文生成完成')
  } catch (error) {
    console.error('生成范文失败', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.reference-page {
  max-width: 1200px;
  margin: 0 auto;
}

h2 {
  margin-bottom: 20px;
  color: #303133;
}

.reference-section {
  margin-top: 20px;
}

.reference-section h3 {
  margin-bottom: 15px;
  color: #303133;
}

.reference-content {
  background-color: #f5f7fa;
  padding: 20px;
  line-height: 1.8;
  border-radius: 4px;
  margin-bottom: 20px;
  white-space: pre-wrap;
}

.reference-info p {
  margin: 10px 0;
}
</style>