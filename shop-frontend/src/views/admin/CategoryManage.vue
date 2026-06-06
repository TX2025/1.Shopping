<template>
  <div class="cm-page">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h2><i class="cm-header-icon">#</i> 分类管理</h2>
        <div class="page-subtitle">管理商品分类结构，支持多级分类与数据统计</div>
      </div>
      <div class="page-header-actions">
        <button class="btn btn-outline" @click="ElMessage.info('导出功能开发中')"><el-icon :size="14"><Download /></el-icon> 导出</button>
        <button class="btn btn-outline" @click="expandAll"><el-icon :size="14"><Expand /></el-icon> 展开全部</button>
        <button class="btn btn-outline" @click="collapseAll"><el-icon :size="14"><Fold /></el-icon> 折叠全部</button>
        <button class="btn btn-primary" @click="openCatAdd(null)"><el-icon :size="14"><Plus /></el-icon> 新增分类</button>
      </div>
    </div>

    <!-- Stats Strip -->
    <div class="cm-stat-strip">
      <div class="cm-stat-card" v-for="k in catStats" :key="k.label">
        <div class="cm-stat-icon" :style="{background:k.bg,color:k.color}"><el-icon :size="16"><component :is="k.icon" /></el-icon></div>
        <div>
          <div class="cm-stat-label">{{ k.label }}</div>
          <div class="cm-stat-val" :style="{color:k.color}">{{ k.val }}</div>
          <div class="cm-stat-sub">{{ k.sub }}</div>
        </div>
      </div>
    </div>

    <!-- Main Layout -->
    <div class="cm-layout">
      <!-- Left: Tree Panel -->
      <div class="card cm-tree-panel">
        <div class="cm-tree-search">
          <el-icon :size="13"><Search /></el-icon>
          <input v-model="catSearch" placeholder="搜索分类..." />
        </div>
        <div class="cm-tree-actions">
          <button class="btn btn-sm btn-primary" @click="openCatAdd(null)"><el-icon :size="13"><Plus /></el-icon> 新增顶级</button>
          <button class="btn btn-sm btn-outline" @click="openCatAdd(currentCat)"><el-icon :size="13"><FolderAdd /></el-icon> 子分类</button>
          <button class="btn btn-sm btn-outline" title="按名称排序" @click="sortTree('name')"><el-icon :size="13"><Sort /></el-icon></button>
          <button class="btn btn-sm btn-outline" title="按商品数排序" @click="sortTree('count')"><el-icon :size="13"><Histogram /></el-icon></button>
        </div>
        <div class="cm-tree-list">
          <el-tree :data="filteredTree" :props="{children:'children',label:'name'}" node-key="id"
            highlight-current default-expand-all :expand-on-click-node="false"
            @node-click="onTreeSelect" ref="treeRef">
            <template #default="{data}">
              <span class="tree-node-row">
                <el-icon :size="14" class="tree-icon"><component :is="getCatIcon(data.icon)" /></el-icon>
                <span class="tree-name">{{ data.name }}</span>
                <span class="tree-badge" v-if="counts[data.id]!==undefined">{{ counts[data.id] }}</span>
                <span class="tree-node-actions">
                  <el-button link size="small" @click.stop="openCatAdd(data)"><el-icon :size="13"><Plus /></el-icon></el-button>
                  <el-button link size="small" @click.stop="openCatEdit(data)"><el-icon :size="13"><EditPen /></el-icon></el-button>
                  <el-button link size="small" class="btn-danger" @click.stop="handleCatDelete(data)"><el-icon :size="13"><Delete /></el-icon></el-button>
                </span>
              </span>
            </template>
          </el-tree>
          <el-empty v-if="filteredTree.length===0&&catSearch" description="无匹配分类" :image-size="48" />
        </div>
        <div class="cm-tree-footer">
          <span>共 {{ allCats.length }} 个分类</span>
          <span>顶级 {{ topCats.length }} · 子级 {{ allCats.length - topCats.length }}</span>
        </div>
      </div>

      <!-- Right Panel -->
      <div class="cm-right-panel">
        <!-- Category Detail Card -->
        <div class="card cm-cat-detail" v-if="currentCat">
          <div class="cm-cat-detail-inner">
            <div class="cm-cat-detail-icon"><el-icon :size="18"><component :is="getCatIcon(currentCat.icon)" /></el-icon></div>
            <div class="cm-cat-detail-info">
              <div class="cm-cat-detail-name">{{ currentCat.name }}</div>
              <div class="cm-cat-detail-meta">
                <span>{{ counts[currentCat.id]||0 }} 件产品</span>
                <span v-if="currentCat.parentId">上级: {{ getCatName(currentCat.parentId) }}</span>
                <span v-else>顶级分类</span>
                <span>排序: {{ currentCat.sortOrder||0 }}</span>
              </div>
            </div>
            <div class="cm-cat-detail-actions">
              <el-button size="small" @click="openCatEdit(currentCat)"><el-icon :size="13"><EditPen /></el-icon></el-button>
              <el-button size="small" type="danger" plain @click="handleCatDelete(currentCat)"><el-icon :size="13"><Delete /></el-icon></el-button>
            </div>
          </div>
        </div>

        <!-- Product List Card -->
        <div class="card cm-product-panel">
          <div class="card-header">
            <div style="display:flex;align-items:center;gap:10px">
              <div class="cm-cat-icon-badge"><el-icon :size="16"><component :is="currentCat?getCatIcon(currentCat.icon):FolderOpened" /></el-icon></div>
              <div>
                <h3 style="font-size:15px;font-weight:600;margin:0">{{ currentCat ? currentCat.name : '全部产品' }}</h3>
                <div class="card-subtitle">{{ currentCat ? currentCat.name + '分类下的产品' : '所有分类的产品' }}</div>
              </div>
            </div>
            <div style="display:flex;gap:8px;align-items:center">
              <div class="cm-search-wrap"><el-icon :size="12"><Search /></el-icon><input v-model="prodKeyword" placeholder="搜索产品..." @keyup.enter="page=1;loadProducts()" @input="onProdSearch" /></div>
              <select class="pm-select" v-model="prodSort" @change="page=1;loadProducts()">
                <option value="sales-desc">销量 ↓</option><option value="sales-asc">销量 ↑</option>
                <option value="price-desc">价格 ↓</option><option value="price-asc">价格 ↑</option>
                <option value="stock-asc">库存 ↑</option><option value="name-asc">名称 A-Z</option>
              </select>
              <button class="btn btn-sm btn-outline" @click="openProdAdd"><el-icon :size="13"><Plus /></el-icon> 添加</button>
            </div>
          </div>
          <div class="cm-product-toolbar">
            <div class="cm-filter-tabs">
              <button class="cm-filter-tab" :class="{active:statusFilter==='all'}" @click="statusFilter='all';page=1;loadProducts()">全部 <span class="tab-count">{{ tabCounts.all }}</span></button>
              <button class="cm-filter-tab" :class="{active:statusFilter==='ON'}" @click="statusFilter='ON';page=1;loadProducts()"><i class="status-dot on"></i> 在售 <span class="tab-count">{{ tabCounts.ON }}</span></button>
              <button class="cm-filter-tab" :class="{active:statusFilter==='OFF'}" @click="statusFilter='OFF';page=1;loadProducts()"><i class="status-dot off"></i> 下架 <span class="tab-count">{{ tabCounts.OFF }}</span></button>
            </div>
            <div class="cm-batch-bar" v-if="selectedIds.length">
              <span>已选 <strong>{{ selectedIds.length }}</strong> 件</span>
              <button class="btn btn-sm btn-outline" @click="batchStatus('ON')">上架</button>
              <button class="btn btn-sm btn-outline" @click="batchStatus('OFF')">下架</button>
              <button v-if="currentCat" class="btn btn-sm btn-outline" @click="openMoveDialog(null)">移动分类</button>
              <button class="btn btn-sm" style="color:#e17055;border:1px solid #e0e3e8" @click="handleBatchDelete">删除</button>
            </div>
          </div>
          <div class="product-table" v-loading="prodLoading">
            <el-table :data="sortedProducts" stripe row-key="id" size="small" @selection-change="onSelectionChange" ref="tableRef">
              <el-table-column type="selection" width="40" />
              <el-table-column label="产品信息" min-width="220">
                <template #default="{row}">
                  <div class="prod-cell">
                    <div class="prod-thumb">
                      <video v-if="isVideo(coverMedia(row))" :src="coverMedia(row)" muted />
                      <img v-else-if="coverMedia(row)" :src="coverMedia(row)" />
                      <el-icon v-else :size="16" color="#a29bfe"><Box /></el-icon>
                    </div>
                    <div>
                      <div class="prod-name">{{ row.name }}</div>
                      <div class="prod-sku">SKU: {{ row.sku || row.id }}</div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="分类" width="90">
                <template #default="{row}">{{ currentCat?.name || getCatName(row.categoryId) }}</template>
              </el-table-column>
              <el-table-column label="价格" width="90" sortable prop="price">
                <template #default="{row}"><span style="font-weight:600;color:#6c5ce7">¥{{ Number(row.price).toFixed(2) }}</span></template>
              </el-table-column>
              <el-table-column label="库存" width="80" sortable prop="stock" align="center">
                <template #default="{row}"><span :class="row.stock<=0?'stock-zero':''">{{ row.stock }}</span></template>
              </el-table-column>
              <el-table-column label="销量" width="80" sortable prop="sales" align="center">
                <template #default="{row}"><span style="font-weight:600">{{ row.sales||0 }}</span></template>
              </el-table-column>
              <el-table-column label="状态" width="80" align="center">
                <template #default="{row}"><span class="status-badge" :class="row.status==='ON'?'on':'off'">{{ row.status==='ON'?'在售':'下架' }}</span></template>
              </el-table-column>
              <el-table-column label="操作" width="130" fixed="right" align="center">
                <template #default="{row}">
                  <div class="table-actions">
                    <button class="action-btn" title="编辑" @click="openProdEdit(row)"><el-icon :size="13"><EditPen /></el-icon></button>
                    <button v-if="currentCat" class="action-btn" title="移动" @click="openMoveDialog(row)"><el-icon :size="13"><Switch /></el-icon></button>
                    <button class="action-btn danger" title="删除" @click="handleProdDelete(row)"><el-icon :size="13"><Delete /></el-icon></button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!prodLoading&&sortedProducts.length===0" description="暂无产品" :image-size="48" style="padding:40px 0" />
          </div>
          <div class="cm-pagination" v-if="total>0">
            <span>共 {{ total }} 件产品，第 {{ page }}/{{ Math.ceil(total/size) }} 页</span>
            <el-pagination v-model:current-page="page" :page-size="size" :page-sizes="[10,20,50]" :total="total"
              layout="sizes,prev,pager,next" @size-change="s=>{size=s;page=1;loadProducts()}" @current-change="loadProducts" background small />
          </div>
        </div>
      </div>
    </div>

    <!-- Category Dialog -->
    <el-dialog v-model="catDialogVisible" :title="catEditId?'编辑分类':'新增分类'" width="520px" destroy-on-close>
      <el-form :model="catForm" label-width="80px" label-position="top">
        <el-row :gutter="12">
          <el-col :span="16"><el-form-item label="分类名称"><el-input v-model="catForm.name" placeholder="输入分类名称" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="图标">
            <el-select v-model="catForm.icon" placeholder="选择图标" clearable style="width:100%">
              <template #prefix><el-icon :size="14" v-if="catForm.icon"><component :is="iconMap[catForm.icon]" /></el-icon></template>
              <el-option v-for="k in iconOptions" :key="k" :label="k" :value="k">
                <span style="display:flex;align-items:center;gap:8px"><el-icon :size="14"><component :is="iconMap[k]" /></el-icon>{{ k }}</span>
              </el-option>
            </el-select>
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="上级分类">
          <el-select v-model="catForm.parentId" placeholder="无（顶级分类）" clearable style="width:100%">
            <el-option v-for="c in parentCatOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="catForm.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="catDialogVisible=false">取消</el-button><el-button type="primary" @click="saveCat" :loading="catSaving">保存</el-button></template>
    </el-dialog>

    <!-- Product Dialog -->
    <el-dialog v-model="prodDialogVisible" :title="prodEditId?'编辑产品':'新增产品'" width="700px" destroy-on-close>
      <el-form :model="prodForm" label-width="90px" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="产品名称"><el-input v-model="prodForm.name" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="SKU"><el-input v-model="prodForm.sku" placeholder="如 BT-EAR-001" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="状态"><el-switch v-model="prodForm.status" active-value="ON" inactive-value="OFF" inline-prompt active-text="在售" inactive-text="下架" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="描述"><el-input v-model="prodForm.description" type="textarea" :rows="2" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="价格"><el-input-number v-model="prodForm.price" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="原价"><el-input-number v-model="prodForm.originalPrice" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="库存"><el-input-number v-model="prodForm.stock" :min="0" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="产品图片">
          <el-upload :action="uploadAction" :headers="uploadHeaders" list-type="picture-card" :file-list="prodImageFileList"
            :before-upload="beforeUploadImage" :on-success="onProdImageUpload" :on-remove="onProdImageRemove" accept=".jpg,.jpeg,.png,.gif">
            <el-icon :size="28"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="产品视频">
          <el-upload :action="uploadAction" :headers="uploadHeaders" :file-list="prodVideoFileList"
            :before-upload="beforeUploadVideo" :on-success="onProdVideoUpload" :on-remove="onProdVideoRemove" accept=".mp4" drag>
            <el-icon :size="32"><UploadFilled /></el-icon><div style="margin-top:8px">拖拽或点击上传 MP4 视频</div>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="prodDialogVisible=false">取消</el-button><el-button type="primary" @click="saveProd" :loading="prodSaving">保存</el-button></template>
    </el-dialog>

    <!-- Move Dialog -->
    <el-dialog v-model="moveDialogVisible" title="移动到分类" width="420px">
      <el-tree :data="treeData" :props="{children:'children',label:'name'}" node-key="id" highlight-current default-expand-all
        @node-click="onMoveTargetSelect" ref="moveTreeRef" />
      <div class="move-selected" v-if="moveTarget">目标分类：<strong>{{ moveTarget.name }}</strong></div>
      <template #footer><el-button @click="moveDialogVisible=false">取消</el-button><el-button type="primary" @click="confirmMove" :disabled="!moveTarget">确认移动</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import {
  getAdminCategories, getCategoryProducts, getAdminProducts, getCategoryProductCounts,
  createCategory, updateCategory, deleteCategory,
  createProduct, updateProduct, deleteProduct, updateProductStatus,
  batchMoveProducts, batchDeleteProducts, uploadFile
} from '../../api/admin'
import { useAuthStore } from '../../stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, EditPen, Delete, Search, Close, Menu, Switch, Download, Expand, Fold,
  InfoFilled, FolderOpened, FolderAdd, PictureFilled, Check, Box, Warning,
  Monitor, Iphone, Headset, Present, User, UserFilled, HomeFilled,
  Camera, ShoppingBag, OfficeBuilding, Star, ShoppingCartFull,
  CreditCard, Timer, AlarmClock, Van, ChatDotRound, ChatLineRound,
  DataAnalysis, PieChart, Bell, Phone, Message, VideoCamera, Film,
  Picture, Document, Files, CollectionTag, PriceTag, Link,
  Connection, DataBoard, Management, HelpFilled, Suitcase, TrendCharts,
  School, KnifeFork, Sell, Share, Sort, Histogram, UploadFilled, VideoPlay
} from '@element-plus/icons-vue'

