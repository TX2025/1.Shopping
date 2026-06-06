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
        <div class="detail-gallery" @mouseenter="pauseAutoSwitch" @mouseleave="resumeAutoSwitch">
          <div class="gallery-thumbs" v-if="allMedia.length > 1">
            <div
              v-for="(item, idx) in allMedia"
              :key="idx"
              class="thumb-item"
              :class="{ active: currentIndex === idx }"
              @click="selectMedia(idx)"
            >
              <img v-if="item.type === 'image' || item.type === 'gif'" :src="item.url" :alt="`${product.name} ${idx+1}`" />
              <div v-else-if="item.type === 'video'" class="thumb-video">
                <video :src="item.url" muted preload="metadata" />
                <el-icon :size="16" class="thumb-play"><VideoCameraFilled /></el-icon>
              </div>
            </div>
          </div>
          <div class="gallery-main">
            <video v-if="isVideo(currentMedia)" :src="currentMedia" controls autoplay class="gallery-video" />
            <img v-else-if="currentMedia" :src="currentMedia" :alt="product.name" />
            <span v-else class="gallery-placeholder">{{ product.name?.charAt(0) }}</span>
          </div>
        </div>
        <div class="detail-info">
          <h1>{{ product.name }}</h1>
          <div class="detail-price">
            <span class="price-label">Starting at</span>
            <span class="price-now">¥{{ product.price }}</span>
            <span class="price-old" v-if="product.originalPrice && product.originalPrice > product.price">¥{{ product.originalPrice }}</span>
            <span class="discount-badge" v-if="discountPercent">-{{ discountPercent }}% OFF</span>
          </div>
          <div class="detail-meta">
            <span v-if="product.sku" class="meta-sku">SKU: {{ product.sku }}</span>
            <span v-if="product.brand" class="meta-brand">{{ product.brand }}</span>
          </div>
          <div v-if="product.rating" class="detail-rating">
            <span class="stars">{{ '★'.repeat(Math.floor(product.rating)) }}{{ product.rating%1>=0.5?'½':'' }}{{ '☆'.repeat(5-Math.ceil(product.rating)) }}</span>
            <span class="rating-val">{{ product.rating }}</span>
            <span class="rating-count">({{ product.reviews||0 }} 条评价)</span>
          </div>
          <span v-if="product.tag" class="detail-tag" :class="'tag-'+product.tag">{{ {hot:'🔥热销',new:'🆕新品',sale:'💰促销',recommend:'👍推荐'}[product.tag]||product.tag }}</span>
          <p v-if="pc.showSalesCount !== false" class="sales">已售 {{ product.sales }} 件</p>
          <p class="stock">
            <el-icon :size="14"><CircleCheckFilled /></el-icon>
            {{ product.stock > 0 ? `库存: ${product.stock} 件` : '暂时缺货' }}
          </p>
          <p class="description">{{ product.description }}</p>
          <div class="actions">
            <div class="quantity-wrap">
              <span class="quantity-label">数量</span>
              <el-input-number v-model="quantity" :min="1" :max="product.stock" size="large" controls-position="right" />
            </div>
            <el-button type="primary" size="large" class="add-cart-btn" @click="addToCart">
              <el-icon :size="18"><ShoppingCart /></el-icon>
              加入购物车
            </el-button>
            <el-button type="danger" size="large" class="buy-now-btn" @click="buyNow">立即购买</el-button>
            <el-button size="large" class="inquiry-btn" @click="showInquiry=true"><el-icon :size="16"><ChatDotRound /></el-icon> 咨询</el-button>
          </div>
          <div class="share" v-if="pc.showShareButtons !== false">
            <span>分享：</span>
            <el-button circle size="small" @click="copyLink"><el-icon><Share /></el-icon></el-button>
            <el-button circle size="small" @click="copyLink"><el-icon><Link /></el-icon></el-button>
          </div>
        </div>
      </div>

      <div class="related" v-if="pc.showRelatedProducts !== false && relatedProducts.length">
        <h2 class="section-title">You May Also Like</h2>
        <div class="related-grid">
          <div class="product-card" v-for="p in relatedProducts" :key="p.id"
            @click="goToProduct(p)"
            @mouseenter="relatedHoverId = p.id" @mouseleave="relatedHoverId = null">
            <div class="product-image">
              <video v-if="relatedHoverId === p.id && getRelatedFirstVideo(p)" :src="getRelatedFirstVideo(p)" autoplay muted loop playsinline class="product-video" />
              <img v-else-if="getRelatedCover(p)" :src="getRelatedCover(p)" :alt="p.name" />
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

    <!-- Inquiry Dialog -->
    <el-dialog v-model="showInquiry" title="产品咨询" width="460px">
      <el-form :model="inquiryForm" label-width="80px" label-position="top">
        <el-form-item label="感兴趣的型号">{{ product?.name }}</el-form-item>
        <el-form-item label="联系人"><el-input v-model="inquiryForm.customer" placeholder="您的姓名" /></el-form-item>
        <el-form-item label="联系方式"><el-input v-model="inquiryForm.contact" placeholder="手机号或邮箱" /></el-form-item>
        <el-form-item label="咨询内容"><el-input v-model="inquiryForm.summary" type="textarea" :rows="3" placeholder="请输入您想咨询的内容..." /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showInquiry=false">取消</el-button><el-button type="primary" @click="submitInquiry" :loading="inqSaving">提交咨询</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail, getProducts } from '../../api/product'
