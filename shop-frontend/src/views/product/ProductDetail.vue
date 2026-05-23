<template>
  <div class="product-detail">
    <div class="container" v-if="product">
      <div class="breadcrumb" v-if="pc.showBreadcrumb !== false">
        <el-breadcrumb separator=">">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/products' }">全部商品</el-breadcrumb-item>
          <el-breadcrumb-item>{{ product.name }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <div class="detail-main">
        <div class="detail-gallery">
          <div class="gallery-main">
            <img v-if="product.coverImage" :src="product.coverImage" :alt="product.name" />
            <span v-else class="gallery-placeholder">{{ product.name?.charAt(0) }}</span>
          </div>
        </div>
        <div class="detail-info">
          <h1>{{ product.name }}</h1>
          <div class="detail-price">
            <span class="price-now">¥{{ product.price }}</span>
            <span class="price-old" v-if="product.originalPrice && product.originalPrice > product.price">¥{{ product.originalPrice }}</span>
          </div>
          <p v-if="pc.showSalesCount !== false" class="sales">已售 {{ product.sales }} 件</p>
          <p class="stock">库存: {{ product.stock }}</p>
          <p class="description">{{ product.description }}</p>
          <div class="actions">
            <el-input-number v-model="quantity" :min="1" :max="product.stock" size="large" />
            <el-button type="primary" size="large" @click="addToCart">加入购物车</el-button>
            <el-button type="danger" size="large" @click="buyNow">立即购买</el-button>
          </div>
          <div class="share" v-if="pc.showShareButtons !== false">
            <span>分享：</span>
            <el-button circle size="small" @click="copyLink"><el-icon><Share /></el-icon></el-button>
          </div>
        </div>
      </div>

      <div class="related" v-if="pc.showRelatedProducts !== false && relatedProducts.length">
        <h2 class="section-title">相关推荐</h2>
        <div class="related-grid">
          <div class="product-card" v-for="p in relatedProducts" :key="p.id" @click="$router.push(`/product/${p.id}`)">
            <div class="product-image">
              <img v-if="p.coverImage" :src="p.coverImage" :alt="p.name" />
              <span v-else class="image-placeholder">{{ p.name?.charAt(0) }}</span>
            </div>
            <div class="product-info">
              <h4>{{ p.name }}</h4>
              <span class="price-now">¥{{ p.price }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="container" style="text-align:center;padding:100px"><h2>商品加载中...</h2></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail, getProducts } from '../../api/product'
import { usePageConfig } from '../../composables/usePageConfig'
import { useCartStore } from '../../stores/cart'
import { ElMessage } from 'element-plus'
import { Share } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const cart = useCartStore()
const { config: pc } = usePageConfig('PRODUCT_DETAIL')

const product = ref(null)
const quantity = ref(1)
const relatedProducts = ref([])

onMounted(async () => {
  try {
    const res = await getProductDetail(route.params.id)
    product.value = res.data
  } catch {}

  if (pc.value.showRelatedProducts !== false) {
    try {
      const count = pc.value.relatedCount || 4
      const res = await getProducts({ page: 1, size: count, sort: 'sales' })
      relatedProducts.value = (res.data?.list || []).filter(p => p.id !== Number(route.params.id)).slice(0, count)
    } catch {}
  }
})

async function addToCart() {
  if (!product.value) return
  try {
    await cart.addToCart(product.value.id, quantity.value)
    ElMessage.success('已添加到购物车')
  } catch {}
}

async function buyNow() {
  await addToCart()
  router.push('/cart')
}

function copyLink() {
  navigator.clipboard?.writeText(window.location.href)
  ElMessage.success('链接已复制')
}
</script>

<style scoped>
.container { max-width: 1200px; margin: 30px auto; padding: 0 20px; }
.breadcrumb { margin-bottom: 20px; }
.detail-main { display: flex; gap: 48px; }
.detail-gallery { flex: 0 0 460px; }
.gallery-main { width: 460px; height: 460px; background: #f5f5f5; border-radius: 8px; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.gallery-main img { width: 100%; height: 100%; object-fit: contain; }
.gallery-placeholder { font-size: 120px; color: #00676b; font-weight: bold; }
.detail-info { flex: 1; }
.detail-info h1 { font-size: 24px; margin-bottom: 20px; color: #333; }
.detail-price { display: flex; align-items: baseline; gap: 12px; margin-bottom: 16px; }
.price-now { color: #e74c3c; font-size: 28px; font-weight: bold; }
.price-old { font-size: 16px; color: #bbb; text-decoration: line-through; }
.sales, .stock { color: #666; margin-bottom: 10px; font-size: 14px; }
.description { color: #555; line-height: 1.8; margin: 16px 0 24px; }
.actions { display: flex; gap: 12px; align-items: center; margin-bottom: 16px; }
.share { display: flex; align-items: center; gap: 8px; color: #999; font-size: 13px; margin-top: 20px; }

.related { margin-top: 60px; }
.section-title { text-align: center; font-size: 24px; margin-bottom: 24px; color: #333; }
.related-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.product-card { background: #fff; border-radius: 8px; overflow: hidden; cursor: pointer; transition: all 0.3s; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.product-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.1); }
.product-image { height: 180px; background: #f0f2f5; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.product-image img { width: 100%; height: 100%; object-fit: cover; }
.image-placeholder { font-size: 48px; color: #00676b; font-weight: bold; }
.product-info { padding: 12px; }
.product-info h4 { font-size: 14px; margin: 0 0 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

@media (max-width: 768px) {
  .detail-main { flex-direction: column; }
  .detail-gallery { flex: 0; }
  .gallery-main { width: 100%; height: 300px; }
  .related-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