// ===== Auth =====
const authStore = useAuthStore()
const uploadAction = '/api/admin/upload'
const uploadHeaders = computed(() => ({ Authorization: authStore.token ? `Bearer ${authStore.token}` : '' }))

// ===== Icon map =====
const iconMap = {
  Monitor,Iphone,Headset,Present,User,UserFilled,HomeFilled,Suitcase,KnifeFork,Camera,ShoppingBag,OfficeBuilding,Star,CollectionTag,ShoppingCartFull,CreditCard,Timer,AlarmClock,Van,ChatDotRound,ChatLineRound,DataAnalysis,PieChart,Bell,Phone,Message,VideoCamera,Film,Picture,Document,Files,FolderOpened,PriceTag,Link,Connection,DataBoard,Management,HelpFilled,School,Sell,Share,TrendCharts,Box,Goods:ShoppingBag,Setting:Switch,}
const iconOptions = Object.keys(iconMap).filter(k=>!['Goods','Setting'].includes(k))
function getCatIcon(n){const a={House:HomeFilled,Dishwasher:Suitcase,Collection:CollectionTag,ShoppingCart:ShoppingCartFull,ChatDotSquare:ChatDotRound,ChatLineSquare:ChatLineRound,Histogram:TrendCharts,Files:Document};return iconMap[a[n]||n]||FolderOpened}

