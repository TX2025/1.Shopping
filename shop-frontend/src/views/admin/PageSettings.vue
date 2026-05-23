<template>
  <div class="page-settings">
    <h2>页面布局设置</h2>
    <el-tabs v-model="activeTab" style="margin-top:16px" @tab-change="switchTab">
      <el-tab-pane v-for="page in pages" :key="page.type" :label="page.label" :name="page.type">
        <el-card v-loading="loading">

          <!-- HOME: hero editor -->
          <template v-if="activeTab === 'HOME'">
            <div class="schema-form">
              <el-form-item v-for="(field, key) in currentSchema" :key="key" :label="field.label" v-show="key !== 'hero_enabled' && key !== 'showcase_enabled' && key !== 'popular_enabled'">
                <template v-if="field.type === 'boolean'">
                  <el-switch v-model="formData[key]" />
                </template>
                <template v-else>
                  <el-input v-model="formData[key]" />
                </template>
              </el-form-item>
            </div>

            <el-divider content-position="left">首页轮播设置</el-divider>

            <el-form label-width="120px" size="small">
              <el-form-item label="启用轮播">
                <el-switch v-model="formData.hero_enabled" />
              </el-form-item>
              <el-form-item label="切换间隔(ms)">
                <el-input-number v-model="heroInterval" :min="1000" :step="500" :max="30000" />
                <span style="margin-left:8px;color:#999;font-size:12px">图片/GIF 切换间隔；视频播放完毕后才切换</span>
              </el-form-item>
              <el-form-item label="宽度">
                <el-input v-model="formData.hero_width" placeholder="如 100% 或 1200" style="width:200px" />
                <span style="margin-left:8px;color:#999;font-size:12px">支持 % 或 px</span>
              </el-form-item>
              <el-form-item label="高度(px)">
                <el-input-number v-model="heroHeight" :min="200" :max="900" :step="20" />
              </el-form-item>
            </el-form>

            <el-divider content-position="left">幻灯片</el-divider>

            <div class="hero-slides-editor">
              <div class="slide-card" v-for="(slide, idx) in heroSlides" :key="idx">
                <div class="slide-header">
                  <span class="slide-label">#{{ idx + 1 }}</span>
                  <div class="slide-actions">
                    <el-button size="small" :disabled="idx===0" @click="moveSlide(idx,-1)" :icon="ArrowUp" circle />
                    <el-button size="small" :disabled="idx===heroSlides.length-1" @click="moveSlide(idx,1)" :icon="ArrowDown" circle />
                    <el-button size="small" type="danger" @click="removeSlide(idx)" :icon="Delete" circle />
                  </div>
                </div>
                <el-row :gutter="16">
                  <el-col :span="6">
                    <div class="slide-preview" @click="triggerUpload(idx)">
                      <video v-if="isVideo(slide.image)" :src="slide.image" muted class="slide-preview-media" />
                      <img v-else-if="slide.image" :src="slide.image" class="slide-preview-media" />
                      <div v-else class="slide-preview-empty"><el-icon :size="28"><Plus /></el-icon><span>上传媒体</span></div>
                      <div class="slide-preview-type" v-if="slide.image">
                        <el-tag size="small" :type="isVideo(slide.image)?'warning':''">{{ isVideo(slide.image)?'视频':isGif(slide.image)?'GIF':'图片' }}</el-tag>
                      </div>
                    </div>
                    <input type="file" :ref="el => { if (el) uploadRefs[idx] = el }" accept="image/jpeg,image/png,image/gif,video/mp4" style="display:none" @change="e => handleUpload(e, idx)" />
                    <el-button size="small" style="width:100%;margin-top:6px" @click="triggerUpload(idx)">选择文件</el-button>
                  </el-col>
                  <el-col :span="18">
                    <el-form label-width="70px" size="small">
                      <el-form-item label="媒体URL"><el-input v-model="slide.image" placeholder="图片/GIF/视频 URL" /></el-form-item>
                      <el-form-item label="标题"><el-input v-model="slide.title" placeholder="叠加标题" /></el-form-item>
                      <el-form-item label="链接"><el-input v-model="slide.link" placeholder="如 /products" /></el-form-item>
                    </el-form>
                  </el-col>
                </el-row>
              </div>
              <el-button style="margin-top:12px;width:100%" @click="addSlide" :icon="Plus" dashed>添加幻灯片</el-button>
            </div>
          </template>

          <!-- POPULAR: showcase & popular products editor (uses HOME config) -->
          <template v-else-if="activeTab === 'POPULAR'">
            <el-form label-width="120px" size="small" style="margin-bottom:8px">
              <el-form-item label="启用热门分类">
                <el-switch v-model="formData.showcase_enabled" />
              </el-form-item>
              <el-form-item label="启用热门推荐">
                <el-switch v-model="formData.popular_enabled" />
              </el-form-item>
            </el-form>

            <el-divider content-position="left">热门分类设置</el-divider>
            <div class="items-editor">
              <div class="slide-card" v-for="(item, idx) in showcaseItems" :key="idx">
                <div class="slide-header">
                  <span class="slide-label">#{{ idx + 1 }}</span>
                  <div class="slide-actions">
                    <el-button size="small" :disabled="idx===0" @click="moveShowcaseItem(idx,-1)" :icon="ArrowUp" circle />
                    <el-button size="small" :disabled="idx===showcaseItems.length-1" @click="moveShowcaseItem(idx,1)" :icon="ArrowDown" circle />
                    <el-button size="small" type="danger" @click="removeShowcaseItem(idx)" :icon="Delete" circle />
                  </div>
                </div>
                <el-row :gutter="16">
                  <el-col :span="6">
                    <div class="slide-preview" @click="triggerShowcaseUpload(idx)">
                      <video v-if="isVideo(showcasePreview(idx))" :src="showcasePreview(idx)" muted class="slide-preview-media" />
                      <img v-else-if="showcasePreview(idx)" :src="showcasePreview(idx)" class="slide-preview-media" />
                      <div v-else class="slide-preview-empty"><el-icon :size="28"><Plus /></el-icon><span>上传封面</span></div>
                    </div>
                    <input type="file" :ref="el => { if (el) scUploadRefs[idx] = el }" accept="image/jpeg,image/png,image/gif,video/mp4" style="display:none" @change="e => handleShowcaseUpload(e, idx)" />
                    <el-button size="small" style="width:100%;margin-top:6px" @click="triggerShowcaseUpload(idx)">上传封面</el-button>
                  </el-col>
                  <el-col :span="18">
                    <el-form label-width="70px" size="small">
                      <el-form-item label="分类">
                        <el-select v-model="item.categoryId" placeholder="选择分类" filterable style="width:100%" @change="onShowcaseCategoryChange(idx)">
                          <el-option v-for="c in allCategoryOptions" :key="c.id" :label="c.name" :value="c.id" />
                        </el-select>
                      </el-form-item>
                      <el-form-item label="封面URL">
                        <el-input v-model="item.coverImage" placeholder="留空则使用该分类首款产品封面" />
                      </el-form-item>
                      <el-form-item label="视频">
                        <div style="display:flex;gap:8px">
                          <el-input v-model="scVideoUrls[idx]" placeholder="输入视频URL" size="small" />
                          <el-button size="small" @click="addShowcaseVideo(idx)">添加</el-button>
                        </div>
                        <div style="margin-top:4px">
                          <el-tag v-for="(v, vi) in showcaseVideoList(idx)" :key="vi" closable @close="removeShowcaseVideo(idx, vi)" style="margin:2px" size="small">{{ v }}</el-tag>
                        </div>
                      </el-form-item>
                    </el-form>
                  </el-col>
                </el-row>
              </div>
              <el-button style="margin-top:12px;width:100%" @click="addShowcaseItem" :icon="Plus" dashed>添加分类</el-button>
            </div>

            <el-divider content-position="left">热门推荐设置</el-divider>
            <div class="items-editor">
              <div class="slide-card" v-for="(item, idx) in popularItems" :key="idx">
                <div class="slide-header">
                  <span class="slide-label">#{{ idx + 1 }}</span>
                  <div class="slide-actions">
                    <el-button size="small" :disabled="idx===0" @click="movePopularItem(idx,-1)" :icon="ArrowUp" circle />
                    <el-button size="small" :disabled="idx===popularItems.length-1" @click="movePopularItem(idx,1)" :icon="ArrowDown" circle />
                    <el-button size="small" type="danger" @click="removePopularItem(idx)" :icon="Delete" circle />
                  </div>
                </div>
                <el-form label-width="70px" size="small">
                  <el-form-item label="商品">
                    <el-select v-model="item.productId" placeholder="选择商品" filterable style="width:100%">
                      <el-option v-for="p in allProductOptions" :key="p.id" :label="`#${p.id} ${p.name} (¥${p.price})`" :value="p.id" />
                    </el-select>
                  </el-form-item>
                </el-form>
              </div>
              <el-button style="margin-top:12px;width:100%" @click="addPopularItem" :icon="Plus" dashed>添加商品</el-button>
            </div>
          </template>

          <!-- FOOTER: footer editor -->
          <template v-else-if="activeTab === 'FOOTER'">
            <el-form label-width="140px" size="small">
              <el-form-item label="关于文字">
                <el-input v-model="formData.footer_about" type="textarea" :rows="2" />
              </el-form-item>
              <el-form-item label="订阅引导语">
                <el-input v-model="formData.footer_subscribe_text" />
              </el-form-item>
              <el-form-item label="栏目列 (JSON)">
                <el-input v-model="footerColumnsJson" type="textarea" :rows="8" placeholder='[{"title":"标题","links":[{"label":"名称","url":"#"}]}]' />
              </el-form-item>
              <el-form-item label="社交图标 (JSON)">
                <el-input v-model="footerSocialJson" type="textarea" :rows="4" placeholder='[{"platform":"微信","url":"#","icon":"ChatDotSquare"}]' />
              </el-form-item>
              <el-form-item label="支付图标 (JSON)">
                <el-input v-model="footerPaymentJson" type="textarea" :rows="4" placeholder='[{"name":"微信支付","icon":"Wallet"}]' />
              </el-form-item>
            </el-form>
          </template>

          <!-- Generic: schema form + JSON toggle -->
          <template v-else>
            <div class="schema-form" v-if="currentSchema && Object.keys(currentSchema).length">
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

          <!-- JSON editor (all tabs) -->
          <template v-if="showRaw">
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
import { ref, onMounted, watch, reactive, computed } from 'vue'
import { getPageConfigs, getPageConfigSchema, updatePageConfig, uploadFile, getAdminCategories, getAdminProducts } from '../../api/admin'
import { ElMessage } from 'element-plus'
import { Plus, ArrowUp, ArrowDown, Delete } from '@element-plus/icons-vue'

