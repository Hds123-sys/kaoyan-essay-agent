<template>
  <el-container class="layout-container">
    <el-aside :width="collapsed ? '64px' : '200px'" class="aside">
      <div class="logo">
        <span v-if="!collapsed">考研英语作文批改Agent</span>
        <el-icon v-else class="logo-icon"><Edit /></el-icon>
      </div>
      <el-menu
        :default-active="currentRoute"
        :collapse="collapsed"
        router
        background-color="#409eff"
        text-color="#fff"
        active-text-color="#fff"
      >
        <el-menu-item index="/correct">
          <el-icon><Edit /></el-icon>
          <template #title>作文批改</template>
        </el-menu-item>
        <el-menu-item index="/topic">
          <el-icon><Document /></el-icon>
          <template #title>随机出题</template>
        </el-menu-item>
        <el-menu-item index="/reference">
          <el-icon><Notebook /></el-icon>
          <template #title>范文生成</template>
        </el-menu-item>
        <el-menu-item index="/history">
          <el-icon><Clock /></el-icon>
          <template #title>历史记录</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <el-button
          type="primary"
          text
          @click="collapsed = !collapsed"
          class="collapse-btn"
        >
          <el-icon>
            <Expand v-if="collapsed" />
            <Fold v-else />
          </el-icon>
        </el-button>
        <span class="header-title">考研英语作文批改Agent</span>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Edit, Document, Notebook, Clock, Expand, Fold } from '@element-plus/icons-vue'

const route = useRoute()
const collapsed = ref(false)
const currentRoute = computed(() => route.path)
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.aside {
  background-color: #409eff;
  transition: width 0.3s ease;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-weight: bold;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  font-size: 14px;
  overflow: hidden;
  white-space: nowrap;
}

.logo-icon {
  font-size: 24px;
  line-height: 60px;
}

.el-menu {
  border-right: none;
}

.header {
  background-color: #fff;
  color: #303133;
  display: flex;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.collapse-btn {
  margin-right: 16px;
  padding: 0;
  font-size: 20px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}

.el-main {
  background-color: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}

@media (max-width: 768px) {
  .aside {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 1000;
  }

  .aside.collapsed-mobile {
    width: 0 !important;
  }

  .header-title {
    font-size: 16px;
  }

  .el-main {
    padding: 15px 10px;
  }
}
</style>