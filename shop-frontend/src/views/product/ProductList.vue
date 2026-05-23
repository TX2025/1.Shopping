<template>
  <div class="product-list-page">
    <div class="container" :class="{ 'has-sidebar': showSidebar }">
      <div class="sidebar" v-if="showSidebar">
        <h3>商品分类</h3>
        <el-menu>
          <el-menu-item index="" @click="filters.categoryId = null; loadProducts()">全部分类</el-menu-item>
          <el-menu-item v-for="cat in categories" :key="cat.id" :index="String(cat.id)"
            @click="filters.categoryId = cat.id; loadProducts()">{{ cat.name }}</el-menu-item>
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

        <div v-if="displayMode === 'grid'" class="product-grid">
          <div class="product-card" v-for="p in products" :key="p.id" @click="$router.push(`/product/${p.id}`)">
            <div class="product-image">
              <img v-if="p.coverImage" :src="p.coverImage" :alt="p.name" />
              <span v-else class="image-placeholder">{{ p.name?.charAt(0) }}</span>
            </div>
            <div class="product-info">
              <h4>{{ p.name }}</h4>
              <div class="product-price">
                <span class="price-now">¥{{ p.price }}</span>
                <span class="price-old" v-if="pc.showOriginalPrice !== false && p.originalPrice && p.originalPrice > p.price">¥{{ p.originalPrice }}</span>
              </div>
              <p class="sales" v-if="pc.showSalesCount !== false">已售 {{ p.sales }} 件</p>
            </div>
          </div>
        </div>

        <div v-else>
          <div class="list-item" v-for="p in products" :key="p.id" @click="$router.push(`/product/${p.id}`)">
            <div class="list-image">
              <img v-if="p.coverImage" :src="p.coverImage" :alt="p.name" />
              <span v-else class="image-placeholder">{{ p.name?.charAt(0) }}</span>
            </div>
            <div class="list-info">
              <h4>{{ p.name }}</h4>
              <p class="desc">{{ p.description?.substring(0, 100) }}</p>
              <div class="product-price">
                <span class="price-now">¥{{ p.price }}</span>
                <span class="price-old" v-if="pc.showOriginalPrice !== false && p.originalPrice && p.originalPrice > p.price">¥{{ p.originalPrice }}</span>
              </div>
              <p class="sales" v-if="pc.showSalesCount !== false">已售 {{ p.sales }} 件</p>
            </div>
          </div>
        </div>

        <el-pagination v-if="total > pageSize" background layout="prev, pager, next"
          :total="total" :page-size="pageSize" :current-page="page" @current-change="p => { page=p; loadProducts() }" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { getProducts, getCategories } from '../../api/product'
import { usePageConfig } from '../../composables/usePageConfig'

const route = useRoute()
const { config: pc } = usePageConfig('PRODUCT_LIST')

const products = ref([])
const categories = ref([])
const total = ref(0)
const page = ref(1)
const displayMode = ref('grid')
const filters = ref({ categoryId: null, keyword: '', sort: 'newest' })

const showSidebar = computed(() => pc.value.showSidebar !== false)
const pageSize = computed(() => pc.value.pageSize || 20)

onMounted(async () => {
  filters.value.keyword = route.query.keyword || ''
  filters.value.categoryId = route.query.categoryId ? Number(route.query.categoryId) : null
  displayMode.value = pc.value.displayMode || 'grid'
  filters.value.sort = pc.value.defaultSort || 'newest'

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
      size: pageSize.value,
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
.container { max-width: 1200px; margin: 20px auto; display: flex; gap: 24px; padding: 0 20px; }
.has-sidebar { flex-direction: row; }
.sidebar { width: 200px; flex-shrink: 0; }
.sidebar h3 { font-size: 16px; margin: 0 0 12px 12px; color: #333; }
.main { flex: 1; min-width: 0; }
.toolbar { display: flex; justify-content: space-between; margin-bottom: 20px; }

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.product-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
}
.product-image {
  height: 200px;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.product-card:hover .product-image img {
  transform: scale(1.05);
}
.image-placeholder {
  font-size: 56px;
  color: #00676b;
  font-weight: bold;
}
.product-info {
  padding: 14px;
}
.product-info h4 {
  margin: 0 0 8px;
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.product-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 4px;
}
.price-now { color: #e74c3c; font-size: 17px; font-weight: bold; }
.price-old { color: #bbb; font-size: 12px; text-decoration: line-through; }
.sales { font-size: 12px; color: #999; margin: 0; }

.list-item { display: flex; gap: 20px; padding: 16px; margin-bottom: 12px; background: #fff; border-radius: 8px; cursor: pointer; box-shadow: 0 1px 6px rgba(0,0,0,0.05); transition: box-shadow 0.3s; }
.list-item:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.1); }
.list-image { width: 160px; height: 160px; background: #f0f2f5; display: flex; align-items: center; justify-content: center; flex-shrink: 0; border-radius: 4px; overflow: hidden; }
.list-image img { width: 100%; height: 100%; object-fit: cover; }
.list-info { flex: 1; }
.list-info h4 { margin: 0 0 8px; font-size: 16px; }
.desc { color: #888; font-size: 13px; margin-bottom: 8px; line-height: 1.5; }

@media (max-width: 1024px) {
  .product-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 768px) {
  .container { flex-direction: column; }
  .sidebar { width: 100%; }
  .product-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
