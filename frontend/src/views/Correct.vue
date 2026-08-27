<template>
  <div class="correct-page">
    <el-card>
      <h2>作文批改</h2>
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
          <el-input v-model="formData.topic" type="textarea" :rows="2" placeholder="输入作文题目" />
        </el-form-item>
        <el-form-item label="学生作文">
          <el-input v-model="formData.userEssay" type="textarea" :rows="8" placeholder="输入学生作文内容" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleCorrect" :loading="loading">
            开始批改
          </el-button>
          <el-button @click="handleReCorrect" :disabled="!lastResult">
            重新批改
          </el-button>
        </el-form-item>
      </el-form>

      <div v-if="result" class="result-section">
        <ResultCard :result="result" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { correct } from '@/api/essay'
import ResultCard from '@/components/ResultCard.vue'

const loading = ref(false)
const formData = ref({
  essayType: 'EN1_PICTURE',
  topic: '',
  userEssay: '',
  imageUrl: ''
})

const result = ref(null)
const lastResult = ref(null)

const handleCorrect = async () => {
  if (!formData.value.userEssay) {
    ElMessage.warning('请输入学生作文内容')
    return
  }

  loading.value = true
  try {
    result.value = await correct(formData.value)
    lastResult.value = result.value
    ElMessage.success('批改完成')
  } catch (error) {
    console.error('批改失败', error)
  } finally {
    loading.value = false
  }
}

const handleReCorrect = async () => {
  if (!lastResult.value) return

  loading.value = true
  try {
    result.value = await correct({
      ...formData.value,
      recordId: lastResult.value.id
    })
    lastResult.value = result.value
    ElMessage.success('重新批改完成')
  } catch (error) {
    console.error('重新批改失败', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.correct-page {
  max-width: 1200px;
  margin: 0 auto;
}

h2 {
  margin-bottom: 20px;
  color: #303133;
}

.result-section {
  margin-top: 20px;
}
</style>