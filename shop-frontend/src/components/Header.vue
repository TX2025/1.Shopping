<template>
  <div class="header-wrapper">
    <PromoBar />
    <div class="header-main">
      <div class="header-inner">
        <router-link to="/" class="logo">{{ siteName }}</router-link>
        <div class="search-bar">
          <el-input v-model="keyword" placeholder="Search..." size="large" @keyup.enter="search" clearable
            :prefix-icon="Search">
            <template #append>
              <el-button @click="search">搜索</el-button>
            </template>
          </el-input>
        </div>
        <div class="header-actions">
          <a v-if="headerPhone" :href="`tel:${headerPhone}`" class="header-phone">
            <el-icon><Phone /></el-icon>
            <span>{{ headerPhone }}</span>
          </a>
          <div class="lang-switch">
            <span class="lang-item active">中文</span>
            <span class="lang-sep">|</span>
            <span class="lang-item">EN</span>
          </div>
          <el-badge :value="cartTotal" :hidden="!cartTotal" class="cart-badge">
            <el-button circle size="large" @click="$router.push('/cart')" class="cart-btn">
              <el-icon :size="20"><ShoppingCart /></el-icon>
            </el-button>
          </el-badge>
          <template v-if="auth.isLoggedIn">
            <el-dropdown trigger="click">
              <span class="user-info">
                <el-icon :size="16"><User /></el-icon>
                {{ auth.username }}
                <el-icon :size="12"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/user/profile')">个人中心</el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/user/orders')">我的订单</el-dropdown-item>
                  <el-dropdown-item v-if="auth.isAdmin" divided @click="$router.push('/admin')">后台管理</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button class="login-btn" @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" class="register-btn" @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </div>
    <CategoryMegaMenu />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'
import { useSiteConfig } from '../composables/useSiteConfig'
import { Search, ShoppingCart, Phone, ArrowDown, User } from '@element-plus/icons-vue'
import PromoBar from './PromoBar.vue'
import CategoryMegaMenu from './CategoryMegaMenu.vue'

const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()
const keyword = ref('')
const cartTotal = computed(() => auth.isLoggedIn ? cart.totalCount : 0)
const siteName = ref('MyShop')

const { config } = useSiteConfig()

const headerPhone = computed(() => config.value?.header_phone || '')

onMounted(async () => {
  try {
    if (config.value?.site_name) {
      siteName.value = config.value.site_name
    }
  } catch {}
  if (auth.isLoggedIn) {
    try { await cart.fetchCart() } catch {}
  }
})

function search() {
  if (keyword.value.trim()) {
    router.push({ path: '/products', query: { keyword: keyword.value } })
    keyword.value = ''
  }
}
function handleLogout() {
  auth.logout()
  router.push('/')
}
</script>

<style scoped>
.header-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
}
.header-main {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 60px;
  padding: 0 20px;
  gap: 24px;
}
.logo {
  font-size: 22px;
  font-weight: bold;
  color: #00676b;
  white-space: nowrap;
  text-decoration: none;
}
.search-bar {
  flex: 1;
  max-width: 520px;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 14px;
  white-space: nowrap;
}
.header-phone {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #666;
  font-size: 14px;
  text-decoration: none;
}
.header-phone:hover {
  color: #00676b;
}
.user-info {
  cursor: pointer;
  color: #333;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
}
.lang-switch {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #999;
  white-space: nowrap;
}
.lang-item {
  cursor: pointer;
  transition: color 0.2s;
}
.lang-item:hover,
.lang-item.active {
  color: #00676b;
  font-weight: 600;
}
.lang-sep {
  color: #ddd;
}
.cart-btn {
  border-color: #e0e0e0;
}
.cart-badge {
  margin: 0 2px;
}
.login-btn {
  border-color: #ddd;
  color: #555;
}
.register-btn {
  font-weight: 500;
}

@media (max-width: 768px) {
  .header-phone { display: none; }
  .lang-switch { display: none; }
  .search-bar { max-width: 180px; }
}
</style>
