<template>
  <div class="dashboard">
    <h2>仪表盘</h2>

    <!-- Stat Cards -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background:#e8f4fd"><el-icon :size="28" color="#409EFF"><GoodsFilled /></el-icon></div>
          <div class="stat-body">
            <h3>{{ stats.productCount }}</h3>
            <p>商品总数</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background:#e8f8e8"><el-icon :size="28" color="#67C23A"><Document /></el-icon></div>
          <div class="stat-body">
            <h3>{{ stats.orderCount }}</h3>
            <p>订单总数</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background:#fef0e8"><el-icon :size="28" color="#E6A23C"><UserFilled /></el-icon></div>
          <div class="stat-body">
            <h3>{{ stats.userCount }}</h3>
            <p>用户总数</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background:#fde8ec"><el-icon :size="28" color="#F56C6C"><Money /></el-icon></div>
          <div class="stat-body">
            <h3>¥{{ formatMoney(stats.totalRevenue) }}</h3>
            <p>总营收</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts Row -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header><span class="card-title">近7天订单趋势</span></template>
          <v-chart :option="barOption" style="height:350px" autoresize />
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header><span class="card-title">订单状态分布</span></template>
          <v-chart :option="pieOption" style="height:350px" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <!-- Revenue Trend + Recent Orders -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header><span class="card-title">营收趋势</span></template>
          <v-chart :option="lineOption" style="height:320px" autoresize />
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header><span class="card-title">最近订单</span></template>
          <el-table :data="stats.recentOrders || []" size="small" style="width:100%" max-height="280">
            <el-table-column prop="orderNo" label="订单号" width="160" show-overflow-tooltip />
            <el-table-column prop="totalAmount" label="金额" width="90">
              <template #default="{ row }">¥{{ row.totalAmount }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="150">
              <template #default="{ row }">{{ row.createTime?.substring(0, 16).replace('T', ' ') }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getDashboardStats } from '../../api/admin'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'
import { GoodsFilled, Document, UserFilled, Money } from '@element-plus/icons-vue'

use([CanvasRenderer, BarChart, LineChart, PieChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent])

const stats = ref({
  productCount: 0,
  orderCount: 0,
  userCount: 0,
  totalRevenue: 0,
  statusCounts: {},
  dailyOrders: [],
  recentOrders: [],
})

const statusMap = { PENDING: '待付款', PAID: '已支付', SHIPPED: '已发货', CANCELLED: '已取消' }
function statusLabel(s) { return statusMap[s] || s }
function statusTagType(s) {
  return { PENDING: 'warning', PAID: 'success', SHIPPED: '', CANCELLED: 'danger' }[s] || 'info'
}
function formatMoney(v) { return Number(v || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }

const barOption = computed(() => {
  const daily = stats.value.dailyOrders || []
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '营收(¥)'], bottom: 0 },
    grid: { left: 10, right: 10, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: daily.map(d => d.date.substring(5)) },
    yAxis: [
      { type: 'value', name: '订单数', minInterval: 1 },
      { type: 'value', name: '营收(¥)', axisLabel: { formatter: '¥{value}' } },
    ],
    series: [
      {
        name: '订单数',
        type: 'bar',
        data: daily.map(d => d.count),
        itemStyle: { color: '#409EFF', borderRadius: [4, 4, 0, 0] },
        barMaxWidth: 32,
      },
      {
        name: '营收(¥)',
        type: 'line',
        yAxisIndex: 1,
        data: daily.map(d => d.revenue),
        smooth: true,
        lineStyle: { color: '#F56C6C', width: 2 },
        itemStyle: { color: '#F56C6C' },
        symbol: 'circle',
        symbolSize: 6,
      },
    ],
  }
})

const pieOption = computed(() => {
  const counts = stats.value.statusCounts || {}
  const data = Object.entries(counts).map(([k, v]) => ({ name: statusLabel(k), value: v }))
  if (data.length === 0) data.push({ name: '暂无数据', value: 1 })
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['45%', '72%'],
      center: ['50%', '45%'],
      data,
      label: { show: true, formatter: '{b}\n{d}%' },
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      color: ['#E6A23C', '#67C23A', '#409EFF', '#F56C6C'],
      emphasis: {
        label: { fontSize: 16, fontWeight: 'bold' },
        scaleSize: 8,
      },
    }],
  }
})

const lineOption = computed(() => {
  const daily = stats.value.dailyOrders || []
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 10, right: 10, top: 20, bottom: 20 },
    xAxis: { type: 'category', data: daily.map(d => d.date.substring(5)), boundaryGap: false },
    yAxis: { type: 'value', axisLabel: { formatter: '¥{value}' } },
    series: [{
      type: 'line',
      data: daily.map(d => d.revenue),
      smooth: true,
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(64,158,255,0.25)' },
            { offset: 1, color: 'rgba(64,158,255,0.02)' },
          ],
        },
      },
      lineStyle: { color: '#409EFF', width: 2.5 },
      itemStyle: { color: '#409EFF' },
      symbol: 'circle',
      symbolSize: 7,
    }],
  }
})

onMounted(async () => {
  try {
    const res = await getDashboardStats()
    if (res.data) stats.value = res.data
  } catch {}
})
</script>

<style scoped>
.dashboard h2 { margin: 0 0 24px; font-size: 20px; color: #333; }

.stat-row { margin-bottom: 20px; }
.stat-card { display: flex; align-items: center; }
.stat-card :deep(.el-card__body) { display: flex; align-items: center; gap: 16px; padding: 20px; }
.stat-icon { width: 56px; height: 56px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.stat-body h3 { font-size: 26px; margin: 0 0 4px; color: #333; line-height: 1.1; }
.stat-body p { margin: 0; font-size: 13px; color: #999; }

.chart-row { margin-bottom: 20px; }
.card-title { font-weight: 600; font-size: 15px; color: #333; }
</style>
