<template>
  <div class="home-page">
    <HeroBanner v-if="pc.hero_enabled !== false" />

    <div class="container" v-if="pc.popular_enabled !== false">
      <h2 class="section-title">{{ popularTitle }}</h2>
      <div class="product-grid">
        <div class="product-card" v-for="p in popularProducts" :key="p.id"
          @click="$router.push(`/product/${p.id}`)"
          @mouseenter="hoveredId = p.id" @mouseleave="hoveredId = null">
          <div class="product-image">
            <video v-if="hoveredId === p.id && getFirstVideo(p)" :src="getFirstVideo(p)" autoplay muted loop playsinline class="product-video" />
            <img v-else-if="getCoverMedia(p)" :src="hoveredId === p.id ? getHoverMedia(p) : getCoverMedia(p)" :alt="p.name" />
            <span v-else class="image-placeholder">{{ p.name?.charAt(0) }}</span>
            <span class="media-badge" v-if="getFirstVideo(p)"><el-icon :size="14"><VideoCameraFilled /></el-icon></span>
            <div class="product-actions" v-if="hoveredId === p.id">
              <el-button type="primary" size="small" round @click.stop="$router.push(`/product/${p.id}`)">查看详情</el-button>
            </div>
          </div>
          <div class="product-info">
            <span class="product-category" v-if="getCategoryName(p.categoryId)">{{ getCategoryName(p.categoryId) }}</span>
            <h4>{{ p.name }}</h4>
            <div v-if="p.rating" class="hp-rating"><span class="hp-stars">{{ '★'.repeat(Math.floor(p.rating)) }}</span><span class="hp-rv">{{ p.rating }}</span></div>
            <span v-if="p.tag" class="hp-tag" :class="'tag-'+p.tag">{{ {hot:'🔥热销',new:'🆕新品',sale:'💰促销',recommend:'👍推荐'}[p.tag] }}</span>
            <div class="product-price">
              <span class="price-label">Starting at</span>
              <span class="price-now">¥{{ p.price }}</span>
              <span class="price-old" v-if="p.originalPrice && p.originalPrice > p.price">¥{{ p.originalPrice }}</span>
            </div>
            <span class="sales" v-if="p.sales">已售 {{ p.sales }} 件</span>
          </div>
        </div>
      </div>
      <div class="loading-more" v-if="popularLoading">
        <el-icon class="loading-icon" :size="24"><Loading /></el-icon>
      </div>
    </div>

    <TrustSection v-if="pc.trust_enabled !== false && config?.trust_enabled !== 'false'" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getProducts, getCategories } from '../../api/product'
import { usePageConfig } from '../../composables/usePageConfig'
import { useSiteConfig } from '../../composables/useSiteConfig'
import { Loading, VideoCameraFilled } from '@element-plus/icons-vue'
import HeroBanner from '../../components/HeroBanner.vue'
import TrustSection from '../../components/TrustSection.vue'

const { config: pc } = usePageConfig('HOME')
const { config } = useSiteConfig()

const popularProducts = ref([])
const popularLoading = ref(true)
const hoveredId = ref(null)
const allCategories = ref([])
const categoryMap = ref({})

const popularTitle = computed(() => config.value?.popular_title || '热门推荐')

function parseImages(product) {
  if (!product.images) return []
  try {
    return typeof product.images === 'string' ? JSON.parse(product.images) : product.images
  } catch { return [] }
}

function parseVideos(product) {
  if (!product.videos) return []
  try {
    return typeof product.videos === 'string' ? JSON.parse(product.videos) : product.videos
  } catch { return [] }
}

function getFirstVideo(product) {
  const videos = parseVideos(product)
  return videos.length > 0 ? videos[0] : null
}

// Priority: video > gif > static image > coverImage
function getCoverMedia(product) {
  const firstVideo = getFirstVideo(product)
  if (firstVideo) return firstVideo
  const imgs = parseImages(product)
  if (imgs.length > 0) return imgs[0]
  return product.coverImage || null
}

