<template>
  <div class="sh-page">
    <div class="page-header">
      <div>
        <h2><el-icon :size="20" color="#6c5ce7" style="margin-right:8px"><Van /></el-icon>配送服务</h2>
        <div class="page-subtitle">管理配送方式、运费模板与物流追踪</div>
      </div>
      <div class="page-header-actions">
        <button class="btn btn-outline" @click="ElMessage.info('导出')"><el-icon :size="14"><Download /></el-icon> 导出</button>
        <button class="btn btn-primary" @click="openAdd"><el-icon :size="14"><Plus /></el-icon> 添加配送方式</button>
      </div>
    </div>

    <!-- KPI Strip -->
    <div class="om-kpi-strip">
      <div class="om-kpi-card">
        <div class="om-kpi-icon" style="background:rgba(108,92,231,.1);color:#6c5ce7"><el-icon :size="17"><Van /></el-icon></div>
        <div class="om-kpi-body"><div class="om-kpi-label">配送方式</div><div class="om-kpi-val" style="color:#6c5ce7">{{ list.length }}</div><div class="om-kpi-sub">启用 {{ enabledCount }} 个</div></div>
      </div>
      <div class="om-kpi-card">
        <div class="om-kpi-icon" style="background:rgba(0,184,148,.1);color:#00b894"><el-icon :size="17"><Box /></el-icon></div>
        <div class="om-kpi-body"><div class="om-kpi-label">配送订单量</div><div class="om-kpi-val" style="color:#00b894">0</div><div class="om-kpi-sub">本月</div></div>
      </div>
      <div class="om-kpi-card">
        <div class="om-kpi-icon" style="background:rgba(253,203,110,.12);color:#e67e22"><el-icon :size="17"><Money /></el-icon></div>
        <div class="om-kpi-body"><div class="om-kpi-label">平均运费</div><div class="om-kpi-val" style="color:#e67e22">¥{{ avgFee }}</div><div class="om-kpi-sub">基准价</div></div>
      </div>
      <div class="om-kpi-card">
        <div class="om-kpi-icon" style="background:rgba(116,185,255,.1);color:#74b9ff"><el-icon :size="17"><CircleCheck /></el-icon></div>
        <div class="om-kpi-body"><div class="om-kpi-label">准时交付率</div><div class="om-kpi-val" style="color:#74b9ff">96.2%</div><div class="om-kpi-sub">目标≥95%</div></div>
      </div>
      <div class="om-kpi-card">
        <div class="om-kpi-icon" style="background:rgba(225,112,85,.1);color:#e17055"><el-icon :size="17"><TrendCharts /></el-icon></div>
        <div class="om-kpi-body"><div class="om-kpi-label">物流成本</div><div class="om-kpi-val" style="color:#e17055">¥3.2万</div><div class="om-kpi-sub">本月</div></div>
      </div>
    </div>

    <!-- Table Card -->
    <div class="card">
      <div class="card-body" style="padding:12px 16px">
        <div class="pm-toolbar" style="margin-bottom:0">
          <div class="pm-tab-group">
            <button class="pm-tab" :class="{active:statusFilter==='all'}" @click="statusFilter='all'">全部 <span class="pm-tab-count">{{ list.length }}</span></button>
            <button class="pm-tab" :class="{active:statusFilter==='ON'}" @click="statusFilter='ON'"><i class="tab-dot" style="background:#00b894;margin-right:3px"></i>启用 <span class="pm-tab-count">{{ enabledCount }}</span></button>
            <button class="pm-tab" :class="{active:statusFilter==='OFF'}" @click="statusFilter='OFF'"><i class="tab-dot" style="background:#e17055;margin-right:3px"></i>停用 <span class="pm-tab-count">{{ list.length - enabledCount }}</span></button>
          </div>
        </div>
        <div style="overflow:auto;margin-top:8px">
          <el-table :data="filteredList" stripe v-loading="loading" row-key="id" size="small">
            <el-table-column label="配送方式" min-width="150">
              <template #default="{row}"><el-icon :size="16" :color="shipIconColor(row)" style="margin-right:8px"><component :is="shipIcon(row)" /></el-icon><strong>{{ row.name }}</strong></template>
            </el-table-column>
            <el-table-column prop="region" label="配送区域" min-width="110" />
            <el-table-column label="运费" width="100">
              <template #default="{row}"><span style="font-weight:600">{{ Number(row.fee)===0?'免费':'¥'+Number(row.fee).toFixed(2) }}</span></template>
            </el-table-column>
            <el-table-column label="免费门槛" width="130">
              <template #default="{row}">{{ row.freeThreshold&&Number(row.freeThreshold)>0?'满¥'+Number(row.freeThreshold).toFixed(2)+'免运费':'无' }}</template>
            </el-table-column>
            <el-table-column prop="estimate" label="预计时效" width="120" />
            <el-table-column label="订单量" width="80" align="center">
              <template #default="{row}"><span style="font-weight:600">0</span></template>
            </el-table-column>
            <el-table-column label="状态" width="80" align="center">
              <template #default="{row}"><span class="order-status-badge" :class="row.status==='ON'?'os-done':'os-cancelled'">{{ row.status==='ON'?'启用':'停用' }}</span></template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right" align="center">
              <template #default="{row}">
                <div class="om-actions">
                  <button class="om-action-btn" title="编辑" @click="openEdit(row)"><el-icon :size="13"><EditPen /></el-icon></button>
                  <button class="om-action-btn" :title="row.status==='ON'?'停用':'启用'" @click="toggleStatus(row,row.status!=='ON')" :style="row.status==='ON'?{color:'#e17055',borderColor:'#e17055'}:{color:'#67C23A',borderColor:'#67C23A'}"><el-icon :size="13"><CircleClose v-if="row.status==='ON'" /><CircleCheck v-else /></el-icon></button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="editId?'编辑配送方式':'添加配送方式'" width="500px" destroy-on-close>
      <el-form :model="form" label-width="100px" label-position="top">
        <el-row :gutter="16">
          <el-col :span="14"><el-form-item label="配送方式名称"><el-input v-model="form.name" placeholder="如 标准快递" /></el-form-item></el-col>
          <el-col :span="10"><el-form-item label="状态"><el-switch v-model="form.status" active-value="ON" inactive-value="OFF" inline-prompt active-text="启用" inactive-text="停用" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="配送区域"><el-input v-model="form.region" placeholder="如 全国 / 一二线城市" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="运费 (¥)"><el-input-number v-model="form.fee" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="免费门槛 (¥)"><el-input-number v-model="form.freeThreshold" :min="0" :precision="2" style="width:100%" placeholder="无" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="预计时效"><el-input v-model="form.estimate" placeholder="如 3-5个工作日" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save" :loading="saving">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getShippingMethods, createShippingMethod, updateShippingMethod, deleteShippingMethod, updateShippingStatus } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, EditPen, CircleClose, CircleCheck, Van, Box, Money, TrendCharts, Ship, Timer, ShoppingBag } from '@element-plus/icons-vue'

