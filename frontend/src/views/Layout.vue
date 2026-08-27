<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <div class="logo">考研英语作文批改Agent</div>
      <el-menu
        :default-active="currentRoute"
        router
        background-color="#545c64"
        text-color="#fff"
        active-text-color="#ffd04b"
      >
        <el-menu-item index="/correct">
          <el-icon><Edit /></el-icon>
          <span>作文批改</span>
        </el-menu-item>
        <el-menu-item index="/topic">
          <el-icon><Document /></el-icon>
          <span>随机出题</span>
        </el-menu-item>
        <el-menu-item index="/reference">
          <el-icon><Notebook /></el-icon>
          <span>范文生成</span>
        </el-menu-item>
        <el-menu-item index="/history">
          <el-icon><Clock /></el-icon>
          <span>历史记录</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>考研英语作文批改Agent</el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Edit, Document, Notebook, Clock } from '@element-plus/icons-vue'

const route = useRoute()
const currentRoute = computed(() => route.path)
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.el-aside {
  background-color: #545c64;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-weight: bold;
  border-bottom: 1px solid #434a50;
}

.el-menu {
  border-right: none;
}

.el-header {
  background-color: #b3c0d1;
  color: #333;
  line-height: 60px;
  text-align: center;
  font-size: 20px;
  font-weight: bold;
}

.el-main {
  background-color: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}
</style>