<template>
  <div class="order-manage">
    <h2>订单管理</h2>
    <el-table :data="orders" v-loading="loading" border style="margin-top:16px">
      <el-table-column prop="order.orderNo" label="订单号" width="180" />
      <el-table-column label="商品" min-width="250">
        <template #default="{row}">
          <span v-for="item in row.items" :key="item.id" style="margin-right:8px">{{ item.productName }} x{{ item.quantity }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="order.totalAmount" label="金额" width="100"><template #default="{row}">¥{{ row.order.totalAmount }}</template></el-table-column>
      <el-table-column prop="order.receiverName" label="收货人" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="statusType(row.order.status)">{{ statusText(row.order.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <template v-if="row.order.status === 'PENDING'">
            <el-button size="small" type="success" @click="updateStatus(row.order.id, 'PAID')">标记支付</el-button>
          </template>
          <template v-if="row.order.status === 'PAID'">
            <el-button size="small" type="primary" @click="updateStatus(row.order.id, 'SHIPPED')">发货</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminOrders, updateOrderStatus } from '../../api/admin'
import { ElMessage } from 'element-plus'

const orders = ref([])
const loading = ref(false)

onMounted(() => load())

async function load() {
  loading.value = true
  try {
    const res = await getAdminOrders({ page: 1, size: 100 })
    orders.value = res.data?.list || []
  } catch {} finally { loading.value = false }
}

async function updateStatus(id, status) {
  try {
    await updateOrderStatus(id, status)
    ElMessage.success('状态更新成功')
    load()
  } catch {}
}

function statusText(s) { const m = { PENDING: '待支付', PAID: '已支付', SHIPPED: '已发货', CANCELLED: '已取消' }; return m[s] || s }
function statusType(s) { const m = { PENDING: 'warning', PAID: 'success', SHIPPED: 'primary', CANCELLED: 'info' }; return m[s] || 'info' }
</script>
