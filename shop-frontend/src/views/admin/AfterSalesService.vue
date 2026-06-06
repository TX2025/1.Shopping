<template>
  <div class="as-page">
    <div class="page-header">
      <div>
        <h2><el-icon :size="20" color="#6c5ce7" style="margin-right:8px"><Refresh /></el-icon>售后服务</h2>
        <div class="page-subtitle">处理退货退款、换货维修等售后申请</div>
      </div>
      <div class="page-header-actions">
        <button class="btn btn-outline" @click="ElMessage.info('导出记录')"><el-icon :size="14"><Download /></el-icon> 导出记录</button>
        <button class="btn btn-primary" @click="openAdd"><el-icon :size="14"><Plus /></el-icon> 新建售后单</button>
      </div>
    </div>

    <!-- KPI Strip -->
    <div class="om-kpi-strip" v-if="stats">
      <div class="om-kpi-card"><div class="om-kpi-icon" style="background:rgba(253,203,110,.12);color:#e67e22"><el-icon :size="17"><Timer /></el-icon></div><div class="om-kpi-body"><div class="om-kpi-label">待审核</div><div class="om-kpi-val" style="color:#e67e22">{{ stats.pending }}</div><div class="om-kpi-sub">需及时处理</div></div></div>
      <div class="om-kpi-card"><div class="om-kpi-icon" style="background:rgba(108,92,231,.1);color:#6c5ce7"><el-icon :size="17"><RefreshRight /></el-icon></div><div class="om-kpi-body"><div class="om-kpi-label">处理中</div><div class="om-kpi-val" style="color:#6c5ce7">{{ stats.processing }}</div><div class="om-kpi-sub">进行中</div></div></div>
      <div class="om-kpi-card"><div class="om-kpi-icon" style="background:rgba(0,184,148,.1);color:#00b894"><el-icon :size="17"><CircleCheck /></el-icon></div><div class="om-kpi-body"><div class="om-kpi-label">已完成</div><div class="om-kpi-val" style="color:#00b894">{{ stats.completed }}</div><div class="om-kpi-sub">已结案</div></div></div>
      <div class="om-kpi-card"><div class="om-kpi-icon" style="background:rgba(225,112,85,.1);color:#e17055"><el-icon :size="17"><CircleClose /></el-icon></div><div class="om-kpi-body"><div class="om-kpi-label">已拒绝</div><div class="om-kpi-val" style="color:#e17055">{{ stats.rejected }}</div><div class="om-kpi-sub">未通过</div></div></div>
      <div class="om-kpi-card"><div class="om-kpi-icon" style="background:rgba(116,185,255,.1);color:#74b9ff"><el-icon :size="17"><Money /></el-icon></div><div class="om-kpi-body"><div class="om-kpi-label">退款金额</div><div class="om-kpi-val" style="color:#74b9ff">{{ refundTotal }}</div><div class="om-kpi-sub">累计退款</div></div></div>
    </div>

    <!-- Table Card -->
    <div class="card">
      <div class="card-body" style="padding:12px 16px">
        <div class="pm-toolbar">
          <div class="pm-tab-group">
            <button v-for="t in statusTabs" :key="t.key" class="pm-tab" :class="{active:statusFilter===t.key}" @click="statusFilter=t.key;page=1;load()">
              <el-icon v-if="t.icon" :size="11" :style="{color:t.iconColor}"><component :is="t.icon" /></el-icon>
              {{ t.label }} <span class="pm-tab-count">{{ tabCount(t.key) }}</span>
            </button>
          </div>
          <div class="pm-toolbar-right">
            <select class="pm-select" v-model="typeFilter" @change="page=1;load()">
              <option value="">全部类型</option><option value="RETURN_REFUND">退货退款</option><option value="EXCHANGE">换货</option><option value="REFUND_ONLY">仅退款</option><option value="REPAIR">维修</option>
            </select>
            <div class="pm-search-wrap"><el-icon :size="12"><Search /></el-icon><input v-model="searchKeyword" placeholder="搜索单号/客户..." @input="onSearch" /></div>
          </div>
        </div>

        <transition name="fade"><div class="pm-batch-bar" v-if="selectedIds.length" style="margin:8px 0">
          <span>已选 <strong>{{ selectedIds.length }}</strong> 个</span>
          <div style="margin-left:auto;display:flex;gap:6px">
            <button class="btn btn-sm btn-outline" @click="batchApprove" style="color:#00b894;border-color:#00b894"><el-icon :size="12"><Check /></el-icon> 审核通过</button>
            <button class="btn btn-sm btn-outline" @click="batchReject" style="color:#e17055;border-color:#e17055"><el-icon :size="12"><Close /></el-icon> 拒绝</button>
          </div>
        </div></transition>

        <div style="overflow:auto">
          <el-table :data="list" stripe v-loading="loading" row-key="id" size="small" @selection-change="onSelectChange">
            <el-table-column type="selection" width="40" />
            <el-table-column label="售后单号" width="140">
              <template #default="{row}"><span class="order-id">{{ row.afterSaleNo }}</span></template>
            </el-table-column>
            <el-table-column label="关联订单" width="140">
              <template #default="{row}"><span class="order-id">{{ row.orderNo }}</span></template>
            </el-table-column>
            <el-table-column prop="customer" label="客户" width="90" />
            <el-table-column label="类型" width="100" align="center">
              <template #default="{row}"><span class="as-type-badge" :class="'as-type-'+row.type">{{ typeLabel(row.type) }}</span></template>
            </el-table-column>
            <el-table-column prop="reason" label="原因" min-width="120" show-overflow-tooltip />
            <el-table-column label="金额" width="100">
              <template #default="{row}"><span style="font-weight:600;color:#6c5ce7">{{ Number(row.amount)>0?'¥'+Number(row.amount).toLocaleString():'-' }}</span></template>
            </el-table-column>
            <el-table-column label="状态" width="85" align="center">
              <template #default="{row}"><span class="order-status-badge" :class="statusBadge(row.status)">{{ statusLabel(row.status) }}</span></template>
            </el-table-column>
            <el-table-column label="操作" width="110" fixed="right" align="center">
              <template #default="{row}">
                <div class="om-actions">
                  <button v-if="row.status==='PENDING'" class="om-action-btn" title="审核通过" @click="approve(row)" style="color:#00b894;border-color:#00b894"><el-icon :size="13"><Check /></el-icon></button>
                  <button v-if="row.status==='PENDING'" class="om-action-btn" title="拒绝" @click="reject(row)" style="color:#e17055;border-color:#e17055"><el-icon :size="13"><Close /></el-icon></button>
                  <button class="om-action-btn" title="详情"><el-icon :size="13"><View /></el-icon></button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div class="om-pagination" v-if="total>0">
          <span>共 {{ total }} 条 · {{ page }}/{{ Math.max(1,Math.ceil(total/size)) }}</span>
          <el-pagination v-model:current-page="page" :page-size="size" :page-sizes="[10,20,50]" :total="total"
            layout="sizes,prev,pager,next" @size-change="s=>{size=s;page=1;load()}" @current-change="load" background small />
        </div>
      </div>
    </div>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="editId?'编辑售后单':'新建售后单'" width="520px" destroy-on-close>
      <el-form :model="form" label-width="80px" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="售后单号"><el-input v-model="form.afterSaleNo" :disabled="!!editId" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="关联订单号"><el-input v-model="form.orderNo" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="客户"><el-input v-model="form.customer" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="金额"><el-input-number v-model="form.amount" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="类型"><el-select v-model="form.type" style="width:100%">
            <el-option v-for="t in types" :key="t.value" :label="t.label" :value="t.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态"><el-select v-model="form.status" style="width:100%">
            <el-option v-for="s in statuses" :key="s.value" :label="s.label" :value="s.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="原因"><el-input v-model="form.reason" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save" :loading="saving">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getAfterSales, createAfterSale, updateAfterSale, deleteAfterSale, getAfterSaleStats } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, Search, Check, Close, View, Refresh, Timer, Loading, RefreshRight, CircleCheck, CircleClose, Money } from '@element-plus/icons-vue'

