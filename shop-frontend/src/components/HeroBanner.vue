<template>
  <div class="hero-wrapper" v-if="slides.length" :style="wrapperStyle" @mouseenter="pause" @mouseleave="resume">
    <div class="hero-viewport" :style="viewportStyle">
      <Transition :name="transitionName">
        <div class="hero-slide" :key="current" :style="slideStyle">
          <a v-if="currentSlide.link" :href="currentSlide.link" class="slide-media-wrap">
            <video
              v-if="isVideo(currentSlide.image)"
              ref="videoRef"
              :src="currentSlide.image"
              :muted="true"
              :loop="false"
              playsinline
              class="slide-media"
              @loadedmetadata="onVideoReady"
              @ended="onVideoEnd"
            />
            <img v-else :src="currentSlide.image" class="slide-media" alt="" />
          </a>
          <div v-else class="slide-media-wrap">
            <video
              v-if="isVideo(currentSlide.image)"
              ref="videoRef"
              :src="currentSlide.image"
              :muted="true"
              :loop="false"
              playsinline
              class="slide-media"
              @loadedmetadata="onVideoReady"
              @ended="onVideoEnd"
            />
            <img v-else :src="currentSlide.image" class="slide-media" alt="" />
          </div>
          <div class="hero-caption" v-if="currentSlide.title">
            <h2>{{ currentSlide.title }}</h2>
          </div>
        </div>
      </Transition>

      <button class="hero-arrow left" @click="prev">
        <span>&lsaquo;</span>
      </button>
      <button class="hero-arrow right" @click="next">
        <span>&rsaquo;</span>
      </button>

      <div class="hero-dots" v-if="slides.length > 1">
        <button
          v-for="(s, i) in slides"
          :key="i"
          class="hero-dot"
          :class="{ active: i === current }"
          @click="goTo(i)"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { usePageConfig } from '../composables/usePageConfig'

const { config: pc } = usePageConfig('HOME')

const slides = computed(() => {
  try {
    const raw = pc.value?.hero_slides
    if (!raw) return []
    const arr = typeof raw === 'string' ? JSON.parse(raw) : raw
    return arr.sort((a, b) => (a.sort || 0) - (b.sort || 0))
  } catch { return [] }
})

const interval = computed(() => parseInt(pc.value?.hero_interval || '5000'))
const bannerWidth = computed(() => pc.value?.hero_width || '100%')
const bannerHeight = computed(() => parseInt(pc.value?.hero_height || '500'))

const wrapperStyle = computed(() => ({
  width: isNaN(bannerWidth.value) ? bannerWidth.value : bannerWidth.value + 'px',
  maxWidth: '100vw',
  margin: '0 auto',
}))

const viewportStyle = computed(() => ({
  height: bannerHeight.value + 'px',
}))

const slideStyle = computed(() => ({
  height: bannerHeight.value + 'px',
}))

const current = ref(0)
const videoRef = ref(null)
const videoReady = ref(false)
const isVideoSlide = ref(false)
const transitionName = ref('slide-left')
let timer = null
let paused = false

const currentSlide = computed(() => slides.value[current.value] || {})

function isVideo(url) {
  return url && /\.mp4$/i.test(url)
}

function stopTimer() {
  if (timer) { clearTimeout(timer); timer = null }
}

function startTimer(delay) {
  stopTimer()
  if (paused) return
  const ms = delay || interval.value
  if (ms <= 0) return
  timer = setTimeout(() => go(1), ms)
}

function go(dir) {
  stopTimer()
  transitionName.value = dir > 0 ? 'slide-left' : 'slide-right'
  current.value = (current.value + dir + slides.value.length) % slides.value.length
}

function next() { go(1) }
function prev() { go(-1) }

function goTo(i) {
  if (i === current.value) return
  stopTimer()
  transitionName.value = i > current.value ? 'slide-left' : 'slide-right'
  current.value = i
}

