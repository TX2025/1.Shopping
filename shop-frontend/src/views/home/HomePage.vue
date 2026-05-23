<template>
  <div class="home-page">
    <HeroBanner v-if="pc.hero_enabled !== false && config?.hero_enabled !== 'false'" />

    <div class="container" v-if="pc.showcase_enabled !== false && showcaseCats.length">
      <h2 class="section-title">{{ pc.showcase_title || '热门分类' }}</h2>
      <div class="showcase-grid">
        <div class="showcase-card" v-for="cat in showcaseCats" :key="cat.id"
          @click="$router.push(`/products?categoryId=${cat.id}`)">
          <div class="showcase-icon">
            <el-icon :size="36"><FolderOpened /></el-icon>
          </div>
          <span>{{ cat.name }}</span>
        </div>
      </div>
    </div>

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
            <span class="media-badge gif-badge" v-else-if="hasGif(p)">GIF</span>
            <div class="product-actions" v-if="hoveredId === p.id">
              <el-button type="primary" size="small" round @click.stop="$router.push(`/product/${p.id}`)">查看详情</el-button>
            </div>
          </div>
          <div class="product-info">
            <span class="product-category" v-if="getCategoryName(p.categoryId)">{{ getCategoryName(p.categoryId) }}</span>
            <h4>{{ p.name }}</h4>
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
import { FolderOpened, Loading, VideoCameraFilled } from '@element-plus/icons-vue'
import HeroBanner from '../../components/HeroBanner.vue'
import TrustSection from '../../components/TrustSection.vue'

const { config: pc } = usePageConfig('HOME')
const { config } = useSiteConfig()

const popularProducts = ref([])
const showcaseCats = ref([])
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

function isVideo(url) { return /\.mp4$/i.test(url) }
function isGif(url) { return /\.gif$/i.test(url) }

function getFirstVideo(product) {
  const videos = parseVideos(product)
  return videos.length > 0 ? videos[0] : null
}

function hasGif(product) {
  const imgs = parseImages(product)
  return imgs.some(u => isGif(u))
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
    showcaseCats.value = (res.data || []).slice(0, 4)
    const flat = []
    function walk(items) {
      items.forEach(i => { flat.push(i); categoryMap.value[i.id] = i.name; if (i.children) walk(i.children) })
    }
    walk(res.data || [])
    allCategories.value = flat
  } catch {}

  // Load popular products
  const count = parseInt(config.value?.popular_count || '8')
  const sort = config.value?.popular_sort || 'sales'
  try {
    const res = await getProducts({ page: 1, size: count, sort })
    popularProducts.value = res.data?.list || []
  } catch {} finally { popularLoading.value = false }
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

.showcase-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.showcase-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 30px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  color: #333;
}
.showcase-card:hover {
  background: #e8f4f5;
  color: #00676b;
  transform: translateY(-2px);
}
.showcase-icon {
  color: #00676b;
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
.gif-badge {
  background: rgba(0,103,107,0.8);
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
  .showcase-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 768px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); }
  .showcase-grid { grid-template-columns: repeat(2, 1fr); }
  .product-image { height: 180px; }
}
</style>