const defaults = {
  HOME: '{"hero_enabled":true,"hero_slides":[{"image":"https://placehold.co/1920x500/00676b/ffffff?text=新品上市","link":"/products","title":"新品上市","sort":1},{"image":"https://placehold.co/1920x500/0a8f94/ffffff?text=限时特惠","link":"/products?sort=sales","title":"限时特惠","sort":2},{"image":"https://placehold.co/1920x500/2c3e50/ffffff?text=品质保证","link":"/products","title":"品质保证","sort":3}],"hero_interval":5000,"hero_width":"100%","hero_height":500,"showcase_enabled":true,"showcase_categories":[],"popular_enabled":true,"popular_products":[],"trust_enabled":true}',
  PRODUCT_LIST: '{"displayMode":"grid","pageSize":20,"showSidebar":true,"showSalesCount":true,"showOriginalPrice":true,"showFilters":["category","price","keyword"],"defaultSort":"newest","sidebarPosition":"left"}',
  PRODUCT_DETAIL: '{"showBreadcrumb":true,"showRelatedProducts":true,"relatedCount":4,"imageDisplayMode":"thumbnail","showSalesCount":true,"showShareButtons":true,"imageSwitchInterval":5}',
  CART: '{"promotionText":"满99元免运费","showCouponInput":true,"showRelatedProducts":true,"showShippingEstimate":false}',
  PAYMENT: '{"instructionText":"请在15分钟内完成支付","showOrderSummary":true}',
  THANK_YOU: '{"message":"感谢您的购买！","subMessage":"我们会尽快为您发货","buttonText":"继续购物","buttonLink":"/products","autoRedirectSeconds":10}',
  FOOTER: '{"footer_about":"MyShop 致力于为您提供优质的购物体验，汇聚全球好物，让生活更美好。","footer_columns":[{"title":"购物指南","links":[{"label":"如何下单","url":"#"},{"label":"支付方式","url":"#"},{"label":"配送说明","url":"#"}]},{"title":"售后服务","links":[{"label":"退换货政策","url":"#"},{"label":"退款流程","url":"#"},{"label":"投诉建议","url":"#"}]},{"title":"关于我们","links":[{"label":"公司介绍","url":"#"},{"label":"联系我们","url":"#"},{"label":"加入我们","url":"#"}]},{"title":"关注我们","links":[{"label":"微博","url":"#"},{"label":"微信公众号","url":"#"},{"label":"小红书","url":"#"}]}],"footer_subscribe_text":"订阅我们的资讯，获取最新优惠信息","footer_social":[{"platform":"微信","url":"#","icon":"ChatDotSquare"},{"platform":"微博","url":"#","icon":"Share"}],"footer_payment_icons":[{"name":"微信支付","icon":"Wallet"},{"name":"支付宝","icon":"Money"},{"name":"银联","icon":"CreditCard"}]}',
}

