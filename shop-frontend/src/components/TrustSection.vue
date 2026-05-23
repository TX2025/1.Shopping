<template>
  <div class="trust-section" v-if="features.length">
    <div class="trust-container">
      <h2 class="trust-title">{{ trustTitle }}</h2>
      <div class="trust-grid">
        <div class="trust-item" v-for="(item, i) in features" :key="i">
          <el-icon :size="36" color="#00676b"><component :is="item.icon" /></el-icon>
          <h3>{{ item.title }}</h3>
          <p>{{ item.description }}</p>
          <div class="trust-video-btn" v-if="item.videoUrl" @click.stop="openVideo(item)">
            <el-icon :size="18"><VideoPlay /></el-icon>
            <span>Watch Video</span>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="videoVisible" :title="currentVideoTitle" width="760px" destroy-on-close center
      @close="stopVideo">
      <div class="video-wrapper" v-if="videoVisible">
        <template v-if="isYoutube">
          <iframe :src="youtubeEmbed" frameborder="0" allowfullscreen
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            class="video-iframe"></iframe>
        </template>
        <template v-else>
          <video ref="videoRef" :src="currentVideoUrl" controls autoplay class="video-player"
            controlsList="nodownload"></video>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useSiteConfig } from '../composables/useSiteConfig'
import { VideoPlay } from '@element-plus/icons-vue'

const { config } = useSiteConfig()

const trustTitle = computed(() => config.value?.trust_title || '为什么选择我们')

const features = computed(() => {
  try {
    const raw = config.value?.trust_features
    if (!raw) return []
    return typeof raw === 'string' ? JSON.parse(raw) : raw
  } catch { return [] }
})

const videoVisible = ref(false)
const currentVideoUrl = ref('')
const currentVideoTitle = ref('')
const videoRef = ref(null)

const isYoutube = computed(() => {
  return /youtube\.com|youtu\.be/i.test(currentVideoUrl.value)
})

const youtubeEmbed = computed(() => {
  const url = currentVideoUrl.value
  const match = url.match(/(?:v=|\/)([a-zA-Z0-9_-]{11})/)
  return match ? `https://www.youtube.com/embed/${match[1]}?autoplay=1` : ''
})

function openVideo(item) {
  currentVideoUrl.value = item.videoUrl
  currentVideoTitle.value = item.title || ''
  videoVisible.value = true
}

function stopVideo() {
  if (videoRef.value) {
    videoRef.value.pause()
    videoRef.value.currentTime = 0
  }
  currentVideoUrl.value = ''
}
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
  position: relative;
}
.trust-title::after {
  content: '';
  display: block;
  width: 50px;
  height: 3px;
  background: #00676b;
  margin: 12px auto 0;
  border-radius: 2px;
}
.trust-grid {
  display: flex;
  justify-content: center;
  gap: 32px;
  flex-wrap: wrap;
}
.trust-item {
  flex: 0 0 240px;
  text-align: center;
  padding: 32px 20px 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.06);
  transition: transform 0.3s, box-shadow 0.3s;
  position: relative;
}
.trust-item:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 28px rgba(0,0,0,0.1);
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
  margin: 0 0 16px;
  line-height: 1.6;
}
.trust-video-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  background: #f0f7f7;
  color: #00676b;
  border-radius: 20px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s;
  user-select: none;
}
.trust-video-btn:hover {
  background: #00676b;
  color: #fff;
}

.video-wrapper {
  aspect-ratio: 16/9;
  background: #000;
  border-radius: 6px;
  overflow: hidden;
}
.video-iframe,
.video-player {
  width: 100%;
  height: 100%;
  display: block;
}

@media (max-width: 768px) {
  .trust-grid {
    gap: 16px;
  }
  .trust-item {
    flex: 0 0 44%;
    padding: 24px 12px 20px;
  }
}
</style>