import { usePageConfig } from '../../composables/usePageConfig'
import { useCartStore } from '../../stores/cart'
import { useAuthStore } from '../../stores/auth'
import { ElMessage } from 'element-plus'
import { Share, Link, ShoppingCart, ChatDotRound, CircleCheckFilled, VideoCameraFilled } from '@element-plus/icons-vue'
import request from '../../utils/request'

const route = useRoute()
const router = useRouter()
const cart = useCartStore()
const auth = useAuthStore()
const { config: pc } = usePageConfig('PRODUCT_DETAIL')

const product = ref(null)
const quantity = ref(1)
const relatedProducts = ref([])
const relatedHoverId = ref(null)
const currentIndex = ref(0)
const showInquiry = ref(false)
const inquiryForm = ref({customer:'',contact:'',summary:''})
const inqSaving = ref(false)

async function submitInquiry(){
  if(!inquiryForm.value.summary.trim()){ElMessage.warning('请输入咨询内容');return}
  inqSaving.value=true
  try{await request.post('/api/inquiries',{...inquiryForm.value,interest:product.value?.name,source:'官网'});ElMessage.success('咨询已提交');showInquiry.value=false;inquiryForm.value={customer:'',contact:'',summary:''}}catch{}finally{inqSaving.value=false}
}
let autoTimer = null
let autoPaused = false

const currentMedia = computed(() => {
  const items = allMedia.value
  if (items.length === 0) return ''
  const idx = Math.min(currentIndex.value, items.length - 1)
  return items[idx]?.url || ''
})

const allMedia = computed(() => {
  if (!product.value) return []
  const items = []
  const seen = new Set()
  function add(url, type) {
    if (!url || seen.has(url)) return
    seen.add(url)
    // Determine actual type from extension
    let actualType = type
    if (/\.mp4$/i.test(url)) actualType = 'video'
    else if (/\.gif$/i.test(url)) actualType = 'gif'
    else actualType = 'image'
    items.push({ url, type: actualType })
  }
  // Priority: videos first, then gifs, then static images
  if (product.value.videos) {
    try {
      const videos = typeof product.value.videos === 'string' ? JSON.parse(product.value.videos) : product.value.videos
      videos.forEach(url => add(url, 'video'))
    } catch {}
  }
  if (product.value.images) {
    try {
      const extra = typeof product.value.images === 'string' ? JSON.parse(product.value.images) : product.value.images
      extra.forEach(url => add(url, 'image'))
    } catch {}
  }
  if (product.value.coverImage) add(product.value.coverImage, 'image')
  return items
})

function isVideo(url) { return url && /\.mp4$/i.test(url) }

