<template>
  <div class="promo-bar" v-if="messages.length" :style="{ background: current.bgColor || '#00676b' }">
    <div class="promo-content">
      <span v-for="(msg, i) in messages" :key="i" class="promo-item" :class="{ active: currentIndex === i }"
        :style="{ color: current.textColor || '#ffffff' }">
        {{ msg.text }}
      </span>
    </div>
    <span class="promo-close" @click="dismiss" :style="{ color: current.textColor || '#ffffff' }">✕</span>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useSiteConfig } from '../composables/useSiteConfig'

const { config } = useSiteConfig()

const dismissed = ref(sessionStorage.getItem('promo_dismissed') === '1')
const currentIndex = ref(0)
let timer = null

const messages = computed(() => {
  if (dismissed.value) return []
  try {
    const raw = config.value?.promo_bar_messages
    if (!raw) return []
    return typeof raw === 'string' ? JSON.parse(raw) : raw
  } catch { return [] }
})

const current = computed(() => messages.value[currentIndex.value] || {})

function rotate() {
  if (messages.value.length > 1) {
    currentIndex.value = (currentIndex.value + 1) % messages.value.length
  }
}

function dismiss() {
  dismissed.value = true
  sessionStorage.setItem('promo_dismissed', '1')
  if (timer) clearInterval(timer)
}

onMounted(() => {
  if (messages.value.length > 1) {
    timer = setInterval(rotate, 3000)
  }
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.promo-bar {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  z-index: 1001;
}
.promo-content {
  position: relative;
  height: 100%;
  display: flex;
  align-items: center;
}
.promo-item {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  white-space: nowrap;
  opacity: 0;
  transition: opacity 0.4s;
  font-size: 14px;
}
.promo-item.active {
  opacity: 1;
}
.promo-close {
  position: absolute;
  right: 20px;
  cursor: pointer;
  font-size: 14px;
  opacity: 0.7;
}
.promo-close:hover {
  opacity: 1;
}
</style>
