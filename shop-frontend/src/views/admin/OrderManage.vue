<template>
  <div class="om-page">
    <div class="page-header">
      <h2>订单管理</h2>
      <div class="page-header-actions">
        <button class="btn btn-outline" @click="ElMessage.info('导出订单')"><el-icon :size="14"><Download /></el-icon> 导出订单</button>
        <button class="btn btn-primary"><el-icon :size="14"><Plus /></el-icon> 创建订单</button>
      </div>
    </div>

    <!-- KPI Strip -->
    <div class="om-kpi-strip">
      <div v-for="k in kpis" :key="k.label" class="om-kpi-card">
        <div class="om-kpi-icon" :style="{background:k.bg,color:k.color}"><el-icon :size="17"><component :is="k.icon" /></el-icon></div>
        <div class="om-kpi-body">
          <div class="om-kpi-label">{{ k.label }}</div>
          <div class="om-kpi-val" :style="{color:k.color}">{{ k.val }}</div>
          <div class="om-kpi-sub" v-html="k.sub"></div>
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
          <div class="pm-time-summary" v-else>当前统计周期：<b style="color:#6c5ce7">{{ periodLabel }}</b></div>
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
            <div class="pm-search-wrap"><el-icon :size="12"><Search /></el-icon><input v-model="searchKeyword" placeholder="订单号、客户名、手机号..." @input="onSearch" /></div>
            <select class="pm-select" v-model="channelFilter" @change="page=1;load()">
              <option value="">全部渠道</option>
              <option v-for="ch in channels" :key="ch" :value="ch">{{ ch }}</option>
            </select>
            <select class="pm-select" v-model="sortKey" @change="page=1;load()">
              <option value="newest">最新下单</option>
              <option value="amount_desc">金额最高</option>
              <option value="amount_asc">金额最低</option>
            </select>
          </div>
        </div>

        <!-- Batch bar -->
        <transition name="fade"><div class="pm-batch-bar" v-if="selectedIds.length" style="margin-top:10px">
          <span>已选 <strong>{{ selectedIds.length }}</strong> 单</span>
          <div style="display:flex;gap:8px;margin-left:auto">
            <button class="btn btn-sm btn-outline" @click="batchStatus('SHIPPED')"><el-icon :size="12"><Van /></el-icon> 批量发货</button>
            <button class="btn btn-sm btn-outline" @click="ElMessage.info('导出选中')"><el-icon :size="12"><Download /></el-icon> 导出选中</button>
            <button class="btn btn-sm" style="background:rgba(225,112,85,0.1);color:#e17055;border:1px solid rgba(225,112,85,0.3)" @click="batchStatus('CANCELLED')"><el-icon :size="12"><Close /></el-icon> 批量取消</button>
          </div>
        </div></transition>
      </div>
    </div>

    <!-- Table -->
    <div class="card">
      <div class="card-body" style="padding:0">
        <el-table :data="orders" stripe v-loading="loading" row-key="id" size="small" @selection-change="onSelectChange">
          <el-table-column type="selection" width="40" />
          <el-table-column label="订单号" min-width="150">
            <template #default="{row}"><span class="om-order-id" @click="openDetail(row)">{{ row.order?.orderNo }}</span></template>
          </el-table-column>
          <el-table-column label="客户" width="130">
            <template #default="{row}">
              <div class="om-customer-cell">
                <div class="om-avatar" :style="{background:avatarColor(row.order?.receiverName)}">{{ avatarText(row.order?.receiverName) }}</div>
                <div>
                  <div class="om-cust-name">{{ row.order?.receiverName || '-' }}</div>
                  <div class="om-cust-phone">{{ row.order?.receiverPhone || '-' }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="产品摘要" min-width="150">
            <template #default="{row}">
              <div class="om-product-summary" v-if="row.items?.length">{{ row.items.length }}件: {{ row.items.map(i=>i.productName).join(' + ') }}</div>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="110">
            <template #default="{row}"><span class="om-amount">¥{{ Number(row.order?.totalAmount||0).toLocaleString() }}</span></template>
          </el-table-column>
          <el-table-column label="渠道" width="90" align="center">
            <template #default="{row}"><span class="om-channel-tag" :class="channelClass(row.order?.channel)">{{ channelLabel(row.order?.channel) }}</span></template>
          </el-table-column>
          <el-table-column label="支付" width="70" align="center">
            <template #default="{row}"><span :style="{color:payColor(row.order?.paymentMethod),fontWeight:600,fontSize:'12px'}">{{ payLabel(row.order?.paymentMethod) }}</span></template>
          </el-table-column>
          <el-table-column label="状态" width="85">
            <template #default="{row}"><span class="order-status-badge" :class="'os-'+statusMap[row.order?.status]">{{ statusLabel(row.order?.status) }}</span></template>
          </el-table-column>
          <el-table-column label="下单时间" width="140">
            <template #default="{row}"><span style="font-size:12px;color:#909399">{{ formatTime(row.order?.createTime) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right" align="center">
            <template #default="{row}">
              <div class="om-actions">
                <button class="om-action-btn" title="查看详情" @click="openDetail(row)"><el-icon :size="13"><View /></el-icon></button>
                <button v-if="row.order?.status==='PENDING'" class="om-action-btn" title="标记支付" @click="changeStatus(row,'PAID')" style="color:#67C23A;border-color:#67C23A"><el-icon :size="13"><Check /></el-icon></button>
                <button v-if="row.order?.status==='PAID'" class="om-action-btn" title="发货" @click="changeStatus(row,'SHIPPED')" style="color:#409EFF;border-color:#409EFF"><el-icon :size="13"><Van /></el-icon></button>
                <button v-if="row.order?.status==='PENDING'||row.order?.status==='PAID'" class="om-action-btn danger" title="取消" @click="changeStatus(row,'CANCELLED')"><el-icon :size="13"><Close /></el-icon></button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading&&orders.length===0" description="暂无符合条件的订单" :image-size="64" style="padding:40px 0" />
      </div>
      <div class="om-pagination" v-if="total>0">
        <span>共 {{ total }} 条，第 {{ page }}/{{ Math.max(1,Math.ceil(total/size)) }} 页</span>
        <el-pagination v-model:current-page="page" :page-size="size" :page-sizes="[10,20,50]" :total="total"
          layout="sizes,prev,pager,next" @size-change="s=>{size=s;page=1;load()}" @current-change="load" background small />
      </div>
    </div>

    <!-- Detail Drawer -->
    <el-drawer v-model="drawerVisible" title="订单详情" direction="rtl" size="460px">
      <template v-if="drawerOrder">
        <div class="drawer-section">
          <div class="drawer-section-title">订单信息</div>
          <div class="drawer-row"><span class="drawer-label">订单号</span><span class="drawer-val om-order-id">{{ drawerOrder.order?.orderNo }}</span></div>
          <div class="drawer-row"><span class="drawer-label">状态</span><span class="drawer-val"><span class="order-status-badge" :class="'os-'+statusMap[drawerOrder.order?.status]">{{ statusLabel(drawerOrder.order?.status) }}</span></span></div>
          <div class="drawer-row"><span class="drawer-label">金额</span><span class="drawer-val om-amount">¥{{ Number(drawerOrder.order?.totalAmount||0).toLocaleString() }}</span></div>
          <div class="drawer-row"><span class="drawer-label">渠道</span><span class="drawer-val">{{ channelLabel(drawerOrder.order?.channel) }}</span></div>
          <div class="drawer-row"><span class="drawer-label">支付方式</span><span class="drawer-val">{{ payLabel(drawerOrder.order?.paymentMethod) }}</span></div>
          <div class="drawer-row"><span class="drawer-label">下单时间</span><span class="drawer-val">{{ formatTime(drawerOrder.order?.createTime) }}</span></div>
          <div class="drawer-row" v-if="drawerOrder.order?.payTime"><span class="drawer-label">支付时间</span><span class="drawer-val">{{ formatTime(drawerOrder.order?.payTime) }}</span></div>
          <div class="drawer-row" v-if="drawerOrder.order?.remark"><span class="drawer-label">备注</span><span class="drawer-val">{{ drawerOrder.order?.remark }}</span></div>
        </div>
        <div class="drawer-section">
          <div class="drawer-section-title">收货信息</div>
          <div class="drawer-row"><span class="drawer-label">收货人</span><span class="drawer-val">{{ drawerOrder.order?.receiverName || '-' }}</span></div>
          <div class="drawer-row"><span class="drawer-label">电话</span><span class="drawer-val">{{ drawerOrder.order?.receiverPhone || '-' }}</span></div>
          <div class="drawer-row"><span class="drawer-label">地址</span><span class="drawer-val">{{ drawerOrder.order?.receiverAddress || '-' }}</span></div>
        </div>
        <div class="drawer-section" v-if="drawerOrder.items?.length">
          <div class="drawer-section-title">产品明细</div>
          <div v-for="item in drawerOrder.items" :key="item.id" class="drawer-row">
            <span class="drawer-label">{{ item.productName }} x{{ item.quantity }}</span>
            <span class="drawer-val">¥{{ Number(item.price||0).toFixed(2) }}</span>
          </div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getAdminOrders, updateOrderStatus, getOrderStats } from '../../api/admin'
