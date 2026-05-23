<template>
  <div class="hero-banner" v-if="slides.length">
    <el-carousel :interval="interval" height="auto" trigger="click" arrow="always">
      <el-carousel-item v-for="(slide, i) in slides" :key="i">
        <a :href="slide.link || '#'" class="slide-link">
          <img :src="slide.image" :alt="slide.title" class="slide-image" />
          <div class="slide-overlay" v-if="slide.title">
            <h2>{{ slide.title }}</h2>
          </div>
        </a>
      </el-carousel-item>
    </el-carousel>
    <div class="hero-dots" v-if="false"></div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useSiteConfig } from '../composables/useSiteConfig'

const { config } = useSiteConfig()

const slides = computed(() => {
  try {
    const raw = config.value?.hero_slides
    if (!raw) return []
    const arr = typeof raw === 'string' ? JSON.parse(raw) : raw
    return arr.sort((a, b) => (a.sort || 0) - (b.sort || 0))
  } catch { return [] }
})

const interval = computed(() => parseInt(config.value?.hero_interval || '4000'))
</script>

<style scoped>
.hero-banner {
  width: 100%;
  max-width: 100vw;
}
.slide-link {
  display: block;
  position: relative;
  width: 100%;
}
.slide-image {
  width: 100%;
  height: 460px;
  object-fit: cover;
  display: block;
}
.slide-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 40px 20px 20px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.3));
}
.slide-overlay h2 {
  color: #fff;
  font-size: 28px;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

@media (max-width: 768px) {
  .slide-image {
    height: 200px;
  }
  .slide-overlay h2 {
    font-size: 18px;
  }
}
</style>
