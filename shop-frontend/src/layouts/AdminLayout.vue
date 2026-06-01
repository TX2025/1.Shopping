<template>
  <div class="admin-layout">
    <!-- ===== Sidebar ===== -->
    <aside class="admin-sidebar" :class="{ collapsed: sidebarCollapsed }" id="adminSidebar">
      <div class="sidebar-header">
        <div class="sidebar-logo" @click="$router.push('/admin')">S</div>
        <span class="sidebar-title">ShopAdmin</span>
      </div>
      <div class="sidebar-toggle" @click="sidebarCollapsed = !sidebarCollapsed" title="折叠/展开">
        <el-icon :size="12"><ArrowLeft v-if="!sidebarCollapsed" /><ArrowRight v-else /></el-icon>
      </div>
      <nav class="sidebar-nav">
        <!-- 概览 -->
        <div class="nav-section">概览</div>
        <div class="nav-item" :class="{ active: isActive('/admin') }" data-tooltip="仪表盘" @click="navigate('/admin')">
          <el-icon :size="15"><DataAnalysis /></el-icon><span>仪表盘</span>
        </div>

        <!-- 商品 -->
        <div class="nav-section">商品</div>
        <div class="nav-item" :class="{ active: isActive('/admin/categories') }" data-tooltip="分类管理" @click="navigate('/admin/categories')">
          <el-icon :size="15"><FolderOpened /></el-icon><span>分类管理</span>
        </div>
        <div class="nav-item" :class="{ active: isActive('/admin/products') }" data-tooltip="产品管理" @click="navigate('/admin/products')">
          <el-icon :size="15"><Goods /></el-icon><span>产品管理</span>
        </div>

        <!-- 交易 -->
        <div class="nav-section">交易</div>
        <div class="nav-item" :class="{ active: isActive('/admin/orders') }" data-tooltip="订单管理" @click="navigate('/admin/orders')">
          <el-icon :size="15"><Document /></el-icon><span>订单管理</span>
        </div>
        <div class="nav-item" :class="{ active: isActive('/admin/users') }" data-tooltip="用户管理" @click="navigate('/admin/users')">
          <el-icon :size="15"><User /></el-icon><span>用户管理</span>
        </div>

        <!-- 运营 -->
        <div class="nav-section">运营</div>
        <div class="nav-item" :class="{ active: isActive('/admin/settings/pages') }" data-tooltip="页面布局" @click="navigate('/admin/settings/pages')">
          <el-icon :size="15"><Grid /></el-icon><span>页面布局</span>
        </div>
        <div class="nav-item" :class="{ active: isActive('/admin/settings/site') }" data-tooltip="联系方式" @click="navigate('/admin/settings/site')">
          <el-icon :size="15"><Phone /></el-icon><span>联系方式</span>
        </div>
        <div class="nav-item" :class="{ active: isActive('/admin/settings/marketing') }" data-tooltip="营销设置" @click="navigate('/admin/settings/marketing')">
          <el-icon :size="15"><Present /></el-icon><span>营销设置</span>
        </div>

        <!-- 服务 -->
        <div class="nav-section">服务</div>
        <div class="nav-item" :class="{ active: isActive('/admin/service/shipping') }" data-tooltip="配送服务" @click="navigate('/admin/service/shipping')">
          <el-icon :size="15"><Van /></el-icon><span>配送服务</span>
        </div>
        <div class="nav-item" :class="{ active: isActive('/admin/service/aftersales') }" data-tooltip="售后服务" @click="navigate('/admin/service/aftersales')">
          <el-icon :size="15"><CircleCheck /></el-icon><span>售后服务</span>
        </div>
        <div class="nav-item" :class="{ active: isActive('/admin/service/inquiries') }" data-tooltip="询盘管理" @click="navigate('/admin/service/inquiries')">
          <el-icon :size="15"><ChatDotRound /></el-icon><span>询盘管理</span>
        </div>
      </nav>
    </aside>

    <!-- ===== Main Area ===== -->
    <div class="admin-main">
      <!-- Topbar -->
      <header class="admin-topbar">
        <div class="topbar-left">
          <div class="breadcrumb">
            <a href="#" @click.prevent="$router.push('/admin')">首页</a>
            <el-icon :size="10"><ArrowRight /></el-icon>
            <span class="current">{{ breadcrumbCurrent }}</span>
          </div>
        </div>
        <div class="topbar-right">
          <div class="topbar-search">
            <el-icon :size="13"><Search /></el-icon>
            <input type="text" placeholder="搜索功能、产品、订单..." />
          </div>
          <div class="topbar-icon" title="通知">
            <el-icon :size="16"><Bell /></el-icon>
            <span class="badge-dot"></span>
          </div>
          <div class="topbar-icon" title="设置">
            <el-icon :size="16"><Setting /></el-icon>
          </div>
          <div class="topbar-user">
            <div class="topbar-avatar">{{ (auth.username || 'A').charAt(0).toUpperCase() }}</div>
            <span class="topbar-username">{{ auth.username }}</span>
            <el-icon :size="10" color="#909399"><ArrowDown /></el-icon>
            <div class="topbar-user-menu">
              <div class="menu-item" @click="$router.push('/')">返回前台</div>
              <div class="menu-item danger" @click="handleLogout">退出登录</div>
            </div>
          </div>
        </div>
      </header>

      <!-- Content -->
      <div class="admin-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import {
  DataAnalysis, Goods, Document, User, Grid, Setting,
  FolderOpened, Phone, Present, Search, Bell, Van, CircleCheck, ChatDotRound,
  ArrowLeft, ArrowRight, ArrowDown
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const sidebarCollapsed = ref(false)