// Hover: swap to second image or keep cover (video handled separately in template)
function getHoverMedia(product) {
  const imgs = parseImages(product)
  const cover = getCoverMedia(product)
  const second = imgs.find(u => u !== cover)
  return second || cover
}

function getCategoryName(catId) {
  return categoryMap.value[catId] || ''
}

onMounted(async () => {
  // Load categories for name mapping
  try {
    const res = await getCategories()
    const flat = []
    function walk(items) {
      items.forEach(i => { flat.push(i); categoryMap.value[i.id] = i.name; if (i.children) walk(i.children) })
    }
    walk(res.data || [])
    allCategories.value = flat
  } catch {}

  // Load popular products: admin-selected or auto by sales
  const rawPopular = pc.value.popular_products
  if (Array.isArray(rawPopular) && rawPopular.length > 0) {
    const ids = rawPopular.map(item => typeof item === 'object' ? item.productId : item)
    try {
      const res = await getProducts({ page: 1, size: ids.length, sort: 'id' })
      const all = res.data?.list || []
      popularProducts.value = ids.map(id => all.find(p => p.id === id)).filter(Boolean)
    } catch {} finally { popularLoading.value = false }
  } else {
    const count = parseInt(config.value?.popular_count || '8')
    const sort = config.value?.popular_sort || 'sales'
    try {
      const res = await getProducts({ page: 1, size: count, sort })
      popularProducts.value = res.data?.list || []
    } catch {} finally { popularLoading.value = false }
  }
})
</script>

<style scoped>
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
}
.section-title {
  text-align: center;
  font-size: 26px;
  color: #333;
  margin: 20px 0 28px;
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

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
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
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(0,0,0,0.12);
  border-color: transparent;
}
.product-image {
  height: 240px;
  background: #f7f8f9;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}
.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}
.product-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.product-card:hover .product-image img {
  transform: scale(1.08);
}
.media-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(0,0,0,0.55);
  color: #fff;
  border-radius: 4px;
  padding: 2px 6px;
  display: flex;
  align-items: center;
  font-size: 11px;
  font-weight: 600;
  z-index: 2;
}
.image-placeholder {
  font-size: 64px;
  color: #00676b;
  font-weight: bold;
}
.product-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px;
  background: linear-gradient(transparent, rgba(0,0,0,0.5));
  display: flex;
  justify-content: center;
  animation: fadeInUp 0.25s ease;
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
.product-info {
  padding: 16px;
}
.hp-rating { display:flex; align-items:center; gap:4px; margin:2px 0 }
.hp-stars { color:#fdcb6e; font-size:13px; letter-spacing:1px }
.hp-rv { font-weight:700; color:#e67e22; font-size:12px }
.hp-tag { display:inline-block; padding:1px 6px; border-radius:8px; font-size:10px; font-weight:700; margin:2px 0 }
.tag-hot { background:rgba(225,112,85,.12); color:#c0392b }
.tag-new { background:rgba(116,185,255,.15); color:#2980b9 }
.tag-sale { background:rgba(253,203,110,.2); color:#d68910 }
.tag-recommend { background:rgba(0,184,148,.12); color:#00796b }
.product-category {
  font-size: 11px;
  color: #00676b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 600;
}
.product-info h4 {
  margin: 4px 0 10px;
  font-size: 15px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}
.product-price {
  display: flex;
  align-items: baseline;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 6px;
}
.price-label {
  font-size: 12px;
  color: #999;
  width: 100%;
  margin-bottom: 2px;
}
.price-now {
  color: #e74c3c;
  font-size: 20px;
  font-weight: bold;
}
.price-old {
  color: #bbb;
  font-size: 13px;
  text-decoration: line-through;
}
.sales {
  font-size: 12px;
  color: #999;
}

.loading-more {
  text-align: center;
  padding: 30px;
}
.loading-icon {
  animation: spin 1s linear infinite;
  color: #00676b;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@media (max-width: 1024px) {
  .product-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 768px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); }
  .product-image { height: 180px; }
}
</style>
