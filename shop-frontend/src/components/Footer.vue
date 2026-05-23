<template>
  <footer class="footer">
    <div class="footer-top">
      <div class="footer-inner">
        <div class="footer-brand-col">
          <h3 class="footer-logo">{{ siteName }}</h3>
          <p class="footer-about" v-if="aboutText">{{ aboutText }}</p>
          <div class="footer-social" v-if="socialList.length">
            <a v-for="(s, i) in socialList" :key="i" :href="s.url || '#'" class="social-link" :title="s.platform">
              <el-icon :size="18"><component :is="s.icon" /></el-icon>
            </a>
          </div>
        </div>
        <div v-for="(col, ci) in columns" :key="ci" class="footer-col">
          <h4>{{ col.title }}</h4>
          <a v-for="(link, li) in col.links" :key="li" :href="link.url || '#'">{{ link.label }}</a>
        </div>
        <div class="footer-col footer-subscribe">
          <h4>Newsletter</h4>
          <p class="subscribe-text">{{ subscribeText }}</p>
          <div class="subscribe-form">
            <input v-model="subscribeEmail" placeholder="Your email address" class="subscribe-input" @keyup.enter="doSubscribe" />
            <button class="subscribe-btn" @click="doSubscribe">Subscribe</button>
          </div>
        </div>
      </div>
    </div>
    <div class="footer-bottom">
      <div class="footer-inner">
        <div class="payment-icons" v-if="paymentList.length">
          <span v-for="(p, i) in paymentList" :key="i" class="payment-item">
            <el-icon :size="20"><component :is="p.icon" /></el-icon>{{ p.name }}
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
  background: #1a1a2e;
  color: #b0bec5;
  margin-top: 60px;
}
.footer-top {
  padding: 56px 0 40px;
}
.footer-bottom {
  padding: 24px 0;
  border-top: 1px solid rgba(255,255,255,0.06);
  background: #16162a;
}
.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 32px;
  justify-content: space-between;
}
.footer-brand-col {
  flex: 0 0 260px;
  min-width: 200px;
}
.footer-logo {
  color: #fff;
  font-size: 22px;
  margin: 0 0 14px;
  font-weight: 700;
}
.footer-about {
  font-size: 13px;
  color: #90a4ae;
  line-height: 1.8;
  margin: 0 0 20px;
}
.footer-social {
  display: flex;
  gap: 14px;
}
.social-link {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: rgba(255,255,255,0.08);
  color: #90a4ae;
  transition: all 0.2s;
}
.social-link:hover {
  background: #00676b;
  color: #fff;
}
.footer-col {
  flex: 0 0 auto;
  min-width: 120px;
}
.footer-col h4 {
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 16px;
}
.footer-col a {
  display: block;
  color: #90a4ae;
  text-decoration: none;
  font-size: 13px;
  line-height: 2.4;
  transition: color 0.2s;
}
.footer-col a:hover {
  color: #00676b;
}
.footer-subscribe {
  flex: 0 0 280px;
  min-width: 240px;
}
.subscribe-text {
  font-size: 13px;
  color: #90a4ae;
  margin: 0 0 14px;
  line-height: 1.6;
}
.subscribe-form {
  display: flex;
  gap: 0;
}
.subscribe-input {
  flex: 1;
  padding: 10px 14px;
  background: rgba(255,255,255,0.06);
  border: 1px solid rgba(255,255,255,0.1);
  border-right: none;
  border-radius: 6px 0 0 6px;
  color: #fff;
  font-size: 13px;
  outline: none;
  transition: border-color 0.2s;
}
.subscribe-input::placeholder {
  color: #78909c;
}
.subscribe-input:focus {
  border-color: #00676b;
}
.subscribe-btn {
  padding: 10px 20px;
  background: #00676b;
  color: #fff;
  border: none;
  border-radius: 0 6px 6px 0;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
}
.subscribe-btn:hover {
  background: #0a8f94;
}

.payment-icons {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  align-items: center;
}
.payment-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #78909c;
}
.copyright {
  font-size: 12px;
  color: #607d8b;
  margin: 14px 0 0;
  width: 100%;
}

@media (max-width: 768px) {
  .footer-inner {
    gap: 24px;
  }
  .footer-brand-col {
    flex: 0 0 100%;
  }
  .footer-col {
    flex: 0 0 44%;
    min-width: 0;
  }
  .footer-subscribe {
    flex: 0 0 100%;
  }
  .subscribe-form {
    max-width: 100%;
  }
}
</style>