function relatedParseImages(p) {
  if (!p.images) return []
  try { return typeof p.images === 'string' ? JSON.parse(p.images) : p.images } catch { return [] }
}
function relatedParseVideos(p) {
  if (!p.videos) return []
  try { return typeof p.videos === 'string' ? JSON.parse(p.videos) : p.videos } catch { return [] }
}
function getRelatedFirstVideo(p) {
  return relatedParseVideos(p)[0] || null
}
function getRelatedCover(p) {
  const vids = relatedParseVideos(p)
  if (vids.length > 0) return vids[0]
  const imgs = relatedParseImages(p)
  if (imgs.length > 0) return imgs[0]
  return p.coverImage || null
}

const discountPercent = computed(() => {
  if (!product.value?.originalPrice || !product.value?.price) return null
  const orig = parseFloat(product.value.originalPrice)
  const now = parseFloat(product.value.price)
  if (orig <= now) return null
  return Math.round((1 - now / orig) * 100)
})

const switchInterval = computed(() => parseInt(pc.value?.imageSwitchInterval || '5') * 1000)

function selectMedia(idx) {
  currentIndex.value = idx
  resetAutoTimer()
}

function pauseAutoSwitch() {
  autoPaused = true
  stopAutoTimer()
}

function resumeAutoSwitch() {
  autoPaused = false
  if (allMedia.value.length > 1) startAutoTimer()
}

function stopAutoTimer() {
  if (autoTimer) { clearInterval(autoTimer); autoTimer = null }
}

function startAutoTimer() {
  stopAutoTimer()
  if (autoPaused) return
  const ms = switchInterval.value
  if (ms <= 0) return
  autoTimer = setInterval(() => {
    const len = allMedia.value.length
    if (len <= 1) return
    currentIndex.value = (currentIndex.value + 1) % len
  }, ms)
}

function resetAutoTimer() {
  stopAutoTimer()
  if (!autoPaused && allMedia.value.length > 1) startAutoTimer()
}

async function loadProduct(id) {
  stopAutoTimer()
  currentIndex.value = 0
  try {
    const res = await getProductDetail(id)
    product.value = res.data
  } catch {}

  if (pc.value.showRelatedProducts !== false) {
    try {
      const count = pc.value.relatedCount || 4
      const res = await getProducts({ page: 1, size: count, sort: 'sales' })
      relatedProducts.value = (res.data?.list || []).filter(p => p.id !== Number(id)).slice(0, count)
    } catch {}
  }

  if (allMedia.value.length > 1) startAutoTimer()
}

onMounted(() => {
  loadProduct(route.params.id)
  // auto-add after login redirect
  const pending = sessionStorage.getItem('pendingAddToCart')
  if (pending) {
    sessionStorage.removeItem('pendingAddToCart')
    try {
      const { productId, qty } = JSON.parse(pending)
      if (productId === Number(route.params.id)) {
        addToCartDirect(productId, qty)
      }
    } catch {}
  }
})

watch(() => route.params.id, (newId) => {
  if (newId) loadProduct(newId)
})

onBeforeUnmount(() => stopAutoTimer())

async function addToCart() {
  if (!product.value) return
  if (!auth.isLoggedIn) {
    sessionStorage.setItem('pendingAddToCart', JSON.stringify({
      productId: product.value.id,
      qty: quantity.value
    }))
    router.push('/login?redirect=' + encodeURIComponent(route.fullPath))
    return
  }
  await addToCartDirect(product.value.id, quantity.value)
}

async function addToCartDirect(productId, qty) {
  try {
    await cart.addToCart(productId, qty)
    ElMessage.success('已添加到购物车')
    router.push('/cart')
  } catch {}
}

async function buyNow() {
  await addToCart()
  router.push('/cart')
}

function goToProduct(p) {
  if (!p?.id) return
  router.push(`/product/${p.id}`)
}

function copyLink() {
  navigator.clipboard?.writeText(window.location.href)
  ElMessage.success('链接已复制')
}
</script>

<style scoped>
.container { max-width: 1200px; margin: 30px auto; padding: 0 20px; }
.breadcrumb { margin-bottom: 24px; }

