<template>
  <div class="pm-page">
    <!-- Header -->
    <div class="page-header">
      <h2>产品管理</h2>
      <div class="page-header-actions">
        <el-button plain><el-icon :size="14"><Upload /></el-icon> 批量导入</el-button>
        <el-button plain><el-icon :size="14"><Download /></el-icon> 导出</el-button>
        <el-button type="primary" @click="$router.push('/admin/products/add')"><el-icon :size="14"><Plus /></el-icon> 新增产品</el-button>
      </div>
    </div>

    <!-- Toolbar Card -->
    <div class="card" style="margin-bottom:16px">
      <div class="card-body" style="padding:12px 16px">
        <!-- Status tabs -->
        <div class="pm-toolbar">
          <div class="pm-tab-group">
            <button class="pm-tab" :class="{ active: statusFilter === 'all' }" @click="statusFilter='all';page=1;load()">
              全部 <span class="pm-tab-count" v-if="tabCounts.all !== undefined">{{ tabCounts.all }}</span>
            </button>
            <button class="pm-tab" :class="{ active: statusFilter === 'ON' }" @click="statusFilter='ON';page=1;load()">
              在售 <span class="pm-tab-count" v-if="tabCounts.ON !== undefined">{{ tabCounts.ON }}</span>
            </button>
            <button class="pm-tab" :class="{ active: statusFilter === 'OFF' }" @click="statusFilter='OFF';page=1;load()">
              已下架 <span class="pm-tab-count" v-if="tabCounts.OFF !== undefined">{{ tabCounts.OFF }}</span>
            </button>
          </div>
          <div class="pm-toolbar-right">
            <div class="pm-search-wrap">
              <el-icon :size="12"><Search /></el-icon>
              <input v-model="searchKeyword" placeholder="搜索产品名、SKU..." @input="onSearch" />
            </div>
            <select class="pm-select" v-model="catFilter" @change="page=1;load()">
              <option value="">全部分类</option>
              <option v-for="c in allCats" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>
        </div>

        <!-- Batch bar -->
        <transition name="fade">
          <div class="pm-batch-bar" v-if="selectedIds.length">
            <span>已选 <strong>{{ selectedIds.length }}</strong> 个产品</span>
            <div style="display:flex;gap:8px;margin-left:auto">
              <el-button size="small" @click="batchStatus('ON')">上架</el-button>
              <el-button size="small" @click="batchStatus('OFF')">下架</el-button>
              <el-button size="small" type="danger" plain @click="batchDelete">批量删除</el-button>
            </div>
          </div>
        </transition>
      </div>
    </div>

    <!-- Table -->
    <div class="card">
      <div class="card-body" style="padding:0">
        <el-table
          :data="products" stripe v-loading="loading" row-key="id" size="small"
          @selection-change="onSelectChange" ref="tableRef"
        >
          <el-table-column type="selection" width="40" />
          <el-table-column label="产品" min-width="240">
            <template #default="{row}">
              <div class="pm-product-cell">
                <div class="pm-product-thumb">
                  <video v-if="isVideo(coverMedia(row))" :src="coverMedia(row)" muted />
                  <img v-else-if="coverMedia(row)" :src="coverMedia(row)" />
                  <el-icon v-else :size="17" color="#a29bfe"><PictureFilled /></el-icon>
                </div>
                <div>
                  <div class="pm-product-name">{{ row.name }}</div>
                  <div class="pm-product-meta">
                    <span>SKU: {{ row.sku || row.id }}</span>
                    <span v-if="row.originalPrice > row.price" style="color:#e17055;font-weight:700;font-size:10px">
                      -{{ Math.round((1 - row.price/row.originalPrice) * 100) }}%
                    </span>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="分类" width="100">
            <template #default="{row}">{{ catMap[row.categoryId] || '-' }}</template>
          </el-table-column>
          <el-table-column label="价格" width="100" sortable prop="price">
            <template #default="{row}">
              <div class="pm-price">¥{{ Number(row.price).toFixed(2) }}</div>
              <div v-if="row.originalPrice > row.price" class="pm-original">¥{{ Number(row.originalPrice).toFixed(2) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="库存" width="110" sortable prop="stock">
            <template #default="{row}">
              <div class="pm-stock">
                <div class="pm-stock-bar"><div class="pm-stock-fill" :style="{ width: stockPct(row)+'%', background: stockColor(row) }"></div></div>
                <span :style="{ fontSize:'12px',fontWeight:600,color:stockColor(row) }">{{ row.stock === 0 ? '售罄' : row.stock }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="销量" width="80" sortable prop="sales" align="center">
            <template #default="{row}"><span style="font-weight:600">{{ row.sales || 0 }}</span></template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template #default="{row}">
              <span class="status-badge" :class="row.status==='ON'?'on':'off'">{{ row.status==='ON'?'在售':'下架' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="130" fixed="right" align="center">
            <template #default="{row}">
              <div class="pm-actions">
                <button class="pm-action-btn" title="编辑" @click="$router.push(`/admin/products/${row.id}`)"><el-icon :size="13"><EditPen /></el-icon></button>
                <button class="pm-action-btn" title="复制"><el-icon :size="13"><DocumentCopy /></el-icon></button>
                <button class="pm-action-btn danger" title="删除" @click="handleDelete(row)"><el-icon :size="13"><Delete /></el-icon></button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && products.length===0" description="暂无产品" :image-size="64" style="padding:40px 0" />
      </div>
      <div class="pm-pagination" v-if="total>0">
        <span>显示 {{ (page-1)*size+1 }}-{{ Math.min(page*size, total) }} 共 {{ total }} 条</span>
        <el-pagination
          v-model:current-page="page" :page-size="size" :page-sizes="[10, 20, 50]"
          :total="total" layout="sizes, prev, pager, next"
          @size-change="s=>{size=s;page=1;load()}" @current-change="load" background small
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminProducts, getAdminCategories, updateProductStatus, deleteProduct, batchDeleteProducts } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload, Download, Search, EditPen, DocumentCopy, Delete, PictureFilled } from '@element-plus/icons-vue'

const products = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const statusFilter = ref('all')
const searchKeyword = ref('')
const catFilter = ref('')
const selectedIds = ref([])
const allCats = ref([])
const catMap = ref({})
const tabCounts = ref({})
const tableRef = ref(null)
let searchTimer = null

onMounted(() => { load(); loadCategories() })

async function loadCategories() {
  try {
    const res = await getAdminCategories()
    const all = res.data || []
    allCats.value = all
    const map = {}
    all.forEach(c => { map[c.id] = c.name })
    catMap.value = map
  } catch {}
}

function getStockAlert(sales) { return sales > 500 ? 30 : sales > 100 ? 10 : 5 }
function stockPct(row) {
  const alert = getStockAlert(row.sales)
  return Math.min(100, Math.round(row.stock / (alert * 4) * 100))
}
function stockColor(row) {
  if (row.stock === 0) return '#e17055'
  const alert = getStockAlert(row.sales)
  return row.stock <= alert ? '#fdcb6e' : '#00b894'
}

function parseJsonField(val) {
  if (!val) return []
  if (Array.isArray(val)) return val
  try { return JSON.parse(val) } catch { return [] }
}
function isVideo(url) { return url && /\.mp4$/i.test(url) }
function coverMedia(row) {
  const videos = parseJsonField(row.videos)
  if (videos.length > 0) return videos[0]
  const imgs = parseJsonField(row.images)
  if (imgs.length > 0) return imgs[0]
  return row.coverImage || null
}

function onSelectChange(rows) { selectedIds.value = rows.map(r => r.id) }
function onSearch() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { page.value = 1; load() }, 300)
}

async function load() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (statusFilter.value !== 'all') params.status = statusFilter.value
    const res = await getAdminProducts(params)
    let list = res.data?.list || []
    total.value = res.data?.total || 0
    // client-side category filter
    if (catFilter.value) list = list.filter(p => p.categoryId == catFilter.value)
    // count tabs
    if (statusFilter.value === 'all') {
      tabCounts.value.all = res.data?.total || 0
    }
    products.value = list
    selectedIds.value = []
  } catch {} finally { loading.value = false }
}