function pause() {
  paused = true
  stopTimer()
  if (videoRef.value && isVideoSlide.value) {
    videoRef.value.pause()
  }
}

function resume() {
  paused = false
  if (isVideoSlide.value && videoRef.value && videoReady.value) {
    videoRef.value.play().catch(() => {})
  } else if (!isVideoSlide.value) {
    startTimer()
  }
}

function onVideoReady() {
  videoReady.value = true
  if (videoRef.value && !paused) {
    videoRef.value.play().catch(() => {})
  }
}

function onVideoEnd() {
  if (!paused) {
    go(1)
  }
}

watch(current, async () => {
  await nextTick()
  stopTimer()
  videoReady.value = false
  isVideoSlide.value = isVideo(currentSlide.value.image)

  if (!isVideoSlide.value) {
    if (!paused) startTimer()
  }
})

onMounted(() => {
  isVideoSlide.value = isVideo(currentSlide.value.image)
  if (!isVideoSlide.value) {
    startTimer()
  }
})

onBeforeUnmount(() => stopTimer())
</script>

<style scoped>
.hero-wrapper {
  position: relative;
  background: #1a1a1a;
}
.hero-viewport {
  position: relative;
  overflow: hidden;
  width: 100%;
  background: #1a1a1a;
}
.hero-slide {
  position: relative;
  width: 100%;
  overflow: hidden;
  background: #1a1a1a;
}
.slide-media-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  cursor: pointer;
}
.slide-media {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

/* Caption */
.hero-caption {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 60px 40px 30px;
  background: linear-gradient(transparent, rgba(0,0,0,0.45));
  pointer-events: none;
}
.hero-caption h2 {
  color: #fff;
  font-size: 32px;
  font-weight: 700;
  margin: 0;
  text-shadow: 0 2px 8px rgba(0,0,0,0.3);
  letter-spacing: 1px;
}

/* Arrows */
.hero-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: none;
  background: rgba(255,255,255,0.85);
  color: #333;
  font-size: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.3s, background 0.2s, box-shadow 0.2s;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0,0,0,0.12);
  line-height: 1;
  padding: 0;
}
.hero-arrow:hover {
  background: #fff;
  box-shadow: 0 4px 16px rgba(0,0,0,0.18);
}
.hero-arrow.left { left: 16px; }
.hero-arrow.right { right: 16px; }
.hero-arrow span { display: block; margin-top: -2px; }
.hero-viewport:hover .hero-arrow { opacity: 1; }

/* Dots */
.hero-dots {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 10px;
  z-index: 10;
}
.hero-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 2px solid rgba(255,255,255,0.7);
  background: transparent;
  cursor: pointer;
  padding: 0;
  transition: all 0.3s;
}
.hero-dot.active {
  background: #fff;
  border-color: #fff;
  box-shadow: 0 0 6px rgba(0,0,0,0.2);
}
.hero-dot:hover { border-color: #fff; }

/* Slide-left: current exits left, next enters from right */
.slide-left-enter-active,
.slide-left-leave-active {
  transition: transform 0.45s ease;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
}
.slide-left-enter-from {
  transform: translateX(100%);
}
.slide-left-leave-to {
  transform: translateX(-100%);
}
.slide-left-enter-to,
.slide-left-leave-from {
  transform: translateX(0);
}

/* Slide-right: current exits right, next enters from left */
.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform 0.45s ease;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
}
.slide-right-enter-from {
  transform: translateX(-100%);
}
.slide-right-leave-to {
  transform: translateX(100%);
}
.slide-right-enter-to,
.slide-right-leave-from {
  transform: translateX(0);
}

@media (max-width: 768px) {
  .hero-viewport { height: 220px !important; }
  .hero-slide { height: 220px !important; }
  .hero-arrow { width: 36px; height: 36px; font-size: 22px; }
  .hero-arrow.left { left: 8px; }
  .hero-arrow.right { right: 8px; }
  .hero-caption h2 { font-size: 18px; }
}
</style>
