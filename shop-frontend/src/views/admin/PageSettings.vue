<template>
  <div class="pg-page">
    <div class="page-header">
      <div>
        <h2>页面布局</h2>
        <div class="page-subtitle">管理商城页面模板与组件布局</div>
      </div>
      <div class="page-header-actions">
        <button class="btn btn-outline"><el-icon :size="14"><Download /></el-icon> 导出</button>
        <button class="btn btn-primary" @click="openEdit('')"><el-icon :size="14"><Plus /></el-icon> 新增页面</button>
      </div>
    </div>

    <!-- KPI Strip -->
    <div class="om-kpi-strip">
      <div v-for="k in kpis" :key="k.label" class="om-kpi-card">
        <div class="om-kpi-icon" :style="{background:k.bg,color:k.color}"><el-icon :size="17"><component :is="k.icon" /></el-icon></div>
        <div class="om-kpi-body">
          <div class="om-kpi-label">{{ k.label }}</div>
          <div class="om-kpi-val" :style="{color:k.color}">{{ k.val }}</div>
          <div class="om-kpi-sub">{{ k.sub }}</div>
        </div>
      </div>
    </div>

    <!-- Table Card -->
    <div class="card">
      <div class="card-body" style="padding:0">
        <div class="pm-toolbar" style="padding:12px 16px;border-bottom:1px solid #e0e3e8">
          <div class="pm-tab-group">
            <button class="pm-tab" :class="{active:statusFilter==='all'}" @click="statusFilter='all'">全部 <span class="pm-tab-count">{{ pageList.length }}</span></button>
            <button class="pm-tab" :class="{active:statusFilter==='published'}" @click="statusFilter='published'"><i class="tab-dot" style="background:#00b894;margin-right:3px"></i>已发布</button>
            <button class="pm-tab" :class="{active:statusFilter==='draft'}" @click="statusFilter='draft'"><i class="tab-dot" style="background:#909399;margin-right:3px"></i>草稿</button>
            <button class="pm-tab" :class="{active:statusFilter==='offline'}" @click="statusFilter='offline'"><i class="tab-dot" style="background:#e17055;margin-right:3px"></i>已下线</button>
          </div>
          <div class="pm-toolbar-right">
            <div class="pm-search-wrap"><el-icon :size="12"><Search /></el-icon><input v-model="searchKeyword" placeholder="搜索页面..." /></div>
          </div>
        </div>
        <el-table :data="filteredPages" stripe row-key="type" size="small">
          <el-table-column label="页面名称" min-width="150">
            <template #default="{row}">
              <div style="display:flex;align-items:center;gap:8px">
                <el-icon :size="16" color="#6c5ce7"><component :is="pageIcon(row.type)" /></el-icon>
                <div>
                  <div style="font-weight:500;font-size:13px">{{ row.label }}</div>
                  <div style="font-size:11px;color:#909399">{{ pagePath(row.type) }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="路径" width="130">
            <template #default="{row}"><code style="color:#6c5ce7;font-size:12px;background:#f5f7fa;padding:2px 6px;border-radius:4px">{{ pagePath(row.type) }}</code></template>
          </el-table-column>
          <el-table-column label="模板" width="90" align="center">
            <template #default="{row}"><span style="font-size:12px;color:#909399">默认</span></template>
          </el-table-column>
          <el-table-column label="组件数" width="80" align="center">
            <template #default="{row}"><span style="font-weight:600">{{ fieldCount(row) }}</span></template>
          </el-table-column>
          <el-table-column label="访问量" width="90" align="center">
            <template #default="{row}"><span style="font-size:12px;color:#909399">-</span></template>
          </el-table-column>
          <el-table-column label="最后修改" width="120">
            <template #default="{row}"><span style="font-size:12px;color:#909399">-</span></template>
          </el-table-column>
          <el-table-column label="状态" width="85" align="center">
            <template #default="{row}"><span class="order-status-badge os-done">已发布</span></template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right" align="center">
            <template #default="{row}">
              <div class="om-actions">
                <button class="om-action-btn" title="编辑" @click="openEdit(row.type)"><el-icon :size="13"><EditPen /></el-icon></button>
                <button class="om-action-btn" title="预览"><el-icon :size="13"><View /></el-icon></button>
                <button class="om-action-btn" title="设置"><el-icon :size="13"><Setting /></el-icon></button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="editType ? '编辑页面布局' : '新增页面'" width="700px" destroy-on-close>
      <template v-if="editType">
        <div v-if="currentSchema">
          <el-form v-loading="loading" label-width="100px" label-position="top">
            <el-form-item v-for="(field, key) in currentSchema" :key="key" :label="field.label">
              <template v-if="field.type === 'boolean'"><el-switch v-model="formData[key]" /></template>
              <template v-else><el-input v-model="formData[key]" /></template>
            </el-form-item>
            <template v-if="editType==='HOME'">
              <el-divider content-position="left">首页轮播设置</el-divider>
              <el-form-item label="启用轮播"><el-switch v-model="formData.hero_enabled" /></el-form-item>
              <el-form-item label="切换间隔(ms)"><el-input-number v-model="heroInterval" :min="1000" :step="500" :max="30000" /></el-form-item>
              <el-divider content-position="left">首页模块开关</el-divider>
              <el-form-item label="显示推荐商品"><el-switch v-model="formData.showcase_enabled" /></el-form-item>
              <el-form-item label="显示热门商品"><el-switch v-model="formData.popular_enabled" /></el-form-item>
            </template>
          </el-form>
        </div>
        <el-empty v-else description="该页面暂无可配置项" :image-size="48" />
      </template>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="savePage" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getPageConfigs, updatePageConfig } from '../../api/admin'
