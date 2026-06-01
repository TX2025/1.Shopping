<template>
  <div class="cat-mg">
    <!-- 左侧边栏开关 -->
    <div class="sidebar-toggle" @click="sidebarOpen = !sidebarOpen" v-if="!sidebarOpen">
      <el-icon :size="18"><Menu /></el-icon>
    </div>

    <!-- 遮罩层 -->
    <div v-if="sidebarOpen && isMobile" class="overlay" @click="sidebarOpen = false"></div>

    <!-- 左侧：分类面板 -->
    <aside class="cat-sidebar" :class="{ open: sidebarOpen }">
      <div class="sidebar-card">
        <div class="sidebar-hd">
          <h3 class="sidebar-title">分类目录</h3>
          <div class="sidebar-hd-actions">
            <el-button :icon="Close" size="small" circle class="btn-close-sidebar" @click="sidebarOpen = false" v-if="isMobile" />
          </div>
        </div>

        <div class="category-search">
          <el-icon :size="13"><Search /></el-icon>
          <input
            v-model="catSearch"
            placeholder="搜索分类..."
          />
        </div>

        <div class="tree-actions">
          <button class="btn-tree-action" @click="openCatAdd(null)"><el-icon :size="13"><Plus /></el-icon> 分类</button>
          <button class="btn-tree-action" @click="openCatAdd(currentCat)"><el-icon :size="13"><FolderAdd /></el-icon> 子分类</button>
        </div>

        <div class="tree-wrap">
          <el-tree
            :data="filteredTree"
            :props="{ children: 'children', label: 'name' }"
            node-key="id"
            highlight-current
            default-expand-all
            :expand-on-click-node="false"
            :filter-node-method="() => true"
            @node-click="onTreeSelect"
            ref="treeRef"
          >
            <template #default="{ data }">
              <span class="tree-node-row">
                <el-icon :size="16" class="tree-icon"><component :is="getCatIcon(data.icon)" /></el-icon>
                <span class="tree-name">{{ data.name }}</span>
                <span class="tree-badge" v-if="counts[data.id] !== undefined">{{ counts[data.id] }}</span>
                <span class="tree-node-actions">
                  <el-button link size="small" @click.stop="openCatAdd(data)" title="新增子分类">
                    <el-icon :size="14"><Plus /></el-icon>
                  </el-button>
                  <el-button link size="small" @click.stop="openCatEdit(data)" title="编辑">
                    <el-icon :size="14"><EditPen /></el-icon>
                  </el-button>
                  <el-button link size="small" class="btn-danger" @click.stop="handleCatDelete(data)" title="删除">
                    <el-icon :size="14"><Delete /></el-icon>
                  </el-button>
                </span>
              </span>
            </template>
          </el-tree>

          <el-empty v-if="filteredTree.length === 0 && catSearch" description="无匹配分类" :image-size="48" />
        </div>
      </div>
    </aside>

    <!-- 右侧：产品面板 -->
    <main class="cat-main">
      <!-- 头部工具栏 -->
      <div class="main-toolbar">
        <div class="toolbar-left">
          <el-button v-if="!sidebarOpen" :icon="Menu" size="small" @click="sidebarOpen = true" />
          <h2 class="toolbar-title">
            <span class="title-icon">#</span>
            {{ currentCat ? currentCat.name : '全部产品' }}
            <el-tag size="small" round class="title-count">{{ total }} 个产品</el-tag>
          </h2>
        </div>
          <div class="toolbar-right">
            <el-input
              v-model="prodKeyword"
              placeholder="搜索产品名称..."
              :prefix-icon="Search"
              size="small"
              clearable
              class="prod-search"
              @keyup.enter="page=1;loadProducts()"
              @clear="page=1;loadProducts()"
            />
            <el-button size="small" type="primary" @click="openProdAdd">
              <el-icon :size="14"><Plus /></el-icon>
              新增产品
            </el-button>
            <el-divider direction="vertical" />
            <el-button v-if="currentCat" size="small" @click="openMoveDialog(null)" :disabled="!selectedIds.length">
              <el-icon :size="14"><Switch /></el-icon>
              批量移动
            </el-button>
            <el-button size="small" type="danger" plain @click="handleBatchDelete" :disabled="!selectedIds.length">
              <el-icon :size="14"><Delete /></el-icon>
              批量删除
            </el-button>
          </div>
        </div>

        <!-- 筛选 + 操作提示条 -->
        <div class="product-toolbar">
          <div class="product-toolbar-left">
            <button class="filter-chip" :class="{ active: statusFilter === 'all' }" @click="statusFilter='all';page=1;loadProducts()">全部</button>
            <button class="filter-chip" :class="{ active: statusFilter === 'ON' }" @click="statusFilter='ON';page=1;loadProducts()">在售</button>
            <button class="filter-chip" :class="{ active: statusFilter === 'OFF' }" @click="statusFilter='OFF';page=1;loadProducts()">下架</button>
          </div>
          <transition name="fade">
            <div v-if="selectedIds.length" class="selection-bar">
              <el-icon :size="16"><InfoFilled /></el-icon>
              已选择 <strong>{{ selectedIds.length }}</strong> 个产品
              <el-button link size="small" @click="clearSelection">取消选择</el-button>
            </div>
          </transition>
        </div>

        <!-- 产品表格 -->
        <div class="table-card" v-loading="prodLoading">
          <el-table
            :data="products"
            @selection-change="onSelectionChange"
            ref="tableRef"
            stripe
            size="small"
            row-key="id"
          >
            <el-table-column type="selection" width="40" />
            <el-table-column label="产品" min-width="200">
              <template #default="{row}">
                <div class="product-name-cell">
                  <div class="product-img">
                    <video v-if="isVideo(coverMedia(row))" :src="coverMedia(row)" muted />
                    <img v-else-if="coverMedia(row)" :src="coverMedia(row)" />
                    <el-icon v-else :size="18" color="#bfcbd9"><PictureFilled /></el-icon>
                  </div>
                  <div>
                    <div class="product-name">{{ row.name }}</div>
                    <div class="product-sku">SKU: {{ row.id }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="分类" width="100">
              <template #default="{row}">{{ currentCat?.name || getCatName(row.categoryId) }}</template>
            </el-table-column>
            <el-table-column label="价格" width="100" sortable prop="price">
              <template #default="{row}"><span style="font-weight:500">¥{{ Number(row.price).toFixed(2) }}</span></template>
            </el-table-column>
            <el-table-column label="库存" width="80" prop="stock" align="center">
              <template #default="{row}"><span :class="row.stock <= 0 ? 'stock-zero' : ''">{{ row.stock }}</span></template>
            </el-table-column>
            <el-table-column label="销量" width="80" prop="sales" align="center" />
            <el-table-column label="状态" width="90" align="center">
              <template #default="{row}">
                <span class="status-badge" :class="row.status==='ON'?'on':'off'">{{ row.status==='ON'?'在售':'下架' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" fixed="right" align="center">
              <template #default="{row}">
                <div class="table-actions">
                  <button class="action-btn" title="编辑" @click="openProdEdit(row)"><el-icon :size="13"><EditPen /></el-icon></button>
                  <button class="action-btn" title="移动" @click="openMoveDialog(row)"><el-icon :size="13"><Switch /></el-icon></button>
                  <button class="action-btn danger" title="删除" @click="handleProdDelete(row)"><el-icon :size="13"><Delete /></el-icon></button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="!prodLoading && products.length === 0" description="暂无产品" :image-size="48" style="padding:40px 0" />
        </div>

        <!-- 分页 -->
        <div class="pagination" v-if="total > 0">
          <span class="pagination-info">显示 {{ (page-1)*size+1 }}-{{ Math.min(page*size, total) }} 共 {{ total }} 条</span>
          <el-pagination
            v-model:current-page="page"
            :page-size="size"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="sizes, prev, pager, next, jumper"
            @size-change="onSizeChange"
            @current-change="loadProducts"
            background
            small
          />
        </div>
    </main>

    <!-- ==================== 弹窗 ==================== -->

    <!-- 分类编辑 -->
    <el-dialog v-model="catDialogVisible" :title="catEditId ? '编辑分类' : '新增分类'" width="520px" destroy-on-close>
      <el-form :model="catForm" label-width="80px" label-position="top">
        <el-row :gutter="12">
          <el-col :span="16"><el-form-item label="分类名称">
            <el-input v-model="catForm.name" placeholder="输入分类名称" />
          </el-form-item></el-col>
          <el-col :span="8"><el-form-item label="图标">
            <el-select v-model="catForm.icon" placeholder="选择图标" clearable style="width:100%">
              <template #prefix>
                <el-icon :size="14" v-if="catForm.icon"><component :is="iconMap[catForm.icon]" /></el-icon>
              </template>
              <el-option v-for="k in iconOptions" :key="k" :label="k" :value="k">
                <span style="display:flex;align-items:center;gap:8px">
                  <el-icon :size="14"><component :is="iconMap[k]" /></el-icon>
                  {{ k }}
                </span>
              </el-option>
            </el-select>
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="上级分类">
          <el-select v-model="catForm.parentId" placeholder="无（顶级分类）" clearable style="width:100%">
            <el-option v-for="c in parentCatOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="catForm.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="封面图">
          <div class="cover-row">
            <div class="cover-box" @click="triggerCoverUpload">
              <img v-if="catForm.coverImage" :src="catForm.coverImage" class="cover-img" />
              <el-icon v-else :size="24" color="#c0c4cc"><Plus /></el-icon>
            </div>
            <input type="file" ref="coverInput" accept="image/jpeg,image/png,image/gif" hidden @change="handleCoverUpload" />
            <div class="cover-url">
              <el-button size="small" @click="triggerCoverUpload">上传图片</el-button>
              <el-input v-model="catForm.coverImage" placeholder="或粘贴图片URL" size="small" style="width:200px" />
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="catDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCat" :loading="catSaving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 产品编辑 -->
    <el-dialog v-model="prodDialogVisible" :title="prodEditId ? '编辑产品' : '新增产品'" width="700px" destroy-on-close>
      <el-form :model="prodForm" label-width="90px" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="产品名称">
              <el-input v-model="prodForm.name" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="SKU">
              <el-input v-model="prodForm.sku" placeholder="如 BT-EAR-001" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态">
              <el-switch v-model="prodForm.status" active-value="ON" inactive-value="OFF" inline-prompt active-text="上架" inactive-text="下架" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="prodForm.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="价格">
              <el-input-number v-model="prodForm.price" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="原价">
              <el-input-number v-model="prodForm.originalPrice" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库存">
              <el-input-number v-model="prodForm.stock" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="产品图片">
          <el-upload
            :action="uploadAction"
            :headers="uploadHeaders"
            list-type="picture-card"
            :file-list="prodImageFileList"
            :before-upload="beforeUploadImage"
            :on-success="onProdImageUpload"
            :on-remove="onProdImageRemove"
            accept=".jpg,.jpeg,.png,.gif"
          >
            <el-icon :size="28"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="产品视频">
          <el-upload
            :action="uploadAction"
            :headers="uploadHeaders"
            :file-list="prodVideoFileList"
            :before-upload="beforeUploadVideo"
            :on-success="onProdVideoUpload"
            :on-remove="onProdVideoRemove"
            accept=".mp4"
            drag
          >
            <el-icon :size="32"><UploadFilled /></el-icon>
            <div style="margin-top:8px">拖拽或点击上传 MP4 视频</div>
          </el-upload>
          <div v-if="prodForm.videos.length" style="margin-top:8px;display:flex;gap:10px;flex-wrap:wrap">
            <div v-for="(v, i) in prodForm.videos" :key="i" style="position:relative">
              <video :src="v" controls muted style="width:160px;height:90px;object-fit:cover;border-radius:6px" />
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="prodDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProd" :loading="prodSaving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 移动分类 -->
    <el-dialog v-model="moveDialogVisible" title="移动到分类" width="420px">
      <p class="move-hint">选择产品要移动到的目标分类：</p>
      <el-tree
        :data="treeData"
        :props="{ children: 'children', label: 'name' }"
        node-key="id"
        highlight-current
        default-expand-all
        @node-click="onMoveTargetSelect"
        ref="moveTreeRef"
      />
      <div class="move-selected" v-if="moveTarget">
        <el-icon :size="16"><Check /></el-icon>
        目标分类：<strong>{{ moveTarget.name }}</strong>
      </div>
      <template #footer>
        <el-button @click="moveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmMove" :disabled="!moveTarget || (currentCat && moveTarget.id === currentCat.id)">
          确认移动
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import {
  getAdminCategories, createCategory, updateCategory, deleteCategory,
  getCategoryProducts, getAdminProducts, getCategoryProductCounts, createProduct, updateProduct, deleteProduct, updateProductStatus,
  batchMoveProducts, batchDeleteProducts, uploadFile
} from '../../api/admin'
import { useAuthStore } from '../../stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, EditPen, Delete, Search, Close, Menu, Switch,
  InfoFilled, FolderOpened, FolderAdd, PictureFilled, Check,
  Monitor, Iphone, Headset, Present, User, UserFilled, HomeFilled,
  Camera, ShoppingBag, OfficeBuilding, Star, ShoppingCartFull,
  Box, CreditCard, Timer, AlarmClock, Van, ChatDotRound, ChatLineRound,
  DataAnalysis, PieChart, Bell, Phone, Message, VideoCamera, Film,
  Picture, Document, Files, CollectionTag, PriceTag, Link,
  Connection, DataBoard, Management, HelpFilled, Suitcase, TrendCharts,
  School, KnifeFork, Sell, Share
} from '@element-plus/icons-vue'

const authStore = useAuthStore()
const uploadAction = '/api/admin/upload'
const uploadHeaders = computed(() => ({
  Authorization: authStore.token ? `Bearer ${authStore.token}` : ''
}))

// ======== Helpers (consistent with ProductManage) ========
function parseJsonField(val) {
  if (!val) return []
  if (Array.isArray(val)) return val
  try { return JSON.parse(val) } catch { return [] }
}

function isVideo(url) { return url && /\.mp4$/i.test(url) }
function vidCount(row) { return parseJsonField(row.videos).length }

function coverMedia(row) {
  const videos = parseJsonField(row.videos)
  if (videos.length > 0) return videos[0]
  const imgs = parseJsonField(row.images)
  if (imgs.length > 0) return imgs[0]
  return row.coverImage || null
}

// ======== Category icon mapping ========
const iconMap = {
  Monitor, Iphone, Headset, Present, User, UserFilled, HomeFilled, Suitcase, KnifeFork,
  Camera, ShoppingBag, OfficeBuilding, Star, CollectionTag,
  ShoppingCartFull, Box, CreditCard, Timer, AlarmClock,
  Van, ChatDotRound, ChatLineRound, DataAnalysis, TrendCharts, PieChart,
  Bell, Phone, Message, VideoCamera, Film, Picture, Document, Files,
  FolderOpened, PriceTag, Link, Connection,
  DataBoard, Management, HelpFilled, School, Sell, Share,
  Goods: ShoppingBag, Setting: Switch
}
const iconOptions = Object.keys(iconMap).filter(k => !['Goods','Setting'].includes(k))

function getCatIcon(iconName) {
  // fallback old icon names to new ones
  const alias = { House: HomeFilled, Dishwasher: Suitcase, Collection: CollectionTag, ShoppingCart: ShoppingCartFull, ChatDotSquare: ChatDotRound, ChatLineSquare: ChatLineRound, Histogram: TrendCharts, Files: Document }
  const name = alias[iconName] || iconName
  return iconMap[name] || FolderOpened
}

// ======== Layout ========
const sidebarOpen = ref(true)
const isMobile = ref(window.innerWidth <= 768)
window.addEventListener('resize', () => {
  isMobile.value = window.innerWidth <= 768
  if (!isMobile.value) sidebarOpen.value = true
})

// ======== Categories ========
const treeRef = ref(null)
const treeData = ref([])
const allCats = ref([])
const currentCat = ref(null)
const catSearch = ref('')
const counts = ref({})

const parentCatOptions = computed(() => allCats.value.filter(c => c.id !== catEditId.value))

const filteredTree = computed(() => {
  if (!catSearch.value.trim()) return treeData.value
  const kw = catSearch.value.toLowerCase()
  return treeData.value.filter(c => matchTree(c, kw)).map(c => pruneTree(c, kw)).filter(Boolean)
})

function matchTree(node, kw) {
  if (node.name.toLowerCase().includes(kw)) return true
  if (node.children) return node.children.some(c => matchTree(c, kw))
  return false
}
function pruneTree(node, kw) {
  const nameMatch = node.name.toLowerCase().includes(kw)
  if (node.children) {
    const filtered = node.children.map(c => pruneTree(c, kw)).filter(Boolean)
    return { ...node, children: filtered }
  }
  return nameMatch ? node : null
}

watch(catSearch, (v) => {
  treeRef.value?.filter(v)
})

async function loadCategories() {
  try {
    const res = await getAdminCategories()
    const raw = res.data || []
    // Build tree from flat list using parentId
    allCats.value = raw
    const map = {}
    const roots = []
    raw.forEach(c => { map[c.id] = { ...c, children: [] } })
    raw.forEach(c => {
      if (c.parentId && map[c.parentId]) {
        map[c.parentId].children.push(map[c.id])
      } else if (!c.parentId) {
        roots.push(map[c.id])
      }
    })
    treeData.value = roots.length > 0 ? roots : raw
  } catch {}
}

function getCatName(catId) {
  if (!catId) return '-'
  const cat = allCats.value.find(c => c.id === catId)
  return cat ? cat.name : '-'
}
function onTreeSelect(data) {
  currentCat.value = data
  page.value = 1
  prodKeyword.value = ''
  selectedIds.value = []
  loadProducts()
}

// ======== Products ========
const tableRef = ref(null)
const products = ref([])
const prodLoading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const selectedIds = ref([])
const prodKeyword = ref('')
const statusFilter = ref('all')

function onSelectionChange(rows) { selectedIds.value = rows.map(r => r.id) }
function clearSelection() {
  selectedIds.value = []
  tableRef.value?.clearSelection()
}

async function loadProducts() {
  prodLoading.value = true
  try {
    let res
    if (currentCat.value) {
      res = await getCategoryProducts(currentCat.value.id, {
        page: page.value,
        size: size.value,
        keyword: prodKeyword.value || undefined,
        status: statusFilter.value !== 'all' ? statusFilter.value : undefined,
      })
    } else {
      res = await getAdminProducts({
        page: page.value,
        size: size.value,
        status: statusFilter.value !== 'all' ? statusFilter.value : undefined,
      })
    }
    products.value = res.data?.list || []
    total.value = res.data?.total || 0
    if (currentCat.value) counts.value[currentCat.value.id] = total.value
  } catch {} finally { prodLoading.value = false }
}

function onSizeChange(s) {
  size.value = s
  page.value = 1
  loadProducts()
}

// ======== Category CRUD ========
const catDialogVisible = ref(false)
const catEditId = ref(null)
const catForm = ref({ name: '', sortOrder: 0, parentId: null, coverImage: '' })
const catSaving = ref(false)
const coverInput = ref(null)

function openCatAdd(parent) {
  catEditId.value = null
  catForm.value = { name: '', sortOrder: 0, parentId: parent?.id || null, coverImage: '', icon: '' }
  catDialogVisible.value = true
}
function openCatEdit(data) {
  catEditId.value = data.id
  catForm.value = { name: data.name, sortOrder: data.sortOrder || 0, parentId: data.parentId || null, coverImage: data.coverImage || '', icon: data.icon || '' }
  catDialogVisible.value = true
}
function triggerCoverUpload() { coverInput.value?.click() }

async function handleCoverUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return
  try {
    const res = await uploadFile(file)
    if (res.data?.url) catForm.value.coverImage = res.data.url
    ElMessage.success('上传成功')
  } catch { ElMessage.error('上传失败') }
  e.target.value = ''
}

async function saveCat() {
  if (!catForm.value.name.trim()) { ElMessage.warning('请输入分类名称'); return }
  catSaving.value = true
  try {
    if (catEditId.value) {
      await updateCategory(catEditId.value, catForm.value)
      ElMessage.success('更新成功')
    } else {
      await createCategory(catForm.value)
      ElMessage.success('创建成功')
    }
    catDialogVisible.value = false
    await loadCategories(); loadCounts()
    if (catEditId.value && currentCat.value?.id === catEditId.value) {
      const u = allCats.value.find(c => c.id === catEditId.value)
      if (u) currentCat.value = u
    }
  } catch {} finally { catSaving.value = false }
}

async function handleCatDelete(data) {
  try {
    await ElMessageBox.confirm(
      `确定删除分类「${data.name}」？删除后该分类下产品不会删除，但将失去分类归属。`,
      '删除分类',
      { confirmButtonText: '确定删除', type: 'warning' }
    )
    await deleteCategory(data.id)
    ElMessage.success('删除成功')
    if (currentCat.value?.id === data.id) currentCat.value = null
    await loadCategories(); loadCounts()
  } catch {}
}

// ======== Product CRUD ========
const prodDialogVisible = ref(false)
const prodForm = reactive({
  name: '', sku: '', description: '', price: 0, originalPrice: 0, stock: 0,
  categoryId: null, status: 'ON', images: [], videos: [],
})
const prodEditId = ref(null)
const prodSaving = ref(false)

const prodImageFileList = computed(() =>
  prodForm.images.map((url, i) => ({ uid: i, name: `img-${i}`, url, status: 'success' }))
)
const prodVideoFileList = computed(() =>
  prodForm.videos.map((url, i) => ({ uid: i, name: `vid-${i}`, url, status: 'success' }))
)

function resetProdForm() {
  prodEditId.value = null
  prodForm.name = ''
  prodForm.sku = ''
  prodForm.description = ''
  prodForm.price = 0
  prodForm.originalPrice = 0
  prodForm.stock = 0
  prodForm.categoryId = currentCat.value?.id || null
  prodForm.status = 'ON'
  prodForm.images = []
  prodForm.videos = []
}

function openProdAdd() {
  resetProdForm()
  prodDialogVisible.value = true
}

function openProdEdit(row) {
  prodEditId.value = row.id
  prodForm.name = row.name
  prodForm.sku = row.sku || ''
  prodForm.description = row.description || ''
  prodForm.price = row.price
  prodForm.originalPrice = row.originalPrice || 0
  prodForm.stock = row.stock
  prodForm.status = row.status
  prodForm.categoryId = row.categoryId
  prodForm.images = parseJsonField(row.images)
  prodForm.videos = parseJsonField(row.videos)
  prodDialogVisible.value = true
}

// ======== Upload ========
function beforeUploadImage(file) {
  const ext = file.name.split('.').pop().toLowerCase()
  if (!['jpg', 'jpeg', 'png', 'gif'].includes(ext)) {
    ElMessage.error('仅支持 jpg/jpeg/png/gif 格式图片')
    return false
  }
  if (file.size > 10 * 1024 * 1024) { ElMessage.error('图片大小不能超过 10MB'); return false }
  return true
}
function beforeUploadVideo(file) {
  const ext = file.name.split('.').pop().toLowerCase()
  if (ext !== 'mp4') { ElMessage.error('仅支持 mp4 格式视频'); return false }
  if (file.size > 50 * 1024 * 1024) { ElMessage.error('视频大小不能超过 50MB'); return false }
  return true
}
function onProdImageUpload(res) {
  const url = res.data?.url || res.url
  if (url) prodForm.images.push(url)
}
function onProdImageRemove(file) {
  const idx = prodForm.images.indexOf(file.url)
  if (idx > -1) prodForm.images.splice(idx, 1)
}
function onProdVideoUpload(res) {
  const url = res.data?.url || res.url
  if (url) prodForm.videos.push(url)
}
function onProdVideoRemove(file) {
  const idx = prodForm.videos.indexOf(file.url)
  if (idx > -1) prodForm.videos.splice(idx, 1)
}

async function saveProd() {
  prodSaving.value = true
  try {
    const data = {
      ...prodForm,
      price: parseFloat(prodForm.price),
      originalPrice: parseFloat(prodForm.originalPrice) || null,
      sku: prodForm.sku || null,
      images: prodForm.images.length ? JSON.stringify(prodForm.images) : null,
      videos: prodForm.videos.length ? JSON.stringify(prodForm.videos) : null,
    }
    if (prodEditId.value) {
      await updateProduct(prodEditId.value, data)
    } else {
      await createProduct(data)
    }
    ElMessage.success(prodEditId.value ? '保存成功' : '创建成功')
    prodDialogVisible.value = false
    loadProducts(); loadCounts()
  } catch {} finally { prodSaving.value = false }
}

async function toggleStatus(row, val) {
  const status = val ? 'ON' : 'OFF'
  try {
    await updateProductStatus(row.id, status)
    row.status = status
    ElMessage.success(val ? '已上架' : '已下架')
  } catch {}
}

async function handleProdDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.name}」？此操作不可恢复。`, '删除产品', {
      confirmButtonText: '确定删除',
      type: 'warning'
    })
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    loadProducts(); loadCounts()
  } catch {}
}

async function handleBatchDelete() {
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个产品？此操作不可恢复。`, '批量删除', {
      confirmButtonText: '确定删除',
      type: 'warning'
    })
    await batchDeleteProducts(selectedIds.value)
    ElMessage.success(`已删除 ${selectedIds.value.length} 个产品`)
    selectedIds.value = []
    loadProducts(); loadCounts()
  } catch {}
}