const pages = [
  { type: 'HOME', label: '首页' }, { type: 'POPULAR', label: '热门推荐' },
  { type: 'PRODUCT_LIST', label: '产品页' }, { type: 'PRODUCT_DETAIL', label: '产品详情页' },
  { type: 'CART', label: '购物车页' }, { type: 'PAYMENT', label: '支付页' },
  { type: 'THANK_YOU', label: '感谢页' }, { type: 'FOOTER', label: 'Footer' },
]

const activeTab = ref('HOME')
const editingJson = ref('')
const saving = ref(false)
const loading = ref(false)
const showRaw = ref(false)
const currentConfigs = ref({})
const currentSchema = ref({})
const formData = reactive({})
const uploadRefs = {}

// Hero-specific
const heroSlides = ref([])
const heroInterval = computed({
  get() { return parseInt(formData.hero_interval) || 5000 },
  set(v) { formData.hero_interval = v }
})
const heroHeight = computed({
  get() { return parseInt(formData.hero_height) || 500 },
  set(v) { formData.hero_height = v }
})

// Footer JSON fields
const footerColumnsJson = ref('')
const footerSocialJson = ref('')
const footerPaymentJson = ref('')

// Showcase categories & popular products
const allCategoryOptions = ref([])
const allProductOptions = ref([])
const showcaseItems = ref([])
const popularItems = ref([])
const scUploadRefs = {}
const scVideoUrls = reactive({})

