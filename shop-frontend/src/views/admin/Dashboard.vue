<template>
  <div class="dash-page">
    <!-- Header -->
    <div class="page-header">
      <h2>仪表盘</h2>
      <div class="page-header-actions">
        <div class="dash-date-tabs">
          <button class="dash-date-tab" :class="{active:dateTab==='today'}" @click="dateTab='today'">今日</button>
          <button class="dash-date-tab" :class="{active:dateTab==='month'}" @click="dateTab='month'">本月</button>
          <button class="dash-date-tab" :class="{active:dateTab==='quarter'}" @click="dateTab='quarter'">季度</button>
          <button class="dash-date-tab" :class="{active:dateTab==='year'}" @click="dateTab='year'">全年</button>
        </div>
        <button class="btn btn-outline"><el-icon :size="14"><Download /></el-icon> 导出报表</button>
      </div>
    </div>

    <!-- Alerts -->
    <div class="dash-alerts">
      <div class="dash-alert warning"><el-icon :size="14"><Warning /></el-icon><span>库存预警：有 <b>{{ lowStockCount }}</b> 件产品库存低于安全线，请及时补货。</span><button class="alert-close" @click="showWarn=false" v-if="showWarn"><el-icon :size="12"><Close /></el-icon></button></div>
      <div class="dash-alert info" v-if="activePromos>0"><el-icon :size="14"><Present /></el-icon><span>当前有 <b>{{ activePromos }}</b> 个促销活动进行中。</span></div>
    </div>

    <!-- 8 KPI Stats -->
    <div class="stat-grid">
      <div v-for="k in kpiCards" :key="k.label" class="stat-card">
        <div class="stat-card-header"><div class="stat-card-icon" :style="{background:k.bg,color:k.color}"><el-icon :size="18"><component :is="k.icon" /></el-icon></div><div class="stat-card-trend" :class="k.trendDir"><el-icon :size="10"><component :is="k.trendIcon" /></el-icon> {{ k.trendVal }}</div></div>
        <div class="stat-card-value">{{ k.val }}</div><div class="stat-card-label">{{ k.label }}</div><div class="stat-card-sub">{{ k.sub }}</div>
      </div>
    </div>

    <!-- Row 1: Sales Trend + Donut + Week Compare -->
    <div class="dash-grid-wide">
      <div class="card">
        <div class="card-header"><h3><el-icon :size="16" color="#6c5ce7" style="margin-right:6px"><TrendCharts /></el-icon>销售趋势</h3><div style="display:flex;gap:14px;font-size:11px"><span style="display:flex;align-items:center;gap:4px"><span class="legend-line" style="background:#6c5ce7"></span>销售额</span><span style="display:flex;align-items:center;gap:4px"><span class="legend-line" style="background:#00b894"></span>订单量</span><span style="display:flex;align-items:center;gap:4px"><span class="legend-line dashed" style="border-color:#fd79a8"></span>毛利</span></div></div>
        <div class="card-body" style="padding:8px 16px 0"><v-chart :option="barOption" style="height:200px" autoresize /></div>
        <div class="profit-strip"><div v-for="ps in profitStrip" :key="ps.label" class="profit-item"><div class="profit-val" :style="{color:ps.color}">{{ ps.val }}</div><div class="profit-label">{{ ps.label }}</div><div class="profit-sub">{{ ps.sub }}</div></div></div>
      </div>
      <div style="display:flex;flex-direction:column;gap:20px">
        <div class="card">
          <div class="card-header"><h3><el-icon :size="16" color="#00b894" style="margin-right:6px"><PieChartIcn /></el-icon>分类销售占比</h3><button class="btn btn-sm btn-outline" @click="$router.push('/admin/categories')">查看全部</button></div>
          <div class="card-body" style="padding:12px 16px;display:flex;align-items:center;gap:14px">
            <v-chart :option="pieOption" style="width:120px;height:120px;flex-shrink:0" autoresize />
            <div style="flex:1;font-size:12px"><div v-for="(d,i) in donutData" :key="i" class="donut-legend-row"><span class="donut-dot" :style="{background:d.color}"></span>{{ d.name }} <span style="margin-left:auto;font-weight:600">{{ d.pct }}%</span></div></div>
          </div>
        </div>
        <div class="card" style="flex:1">
          <div class="card-header"><h3><el-icon :size="16" color="#e67e22" style="margin-right:6px"><TrendCharts /></el-icon>本周 vs 上周</h3></div>
          <div class="card-body" style="padding:8px 12px 4px"><v-chart :option="weekOption" style="height:130px" autoresize /></div>
          <div class="week-note"><span><span class="legend-line" style="background:#a29bfe;opacity:.6"></span> 上周</span><span><span class="legend-line" style="background:#6c5ce7"></span> 本周</span><span :style="{color:Number(weekDiff.diff)>=0?'#00b894':'#e17055',fontWeight:600}">{{ Number(weekDiff.diff)>=0?'↑':'↓' }} {{ Math.abs(weekDiff.diff) }}%</span></div>
        </div>
      </div>
    </div>

    <!-- Row 2: Top5 + Category Sparklines + Inventory + Activity -->
    <div class="dash-row4">
      <!-- Top 5 Products -->
      <div class="card">
        <div class="card-header"><h3><el-icon :size="16" color="#fdcb6e" style="margin-right:6px"><TrophyBase /></el-icon>Top 5 产品</h3><button class="btn btn-sm btn-outline" @click="$router.push('/admin/products')">全部</button></div>
        <div class="card-body" style="padding:6px 14px">
          <div v-for="(p,i) in topProducts" :key="p.id" class="top-product-row">
            <span class="top-rank" :class="i===0?'rank-1':i===1?'rank-2':i===2?'rank-3':''">{{ i+1 }}</span>
            <div class="top-icon"><el-icon :size="16" color="#a29bfe"><Box /></el-icon></div>
            <div style="flex:1;overflow:hidden">
              <div class="top-name">{{ p.name }}</div>
              <div class="top-sales">销量 {{ (p.sales||0).toLocaleString() }} · 毛利 <span :style="{color:marginColor(p),fontWeight:600}">{{ marginPct(p) }}%</span></div>
            </div>
            <div class="top-rev">¥{{ ((p.sales||0)*Number(p.price||0)).toLocaleString() }}</div>
          </div>
        </div>
      </div>

      <!-- Category Sparklines (NEW) -->
      <div class="card">
        <div class="card-header"><h3><el-icon :size="16" color="#74b9ff" style="margin-right:6px"><DataAnalysis /></el-icon>分类概览</h3></div>
        <div class="card-body" style="padding:6px 14px">
          <div v-for="d in catSparkData" :key="d.name" class="spark-row">
            <div class="spark-info"><span class="spark-dot" :style="{background:d.color}"></span><span class="spark-label">{{ d.name }}</span></div>
            <div style="display:flex;align-items:center;gap:6px">
              <div class="spark-bar-wrap"><div class="spark-bar" :style="{width:(d.pct/38*100)+'%',background:d.color}"></div></div>
              <span class="spark-val">{{ d.val }}</span>
              <span class="spark-trend" :class="d.trend==='up'?'up':d.trend==='down'?'down':''">{{ d.trendVal }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Inventory Health -->
      <div class="card">
        <div class="card-header"><h3><el-icon :size="16" color="#e17055" style="margin-right:6px"><Box /></el-icon>库存健康</h3></div>
        <div class="card-body" style="padding:10px 14px">
          <div class="inv-summary">
            <div class="inv-badge" style="background:rgba(0,184,148,.1)"><div class="inv-val" style="color:#00b894">{{ healthy }}</div><div class="inv-label" style="color:#00b894">库存充足</div></div>
            <div class="inv-badge" style="background:rgba(253,203,110,.15)"><div class="inv-val" style="color:#d68910">{{ lowStock }}</div><div class="inv-label" style="color:#d68910">库存偏低</div></div>
            <div class="inv-badge" style="background:rgba(225,112,85,.1)"><div class="inv-val" style="color:#e17055">{{ outStock }}</div><div class="inv-label" style="color:#e17055">已售罄</div></div>
            <div class="inv-badge" style="background:#f5f7fa"><div class="inv-val" style="color:#606266">{{ totalProducts }}</div><div class="inv-label" style="color:#909399">总产品</div></div>
          </div>
          <div v-if="lowStockItems.length">
            <div class="inv-section-title">需关注产品</div>
            <div v-for="it in lowStockItems" :key="it.id" class="inv-item"><span class="inv-dot" :style="{background:it.stock===0?'#e17055':'#fdcb6e'}"></span><span style="flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ it.name }}</span><span :style="{fontWeight:700,color:it.stock===0?'#e17055':'#d68910'}">{{ it.stock===0?'售罄':it.stock+'件' }}</span></div>
          </div>
        </div>
      </div>

      <!-- Activity Feed -->
      <div class="card">
        <div class="card-header"><h3><el-icon :size="16" color="#74b9ff" style="margin-right:6px"><Bell /></el-icon>动态消息</h3></div>
        <div class="card-body" style="padding:4px 14px">
          <div v-for="(a,i) in activities" :key="i" class="activity-row">
            <div class="act-icon" :style="{background:a.bg,color:a.fg}"><el-icon :size="12"><component :is="a.icon" /></el-icon></div>
            <div class="act-body"><div class="act-title" v-html="a.title"></div><div class="act-time"><el-icon :size="10"><Timer /></el-icon> {{ a.time }}</div></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Row 3: Recent Orders (wide) + 24h Hourly + Geo Sales Top 6 -->
    <div class="dash-grid-wide">
      <div class="card">
        <div class="card-header"><h3><el-icon :size="16" color="#6c5ce7" style="margin-right:6px"><Document /></el-icon>最近订单</h3><div style="display:flex;gap:6px"><button v-for="f in orderFilters" :key="f.key" class="order-filter-btn" :class="{active:f.key===orderFilter}" @click="orderFilter=f.key">{{ f.label }}</button><button class="btn btn-sm btn-outline" @click="$router.push('/admin/orders')">查看全部</button></div></div>
        <div class="card-body" style="padding:0">
          <el-table :data="recentOrders" size="small" stripe>
            <el-table-column label="订单号" width="130"><template #default="{r}"><span class="order-id">{{ r.orderNo }}</span></template></el-table-column>
            <el-table-column label="客户" width="70"><template #default="{r}">{{ r.receiverName||'-' }}</template></el-table-column>
            <el-table-column label="产品" min-width="120" show-overflow-tooltip><template #default="{r}">{{ r.itemsSummary||'-' }}</template></el-table-column>
            <el-table-column label="金额" width="85"><template #default="{r}"><span style="font-weight:600">¥{{ Number(r.totalAmount||0).toFixed(2) }}</span></template></el-table-column>
            <el-table-column label="渠道" width="80"><template #default="{r}"><span style="font-size:11px;color:#909399">{{ r.channel||'-' }}</span></template></el-table-column>
            <el-table-column label="状态" width="75"><template #default="{r}"><span class="status-badge" :class="orderStatusClass(r.status)">{{ orderStatusLabel(r.status) }}</span></template></el-table-column>
            <el-table-column label="日期" width="100"><template #default="{r}"><span style="color:#909399;font-size:12px">{{ r.dateLabel||(r.createTime?.substring(0,10)) }}</span></template></el-table-column>
          </el-table>
        </div>
        <div class="order-footer"><span>显示 <b>{{ recentOrders.length }}</b> / 共 <b>{{ totalOrderCount }}</b> 条最近订单</span><a href="#" @click.prevent="$router.push('/admin/orders')" style="color:#6c5ce7;text-decoration:none;font-weight:500">查看全部订单 →</a></div>
      </div>
      <div style="display:flex;flex-direction:column;gap:20px">
        <!-- 24h Hourly Distribution -->
        <div class="card">
          <div class="card-header"><h3><el-icon :size="16" color="#00cec9" style="margin-right:6px"><Timer /></el-icon>今日24小时分布</h3></div>
          <div class="card-body" style="padding:8px 12px 4px"><v-chart :option="hourlyOption" style="height:150px" autoresize /></div>
          <div class="hourly-peak">峰值时段 <b style="color:#6c5ce7">{{ peakHour }}:00 — {{ peakHour+1 }}:00</b>，下单 <b>{{ peakVal }}</b> 笔</div>
        </div>
        <!-- Geo Sales Top 6 -->
        <div class="card" style="flex:1">
          <div class="card-header"><h3><el-icon :size="16" color="#fd79a8" style="margin-right:6px"><Location /></el-icon>城市销售 Top 6</h3></div>
          <div class="card-body" style="padding:8px 14px">
            <div v-for="(c,i) in geoCities" :key="i" class="geo-row">
              <div class="geo-rank" :style="{background:rankBg(i),color:rankFg(i)}">{{ i+1 }}</div>
              <div style="width:36px;font-size:12px;font-weight:500;flex-shrink:0">{{ c.name }}</div>
              <div class="geo-bar-wrap"><div class="geo-bar-fill" :style="{width:Math.round(c.pct/100*100)+'%',background:c.color}"></div></div>
              <span class="geo-val">{{ c.val }}</span>
              <span class="geo-trend" :style="{background:c.up?'rgba(0,184,148,.1)':'rgba(225,112,85,.1)',color:c.up?'#00b894':'#e17055'}">{{ c.trend }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Row 4: Conversion Funnel | User Analytics | KPI Gauges | Satisfaction -->
    <div class="dash-row4" style="margin-bottom:20px">
      <!-- Conversion Funnel -->
      <div class="card">
        <div class="card-header"><h3><el-icon :size="16" color="#6c5ce7" style="margin-right:6px"><DataAnalysis /></el-icon>转化漏斗</h3></div>
        <div class="card-body" style="padding:10px 14px">
          <div v-for="(f,i) in funnelSteps" :key="i" style="margin-bottom:9px">
            <div style="display:flex;justify-content:space-between;font-size:12px;margin-bottom:3px;align-items:center">
              <span style="color:#909399">{{ f.label }}</span>
              <span style="font-weight:600">{{ f.value.toLocaleString() }}<span v-if="i>0" style="color:#909399;font-weight:400;font-size:11px"> ({{ convRate(i) }}转化)</span><span v-if="i>0" style="color:#e17055;font-size:10px;margin-left:3px"> -{{ dropRate(i) }}%流失</span></span>
            </div>
            <div style="height:16px;background:#f0f2f5;border-radius:4px;overflow:hidden"><div :style="{height:'100%',width:Math.round(f.value/funnelSteps[0].value*100)+'%',background:f.color,borderRadius:'4px',opacity:.85}"></div></div>
          </div>
        </div>
      </div>

      <!-- User Analytics -->
      <div class="card">
        <div class="card-header"><h3><el-icon :size="16" color="#00b894" style="margin-right:6px"><User /></el-icon>用户分析</h3></div>
        <div class="card-body" style="padding:10px 14px">
          <div style="margin-bottom:10px">
            <div style="display:flex;justify-content:space-between;font-size:12px;margin-bottom:4px"><span style="color:#909399">新用户</span><span style="font-weight:700;color:#6c5ce7">4,820 <span style="color:#909399;font-weight:400">(47%)</span></span></div>
            <div style="display:flex;justify-content:space-between;font-size:12px;margin-bottom:8px"><span style="color:#909399">老用户</span><span style="font-weight:700;color:#00b894">5,350 <span style="color:#909399;font-weight:400">(53%)</span></span></div>
            <div style="height:8px;background:#f0f2f5;border-radius:4px;overflow:hidden;display:flex"><div style="width:47%;background:#6c5ce7;border-radius:4px 0 0 4px"></div><div style="flex:1;background:#00b894;border-radius:0 4px 4px 0"></div></div>
          </div>
          <div class="user-metric-row"><span style="color:#909399">跳出率</span><span class="user-metric-val" style="color:#00b894">38.2%</span></div>
          <div class="user-metric-row"><span style="color:#909399">平均会话时长</span><span class="user-metric-val">4m 32s</span></div>
          <div style="margin-top:10px;font-size:11px;color:#909399;font-weight:600;margin-bottom:6px">用户来源</div>
          <div v-for="s in userSources" :key="s.label" class="user-source-row">
            <span style="width:52px;color:#909399">{{ s.label }}</span>
            <div style="flex:1;height:5px;background:#f0f2f5;border-radius:3px;overflow:hidden;margin:0 6px"><div :style="{width:s.pct+'%',height:'100%',background:s.color,borderRadius:'3px'}"></div></div>
            <span style="width:28px;text-align:right;font-weight:600">{{ s.pct }}%</span>
          </div>
        </div>
      </div>

      <!-- KPI Gauges -->
      <div class="card">
        <div class="card-header"><h3><el-icon :size="16" color="#e17055" style="margin-right:6px"><Aim /></el-icon>核心KPI</h3></div>
        <div class="card-body" style="padding:10px 8px">
          <div class="kpi-grid2">
            <div v-for="g in kpiGauges" :key="g.label" class="kpi-gauge-item">
              <svg width="68" height="68" viewBox="0 0 68 68"><circle cx="34" cy="34" r="26" fill="none" stroke="#f0f2f5" stroke-width="6"/><circle cx="34" cy="34" r="26" fill="none" :stroke="g.color" stroke-width="6" :stroke-dasharray="g.pct/100*(2*Math.PI*26)+' '+(2*Math.PI*26)" stroke-dashoffset="40.8" stroke-linecap="round" transform="rotate(-90 34 34)" style="transform-origin:34px 34px"/></svg>
              <span class="gauge-val" :style="{color:g.color}">{{ g.val }}</span>
              <div class="gauge-label">{{ g.label }}</div>
            </div>
          </div>
          <div class="kpi-extra"><div v-for="e in kpiExtra" :key="e.label" class="kpi-extra-row"><span style="color:#909399">{{ e.label }}</span><span class="kpi-extra-val" :style="{color:e.color}">{{ e.val }}</span></div></div>
        </div>
      </div>

      <!-- Satisfaction -->
      <div class="card">
        <div class="card-header"><h3><el-icon :size="16" color="#fdcb6e" style="margin-right:6px"><StarFilled /></el-icon>满意度评分</h3></div>
        <div class="card-body" style="padding:10px 14px">
          <div style="text-align:center;margin-bottom:8px">
            <div style="font-size:36px;font-weight:700;color:#fdcb6e">4.8</div>
            <div class="stars-display"><span v-for="i in 5" :key="i" :style="{color:i<=4?'#fdcb6e':i-0.2<=4?'#fdcb6e':'#e0e3e8'}">★</span></div>
            <div style="font-size:12px;color:#909399">基于 2,968 条评价</div>
          </div>
          <div v-for="(r,i) in ratingBars" :key="i" class="rating-bar-row">
            <span class="rating-star-label">{{ r.star }}★</span>
            <div class="rating-bar-wrap"><div class="rating-bar-fill" :style="{width:r.pct+'%',background:r.color}"></div></div>
            <span class="rating-count">{{ r.count }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Row 5: Manager Todo -->
    <div class="card" style="margin-bottom:20px">
      <div class="card-header"><h3><el-icon :size="16" color="#6c5ce7" style="margin-right:6px"><List /></el-icon>待处理事项</h3><span class="todo-badge">{{ todoCount }}</span></div>
      <div class="card-body" style="padding:0 16px 8px">
        <div v-for="(t,i) in todos" :key="i" class="todo-row2">
          <div class="todo-priority" :class="'prio-'+t.priority"></div>
          <div style="flex:1"><div class="todo-title">{{ t.title }}</div><div class="todo-desc">{{ t.desc }}</div></div>
          <span class="todo-tag" :style="{background:t.tagBg,color:t.tagFg}">{{ t.tag }}</span>
          <span class="todo-due">{{ t.due }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getDashboardStats, getAdminProducts, getAdminOrders, getPromotionStats } from '../../api/admin'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart as EChartsPieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'
import { Download, Warning, Present, Close, TrendCharts, PieChart as PieChartIcn, TrophyBase, Box, Bell, Timer, Document, Location, List, CircleCheck, CircleClose, Money, ShoppingBag, User, View, Star, StarFilled, Aim, DataAnalysis, ArrowUp, ArrowDown, Minus } from '@element-plus/icons-vue'

use([CanvasRenderer, BarChart, LineChart, EChartsPieChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent])

const stats=ref({});const allProducts=ref([]);const allOrders=ref([]);const promoStats=ref({})
const dateTab=ref('month');const showWarn=ref(true);const orderFilter=ref('all')

// KPI Cards (8)
const lowStockCount=computed(()=>(allProducts.value||[]).filter(p=>p.stock<=5&&p.stock>0).length)
const outStock=computed(()=>(allProducts.value||[]).filter(p=>p.stock===0).length)
const lowStock=computed(()=>lowStockCount.value)
const healthy=computed(()=>(allProducts.value||[]).length-lowStock.value-outStock.value)
const totalProducts=computed(()=>(allProducts.value||[]).length)
const activePromos=computed(()=>promoStats.value?.active||0)

const kpiCards=computed(()=>{
  const s=stats.value||{}
  return[
    {icon:Money,bg:'rgba(108,92,231,.1)',color:'#6c5ce7',label:'销售额',val:'¥'+(Number(s.totalRevenue||0)/10000).toFixed(1)+'万',trendVal:'12.5%',trendDir:'up',trendIcon:ArrowUp,sub:'较上期'},
    {icon:TrendCharts,bg:'rgba(0,184,148,.1)',color:'#00b894',label:'毛利润',val:'¥'+(Number(s.totalRevenue||0)*0.4/10000).toFixed(1)+'万',trendVal:'9.8%',trendDir:'up',trendIcon:ArrowUp,sub:'毛利率 40.0%'},
    {icon:ShoppingBag,bg:'rgba(230,126,34,.12)',color:'#e67e22',label:'订单数',val:(s.orderCount||0).toLocaleString(),trendVal:'8.2%',trendDir:'up',trendIcon:ArrowUp,sub:'较上期'},
    {icon:Document,bg:'rgba(253,121,168,.12)',color:'#fd79a8',label:'客单价',val:'¥'+((Number(s.totalRevenue||0))/(s.orderCount||1)).toFixed(0),trendVal:'4.1%',trendDir:'up',trendIcon:ArrowUp,sub:'较上期'},
    {icon:User,bg:'rgba(116,185,255,.1)',color:'#74b9ff',label:'活跃用户',val:(s.userCount||0).toLocaleString(),trendVal:'3.1%',trendDir:'down',trendIcon:ArrowDown,sub:'较上期'},
    {icon:View,bg:'rgba(0,206,201,.12)',color:'#00cec9',label:'页面浏览量',val:'89.0万',trendVal:'15.4%',trendDir:'up',trendIcon:ArrowUp,sub:'较上期'},
    {icon:DataAnalysis,bg:'rgba(162,155,254,.12)',color:'#a29bfe',label:'转化率',val:'3.8%',trendVal:'0.6%',trendDir:'up',trendIcon:ArrowUp,sub:'访客→购买'},
    {icon:CircleClose,bg:'rgba(225,112,85,.1)',color:'#e17055',label:'退款率',val:'2.3%',trendVal:'-',trendDir:'flat',trendIcon:Minus,sub:'低于行业均值'},
  ]
})

// Top products
const topProducts=computed(()=>[...(allProducts.value||[])].sort((a,b)=>(b.sales||0)-(a.sales||0)).slice(0,5))
function marginPct(p){const price=Number(p.price||0);const orig=Number(p.originalPrice||price);return orig>price?Math.round((1-price/orig)*100):42}
function marginColor(p){const m=marginPct(p);return m>=50?'#00b894':m>=35?'#e67e22':'#e17055'}

// Category sparkline data
const catSparkData=ref([{name:'数码产品',color:'#6c5ce7',pct:38,val:'¥487K',trend:'up',trendVal:'+15%'},{name:'电脑办公',color:'#00b894',pct:24,val:'¥308K',trend:'up',trendVal:'+8%'},{name:'摄影器材',color:'#74b9ff',pct:18,val:'¥231K',trend:'down',trendVal:'-3%'},{name:'服装鞋帽',color:'#fd79a8',pct:11,val:'¥141K',trend:'up',trendVal:'+5%'},{name:'家居生活',color:'#fdcb6e',pct:6,val:'¥77K',trend:'flat',trendVal:'0%'},{name:'运动户外',color:'#e17055',pct:3,val:'¥38K',trend:'down',trendVal:'-2%'}])

// Activities
const activities=ref([{icon:Box,bg:'rgba(108,92,231,.12)',fg:'#6c5ce7',title:'新品 <b>无线鼠标静音版</b> 已上架',time:'30分钟前'},{icon:TrendCharts,bg:'rgba(230,126,34,.12)',fg:'#e67e22',title:'销售额突破 <b>¥50万</b> 大关',time:'2小时前'},{icon:Star,bg:'rgba(0,184,148,.12)',fg:'#00b894',title:'用户好评率达到 <b>96.8%</b>',time:'5小时前'},{icon:Document,bg:'rgba(225,112,85,.12)',fg:'#e17055',title:'订单 <b>#20241215001</b> 已退款完成',time:'6小时前'},{icon:ShoppingBag,bg:'rgba(0,206,201,.12)',fg:'#00cec9',title:'订单 <b>#ORD-2025-8830</b> 已发货',time:'8小时前'}])
// Low stock items
const lowStockItems=computed(()=>(allProducts.value||[]).filter(p=>p.stock<=5).slice(0,5))
// Recent orders (prototype mock data)
const recentOrders=computed(()=>{
  const mock=[{orderNo:'#ORD-2025-8842',receiverName:'张伟',itemsSummary:'5G智能手机 旗舰版',totalAmount:3999,channel:'微信小程序',status:'on',statusText:'已完成',dateLabel:'2025-05-30'},{orderNo:'#ORD-2025-8841',receiverName:'李娜',itemsSummary:'智能手表 Pro',totalAmount:1599,channel:'APP',status:'draft',statusText:'处理中',dateLabel:'2025-05-30'},{orderNo:'#ORD-2025-8840',receiverName:'王芳',itemsSummary:'无线蓝牙耳机',totalAmount:299,channel:'PC官网',status:'on',statusText:'已完成',dateLabel:'2025-05-30'},{orderNo:'#ORD-2025-8839',receiverName:'刘洋',itemsSummary:'机械键盘 87键',totalAmount:459,channel:'APP',status:'draft',statusText:'待发货',dateLabel:'2025-05-29'},{orderNo:'#ORD-2025-8838',receiverName:'陈晨',itemsSummary:'专业定焦镜头 50mm',totalAmount:2899,channel:'PC官网',status:'on',statusText:'已完成',dateLabel:'2025-05-29'}]
  return mock.filter(o=>orderFilter.value==='all'||o.status===orderFilter.value)
})
const totalOrderCount=computed(()=>(allOrders.value?.length)||10)
const orderFilters=[{key:'all',label:'全部'},{key:'on',label:'已完成'},{key:'draft',label:'处理中'},{key:'off',label:'已取消'}]
function orderStatusLabel(s){const m={on:'已完成',draft:'处理中',off:'已取消',PENDING:'待付款',PAID:'已支付',SHIPPED:'已发货',CANCELLED:'已取消'};return m[s]||s}
function orderStatusClass(s){return{on:'sb-done',draft:'sb-draft',off:'sb-off',PENDING:'sb-draft',PAID:'sb-done',SHIPPED:'sb-on',CANCELLED:'sb-off'}[s]||''}

// 24h hourly
const hourlyData=[2,1,0,0,1,3,8,18,26,32,28,35,40,38,30,32,36,42,48,44,38,30,20,12]
const peakVal=computed(()=>Math.max(...hourlyData))
const peakHour=computed(()=>hourlyData.indexOf(peakVal.value))
const hourlyOption={tooltip:{trigger:'axis'},grid:{left:25,right:8,top:8,bottom:20},xAxis:{type:'category',data:hourlyData.map((_,i)=>i%4===0?i+':00':''),axisLabel:{fontSize:9,color:'#b2bec3',interval:0},axisLine:{show:false},axisTick:{show:false}},yAxis:{type:'value',splitLine:{lineStyle:{color:'#e8ecf0'}},axisLabel:{fontSize:8,color:'#b2bec3'}},series:[{type:'bar',data:hourlyData,itemStyle:{color:'#a29bfe',opacity:.55,borderRadius:[2,2,0,0]},barMaxWidth:10,barGap:'10%',emphasis:{itemStyle:{color:'#6c5ce7',opacity:1}}}]}

// Geo sales
const geoCities=ref([{name:'上海',val:'¥285K',pct:100,trend:'+18%',up:true,color:'#6c5ce7'},{name:'北京',val:'¥241K',pct:84,trend:'+12%',up:true,color:'#6c5ce7'},{name:'广州',val:'¥178K',pct:62,trend:'+9%',up:true,color:'#6c5ce7'},{name:'深圳',val:'¥156K',pct:55,trend:'+21%',up:true,color:'#00b894'},{name:'杭州',val:'¥112K',pct:39,trend:'-2%',up:false,color:'#e17055'},{name:'成都',val:'¥89K',pct:31,trend:'+5%',up:true,color:'#6c5ce7'}])
const rankColors=['rgba(253,203,110,.3)','rgba(189,195,199,.3)','rgba(230,126,34,.2)']
const rankFgMap=['#d68910','#7f8c8d','#a04000']
function rankBg(i){return i<3?rankColors[i]:'#f5f7fa'}
function rankFg(i){return i<3?rankFgMap[i]:'#909399'}

// Donut
const donutData=computed(()=>[{name:'电子产品',color:'#6c5ce7',pct:38},{name:'服装',color:'#00b894',pct:22},{name:'家居',color:'#e67e22',pct:18},{name:'运动',color:'#74b9ff',pct:12},{name:'其他',color:'#fd79a8',pct:10}])

// Geo

// Funnel with conversion/drop rates
const funnelSteps=[{label:'访问页面',value:89400,color:'#6c5ce7'},{label:'浏览商品',value:52300,color:'#a29bfe'},{label:'加入购物车',value:18600,color:'#74b9ff'},{label:'发起结算',value:8240,color:'#00b894'},{label:'完成支付',value:3396,color:'#00cec9'}]
function convRate(i){return i>0?((funnelSteps[i].value/funnelSteps[i-1].value)*100).toFixed(1)+'%':'100%'}
function dropRate(i){return i>0?(100-(funnelSteps[i].value/funnelSteps[i-1].value)*100).toFixed(1):'0'}

// User sources
const userSources=ref([{label:'搜索引擎',pct:38,color:'#6c5ce7'},{label:'直接访问',pct:27,color:'#00b894'},{label:'社交媒体',pct:20,color:'#74b9ff'},{label:'推荐链接',pct:10,color:'#fd79a8'}])

// KPI Gauges with SVG rings
const kpiGauges=[{label:'转化率',val:'3.8%',pct:38,color:'#6c5ce7'},{label:'复购率',val:'34.2%',pct:34,color:'#00b894'},{label:'满意度',val:'94.6%',pct:95,color:'#fdcb6e'}]
const kpiExtra=[{label:'NPS 净推荐值',val:'68',color:'#00b894'},{label:'购物车放弃率',val:'62.4%',color:'#e67e22'},{label:'首次响应时长',val:'< 2h',color:'#6c5ce7'},{label:'售后解决率',val:'96.8%',color:'#00b894'}]

// Rating bars
const ratingBars=ref([{star:5,pct:62,count:1842,color:'#00b894'},{star:4,pct:24,count:712,color:'#a2d679'},{star:3,pct:8,count:238,color:'#fdcb6e'},{star:2,pct:3,count:89,color:'#e67e22'},{star:1,pct:3,count:89,color:'#e17055'}])

// Todos (matching prototype)
const todos=ref([{priority:'danger',title:'处理退款申请',desc:'3 笔待审核退款，最早已等待 18 小时',tag:'售后',tagBg:'rgba(225,112,85,.12)',tagFg:'#e17055',due:'今日'},{priority:'warning',title:'补充库存 — 便携充电宝',desc:'库存为 0，历史日均销量 107 件',tag:'库存',tagBg:'rgba(253,203,110,.2)',tagFg:'#d68910',due:'今日'},{priority:'warning',title:'审核双12促销活动',desc:'活动明日上线，3 个方案待最终确认',tag:'营销',tagBg:'rgba(116,185,255,.2)',tagFg:'#2980b9',due:'今日'},{priority:'info',title:'更新首页 Banner 素材',desc:'本周五到期，需上传新版视觉物料',tag:'页面',tagBg:'rgba(108,92,231,.1)',tagFg:'#6c5ce7',due:'本周'},{priority:'info',title:'回复询盘 — 海外客户',desc:'询盘已超 48 小时未回复',tag:'询盘',tagBg:'rgba(0,184,148,.12)',tagFg:'#00b894',due:'本周'},{priority:'muted',title:'导出 11 月销售报表',desc:'财务部请求提供完整月度报表',tag:'报表',tagBg:'rgba(99,110,114,.1)',tagFg:'#636e72',due:'本月'}])
const todoCount=computed(()=>todos.value.filter(t=>t.priority!=='muted').length)

// Charts
// Sales trend: 30 data points, 3-line overlay (purple revenue + green orders + pink dashed profit)
const salesLine=[38,52,45,68,72,60,80,95,88,105,112,98,125,140,132,118,145,162,158,175,190,185,200,215,210,228,240,235,252,265]
const orderLine2=[12,18,15,22,25,20,28,32,30,36,38,34,42,46,44,40,48,54,52,58,62,60,66,70,68,72,76,74,80,84]
const profitLine2=[15,20,18,26,28,24,32,38,35,40,44,39,50,56,52,47,58,65,63,70,76,74,80,86,84,91,96,94,100,106]
const barLabels=salesLine.map((_,i)=>i%5===0||i===29?String(i+1):'')

const barOption={tooltip:{trigger:'axis'},grid:{left:48,right:16,top:16,bottom:28},xAxis:{type:'category',data:barLabels,axisLine:{lineStyle:{color:'#e8ecf0'}},axisTick:{show:false},axisLabel:{color:'#b2bec3',fontSize:10,interval:0}},yAxis:{type:'value',splitLine:{lineStyle:{color:'#e8ecf0'}},axisLabel:{color:'#b2bec3',fontSize:10,formatter:v=>v>=1000?Math.round(v/1000)+'K':v}},series:[{name:'销售额',type:'line',data:salesLine,smooth:false,lineStyle:{color:'#6c5ce7',width:2.5},symbol:'circle',symbolSize:3,itemStyle:{color:'#6c5ce7'},areaStyle:{color:{type:'linear',x:0,y:0,x2:0,y2:1,colorStops:[{offset:0,color:'rgba(108,92,231,.18)'},{offset:1,color:'rgba(108,92,231,0)'}]}}},{name:'订单量',type:'line',data:orderLine2,smooth:false,lineStyle:{color:'#00b894',width:2,opacity:.7},symbol:'none'},{name:'毛利',type:'line',data:profitLine2,smooth:false,lineStyle:{color:'#fd79a8',width:1.8,type:'dashed',opacity:.8},symbol:'none'}]}

const pieOption={tooltip:{trigger:'item',formatter:'{b}: {c}%'},series:[{type:'pie',radius:['57%','77%'],center:['50%','50%'],data:donutData.value.map(d=>({name:d.name,value:d.pct,itemStyle:{color:d.color}})),label:{show:false},emphasis:{scaleSize:6}}]}

const thisWeek=[180,210,185,250,320,420,380];const lastWeek2=[160,195,170,228,290,385,345]
const weekDiff=computed(()=>{const t=thisWeek.reduce((a,b)=>a+b,0);const l=lastWeek2.reduce((a,b)=>a+b,0);return{total:t,diff:((t-l)/l*100).toFixed(1)}})
const weekOption={tooltip:{trigger:'axis'},grid:{left:28,right:8,top:10,bottom:20},xAxis:{type:'category',data:['一','二','三','四','五','六','日'],axisLabel:{fontSize:9,color:'#b2bec3'},axisLine:{show:false},axisTick:{show:false}},yAxis:{type:'value',splitLine:{lineStyle:{color:'#e8ecf0'}},axisLabel:{fontSize:8,color:'#b2bec3',formatter:v=>v>=1000?Math.round(v/1000)+'K':v}},series:[{name:'上周',type:'bar',data:lastWeek2,itemStyle:{color:'#a29bfe',opacity:.6,borderRadius:[2,2,0,0]},barMaxWidth:10,barGap:'20%'},{name:'本周',type:'bar',data:thisWeek,itemStyle:{color:'#6c5ce7',opacity:.9,borderRadius:[2,2,0,0]},barMaxWidth:10}]}

// Profit strip below sales trend (4 items matching prototype)
const profitStrip=computed(()=>{const s=stats.value;return[{label:'本期收入',val:'¥'+(Number(s.totalRevenue||1285600)/10000).toFixed(1)+'万',color:'#6c5ce7',sub:'↑ 12.5% vs 上期'},{label:'毛利润',val:'¥'+(Number(s.totalRevenue||1285600)*0.4/10000).toFixed(1)+'万',color:'#00b894',sub:'毛利率 40.0%'},{label:'客单价',val:'¥'+Math.round((Number(s.totalRevenue||1285600))/(s.orderCount||12840)),color:'#e67e22',sub:'↑ 4.1% vs 上期'},{label:'订单量',val:(s.orderCount||12840).toLocaleString(),color:'#74b9ff',sub:'↑ 8.2% vs 上期'}]})

onMounted(async()=>{
  try{const r=await getDashboardStats();if(r.data)stats.value=r.data}catch{}
  try{const p=await getAdminProducts({page:1,size:1000});allProducts.value=p.data?.list||[]}catch{}
  try{const o=await getAdminOrders({page:1,size:100});allOrders.value=o.data?.list||[]}catch{}
  try{const pm=await getPromotionStats();if(pm.data)promoStats.value=pm.data}catch{}
})
</script>

<style scoped>
.dash-page{max-width:100%}
.page-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:16px}
.page-header h2{font-size:20px;font-weight:600;margin:0}
.page-header-actions{display:flex;gap:12px;align-items:center}
.btn{padding:8px 16px;border:none;border-radius:8px;cursor:pointer;font-size:13px;font-weight:500;display:inline-flex;align-items:center;gap:6px;transition:all .2s;font-family:inherit}
.btn-outline{background:transparent;border:1px solid #e0e3e8;color:#606266}.btn-outline:hover{border-color:#6c5ce7;color:#6c5ce7}
.btn-sm{padding:5px 12px;font-size:12px}
.card{background:#fff;border-radius:12px;border:1px solid #e0e3e8;box-shadow:0 1px 4px rgba(0,0,0,.04)}
.card-header{padding:14px 16px;border-bottom:1px solid #e0e3e8;display:flex;align-items:center;justify-content:space-between}
.card-header h3{font-size:14px;font-weight:600;margin:0;display:flex;align-items:center}
.card-body{padding:20px}

/* Date tabs */
.dash-date-tabs{display:flex;gap:2px;background:#f5f7fa;border-radius:8px;padding:2px}
.dash-date-tab{padding:5px 12px;border:none;border-radius:6px;font-size:12px;cursor:pointer;background:transparent;color:#909399;font-family:inherit;transition:all .2s}
.dash-date-tab.active{background:#fff;color:#6c5ce7;box-shadow:0 1px 3px rgba(0,0,0,.06);font-weight:600}

/* Alerts */
.dash-alerts{margin-bottom:16px;display:flex;flex-direction:column;gap:8px}
.dash-alert{display:flex;align-items:center;gap:8px;padding:10px 14px;border-radius:8px;font-size:13px}
.dash-alert.warning{background:rgba(253,203,110,.12);border:1px solid rgba(253,203,110,.3);color:#8a6d14}
.dash-alert.info{background:rgba(116,185,255,.1);border:1px solid rgba(116,185,255,.25);color:#225588}
.dash-alert span{flex:1}.dash-alert b{font-weight:700}
.alert-close{background:none;border:none;cursor:pointer;color:inherit;opacity:.6;padding:0}.alert-close:hover{opacity:1}

/* 8 Stat cards */
.stat-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:14px;margin-bottom:20px}
.stat-card{background:#fff;border-radius:12px;padding:18px 20px;border:1px solid #e0e3e8;box-shadow:0 1px 4px rgba(0,0,0,.04);transition:all .2s}
.stat-card:hover{box-shadow:0 2px 12px rgba(0,0,0,.06);transform:translateY(-1px)}
.stat-card-header{display:flex;align-items:center;justify-content:space-between;margin-bottom:10px}
.stat-card-icon{width:40px;height:40px;border-radius:10px;display:flex;align-items:center;justify-content:center}
.stat-card-trend{font-size:12px;font-weight:500;display:flex;align-items:center;gap:2px}.stat-card-trend.up{color:#00b894}.stat-card-trend.down{color:#e17055}.stat-card-trend.flat{color:#909399}
.stat-card-value{font-size:26px;font-weight:700;margin-bottom:2px}
.stat-card-label{font-size:13px;color:#909399}.stat-card-sub{font-size:11px;color:#c0c4cc;margin-top:2px}

/* Profit strip */
.profit-strip{display:flex;border-top:1px solid #e0e3e8;padding:10px 16px}
.profit-item{flex:1;text-align:center;padding:0 12px;border-right:1px solid #f0f0f0}.profit-item:last-child{border-right:none}
.profit-label{font-size:11px;color:#909399;margin-bottom:2px}
.profit-val{font-size:14px;font-weight:700}
.profit-sub{font-size:10px;color:#b2bec3}

/* Week compare note */
.week-note{display:flex;justify-content:center;gap:14px;font-size:11px;padding:4px 0 2px}

/* Grid layouts */
.dash-grid-wide{display:grid;grid-template-columns:1fr 380px;gap:20px;margin-bottom:20px}
.dash-row4{display:grid;grid-template-columns:repeat(4,1fr);gap:16px;margin-bottom:20px}

.legend-line{width:20px;height:3px;border-radius:2px;display:inline-block}.dashed{background:transparent!important;border-top:2px dashed #fd79a8;height:0}

/* Donut legend */
.donut-legend-row{display:flex;align-items:center;gap:6px;padding:3px 0}
.donut-dot{width:8px;height:8px;border-radius:2px;flex-shrink:0}

/* Top products */
.top-product-row{display:flex;align-items:center;gap:8px;padding:7px 0;border-bottom:1px solid #f5f7fa}.top-product-row:last-child{border-bottom:none}
.top-rank{width:20px;height:20px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:11px;font-weight:700;flex-shrink:0;background:#f5f7fa;color:#909399}
.top-rank.rank-1{background:#6c5ce7;color:#fff}.top-rank.rank-2{background:rgba(108,92,231,.2);color:#6c5ce7}.top-rank.rank-3{background:rgba(108,92,231,.1);color:#6c5ce7}
.top-icon{width:24px;height:24px;border-radius:6px;background:rgba(108,92,231,.08);display:flex;align-items:center;justify-content:center;flex-shrink:0}
.top-name{font-size:13px;font-weight:500;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.top-sales{font-size:11px;color:#909399}.top-rev{font-weight:600;color:#6c5ce7;font-size:12px;flex-shrink:0}

/* Category sparklines */
.spark-row{padding:5px 0;border-bottom:1px solid #f5f7fa}.spark-row:last-child{border-bottom:none}
.spark-info{display:flex;align-items:center;gap:6px;margin-bottom:4px}
.spark-dot{width:6px;height:6px;border-radius:50%;flex-shrink:0}
.spark-label{font-size:11px;color:#909399}
.spark-bar-wrap{flex:1;height:5px;background:#f5f7fa;border-radius:3px;overflow:hidden}
.spark-bar{height:100%;border-radius:3px;transition:width .5s}
.spark-val{font-size:11px;color:#606266;font-weight:500;flex-shrink:0}
.spark-trend{font-size:10px;font-weight:600;flex-shrink:0}.spark-trend.up{color:#00b894}.spark-trend.down{color:#e17055}

/* Inventory */
.inv-summary{display:grid;grid-template-columns:repeat(4,1fr);gap:6px;margin-bottom:4px}
.inv-badge{border-radius:8px;padding:8px;text-align:center}
.inv-val{font-size:18px;font-weight:700}.inv-label{font-size:10px;margin-top:2px}
.inv-section-title{font-size:11px;color:#909399;font-weight:600;margin:8px 0 4px}
.inv-item{display:flex;align-items:center;gap:6px;padding:3px 0;font-size:12px}
.inv-dot{width:6px;height:6px;border-radius:50%;flex-shrink:0}

/* Activity */
.activity-row{display:flex;gap:8px;padding:7px 0;border-bottom:1px solid #f5f7fa}.activity-row:last-child{border-bottom:none}
.act-icon{width:26px;height:26px;border-radius:6px;display:flex;align-items:center;justify-content:center;flex-shrink:0}
.act-title{font-size:12px;color:#303133}.act-time{font-size:10px;color:#909399;margin-top:1px;display:flex;align-items:center;gap:3px}

/* Recent orders */
.order-filter-btn{padding:4px 10px;border:1px solid #e0e3e8;border-radius:14px;font-size:11px;cursor:pointer;background:transparent;color:#909399;font-family:inherit;transition:all .15s}.order-filter-btn.active,.order-filter-btn:hover{border-color:#6c5ce7;color:#6c5ce7;background:rgba(108,92,231,.06)}
.status-badge{display:inline-block;padding:2px 6px;border-radius:10px;font-size:10px;font-weight:500;white-space:nowrap}
.sb-draft{background:rgba(253,203,110,.15);color:#e67e22}.sb-done{background:rgba(0,184,148,.1);color:#00b894}.sb-on{background:rgba(108,92,231,.1);color:#6c5ce7}.sb-off{background:#f5f5f5;color:#b2b2b2}

/* Recent orders footer */
.order-footer{display:flex;align-items:center;justify-content:space-between;padding:10px 16px;border-top:1px solid #e0e3e8;font-size:12px;color:#909399}
.order-id{color:#6c5ce7;font-weight:500}

/* Hourly peak */
.hourly-peak{font-size:11px;color:#909399;text-align:center;padding:4px 0}

/* Geo */
.geo-row{display:flex;align-items:center;gap:6px;padding:5px 0;font-size:13px;border-bottom:1px solid #f5f7fa}.geo-row:last-child{border-bottom:none}
.geo-rank{width:20px;height:20px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:11px;font-weight:600;flex-shrink:0}
.geo-bar-wrap{flex:1;height:5px;background:#f5f7fa;border-radius:3px;overflow:hidden;margin:0 6px}
.geo-bar-fill{height:100%;border-radius:3px}
.geo-val{font-weight:600;font-size:12px;flex-shrink:0}
.geo-trend{font-size:10px;font-weight:600;padding:1px 5px;border-radius:4px;flex-shrink:0}

/* User metrics */
.user-metric-row{display:flex;justify-content:space-between;font-size:12px;padding:3px 0}
.user-metric-val{font-weight:600}

/* KPI Gauges */
.kpi-grid2{display:flex;justify-content:space-around;gap:8px}
.kpi-gauge-item{text-align:center;position:relative}
.kpi-gauge-item svg{display:block;margin:0 auto}
.gauge-val{position:absolute;top:22px;left:50%;transform:translateX(-50%);font-size:11px;font-weight:700}
.gauge-label{font-size:11px;color:#909399;margin-top:2px}
.kpi-extra{margin-top:12px;padding-top:10px;border-top:1px solid #e0e3e8}
.kpi-extra-row{display:flex;justify-content:space-between;padding:3px 0;font-size:12px}
.kpi-extra-val{font-weight:600}

/* Todos */
.todo-badge{background:#e17055;color:#fff;font-size:11px;padding:2px 8px;border-radius:10px;font-weight:600}
.todo-row2{display:flex;align-items:center;gap:10px;padding:10px 0;border-bottom:1px solid #f5f7fa}.todo-row2:last-child{border-bottom:none}
.todo-priority{width:6px;height:6px;border-radius:50%;flex-shrink:0}.prio-danger{background:#e17055}.prio-warning{background:#fdcb6e}.prio-info{background:#74b9ff}.prio-muted{background:#b2bec2}
.todo-title{font-size:13px;font-weight:500}.todo-desc{font-size:11px;color:#909399;margin-top:1px}
.todo-tag{font-size:10px;padding:1px 6px;border-radius:4px;font-weight:600;flex-shrink:0}
.todo-due{font-size:11px;color:#909399;flex-shrink:0}

/* Funnel */
.funnel-row{display:flex;align-items:center;gap:8px;padding:5px 0;font-size:12px}
.funnel-label{width:60px;flex-shrink:0;color:#909399;font-size:11px}
.funnel-bar-wrap{flex:1;height:18px;background:#f5f7fa;border-radius:4px;overflow:hidden}
.funnel-bar{height:100%;border-radius:4px;transition:width .5s}
.funnel-val{width:50px;text-align:right;font-weight:600;font-size:11px;flex-shrink:0}

/* User sources */
.user-source-row{display:flex;align-items:center;gap:6px;padding:4px 0;font-size:12px}
.source-dot{width:8px;height:8px;border-radius:2px;flex-shrink:0}
.source-label{width:55px;font-size:11px;color:#909399;flex-shrink:0}
.source-val{width:28px;text-align:right;font-weight:600;font-size:11px}
.source-bar-wrap{flex:1;height:6px;background:#f5f7fa;border-radius:3px;overflow:hidden}
.source-bar{height:100%;border-radius:3px}

/* KPI Gauges */
.kpi-grid{display:grid;grid-template-columns:1fr 1fr;gap:10px}
.kpi-gauge-item{text-align:center}
.gauge-label{font-size:11px;color:#909399;margin-bottom:4px}
.gauge-ring{width:48px;height:48px;border-radius:50%;display:flex;align-items:center;justify-content:center;margin:0 auto 4px;position:relative}
.gauge-inner{width:36px;height:36px;border-radius:50%;background:#fff;display:flex;align-items:center;justify-content:center;font-size:13px;font-weight:700}
.gauge-sub{font-size:11px;color:#909399}

/* Satisfaction */
.stars-display{font-size:22px;letter-spacing:3px;margin:4px 0}
.rating-bar-row{display:flex;align-items:center;gap:6px;padding:2px 0;font-size:12px}
.rating-star-label{width:24px;color:#909399;flex-shrink:0}
.rating-bar-wrap{flex:1;height:8px;background:#f5f7fa;border-radius:4px;overflow:hidden}
.rating-bar-fill{height:100%;border-radius:4px}
.rating-count{width:30px;text-align:right;color:#909399;font-size:11px;flex-shrink:0}

@media(max-width:1200px){.stat-grid{grid-template-columns:repeat(3,1fr)}.dash-grid-wide{grid-template-columns:1fr}.dash-row4{grid-template-columns:repeat(2,1fr)}}
@media(max-width:768px){.stat-grid{grid-template-columns:repeat(2,1fr)}.dash-row4{grid-template-columns:1fr}}
</style>