const list=ref([]);const loading=ref(false);const stats=ref(null)
const page=ref(1);const size=ref(10);const total=ref(0)
const statusFilter=ref('all');const typeFilter=ref('');const searchKeyword=ref('')
const selectedIds=ref([])
const dialogVisible=ref(false);const editId=ref(null);const saving=ref(false)
const form=ref({afterSaleNo:'',orderNo:'',customer:'',type:'RETURN_REFUND',reason:'',amount:0,status:'PENDING'})
let searchTimer=null

const statusTabs=[{key:'all',label:'全部'},{key:'pending',label:'待审核',icon:Timer,iconColor:'#e67e22'},{key:'processing',label:'处理中',icon:Loading,iconColor:'#6c5ce7'},{key:'done',label:'已完成',icon:CircleCheck,iconColor:'#00b894'},{key:'rejected',label:'已拒绝',icon:CircleClose,iconColor:'#e17055'}]
const types=[{value:'RETURN_REFUND',label:'退货退款'},{value:'EXCHANGE',label:'换货'},{value:'REFUND_ONLY',label:'仅退款'},{value:'REPAIR',label:'维修'}]
const statuses=[{value:'PENDING',label:'待审核'},{value:'PROCESSING',label:'处理中'},{value:'COMPLETED',label:'已完成'},{value:'REJECTED',label:'已拒绝'}]
const typeLabels={RETURN_REFUND:'退货退款',EXCHANGE:'换货',REFUND_ONLY:'仅退款',REPAIR:'维修'}
const statusLabels={PENDING:'待审核',PROCESSING:'处理中',COMPLETED:'已完成',REJECTED:'已拒绝'}
const statusMap={pending:'PENDING',processing:'PROCESSING',done:'COMPLETED',rejected:'REJECTED'}

