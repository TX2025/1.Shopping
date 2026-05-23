<template>
  <footer class="footer">
    <div class="footer-top">
      <div class="footer-inner">
        <div v-for="(col, ci) in columns" :key="ci" class="footer-col">
          <h4>{{ col.title }}</h4>
          <a v-for="(link, li) in col.links" :key="li" :href="link.url || '#'">{{ link.label }}</a>
        </div>
        <div class="footer-col footer-subscribe">
          <h4>订阅资讯</h4>
          <p class="subscribe-text">{{ subscribeText }}</p>
          <div class="subscribe-form">
            <el-input v-model="subscribeEmail" placeholder="请输入邮箱" size="small" />
            <el-button type="primary" size="small" @click="doSubscribe">订阅</el-button>
          </div>
        </div>
      </div>
    </div>
    <div class="footer-mid">
      <div class="footer-inner">
        <p class="footer-about" v-if="aboutText">{{ aboutText }}</p>
        <div class="footer-icons" v-if="socialList.length">
          <a v-for="(s, i) in socialList" :key="i" :href="s.url || '#'" class="social-link" :title="s.platform">
            <el-icon :size="20"><component :is="s.icon" /></el-icon>
          </a>
        </div>
      </div>
    </div>
    <div class="footer-bottom">
      <div class="footer-inner">
        <div class="payment-icons" v-if="paymentList.length">
          <span v-for="(p, i) in paymentList" :key="i" class="payment-item">
            <el-icon :size="22"><component :is="p.icon" /></el-icon>{{ p.name }}
          </span>
        </div>
        <p class="copyright">&copy; {{ year }} {{ siteName }}. All Rights Reserved.</p>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useSiteConfig } from '../composables/useSiteConfig'

const { config } = useSiteConfig()
const year = new Date().getFullYear()
const subscribeEmail = ref('')

const siteName = computed(() => config.value?.site_name || 'MyShop')
const aboutText = computed(() => config.value?.footer_about || '')
const subscribeText = computed(() => config.value?.footer_subscribe_text || '订阅我们的资讯')

const columns = computed(() => {
  try {
    const raw = config.value?.footer_columns
    if (!raw) return []
    return typeof raw === 'string' ? JSON.parse(raw) : raw
  } catch { return [] }
})

const socialList = computed(() => {
  try {
    const raw = config.value?.footer_social
    if (!raw) return []
    return typeof raw === 'string' ? JSON.parse(raw) : raw
  } catch { return [] }
})

const paymentList = computed(() => {
  try {
    const raw = config.value?.footer_payment_icons
    if (!raw) return []
    return typeof raw === 'string' ? JSON.parse(raw) : raw
  } catch { return [] }
})

function doSubscribe() {
  if (!subscribeEmail.value) {
    ElMessage.warning('请输入邮箱地址')
    return
  }
  ElMessage.success('订阅成功！感谢您的关注')
  subscribeEmail.value = ''
}
</script>

<style scoped>
.footer {
  background: #2c3e50;
  color: #b0bec5;
  margin-top: 60px;
}
.footer-top {
  padding: 48px 0 32px;
}
.footer-mid {
  padding: 20px 0;
  border-top: 1px solid rgba(255,255,255,0.08);
}
.footer-bottom {
  padding: 20px 0;
  border-top: 1px solid rgba(255,255,255,0.08);
}
.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 30px;
}
.footer-col {
  flex: 1;
  min-width: 150px;
}
.footer-col h4 {
  color: #fff;
  font-size: 15px;
  margin: 0 0 14px;
}
.footer-col a {
  display: block;
  color: #90a4ae;
  text-decoration: none;
  font-size: 13px;
  line-height: 2.2;
  transition: color 0.2s;
}
.footer-col a:hover {
  color: #fff;
}
.footer-subscribe {
  flex: 1.5;
  min-width: 260px;
}
.subscribe-text {
  font-size: 13px;
  color: #90a4ae;
  margin: 0 0 12px;
}
.subscribe-form {
  display: flex;
  gap: 8px;
}
.footer-about {
  font-size: 13px;
  color: #78909c;
  max-width: 500px;
  margin: 0;
  line-height: 1.8;
}
.footer-icons {
  display: flex;
  gap: 16px;
  align-items: center;
}
.social-link {
  color: #90a4ae;
  transition: color 0.2s;
}
.social-link:hover {
  color: #fff;
}
.payment-icons {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}
.payment-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #78909c;
}
.copyright {
  font-size: 12px;
  color: #607d8b;
  margin: 12px 0 0;
  width: 100%;
}
@media (max-width: 768px) {
  .footer-col {
    flex: 0 0 45%;
    min-width: 0;
  }
}
</style>