function showcasePreview(idx) {
  const item = showcaseItems.value[idx]
  if (!item) return null
  if (item.coverImage) return item.coverImage
  const vids = parseShowcaseVideos(item)
  if (vids.length > 0) return vids[0]
  // Default: use first product cover for the selected category
  if (item.categoryId && item._firstProductCover) return item._firstProductCover
  return null
}

function parseShowcaseVideos(item) {
  if (!item || !item.videos) return []
  try { return typeof item.videos === 'string' ? JSON.parse(item.videos) : item.videos } catch { return [] }
}

function showcaseVideoList(idx) {
  const item = showcaseItems.value[idx]
  return item ? parseShowcaseVideos(item) : []
}

function addShowcaseVideo(idx) {
  const url = scVideoUrls[idx]
  if (!url) return
  const item = showcaseItems.value[idx]
  if (!item) return
  const vids = parseShowcaseVideos(item)
  if (!vids.includes(url)) {
    vids.push(url)
    item.videos = JSON.stringify(vids)
    scVideoUrls[idx] = ''
  }
}

function removeShowcaseVideo(idx, vi) {
  const item = showcaseItems.value[idx]
  if (!item) return
  const vids = parseShowcaseVideos(item)
  vids.splice(vi, 1)
  item.videos = vids.length > 0 ? JSON.stringify(vids) : ''
}

