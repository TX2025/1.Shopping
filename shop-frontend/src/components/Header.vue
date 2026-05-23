<template>
  <el-header class="header">
    <div class="header-inner">
      <router-link to="/" class="logo">{{ siteName }}</router-link>
      <div class="search-bar">
        <el-input v-model="keyword" placeholder="搜索商品..." @keyup.enter="search">
          <template #append><el-button @click="search" :icon="Search">搜索</el-button></template>
        </el-input>
      </div>
      <div class="header-actions">
        <el-badge :value="cartTotal" :hidden="!cartTotal">
          <el-button circle :icon="ShoppingCart" @click="$router.push('/cart')" />
        </el-badge>
        <template v-if="auth.isLoggedIn">
          <el-dropdown>
            <span class="user-info">{{ auth.username }}</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/user/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item @click="$router.push('/user/orders')">我的订单</el-dropdown-item>
                <el-dropdown-item v-if="auth.isAdmin" @click="$router.push('/admin')" divided>后台管理</el-dropdown-item>
                <el-dropdown-item @click="handleLogout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button @click="$router.push('/login')">登录</el-button>
          <el-button type="primary" @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </div>
  </el-header>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'
import { getSiteConfig } from '../api/admin'

const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()
const keyword = ref('')
const cartTotal = ref(0)
const siteName = ref('MyShop')

onMounted(async () => {
  try {
    const res = await getSiteConfig()
    if (res.data) siteName.value = res.data.site_name || 'MyShop'
  } catch {}
  if (auth.isLoggedIn) {
    try { await cart.fetchCart(); cartTotal.value = cart.totalCount } catch {}
  }
})

function search() {
  if (keyword.value.trim()) {
    router.push({ path: '/products', query: { keyword: keyword.value } })
  }
}
function handleLogout() {
  auth.logout()
  router.push('/')
}
</script>

<style scoped>
.header { background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.06); position: fixed; top: 0; z-index: 100; width: 100%; height: 64px; }
.header-inner { max-width: 1200px; margin: 0 auto; display: flex; align-items: center; height: 100%; gap: 24px; }
.logo { font-size: 22px; font-weight: bold; color: #409EFF; white-space: nowrap; }
.search-bar { flex: 1; max-width: 500px; }
.header-actions { display: flex; align-items: center; gap: 12px; }
.user-info { cursor: pointer; color: #333; }
</style>
