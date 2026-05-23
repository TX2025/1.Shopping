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
          @click="$router.push(`/product/${p.id}`)">
          <div class="product-image">
            <img v-if="p.coverImage" :src="p.coverImage" :alt="p.name" />
            <span v-else class="image-placeholder">{{ p.name?.charAt(0) }}</span>
          </div>
          <div class="product-info">
            <h4>{{ p.name }}</h4>
            <div class="product-price">
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
import { FolderOpened, Loading } from '@element-plus/icons-vue'
import HeroBanner from '../../components/HeroBanner.vue'
import TrustSection from '../../components/TrustSection.vue'

const { config: pc } = usePageConfig('HOME')
const { config } = useSiteConfig()

const popularProducts = ref([])
const showcaseCats = ref([])
const popularLoading = ref(true)

const popularTitle = computed(() => config.value?.popular_title || '热门推荐')

onMounted(async () => {
  // Load popular products
  const count = parseInt(config.value?.popular_count || '8')
  const sort = config.value?.popular_sort || 'sales'
  try {
    const res = await getProducts({ page: 1, size: count, sort })
    popularProducts.value = res.data?.list || []
  } catch {} finally { popularLoading.value = false }

  // Load showcase categories
  try {
    const res = await getCategories()
    showcaseCats.value = (res.data || []).slice(0, 4)
  } catch {}
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
  font-size: 64px;
  color: #00676b;
  font-weight: bold;
}
.product-info {
  padding: 16px;
}
.product-info h4 {
  margin: 0 0 10px;
  font-size: 15px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.product-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 6px;
}
.price-now {
  color: #e74c3c;
  font-size: 18px;
  font-weight: bold;
}
.price-old {
  color: #999;
  font-size: 13px;
  text-decoration: line-through;
}
.sales {
  font-size: 12px;
  color: #999;
}

.loading-more {
  text-align: center;
  padding: 20px;
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
}
</style>
