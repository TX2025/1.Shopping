<template>
  <div class="product-manage">
    <div class="toolbar">
      <h2>产品管理</h2>
      <el-button type="primary" @click="$router.push('/admin/products/add')">新增产品</el-button>
    </div>
    <el-table :data="products" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="名称" min-width="180" />
      <el-table-column prop="price" label="价格" width="100"><template #default="{row}">¥{{ row.price }}</template></el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="sales" label="销量" width="80" />
      <el-table-column label="状态" width="80">
        <template #default="{row}"><el-tag :type="row.status==='ON'?'success':'info'">{{ row.status==='ON'?'上架':'下架' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{row}">
          <el-button size="small" @click="$router.push(`/admin/products/${row.id}`)">编辑</el-button>
          <el-button size="small" :type="row.status==='ON'?'warning':'success'"
            @click="toggleStatus(row)">{{ row.status==='ON'?'下架':'上架' }}</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top:16px;text-align:right" background layout="prev,pager,next"
      :total="total" :page-size="10" :current-page="page" @current-change="p=>{page=p;load()}" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminProducts, updateProductStatus, deleteProduct } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const products = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)

onMounted(() => load())

async function load() {
  loading.value = true
  try {
    const res = await getAdminProducts({ page: page.value, size: 10 })
    products.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch {} finally { loading.value = false }
}

async function toggleStatus(row) {
  try {
    const newStatus = row.status === 'ON' ? 'OFF' : 'ON'
    await updateProductStatus(row.id, newStatus)
    ElMessage.success('状态更新成功')
    load()
  } catch {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该产品？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    load()
  } catch {}
}
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
</style>
