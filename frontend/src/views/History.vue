<template>
  <div class="history-page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>历史记录</span>
          <el-button size="small" @click="handleRefresh" :icon="Refresh">
            刷新
          </el-button>
        </div>
      </template>

      <el-table
        :data="historyList"
        style="width: 100%"
        v-loading="loading"
        stripe
        border
        :header-cell-style="{ background: '#f5f7fa', color: '#303133' }"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="作文类型" width="140" align="center">
          <template #default="scope">
            <el-tag :type="getEssayTypeTag(scope.row.essay_type)" size="small">
              {{ getEssayTypeLabel(scope.row.essay_type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="题目摘要" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            {{ getTopicSummary(scope.row.topic) }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.created_at) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleViewDetail(scope.row)">
              查看详情
            </el-button>
            <el-button size="small" type="warning" @click="handleReCorrect(scope.row)">
              重新批改
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          :default-page-size="20"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="历史记录详情"
      width="80%"
      top="5vh"
    >
      <div v-if="currentDetail" v-loading="detailLoading">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ currentDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="作文类型">
            <el-tag :type="getEssayTypeTag(currentDetail.essay_type)" size="small">
              {{ getEssayTypeLabel(currentDetail.essay_type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ formatDate(currentDetail.created_at) }}
          </el-descriptions-item>
          <el-descriptions-item label="题目" :span="2">
            <div class="detail-text">{{ currentDetail.topic || '无' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="学生原文" :span="2">
            <div class="detail-text">{{ currentDetail.user_essay }}</div>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">批改结果</el-divider>
        <div v-if="currentResult" class="result-section">
          <ResultCard :result="currentResult" />
        </div>
        <div v-else-if="currentDetail.result_json" class="result-json">
          <el-alert type="info" :closable="false" show-icon>
            结果无法解析为JSON，以下是原始数据
          </el-alert>
          <pre class="json-content">{{ currentDetail.result_json }}</pre>
        </div>
        <el-empty v-else description="暂无批改结果" />
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="warning" @click="handleReCorrectFromDetail">重新批改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHistoryList, getHistoryDetail } from '@/api/history'
import ResultCard from '@/components/ResultCard.vue'
import { Refresh } from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(false)
const historyList = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const detailVisible = ref(false)
const detailLoading = ref(false)
const currentDetail = ref(null)
const currentResult = ref(null)

// 获取历史记录列表
const fetchHistoryList = async () => {
  loading.value = true
  try {
    const result = await getHistoryList(currentPage.value, pageSize.value)
    historyList.value = result.records || []
    total.value = result.total || 0
  } catch (error) {
    console.error('获取历史记录失败', error)
    ElMessage.error('获取历史记录失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 刷新
const handleRefresh = () => {
  fetchHistoryList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchHistoryList()
}

// 当前页变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchHistoryList()
}

// 查看详情
const handleViewDetail = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  currentDetail.value = null
  currentResult.value = null

  try {
    const detail = await getHistoryDetail(row.id)
    currentDetail.value = detail

    // 尝试解析批改结果JSON
    if (detail.result_json) {
      try {
        currentResult.value = JSON.parse(detail.result_json)
      } catch (e) {
        console.error('解析批改结果JSON失败', e)
        currentResult.value = null
      }
    }
  } catch (error) {
    console.error('获取详情失败', error)
    ElMessage.error('获取详情失败: ' + (error.message || '未知错误'))
  } finally {
    detailLoading.value = false
  }
}

// 重新批改（从列表）
const handleReCorrect = (row) => {
  router.push({
    path: '/correct',
    query: {
      recordId: row.id,
      topic: row.topic,
      essayType: row.essay_type,
      userEssay: row.user_essay
    }
  })
}

// 重新批改（从详情弹窗）
const handleReCorrectFromDetail = () => {
  if (!currentDetail.value) return
  detailVisible.value = false
  handleReCorrect(currentDetail.value)
}

// 获取作文类型标签
const getEssayTypeTag = (type) => {
  const tags = {
    'EN1_PICTURE': 'primary',
    'EN2_CHART': 'success',
    'LETTER': 'warning'
  }
  return tags[type] || 'info'
}

// 获取作文类型标签文本
const getEssayTypeLabel = (type) => {
  const labels = {
    'EN1_PICTURE': '英语一图画',
    'EN2_CHART': '英语二图表',
    'LETTER': '应用文'
  }
  return labels[type] || type
}

// 获取题目摘要
const getTopicSummary = (topic) => {
  if (!topic) return '无'
  if (topic.length <= 30) return topic
  return topic.substring(0, 30) + '...'
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '无'
  try {
    const date = new Date(dateStr)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (e) {
    return dateStr
  }
}

onMounted(() => {
  fetchHistoryList()
})
</script>

<style scoped>
.history-page {
  padding: 20px;
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.detail-text {
  line-height: 1.8;
  white-space: pre-wrap;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
}

.result-section {
  margin-top: 10px;
}

.result-json {
  margin-top: 10px;
}

.json-content {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 12px;
  line-height: 1.6;
  max-height: 400px;
  overflow-y: auto;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}

@media (max-width: 768px) {
  .history-page {
    padding: 10px;
  }

  .pagination-wrapper {
    justify-content: center;
  }

  .history-table {
    font-size: 12px;
  }

  .hide-mobile {
    display: none;
  }

  .detail-text,
  .json-content {
    font-size: 11px;
    padding: 8px;
  }
}
</style>