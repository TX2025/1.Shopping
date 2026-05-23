<template>
  <div class="user-manage">
    <h2>用户管理</h2>
    <el-table :data="users" v-loading="loading" border style="margin-top:16px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="email" label="邮箱" width="180" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column label="角色" width="80">
        <template #default="{row}"><el-tag :type="row.role==='ADMIN'?'danger':'info'">{{ row.role==='ADMIN'?'管理员':'用户' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{row}"><el-tag :type="row.status==='ACTIVE'?'success':'danger'">{{ row.status==='ACTIVE'?'正常':'禁用' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="160" />
      <el-table-column label="操作" width="120">
        <template #default="{row}">
          <el-button v-if="row.role !== 'ADMIN'" size="small"
            :type="row.status==='ACTIVE'?'danger':'success'"
            @click="toggleStatus(row)">{{ row.status==='ACTIVE'?'禁用':'启用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminUsers, updateUserStatus } from '../../api/admin'
import { ElMessage } from 'element-plus'

const users = ref([])
const loading = ref(false)

onMounted(() => load())

async function load() {
  loading.value = true
  try {
    const res = await getAdminUsers({ page: 1, size: 100 })
    users.value = res.data?.list || []
  } catch {} finally { loading.value = false }
}

async function toggleStatus(row) {
  const newStatus = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  try {
    await updateUserStatus(row.id, newStatus)
    ElMessage.success('状态更新成功')
    load()
  } catch {}
}
</script>
