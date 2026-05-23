<template>
  <div class="thank-you-page">
    <div class="container">
      <el-result icon="success" :title="pageConfig.message" :sub-title="`${pageConfig.subMessage} 订单号: ${orderNo}`">
        <template #extra>
          <el-button type="primary" @click="$router.push(pageConfig.buttonLink || '/products')">{{ pageConfig.buttonText }}</el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPageConfig } from '../../api/admin'

const route = useRoute()
const orderNo = ref(route.query.orderNo || '')
const pageConfig = ref({ message: '感谢您的购买！', subMessage: '我们会尽快为您发货', buttonText: '继续购物', buttonLink: '/products', autoRedirectSeconds: 10 })

onMounted(async () => {
  try {
    const res = await getPageConfig('THANK_YOU')
    if (res.data?.configJson) {
      pageConfig.value = { ...pageConfig.value, ...JSON.parse(res.data.configJson) }
      if (pageConfig.value.autoRedirectSeconds > 0) {
        setTimeout(() => {
          window.location.href = pageConfig.value.buttonLink
        }, pageConfig.value.autoRedirectSeconds * 1000)
      }
    }
  } catch {}
})
</script>

<style scoped>
.container { max-width: 600px; margin: 80px auto; }
</style>