// ===== Categories =====
const treeRef=ref(null);const treeData=ref([]);const allCats=ref([]);const currentCat=ref(null)
const catSearch=ref('');const counts=ref({})
const topCats=computed(()=>allCats.value.filter(c=>!c.parentId))
const parentCatOptions=computed(()=>allCats.value.filter(c=>c.id!==catEditId.value))

const filteredTree=computed(()=>{
  if(!catSearch.value.trim())return treeData.value
  const kw=catSearch.value.toLowerCase()
  return treeData.value.filter(c=>matchTree(c,kw)).map(c=>pruneTree(c,kw)).filter(Boolean)
})
function matchTree(n,k){if(n.name.toLowerCase().includes(k))return true;if(n.children)return n.children.some(c=>matchTree(c,k));return false}
function pruneTree(n,k){const m=n.name.toLowerCase().includes(k);if(n.children){const f=n.children.map(c=>pruneTree(c,k)).filter(Boolean);return{...n,children:f}};return m?n:null}

// Category stats
const catStats=computed(()=>{
  const total=allCats.value.length;const top=topCats.value.length
  const empty=Object.entries(counts.value).filter(([,v])=>Number(v)===0).length
  const totalProd=Object.values(counts.value).reduce((s,v)=>s+(Number(v)||0),0)
  const onSale=Object.values(counts.value).reduce((s,v)=>s+(Number(v)||0),0)
  return[
    {icon:FolderOpened,color:'#6c5ce7',bg:'rgba(108,92,231,0.1)',label:'总分类数',val:total,sub:`顶级 ${top} 个`},
    {icon:Check,color:'#00b894',bg:'rgba(0,184,148,0.1)',label:'子分类数',val:total-top,sub:`占比 ${total?Math.round((total-top)/total*100):0}%`},
    {icon:Star,color:'#fdcb6e',bg:'rgba(253,203,110,0.12)',label:'热门分类',val:Object.entries(counts.value).filter(([,v])=>Number(v)>=10).length,sub:'产品≥10'},
    {icon:Warning,color:'#74b9ff',bg:'rgba(116,185,255,0.1)',label:'空分类',val:empty,sub:'暂无产品'},
    {icon:Box,color:'#a29bfe',bg:'rgba(162,155,254,0.12)',label:'总产品数',val:totalProd,sub:`在售 ${onSale} 件`},
  ]
})