const list=ref([]);const loading=ref(false);const statusFilter=ref('all')
const dialogVisible=ref(false);const editId=ref(null);const saving=ref(false)
const form=ref({name:'',fee:0,freeThreshold:null,region:'',estimate:'',status:'ON'})

onMounted(()=>load())

const filteredList=computed(()=>statusFilter.value==='all'?list.value:list.value.filter(s=>s.status===statusFilter.value))
const enabledCount=computed(()=>list.value.filter(s=>s.status==='ON').length)
const avgFee=computed(()=>{if(!list.value.length)return'0.00';const sum=list.value.reduce((s,r)=>s+Number(r.fee||0),0);return (sum/list.value.length).toFixed(1)})

function shipIcon(row){const n=row.name||'';if(n.includes('国际')||n.includes('海外'))return Ship;if(n.includes('加急'))return Timer;if(n.includes('自提')||n.includes('门店'))return ShoppingBag;return Van}
function shipIconColor(row){const n=row.name||'';if(n.includes('国际'))return '#74b9ff';if(n.includes('加急'))return '#e67e22';if(n.includes('自提'))return '#00b894';return '#6c5ce7'}

async function load(){loading.value=true;try{const res=await getShippingMethods();list.value=res.data||[]}catch{}finally{loading.value=false}}

function openAdd(){editId.value=null;form.value={name:'',fee:0,freeThreshold:null,region:'',estimate:'',status:'ON'};dialogVisible.value=true}
function openEdit(row){editId.value=row.id;form.value={...row};dialogVisible.value=true}

async function save(){
  if(!form.value.name.trim()){ElMessage.warning('请输入名称');return}
  saving.value=true
  try{if(editId.value){await updateShippingMethod(editId.value,form.value);ElMessage.success('更新成功')}else{await createShippingMethod(form.value);ElMessage.success('添加成功')}
  dialogVisible.value=false;load()}catch{}finally{saving.value=false}
}

async function toggleStatus(row,val){const s=val?'ON':'OFF';try{await updateShippingStatus(row.id,s);row.status=s;ElMessage.success(val?'已启用':'已停用')}catch{}}
async function handleDelete(row){try{await ElMessageBox.confirm(`确定删除「${row.name}」？`,'删除',{type:'warning',confirmButtonText:'确定'});await deleteShippingMethod(row.id);ElMessage.success('已删除');load()}catch{}}
</script>

<style scoped>
.sh-page{max-width:100%}
.page-header{display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:20px}
.page-header h2{font-size:20px;font-weight:600;margin:0;display:flex;align-items:center}
.page-subtitle{font-size:12px;color:#909399;margin-top:3px}
.page-header-actions{display:flex;gap:8px}
.btn{padding:8px 16px;border:none;border-radius:8px;cursor:pointer;font-size:13px;font-weight:500;display:inline-flex;align-items:center;gap:6px;transition:all .2s;font-family:inherit}
.btn-primary{background:#6c5ce7;color:#fff}.btn-primary:hover{background:#5b4cdb}
.btn-outline{background:transparent;border:1px solid #e0e3e8;color:#606266}.btn-outline:hover{border-color:#6c5ce7;color:#6c5ce7}
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

.order-status-badge{display:inline-block;padding:2px 8px;border-radius:20px;font-size:11px;font-weight:500;white-space:nowrap}
.os-done{background:rgba(0,184,148,.1);color:#00b894}
.os-cancelled{background:#f5f5f5;color:#b2b2b2}

.om-actions{display:flex;gap:4px;justify-content:center}
.om-action-btn{width:28px;height:28px;padding:0;border-radius:6px;background:transparent;border:1px solid #e0e3e8;color:#909399;cursor:pointer;transition:all .2s;display:flex;align-items:center;justify-content:center}
.om-action-btn:hover{border-color:#6c5ce7;color:#6c5ce7;background:rgba(108,92,231,.08)}

@media(max-width:1200px){.om-kpi-strip{grid-template-columns:repeat(3,1fr)}}
@media(max-width:768px){.om-kpi-strip{grid-template-columns:repeat(2,1fr)}}
</style>