function typeLabel(v){return typeLabels[v]||v}
function statusLabel(v){return statusLabels[v]||v}
function statusBadge(s){return{PENDING:'os-pending',PROCESSING:'os-shipping',COMPLETED:'os-done',REJECTED:'os-refund'}[s]||''}
function tabCount(k){if(k==='all')return total.value;const s=stats.value||{};if(k==='pending')return s.pending||0;if(k==='processing')return s.processing||0;if(k==='done')return s.completed||0;if(k==='rejected')return s.rejected||0;return 0}
const refundTotal=computed(()=>{if(!stats.value)return'¥0';const total=list.value.filter(r=>r.status==='COMPLETED').reduce((s,r)=>s+Number(r.amount||0),0);return total>=10000?'¥'+(total/10000).toFixed(1)+'万':'¥'+total.toLocaleString()})

onMounted(()=>{load();loadStats()})

function onSelectChange(rows){selectedIds.value=rows.map(r=>r.id)}
function onSearch(){clearTimeout(searchTimer);searchTimer=setTimeout(()=>{page.value=1;load()},300)}

async function load(){
  loading.value=true
  try{
    const res=await getAfterSales({page:page.value,size:size.value})
    list.value=res.data?.list||[];total.value=res.data?.total||0
    // client-side filters
    let l=list.value
    if(statusFilter.value!=='all'){const m={pending:'PENDING',processing:'PROCESSING',done:'COMPLETED',rejected:'REJECTED'};l=l.filter(r=>r.status===m[statusFilter.value])}
    if(typeFilter.value)l=l.filter(r=>r.type===typeFilter.value)
    if(searchKeyword.value){const kw=searchKeyword.value.toLowerCase();l=l.filter(r=>(r.afterSaleNo||'').toLowerCase().includes(kw)||(r.customer||'').toLowerCase().includes(kw)||(r.orderNo||'').toLowerCase().includes(kw))}
    list.value=l;selectedIds.value=[]
  }catch{}finally{loading.value=false}
}

