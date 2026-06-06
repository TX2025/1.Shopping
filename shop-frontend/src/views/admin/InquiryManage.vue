<template>
  <div class="iq-page">
    <div class="page-header">
      <div>
        <h2><el-icon :size="20" color="#6c5ce7" style="margin-right:8px"><ChatDotRound /></el-icon>询盘管理</h2>
        <div class="page-subtitle">管理客户咨询、询价与产品问询</div>
      </div>
      <div class="page-header-actions">
        <button class="btn btn-outline" @click="ElMessage.info('导出')"><el-icon :size="14"><Download /></el-icon> 导出询盘</button>
        <button class="btn btn-primary" @click="openAdd"><el-icon :size="14"><Plus /></el-icon> 新增询盘</button>
      </div>
    </div>

    <!-- KPI Strip -->
    <div class="om-kpi-strip" v-if="stats">
      <div class="om-kpi-card"><div class="om-kpi-icon" style="background:rgba(253,203,110,.12);color:#e67e22"><el-icon :size="17"><Timer /></el-icon></div><div class="om-kpi-body"><div class="om-kpi-label">待回复</div><div class="om-kpi-val" style="color:#e67e22">{{ stats.pending }}</div><div class="om-kpi-sub">需及时处理</div></div></div>
      <div class="om-kpi-card"><div class="om-kpi-icon" style="background:rgba(0,184,148,.1);color:#00b894"><el-icon :size="17"><ChatDotRound /></el-icon></div><div class="om-kpi-body"><div class="om-kpi-label">已回复</div><div class="om-kpi-val" style="color:#00b894">{{ stats.replied }}</div><div class="om-kpi-sub">等待客户反馈</div></div></div>
      <div class="om-kpi-card"><div class="om-kpi-icon" style="background:rgba(108,92,231,.1);color:#6c5ce7"><el-icon :size="17"><CircleCheck /></el-icon></div><div class="om-kpi-body"><div class="om-kpi-label">已转化</div><div class="om-kpi-val" style="color:#6c5ce7">{{ stats.converted }}</div><div class="om-kpi-sub">成功成交</div></div></div>
      <div class="om-kpi-card"><div class="om-kpi-icon" style="background:rgba(116,185,255,.1);color:#74b9ff"><el-icon :size="17"><TrendCharts /></el-icon></div><div class="om-kpi-body"><div class="om-kpi-label">转化率</div><div class="om-kpi-val" style="color:#74b9ff">{{ convRate }}%</div><div class="om-kpi-sub">{{ stats.converted }}/{{ totalCount }}</div></div></div>
      <div class="om-kpi-card"><div class="om-kpi-icon" style="background:rgba(225,112,85,.1);color:#e17055"><el-icon :size="17"><Connection /></el-icon></div><div class="om-kpi-body"><div class="om-kpi-label">询盘来源</div><div class="om-kpi-val" style="color:#e17055">{{ sourceCount }}</div><div class="om-kpi-sub">渠道数</div></div></div>
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
            <select class="pm-select" v-model="sourceFilter" @change="page=1;load()">
              <option value="">全部来源</option><option v-for="s in sources" :key="s" :value="s">{{ s }}</option>
            </select>
            <div class="pm-search-wrap"><el-icon :size="12"><Search /></el-icon><input v-model="searchKeyword" placeholder="搜索客户/产品..." @input="onSearch" /></div>
          </div>
        </div>

        <transition name="fade"><div class="pm-batch-bar" v-if="selectedIds.length" style="margin:8px 0">
          <span>已选 <strong>{{ selectedIds.length }}</strong> 条</span>
          <div style="margin-left:auto;display:flex;gap:6px">
            <button class="btn btn-sm btn-outline" @click="batchReply" style="color:#6c5ce7;border-color:#6c5ce7"><el-icon :size="12"><ChatDotRound /></el-icon> 批量回复</button>
            <button class="btn btn-sm btn-outline" style="color:#909399;border-color:#e0e3e8" @click="batchClose"><el-icon :size="12"><Close /></el-icon> 关闭</button>
          </div>
        </div></transition>

        <div style="overflow:auto">
          <el-table :data="filteredList" stripe v-loading="loading" row-key="id" size="small" @selection-change="onSelectChange">
            <el-table-column type="selection" width="40" />
            <el-table-column label="询盘编号" width="130">
              <template #default="{row}"><span class="order-id">{{ row.inquiryNo }}</span></template>
            </el-table-column>
            <el-table-column prop="customer" label="客户" width="100" />
            <el-table-column label="来源" width="90" align="center">
              <template #default="{row}"><el-icon :size="12" :style="{color:sourceColor(row.source)}" style="margin-right:4px"><component :is="sourceIcon(row.source)" /></el-icon>{{ row.source }}</template>
            </el-table-column>
            <el-table-column prop="interest" label="产品兴趣" min-width="120" show-overflow-tooltip />
            <el-table-column prop="summary" label="内容摘要" min-width="180" show-overflow-tooltip />
            <el-table-column label="时间" width="140">
              <template #default="{row}"><span style="font-size:12px;color:#909399">{{ formatTime(row.createTime) }}</span></template>
            </el-table-column>
            <el-table-column label="状态" width="80" align="center">
              <template #default="{row}"><span class="order-status-badge" :class="statusBadge(row.status)">{{ statusLabel(row.status) }}</span></template>
            </el-table-column>
            <el-table-column label="操作" width="110" fixed="right" align="center">
              <template #default="{row}">
                <div class="om-actions">
                  <button v-if="row.status==='PENDING'" class="om-action-btn" title="回复" @click="replyInquiry(row)" style="color:#6c5ce7;border-color:#6c5ce7"><el-icon :size="13"><ChatDotRound /></el-icon></button>
                  <button class="om-action-btn" title="详情"><el-icon :size="13"><View /></el-icon></button>
                  <button v-if="row.status==='REPLIED'" class="om-action-btn" title="跟进" @click="followUp(row)"><el-icon :size="13"><Phone /></el-icon></button>
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
    <el-dialog v-model="dialogVisible" :title="editId?'编辑询盘':'新增询盘'" width="520px" destroy-on-close>
      <el-form :model="form" label-width="80px" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="询盘编号"><el-input v-model="form.inquiryNo" :disabled="!!editId" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="来源"><el-select v-model="form.source" style="width:100%"><el-option v-for="s in sources" :key="s" :label="s" :value="s" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="客户"><el-input v-model="form.customer" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态"><el-select v-model="form.status" style="width:100%"><el-option v-for="s in stOptions" :key="s.value" :label="s.label" :value="s.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="产品兴趣"><el-input v-model="form.interest" placeholder="客户感兴趣的产品" /></el-form-item>
        <el-form-item label="内容摘要"><el-input v-model="form.summary" type="textarea" :rows="3" placeholder="询盘内容摘要..." /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save" :loading="saving">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getInquiries, createInquiry, updateInquiry, deleteInquiry, getInquiryStats } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, Search, ChatDotRound, View, Phone, Close, Timer, CircleCheck, TrendCharts, Connection, EditPen, Delete } from '@element-plus/icons-vue'

