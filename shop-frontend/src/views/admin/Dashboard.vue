<template>
  <div class="dashboard">
    <h2>仪表盘</h2>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card><div class="stat"><h3>{{ stats.products }}</h3><p>商品总数</p></div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card><div class="stat"><h3>{{ stats.orders }}</h3><p>订单总数</p></div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card><div class="stat"><h3>{{ stats.users }}</h3><p>用户总数</p></div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card><div class="stat"><h3>¥{{ stats.revenue }}</h3><p>总营收</p></div></el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminProducts, getAdminOrders, getAdminUsers } from '../../api/admin'

const stats = ref({ products: 0, orders: 0, users: 0, revenue: 0 })

onMounted(async () => {
  try {
    const [p, o, u] = await Promise.all([
      getAdminProducts({ page: 1, size: 1 }),
      getAdminOrders({ page: 1, size: 1 }),
      getAdminUsers({ page: 1, size: 1 }),
    ])
    stats.value.products = p.data?.total || 0
    stats.value.orders = o.data?.total || 0
    stats.value.users = u.data?.total || 0
    stats.value.revenue = (o.data?.list || []).reduce((s, o) => s + (o.order?.totalAmount || 0), 0)
  } catch {}
})
</script>

<style scoped>
.stat { text-align: center; padding: 10px; }
.stat h3 { font-size: 28px; color: #409EFF; }
.stat p { margin-top: 8px; color: #666; }
</style>
