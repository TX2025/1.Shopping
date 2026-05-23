<template>
  <div class="trust-section" v-if="features.length">
    <div class="trust-container">
      <h2 class="trust-title">{{ trustTitle }}</h2>
      <div class="trust-grid">
        <div class="trust-item" v-for="(item, i) in features" :key="i">
          <el-icon :size="36" color="#00676b"><component :is="item.icon" /></el-icon>
          <h3>{{ item.title }}</h3>
          <p>{{ item.description }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useSiteConfig } from '../composables/useSiteConfig'

const { config } = useSiteConfig()

const trustTitle = computed(() => config.value?.trust_title || '为什么选择我们')

const features = computed(() => {
  try {
    const raw = config.value?.trust_features
    if (!raw) return []
    return typeof raw === 'string' ? JSON.parse(raw) : raw
  } catch { return [] }
})
</script>

<style scoped>
.trust-section {
  padding: 60px 0;
  background: #f8f9fa;
}
.trust-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}
.trust-title {
  text-align: center;
  font-size: 28px;
  margin-bottom: 40px;
  color: #333;
}
.trust-grid {
  display: flex;
  justify-content: center;
  gap: 40px;
  flex-wrap: wrap;
}
.trust-item {
  flex: 0 0 220px;
  text-align: center;
  padding: 30px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: transform 0.3s;
}
.trust-item:hover {
  transform: translateY(-4px);
}
.trust-item .el-icon {
  margin-bottom: 16px;
}
.trust-item h3 {
  font-size: 16px;
  margin: 0 0 8px;
  color: #333;
}
.trust-item p {
  font-size: 13px;
  color: #999;
  margin: 0;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .trust-grid {
    gap: 16px;
  }
  .trust-item {
    flex: 0 0 45%;
    padding: 20px 12px;
  }
}
</style>