.detail-main { display: flex; gap: 48px; }
.detail-gallery {
  display: flex;
  gap: 12px;
  flex: 0 0 auto;
}
.gallery-thumbs {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
  width: 72px;
}
.gallery-main {
  width: 480px;
  height: 480px;
  background: #f8f8f8;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}
.gallery-main img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: opacity 0.3s;
}
.gallery-placeholder {
  font-size: 120px;
  color: #00676b;
  font-weight: bold;
}
.thumb-item {
  width: 72px;
  height: 72px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.2s;
  background: #f5f5f5;
  flex-shrink: 0;
}
.thumb-item.active {
  border-color: #00676b;
}
.thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.thumb-video {
  width: 100%;
  height: 100%;
  position: relative;
}
.thumb-video video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.thumb-play {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #fff;
  background: rgba(0,0,0,0.5);
  border-radius: 50%;
  padding: 3px;
}
.gallery-video {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #000;
}

.detail-info { flex: 1; }
.detail-info h1 {
  font-size: 24px;
  margin: 0 0 20px;
  color: #222;
  line-height: 1.3;
}
.detail-price {
  display: flex;
  align-items: baseline;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}
.price-label {
  font-size: 13px;
  color: #999;
  width: 100%;
  margin-bottom: 2px;
}
.price-now { color: #e74c3c; font-size: 30px; font-weight: bold; }
.price-old { font-size: 17px; color: #bbb; text-decoration: line-through; }
.discount-badge {
  background: #e74c3c;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 4px;
}
.detail-meta { display:flex; gap:10px; margin-bottom:8px; align-items:center }
.meta-sku { color:#909399; font-size:13px; background:#f5f7fa; padding:2px 8px; border-radius:4px }
.meta-brand { color:#6c5ce7; font-size:12px; font-weight:600; background:rgba(108,92,231,.08); padding:2px 8px; border-radius:4px }
.detail-rating { display:flex; align-items:center; gap:6px; margin-bottom:8px }
.detail-rating .stars { color:#fdcb6e; font-size:15px; letter-spacing:2px }
.detail-rating .rating-val { font-weight:700; color:#e67e22; font-size:14px }
.detail-rating .rating-count { color:#909399; font-size:12px }
.detail-tag { display:inline-block; padding:2px 10px; border-radius:12px; font-size:11px; font-weight:700; margin-bottom:8px }
.tag-hot { background:rgba(225,112,85,.12); color:#c0392b }
.tag-new { background:rgba(116,185,255,.15); color:#2980b9 }
.tag-sale { background:rgba(253,203,110,.2); color:#d68910 }
.tag-recommend { background:rgba(0,184,148,.12); color:#00796b }
.sales { color: #888; margin-bottom: 8px; font-size: 14px; }
.stock {
  color: #4caf50;
  margin-bottom: 12px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.description {
  color: #555;
  line-height: 1.8;
  margin: 18px 0 24px;
  font-size: 14px;
}
.actions {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.quantity-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}
.quantity-label { font-size: 14px; color: #666; }
.add-cart-btn, .buy-now-btn {
  padding: 12px 24px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 8px;
}
.share {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #999;
  font-size: 13px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.related { margin-top: 64px; }
.section-title {
  text-align: center;
  font-size: 24px;
  margin-bottom: 28px;
  color: #333;
  position: relative;
}
.section-title::after {
  content: '';
  display: block;
  width: 50px;
  height: 3px;
  background: #00676b;
  margin: 12px auto 0;
  border-radius: 2px;
}
.related-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.product-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  border: 1px solid #f0f0f0;
}
.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
}
.product-image {
  height: 200px;
  background: #f7f8f9;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.product-image img,
.product-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s;
}
.product-card:hover .product-image img {
  transform: scale(1.05);
}
.image-placeholder {
  font-size: 48px;
  color: #00676b;
  font-weight: bold;
}
.product-info { padding: 12px 14px; }
.product-info h4 {
  font-size: 14px;
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #333;
}
.product-info .price-now {
  font-size: 16px;
}

@media (max-width: 768px) {
  .detail-main { flex-direction: column; gap: 24px; }
  .detail-gallery { flex-direction: column-reverse; }
  .gallery-thumbs { flex-direction: row; width: auto; overflow-x: auto; }
  .gallery-main { width: 100%; height: 340px; }
  .thumb-item { width: 56px; height: 56px; }
  .related-grid { grid-template-columns: repeat(2, 1fr); }
  .actions { flex-direction: column; align-items: stretch; }
  .add-cart-btn, .buy-now-btn { width: 100%; }
}
</style>