const list=ref([]);const loading=ref(false);const stats=ref(null)
const page=ref(1);const size=ref(10);const total=ref(0)
const statusFilter=ref('all');const sourceFilter=ref('');const searchKeyword=ref('')
const selectedIds=ref([]);let searchTimer=null
const dialogVisible=ref(false);const editId=ref(null);const saving=ref(false)
const form=ref({inquiryNo:'',customer:'',source:'官网',interest:'',summary:'',status:'PENDING'})
const sources=['官网','WhatsApp','邮件','展会','电话','APP','其他']
const statusTabs=[{key:'all',label:'全部'},{key:'pending',label:'待回复',icon:Timer,iconColor:'#e67e22'},{key:'replied',label:'已回复',icon:ChatDotRound,iconColor:'#00b894'},{key:'converted',label:'已转化',icon:CircleCheck,iconColor:'#6c5ce7'},{key:'closed',label:'已关闭',icon:Close,iconColor:'#909399'}]
const stOptions=[{value:'PENDING',label:'待回复'},{value:'REPLIED',label:'已回复'},{value:'CONVERTED',label:'已转化'},{value:'CLOSED',label:'已关闭'}]
const statusLabels={PENDING:'待回复',REPLIED:'已回复',CONVERTED:'已转化',CLOSED:'已关闭'}
const statusMap={pending:'PENDING',replied:'REPLIED',converted:'CONVERTED',closed:'CLOSED'}

function statusLabel(v){return statusLabels[v]||v}
function statusBadge(s){return{PENDING:'os-pending',REPLIED:'os-done',CONVERTED:'os-shipping',CLOSED:'os-cancelled'}[s]||''}
function tabCount(k){if(k==='all')return total.value;const s=stats.value||{};if(k==='pending')return s.pending||0;if(k==='replied')return s.replied||0;if(k==='converted')return s.converted||0;if(k==='closed')return (total.value-(s.pending||0)-(s.replied||0)-(s.converted||0));return 0}
const totalCount=computed(()=>{const s=stats.value||{};return(s.pending||0)+(s.replied||0)+(s.converted||0)})
const convRate=computed(()=>totalCount.value>0?Math.round((stats.value?.converted||0)/totalCount.value*100):0)
const sourceCount=computed(()=>new Set(list.value.map(r=>r.source)).size)