async function loadStats(){try{const r=await getAfterSaleStats();if(r.data)stats.value=r.data}catch{}}

function openAdd(){editId.value=null;form.value={afterSaleNo:'',orderNo:'',customer:'',type:'RETURN_REFUND',reason:'',amount:0,status:'PENDING'};dialogVisible.value=true}
function openEdit(row){editId.value=row.id;form.value={...row,amount:Number(row.amount)||0};dialogVisible.value=true}

async function save(){
  saving.value=true
  try{if(editId.value){await updateAfterSale(editId.value,form.value);ElMessage.success('更新成功')}else{await createAfterSale(form.value);ElMessage.success('创建成功')}
  dialogVisible.value=false;load();loadStats()}catch{}finally{saving.value=false}
}

async function approve(row){try{await updateAfterSale(row.id,{...row,status:'PROCESSING'});ElMessage.success('已审核通过');load();loadStats()}catch{}}
async function reject(row){try{await updateAfterSale(row.id,{...row,status:'REJECTED'});ElMessage.warning('已拒绝');load();loadStats()}catch{}}
async function batchApprove(){try{await Promise.all(selectedIds.value.map(id=>{const r=list.value.find(x=>x.id===id);return r?updateAfterSale(id,{...r,status:'PROCESSING'}):null}));ElMessage.success(`已审核 ${selectedIds.value.length} 个`);load();loadStats()}catch{}}
async function batchReject(){try{await Promise.all(selectedIds.value.map(id=>{const r=list.value.find(x=>x.id===id);return r?updateAfterSale(id,{...r,status:'REJECTED'}):null}));ElMessage.warning(`已拒绝 ${selectedIds.value.length} 个`);load();loadStats()}catch{}}
</script>

<style scoped>
.as-page{max-width:100%}
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
.pm-tab-group{display:flex;gap:2px;border-bottom:2px solid #e0e3e8;flex:1;overflow-x:auto}
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

.order-id{color:#6c5ce7;font-weight:500}
.as-type-badge{display:inline-block;padding:2px 8px;border-radius:20px;font-size:11px;font-weight:500;white-space:nowrap}
.as-type-RETURN_REFUND{background:rgba(225,112,85,.12);color:#e17055}
.as-type-EXCHANGE{background:rgba(116,185,255,.15);color:#2980b9}
.as-type-REFUND_ONLY{background:rgba(108,92,231,.1);color:#6c5ce7}
.as-type-REPAIR{background:rgba(253,203,110,.15);color:#d68910}

.order-status-badge{display:inline-block;padding:2px 8px;border-radius:20px;font-size:11px;font-weight:500;white-space:nowrap}
.os-pending{background:rgba(253,203,110,.15);color:#e67e22}
.os-shipping{background:rgba(108,92,231,.1);color:#6c5ce7}
.os-done{background:rgba(0,184,148,.1);color:#00b894}
.os-refund{background:rgba(225,112,85,.12);color:#e17055}

.om-actions{display:flex;gap:4px;justify-content:center}
.om-action-btn{width:28px;height:28px;padding:0;border-radius:6px;background:transparent;border:1px solid #e0e3e8;color:#909399;cursor:pointer;transition:all .2s;display:flex;align-items:center;justify-content:center}
.om-action-btn:hover{border-color:#6c5ce7;color:#6c5ce7;background:rgba(108,92,231,.08)}
.om-pagination{display:flex;align-items:center;justify-content:space-between;padding:12px 0 0;border-top:1px solid #e0e3e8;margin-top:8px;font-size:12px;color:#909399}

@media(max-width:1200px){.om-kpi-strip{grid-template-columns:repeat(3,1fr)}}
@media(max-width:768px){.om-kpi-strip{grid-template-columns:repeat(2,1fr)}}
</style>
