<template>
  <div class="page-settings">
    <h2>页面布局设置</h2>
    <el-tabs v-model="activeTab" style="margin-top:16px">
      <el-tab-pane v-for="page in pages" :key="page.type" :label="page.label" :name="page.type">
        <el-card>
          <div class="editor-header">
            <span>编辑 {{ page.label }} 的 JSON 配置</span>
            <el-button size="small" @click="resetPage(page)">恢复默认</el-button>
          </div>
          <el-input v-model="editingJson" type="textarea" :rows="15" style="margin-top:12px;font-family:monospace" />
          <el-button type="primary" @click="savePage(page.type)" :loading="saving" style="margin-top:12px">保存</el-button>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getPageConfigs, updatePageConfig } from '../../api/admin'
import { ElMessage } from 'element-plus'

const defaults = {
  HOME: '{"banners":[],"sections":[{"type":"category_showcase","title":"热门分类","categoryIds":[]},{"type":"product_grid","title":"推荐商品","displayCount":8,"sortBy":"sales"}]}',
  PRODUCT_LIST: '{"displayMode":"grid","pageSize":20,"showFilters":["category","price","keyword"],"defaultSort":"newest","sidebarPosition":"left"}',
  PRODUCT_DETAIL: '{"showRelatedProducts":true,"relatedCount":4,"imageDisplayMode":"thumbnail","showSalesCount":true}',
  CART: '{"showRelatedProducts":true,"promotionText":"满99元免运费","showQuantityDiscount":false}',
  PAYMENT: '{"paymentMethods":["alipay","wechat"],"instructionText":"请在15分钟内完成支付","showOrderSummary":true}',
  THANK_YOU: '{"message":"感谢您的购买！","subMessage":"我们会尽快为您发货","buttonText":"继续购物","buttonLink":"/products","autoRedirectSeconds":10}',
}

const pages = [
  { type: 'HOME', label: '首页' }, { type: 'PRODUCT_LIST', label: '产品页' },
  { type: 'PRODUCT_DETAIL', label: '产品详情页' }, { type: 'CART', label: '购物车页' },
  { type: 'PAYMENT', label: '支付页' }, { type: 'THANK_YOU', label: '感谢页' },
]

const activeTab = ref('HOME')
const editingJson = ref('')
const saving = ref(false)
const currentConfigs = ref({})

onMounted(async () => {
  try {
    const res = await getPageConfigs()
    if (res.data) {
      res.data.forEach(p => { currentConfigs.value[p.pageType] = p.configJson })
    }
  } catch {}
  editingJson.value = currentConfigs.value.HOME || defaults.HOME
})

watch(activeTab, (tab) => {
  editingJson.value = currentConfigs.value[tab] || defaults[tab]
})

function resetPage(page) {
  editingJson.value = defaults[page.type]
}

async function savePage(pageType) {
  saving.value = true
  try {
    await updatePageConfig(pageType, { configJson: editingJson.value })
    currentConfigs.value[pageType] = editingJson.value
    ElMessage.success('保存成功')
  } catch {} finally { saving.value = false }
}
</script>

<style scoped>
.editor-header { display: flex; justify-content: space-between; align-items: center; }
</style>