async function loadCategories(){
  try{const res=await getAdminCategories();const raw=res.data||[];allCats.value=raw;const map={};const roots=[]
  raw.forEach(c=>{map[c.id]={...c,children:[]}})
  raw.forEach(c=>{if(c.parentId&&map[c.parentId])map[c.parentId].children.push(map[c.id]);else if(!c.parentId)roots.push(map[c.id])})
  treeData.value=roots.length>0?roots:raw}catch{}
}

function getCatName(id){if(!id)return'-';const c=allCats.value.find(x=>x.id===id);return c?c.name:'-'}

function sortTree(by){
  if(by==='name')allCats.value.sort((a,b)=>a.name.localeCompare(b.name,'zh'))
  else allCats.value.sort((a,b)=>(counts.value[b.id]||0)-(counts.value[a.id]||0))
  loadCategories()
}

function expandAll(){const nodes=treeRef.value?.store?.nodesMap;if(nodes)Object.values(nodes).forEach(n=>n.expanded=true)}
function collapseAll(){const nodes=treeRef.value?.store?.nodesMap;if(nodes)Object.values(nodes).forEach(n=>n.expanded=false)}

function onTreeSelect(data){currentCat.value=data;page.value=1;prodKeyword.value='';prodSort.value='sales-desc';selectedIds.value=[];loadProducts()}