async function batchStatus(st) {
  try {
    await Promise.all(selectedIds.value.map(id => updateProductStatus(id, st)))
    ElMessage.success(`已${st === 'ON' ? '上架' : '下架'} ${selectedIds.value.length} 个产品`)
    load()
  } catch {}
}
async function batchDelete() {
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个产品？`, '批量删除', { type: 'warning' })
    await batchDeleteProducts(selectedIds.value)
    ElMessage.success('已删除')
    load()
  } catch {}
}
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.name}」？`, '删除', { type: 'warning', confirmButtonText: '确定' })
    await deleteProduct(row.id)
    ElMessage.success('已删除')
    load()
  } catch {}
}
</script>

<style scoped>
/* ===== Layout ===== */
.pm-page { max-width: 100%; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 20px; font-weight: 600; margin: 0; }
.page-header-actions { display: flex; gap: 8px; }

.card { background: #fff; border-radius: 12px; border: 1px solid #e0e3e8; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.card-body { padding: 20px; }

/* ===== Toolbar ===== */
.pm-toolbar { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.pm-tab-group { display: flex; gap: 2px; border-bottom: 2px solid #e0e3e8; flex: 1; }
.pm-tab {
  padding: 7px 14px; font-size: 13px; font-weight: 500; border: none; background: transparent;
  cursor: pointer; color: #909399; border-bottom: 2px solid transparent; margin-bottom: -2px;
  border-radius: 6px 6px 0 0; white-space: nowrap; font-family: inherit; transition: all .2s;
}
.pm-tab:hover { color: #606266; background: #f5f7fa; }
.pm-tab.active { color: #6c5ce7; border-bottom-color: #6c5ce7; background: rgba(108,92,231,0.08); }
.pm-tab-count {
  background: #f5f7fa; color: #909399; font-size: 10px; padding: 1px 6px;
  border-radius: 8px; margin-left: 4px; font-weight: 600;
}
.pm-tab.active .pm-tab-count { background: rgba(108,92,231,0.15); color: #6c5ce7; }
.pm-toolbar-right { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.pm-search-wrap { position: relative; display: flex; align-items: center; }
.pm-search-wrap :deep(.el-icon) { position: absolute; left: 10px; color: #909399; z-index: 1; }
.pm-search-wrap input {
  padding: 7px 10px 7px 30px; border: 1px solid #e0e3e8; border-radius: 20px;
  font-size: 12px; outline: none; width: 200px; background: #f5f7fa; font-family: inherit;
  transition: all .2s;
}
.pm-search-wrap input:focus { border-color: #6c5ce7; background: #fff; box-shadow: 0 0 0 3px rgba(108,92,231,0.08); }
.pm-select {
  padding: 7px 10px; border: 1px solid #e0e3e8; border-radius: 8px; font-size: 12px;
  outline: none; background: #f5f7fa; color: #606266; cursor: pointer; font-family: inherit; transition: all .2s;
}
.pm-select:focus { border-color: #6c5ce7; }

/* Batch bar */
.pm-batch-bar {
  display: flex; align-items: center; gap: 10px; margin-top: 10px;
  padding: 8px 12px; background: rgba(108,92,231,0.06); border-radius: 8px;
  border: 1px solid rgba(108,92,231,0.2); font-size: 13px; color: #6c5ce7;
}
.fade-enter-active, .fade-leave-active { transition: all .25s; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: translateY(-4px); }

/* ===== Table ===== */
.pm-product-cell { display: flex; align-items: center; gap: 10px; }
.pm-product-thumb {
  width: 42px; height: 42px; border-radius: 8px; overflow: hidden; flex-shrink: 0;
  background: linear-gradient(135deg, rgba(108,92,231,0.08), rgba(162,155,254,0.15));
  display: flex; align-items: center; justify-content: center;
  border: 1px solid rgba(108,92,231,0.1);
}
.pm-product-thumb img, .pm-product-thumb video { width: 100%; height: 100%; object-fit: cover; }
.pm-product-name { font-size: 13px; font-weight: 600; color: #2d3436; }
.pm-product-meta { font-size: 11px; color: #909399; display: flex; gap: 8px; align-items: center; margin-top: 2px; }

/* Price */
.pm-price { font-weight: 700; font-size: 14px; color: #6c5ce7; }
.pm-original { font-size: 11px; color: #909399; text-decoration: line-through; }

/* Stock */
.pm-stock { display: flex; align-items: center; gap: 5px; }
.pm-stock-bar { width: 44px; height: 4px; background: #f5f7fa; border-radius: 2px; overflow: hidden; flex-shrink: 0; }
.pm-stock-fill { height: 100%; border-radius: 2px; }

/* Status */
.status-badge {
  display: inline-block; padding: 2px 8px; border-radius: 20px;
  font-size: 11px; font-weight: 500; white-space: nowrap; line-height: 1.6;
}
.status-badge.on { background: rgba(0,184,148,0.1); color: #00b894; }
.status-badge.off { background: rgba(225,112,85,0.1); color: #e17055; }

/* Actions */
.pm-actions { display: flex; gap: 4px; opacity: 0; transition: opacity .15s; justify-content: center; }
:deep(.el-table__row:hover) .pm-actions { opacity: 1; }
.pm-action-btn {
  width: 28px; height: 28px; border: 1px solid #e0e3e8; border-radius: 6px;
  background: #fff; cursor: pointer; display: flex; align-items: center; justify-content: center;
  font-size: 12px; color: #909399; transition: all .2s;
}
.pm-action-btn:hover { border-color: #6c5ce7; color: #6c5ce7; background: rgba(108,92,231,0.08); }
.pm-action-btn.danger:hover { border-color: #e17055; color: #e17055; background: rgba(225,112,85,0.08); }

/* Pagination */
.pm-pagination {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 16px; border-top: 1px solid #e0e3e8; font-size: 12px; color: #909399;
}

@media (max-width: 768px) {
  .pm-toolbar { flex-direction: column; align-items: stretch; }
  .pm-tab-group { overflow-x: auto; }
}
</style>
