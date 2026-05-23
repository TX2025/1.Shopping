<template>
  <div class="header-wrapper">
    <PromoBar />
    <div class="header-main">
      <div class="header-inner">
        <router-link to="/" class="logo">{{ siteName }}</router-link>
        <div class="search-bar">
          <el-input v-model="keyword" placeholder="搜索商品..." size="large" @keyup.enter="search" clearable
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
          <el-badge :value="cartTotal" :hidden="!cartTotal">
            <el-button circle size="large" @click="$router.push('/cart')">
              <el-icon :size="20"><ShoppingCart /></el-icon>
            </el-button>
          </el-badge>
          <template v-if="auth.isLoggedIn">
            <el-dropdown trigger="click">
              <span class="user-info">{{ auth.username }} <el-icon><ArrowDown /></el-icon></span>
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
            <el-button @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </div>
    <div class="header-nav">
      <div class="nav-inner">
        <router-link to="/" class="nav-item">首页</router-link>
        <router-link to="/products" class="nav-item">全部商品</router-link>
        <router-link v-for="cat in topCategories" :key="cat.id" :to="`/products?categoryId=${cat.id}`" class="nav-item">
          {{ cat.name }}
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'
import { getCategories } from '../api/product'
import { useSiteConfig } from '../composables/useSiteConfig'
import { Search, ShoppingCart, Phone, ArrowDown } from '@element-plus/icons-vue'
import PromoBar from './PromoBar.vue'

const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()
const keyword = ref('')
const cartTotal = ref(0)
const siteName = ref('MyShop')
const topCategories = ref([])

const { config } = useSiteConfig()

const headerPhone = computed(() => config.value?.header_phone || '')

onMounted(async () => {
  try {
    if (config.value?.site_name) {
      siteName.value = config.value.site_name
    }
  } catch {}
  if (auth.isLoggedIn) {
    try { await cart.fetchCart(); cartTotal.value = cart.totalCount } catch {}
  }
  try {
    const res = await getCategories()
    topCategories.value = (res.data || []).slice(0, 6)
  } catch {}
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
.header-nav {
  background: #00676b;
  height: 40px;
}
.nav-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
}
.nav-item {
  color: rgba(255,255,255,0.9);
  text-decoration: none;
  padding: 8px 18px;
  font-size: 14px;
  border-radius: 2px;
  transition: background 0.2s;
}
.nav-item:hover, .nav-item.router-link-active {
  background: rgba(255,255,255,0.15);
  color: #fff;
}

@media (max-width: 768px) {
  .header-phone { display: none; }
  .search-bar { max-width: 200px; }
  .nav-inner { overflow-x: auto; }
}
</style>
