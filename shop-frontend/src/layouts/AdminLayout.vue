<template>
  <div class="admin-layout">
    <el-container>
      <el-aside width="220px" class="admin-sidebar">
        <div class="admin-logo" @click="$router.push('/admin')">
          <span>后台管理</span>
        </div>
        <el-menu router :default-active="route.path" background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF">
          <el-menu-item index="/admin">
            <el-icon><DataAnalysis /></el-icon> 仪表盘
          </el-menu-item>
          <el-menu-item index="/admin/products">
            <el-icon><Goods /></el-icon> 产品管理
          </el-menu-item>
          <el-menu-item index="/admin/orders">
            <el-icon><Document /></el-icon> 订单管理
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon> 用户管理
          </el-menu-item>
          <el-menu-item index="/admin/categories">
            <el-icon><Grid /></el-icon> 分类管理
          </el-menu-item>
          <el-sub-menu index="settings">
            <template #title><el-icon><Setting /></el-icon> 站点设置</template>
            <el-menu-item index="/admin/settings/site">联系方式</el-menu-item>
            <el-menu-item index="/admin/settings/pages">页面布局</el-menu-item>
            <el-menu-item index="/admin/settings/marketing">营销设置</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="admin-header">
          <span>欢迎回来，{{ auth.username }}</span>
          <el-button @click="handleLogout" size="small">退出</el-button>
          <el-button size="small" @click="$router.push('/')">返回前台</el-button>
        </el-header>
        <el-main><router-view /></el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout { height: 100vh; }
.admin-sidebar { background: #304156; overflow-y: auto; }
.admin-logo { height: 64px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 18px; cursor: pointer; }
.admin-header { background: #fff; border-bottom: 1px solid #e6e6e6; display: flex; align-items: center; justify-content: flex-end; gap: 12px; }
</style>