const breadcrumbMap = {
  '/admin': '仪表盘',
  '/admin/products': '产品管理',
  '/admin/orders': '订单管理',
  '/admin/users': '用户管理',
  '/admin/categories': '分类管理',
  '/admin/settings/site': '联系方式',
  '/admin/settings/pages': '页面布局',
  '/admin/settings/marketing': '营销设置',
  '/admin/service/shipping': '配送服务',
  '/admin/service/aftersales': '售后服务',
  '/admin/service/inquiries': '询盘管理',
}

const breadcrumbCurrent = computed(() => {
  // check exact match first, then prefix
  if (breadcrumbMap[route.path]) return breadcrumbMap[route.path]
  if (route.path.startsWith('/admin/products/')) return '编辑产品'
  return '仪表盘'
})

function isActive(path) {
  if (path === '/admin') return route.path === '/admin'
  return route.path.startsWith(path)
}

function navigate(path) {
  router.push(path)
}

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
/* ===== CSS Variables ===== */
.admin-layout {
  --sidebar-bg: #1e1e2d;
  --sidebar-hover: #2a2a3c;
  --sidebar-active: #6c5ce7;
  --primary: #6c5ce7;
  --primary-light: #a29bfe;
  --primary-bg: rgba(108,92,231,0.08);
  --bg: #f0f2f5;
  --surface: #fff;
  --text: #2d3436;
  --text-muted: #909399;
  --border: #e0e3e8;
  --danger: #e17055;
  --sidebar-w: 240px;
  --sidebar-collapsed-w: 64px;
  --topbar-h: 56px;
  --transition: all .2s ease;

  display: flex;
  height: 100vh;
  overflow: hidden;
  font-family: -apple-system,BlinkMacSystemFont,'Segoe UI','PingFang SC','Microsoft YaHei',sans-serif;
}

/* ===== Sidebar ===== */
.admin-sidebar {
  width: var(--sidebar-w);
  background: var(--sidebar-bg);
  color: #a0a3b1;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: var(--transition);
  overflow: hidden;
  position: relative;
  z-index: 100;
}
.admin-sidebar.collapsed {
  width: var(--sidebar-collapsed-w);
}

/* Logo */
.sidebar-header {
  padding: 20px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}
.sidebar-logo {
  width: 32px; height: 32px;
  background: var(--primary);
  border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-weight: 700; font-size: 16px;
  flex-shrink: 0; cursor: pointer;
}
.sidebar-title {
  font-size: 16px; font-weight: 600; color: #fff;
  white-space: nowrap; overflow: hidden;
}
.admin-sidebar.collapsed .sidebar-title { display: none; }

/* Collapse toggle */
.sidebar-toggle {
  position: absolute; top: 20px; right: -14px;
  width: 28px; height: 28px;
  background: var(--primary); border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: #fff; cursor: pointer; z-index: 101;
  border: 2px solid var(--bg); transition: var(--transition);
}
.sidebar-toggle:hover { transform: scale(1.1); }

/* Nav */
.sidebar-nav {
  flex: 1; overflow-y: auto; padding: 8px 0;
  scrollbar-width: thin;
  scrollbar-color: rgba(255,255,255,0.1) transparent;
}
.sidebar-nav::-webkit-scrollbar { width: 4px; }
.sidebar-nav::-webkit-scrollbar-thumb {
  background: rgba(255,255,255,0.15); border-radius: 4px;
}

.nav-section {
  padding: 8px 16px 4px;
  font-size: 11px; text-transform: uppercase; letter-spacing: 1px;
  color: rgba(255,255,255,0.25);
  white-space: nowrap; overflow: hidden;
}
.admin-sidebar.collapsed .nav-section {
  text-align: center; padding: 8px 0 4px; font-size: 9px;
}

