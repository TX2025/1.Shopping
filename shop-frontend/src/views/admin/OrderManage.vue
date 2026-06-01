<template>
  <div class="order-page">
    <div class="page-header">
      <h2>订单管理</h2>
      <div class="page-header-actions">
        <el-button plain><el-icon :size="14"><Download /></el-icon> 导出订单</el-button>
        <el-button type="primary"><el-icon :size="14"><Plus /></el-icon> 创建订单</el-button>
      </div>
    </div>

    <!-- Stats -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <div class="stat-card"><div class="stat-value" style="color:#E6A23C">{{ stats.PENDING || 0 }}</div><div class="stat-label">待支付</div></div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card"><div class="stat-value" style="color:#409EFF">{{ stats.PAID || 0 }}</div><div class="stat-label">待发货</div></div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card"><div class="stat-value" style="color:#67C23A">{{ stats.SHIPPED || 0 }}</div><div class="stat-label">已发货</div></div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card"><div class="stat-value" style="color:#F56C6C">{{ stats.CANCELLED || 0 }}</div><div class="stat-label">已取消</div></div>
      </el-col>
    </el-row>

    <!-- Table -->
    <div class="card">
      <div class="card-body" style="padding:0">
        <el-table :data="orders" stripe v-loading="loading" row-key="id" size="small">
          <el-table-column label="订单号" min-width="160">
            <template #default="{row}"><span class="order-id">{{ row.order?.orderNo }}</span></template>
          </el-table-column>
          <el-table-column label="客户" width="100">
            <template #default="{row}">{{ row.order?.receiverName || '-' }}</template>
          </el-table-column>
          <el-table-column label="产品" min-width="180">
            <template #default="{row}">
              <span v-for="(item, i) in row.items" :key="item.id">
                {{ item.productName }} x{{ item.quantity }}<span v-if="i < row.items.length - 1">, </span>
              </span>
              <span v-if="!row.items?.length">-</span>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="100">
            <template #default="{row}">¥{{ Number(row.order?.totalAmount || 0).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{row}">
              <span class="status-badge" :class="statusClass(row.order?.status)">{{ statusText(row.order?.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="160">
            <template #default="{row}">{{ formatTime(row.order?.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="130" fixed="right" align="center">
            <template #default="{row}">
              <div class="table-actions">
                <button class="action-btn" title="查看详情"><el-icon :size="14"><View /></el-icon></button>
                <template v-if="row.order?.status === 'PENDING'">
                  <button class="action-btn" title="标记支付" @click="updateStatus(row.order.id, 'PAID')" style="color:#67C23A;border-color:#67C23A"><el-icon :size="14"><Check /></el-icon></button>
                </template>
                <template v-if="row.order?.status === 'PAID'">
                  <button class="action-btn" title="发货" @click="updateStatus(row.order.id, 'SHIPPED')" style="color:#409EFF;border-color:#409EFF"><el-icon :size="14"><Van /></el-icon></button>
                </template>
                <template v-if="row.order?.status === 'PENDING'">
                  <button class="action-btn danger" title="取消" @click="updateStatus(row.order.id, 'CANCELLED')"><el-icon :size="14"><Close /></el-icon></button>
                </template>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && orders.length===0" description="暂无订单" :image-size="64" style="padding:40px 0" />
      </div>
      <div class="pagination" v-if="total>0">
        <span class="pagination-info">显示 {{ (page-1)*size+1 }}-{{ Math.min(page*size, total) }} 共 {{ total }} 条</span>
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="sizes, prev, pager, next"
          @size-change="s => { size = s; page = 1; load() }"
          @current-change="load"
          background
          small
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminOrders, updateOrderStatus, getOrderStats } from '../../api/admin'
import { ElMessage } from 'element-plus'
import { Download, Plus, View, Check, Close, Van } from '@element-plus/icons-vue'

const orders = ref([])
const loading = ref(false)
const stats = ref({})
const page = ref(1)
const size = ref(10)
const total = ref(0)

onMounted(() => { load(); loadStats() })

async function load() {
  loading.value = true
  try {
    const res = await getAdminOrders({ page: page.value, size: size.value })
    orders.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch {} finally { loading.value = false }
}

async function loadStats() {
  try {
    const res = await getOrderStats()
    if (res.data) stats.value = res.data
  } catch {}
}

async function updateStatus(id, status) {
  try {
    await updateOrderStatus(id, status)
    ElMessage.success('状态更新成功')
    load(); loadStats()
  } catch {}
}

function statusText(s) {
  const m = { PENDING: '待支付', PAID: '待发货', SHIPPED: '已发货', CANCELLED: '已取消' }
  return m[s] || s
}
function statusClass(s) {
  return { PENDING: 'draft', PAID: 'on', SHIPPED: 'on', CANCELLED: 'off' }[s] || ''
}
function formatTime(t) {
  if (!t) return '-'
  return t.substring(0, 16).replace('T', ' ')
}
</script>

<style scoped>
/* ===== Layout ===== */
.order-page { max-width: 100%; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 20px; font-weight: 600; margin: 0; }
.page-header-actions { display: flex; gap: 8px; }

/* ===== Stat Cards ===== */
.stat-row { margin-bottom: 20px; }
.stat-card {
  background: #fff; border-radius: 12px; padding: 20px;
  border: 1px solid #e0e3e8; box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  transition: all .2s;
}
.stat-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.06); transform: translateY(-1px); }
.stat-value { font-size: 26px; font-weight: 700; margin-bottom: 4px; }
.stat-label { font-size: 13px; color: #909399; }

/* ===== Card / Table ===== */
.card { background: #fff; border-radius: 12px; border: 1px solid #e0e3e8; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.card-body { padding: 20px; }

.order-id { color: #6c5ce7; font-weight: 500; }

/* Status Badge */
.status-badge {
  display: inline-block; padding: 2px 8px; border-radius: 20px;
  font-size: 11px; font-weight: 500; white-space: nowrap; line-height: 1.6;
}
.status-badge.on { background: rgba(0,184,148,0.1); color: #00b894; }
.status-badge.off { background: rgba(225,112,85,0.1); color: #e17055; }
.status-badge.draft { background: rgba(253,203,110,0.15); color: #e67e22; }

/* Action buttons */
.table-actions { display: flex; gap: 4px; justify-content: center; }
.action-btn {
  width: 30px; height: 30px; padding: 0; border-radius: 6px;
  background: transparent; border: 1px solid #e0e3e8; color: #909399;
  cursor: pointer; transition: all .2s; display: flex; align-items: center; justify-content: center;
}
.action-btn:hover { border-color: #6c5ce7; color: #6c5ce7; background: rgba(108,92,231,0.08); }
.action-btn.danger:hover { border-color: #e17055; color: #e17055; background: rgba(225,112,85,0.08); }

/* Pagination */
.pagination {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 16px; border-top: 1px solid #e0e3e8;
}
.pagination-info { font-size: 12px; color: #909399; }
</style>
