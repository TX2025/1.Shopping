<template>
  <div class="um-page">
    <div class="page-header">
      <h2>用户管理</h2>
      <div class="page-header-actions">
        <button class="btn btn-outline" @click="ElMessage.info('导出用户')"><el-icon :size="14"><Download /></el-icon> 导出用户</button>
        <button class="btn btn-primary"><el-icon :size="14"><Plus /></el-icon> 添加用户</button>
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

    <!-- Toolbar Card -->
    <div class="card" style="margin-bottom:16px">
      <div class="card-body" style="padding:12px 16px">
        <!-- Time period row -->
        <div class="pm-time-row">
          <div class="pm-time-label"><el-icon :size="13"><Calendar /></el-icon> 注册时间</div>
          <div class="pm-time-tabs">
            <button v-for="p in periods" :key="p.key" class="pm-time-tab" :class="{active:currentPeriod===p.key}" @click="switchPeriod(p.key)">{{ p.label }}</button>
          </div>
          <div class="pm-time-summary">当前周期：<b style="color:#6c5ce7">{{ periodLabel }}</b></div>
        </div>

        <!-- Status tabs + search + filter -->
        <div class="pm-toolbar" style="margin-top:10px">
          <div class="om-tab-group">
            <button v-for="t in statusTabs" :key="t.key" class="pm-tab" :class="{active:statusFilter===t.key}" @click="statusFilter=t.key;page=1;load()">
              <i v-if="t.dot" class="tab-dot" :style="{background:t.dot}"></i>
              {{ t.label }} <span class="pm-tab-count">{{ tabCounts[t.key]||0 }}</span>
            </button>
          </div>
          <div class="pm-toolbar-right">
            <div class="pm-search-wrap"><el-icon :size="12"><Search /></el-icon><input v-model="searchKeyword" placeholder="姓名、手机、邮箱..." @input="onSearch" /></div>
            <select class="pm-select" v-model="levelFilter" @change="page=1;load()">
              <option value="">全部等级</option>
              <option value="普通">普通会员</option>
              <option value="银牌">银牌会员</option>
              <option value="金牌">金牌会员</option>
              <option value="钻石">钻石会员</option>
            </select>
            <select class="pm-select" v-model="sortKey" @change="page=1;load()">
              <option value="newest">最新注册</option>
              <option value="spend_desc">消费最高</option>
              <option value="orders_desc">订单最多</option>
            </select>
          </div>
        </div>

        <!-- Batch bar -->
        <transition name="fade"><div class="pm-batch-bar" v-if="selectedIds.length" style="margin-top:10px">
          <span>已选 <strong>{{ selectedIds.length }}</strong> 人</span>
          <div style="display:flex;gap:8px;margin-left:auto">
            <button class="btn btn-sm btn-outline" @click="ElMessage.info('发送消息')"><el-icon :size="12"><Message /></el-icon> 发送消息</button>
            <button class="btn btn-sm btn-outline" @click="ElMessage.info('发放优惠券')"><el-icon :size="12"><Present /></el-icon> 发放优惠券</button>
            <button class="btn btn-sm" style="background:rgba(225,112,85,0.1);color:#e17055;border:1px solid rgba(225,112,85,0.3)" @click="batchBan"><el-icon :size="12"><CircleClose /></el-icon> 批量禁用</button>
          </div>
        </div></transition>
      </div>
    </div>

    <!-- Table -->
    <div class="card">
      <div class="card-body" style="padding:0">
        <el-table :data="users" stripe v-loading="loading" row-key="id" size="small" @selection-change="onSelectChange">
          <el-table-column type="selection" width="40" />
          <el-table-column label="用户" min-width="160">
            <template #default="{row}">
              <div class="om-customer-cell">
                <div class="om-avatar" :style="{background:avatarColor(row.username)}">{{ avatarText(row.username) }}</div>
                <div>
                  <div class="om-cust-name">{{ row.username }}</div>
                  <div style="font-size:11px;color:#909399" v-if="row.level">{{ row.level }}会员</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="联系方式" min-width="180">
            <template #default="{row}">
              <div style="font-size:13px">{{ row.email || '-' }}</div>
              <div style="font-size:11px;color:#909399">{{ maskPhone(row.phone) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="等级" width="90" align="center">
            <template #default="{row}"><span class="um-level-badge" :class="levelClass(row.level)">{{ row.level || '普通' }}</span></template>
          </el-table-column>
          <el-table-column label="注册时间" width="110">
            <template #default="{row}"><span style="font-size:12px;color:#909399">{{ formatDate(row.createTime) }}</span></template>
          </el-table-column>
          <el-table-column label="订单数" width="80" align="center">
            <template #default="{row}"><span style="font-weight:600">0</span></template>
          </el-table-column>
          <el-table-column label="累计消费" width="100" align="right">
            <template #default="{row}"><span style="font-weight:500">¥0.00</span></template>
          </el-table-column>
          <el-table-column label="最近活跃" width="110">
            <template #default="{row}"><span style="font-size:12px;color:#909399">{{ formatDate(row.createTime) }}</span></template>
          </el-table-column>
          <el-table-column label="状态" width="85" align="center">
            <template #default="{row}">
              <span class="order-status-badge" :class="row.status==='ACTIVE'?'os-done':'os-cancelled'">{{ row.status==='ACTIVE'?'正常':'已禁用' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right" align="center">
            <template #default="{row}">
              <div class="om-actions">
                <button class="om-action-btn" title="查看详情"><el-icon :size="13"><View /></el-icon></button>
                <button class="om-action-btn" title="编辑"><el-icon :size="13"><EditPen /></el-icon></button>
                <button v-if="row.role!=='ADMIN'" class="om-action-btn danger" :title="row.status==='ACTIVE'?'禁用':'启用'" @click="toggleStatus(row)" :style="row.status==='ACTIVE'?{borderColor:'#e17055',color:'#e17055'}:{borderColor:'#67C23A',color:'#67C23A'}">
                  <el-icon :size="13"><CircleClose v-if="row.status==='ACTIVE'" /><CircleCheck v-else /></el-icon>
                </button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading&&users.length===0" description="暂无用户" :image-size="64" style="padding:40px 0" />
      </div>
      <div class="om-pagination" v-if="total>0">
        <span>共 {{ total }} 人，第 {{ page }}/{{ Math.max(1,Math.ceil(total/size)) }} 页</span>
        <el-pagination v-model:current-page="page" :page-size="size" :page-sizes="[10,20,50]" :total="total"
          layout="sizes,prev,pager,next" @size-change="s=>{size=s;page=1;load()}" @current-change="load" background small />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getAdminUsers, updateUserStatus, getUserStats } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, Search, View, EditPen, CircleClose, CircleCheck, Calendar, Message, Present, Money, User, TrendCharts, Star, Timer } from '@element-plus/icons-vue'

const users=ref([]);const total=ref(0);const page=ref(1);const size=ref(10);const loading=ref(false)
const statusFilter=ref('all');const searchKeyword=ref('');const sortKey=ref('newest');const levelFilter=ref('')
const selectedIds=ref([]);const stats=ref({total:0,active:0,disabled:0})
const tabCounts=ref({all:0,active:0,banned:0})
const currentPeriod=ref('month')
const periods=[{key:'today',label:'今日'},{key:'week',label:'本周'},{key:'month',label:'本月'},{key:'year',label:'全年'},{key:'all',label:'全部'}]
const periodLabels={today:'今日',week:'本周',month:'本月',year:'全年',all:'全部'}
const periodLabel=computed(()=>periodLabels[currentPeriod.value]||'本月')
let searchTimer=null

const statusTabs=[
  {key:'all',label:'全部'},{key:'active',label:'正常',dot:'#00b894'},{key:'banned',label:'已禁用',dot:'#e17055'},
  {key:'vip',label:'VIP会员',dot:'#fdcb6e'},{key:'new',label:'本期新增',dot:'#74b9ff'},
]

const kpis=computed(()=>{
  const s=stats.value
  return[
    {icon:User,bg:'rgba(108,92,231,0.1)',color:'#6c5ce7',label:'总用户数',val:(s.total||0).toLocaleString(),sub:`活跃 ${s.active||0} 人`},
    {icon:Star,bg:'rgba(0,184,148,0.1)',color:'#00b894',label:'正常用户',val:s.active||0,sub:'占比 '+Math.round((s.active/(s.total||1))*100)+'%'},
    {icon:TrendCharts,bg:'rgba(253,203,110,0.12)',color:'#e67e22',label:'VIP会员',val:0,sub:'待开发'},
    {icon:Timer,bg:'rgba(116,185,255,0.15)',color:'#74b9ff',label:'本月新增',val:0,sub:'待统计'},
    {icon:Money,bg:'rgba(225,112,85,0.1)',color:'#e17055',label:'已禁用',val:s.disabled||0,sub:'需要审核'},
  ]
})

onMounted(()=>{load();loadStats()})

function onSelectChange(rows){selectedIds.value=rows.map(r=>r.id)}
function onSearch(){clearTimeout(searchTimer);searchTimer=setTimeout(()=>{page.value=1;load()},300)}
function switchPeriod(k){currentPeriod.value=k}

async function load(){
  loading.value=true
  try{
    const params={page:page.value,size:size.value}
    const res=await getAdminUsers(params)
    let list=res.data?.list||[];total.value=res.data?.total||0
    if(statusFilter.value==='active')list=list.filter(u=>u.status==='ACTIVE')
    else if(statusFilter.value==='banned')list=list.filter(u=>u.status!=='ACTIVE')
    else if(statusFilter.value==='vip')list=list.filter(u=>u.level&&['金牌','钻石'].includes(u.level))
    else if(statusFilter.value==='new')list=list.filter(u=>{const t=new Date(u.createTime);const now=new Date();return t.getMonth()===now.getMonth()&&t.getFullYear()===now.getFullYear()})
    if(levelFilter.value)list=list.filter(u=>(u.level||'普通')===levelFilter.value)
    if(searchKeyword.value){const kw=searchKeyword.value.toLowerCase();list=list.filter(u=>(u.username||'').toLowerCase().includes(kw)||(u.email||'').toLowerCase().includes(kw)||(u.phone||'').includes(kw))}
    users.value=list;selectedIds.value=[]
  }catch{}finally{loading.value=false}
}

async function loadStats(){try{const res=await getUserStats();if(res.data)stats.value=res.data;tabCounts.value={all:res.data?.total||0,active:res.data?.active||0,banned:res.data?.disabled||0,vip:0,new:0}}catch{}}

async function toggleStatus(row){const ns=row.status==='ACTIVE'?'DISABLED':'ACTIVE';try{await updateUserStatus(row.id,ns);ElMessage.success(ns==='ACTIVE'?'已启用':'已禁用');load();loadStats()}catch{}}
async function batchBan(){try{await Promise.all(selectedIds.value.map(id=>updateUserStatus(id,'DISABLED')));ElMessage.success(`已禁用 ${selectedIds.value.length} 人`);load();loadStats()}catch{}}

const avatarColors=['#6c5ce7','#00b894','#e17055','#fdcb6e','#74b9ff','#e67e22','#a29bfe','#fd79a8']
function avatarColor(n){if(!n)return avatarColors[0];return avatarColors[n.charCodeAt(0)%avatarColors.length]}
function avatarText(n){return(n||'?').charAt(0)}
function maskPhone(p){if(!p)return'-';return p.replace(/(\d{3})\d{4}(\d{4})/,'$1****$2')}
function formatDate(t){if(!t)return'-';return t.substring(0,10)}
function levelClass(l){const m={普通:'lv-normal',银牌:'lv-silver',金牌:'lv-gold',钻石:'lv-diamond'};return m[l]||'lv-normal'}
</script>

<style scoped>
.um-page{max-width:100%}
.page-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:20px}
.page-header h2{font-size:20px;font-weight:600;margin:0}
.page-header-actions{display:flex;gap:8px}
.btn{padding:8px 16px;border:none;border-radius:8px;cursor:pointer;font-size:13px;font-weight:500;display:inline-flex;align-items:center;gap:6px;transition:all .2s;font-family:inherit}
.btn-primary{background:#6c5ce7;color:#fff}.btn-primary:hover{background:#5b4cdb}
.btn-outline{background:transparent;border:1px solid #e0e3e8;color:#606266}.btn-outline:hover{border-color:#6c5ce7;color:#6c5ce7}
.btn-sm{padding:5px 12px;font-size:12px}
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

/* Time row */
.pm-time-row{display:flex;align-items:center;gap:12px;flex-wrap:wrap;padding-bottom:10px;border-bottom:1px solid #e0e3e8;margin-bottom:2px}
.pm-time-label{font-size:12px;font-weight:600;color:#909399;white-space:nowrap;display:flex;align-items:center;gap:5px}
.pm-time-tabs{display:flex;gap:3px;background:#f5f7fa;border-radius:8px;padding:3px}
.pm-time-tab{padding:5px 14px;border-radius:6px;font-size:12px;font-weight:500;cursor:pointer;border:none;background:transparent;color:#909399;transition:all .2s;white-space:nowrap;font-family:inherit}
.pm-time-tab:hover{color:#606266;background:rgba(0,0,0,.04)}
.pm-time-tab.active{background:#fff;color:#6c5ce7;box-shadow:0 1px 4px rgba(0,0,0,.04);font-weight:600}
.pm-time-summary{margin-left:auto;font-size:11px;color:#909399;background:#f5f7fa;padding:4px 10px;border-radius:10px;white-space:nowrap}

/* Toolbar */
.pm-toolbar{display:flex;align-items:center;gap:12px;flex-wrap:wrap}
.om-tab-group{display:flex;gap:2px;border-bottom:2px solid #e0e3e8;flex:1}
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

/* Table */
.om-customer-cell{display:flex;align-items:center;gap:8px}
.om-avatar{width:28px;height:28px;border-radius:50%;color:#fff;display:flex;align-items:center;justify-content:center;font-size:12px;font-weight:600;flex-shrink:0}
.om-cust-name{font-size:13px;font-weight:600}

/* Level badge */
.um-level-badge{display:inline-block;padding:2px 8px;border-radius:10px;font-size:10px;font-weight:600}
.lv-normal{background:#f5f5f5;color:#909399}
.lv-silver{background:rgba(162,155,254,.12);color:#6c5ce7}
.lv-gold{background:rgba(253,203,110,.15);color:#d68910}
.lv-diamond{background:rgba(116,185,255,.15);color:#2980b9}

/* Order status badge reuse */
.order-status-badge{display:inline-block;padding:2px 8px;border-radius:20px;font-size:11px;font-weight:500;white-space:nowrap}
.os-done{background:rgba(0,184,148,.1);color:#00b894}
.os-cancelled{background:#f5f5f5;color:#b2b2b2}

/* Actions */
.om-actions{display:flex;gap:4px;justify-content:center}
.om-action-btn{width:28px;height:28px;padding:0;border-radius:6px;background:transparent;border:1px solid #e0e3e8;color:#909399;cursor:pointer;transition:all .2s;display:flex;align-items:center;justify-content:center}
.om-action-btn:hover{border-color:#6c5ce7;color:#6c5ce7;background:rgba(108,92,231,.08)}
.om-action-btn.danger:hover{border-color:#e17055;color:#e17055;background:rgba(225,112,85,.08)}

/* Pagination */
.om-pagination{display:flex;align-items:center;justify-content:space-between;padding:12px 16px;border-top:1px solid #e0e3e8;font-size:12px;color:#909399}

@media(max-width:1200px){.om-kpi-strip{grid-template-columns:repeat(3,1fr)}}
@media(max-width:768px){.om-kpi-strip{grid-template-columns:repeat(2,1fr)}.pm-toolbar{flex-direction:column;align-items:stretch}}
</style>
