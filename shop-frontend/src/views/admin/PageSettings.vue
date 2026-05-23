<template>
  <div class="page-settings">
    <h2>页面布局设置</h2>
    <el-tabs v-model="activeTab" style="margin-top:16px" @tab-change="switchTab">
      <el-tab-pane v-for="page in pages" :key="page.type" :label="page.label" :name="page.type">
        <el-card v-loading="loading">
          <template v-if="currentSchema && Object.keys(currentSchema).length">
            <div class="schema-form">
              <el-form-item v-for="(field, key) in currentSchema" :key="key" :label="field.label">
                <template v-if="field.type === 'boolean'">
                  <el-switch v-model="formData[key]" />
                </template>
                <template v-else-if="field.type === 'number'">
                  <el-input-number v-model="formData[key]" :min="1" style="width:200px" />
                </template>
                <template v-else-if="field.type.startsWith('select:')">
                  <el-select v-model="formData[key]" style="width:200px">
                    <el-option v-for="opt in field.type.substring(7).split(',')" :key="opt" :label="opt" :value="opt" />
                  </el-select>
                </template>
                <template v-else>
                  <el-input v-model="formData[key]" />
                </template>
              </el-form-item>
            </div>
            <div style="margin-top:12px">
              <el-button size="small" @click="toggleRaw">切换JSON编辑</el-button>
            </div>
          </template>

          <template v-if="showRaw || !Object.keys(currentSchema).length">
            <div class="editor-header">
              <span>编辑 {{ pages.find(p => p.type === activeTab)?.label }} 的 JSON 配置</span>
              <el-button size="small" @click="resetPage">恢复默认</el-button>
            </div>
            <el-input v-model="editingJson" type="textarea" :rows="12" style="margin-top:12px;font-family:monospace" />
          </template>

          <el-button type="primary" @click="savePage" :loading="saving" style="margin-top:12px">保存</el-button>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, reactive } from 'vue'
import { getPageConfigs, getPageConfigSchema, updatePageConfig } from '../../api/admin'
import { ElMessage } from 'element-plus'

const defaults = {
  HOME: '{"hero_enabled":true,"showcase_enabled":true,"showcase_categories":[],"popular_enabled":true,"trust_enabled":true,"banners":[],"sections":[{"type":"category_showcase","title":"热门分类","categoryIds":[]},{"type":"product_grid","title":"推荐商品","displayCount":8,"sortBy":"sales"}]}',
  PRODUCT_LIST: '{"displayMode":"grid","pageSize":20,"showSidebar":true,"showSalesCount":true,"showOriginalPrice":true,"showFilters":["category","price","keyword"],"defaultSort":"newest","sidebarPosition":"left"}',
  PRODUCT_DETAIL: '{"showBreadcrumb":true,"showRelatedProducts":true,"relatedCount":4,"imageDisplayMode":"thumbnail","showSalesCount":true,"showShareButtons":true}',
  CART: '{"promotionText":"满99元免运费","showCouponInput":true,"showRelatedProducts":true,"showShippingEstimate":false}',
  PAYMENT: '{"instructionText":"请在15分钟内完成支付","showOrderSummary":true}',
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
const loading = ref(false)
const showRaw = ref(false)
const currentConfigs = ref({})
const currentSchema = ref({})
const formData = reactive({})

onMounted(async () => {
  try {
    const res = await getPageConfigs()
    if (res.data) {
      Object.entries(res.data).forEach(([type, pc]) => {
        currentConfigs.value[type] = pc?.configJson || defaults[type]
      })
    }
  } catch {}
  initTab('HOME')
})

function initTab(type) {
  const json = currentConfigs.value[type] || defaults[type]
  editingJson.value = json
  try {
    const parsed = JSON.parse(json)
    Object.keys(formData).forEach(k => delete formData[k])
    Object.assign(formData, parsed)
  } catch {}

  // Try load schema
  getPageConfigSchema(type).then(res => {
    currentSchema.value = res.data || {}
    // Apply defaults for missing form fields
    Object.entries(currentSchema.value).forEach(([key, field]) => {
      if (formData[key] === undefined) {
        formData[key] = field.default
      }
    })
  }).catch(() => {
    currentSchema.value = {}
    showRaw.value = true
  })
}

function switchTab(type) {
  showRaw.value = false
  initTab(type)
}

function toggleRaw() {
  if (!showRaw.value) {
    editingJson.value = JSON.stringify(formData, null, 2)
  } else {
    try {
      const parsed = JSON.parse(editingJson.value)
      Object.keys(formData).forEach(k => delete formData[k])
      Object.assign(formData, parsed)
    } catch { ElMessage.warning('JSON格式不合法') }
  }
  showRaw.value = !showRaw.value
}

function resetPage() {
  editingJson.value = defaults[activeTab.value]
}

async function savePage() {
  saving.value = true
  try {
    const json = showRaw.value ? editingJson.value : JSON.stringify(formData)
    await updatePageConfig(activeTab.value, { configJson: json })
    currentConfigs.value[activeTab.value] = json
    ElMessage.success('保存成功')
  } catch {} finally { saving.value = false }
}

// Keep editingJson in sync when formData changes
watch(formData, () => {
  if (!showRaw.value) {
    editingJson.value = JSON.stringify(formData)
  }
}, { deep: true })
</script>

<style scoped>
.schema-form {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 20px;
}
.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
@media (max-width: 768px) {
  .schema-form { grid-template-columns: 1fr; }
}
</style>