// ======== Move ========
const moveDialogVisible = ref(false)
const moveTreeRef = ref(null)
const moveTarget = ref(null)
const moveProdId = ref(null)

function openMoveDialog(row) {
  moveProdId.value = row?.id || null
  moveTarget.value = null
  moveDialogVisible.value = true
  nextTick(() => { moveTreeRef.value?.setCurrentKey(null) })
}
function onMoveTargetSelect(data) { moveTarget.value = data }

async function confirmMove() {
  if (!moveTarget.value) { ElMessage.warning('请选择目标分类'); return }
  const ids = moveProdId.value ? [moveProdId.value] : selectedIds.value
  if (!ids.length) return
  try {
    await batchMoveProducts({ ids, targetCategoryId: moveTarget.value.id })
    ElMessage.success('移动成功')
    moveDialogVisible.value = false
    selectedIds.value = []
    loadProducts(); loadCounts()
  } catch {}
}

// ======== Utils ========
function formatDate(d) {
  if (!d) return '-'
  const dt = new Date(d)
  const pad = n => String(n).padStart(2, '0')
  return `${dt.getFullYear()}-${pad(dt.getMonth()+1)}-${pad(dt.getDate())} ${pad(dt.getHours())}:${pad(dt.getMinutes())}`
}

async function loadCounts() {
  try {
    const res = await getCategoryProductCounts()
    if (res.data) counts.value = res.data
  } catch {}
}