import { ElMessage } from 'element-plus'
import { Plus, Download, Search, EditPen, View, Setting, HomeFilled, ShoppingBag, Picture, ShoppingCartFull, CreditCard, CircleCheck, Files, TrendCharts } from '@element-plus/icons-vue'

const activeTab=ref('HOME');const loading=ref(false);const saving=ref(false)
const dialogVisible=ref(false);const editType=ref('')
const statusFilter=ref('all');const searchKeyword=ref('')
const formData=ref({hero_enabled:false,showcase_enabled:false,popular_enabled:false})
const heroInterval=ref(3000)
const currentSchema=ref(null)
const savedConfigs=ref({})

const pages = [
  { type:'HOME', label:'首页', icon:HomeFilled },
  { type:'PRODUCT_LIST', label:'商城页', icon:ShoppingBag },
  { type:'PRODUCT_DETAIL', label:'产品详情页', icon:Picture },
  { type:'CART', label:'购物车页', icon:ShoppingCartFull },
  { type:'PAYMENT', label:'支付页', icon:CreditCard },
  { type:'THANK_YOU', label:'感谢页', icon:CircleCheck },
]

const pageList = computed(()=>pages)
const filteredPages = computed(()=>{
  let list=pageList.value
  if(searchKeyword.value){const kw=searchKeyword.value.toLowerCase();list=list.filter(p=>p.label.toLowerCase().includes(kw)||pagePath(p.type).toLowerCase().includes(kw))}
  return list
})

function pagePath(t){const m={HOME:'/',PRODUCT_LIST:'/shop',PRODUCT_DETAIL:'/product/:id',CART:'/cart',PAYMENT:'/payment',THANK_YOU:'/thank-you'};return m[t]||'/'}
function pageIcon(t){const m={HOME:HomeFilled,PRODUCT_LIST:ShoppingBag,PRODUCT_DETAIL:Picture,CART:ShoppingCartFull,PAYMENT:CreditCard,THANK_YOU:CircleCheck};return m[t]||Files}
function fieldCount(row){return row.type==='HOME'?12:row.type==='PRODUCT_LIST'?8:4}

const kpis=ref([
  {icon:Files,bg:'rgba(108,92,231,0.1)',color:'#6c5ce7',label:'总页面数',val:6,sub:'管理所有页面'},
  {icon:CircleCheck,bg:'rgba(0,184,148,0.1)',color:'#00b894',label:'已发布',val:6,sub:'占比 100%'},
  {icon:EditPen,bg:'rgba(253,203,110,0.12)',color:'#e67e22',label:'草稿',val:0,sub:'待发布'},
  {icon:View,bg:'rgba(225,112,85,0.1)',color:'#e17055',label:'已下线',val:0,sub:'已停用'},
  {icon:TrendCharts,bg:'rgba(116,185,255,0.1)',color:'#74b9ff',label:'总访问量',val:'12,580',sub:'累计PV'},
])

onMounted(()=>loadConfigs())

async function loadConfigs(){loading.value=true;try{const r=await getPageConfigs();if(r.data){const m={};r.data.forEach(c=>{m[c.pageType]=c;try{const j=JSON.parse(c.configJson);m[c.pageType+'_json']=j}catch{}});savedConfigs.value=m}}catch{}finally{loading.value=false}}

async function switchTab(t){activeTab.value=t;await loadSchema()}

async function openEdit(type){
  if(!type){ElMessage.info('新增页面功能开发中');return}
  editType.value=type;dialogVisible.value=true;await loadSchema()
}

async function loadSchema(){
  const page=pages.find(p=>p.type===editType.value);if(!page){currentSchema.value=null;return}
  if(editType.value==='HOME'){currentSchema.value={hero_title:{label:'标题',type:'text'},hero_subtitle:{label:'副标题',type:'text'}};const saved=savedConfigs.value[editType.value+'_json']||{};formData.value={hero_enabled:saved.hero_enabled??true,showcase_enabled:saved.showcase_enabled??true,popular_enabled:saved.popular_enabled??true,...saved}}
  else{currentSchema.value={}}
}

