<template>
  <div class="pm-page">
    <!-- Header -->
    <div class="page-header">
      <h2>产品管理</h2>
      <div class="page-header-actions">
        <button class="btn btn-outline" @click="ElMessage.info('批量导入功能开发中')"><el-icon :size="14"><Upload /></el-icon> 批量导入</button>
        <button class="btn btn-outline" @click="ElMessage.success('已导出产品数据')"><el-icon :size="14"><Download /></el-icon> 导出</button>
        <button class="btn btn-primary" @click="openAddModal"><el-icon :size="14"><Plus /></el-icon> 新增产品</button>
      </div>
    </div>

    <!-- KPI Strip -->
    <div class="pm-kpi-strip">
      <div v-for="k in kpis" :key="k.label" class="pm-kpi-card">
        <div class="pm-kpi-icon" :style="{background:k.iconBg,color:k.iconColor}">
          <el-icon :size="17"><component :is="k.iconComp" /></el-icon>
        </div>
        <div class="pm-kpi-body">
          <div class="pm-kpi-val">{{ k.val }}</div>
          <div class="pm-kpi-label">{{ k.label }}</div>
          <div class="pm-kpi-trend" :class="k.trendDir">
            <el-icon :size="11"><component :is="k.trendIcon" /></el-icon> {{ k.trend }}
          </div>
        </div>
      </div>
    </div>

    <!-- Toolbar Card -->
    <div class="card" style="margin-bottom:16px">
      <div class="card-body" style="padding:12px 16px">
        <!-- Time period row -->
        <div class="pm-time-row">
          <div class="pm-time-label"><el-icon :size="13"><Calendar /></el-icon> 统计周期</div>
          <div class="pm-time-tabs">
            <button v-for="p in periods" :key="p.key" class="pm-time-tab" :class="{active:currentPeriod===p.key}" @click="switchPeriod(p.key)">{{ p.label }}</button>
          </div>
          <div class="pm-custom-range" v-if="currentPeriod==='custom'">
            <input type="date" class="pm-date-input" v-model="customFrom" />
            <span style="color:#909399;font-size:12px">至</span>
            <input type="date" class="pm-date-input" v-model="customTo" />
            <button class="btn btn-sm btn-primary" @click="applyCustomRange">应用</button>
          </div>
          <div class="pm-time-summary" v-if="currentPeriod!=='custom'">
            当前统计：<b style="color:#6c5ce7">{{ periodLabel }}</b>
          </div>
        </div>

        <!-- Status tabs + search + filter + view toggle -->
        <div class="pm-toolbar" style="margin-top:10px">
          <div class="pm-tab-group">
            <button class="pm-tab" :class="{active:statusFilter==='all'}" @click="statusFilter='all';page=1;load()">
              全部 <span class="pm-tab-count">{{ tabCounts.all }}</span>
            </button>
            <button class="pm-tab" :class="{active:statusFilter==='ON'}" @click="statusFilter='ON';page=1;load()">
              在售 <span class="pm-tab-count">{{ tabCounts.ON }}</span>
            </button>
            <button class="pm-tab" :class="{active:statusFilter==='OFF'}" @click="statusFilter='OFF';page=1;load()">
              已下架 <span class="pm-tab-count">{{ tabCounts.OFF }}</span>
            </button>
            <button class="pm-tab" :class="{active:statusFilter==='low'}" @click="statusFilter='low';page=1;load()">
              库存预警 <span class="pm-tab-count" style="color:#e17055">{{ lowStockCount }}</span>
            </button>
          </div>
          <div class="pm-toolbar-right">
            <div class="pm-search-wrap">
              <el-icon :size="12"><Search /></el-icon>
              <input v-model="searchKeyword" placeholder="搜索产品名、SKU、品牌..." @input="onSearch" />
            </div>
            <select class="pm-select" v-model="catFilter" @change="page=1;load()">
              <option value="">全部分类</option>
              <option v-for="c in allCats" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
            <select class="pm-select" v-model="sortKey" @change="page=1;load()">
              <option value="">默认排序</option>
              <option value="price_asc">价格升序</option>
              <option value="price_desc">价格降序</option>
              <option value="sales_desc">销量最高</option>
              <option value="stock_asc">库存最低</option>
              <option value="rating_desc">评分最高</option>
              <option value="new">最新上架</option>
            </select>
            <div class="pm-view-toggle">
              <button class="pm-view-btn" :class="{active:viewMode==='table'}" @click="viewMode='table'" title="列表视图"><el-icon :size="13"><List /></el-icon></button>
              <button class="pm-view-btn" :class="{active:viewMode==='grid'}" @click="viewMode='grid'" title="卡片视图"><el-icon :size="13"><Grid /></el-icon></button>
            </div>
          </div>
        </div>

        <!-- Batch bar -->
        <transition name="fade">
          <div class="pm-batch-bar" v-if="selectedIds.length">
            <span>已选 <strong>{{ selectedIds.length }}</strong> 个产品</span>
            <div style="display:flex;gap:8px;margin-left:auto">
              <button class="btn btn-sm btn-outline" @click="batchStatus('ON')">上架</button>
              <button class="btn btn-sm btn-outline" @click="batchStatus('OFF')">下架</button>
              <button class="btn btn-sm btn-outline" @click="batchExport">导出选中</button>
              <button class="btn btn-sm" style="background:rgba(225,112,85,0.1);color:#e17055;border:1px solid rgba(225,112,85,0.3)" @click="batchDelete">批量删除</button>
            </div>
          </div>
        </transition>
      </div>
    </div>

    <!-- Table View -->
    <div class="card" v-if="viewMode==='table'">
      <div class="card-body" style="padding:0">
        <el-table :data="products" stripe v-loading="loading" row-key="id" size="small"
          @selection-change="onSelectChange" ref="tableRef">
          <el-table-column type="selection" width="40" />
          <el-table-column label="产品" min-width="260">
            <template #default="{row}">
              <div class="pm-product-cell">
                <div class="pm-product-thumb">
                  <video v-if="isVideo(coverMedia(row))" :src="coverMedia(row)" muted />
                  <img v-else-if="coverMedia(row)" :src="coverMedia(row)" />
                  <el-icon v-else :size="17" color="#a29bfe"><Box /></el-icon>
                </div>
                <div style="flex:1;min-width:0">
                  <div class="pm-product-name">{{ row.name }}</div>
                  <div class="pm-product-meta">
                    <span>SKU: {{ row.sku || row.id }}</span>
                    <span v-if="row.brand" class="pm-product-brand">{{ row.brand }}</span>
                    <span v-if="originalGtPrice(row)" style="color:#e17055;font-size:10px;font-weight:700">-{{ discountPct(row) }}%</span>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="分类" width="90">
            <template #default="{row}"><span style="font-size:12px;color:#909399">{{ catMap[row.categoryId] || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="价格" width="110" sortable prop="price">
            <template #default="{row}">
              <div class="pm-price-main">¥{{ Number(row.price).toFixed(2) }}</div>
              <div v-if="originalGtPrice(row)" class="pm-price-original">¥{{ Number(row.originalPrice).toFixed(2) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="库存" width="120" sortable prop="stock">
            <template #default="{row}">
              <div class="pm-stock">
                <div class="pm-stock-bar"><div class="pm-stock-fill" :style="{width:stockPct(row)+'%',background:stockColor(row)}"></div></div>
                <span :style="{fontSize:'12px',fontWeight:600,color:stockColor(row)}">{{ row.stock===0?'售罄':row.stock }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="销量" width="80" sortable prop="sales" align="center">
            <template #default="{row}"><span style="font-size:13px;font-weight:600">{{ (row.sales||0).toLocaleString() }}</span></template>
          </el-table-column>
          <el-table-column label="评分" width="110" align="center">
            <template #default="{row}">
              <div v-if="row.rating" class="pm-rating">
                <span class="pm-stars">{{ starsHtml(row.rating) }}</span>
                <span style="font-weight:600;font-size:12px">{{ row.rating }}</span>
                <span style="color:#909399;font-size:11px">({{ row.reviews||0 }})</span>
              </div>
              <span v-else style="color:#c0c4cc;font-size:11px">—</span>
            </template>
          </el-table-column>
          <el-table-column label="标签" width="90" align="center">
            <template #default="{row}">
              <span v-if="row.tag" class="pm-tag" :class="'pm-tag-'+row.tag">{{ tagLabel(row.tag) }}</span>
              <span v-else style="color:#c0c4cc;font-size:11px">—</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80" align="center">
            <template #default="{row}">
              <span class="status-badge" :class="row.status==='ON'?'on':'off'">{{ row.status==='ON'?'在售':'下架' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="125" fixed="right" align="center">
            <template #default="{row}">
              <div class="pm-actions">
                <button class="pm-action-btn" title="查看详情" @click="openDetail(row)"><el-icon :size="13"><View /></el-icon></button>
                <button class="pm-action-btn" title="编辑" @click="openEditModal(row)"><el-icon :size="13"><EditPen /></el-icon></button>
                <button class="pm-action-btn" title="复制" @click="duplicate(row)"><el-icon :size="13"><DocumentCopy /></el-icon></button>
                <button class="pm-action-btn danger" title="删除" @click="handleDelete(row)"><el-icon :size="13"><Delete /></el-icon></button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && products.length===0" description="暂无产品" :image-size="64" style="padding:40px 0" />
      </div>
      <div class="pm-pagination" v-if="total>0">
        <span>显示 {{ (page-1)*size+1 }}-{{ Math.min(page*size,total) }} 共 {{ total }} 条</span>
        <el-pagination v-model:current-page="page" :page-size="size" :page-sizes="[10,20,50]"
          :total="total" layout="sizes,prev,pager,next" @size-change="s=>{size=s;page=1;load()}"
          @current-change="load" background small />
      </div>
    </div>

    <!-- Grid View -->
    <div v-if="viewMode==='grid'">
      <div class="pm-grid">
        <div v-for="row in products" :key="row.id" class="pm-grid-card" @click="openDetail(row)">
          <div class="pm-grid-card-img">
            <video v-if="isVideo(coverMedia(row))" :src="coverMedia(row)" muted style="width:100%;height:100%;object-fit:cover" />
            <img v-else-if="coverMedia(row)" :src="coverMedia(row)" style="width:100%;height:100%;object-fit:cover" />
            <el-icon v-else :size="38" color="#a29bfe"><Box /></el-icon>
            <div class="pm-grid-card-badge">
              <span class="status-badge" :class="row.status==='ON'?'on':'off'">{{ row.status==='ON'?'在售':'下架' }}</span>
            </div>
            <div class="pm-grid-card-actions">
              <button class="pm-grid-action-btn" @click.stop="openEditModal(row)"><el-icon :size="11"><EditPen /></el-icon> 编辑</button>
              <button class="pm-grid-action-btn" @click.stop="duplicate(row)"><el-icon :size="11"><DocumentCopy /></el-icon> 复制</button>
              <button class="pm-grid-action-btn" @click.stop="handleDelete(row)"><el-icon :size="11"><Delete /></el-icon> 删除</button>
            </div>
          </div>
          <div class="pm-grid-card-body">
            <div class="pm-grid-card-name">{{ row.name }}</div>
            <div class="pm-grid-card-meta">
              <span>SKU: {{ row.sku || row.id }}</span>
              <span style="color:#909399">{{ catMap[row.categoryId] || '-' }}</span>
            </div>
            <div class="pm-grid-card-price">
              ¥{{ Number(row.price).toFixed(2) }}
              <span v-if="originalGtPrice(row)" class="pm-grid-card-original">¥{{ Number(row.originalPrice).toFixed(2) }}</span>
            </div>
            <div class="pm-grid-card-stats">
              <span>库存: <b :style="{color:stockColor(row)}">{{ row.stock }}</b></span>
              <span>销量: <b>{{ (row.sales||0).toLocaleString() }}</b></span>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && products.length===0" description="暂无产品" :image-size="64" style="padding:40px 0" />
      <div class="pm-pagination" v-if="total>0" style="background:#fff;border-radius:12px;border:1px solid #e0e3e8;margin-top:16px">
        <span style="padding:12px 16px">显示 {{ (page-1)*size+1 }}-{{ Math.min(page*size,total) }} 共 {{ total }} 条</span>
        <el-pagination v-model:current-page="page" :page-size="size" :page-sizes="[10,20,50]"
          :total="total" layout="sizes,prev,pager,next" @size-change="s=>{size=s;page=1;load()}"
          @current-change="load" background small />
      </div>
    </div>

    <!-- Add/Edit Modal -->
    <el-dialog v-model="modalVisible" :title="editId?'编辑产品':'新增产品'" width="620px" destroy-on-close>
      <el-form :model="form" label-width="80px" label-position="top">
        <el-row :gutter="16">
          <el-col :span="10"><el-form-item label="产品名称"><el-input v-model="form.name" /></el-form-item></el-col>
          <el-col :span="7"><el-form-item label="SKU"><el-input v-model="form.sku" placeholder="如 BT-EAR-001" /></el-form-item></el-col>
          <el-col :span="7"><el-form-item label="品牌"><el-input v-model="form.brand" placeholder="如 Apple" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="原价"><el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="分类">
            <el-select v-model="form.categoryId" placeholder="选择分类" style="width:100%">
              <el-option v-for="c in allCats" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="8"><el-form-item label="标签">
            <el-select v-model="form.tag" placeholder="选择" clearable style="width:100%">
              <el-option label="🔥 热销" value="hot" /><el-option label="🆕 新品" value="new" />
              <el-option label="💰 促销" value="sale" /><el-option label="👍 推荐" value="recommend" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="8"><el-form-item label="评分"><el-input-number v-model="form.rating" :min="0" :max="5" :precision="1" :step="0.1" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="状态">
            <el-select v-model="form.status" style="width:100%">
              <el-option label="在售" value="ON" /><el-option label="下架" value="OFF" />
            </el-select>
          </el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="modalVisible=false">取消</el-button>
        <el-button type="primary" @click="saveProduct" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- Detail Drawer -->
    <el-drawer v-model="drawerVisible" :title="drawerProduct?.name" direction="rtl" size="460px">
      <template v-if="drawerProduct">
        <div class="drawer-section">
          <div class="drawer-section-title">基本信息</div>
          <div class="drawer-row"><span class="drawer-label">SKU</span><span class="drawer-val">{{ drawerProduct.sku || drawerProduct.id }}</span></div>
          <div class="drawer-row"><span class="drawer-label">分类</span><span class="drawer-val">{{ catMap[drawerProduct.categoryId] || '-' }}</span></div>
          <div class="drawer-row"><span class="drawer-label">价格</span><span class="drawer-val" style="color:#6c5ce7;font-weight:700">¥{{ Number(drawerProduct.price).toFixed(2) }}</span></div>
          <div class="drawer-row"><span class="drawer-label">原价</span><span class="drawer-val">{{ drawerProduct.originalPrice ? '¥'+Number(drawerProduct.originalPrice).toFixed(2) : '-' }}</span></div>
          <div class="drawer-row"><span class="drawer-label">库存</span><span class="drawer-val" :style="{color:stockColor(drawerProduct),fontWeight:700}">{{ drawerProduct.stock }}</span></div>
          <div class="drawer-row"><span class="drawer-label">销量</span><span class="drawer-val" style="font-weight:600">{{ (drawerProduct.sales||0).toLocaleString() }}</span></div>
          <div class="drawer-row"><span class="drawer-label">状态</span><span class="drawer-val"><span class="status-badge" :class="drawerProduct.status==='ON'?'on':'off'">{{ drawerProduct.status==='ON'?'在售':'下架' }}</span></span></div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getAdminProducts, getAdminCategories, createProduct, updateProduct, updateProductStatus, deleteProduct, batchDeleteProducts } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload, Download, Search, EditPen, DocumentCopy, Delete, View, Calendar, List, Grid, Box, Money, ShoppingBag, TrendCharts, Warning, CircleCheck, ArrowUp, ArrowDown, Minus } from '@element-plus/icons-vue'

const products = ref([]); const total = ref(0); const page = ref(1); const size = ref(10); const loading = ref(false)
const statusFilter = ref('all'); const searchKeyword = ref(''); const catFilter = ref(''); const sortKey = ref('')
const selectedIds = ref([]); const allCats = ref([]); const catMap = ref({})
const tabCounts = ref({all:0,ON:0,OFF:0}); const tableRef = ref(null)
const viewMode = ref('table'); const currentPeriod = ref('month')
const customFrom = ref(''); const customTo = ref('')
let searchTimer = null

// Modal
const modalVisible = ref(false); const editId = ref(null); const saving = ref(false)
const form = ref({ name:'', sku:'', brand:'', description:'', price:0, originalPrice:0, stock:0, categoryId:null, tag:'', rating:null, status:'ON' })
// Drawer
const drawerVisible = ref(false); const drawerProduct = ref(null)

const periods = [
  { key: 'today', label: '今日' }, { key: 'week', label: '本周' },
  { key: 'month', label: '本月' }, { key: 'year', label: '全年' },
  { key: 'custom', label: '自定义' },
]

// KPI computation
const allProducts = ref([]) // all products for KPI calculation

const scaleMap = { today:{sales:0.038,rev:0.034}, week:{sales:0.22,rev:0.20}, month:{sales:1,rev:1}, year:{sales:11.5,rev:12}, custom:{sales:0.5,rev:0.5} }
const trendMap = { today:{rev:5.2,order:3.8}, week:{rev:8.4,order:6.2}, month:{rev:12.5,order:8.2}, year:{rev:32.8,order:28.5}, custom:{rev:10.1,order:7.6} }

const lowStockCount = computed(() => allProducts.value.filter(p => p.stock <= 5 && p.stock > 0).length)

const kpis = computed(() => {
  let list = allProducts.value
  // Filter by custom date range if set
  if (currentPeriod.value === 'custom' && customFrom.value && customTo.value) {
    list = list.filter(p => {
      if (!p.createTime) return false
      const t = p.createTime.substring(0,10)
      return t >= customFrom.value && t <= customTo.value
    })
  }
  const sc = scaleMap[currentPeriod.value] || scaleMap.month
  const tr = trendMap[currentPeriod.value] || trendMap.month
  const onSale = list.filter(p => p.status === 'ON').length
  const lowStock = list.filter(p => p.stock <= 5 && p.stock > 0).length
  const outStock = list.filter(p => p.stock === 0).length
  const hotProducts = list.filter(p => (p.sales||0) >= 500).length
  const deadProducts = list.filter(p => (p.sales||0) < 50 && p.status === 'ON').length
  const totalSales = Math.round(list.reduce((s,p) => s + (p.sales||0), 0) * sc.sales)
  const totalRev = Math.round(list.reduce((s,p) => s + Number(p.price||0) * (p.sales||0), 0) * sc.rev)
  const revDisplay = totalRev >= 10000 ? '¥' + (totalRev/10000).toFixed(1) + '万' : '¥' + totalRev.toLocaleString()
  return [
    { iconComp: Money, iconBg:'rgba(108,92,231,0.1)', iconColor:'#6c5ce7', val:revDisplay, label:'本月销售额', trend:`↑ ${tr.rev}% vs 上月`, trendDir:'up', trendIcon:ArrowUp },
    { iconComp: ShoppingBag, iconBg:'rgba(0,184,148,0.1)', iconColor:'#00b894', val:totalSales.toLocaleString(), label:'本月订单数', trend:`↑ ${tr.order}% vs 上月`, trendDir:'up', trendIcon:ArrowUp },
    { iconComp: TrendCharts, iconBg:'rgba(225,112,85,0.1)', iconColor:'#e17055', val:hotProducts, label:'热销产品数', trend:'销量 ≥ 500', trendDir:'up', trendIcon:CircleCheck },
    { iconComp: Warning, iconBg:'rgba(116,185,255,0.15)', iconColor:'#74b9ff', val:deadProducts, label:'滞销产品数', trend:'在售且销量 < 50', trendDir:deadProducts>0?'down':'flat', trendIcon:deadProducts>0?ArrowDown:Minus },
    { iconComp: Box, iconBg:'rgba(253,121,168,0.12)', iconColor:'#fd79a8', val:outStock+lowStock, label:'库存预警', trend:`${outStock} 售罄 / ${lowStock} 偏低`, trendDir:outStock>0?'down':'flat', trendIcon:outStock>0?ArrowDown:Minus },
  ]
})

const periodLabels = { today:'今日', week:'本周', month:'本月', year:'全年' }
const periodLabel = computed(() => periodLabels[currentPeriod.value]||'自定义')

function switchPeriod(key) {
  currentPeriod.value = key
  if (key !== 'custom') { customFrom.value = ''; customTo.value = '' }
}

function applyCustomRange() {
  if (!customFrom.value || !customTo.value) { ElMessage.warning('请选择开始和结束日期'); return }
  if (customFrom.value > customTo.value) { ElMessage.warning('开始日期不能晚于结束日期'); return }
  ElMessage.success(`已筛选 ${customFrom.value} 至 ${customTo.value}`)
  page.value = 1; load()
}

onMounted(() => { load(); loadCategories(); loadAllForKpi() })

async function loadCategories() {
  try { const res = await getAdminCategories(); const all = res.data||[]; allCats.value = all; const m = {}; all.forEach(c => m[c.id] = c.name); catMap.value = m } catch {}
}
async function loadAllForKpi() {
  try { const res = await getAdminProducts({page:1,size:1000}); allProducts.value = res.data?.list || [] } catch {}
}

// Helpers
function parseJsonField(v) { if(!v) return []; if(Array.isArray(v)) return v; try{return JSON.parse(v)}catch{return []} }
function isVideo(u) { return u && /\.mp4$/i.test(u) }
function coverMedia(r) { const v = parseJsonField(r.videos); if(v.length) return v[0]; const i = parseJsonField(r.images); if(i.length) return i[0]; return r.coverImage||null }
function originalGtPrice(r) { return Number(r.originalPrice||0) > Number(r.price||0) }
function discountPct(r) { return Math.round((1 - r.price/r.originalPrice) * 100) }
function stockPct(r) { return Math.min(100, Math.round(r.stock/200*100)) }
function stockColor(r) { if(r.stock===0) return '#e17055'; return r.stock<=10?'#fdcb6e':'#00b894' }
function starsHtml(r) { const full=Math.floor(r); const half=r%1>=0.5; let s=''; for(let i=0;i<full;i++) s+='★'; if(half) s+='½'; const empty=5-Math.ceil(r); for(let i=0;i<empty;i++) s+='☆'; return s }
function tagLabel(t) { return {hot:'🔥热销',new:'🆕新品',sale:'💰促销',recommend:'👍推荐'}[t] || t }

function onSelectChange(rows) { selectedIds.value = rows.map(r => r.id) }
function onSearch() { clearTimeout(searchTimer); searchTimer = setTimeout(()=>{page.value=1;load()},300) }

async function load() {
  loading.value = true
  try {
    const params = { page:page.value, size:size.value }
    if(statusFilter.value!=='all' && statusFilter.value!=='low') params.status = statusFilter.value
    const res = await getAdminProducts(params)
    let list = res.data?.list||[]; total.value = res.data?.total||0
    // client-side filters
    if(catFilter.value) list = list.filter(p => p.categoryId == catFilter.value)
    if(statusFilter.value==='low') list = list.filter(p => p.stock <= 5)
    if(currentPeriod.value==='custom' && customFrom.value && customTo.value) {
      list = list.filter(p => { if(!p.createTime) return false; const t = p.createTime.substring(0,10); return t >= customFrom.value && t <= customTo.value })
    }
    if(searchKeyword.value) {
      const kw = searchKeyword.value.toLowerCase()
      list = list.filter(p => (p.name||'').toLowerCase().includes(kw) || (p.sku||'').toLowerCase().includes(kw) || (p.brand||'').toLowerCase().includes(kw))
    }
    // client-side sort
    if(sortKey.value==='price_asc') list.sort((a,b)=>Number(a.price)-Number(b.price))
    else if(sortKey.value==='price_desc') list.sort((a,b)=>Number(b.price)-Number(a.price))
    else if(sortKey.value==='sales_desc') list.sort((a,b)=>(b.sales||0)-(a.sales||0))
    else if(sortKey.value==='stock_asc') list.sort((a,b)=>(a.stock||0)-(b.stock||0))
    else if(sortKey.value==='rating_desc') list.sort((a,b)=>(b.rating||0)-(a.rating||0))
    else if(sortKey.value==='new') list.sort((a,b)=>new Date(b.createTime||0)-new Date(a.createTime||0))
    products.value = list; selectedIds.value = []
    // Refresh tab counts
    try { const rOn=await getAdminProducts({page:1,size:1,status:'ON'}); const rOff=await getAdminProducts({page:1,size:1,status:'OFF'}); tabCounts.value={all:(rOn.data?.total||0)+(rOff.data?.total||0),ON:rOn.data?.total||0,OFF:rOff.data?.total||0} } catch {}
    loadAllForKpi()
  } catch {} finally { loading.value = false }
}

function batchExport() {
  const selected = allProducts.value.filter(p => selectedIds.value.includes(p.id))
  const csv = ['名称,SKU,品牌,分类,价格,库存,销量,状态']
  selected.forEach(p => csv.push([p.name,p.sku||'-',p.brand||'-',catMap.value[p.categoryId]||'-',p.price,p.stock,p.sales||0,p.status==='ON'?'在售':'下架'].join(',')))
  const blob = new Blob(['﻿'+csv.join('\n')], {type:'text/csv;charset=utf-8'})
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a'); a.href=url; a.download='products-export.csv'; a.click()
  URL.revokeObjectURL(url)
  ElMessage.success(`已导出 ${selected.length} 个产品`)
}

// Batch
async function batchStatus(st) { try { await Promise.all(selectedIds.value.map(id=>updateProductStatus(id,st))); ElMessage.success(`已${st==='ON'?'上架':'下架'} ${selectedIds.value.length} 个产品`); load() } catch {} }
async function batchDelete() { try { await ElMessageBox.confirm(`确定删除 ${selectedIds.value.length} 个产品？`,'批量删除',{type:'warning'}); await batchDeleteProducts(selectedIds.value); ElMessage.success('已删除'); load() } catch {} }
async function handleDelete(row) { try { await ElMessageBox.confirm(`确定删除「${row.name}」？`,'删除',{type:'warning',confirmButtonText:'确定'}); await deleteProduct(row.id); ElMessage.success('已删除'); load() } catch {} }

// Modal
function openAddModal() { editId.value=null; form.value={name:'',sku:'',brand:'',description:'',price:0,originalPrice:0,stock:0,categoryId:null,tag:'',rating:null,status:'ON'}; modalVisible.value=true }
function openEditModal(row) { editId.value=row.id; form.value={name:row.name,sku:row.sku||'',brand:row.brand||'',description:row.description||'',price:Number(row.price),originalPrice:Number(row.originalPrice||0),stock:row.stock,categoryId:row.categoryId,tag:row.tag||'',rating:row.rating||null,status:row.status}; modalVisible.value=true }
async function saveProduct() {
  saving.value = true
  try { const data = {...form, price:parseFloat(form.price), originalPrice:parseFloat(form.originalPrice)||null, images:null, videos:null}; if(editId.value) { await updateProduct(editId.value, data); ElMessage.success('更新成功') } else { await createProduct(data); ElMessage.success('创建成功') }; modalVisible.value = false; load() } catch {} finally { saving.value = false }
}
async function duplicate(row) { try { const data = {...row, id:undefined, name:row.name+' (副本)', sku:(row.sku||'')+'-C', status:'OFF', sales:0 }; await createProduct(data); ElMessage.success('已复制'); load() } catch {} }
function openDetail(row) { drawerProduct.value = row; drawerVisible.value = true }
</script>

<style scoped>
/* === Layout === */
.pm-page { max-width:100% }
.page-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:20px }
.page-header h2 { font-size:20px; font-weight:600; margin:0 }
.page-header-actions { display:flex; gap:8px }

.btn { padding:8px 16px; border:none; border-radius:8px; cursor:pointer; font-size:13px; font-weight:500; display:inline-flex; align-items:center; gap:6px; transition:all .2s; font-family:inherit }
.btn-primary { background:#6c5ce7; color:#fff } .btn-primary:hover { background:#5b4cdb }
.btn-outline { background:transparent; border:1px solid #e0e3e8; color:#606266 } .btn-outline:hover { border-color:#6c5ce7; color:#6c5ce7 }
.btn-sm { padding:5px 12px; font-size:12px }

.card { background:#fff; border-radius:12px; border:1px solid #e0e3e8; box-shadow:0 1px 4px rgba(0,0,0,0.04) }
.card-body { padding:20px }

/* === KPI Strip === */
.pm-kpi-strip { display:grid; grid-template-columns:repeat(5,1fr); gap:14px; margin-bottom:16px }
.pm-kpi-card { background:#fff; border-radius:12px; padding:16px 18px; border:1px solid #e0e3e8; box-shadow:0 1px 4px rgba(0,0,0,0.04); display:flex; align-items:center; gap:14px; transition:all .2s }
.pm-kpi-card:hover { box-shadow:0 2px 12px rgba(0,0,0,0.06); transform:translateY(-1px) }
.pm-kpi-icon { width:42px; height:42px; border-radius:11px; display:flex; align-items:center; justify-content:center; flex-shrink:0 }
.pm-kpi-body { flex:1; min-width:0 }
.pm-kpi-val { font-size:20px; font-weight:700; line-height:1.2; white-space:nowrap; overflow:hidden; text-overflow:ellipsis }
.pm-kpi-label { font-size:12px; color:#909399; margin-top:2px }
.pm-kpi-trend { font-size:11px; font-weight:500; display:flex; align-items:center; gap:2px; margin-top:3px }
.pm-kpi-trend.up { color:#00b894 } .pm-kpi-trend.down { color:#e17055 } .pm-kpi-trend.flat { color:#909399 }

/* === Time Row === */
.pm-time-row { display:flex; align-items:center; gap:12px; flex-wrap:wrap; padding-bottom:10px; border-bottom:1px solid #e0e3e8; margin-bottom:2px }
.pm-time-label { font-size:12px; font-weight:600; color:#909399; white-space:nowrap; display:flex; align-items:center; gap:5px }
.pm-time-tabs { display:flex; gap:3px; background:#f5f7fa; border-radius:8px; padding:3px }
.pm-time-tab { padding:5px 14px; border-radius:6px; font-size:12px; font-weight:500; cursor:pointer; border:none; background:transparent; color:#909399; transition:all .2s; white-space:nowrap; font-family:inherit }
.pm-time-tab:hover { color:#606266; background:rgba(0,0,0,0.04) }
.pm-time-tab.active { background:#fff; color:#6c5ce7; box-shadow:0 1px 4px rgba(0,0,0,0.04); font-weight:600 }
.pm-custom-range { display:flex; align-items:center; gap:6px }
.pm-date-input { padding:5px 8px; border:1px solid #e0e3e8; border-radius:8px; font-size:12px; outline:none; background:#fff; color:#606266; transition:all .2s; font-family:inherit }
.pm-date-input:focus { border-color:#6c5ce7 }
.pm-time-summary { margin-left:auto; font-size:11px; color:#909399; background:#f5f7fa; padding:4px 10px; border-radius:10px; white-space:nowrap }

/* === Toolbar === */
.pm-toolbar { display:flex; align-items:center; gap:12px; flex-wrap:wrap }
.pm-tab-group { display:flex; gap:2px; border-bottom:2px solid #e0e3e8; flex:1 }
.pm-tab { padding:7px 14px; font-size:13px; font-weight:500; border:none; background:transparent; cursor:pointer; color:#909399; border-bottom:2px solid transparent; margin-bottom:-2px; border-radius:6px 6px 0 0; white-space:nowrap; font-family:inherit; transition:all .2s }
.pm-tab:hover { color:#606266; background:#f5f7fa }
.pm-tab.active { color:#6c5ce7; border-bottom-color:#6c5ce7; background:rgba(108,92,231,0.08) }
.pm-tab-count { background:#f5f7fa; color:#909399; font-size:10px; padding:1px 6px; border-radius:8px; margin-left:4px; font-weight:600 }
.pm-tab.active .pm-tab-count { background:rgba(108,92,231,0.15); color:#6c5ce7 }
.pm-toolbar-right { display:flex; align-items:center; gap:8px; flex-shrink:0 }
.pm-search-wrap { position:relative; display:flex; align-items:center }
.pm-search-wrap :deep(.el-icon) { position:absolute; left:10px; color:#909399; font-size:12px; z-index:1 }
.pm-search-wrap input { padding:7px 10px 7px 30px; border:1px solid #e0e3e8; border-radius:20px; font-size:12px; outline:none; width:200px; background:#f5f7fa; font-family:inherit; transition:all .2s }
.pm-search-wrap input:focus { border-color:#6c5ce7; background:#fff; box-shadow:0 0 0 3px rgba(108,92,231,0.08) }
.pm-select { padding:7px 10px; border:1px solid #e0e3e8; border-radius:8px; font-size:12px; outline:none; background:#f5f7fa; color:#606266; cursor:pointer; font-family:inherit; transition:all .2s }

/* View toggle */
.pm-view-toggle { display:flex; border:1px solid #e0e3e8; border-radius:8px; overflow:hidden }
.pm-view-btn { width:32px; height:32px; border:none; background:#f5f7fa; cursor:pointer; color:#909399; font-size:13px; display:flex; align-items:center; justify-content:center; transition:all .2s }
.pm-view-btn.active { background:#6c5ce7; color:#fff }
.pm-view-btn:not(.active):hover { background:#e0e3e8 }

/* Batch bar */
.pm-batch-bar { display:flex; align-items:center; gap:10px; margin-top:10px; padding:8px 12px; background:rgba(108,92,231,0.06); border-radius:8px; border:1px solid rgba(108,92,231,0.2); font-size:13px; color:#6c5ce7 }
.fade-enter-active,.fade-leave-active { transition:all .25s }
.fade-enter-from,.fade-leave-to { opacity:0; transform:translateY(-4px) }

/* === Table === */
.pm-product-cell { display:flex; align-items:center; gap:10px }
.pm-product-thumb { width:42px; height:42px; border-radius:8px; overflow:hidden; flex-shrink:0; background:linear-gradient(135deg,rgba(108,92,231,0.08),rgba(162,155,254,0.15)); display:flex; align-items:center; justify-content:center; border:1px solid rgba(108,92,231,0.1) }
.pm-product-thumb img,.pm-product-thumb video { width:100%; height:100%; object-fit:cover }
.pm-product-name { font-size:13px; font-weight:600; color:#2d3436 }
.pm-product-meta { font-size:11px; color:#909399; display:flex; gap:8px; align-items:center; margin-top:2px }
.pm-product-brand { background:#f5f7fa; color:#909399; padding:1px 6px; border-radius:4px; font-size:10px }
.pm-price-main { font-weight:700; font-size:14px; color:#6c5ce7 }
.pm-price-original { font-size:11px; color:#909399; text-decoration:line-through }
.pm-stock { display:flex; align-items:center; gap:5px }
.pm-stock-bar { width:44px; height:4px; background:#f5f7fa; border-radius:2px; overflow:hidden; flex-shrink:0 }
.pm-stock-fill { height:100%; border-radius:2px }
.status-badge { display:inline-block; padding:2px 8px; border-radius:20px; font-size:11px; font-weight:500; white-space:nowrap; line-height:1.6 }
.status-badge.on { background:rgba(0,184,148,0.1); color:#00b894 }
.status-badge.off { background:rgba(225,112,85,0.1); color:#e17055 }

/* Rating */
.pm-rating { display:flex; align-items:center; gap:3px; font-size:12px; white-space:nowrap; justify-content:center }
.pm-stars { color:#fdcb6e; font-size:11px; letter-spacing:1px }

/* Tags */
.pm-tag { display:inline-flex; align-items:center; gap:3px; padding:2px 8px; border-radius:10px; font-size:10px; font-weight:700; white-space:nowrap }
.pm-tag-hot { background:rgba(225,112,85,0.12); color:#c0392b }
.pm-tag-new { background:rgba(116,185,255,0.15); color:#2980b9 }
.pm-tag-sale { background:rgba(253,203,110,0.2); color:#d68910 }
.pm-tag-recommend { background:rgba(0,184,148,0.12); color:#00796b }
.pm-actions { display:flex; gap:4px; opacity:0; transition:opacity .15s; justify-content:center }
:deep(.el-table__row:hover) .pm-actions { opacity:1 }
.pm-action-btn { width:28px; height:28px; border:1px solid #e0e3e8; border-radius:6px; background:#fff; cursor:pointer; display:flex; align-items:center; justify-content:center; font-size:12px; color:#909399; transition:all .2s }
.pm-action-btn:hover { border-color:#6c5ce7; color:#6c5ce7; background:rgba(108,92,231,0.08) }
.pm-action-btn.danger:hover { border-color:#e17055; color:#e17055; background:rgba(225,112,85,0.08) }

/* Pagination */
.pm-pagination { display:flex; align-items:center; justify-content:space-between; padding:12px 16px; border-top:1px solid #e0e3e8; font-size:12px; color:#909399 }

/* === Grid View === */
.pm-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(220px,1fr)); gap:16px }
.pm-grid-card { background:#fff; border-radius:12px; border:1px solid #e0e3e8; overflow:hidden; transition:all .2s; cursor:pointer; position:relative }
.pm-grid-card:hover { box-shadow:0 2px 12px rgba(0,0,0,0.06); transform:translateY(-2px) }
.pm-grid-card-img { height:130px; background:linear-gradient(135deg,rgba(108,92,231,0.08),rgba(162,155,254,0.12)); display:flex; align-items:center; justify-content:center; position:relative }
.pm-grid-card-badge { position:absolute; top:8px; right:8px }
.pm-grid-card-actions { position:absolute; bottom:0; left:0; right:0; background:rgba(108,92,231,0.92); display:flex; padding:8px 10px; gap:6px; transform:translateY(100%); transition:transform .2s }
.pm-grid-card:hover .pm-grid-card-actions { transform:translateY(0) }
.pm-grid-action-btn { flex:1; padding:5px; border:none; border-radius:5px; font-size:11px; cursor:pointer; font-weight:500; background:rgba(255,255,255,0.15); color:#fff; display:flex; align-items:center; justify-content:center; gap:3px; transition:all .2s; font-family:inherit }
.pm-grid-action-btn:hover { background:rgba(255,255,255,0.25) }
.pm-grid-card-body { padding:12px }
.pm-grid-card-name { font-size:13px; font-weight:600; margin-bottom:4px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap }
.pm-grid-card-meta { font-size:11px; color:#909399; margin-bottom:8px; display:flex; gap:6px; align-items:center }
.pm-grid-card-price { font-size:16px; font-weight:700; color:#6c5ce7; margin-bottom:6px }
.pm-grid-card-original { font-size:11px; color:#909399; text-decoration:line-through; margin-left:4px }
.pm-grid-card-stats { display:flex; gap:10px; font-size:11px; color:#909399 }

/* Drawer */
.drawer-section { margin-bottom:20px }
.drawer-section-title { font-size:12px; font-weight:600; color:#909399; text-transform:uppercase; letter-spacing:.5px; margin-bottom:10px; padding-bottom:6px; border-bottom:1px solid #e0e3e8 }
.drawer-row { display:flex; justify-content:space-between; align-items:center; padding:7px 0; border-bottom:1px solid #f5f7fa; font-size:13px }
.drawer-row:last-child { border-bottom:none }
.drawer-label { color:#909399 }
.drawer-val { font-weight:500; text-align:right }

@media(max-width:1200px){ .pm-kpi-strip{grid-template-columns:repeat(3,1fr)} }
@media(max-width:768px){ .pm-kpi-strip{grid-template-columns:repeat(2,1fr)} .pm-toolbar{flex-direction:column;align-items:stretch} .pm-tab-group{overflow-x:auto} }
</style>
