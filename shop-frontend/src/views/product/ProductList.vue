<template>
  <div class="product-list-page">
    <div class="container">
      <div class="sidebar" v-if="pageConfig.showFilters?.length">
        <h3>商品分类</h3>
        <el-menu v-if="pageConfig.showFilters.includes('category')">
          <el-menu-item v-for="cat in categories" :key="cat.id" :index="String(cat.id)"
            @click="filters.categoryId = cat.id; loadProducts()">{{ cat.name }}</el-menu-item>
          <el-menu-item index="" @click="filters.categoryId = null; loadProducts()">全部分类</el-menu-item>
        </el-menu>
      </div>
      <div class="main">
        <div class="toolbar">
          <el-radio-group v-model="displayMode" size="small">
            <el-radio-button value="grid">网格</el-radio-button>
            <el-radio-button value="list">列表</el-radio-button>
          </el-radio-group>
          <el-select v-model="filters.sort" @change="loadProducts" size="small" style="width:140px">
            <el-option label="最新" value="newest" />
            <el-option label="价格从低到高" value="price_asc" />
            <el-option label="价格从高到低" value="price_desc" />
            <el-option label="销量优先" value="sales" />
          </el-select>
        </div>

        <div v-if="displayMode === 'grid'">
          <el-row :gutter="16">
            <el-col :span="6" v-for="p in products" :key="p.id" style="margin-bottom:16px">
              <el-card shadow="hover" class="product-card" @click="$router.push(`/product/${p.id}`)">
                <div class="product-image">{{ p.name?.charAt(0) }}</div>
                <div class="product-info">
                  <h4>{{ p.name }}</h4>
                  <p class="price">¥{{ p.price }} <span class="original">¥{{ p.originalPrice }}</span></p>
                  <p class="sales">已售 {{ p.sales }}</p>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <div v-else>
          <div class="list-item" v-for="p in products" :key="p.id" @click="$router.push(`/product/${p.id}`)">
            <div class="list-image">{{ p.name?.charAt(0) }}</div>
            <div class="list-info">
              <h4>{{ p.name }}</h4>
              <p class="desc">{{ p.description?.substring(0, 100) }}</p>
              <p class="price">¥{{ p.price }} <span class="original">¥{{ p.originalPrice }}</span></p>
            </div>
          </div>
        </div>

        <el-pagination v-if="total > pageConfig.pageSize" background layout="prev, pager, next"
          :total="total" :page-size="pageConfig.pageSize" :current-page="page" @current-change="p => { page=p; loadProducts() }" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getProducts, getCategories } from '../../api/product'
import { getPageConfig } from '../../api/admin'

const route = useRoute()
const products = ref([])
const categories = ref([])
const total = ref(0)
const page = ref(1)
const displayMode = ref('grid')
const filters = ref({ categoryId: null, keyword: '', sort: 'newest' })
const pageConfig = ref({ displayMode: 'grid', pageSize: 20, showFilters: ['category', 'price', 'keyword'], defaultSort: 'newest' })

onMounted(async () => {
  filters.value.keyword = route.query.keyword || ''
  filters.value.categoryId = route.query.categoryId ? Number(route.query.categoryId) : null
  try {
    const res = await getPageConfig('PRODUCT_LIST')
    if (res.data?.configJson) {
      pageConfig.value = { ...pageConfig.value, ...JSON.parse(res.data.configJson) }
      displayMode.value = pageConfig.value.displayMode
    }
  } catch {}
  try {
    const cres = await getCategories()
    if (cres.data) {
      const flat = []
      function walk(items) { items.forEach(i => { flat.push(i); if (i.children) walk(i.children) }) }
      walk(cres.data)
      categories.value = flat
    }
  } catch {}
  loadProducts()
})

async function loadProducts() {
  try {
    const res = await getProducts({
      page: page.value,
      size: pageConfig.value.pageSize,
      categoryId: filters.value.categoryId,
      keyword: filters.value.keyword || null,
      sort: filters.value.sort,
    })
    products.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch {}
}
</script>

<style scoped>
.container { max-width: 1200px; margin: 20px auto; display: flex; gap: 20px; padding: 0 20px; }
.sidebar { width: 200px; }
.main { flex: 1; }
.toolbar { display: flex; justify-content: space-between; margin-bottom: 16px; }
.product-card { cursor: pointer; }
.product-image { background: #f0f2f5; height: 180px; display: flex; align-items: center; justify-content: center; font-size: 48px; color: #409EFF; }
.product-info { padding: 10px 0; }
.product-info h4 { margin-bottom: 8px; }
.price { color: #f56c6c; font-size: 18px; font-weight: bold; }
.original { font-size: 13px; color: #999; text-decoration: line-through; font-weight: normal; margin-left: 8px; }
.sales { font-size: 12px; color: #999; margin-top: 4px; }
.list-item { display: flex; gap: 16px; padding: 16px; border-bottom: 1px solid #eee; cursor: pointer; }
.list-image { width: 150px; height: 150px; background: #f0f2f5; display: flex; align-items: center; justify-content: center; font-size: 36px; color: #409EFF; flex-shrink: 0; }
.list-info { flex: 1; }
.list-info h4 { margin-bottom: 8px; }
.desc { color: #666; font-size: 14px; margin-bottom: 8px; }
</style>