function addShowcaseItem() {
  const idx = showcaseItems.value.length
  scVideoUrls[idx] = ''
  showcaseItems.value.push({ categoryId: null, coverImage: '', videos: '' })
}
function removeShowcaseItem(idx) { showcaseItems.value.splice(idx, 1) }
function moveShowcaseItem(idx, delta) {
  const arr = showcaseItems.value
  const t = idx + delta
  if (t < 0 || t >= arr.length) return
  const tmp = arr[idx]; arr[idx] = arr[t]; arr[t] = tmp
  showcaseItems.value = [...arr]
}
function triggerShowcaseUpload(idx) { scUploadRefs[idx]?.click() }

async function handleShowcaseUpload(e, idx) {
  const file = e.target.files?.[0]
  if (!file) return
  try {
    const res = await uploadFile(file)
    if (res.data?.url) showcaseItems.value[idx].coverImage = res.data.url
    ElMessage.success('上传成功')
  } catch { ElMessage.error('上传失败') }
  e.target.value = ''
}

async function onShowcaseCategoryChange(idx) {
  const item = showcaseItems.value[idx]
  if (!item || !item.categoryId) return
  // Fetch first product of this category for default cover preview
  try {
    const { getProducts } = await import('../../api/product')
    const res = await getProducts({ categoryId: item.categoryId, page: 1, size: 1 })
    const list = res.data?.list || []
    if (list.length > 0) {
      item._firstProductCover = list[0].coverImage || null
    }
  } catch {}
}

function addPopularItem() {
  popularItems.value.push({ productId: null })
}
function removePopularItem(idx) { popularItems.value.splice(idx, 1) }
function movePopularItem(idx, delta) {
  const arr = popularItems.value
  const t = idx + delta
  if (t < 0 || t >= arr.length) return
  const tmp = arr[idx]; arr[idx] = arr[t]; arr[t] = tmp
  popularItems.value = [...arr]
}

function isVideo(url) { return url && /\.mp4$/i.test(url) }
function isGif(url) { return url && /\.gif$/i.test(url) }

// Sync hero slides to formData
watch(heroSlides, (val) => {
  formData.hero_slides = val.map((s, i) => ({ ...s, sort: i + 1 }))
}, { deep: true })

// Sync footer JSON fields to formData
watch(footerColumnsJson, (v) => { try { formData.footer_columns = JSON.parse(v) } catch {} })
watch(footerSocialJson, (v) => { try { formData.footer_social = JSON.parse(v) } catch {} })
watch(footerPaymentJson, (v) => { try { formData.footer_payment_icons = JSON.parse(v) } catch {} })

// Sync showcase items to formData
watch(showcaseItems, (val) => {
  formData.showcase_categories = val.map((s, i) => ({
    categoryId: s.categoryId,
    coverImage: s.coverImage || '',
    videos: s.videos || ''
  }))
}, { deep: true })

// Sync popular items to formData
watch(popularItems, (val) => {
  formData.popular_products = val.map(p => ({ productId: p.productId }))
}, { deep: true })

function addSlide() {
  heroSlides.value.push({ image: '', title: '', link: '', sort: heroSlides.value.length + 1 })
}
function removeSlide(idx) { heroSlides.value.splice(idx, 1) }
function moveSlide(idx, delta) {
  const arr = heroSlides.value
  const t = idx + delta
  if (t < 0 || t >= arr.length) return
  const tmp = arr[idx]; arr[idx] = arr[t]; arr[t] = tmp
  heroSlides.value = [...arr]
}
function triggerUpload(idx) { uploadRefs[idx]?.click() }

async function handleUpload(e, idx) {
  const file = e.target.files?.[0]
  if (!file) return
  try {
    const res = await uploadFile(file)
    if (res.data?.url) heroSlides.value[idx].image = res.data.url
    ElMessage.success('上传成功')
  } catch { ElMessage.error('上传失败') }
  e.target.value = ''
}

onMounted(async () => {
  try {
    const res = await getPageConfigs()
    if (res.data) {
      Object.entries(res.data).forEach(([type, pc]) => {
        currentConfigs.value[type] = pc?.configJson || defaults[type]
      })
    }
  } catch {}
  // Load options for showcase categories and popular products selectors
  try {
    const catRes = await getAdminCategories()
    const flat = []
    function walk(items) { items.forEach(i => { flat.push(i); if (i.children) walk(i.children) }) }
    walk(catRes.data || [])
    allCategoryOptions.value = flat
  } catch {}
  try {
    const prodRes = await getAdminProducts({ page: 1, size: 200 })
    allProductOptions.value = prodRes.data?.list || []
  } catch {}
  initTab('HOME')
})

