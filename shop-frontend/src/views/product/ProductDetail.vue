<template>
  <div class="product-detail">
    <div class="container" v-if="product">
      <div class="detail-main">
        <div class="detail-image">{{ product.name?.charAt(0) }}</div>
        <div class="detail-info">
          <h1>{{ product.name }}</h1>
          <p class="detail-price">¥{{ product.price }} <span class="original">¥{{ product.originalPrice }}</span></p>
          <p v-if="pageConfig.showSalesCount" class="sales">已售 {{ product.sales }} 件</p>
          <p class="stock">库存: {{ product.stock }}</p>
          <p class="description">{{ product.description }}</p>
          <div class="actions">
            <el-input-number v-model="quantity" :min="1" :max="product.stock" />
            <el-button type="primary" size="large" @click="addToCart">加入购物车</el-button>
            <el-button type="danger" size="large" @click="buyNow">立即购买</el-button>
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
import { getProductDetail } from '../../api/product'
import { getPageConfig } from '../../api/admin'
import { useCartStore } from '../../stores/cart'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const cart = useCartStore()
const product = ref(null)
const quantity = ref(1)
const pageConfig = ref({ showSalesCount: true })

onMounted(async () => {
  try {
    const res = await getProductDetail(route.params.id)
    product.value = res.data
  } catch {}
  try {
    const pr = await getPageConfig('PRODUCT_DETAIL')
    if (pr.data?.configJson) pageConfig.value = { ...pageConfig.value, ...JSON.parse(pr.data.configJson) }
  } catch {}
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
</script>

<style scoped>
.container { max-width: 1000px; margin: 40px auto; padding: 0 20px; }
.detail-main { display: flex; gap: 40px; }
.detail-image { width: 400px; height: 400px; background: #f0f2f5; display: flex; align-items: center; justify-content: center; font-size: 80px; color: #409EFF; flex-shrink: 0; }
.detail-info { flex: 1; }
.detail-info h1 { font-size: 24px; margin-bottom: 16px; }
.detail-price { color: #f56c6c; font-size: 28px; font-weight: bold; margin-bottom: 12px; }
.original { font-size: 16px; color: #999; text-decoration: line-through; font-weight: normal; margin-left: 12px; }
.sales, .stock { color: #666; margin-bottom: 8px; }
.description { color: #333; line-height: 1.8; margin: 16px 0; }
.actions { display: flex; gap: 12px; align-items: center; margin-top: 20px; }
</style>