.nav-item {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 16px; margin: 1px 8px;
  border-radius: 8px; cursor: pointer;
  transition: var(--transition); white-space: nowrap;
  position: relative;
}
.nav-item :deep(.el-icon) { width: 20px; text-align: center; flex-shrink: 0; }
.nav-item span { overflow: hidden; text-overflow: ellipsis; }
.nav-item:hover { background: var(--sidebar-hover); color: #dfe4ea; }
.nav-item.active {
  background: var(--sidebar-active); color: #fff;
  box-shadow: 0 2px 8px rgba(108,92,231,0.4);
}

/* Collapsed tooltips */
.admin-sidebar.collapsed .nav-item {
  padding: 10px 0; justify-content: center; margin: 1px 4px;
}
.admin-sidebar.collapsed .nav-item span { display: none; }
.admin-sidebar.collapsed .nav-item::after {
  content: attr(data-tooltip);
  position: absolute; left: 54px;
  background: #333; color: #fff;
  padding: 4px 10px; border-radius: 4px; font-size: 12px;
  white-space: nowrap; opacity: 0; pointer-events: none;
  transition: opacity .15s; z-index: 200;
}
.admin-sidebar.collapsed .nav-item:hover::after { opacity: 1; }

/* ===== Main ===== */
.admin-main {
  flex: 1; display: flex; flex-direction: column;
  overflow: hidden; background: var(--bg);
}

/* Topbar */
.admin-topbar {
  background: var(--surface); height: var(--topbar-h);
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 24px; border-bottom: 1px solid var(--border);
  flex-shrink: 0; box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.topbar-left { display: flex; align-items: center; gap: 16px; }

.breadcrumb {
  display: flex; align-items: center; gap: 6px;
  color: var(--text-muted); font-size: 13px;
}
.breadcrumb a { color: var(--text-muted); text-decoration: none; }
.breadcrumb a:hover { color: var(--primary); }
.breadcrumb .current { color: var(--text); font-weight: 500; }

.topbar-right { display: flex; align-items: center; gap: 16px; }

.topbar-search { position: relative; }
.topbar-search input {
  padding: 7px 12px 7px 34px; border: 1px solid var(--border);
  border-radius: 20px; font-size: 13px; width: 220px;
  background: var(--bg); outline: none; transition: var(--transition);
}
.topbar-search input:focus { border-color: var(--primary); width: 280px; }
.topbar-search :deep(.el-icon) {
  position: absolute; left: 12px; top: 50%; transform: translateY(-50%);
  color: var(--text-muted);
}

.topbar-icon {
  width: 36px; height: 36px; display: flex; align-items: center; justify-content: center;
  border-radius: 50%; cursor: pointer; transition: var(--transition);
  color: var(--text-muted); position: relative;
}
.topbar-icon:hover { background: var(--bg); color: var(--text); }
.badge-dot {
  position: absolute; top: 4px; right: 4px;
  width: 8px; height: 8px; background: var(--danger);
  border-radius: 50%; border: 2px solid var(--surface);
}

.topbar-user {
  display: flex; align-items: center; gap: 8px; cursor: pointer;
  padding: 4px 8px; border-radius: 8px; transition: var(--transition);
  position: relative;
}
.topbar-user:hover { background: var(--bg); }
.topbar-user:hover .topbar-user-menu { display: block; }
.topbar-avatar {
  width: 30px; height: 30px; border-radius: 50%;
  background: var(--primary); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 600;
}
.topbar-username { font-size: 13px; font-weight: 500; }

.topbar-user-menu {
  display: none; position: absolute; top: 100%; right: 0;
  background: var(--surface); border: 1px solid var(--border);
  border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  min-width: 120px; z-index: 300; padding: 4px 0;
}
.menu-item {
  padding: 8px 16px; font-size: 13px; cursor: pointer;
  transition: var(--transition); color: var(--text);
}
.menu-item:hover { background: var(--bg); }
.menu-item.danger { color: var(--danger); }
.menu-item.danger:hover { background: rgba(225,112,85,0.08); }

/* Content */
.admin-content {
  flex: 1; overflow-y: auto; padding: 24px;
  scrollbar-width: thin;
  scrollbar-color: rgba(0,0,0,0.1) transparent;
}
.admin-content::-webkit-scrollbar { width: 6px; }
.admin-content::-webkit-scrollbar-thumb {
  background: rgba(0,0,0,0.12); border-radius: 4px;
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .admin-sidebar {
    position: fixed; left: -240px; height: 100vh; z-index: 200;
  }
  .admin-sidebar.mobile-open { left: 0; }
  .admin-content { padding: 16px; }
  .topbar-search input { width: 140px; }
  .topbar-search input:focus { width: 180px; }
}
</style>