// ===== Products =====
const tableRef=ref(null);const products=ref([]);const prodLoading=ref(false);const page=ref(1);const size=ref(10);const total=ref(0)
const selectedIds=ref([]);const prodKeyword=ref('');const prodSort=ref('sales-desc');const statusFilter=ref('all')
const tabCounts=ref({all:0,ON:0,OFF:0})
let prodSearchTimer=null

const sortedProducts=computed(()=>{
  let list=[...products.value]
  const[s,d]=prodSort.value.split('-')
  if(d==='desc')list.sort((a,b)=>(Number(b[s])||0)-(Number(a[s])||0))
  else if(d==='asc')list.sort((a,b)=>(Number(a[s])||0)-(Number(b[s])||0))
  else if(s==='name')list.sort((a,b)=>a.name.localeCompare(b.name,'zh'))
  return list
})

function onSelectionChange(rows){selectedIds.value=rows.map(r=>r.id)}
function onProdSearch(){clearTimeout(prodSearchTimer);prodSearchTimer=setTimeout(()=>{page.value=1;loadProducts()},300)}

async function loadProducts(){
  prodLoading.value=true
  try{
    let res
    if(currentCat.value){
      res=await getCategoryProducts(currentCat.value.id,{page:page.value,size:size.value,keyword:prodKeyword.value||undefined,status:statusFilter.value!=='all'?statusFilter.value:undefined})
    }else{
      res=await getAdminProducts({page:page.value,size:size.value,status:statusFilter.value!=='all'?statusFilter.value:undefined})
    }
    products.value=res.data?.list||[];total.value=res.data?.total||0
    if(currentCat.value)counts.value[currentCat.value.id]=total.value
    selectedIds.value=[]
    // Tab counts
    try{let on=0,off=0,all=total.value;if(currentCat.value){const r=await getCategoryProducts(currentCat.value.id,{page:1,size:1,status:'ON'});on=r.data?.total||0;const r2=await getCategoryProducts(currentCat.value.id,{page:1,size:1,status:'OFF'});off=r2.data?.total||0}else{const r=await getAdminProducts({page:1,size:1,status:'ON'});on=r.data?.total||0;const r2=await getAdminProducts({page:1,size:1,status:'OFF'});off=r2.data?.total||0;all=on+off};tabCounts.value={all,ON:on,OFF:off}}catch{}
  }catch{}finally{prodLoading.value=false}
}

async function loadCounts(){try{const res=await getCategoryProductCounts();if(res.data)counts.value=res.data}catch{}}

// Helpers
function parseJsonField(v){if(!v)return[];if(Array.isArray(v))return v;try{return JSON.parse(v)}catch{return[]}}
function isVideo(u){return u&&/\.mp4$/i.test(u)}
function coverMedia(r){const v=parseJsonField(r.videos);if(v.length)return v[0];const i=parseJsonField(r.images);if(i.length)return i[0];return r.coverImage||null}

// ===== Category CRUD =====
const catDialogVisible=ref(false);const catEditId=ref(null);const catForm=ref({name:'',sortOrder:0,parentId:null,icon:''});const catSaving=ref(false)
function openCatAdd(parent){catEditId.value=null;catForm.value={name:'',sortOrder:0,parentId:parent?.id||null,icon:''};catDialogVisible.value=true}
function openCatEdit(data){catEditId.value=data.id;catForm.value={name:data.name,sortOrder:data.sortOrder||0,parentId:data.parentId||null,icon:data.icon||''};catDialogVisible.value=true}
async function saveCat(){
  if(!catForm.value.name.trim()){ElMessage.warning('请输入分类名称');return}
  catSaving.value=true
  try{if(catEditId.value){await updateCategory(catEditId.value,catForm.value);ElMessage.success('更新成功')}else{await createCategory(catForm.value);ElMessage.success('创建成功')}
  catDialogVisible.value=false;await loadCategories();loadCounts()}catch{}finally{catSaving.value=false}
}
async function handleCatDelete(data){
  try{await ElMessageBox.confirm(`确定删除「${data.name}」？分类下产品不会删除但将失去归属。`,'删除分类',{type:'warning',confirmButtonText:'确定'})
  await deleteCategory(data.id);ElMessage.success('删除成功');if(currentCat.value?.id===data.id)currentCat.value=null;await loadCategories();loadCounts()}catch{}
}