onMounted(() => { loadCategories(); loadProducts(); loadCounts() })
</script>

<style scoped>
/* ========== Layout ========== */
.cat-mg {
  display: flex; gap: 16px;
  height: calc(100vh - 130px);
  overflow: hidden;
  position: relative;
}

/* ========== Sidebar ========== */
.sidebar-toggle {
  position: fixed; left: 12px; top: 50%; z-index: 50;
  width: 32px; height: 32px; border-radius: 8px;
  background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,.1);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: all .2s;
}
.sidebar-toggle:hover { box-shadow: 0 4px 16px rgba(0,0,0,.15); transform: scale(1.05); }

.overlay { position: fixed; inset: 0; z-index: 40; background: rgba(0,0,0,.3); }

.cat-sidebar {
  width: 280px; flex-shrink: 0;
  transition: all .3s ease;
  overflow: hidden;
}
.cat-sidebar.open { margin-left: 0; }

.sidebar-card {
  background: #fff; border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,.05), 0 1px 2px rgba(0,0,0,.06);
  height: 100%; display: flex; flex-direction: column;
  overflow: hidden;
}

.sidebar-hd {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 16px 0;
}
.sidebar-title { margin: 0; font-size: 15px; font-weight: 700; color: #1a1a2e; }
.sidebar-hd-actions { display: flex; gap: 4px; }
.btn-close-sidebar { display: none; }

/* Category search */
.category-search {
  position: relative; padding: 8px 16px; border-bottom: 1px solid #e0e3e8;
}
.category-search :deep(.el-icon) {
  position: absolute; left: 28px; top: 50%; transform: translateY(-50%); color: #909399;
}
.category-search input {
  width: 100%; padding: 8px 12px 8px 32px; border: 1px solid #e0e3e8;
  border-radius: 8px; font-size: 13px; outline: none; background: #f5f7fa;
  transition: all .2s; font-family: inherit;
}
.category-search input:focus { border-color: #6c5ce7; background: #fff; }

/* Tree actions */
.tree-actions { padding: 8px 16px; display: flex; gap: 6px; border-bottom: 1px solid #e0e3e8; }
.btn-tree-action {
  flex: 1; padding: 6px 0; border: 1px solid #e0e3e8; border-radius: 8px;
  background: transparent; cursor: pointer; font-size: 12px; font-family: inherit;
  color: #606266; display: inline-flex; align-items: center; justify-content: center; gap: 4px;
  transition: all .2s;
}
.btn-tree-action:hover { border-color: #6c5ce7; color: #6c5ce7; background: rgba(108,92,231,0.06); }

.tree-wrap { flex: 1; overflow-y: auto; padding: 0 8px 12px; }

.tree-node-row {
  display: flex; align-items: center;
  flex: 1; gap: 8px; padding-right: 4px;
  min-width: 0;
}
.tree-icon { flex-shrink: 0; color: #6c5ce7; }
.tree-name { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 13px; }
.tree-badge {
  font-size: 11px; font-weight: 600; color: #909399;
  background: #f0f2f5; border-radius: 10px;
  padding: 1px 7px; min-width: 20px; text-align: center;
  flex-shrink: 0;
}
.tree-node-actions { display: none; gap: 2px; flex-shrink: 0; }
.tree-node-row:hover .tree-node-actions { display: flex; }

.btn-danger { color: #E6A23C !important; }
.btn-danger:hover { color: #F56C6C !important; }

/* ========== Main Area ========== */
.cat-main {
  flex: 1; overflow-y: auto;
  display: flex; flex-direction: column;
  gap: 12px;
}

.empty-state {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  gap: 12px; color: #909399;
}
.empty-state h3 { margin: 0; font-size: 18px; color: #606266; }
.empty-state p { margin: 0; font-size: 14px; }

/* Toolbar */
.main-toolbar {
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; border-radius: 12px;
  padding: 12px 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,.05), 0 1px 2px rgba(0,0,0,.06);
  flex-wrap: wrap; gap: 8px;
}
.toolbar-left { display: flex; align-items: center; gap: 8px; }
.toolbar-title {
  margin: 0; font-size: 17px; font-weight: 700; color: #1a1a2e;
  display: flex; align-items: center; gap: 10px;
}
.title-icon { color: #6c5ce7; font-weight: 800; font-size: 18px; }
.title-count { font-weight: 500; }
.toolbar-right { display: flex; align-items: center; gap: 8px; }
.prod-search { width: 200px; }
.prod-search :deep(.el-input__wrapper) { border-radius: 8px; }

/* Selection bar */
.selection-bar {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 16px; background: #ecf5ff;
  border-radius: 10px; font-size: 13px; color: #409EFF;
}

/* Table card */
.table-card {
  background: #fff; border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,.05), 0 1px 2px rgba(0,0,0,.06);
  overflow: auto; flex: 1;
  display: flex; flex-direction: column;
}
.table-card :deep(.el-table) { flex: 1; display: flex; flex-direction: column; }
.table-card :deep(.el-table__inner-wrapper) { flex: 1; overflow-y: auto; }

/* ===== Product Toolbar ===== */
.product-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 10px 16px; border-bottom: 1px solid var(--border, #e0e3e8);
  gap: 12px; flex-wrap: wrap;
}
.product-toolbar-left { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.filter-chip {
  padding: 5px 12px; border: 1px solid #e0e3e8; border-radius: 20px;
  font-size: 12px; cursor: pointer; transition: all .2s; background: transparent;
  font-family: inherit; color: #606266;
}
.filter-chip:hover, .filter-chip.active {
  border-color: #6c5ce7; color: #6c5ce7;
  background: rgba(108,92,231,0.08);
}

/* Selection bar */
.selection-bar {
  display: flex; align-items: center; gap: 8px;
  padding: 4px 12px; background: rgba(108,92,231,0.06);
  border-radius: 20px; font-size: 13px; color: #6c5ce7;
}

/* ===== Product Table ===== */
.product-name-cell { display: flex; align-items: center; gap: 10px; }
.product-img {
  width: 40px; height: 40px; border-radius: 6px; object-fit: cover;
  background: #f5f7fa; display: flex; align-items: center; justify-content: center;
  overflow: hidden; flex-shrink: 0;
}
.product-img img, .product-img video { width: 100%; height: 100%; object-fit: cover; }
.product-name { font-weight: 500; font-size: 13px; }
.product-sku { font-size: 11px; color: #909399; }

/* Status Badge */
.status-badge {
  display: inline-block; padding: 2px 8px; border-radius: 20px;
  font-size: 11px; font-weight: 500; white-space: nowrap; line-height: 1.6;
}
.status-badge.on { background: rgba(0,184,148,0.1); color: #00b894; }
.status-badge.off { background: rgba(225,112,85,0.1); color: #e17055; }

.stock-zero { color: #F56C6C; font-weight: 600; }

/* Action buttons */
.table-actions { display: flex; gap: 4px; justify-content: center; }
.action-btn {
  width: 30px; height: 30px; padding: 0; border-radius: 6px;
  background: transparent; border: 1px solid #e0e3e8; color: #909399;
  cursor: pointer; transition: all .2s; display: flex; align-items: center; justify-content: center;
}
.action-btn:hover { border-color: #6c5ce7; color: #6c5ce7; background: rgba(108,92,231,0.08); }
.action-btn.danger:hover { border-color: #e17055; color: #e17055; background: rgba(225,112,85,0.08); }

/* Pagination */
.pagination {
  display: flex; align-items: center; justify-content: space-between;
  padding: 10px 16px; border-top: 1px solid #e0e3e8;
}
.pagination-info { font-size: 12px; color: #909399; }

/* Old table footer alias */
.table-footer { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; flex-shrink: 0; }
.footer-info { font-size: 13px; color: #909399; }

/* ========== Cover ========== */
.cover-row { display: flex; align-items: flex-end; gap: 16px; }
.cover-box {
  width: 90px; height: 90px; border-radius: 8px;
  background: #f5f7fa; border: 2px dashed #dcdfe6;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; overflow: hidden; flex-shrink: 0;
  transition: border-color .2s;
}
.cover-box:hover { border-color: #409EFF; }
.cover-img { width: 100%; height: 100%; object-fit: cover; }
.cover-url { display: flex; flex-direction: column; gap: 6px; }

/* ========== Move ========== */
.move-hint { color: #909399; margin-bottom: 12px; font-size: 13px; }
.move-selected {
  margin-top: 12px; padding: 8px 12px;
  background: #f0f9eb; border-radius: 8px;
  display: flex; align-items: center; gap: 6px;
  font-size: 13px; color: #67C23A;
}

/* ========== Transitions ========== */
.fade-enter-active, .fade-leave-active { transition: all .25s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: translateY(-4px); }

/* ========== El-tree overrides ========== */
:deep(.el-tree) { background: transparent; }
:deep(.el-tree-node__content) {
  height: 36px; border-radius: 8px; padding-right: 8px;
  transition: background .15s;
}
:deep(.el-tree-node__content:hover) { background: #f5f7fa; }
:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background: rgba(108,92,231,0.08); color: #6c5ce7;
}
:deep(.el-tree-node.is-current > .el-tree-node__content .tree-badge) {
  background: rgba(108,92,231,0.15); color: #6c5ce7;
}

/* ========== El-table overrides ========== */
:deep(.el-table) { --el-table-border-color: transparent; }
:deep(.el-table tr) { transition: background .15s; }
:deep(.el-table tr.el-table__row:hover > td) { background: #f5f7fa !important; }
:deep(.el-table .el-table__cell) { padding: 10px 0; }

/* ========== Responsive ========== */
@media (max-width: 768px) {
  .cat-sidebar {
    position: fixed; left: 0; top: 0; bottom: 0; z-index: 50;
    width: 300px; margin-left: -320px;
  }
  .cat-sidebar.open { margin-left: 0; }
  .btn-close-sidebar { display: inline-flex; }
  .prod-search { width: 140px; }
  .main-toolbar { padding: 10px 12px; }
  .toolbar-title { font-size: 15px; }
}
</style>
