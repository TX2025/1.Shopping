<template>
  <div class="order-list-page">
    <div class="container">
      <h2>我的订单</h2>
      <el-table :data="orders" v-loading="loading">
        <el-table-column prop="order.orderNo" label="订单号" width="180" />
        <el-table-column label="商品" min-width="300">
          <template #default="{ row }">
            <span v-for="item in row.items" :key="item.id" style="margin-right:12px">
              {{ item.productName }} x{{ item.quantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="order.totalAmount" label="金额" width="100">
          <template #default="{row}">¥{{ row.order.totalAmount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="statusType(row.order.status)">{{ statusText(row.order.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="160">
          <template #default="{row}">{{ row.order.createTime }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{row}">
            <el-button size="small" @click="$router.push(`/user/orders/${row.order.id}`)">详情</el-button>
            <el-button v-if="row.order.status === 'PENDING'" size="small" type="danger" @click="handleCancel(row.order.id)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !orders.length" description="暂无订单" />
      <el-pagination v-if="total > 10" style="margin-top:20px;text-align:center" background layout="prev,pager,next"
        :total="total" :page-size="10" :current-page="page" @current-change="p=>{page=p;loadOrders()}" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOrders, cancelOrder } from '../../api/order'
import { ElMessage, ElMessageBox } from 'element-plus'

const orders = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)

onMounted(() => loadOrders())

async function loadOrders() {
  loading.value = true
  try {
    const res = await getOrders({ page: page.value, size: 10 })
    orders.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch {} finally { loading.value = false }
}

async function handleCancel(id) {
  try {
    await ElMessageBox.confirm('确定取消该订单？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await cancelOrder(id)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch {}
}

function statusText(s) {
  const map = { PENDING: '待支付', PAID: '已支付', SHIPPED: '已发货', CANCELLED: '已取消' }
  return map[s] || s
}
function statusType(s) {
  const map = { PENDING: 'warning', PAID: 'success', SHIPPED: 'primary', CANCELLED: 'info' }
  return map[s] || 'info'
}
</script>

<style scoped>
.container { max-width: 1000px; margin: 30px auto; padding: 0 20px; }
.container h2 { margin-bottom: 20px; }
</style>