async function savePage(){
  saving.value=true
  try{
    const current=savedConfigs.value[editType.value]||{}
    const data={...formData.value};delete data.hero_enabled;delete data.showcase_enabled;delete data.popular_enabled
    data.hero_enabled=formData.value.hero_enabled??true
    data.showcase_enabled=formData.value.showcase_enabled??true
    data.popular_enabled=formData.value.popular_enabled??true
    await updatePageConfig(editType.value,data)
    ElMessage.success('保存成功');dialogVisible.value=false;loadConfigs()
  }catch{}finally{saving.value=false}
}
</script>

<style scoped>
.pg-page{max-width:100%}
.page-header{display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:20px}
.page-header h2{font-size:20px;font-weight:600;margin:0}
.page-subtitle{font-size:12px;color:#909399;margin-top:3px}
.page-header-actions{display:flex;gap:8px}
.btn{padding:8px 16px;border:none;border-radius:8px;cursor:pointer;font-size:13px;font-weight:500;display:inline-flex;align-items:center;gap:6px;transition:all .2s;font-family:inherit}
.btn-primary{background:#6c5ce7;color:#fff}.btn-primary:hover{background:#5b4cdb}
.btn-outline{background:transparent;border:1px solid #e0e3e8;color:#606266}.btn-outline:hover{border-color:#6c5ce7;color:#6c5ce7}
.card{background:#fff;border-radius:12px;border:1px solid #e0e3e8;box-shadow:0 1px 4px rgba(0,0,0,.04)}
.card-body{padding:20px}

/* KPI */
.om-kpi-strip{display:grid;grid-template-columns:repeat(5,1fr);gap:14px;margin-bottom:16px}
.om-kpi-card{background:#fff;border-radius:12px;padding:16px 18px;border:1px solid #e0e3e8;box-shadow:0 1px 4px rgba(0,0,0,.04);display:flex;align-items:center;gap:14px;transition:all .2s}
.om-kpi-card:hover{box-shadow:0 2px 12px rgba(0,0,0,.06);transform:translateY(-1px)}
.om-kpi-icon{width:42px;height:42px;border-radius:11px;display:flex;align-items:center;justify-content:center;flex-shrink:0}
.om-kpi-body{flex:1;min-width:0}
.om-kpi-val{font-size:20px;font-weight:700;line-height:1.2}
.om-kpi-label{font-size:12px;color:#909399}
.om-kpi-sub{font-size:11px;color:#909399;margin-top:2px}

/* Toolbar */
.pm-toolbar{display:flex;align-items:center;gap:12px;flex-wrap:wrap}
.pm-tab-group{display:flex;gap:2px;border-bottom:2px solid #e0e3e8;flex:1}
.pm-tab{padding:7px 14px;font-size:13px;font-weight:500;border:none;background:transparent;cursor:pointer;color:#909399;border-bottom:2px solid transparent;margin-bottom:-2px;border-radius:6px 6px 0 0;white-space:nowrap;font-family:inherit;transition:all .2s;display:flex;align-items:center;gap:3px}
.pm-tab:hover{color:#606266;background:#f5f7fa}
.pm-tab.active{color:#6c5ce7;border-bottom-color:#6c5ce7;background:rgba(108,92,231,.08)}
.pm-tab-count{background:#f5f7fa;color:#909399;font-size:10px;padding:1px 6px;border-radius:8px;margin-left:4px;font-weight:600}
.pm-tab.active .pm-tab-count{background:rgba(108,92,231,.15);color:#6c5ce7}
.tab-dot{width:6px;height:6px;border-radius:50%;flex-shrink:0}
.pm-toolbar-right{display:flex;align-items:center;gap:8px;flex-shrink:0}
.pm-search-wrap{position:relative;display:flex;align-items:center}
.pm-search-wrap :deep(.el-icon){position:absolute;left:10px;color:#909399;font-size:12px;z-index:1}
.pm-search-wrap input{padding:7px 10px 7px 30px;border:1px solid #e0e3e8;border-radius:20px;font-size:12px;outline:none;width:200px;background:#f5f7fa;font-family:inherit;transition:all .2s}
.pm-search-wrap input:focus{border-color:#6c5ce7;background:#fff;box-shadow:0 0 0 3px rgba(108,92,231,.08)}

/* Status badges */
.order-status-badge{display:inline-block;padding:2px 8px;border-radius:20px;font-size:11px;font-weight:500;white-space:nowrap}
.os-done{background:rgba(0,184,148,.1);color:#00b894}

/* Actions */
.om-actions{display:flex;gap:4px;justify-content:center}
.om-action-btn{width:28px;height:28px;padding:0;border-radius:6px;background:transparent;border:1px solid #e0e3e8;color:#909399;cursor:pointer;transition:all .2s;display:flex;align-items:center;justify-content:center}
.om-action-btn:hover{border-color:#6c5ce7;color:#6c5ce7;background:rgba(108,92,231,.08)}

@media(max-width:1200px){.om-kpi-strip{grid-template-columns:repeat(3,1fr)}}
@media(max-width:768px){.om-kpi-strip{grid-template-columns:repeat(2,1fr)}.pm-toolbar{flex-direction:column;align-items:stretch}}
</style>