// ===== Product CRUD =====
const prodDialogVisible=ref(false);const prodForm=reactive({name:'',sku:'',description:'',price:0,originalPrice:0,stock:0,categoryId:null,status:'ON',images:[],videos:[]})
const prodEditId=ref(null);const prodSaving=ref(false)
const prodImageFileList=computed(()=>prodForm.images.map((url,i)=>({uid:i,name:`img-${i}`,url,status:'success'})))
const prodVideoFileList=computed(()=>prodForm.videos.map((url,i)=>({uid:i,name:`vid-${i}`,url,status:'success'})))

function resetProdForm(){prodEditId.value=null;prodForm.name='';prodForm.sku='';prodForm.description='';prodForm.price=0;prodForm.originalPrice=0;prodForm.stock=0;prodForm.categoryId=currentCat.value?.id||null;prodForm.status='ON';prodForm.images=[];prodForm.videos=[]}
function openProdAdd(){resetProdForm();prodDialogVisible.value=true}
function openProdEdit(row){prodEditId.value=row.id;prodForm.name=row.name;prodForm.sku=row.sku||'';prodForm.description=row.description||'';prodForm.price=Number(row.price);prodForm.originalPrice=Number(row.originalPrice||0);prodForm.stock=row.stock;prodForm.status=row.status;prodForm.categoryId=row.categoryId;prodForm.images=parseJsonField(row.images);prodForm.videos=parseJsonField(row.videos);prodDialogVisible.value=true}

function beforeUploadImage(f){const e=f.name.split('.').pop().toLowerCase();if(!['jpg','jpeg','png','gif'].includes(e)){ElMessage.error('仅支持jpg/png/gif');return false};if(f.size>10*1024*1024){ElMessage.error('图片≤10MB');return false};return true}
function beforeUploadVideo(f){if(f.name.split('.').pop().toLowerCase()!=='mp4'){ElMessage.error('仅支持mp4');return false};if(f.size>50*1024*1024){ElMessage.error('视频≤50MB');return false};return true}
function onProdImageUpload(res){const url=res.data?.url||res.url;if(url)prodForm.images.push(url)}
function onProdImageRemove(f){const i=prodForm.images.indexOf(f.url);if(i>-1)prodForm.images.splice(i,1)}
function onProdVideoUpload(res){const url=res.data?.url||res.url;if(url)prodForm.videos.push(url)}
function onProdVideoRemove(f){const i=prodForm.videos.indexOf(f.url);if(i>-1)prodForm.videos.splice(i,1)}

async function saveProd(){
  prodSaving.value=true
  try{const data={...prodForm,price:parseFloat(prodForm.price),originalPrice:parseFloat(prodForm.originalPrice)||null,sku:prodForm.sku||null,images:prodForm.images.length?JSON.stringify(prodForm.images):null,videos:prodForm.videos.length?JSON.stringify(prodForm.videos):null}
  if(prodEditId.value){await updateProduct(prodEditId.value,data);ElMessage.success('更新成功')}else{await createProduct(data);ElMessage.success('创建成功')}
  prodDialogVisible.value=false;loadProducts();loadCounts()}catch{}finally{prodSaving.value=false}
}

async function handleProdDelete(row){try{await ElMessageBox.confirm(`确定删除「${row.name}」？`,'删除',{type:'warning',confirmButtonText:'确定'});await deleteProduct(row.id);ElMessage.success('删除成功');loadProducts();loadCounts()}catch{}}
async function handleBatchDelete(){try{await ElMessageBox.confirm(`确定删除选中 ${selectedIds.value.length} 个产品？`,'批量删除',{type:'warning'});await batchDeleteProducts(selectedIds.value);ElMessage.success('已删除');selectedIds.value=[];loadProducts();loadCounts()}catch{}}

async function batchStatus(st){try{await Promise.all(selectedIds.value.map(id=>updateProductStatus(id,st)));ElMessage.success(`已${st==='ON'?'上架':'下架'} ${selectedIds.value.length} 个`);loadProducts()}catch{}}

// ===== Move =====
const moveDialogVisible=ref(false);const moveTreeRef=ref(null);const moveTarget=ref(null);const moveProdId=ref(null)
function openMoveDialog(row){moveProdId.value=row?.id||null;moveTarget.value=null;moveDialogVisible.value=true;nextTick(()=>{moveTreeRef.value?.setCurrentKey(null)})}
function onMoveTargetSelect(data){moveTarget.value=data}
async function confirmMove(){if(!moveTarget.value){ElMessage.warning('请选择目标分类');return};const ids=moveProdId.value?[moveProdId.value]:selectedIds.value;if(!ids.length)return
try{await batchMoveProducts({ids,targetCategoryId:moveTarget.value.id});ElMessage.success('移动成功');moveDialogVisible.value=false;selectedIds.value=[];loadProducts();loadCounts()}catch{}}