import { ElMessage } from 'element-plus'
import { Plus, Download, Search, View, Check, Close, Van, Money, ShoppingBag, TrendCharts, Timer, Warning, Calendar } from '@element-plus/icons-vue'

const orders=ref([]);const total=ref(0);const page=ref(1);const size=ref(10);const loading=ref(false)
const statusFilter=ref('all');const searchKeyword=ref('');const channelFilter=ref('');const sortKey=ref('newest')
const selectedIds=ref([]);const stats=ref({});const tabCounts=ref({all:0,pending:0,paid:0,shipping:0,cancelled:0})
let searchTimer=null
const drawerVisible=ref(false);const drawerOrder=ref(null)
const channels=['微信小程序','APP','PC官网','H5']
const currentPeriod=ref('month');const customFrom=ref('');const customTo=ref('')
const periods=[{key:'today',label:'今日'},{key:'week',label:'本周'},{key:'month',label:'本月'},{key:'year',label:'全年'},{key:'custom',label:'自定义'}]
const periodLabels={today:'今日',week:'本周',month:'本月',year:'全年'}
const periodLabel=computed(()=>periodLabels[currentPeriod.value]||'自定义')

function switchPeriod(key){
  currentPeriod.value=key
  if(key!=='custom'){customFrom.value='';customTo.value=''}
}
function applyCustomRange(){
  if(!customFrom.value||!customTo.value){ElMessage.warning('请选择开始和结束日期');return}
  if(customFrom.value>customTo.value){ElMessage.warning('开始日期不能晚于结束日期');return}
  ElMessage.success(`已筛选 ${customFrom.value} 至 ${customTo.value}`)
  page.value=1;load()
}

