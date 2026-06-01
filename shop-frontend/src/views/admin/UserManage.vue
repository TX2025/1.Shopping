<template>
  <div class="user-page">
    <div class="page-header">
      <h2>用户管理</h2>
      <div class="page-header-actions">
        <el-button plain><el-icon :size="14"><Download /></el-icon> 导出用户</el-button>
        <el-button type="primary"><el-icon :size="14"><Plus /></el-icon> 添加用户</el-button>
      </div>
    </div>

    <!-- Table -->
    <div class="card">
      <div class="card-body" style="padding:0">
        <el-table :data="users" stripe v-loading="loading" row-key="id" size="small">
          <el-table-column label="用户" min-width="160">
            <template #default="{row}">
              <div class="user-cell">
                <div class="user-avatar" :style="{ background: avatarColor(row.username) }">
                  {{ (row.username || '?').charAt(0).toUpperCase() }}
                </div>
                <div class="user-name">{{ row.username }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
          <el-table-column label="手机号" width="130">
            <template #default="{row}">{{ maskPhone(row.phone) }}</template>
          </el-table-column>
          <el-table-column label="注册时间" width="120">
            <template #default="{row}">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{row}">
              <span class="status-badge" :class="row.status==='ACTIVE'?'on':'off'">
                {{ row.status==='ACTIVE'?'正常':'已禁用' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90" fixed="right" align="center">
            <template #default="{row}">
              <div class="table-actions">
                <button class="action-btn" title="编辑"><el-icon :size="14"><EditPen /></el-icon></button>
                <button
                  v-if="row.role !== 'ADMIN'"
                  class="action-btn"
                  :class="row.status==='ACTIVE'?'danger':''"
                  :title="row.status==='ACTIVE'?'禁用':'启用'"
                  @click="toggleStatus(row)"
                  :style="row.status==='ACTIVE'?{borderColor:'#e17055',color:'#e17055'}:{}"
                >
                  <el-icon :size="14"><CircleClose v-if="row.status==='ACTIVE'" /><CircleCheck v-else /></el-icon>
                </button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && users.length===0" description="暂无用户" :image-size="64" style="padding:40px 0" />
      </div>
      <div class="pagination" v-if="total>0">
        <span class="pagination-info">显示 {{ (page-1)*size+1 }}-{{ Math.min(page*size, total) }} 共 {{ total }} 条</span>
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="sizes, prev, pager, next"
          @size-change="onSizeChange"
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
import { getAdminUsers, updateUserStatus } from '../../api/admin'
import { ElMessage } from 'element-plus'
import { Download, Plus, EditPen, CircleClose, CircleCheck } from '@element-plus/icons-vue'

const users = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const avatarColors = ['#6c5ce7','#00b894','#e17055','#fdcb6e','#74b9ff','#e67e22','#a29bfe','#fd79a8']

onMounted(() => load())

function onSizeChange(s) { size.value = s; page.value = 1; load() }

async function load() {
  loading.value = true
  try {
    const res = await getAdminUsers({ page: page.value, size: size.value })
    users.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch {} finally { loading.value = false }
}

async function toggleStatus(row) {
  const newStatus = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  try {
    await updateUserStatus(row.id, newStatus)
    ElMessage.success(newStatus === 'ACTIVE' ? '已启用' : '已禁用')
    load()
  } catch {}
}

function maskPhone(phone) {
  if (!phone) return '-'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

function avatarColor(name) {
  if (!name) return avatarColors[0]
  return avatarColors[name.charCodeAt(0) % avatarColors.length]
}

function formatTime(t) {
  if (!t) return '-'
  return t.substring(0, 10)
}
</script>

<style scoped>
/* ===== Layout ===== */
.user-page { max-width: 100%; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 20px; font-weight: 600; margin: 0; }
.page-header-actions { display: flex; gap: 8px; }

/* ===== Card / Table ===== */
.card { background: #fff; border-radius: 12px; border: 1px solid #e0e3e8; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.card-body { padding: 20px; }

/* User cell */
.user-cell { display: flex; align-items: center; gap: 10px; }
.user-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 12px; font-weight: 600; flex-shrink: 0;
}
.user-name { font-weight: 500; font-size: 13px; }

/* Status Badge */
.status-badge {
  display: inline-block; padding: 2px 8px; border-radius: 20px;
  font-size: 11px; font-weight: 500; white-space: nowrap; line-height: 1.6;
}
.status-badge.on { background: rgba(0,184,148,0.1); color: #00b894; }
.status-badge.off { background: rgba(225,112,85,0.1); color: #e17055; }

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