function sourceIcon(s){const m={官网:Connection,WhatsApp:ChatDotRound,邮件:Message,展会:OfficeBuilding};return m[s]||Connection}
function sourceColor(s){const m={官网:'#6c5ce7',WhatsApp:'#25d366',邮件:'#e67e22',展会:'#74b9ff'};return m[s]||'#909399'}

const filteredList=computed(()=>{
  let l=list.value
  if(sourceFilter.value)l=l.filter(r=>r.source===sourceFilter.value)
  if(searchKeyword.value){const kw=searchKeyword.value.toLowerCase();l=l.filter(r=>(r.customer||'').toLowerCase().includes(kw)||(r.inquiryNo||'').toLowerCase().includes(kw)||(r.interest||'').toLowerCase().includes(kw))}
  return l
})

onMounted(()=>{load();loadStats()})

function onSelectChange(rows){selectedIds.value=rows.map(r=>r.id)}
function onSearch(){clearTimeout(searchTimer);searchTimer=setTimeout(()=>{page.value=1;load()},300)}

async function load(){
  loading.value=true
  try{
    const res=await getInquiries({page:page.value,size:size.value})
    list.value=res.data?.list||[];total.value=res.data?.total||0
    if(statusFilter.value!=='all'){const m={pending:'PENDING',replied:'REPLIED',converted:'CONVERTED',closed:'CLOSED'};list.value=list.value.filter(r=>r.status===m[statusFilter.value]);total.value=list.value.length}
    selectedIds.value=[]
  }catch{}finally{loading.value=false}
}

async function loadStats(){try{const r=await getInquiryStats();if(r.data)stats.value=r.data}catch{}}

function openAdd(){editId.value=null;form.value={inquiryNo:'',customer:'',source:'官网',interest:'',summary:'',status:'PENDING'};dialogVisible.value=true}
function openEdit(row){editId.value=row.id;form.value={...row};dialogVisible.value=true}

async function save(){
  saving.value=true
  try{if(editId.value){await updateInquiry(editId.value,form.value);ElMessage.success('更新成功')}else{await createInquiry(form.value);ElMessage.success('创建成功')}
  dialogVisible.value=false;load();loadStats()}catch{}finally{saving.value=false}
}

async function replyInquiry(row){try{await updateInquiry(row.id,{...row,status:'REPLIED'});ElMessage.success('已标记为已回复');load();loadStats()}catch{}}
async function followUp(row){ElMessage.info(`跟进询盘: ${row.inquiryNo}`)}
async function batchReply(){try{await Promise.all(selectedIds.value.map(id=>{const r=list.value.find(x=>x.id===id);return r?updateInquiry(id,{...r,status:'REPLIED'}):null}));ElMessage.success(`已回复 ${selectedIds.value.length} 条`);load();loadStats()}catch{}}
async function batchClose(){try{await Promise.all(selectedIds.value.map(id=>{const r=list.value.find(x=>x.id===id);return r?updateInquiry(id,{...r,status:'CLOSED'}):null}));ElMessage.warning(`已关闭 ${selectedIds.value.length} 条`);load();loadStats()}catch{}}
function formatTime(t){if(!t)return'-';return t.substring(0,16).replace('T',' ')}
</script>

<style scoped>
.iq-page{max-width:100%}
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
.order-status-badge{display:inline-block;padding:2px 8px;border-radius:20px;font-size:11px;font-weight:500;white-space:nowrap}
.os-pending{background:rgba(253,203,110,.15);color:#e67e22}
.os-done{background:rgba(0,184,148,.1);color:#00b894}
.os-shipping{background:rgba(108,92,231,.1);color:#6c5ce7}
.os-cancelled{background:#f5f5f5;color:#b2b2b2}

.om-actions{display:flex;gap:4px;justify-content:center}
.om-action-btn{width:28px;height:28px;padding:0;border-radius:6px;background:transparent;border:1px solid #e0e3e8;color:#909399;cursor:pointer;transition:all .2s;display:flex;align-items:center;justify-content:center}
.om-action-btn:hover{border-color:#6c5ce7;color:#6c5ce7;background:rgba(108,92,231,.08)}
.om-pagination{display:flex;align-items:center;justify-content:space-between;padding:12px 0 0;border-top:1px solid #e0e3e8;margin-top:8px;font-size:12px;color:#909399}

@media(max-width:1200px){.om-kpi-strip{grid-template-columns:repeat(3,1fr)}}
@media(max-width:768px){.om-kpi-strip{grid-template-columns:repeat(2,1fr)}}
</style>