const statusTabs=[
  {key:'all',label:'全部'},{key:'pending',label:'待处理',dot:'#fdcb6e'},
  {key:'paid',label:'已支付',dot:'#74b9ff'},{key:'shipping',label:'配送中',dot:'#6c5ce7'},
  {key:'cancelled',label:'已取消'},
]
const statusMap={PENDING:'pending',PAID:'paid',SHIPPED:'shipping',CANCELLED:'cancelled'}
const statusLabels={PENDING:'待付款',PAID:'已付款',SHIPPED:'配送中',CANCELLED:'已取消'}

// KPI
const kpis=computed(()=>{
  const s=stats.value;const total=s.PENDING+s.PAID+s.SHIPPED+s.CANCELLED;const avg=total>0?Math.round((Number(s.totalRevenue||0))/total):0
  return[
    {icon:ShoppingBag,bg:'rgba(108,92,231,0.1)',color:'#6c5ce7',label:'总订单数',val:(total||0).toLocaleString(),sub:'<span style="color:#00b894">↑ 12.3%</span> vs 上月'},
    {icon:Money,bg:'rgba(0,184,148,0.1)',color:'#00b894',label:'总销售额',val:'¥'+(Number(s.totalRevenue||0)).toLocaleString(),sub:'<span style="color:#00b894">↑ 8.7%</span> vs 上月'},
    {icon:TrendCharts,bg:'rgba(116,185,255,0.1)',color:'#74b9ff',label:'客单价',val:'¥'+avg.toLocaleString(),sub:'<span style="color:#00b894">↑ 3.2%</span> vs 上月'},
    {icon:Timer,bg:'rgba(253,203,110,0.12)',color:'#e67e22',label:'待处理订单',val:s.PENDING||0,sub:'<span style="color:#e67e22">需及时处理</span>'},
    {icon:Warning,bg:'rgba(225,112,85,0.1)',color:'#e17055',label:'退款率',val:'8.5%',sub:'<span style="color:#e17055">↑ 0.4%</span> vs 上月'},
  ]
})

