<template>
  <div class="home-page">
    <el-carousel v-if="pageConfig.banners?.length" :interval="4000" height="400px">
      <el-carousel-item v-for="b in pageConfig.banners" :key="b.sort">
        <div class="banner" :style="{ backgroundImage: `url(${b.image})` }" @click="$router.push(b.link||'/')" />
      </el-carousel-item>
    </el-carousel>

    <div class="container" v-for="(section, idx) in pageConfig.sections" :key="idx">
      <template v-if="section.type === 'category_showcase'">
        <h2 class="section-title">{{ section.title }}</h2>
        <el-row :gutter="20">
          <el-col :span="6" v-for="cat in showcaseCategories" :key="cat.id">
            <el-card shadow="hover" class="category-card" @click="$router.push(`/products?categoryId=${cat.id}`)">
              {{ cat.name }}
            </el-card>
          </el-col>
        </el-row>
      </template>

      <template v-if="section.type === 'product_grid'">
        <h2 class="section-title">{{ section.title }}</h2>
        <el-row :gutter="20">
          <el-col :span="6" v-for="p in featuredProducts" :key="p.id">
            <el-card shadow="hover" class="product-card" @click="$router.push(`/product/${p.id}`)">
              <div class="product-image">{{ p.name?.charAt(0) }}</div>
              <div class="product-info">
                <h4>{{ p.name }}</h4>
                <p class="price">¥{{ p.price }}</p>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getProducts } from '../../api/product'
import { getPageConfig } from '../../api/admin'

const pageConfig = ref({ banners: [], sections: [] })
const featuredProducts = ref([])
const showcaseCategories = ref([])

onMounted(async () => {
  try {
    const res = await getPageConfig('HOME')
    if (res.data?.configJson) {
      pageConfig.value = JSON.parse(res.data.configJson)
      if (pageConfig.value.sections) {
        for (const s of pageConfig.value.sections) {
          if (s.type === 'product_grid') {
            const pr = await getProducts({ page: 1, size: s.displayCount || 8, sort: s.sortBy || 'sales' })
            featuredProducts.value = pr.data?.list || []
          }
        }
      }
    }
  } catch {}
})
</script>

<style scoped>
.container { max-width: 1200px; margin: 0 auto; padding: 20px; }
.section-title { margin: 30px 0 16px; font-size: 22px; }
.banner { width: 100%; height: 400px; background-size: cover; background-position: center; cursor: pointer; background-color: #409EFF; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 36px; }
.category-card { text-align: center; cursor: pointer; height: 100px; display: flex; align-items: center; justify-content: center; font-size: 16px; }
.product-card { cursor: pointer; }
.product-image { background: #f0f2f5; height: 180px; display: flex; align-items: center; justify-content: center; font-size: 48px; color: #409EFF; }
.product-info { padding: 10px 0; }
.product-info h4 { margin-bottom: 8px; }
.price { color: #f56c6c; font-size: 18px; font-weight: bold; }
</style>
