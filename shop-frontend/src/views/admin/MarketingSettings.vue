<template>
  <div class="mk-page">
    <div class="page-header">
      <div>
        <h2><el-icon :size="20" color="#6c5ce7" style="margin-right:8px"><Present /></el-icon>营销设置</h2>
        <div class="page-subtitle">管理促销活动、优惠券与营销工具</div>
      </div>
      <div class="page-header-actions">
        <button class="btn btn-outline" @click="ElMessage.info('导出')"><el-icon :size="14"><Download /></el-icon> 导出</button>
        <button class="btn btn-primary" @click="openAdd"><el-icon :size="14"><Plus /></el-icon> 创建活动</button>
      </div>
    </div>

    <!-- KPI Strip -->
    <div class="om-kpi-strip" v-if="stats">
      <div class="om-kpi-card">
        <div class="om-kpi-icon" style="background:rgba(108,92,231,.1);color:#6c5ce7"><el-icon :size="17"><Present /></el-icon></div>
        <div class="om-kpi-body"><div class="om-kpi-label">总活动数</div><div class="om-kpi-val" style="color:#6c5ce7">{{ stats.total }}</div><div class="om-kpi-sub">管理所有活动</div></div>
      </div>
      <div class="om-kpi-card">
        <div class="om-kpi-icon" style="background:rgba(0,184,148,.1);color:#00b894"><el-icon :size="17"><CircleCheck /></el-icon></div>
        <div class="om-kpi-body"><div class="om-kpi-label">进行中</div><div class="om-kpi-val" style="color:#00b894">{{ stats.active }}</div><div class="om-kpi-sub">当前有效活动</div></div>
      </div>
      <div class="om-kpi-card">
        <div class="om-kpi-icon" style="background:rgba(116,185,255,.1);color:#74b9ff"><el-icon :size="17"><Timer /></el-icon></div>
        <div class="om-kpi-body"><div class="om-kpi-label">未开始</div><div class="om-kpi-val" style="color:#74b9ff">{{ stats.upcoming }}</div><div class="om-kpi-sub">即将开始</div></div>
      </div>
      <div class="om-kpi-card">
        <div class="om-kpi-icon" style="background:rgba(180,180,180,.15);color:#909399"><el-icon :size="17"><CircleClose /></el-icon></div>
        <div class="om-kpi-body"><div class="om-kpi-label">已结束</div><div class="om-kpi-val" style="color:#909399">{{ stats.ended }}</div><div class="om-kpi-sub">历史活动</div></div>
      </div>
      <div class="om-kpi-card">
        <div class="om-kpi-icon" style="background:rgba(253,203,110,.12);color:#e67e22"><el-icon :size="17"><TrendCharts /></el-icon></div>
        <div class="om-kpi-body"><div class="om-kpi-label">总使用次数</div><div class="om-kpi-val" style="color:#e67e22">{{ (stats.totalUsage||0).toLocaleString() }}</div><div class="om-kpi-sub">优惠券核销</div></div>
      </div>
    </div>

    <!-- Toolbar + Table -->
    <div class="card">
      <div class="card-body" style="padding:12px 16px">
        <div class="pm-toolbar">
          <div class="pm-tab-group">
            <button class="pm-tab" :class="{active:statusFilter==='all'}" @click="statusFilter='all';load()">全部 <span class="pm-tab-count">{{ statusCount('all') }}</span></button>
            <button class="pm-tab" :class="{active:statusFilter==='active'}" @click="statusFilter='active';load()"><i class="tab-dot" style="background:#00b894;margin-right:3px"></i>进行中 <span class="pm-tab-count">{{ statusCount('active') }}</span></button>
            <button class="pm-tab" :class="{active:statusFilter==='upcoming'}" @click="statusFilter='upcoming';load()"><i class="tab-dot" style="background:#74b9ff;margin-right:3px"></i>未开始 <span class="pm-tab-count">{{ statusCount('upcoming') }}</span></button>
            <button class="pm-tab" :class="{active:statusFilter==='ended'}" @click="statusFilter='ended';load()"><i class="tab-dot" style="background:#909399;margin-right:3px"></i>已结束 <span class="pm-tab-count">{{ statusCount('ended') }}</span></button>
          </div>
          <div class="pm-toolbar-right">
            <select class="pm-select" v-model="typeFilter" @change="filterList">
              <option value="">全部类型</option><option value="满减">满减</option><option value="折扣">折扣</option><option value="赠品">赠品</option><option value="秒杀">秒杀</option>
            </select>
            <div class="pm-search-wrap"><el-icon :size="12"><Search /></el-icon><input v-model="searchKeyword" placeholder="搜索活动..." @input="filterList" /></div>
          </div>
        </div>
        <transition name="fade"><div class="pm-batch-bar" v-if="selectedIds.length" style="margin:8px 0">
          <span>已选 <strong>{{ selectedIds.length }}</strong> 个</span>
          <div style="margin-left:auto;display:flex;gap:6px">
            <button class="btn btn-sm btn-outline" @click="batchStatus('ended')">停止</button>
            <button class="btn btn-sm" style="color:#e17055;border:1px solid #e0e3e8" @click="batchDelete">删除</button>
          </div>
        </div></transition>
        <div style="overflow:auto">
          <el-table :data="filteredList" stripe row-key="id" size="small" @selection-change="onSelectChange">
            <el-table-column type="selection" width="40" />
            <el-table-column label="活动名称" min-width="140" prop="name" />
            <el-table-column label="类型" width="90" align="center">
              <template #default="{row}"><span class="mk-type-badge" :class="'mk-type-'+row.type">{{ row.type }}</span></template>
            </el-table-column>
            <el-table-column label="优惠" min-width="120" prop="discount" />
            <el-table-column label="使用次数" width="90" align="center">
              <template #default="{row}"><span style="font-weight:600">{{ (row.usageCount||0).toLocaleString() }}</span></template>
            </el-table-column>
            <el-table-column label="有效期" min-width="180">
              <template #default="{row}">{{ row.startDate }} ~ {{ row.endDate }}</template>
            </el-table-column>
            <el-table-column label="状态" width="90" align="center">
              <template #default="{row}">
                <span class="order-status-badge" :class="statusClass(row.status)">{{ statusLabel(row.status) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="110" fixed="right" align="center">
              <template #default="{row}">
                <div class="om-actions">
                  <button class="om-action-btn" title="编辑" @click="openEdit(row)"><el-icon :size="13"><EditPen /></el-icon></button>
                  <button v-if="row.status==='active'" class="om-action-btn" title="停止" @click="stopPromo(row)" style="color:#e17055;border-color:#e17055"><el-icon :size="13"><CircleClose /></el-icon></button>
                  <button class="om-action-btn danger" title="删除" @click="handleDelete(row)"><el-icon :size="13"><Delete /></el-icon></button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="editId?'编辑活动':'创建活动'" width="520px" destroy-on-close>
      <el-form :model="form" label-width="80px" label-position="top">
        <el-row :gutter="16">
          <el-col :span="16"><el-form-item label="活动名称"><el-input v-model="form.name" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="类型">
            <el-select v-model="form.type" style="width:100%"><el-option v-for="t in types" :key="t" :label="t" :value="t" /></el-select>
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="优惠描述"><el-input v-model="form.discount" placeholder="如 满200减30" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="开始日期"><el-input v-model="form.startDate" type="date" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="结束日期"><el-input v-model="form.endDate" type="date" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="使用次数"><el-input-number v-model="form.usageCount" :min="0" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态">
            <el-select v-model="form.status" style="width:100%"><el-option v-for="s in statuses" :key="s.value" :label="s.label" :value="s.value" /></el-select>
          </el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save" :loading="saving">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getPromotions, createPromotion, updatePromotion, deletePromotion, getPromotionStats } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, Search, EditPen, Delete, CircleClose, CircleCheck, Present, Timer, TrendCharts } from '@element-plus/icons-vue'

const list=ref([]);const stats=ref(null);const statusFilter=ref('all');const typeFilter=ref('');const searchKeyword=ref('')
const selectedIds=ref([]);const dialogVisible=ref(false);const editId=ref(null);const saving=ref(false)
const form=ref({name:'',type:'满减',discount:'',startDate:'',endDate:'',usageCount:0,status:'upcoming'})
const types=['满减','折扣','赠品','秒杀'];const statuses=[{value:'upcoming',label:'未开始'},{value:'active',label:'进行中'},{value:'ended',label:'已结束'}]

onMounted(()=>{load();loadStats()})

const filteredList=computed(()=>{
  let l=list.value
  if(typeFilter.value)l=l.filter(p=>p.type===typeFilter.value)
  if(searchKeyword.value){const kw=searchKeyword.value.toLowerCase();l=l.filter(p=>p.name.toLowerCase().includes(kw))}
  return l
})
function filterList(){}
function statusCount(s){if(s==='all')return list.value.length;return list.value.filter(p=>p.status===s).length}
function statusLabel(s){return{active:'进行中',upcoming:'未开始',ended:'已结束'}[s]||s}
function statusClass(s){return{active:'os-done',upcoming:'os-blue',ended:'os-cancelled'}[s]||''}
function onSelectChange(rows){selectedIds.value=rows.map(r=>r.id)}

async function load(){try{const r=await getPromotions(statusFilter.value==='all'?null:statusFilter.value);list.value=r.data||[];selectedIds.value=[]}catch{}}
async function loadStats(){try{const r=await getPromotionStats();if(r.data)stats.value=r.data}catch{}}

function openAdd(){editId.value=null;form.value={name:'',type:'满减',discount:'',startDate:'',endDate:'',usageCount:0,status:'upcoming'};dialogVisible.value=true}
function openEdit(row){editId.value=row.id;form.value={...row};dialogVisible.value=true}
async function save(){
  saving.value=true
  try{if(editId.value){await updatePromotion(editId.value,form.value);ElMessage.success('更新成功')}else{await createPromotion(form.value);ElMessage.success('创建成功')}
  dialogVisible.value=false;load();loadStats()}catch{}finally{saving.value=false}
}
async function stopPromo(row){try{await updatePromotion(row.id,{...row,status:'ended'});ElMessage.success('已停止');load();loadStats()}catch{}}
async function handleDelete(row){try{await ElMessageBox.confirm(`确定删除「${row.name}」？`,'删除',{type:'warning',confirmButtonText:'确定'});await deletePromotion(row.id);ElMessage.success('已删除');load();loadStats()}catch{}}
async function batchStatus(st){try{await Promise.all(selectedIds.value.map(id=>{const p=list.value.find(x=>x.id===id);return updatePromotion(id,{...p,status:st})}));ElMessage.success('已操作');load();loadStats()}catch{}}
async function batchDelete(){try{await ElMessageBox.confirm(`确定删除${selectedIds.value.length}个活动？`,'批量删除',{type:'warning'});await Promise.all(selectedIds.value.map(id=>deletePromotion(id)));ElMessage.success('已删除');load();loadStats()}catch{}}
</script>

<style scoped>
.mk-page{max-width:100%}
.page-header{display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:20px}
.page-header h2{font-size:20px;font-weight:600;margin:0;display:flex;align-items:center}
.page-subtitle{font-size:12px;color:#909399;margin-top:3px}
.page-header-actions{display:flex;gap:8px}
.btn{padding:8px 16px;border:none;border-radius:8px;cursor:pointer;font-size:13px;font-weight:500;display:inline-flex;align-items:center;gap:6px;transition:all .2s;font-family:inherit}
.btn-primary{background:#6c5ce7;color:#fff}.btn-primary:hover{background:#5b4cdb}
.btn-outline{background:transparent;border:1px solid #e0e3e8;color:#606266}.btn-outline:hover{border-color:#6c5ce7;color:#6c5ce7}
.btn-sm{padding:5px 12px;font-size:12px}
.card{background:#fff;border-radius:12px;border:1px solid #e0e3e8;box-shadow:0 1px 4px rgba(0,0,0,.04)}
.card-body{padding:20px}

.om-kpi-strip{display:grid;grid-template-columns:repeat(5,1fr);gap:14px;margin-bottom:16px}
.om-kpi-card{background:#fff;border-radius:12px;padding:16px 18px;border:1px solid #e0e3e8;box-shadow:0 1px 4px rgba(0,0,0,.04);display:flex;align-items:center;gap:14px;transition:all .2s}
.om-kpi-card:hover{box-shadow:0 2px 12px rgba(0,0,0,.06);transform:translateY(-1px)}
.om-kpi-icon{width:42px;height:42px;border-radius:11px;display:flex;align-items:center;justify-content:center;flex-shrink:0}
.om-kpi-body{flex:1;min-width:0}
.om-kpi-val{font-size:20px;font-weight:700;line-height:1.2}
.om-kpi-label{font-size:12px;color:#909399}
.om-kpi-sub{font-size:11px;color:#909399;margin-top:2px}

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
.pm-select{padding:7px 10px;border:1px solid #e0e3e8;border-radius:8px;font-size:12px;outline:none;background:#f5f7fa;color:#606266;cursor:pointer;font-family:inherit}
.pm-batch-bar{display:flex;align-items:center;gap:10px;padding:8px 12px;background:rgba(108,92,231,.06);border-radius:8px;border:1px solid rgba(108,92,231,.2);font-size:13px;color:#6c5ce7}
.fade-enter-active,.fade-leave-active{transition:all .25s}
.fade-enter-from,.fade-leave-to{opacity:0;transform:translateY(-4px)}

.mk-type-badge{display:inline-block;padding:2px 6px;border-radius:4px;font-size:10px;font-weight:600}
.mk-type-满减{background:rgba(108,92,231,.1);color:#6c5ce7}
.mk-type-折扣{background:rgba(253,203,110,.15);color:#d68910}
.mk-type-赠品{background:rgba(0,184,148,.1);color:#00b894}
.mk-type-秒杀{background:rgba(225,112,85,.12);color:#e17055}

.order-status-badge{display:inline-block;padding:2px 8px;border-radius:20px;font-size:11px;font-weight:500;white-space:nowrap}
.os-done{background:rgba(0,184,148,.1);color:#00b894}
.os-blue{background:rgba(116,185,255,.15);color:#2980b9}
.os-cancelled{background:#f5f5f5;color:#b2b2b2}

.om-actions{display:flex;gap:4px;justify-content:center}
.om-action-btn{width:28px;height:28px;padding:0;border-radius:6px;background:transparent;border:1px solid #e0e3e8;color:#909399;cursor:pointer;transition:all .2s;display:flex;align-items:center;justify-content:center}
.om-action-btn:hover{border-color:#6c5ce7;color:#6c5ce7;background:rgba(108,92,231,.08)}
.om-action-btn.danger:hover{border-color:#e17055;color:#e17055;background:rgba(225,112,85,.08)}

@media(max-width:1200px){.om-kpi-strip{grid-template-columns:repeat(3,1fr)}}
@media(max-width:768px){.om-kpi-strip{grid-template-columns:repeat(2,1fr)}}
</style>