onMounted(()=>{load();loadStats()})

function onSelectChange(rows){selectedIds.value=rows.map(r=>r.order?.id).filter(Boolean)}
function onSearch(){clearTimeout(searchTimer);searchTimer=setTimeout(()=>{page.value=1;load()},300)}

async function load(){
  loading.value=true
  try{
    const params={page:page.value,size:size.value}
    if(statusFilter.value!=='all'){const m={pending:'PENDING',paid:'PAID',shipping:'SHIPPED',cancelled:'CANCELLED'};params.status=m[statusFilter.value]||statusFilter.value}
    const res=await getAdminOrders(params)
    orders.value=res.data?.list||[];total.value=res.data?.total||0;selectedIds.value=[]
  }catch{}finally{loading.value=false}
}

async function loadStats(){
  try{const res=await getOrderStats();if(res.data){stats.value=res.data;const s=res.data
  tabCounts.value={all:s.PENDING+s.PAID+s.SHIPPED+s.CANCELLED,pending:s.PENDING||0,paid:s.PAID||0,shipping:s.SHIPPED||0,cancelled:s.CANCELLED||0}}}
  catch{}
}

async function changeStatus(row,st){try{await updateOrderStatus(row.order.id,st);ElMessage.success('状态已更新');load();loadStats()}catch{}}
async function batchStatus(st){try{await Promise.all(selectedIds.value.map(id=>updateOrderStatus(id,st)));ElMessage.success(`已批量操作 ${selectedIds.value.length} 单`);load();loadStats()}catch{}}

function statusLabel(s){return statusLabels[s]||s}

// Avatar
const avatarColors=['#6c5ce7','#00b894','#e17055','#fdcb6e','#74b9ff','#e67e22','#a29bfe','#fd79a8']
function avatarColor(n){if(!n)return avatarColors[0];return avatarColors[n.charCodeAt(0)%avatarColors.length]}
function avatarText(n){return (n||'?').charAt(0)}

// Channel
function channelLabel(c){const m={'微信小程序':'微信小程序',APP:'APP','PC官网':'PC官网',H5:'H5'};return m[c]||c||'-'}
function channelClass(c){const m={'微信小程序':'ch-wechat',APP:'ch-app','PC官网':'ch-web',H5:'ch-h5'};return m[c]||''}

// Payment
function payLabel(p){const m={alipay:'支付宝',wechat:'微信',card:'银行卡'};return m[p]||p||'-'}
function payColor(p){const m={alipay:'#1677ff',wechat:'#07c160',card:'#e17055'};return m[p]||'#636e72'}

function formatTime(t){if(!t)return'-';return t.substring(0,16).replace('T',' ')}

function openDetail(row){drawerOrder.value=row;drawerVisible.value=true}
</script>

<style scoped>
.om-page{max-width:100%}
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
.om-kpi-sub :deep(span){white-space:nowrap}

