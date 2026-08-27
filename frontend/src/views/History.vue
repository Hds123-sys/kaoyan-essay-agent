<template>
  <div class="history-page">
    <el-card>
      <h2>历史记录</h2>
      <el-table :data="historyList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="essayType" label="作文类型" width="150" />
        <el-table-column prop="topic" label="题目" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="handleViewDetail(scope.row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="批改详情" width="80%">
      <div v-if="currentDetail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="作文类型">{{ currentDetail.essayType }}</el-descriptions-item>
          <el-descriptions-item label="题目">{{ currentDetail.topic }}</el-descriptions-item>
          <el-descriptions-item label="学生原文">{{ currentDetail.userEssay }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentDetail.createdAt }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="currentDetail.resultJson" class="result-detail">
          <h3>批改结果</h3>
          <ResultCard :result="JSON.parse(currentDetail.resultJson)" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getHistoryList, getHistoryDetail } from '@/api/history'
import ResultCard from '@/components/ResultCard.vue'

const loading = ref(false)
const historyList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const detailVisible = ref(false)
const currentDetail = ref(null)

const fetchHistoryList = async () => {
  loading.value = true
  try {
    const result = await getHistoryList(currentPage.value, pageSize.value)
    historyList.value = result.list || []
    total.value = result.total || 0
  } catch (error) {
    console.error('获取历史记录失败', error)
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (size) => {
  pageSize.value = size
  fetchHistoryList()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchHistoryList()
}

const handleViewDetail = async (row) => {
  try {
    currentDetail.value = await getHistoryDetail(row.id)
    detailVisible.value = true
  } catch (error) {
    console.error('获取详情失败', error)
  }
}

onMounted(() => {
  fetchHistoryList()
})
</script>

<style scoped>
.history-page {
  max-width: 1400px;
  margin: 0 auto;
}

h2 {
  margin-bottom: 20px;
  color: #303133;
}

.result-detail {
  margin-top: 20px;
}

.result-detail h3 {
  margin-bottom: 15px;
  color: #303133;
}
</style>