onMounted(()=>{loadCategories();loadProducts();loadCounts()})
</script>

<style scoped>
/* === Page Layout === */
.cm-page{max-width:100%}
.page-header{display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:16px}
.page-header h2{font-size:20px;font-weight:600;margin:0;display:flex;align-items:center;gap:8px}
.cm-header-icon{color:#6c5ce7;font-weight:800;font-size:18px;font-style:normal}
.page-subtitle{font-size:12px;color:#909399;margin-top:3px}
.page-header-actions{display:flex;gap:8px}

/* Buttons */
.btn{padding:8px 16px;border:none;border-radius:8px;cursor:pointer;font-size:13px;font-weight:500;display:inline-flex;align-items:center;gap:6px;transition:all .2s;font-family:inherit}
.btn-primary{background:#6c5ce7;color:#fff}.btn-primary:hover{background:#5b4cdb}
.btn-outline{background:transparent;border:1px solid #e0e3e8;color:#606266}.btn-outline:hover{border-color:#6c5ce7;color:#6c5ce7}
.btn-sm{padding:5px 12px;font-size:12px}
.card{background:#fff;border-radius:12px;border:1px solid #e0e3e8;box-shadow:0 1px 4px rgba(0,0,0,.04)}
.card-header{flex-shrink:0;padding:14px 16px;border-bottom:1px solid #e0e3e8;display:flex;align-items:center;justify-content:space-between}
.card-header h3{font-size:15px;font-weight:600;margin:0}
.card-subtitle{font-size:11px;color:#909399;margin-top:1px}

/* Stats Strip */
.cm-stat-strip{display:grid;grid-template-columns:repeat(5,1fr);gap:14px;margin-bottom:16px}
.cm-stat-card{background:#fff;border-radius:12px;padding:14px 16px;border:1px solid #e0e3e8;box-shadow:0 1px 4px rgba(0,0,0,.04);display:flex;align-items:center;gap:12px;transition:all .2s}
.cm-stat-card:hover{box-shadow:0 2px 12px rgba(0,0,0,.06);transform:translateY(-1px)}
.cm-stat-icon{width:40px;height:40px;border-radius:10px;display:flex;align-items:center;justify-content:center;flex-shrink:0}
.cm-stat-label{font-size:11px;color:#909399}
.cm-stat-val{font-size:22px;font-weight:700;line-height:1.2}
.cm-stat-sub{font-size:11px;color:#909399;margin-top:1px}

/* Layout */
.cm-layout{display:grid;grid-template-columns:300px 1fr;gap:16px;height:calc(100vh - 280px);overflow:hidden}
.cm-tree-panel{display:flex;flex-direction:column;overflow:hidden}
.cm-tree-search{flex-shrink:0;position:relative;padding:10px 14px;border-bottom:1px solid #e0e3e8}
.cm-tree-search :deep(.el-icon){position:absolute;left:24px;top:50%;transform:translateY(-50%);color:#909399}
.cm-tree-search input{width:100%;padding:8px 10px 8px 30px;border:1px solid #e0e3e8;border-radius:8px;font-size:13px;outline:none;background:#f5f7fa;font-family:inherit;transition:all .2s}
.cm-tree-search input:focus{border-color:#6c5ce7;background:#fff}
.cm-tree-actions{flex-shrink:0;padding:6px 10px;display:flex;gap:4px;border-bottom:1px solid #e0e3e8;flex-wrap:wrap}
.cm-tree-list{flex:1;overflow-y:auto;padding:6px}
.cm-tree-footer{flex-shrink:0;padding:8px 14px;border-top:1px solid #e0e3e8;font-size:11px;color:#909399;display:flex;justify-content:space-between}

.tree-node-row{display:flex;align-items:center;flex:1;gap:6px;padding-right:4px;min-width:0}
.tree-icon{flex-shrink:0;color:#6c5ce7}
.tree-name{flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font-size:13px}
.tree-badge{font-size:11px;font-weight:600;color:#909399;background:#f0f2f5;border-radius:10px;padding:1px 7px;flex-shrink:0}
.tree-node-actions{display:none;gap:2px;flex-shrink:0}
.tree-node-row:hover .tree-node-actions{display:flex}
.btn-danger{color:#E6A23C!important}.btn-danger:hover{color:#F56C6C!important}

:deep(.el-tree){background:transparent}
:deep(.el-tree-node__content){height:34px;border-radius:8px;padding-right:8px;transition:background .15s}
:deep(.el-tree-node__content:hover){background:#f5f7fa}
:deep(.el-tree-node.is-current>.el-tree-node__content){background:rgba(108,92,231,.08);color:#6c5ce7}

/* Right Panel */
.cm-right-panel{display:flex;flex-direction:column;gap:12px;min-width:0;overflow:hidden;height:100%}

/* Category Detail */
.cm-cat-detail{padding:0}
.cm-cat-detail-inner{display:flex;align-items:center;gap:12px;padding:12px 14px}
.cm-cat-detail-icon{width:36px;height:36px;border-radius:8px;background:rgba(108,92,231,.08);color:#6c5ce7;display:flex;align-items:center;justify-content:center;flex-shrink:0}
.cm-cat-detail-name{font-size:14px;font-weight:600}
.cm-cat-detail-meta{font-size:11px;color:#909399;margin-top:2px;display:flex;gap:8px}
.cm-cat-detail-actions{margin-left:auto;display:flex;gap:4px}

/* Product Panel */
.cm-product-panel{display:flex;flex-direction:column;flex:1;min-height:0;overflow:hidden}
.cm-cat-icon-badge{width:36px;height:36px;border-radius:8px;background:rgba(108,92,231,.08);color:#6c5ce7;display:flex;align-items:center;justify-content:center;flex-shrink:0}
.cm-product-toolbar{flex-shrink:0;padding:8px 14px;border-bottom:1px solid #e0e3e8;display:flex;align-items:center;justify-content:space-between;gap:8px;flex-wrap:wrap}
.cm-filter-tabs{display:flex;gap:2px}
.cm-filter-tab{padding:5px 10px;border:1px solid transparent;border-radius:6px;font-size:12px;cursor:pointer;background:transparent;color:#909399;display:flex;align-items:center;gap:4px;font-family:inherit;transition:all .2s}
.cm-filter-tab:hover{color:#606266;background:#f5f7fa}
.cm-filter-tab.active{background:rgba(108,92,231,.08);color:#6c5ce7;border-color:rgba(108,92,231,.2)}
.cm-filter-tab .tab-count{background:#f5f7fa;color:#909399;font-size:10px;padding:1px 6px;border-radius:8px;font-weight:600}
.cm-filter-tab.active .tab-count{background:rgba(108,92,231,.15);color:#6c5ce7}
.status-dot{display:inline-block;width:6px;height:6px;border-radius:50%}
.status-dot.on{background:#00b894}.status-dot.off{background:#e17055}
.cm-batch-bar{display:flex;align-items:center;gap:8px;padding:6px 10px!important;background:rgba(108,92,231,.06);border-radius:8px;border:1px solid rgba(108,92,231,.2);font-size:13px;color:#6c5ce7;margin-left:8px}
.cm-search-wrap{position:relative;display:flex;align-items:center}
.cm-search-wrap :deep(.el-icon){position:absolute;left:8px;color:#909399;font-size:12px;z-index:1}
.cm-search-wrap input{padding:6px 8px 6px 26px;border:1px solid #e0e3e8;border-radius:8px;font-size:12px;outline:none;width:140px;background:#f5f7fa;font-family:inherit;transition:all .2s}
.cm-search-wrap input:focus{border-color:#6c5ce7;background:#fff}
.pm-select{padding:6px 8px;border:1px solid #e0e3e8;border-radius:8px;font-size:12px;outline:none;background:#f5f7fa;color:#606266;cursor:pointer;font-family:inherit}

/* Product table */
.product-table{flex:1;overflow:auto}
.prod-cell{display:flex;align-items:center;gap:10px}
.prod-thumb{width:36px;height:36px;border-radius:6px;overflow:hidden;background:#f5f7fa;display:flex;align-items:center;justify-content:center;flex-shrink:0}
.prod-thumb img,.prod-thumb video{width:100%;height:100%;object-fit:cover}
.prod-name{font-weight:500;font-size:13px}
.prod-sku{font-size:11px;color:#909399}
.stock-zero{color:#e17055;font-weight:600}
.status-badge{display:inline-block;padding:2px 8px;border-radius:20px;font-size:11px;font-weight:500;white-space:nowrap;line-height:1.6}
.status-badge.on{background:rgba(0,184,148,.1);color:#00b894}
.status-badge.off{background:rgba(225,112,85,.1);color:#e17055}

/* Actions */
.table-actions{display:flex;gap:4px;justify-content:center}
.action-btn{width:28px;height:28px;padding:0;border-radius:6px;background:transparent;border:1px solid #e0e3e8;color:#909399;cursor:pointer;transition:all .2s;display:flex;align-items:center;justify-content:center}
.action-btn:hover{border-color:#6c5ce7;color:#6c5ce7;background:rgba(108,92,231,.08)}
.action-btn.danger:hover{border-color:#e17055;color:#e17055;background:rgba(225,112,85,.08)}

/* Pagination */
.cm-pagination{flex-shrink:0;display:flex;align-items:center;justify-content:space-between;padding:10px 14px;border-top:1px solid #e0e3e8;font-size:12px;color:#909399}
.move-selected{margin-top:12px;padding:8px 12px;background:#f0f9eb;border-radius:8px;display:flex;align-items:center;gap:6px;font-size:13px;color:#67C23A}

@media(max-width:1200px){.cm-layout{grid-template-columns:260px 1fr}.cm-stat-strip{grid-template-columns:repeat(3,1fr)}}
@media(max-width:768px){.cm-layout{grid-template-columns:1fr}.cm-stat-strip{grid-template-columns:repeat(2,1fr)}.page-header{flex-direction:column;gap:8px}}
</style>