/* Time period row */
.pm-time-row{display:flex;align-items:center;gap:12px;flex-wrap:wrap;padding-bottom:10px;border-bottom:1px solid #e0e3e8;margin-bottom:2px}
.pm-time-label{font-size:12px;font-weight:600;color:#909399;white-space:nowrap;display:flex;align-items:center;gap:5px}
.pm-time-tabs{display:flex;gap:3px;background:#f5f7fa;border-radius:8px;padding:3px}
.pm-time-tab{padding:5px 14px;border-radius:6px;font-size:12px;font-weight:500;cursor:pointer;border:none;background:transparent;color:#909399;transition:all .2s;white-space:nowrap;font-family:inherit}
.pm-time-tab:hover{color:#606266;background:rgba(0,0,0,.04)}
.pm-time-tab.active{background:#fff;color:#6c5ce7;box-shadow:0 1px 4px rgba(0,0,0,.04);font-weight:600}
.pm-custom-range{display:flex;align-items:center;gap:6px}
.pm-date-input{padding:5px 8px;border:1px solid #e0e3e8;border-radius:8px;font-size:12px;outline:none;background:#fff;color:#606266;transition:all .2s;font-family:inherit}
.pm-date-input:focus{border-color:#6c5ce7}
.pm-time-summary{margin-left:auto;font-size:11px;color:#909399;background:#f5f7fa;padding:4px 10px;border-radius:10px;white-space:nowrap}

/* Toolbar */
.pm-toolbar{display:flex;align-items:center;gap:12px;flex-wrap:wrap}
.om-tab-group{display:flex;gap:2px;border-bottom:2px solid #e0e3e8;flex:1;overflow-x:auto}
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
.om-order-id{color:#6c5ce7;font-weight:500;cursor:pointer;transition:all .15s}
.om-order-id:hover{text-decoration:underline}
.om-customer-cell{display:flex;align-items:center;gap:8px}
.om-avatar{width:28px;height:28px;border-radius:50%;color:#fff;display:flex;align-items:center;justify-content:center;font-size:12px;font-weight:600;flex-shrink:0}
.om-cust-name{font-size:13px;font-weight:600}
.om-cust-phone{font-size:11px;color:#909399}
.om-product-summary{font-size:12px;color:#606266;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;max-width:160px}
.om-amount{font-weight:700;color:#6c5ce7}
.om-channel-tag{display:inline-block;padding:2px 6px;border-radius:4px;font-size:10px;font-weight:600}
.om-channel-tag.ch-wechat{background:rgba(7,193,96,.1);color:#07c160}
.om-channel-tag.ch-app{background:rgba(108,92,231,.1);color:#6c5ce7}
.om-channel-tag.ch-web{background:rgba(116,185,255,.12);color:#2980b9}
.om-channel-tag.ch-h5{background:rgba(253,203,110,.12);color:#d68910}

/* Order status badges (matching prototype color scheme) */
.order-status-badge{display:inline-block;padding:2px 8px;border-radius:20px;font-size:11px;font-weight:500;white-space:nowrap}
.os-pending{background:rgba(253,203,110,.15);color:#e67e22}
.os-paid{background:rgba(116,185,255,.15);color:#2980b9}
.os-shipping{background:rgba(108,92,231,.1);color:#6c5ce7}
.os-done{background:rgba(0,184,148,.1);color:#00b894}
.os-refund{background:rgba(225,112,85,.12);color:#e17055}
.os-cancelled{background:#f5f5f5;color:#b2b2b2}

/* Actions */
.om-actions{display:flex;gap:4px;justify-content:center}
.om-action-btn{width:28px;height:28px;padding:0;border-radius:6px;background:transparent;border:1px solid #e0e3e8;color:#909399;cursor:pointer;transition:all .2s;display:flex;align-items:center;justify-content:center}
.om-action-btn:hover{border-color:#6c5ce7;color:#6c5ce7;background:rgba(108,92,231,.08)}
.om-action-btn.danger:hover{border-color:#e17055;color:#e17055;background:rgba(225,112,85,.08)}

/* Pagination */
.om-pagination{display:flex;align-items:center;justify-content:space-between;padding:12px 16px;border-top:1px solid #e0e3e8;font-size:12px;color:#909399}

/* Drawer */
.drawer-section{margin-bottom:20px}
.drawer-section-title{font-size:12px;font-weight:600;color:#909399;text-transform:uppercase;letter-spacing:.5px;margin-bottom:10px;padding-bottom:6px;border-bottom:1px solid #e0e3e8}
.drawer-row{display:flex;justify-content:space-between;align-items:center;padding:7px 0;border-bottom:1px solid #f5f7fa;font-size:13px}
.drawer-row:last-child{border-bottom:none}
.drawer-label{color:#909399}
.drawer-val{font-weight:500;text-align:right}

@media(max-width:1200px){.om-kpi-strip{grid-template-columns:repeat(3,1fr)}}
@media(max-width:768px){.om-kpi-strip{grid-template-columns:repeat(2,1fr)}.pm-toolbar{flex-direction:column;align-items:stretch}}
</style>
