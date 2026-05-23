<template>
  <el-footer class="footer">
    <div class="footer-inner">
      <div class="footer-section">
        <h4>{{ siteName }}</h4>
        <p>为您提供优质的购物体验</p>
      </div>
      <div class="footer-section">
        <h4>联系方式</h4>
        <p><el-icon><Phone /></el-icon> {{ contact.phone }}</p>
        <p><el-icon><Message /></el-icon> {{ contact.email }}</p>
        <p><el-icon><Clock /></el-icon> {{ contact.hours }}</p>
      </div>
      <div class="footer-section">
        <h4>购物指南</h4>
        <p>如何下单</p>
        <p>支付方式</p>
        <p>配送说明</p>
      </div>
    </div>
    <div class="footer-bottom">
      <p>&copy; {{ year }} {{ siteName }}. All rights reserved.</p>
    </div>
  </el-footer>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSiteConfig } from '../api/admin'

const siteName = ref('MyShop')
const year = new Date().getFullYear()
const contact = ref({ phone: '400-123-4567', email: 'support@shop.com', hours: '周一至周五 9:00-18:00' })

onMounted(async () => {
  try {
    const res = await getSiteConfig()
    if (res.data) {
      siteName.value = res.data.site_name || 'MyShop'
      contact.value.phone = res.data.contact_phone || contact.value.phone
      contact.value.email = res.data.contact_email || contact.value.email
      contact.value.hours = res.data.contact_hours || contact.value.hours
    }
  } catch {}
})
</script>

<style scoped>
.footer { background: #303133; color: #bfc2cc; margin-top: 60px; padding: 40px 0 20px; }
.footer-inner { max-width: 1200px; margin: 0 auto; display: flex; justify-content: space-between; padding: 0 20px; }
.footer-section { flex: 1; }
.footer-section h4 { color: #fff; margin-bottom: 12px; }
.footer-section p { line-height: 2; font-size: 14px; display: flex; align-items: center; gap: 6px; }
.footer-bottom { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #4a4a4a; font-size: 13px; }
</style>