function initTab(type) {
  // POPULAR reads/writes HOME config
  const configType = type === 'POPULAR' ? 'HOME' : type
  const json = currentConfigs.value[configType] || defaults[configType]
  editingJson.value = json
  try {
    const parsed = JSON.parse(json)
    Object.keys(formData).forEach(k => delete formData[k])
    Object.assign(formData, parsed)
    // Init hero slides for HOME
    if (type === 'HOME') {
      heroSlides.value = Array.isArray(formData.hero_slides) ? [...formData.hero_slides] : []
    }
    // Init showcase & popular items for HOME and POPULAR
    if (type === 'HOME' || type === 'POPULAR') {
      const raw = formData.showcase_categories
      if (Array.isArray(raw)) {
        showcaseItems.value = raw.map((item, i) => {
          scVideoUrls[i] = ''
          if (typeof item === 'object' && item !== null) {
            return { categoryId: item.categoryId || null, coverImage: item.coverImage || '', videos: item.videos || '' }
          }
          return { categoryId: item, coverImage: '', videos: '' }
        })
      } else { showcaseItems.value = [] }
      const rawPop = formData.popular_products
      if (Array.isArray(rawPop)) {
        popularItems.value = rawPop.map(item => {
          if (typeof item === 'object' && item !== null) return { productId: item.productId || null }
          return { productId: item }
        })
      } else { popularItems.value = [] }
    }
    // Init footer JSON editors
    if (type === 'FOOTER') {
      footerColumnsJson.value = JSON.stringify(formData.footer_columns || [], null, 2)
      footerSocialJson.value = JSON.stringify(formData.footer_social || [], null, 2)
      footerPaymentJson.value = JSON.stringify(formData.footer_payment_icons || [], null, 2)
    }
  } catch {}

  // POPULAR does not have a schema — just hide schema form
  if (type === 'POPULAR') {
    currentSchema.value = {}
    return
  }

  getPageConfigSchema(type).then(res => {
    currentSchema.value = res.data || {}
    Object.entries(currentSchema.value).forEach(([key, field]) => {
      if (formData[key] === undefined) formData[key] = field.default
    })
  }).catch(() => { currentSchema.value = {}; showRaw.value = true })
}

// Track actual config type for POPULAR tab (saves to HOME)
function effectiveConfigType() {
  return activeTab.value === 'POPULAR' ? 'HOME' : activeTab.value
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
  editingJson.value = defaults[effectiveConfigType()]
}

async function savePage() {
  saving.value = true
  try {
    const json = showRaw.value ? editingJson.value : JSON.stringify(formData)
    const saveType = effectiveConfigType()
    await updatePageConfig(saveType, { configJson: json })
    currentConfigs.value[saveType] = json
    ElMessage.success('保存成功')
  } catch {} finally { saving.value = false }
}

watch(formData, () => {
  if (!showRaw.value) editingJson.value = JSON.stringify(formData, null, 2)
}, { deep: true })
</script>

<style scoped>
.schema-form {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 20px;
}
.editor-header {
  display: flex; justify-content: space-between; align-items: center;
}

.hero-slides-editor, .items-editor { max-width: 900px; }
.slide-card {
  background: #fafafa; border: 1px solid #ebeef5; border-radius: 8px;
  padding: 16px; margin-bottom: 12px;
}
.slide-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.slide-label { font-weight: 600; color: #333; }
.slide-actions { display: flex; gap: 6px; }
.slide-preview {
  width: 100%; height: 120px; background: #f0f2f5; border-radius: 6px;
  overflow: hidden; position: relative; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  border: 2px dashed #d9d9d9; transition: border-color 0.2s;
}
.slide-preview:hover { border-color: #409EFF; }
.slide-preview-media { width: 100%; height: 100%; object-fit: cover; }
.slide-preview-empty { text-align: center; color: #bbb; }
.slide-preview-empty span { display: block; font-size: 12px; margin-top: 4px; }
.slide-preview-type { position: absolute; top: 4px; right: 4px; }

@media (max-width: 768px) {
  .schema-form { grid-template-columns: 1fr; }
}
</style>
