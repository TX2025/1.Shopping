<template>
  <div class="service-page">
    <div class="page-header">
      <h2>售后退换货服务</h2>
      <div class="page-header-actions">
        <el-button plain><el-icon :size="14"><Download /></el-icon> 导出记录</el-button>
        <el-button type="primary" @click="openAdd"><el-icon :size="14"><Plus /></el-icon> 新增售后单</el-button>
      </div>
    </div>

    <!-- Stats -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <div class="stat-card"><div class="stat-value" style="color:#E6A23C">{{ stats.pending||0 }}</div><div class="stat-label">待审核</div></div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card"><div class="stat-value" style="color:#409EFF">{{ stats.processing||0 }}</div><div class="stat-label">处理中</div></div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card"><div class="stat-value" style="color:#67C23A">{{ stats.completed||0 }}</div><div class="stat-label">已完成</div></div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card"><div class="stat-value" style="color:#F56C6C">{{ stats.rejected||0 }}</div><div class="stat-label">已拒绝</div></div>
      </el-col>
    </el-row>

    <!-- Table -->
    <div class="card">
      <div class="card-body" style="padding:0">
        <el-table :data="list" stripe v-loading="loading" row-key="id" size="small">
          <el-table-column label="售后单号" min-width="140">
            <template #default="{row}"><span class="order-id">{{ row.afterSaleNo }}</span></template>
          </el-table-column>
          <el-table-column label="关联订单" min-width="140">
            <template #default="{row}">{{ row.orderNo }}</template>
          </el-table-column>
          <el-table-column prop="customer" label="客户" width="80" />
          <el-table-column label="类型" width="105">
            <template #default="{row}">
              <span class="status-badge" :style="typeStyle(row.type)">{{ typeLabel(row.type) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="原因" min-width="120" show-overflow-tooltip />
          <el-table-column label="金额" width="100">
            <template #default="{row}">{{ row.amount > 0 ? '¥'+Number(row.amount).toFixed(2) : '¥0.00' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="95">
            <template #default="{row}">
              <span class="status-badge" :class="statusClass(row.status)">{{ statusLabel(row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{row}">
              <div class="table-actions">
                <el-tooltip v-if="row.status==='PENDING'" content="审核通过" placement="top">
                  <el-button class="btn-icon" @click="approve(row)"><el-icon :size="14"><Check /></el-icon></el-button>
                </el-tooltip>
                <el-tooltip v-if="row.status==='PENDING'" content="拒绝" placement="top">
                  <el-button class="btn-icon danger" @click="reject(row)"><el-icon :size="14"><Close /></el-icon></el-button>
                </el-tooltip>
                <el-tooltip content="编辑" placement="top">
                  <el-button class="btn-icon" @click="openEdit(row)"><el-icon :size="14"><EditPen /></el-icon></el-button>
                </el-tooltip>
                <el-tooltip content="删除" placement="top">
                  <el-button class="btn-icon danger" @click="handleDelete(row)"><el-icon :size="14"><Delete /></el-icon></el-button>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && list.length===0" description="暂无售后记录" :image-size="64" style="padding:40px 0" />
      </div>
      <div class="pagination" v-if="total>0">
        <span class="pagination-info">显示 {{ (page-1)*size+1 }}-{{ Math.min(page*size, total) }} 共 {{ total }} 条</span>
        <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="prev, pager, next" @current-change="load" background small />
      </div>
    </div>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="editId?'编辑售后单':'新增售后单'" width="520px" destroy-on-close>
      <el-form :model="form" label-width="80px" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="售后单号"><el-input v-model="form.afterSaleNo" placeholder="自动生成" :disabled="!!editId" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="关联订单号"><el-input v-model="form.orderNo" placeholder="如 #ORD-2024-001" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="客户"><el-input v-model="form.customer" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="金额"><el-input-number v-model="form.amount" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="类型">
            <el-select v-model="form.type" style="width:100%">
              <el-option v-for="o in typeOptions" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态">
            <el-select v-model="form.status" style="width:100%">
              <el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="原因"><el-input v-model="form.reason" type="textarea" :rows="2" placeholder="如 质量问题 / 尺寸不合 / 不想要了" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="save" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAfterSales, createAfterSale, updateAfterSale, deleteAfterSale, getAfterSaleStats } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, Check, Close, EditPen, Delete } from '@element-plus/icons-vue'

const list = ref([])
const loading = ref(false)
const stats = ref({})
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const editId = ref(null)
const saving = ref(false)
const form = ref({ afterSaleNo: '', orderNo: '', customer: '', type: 'RETURN_REFUND', reason: '', amount: 0, status: 'PENDING' })

const typeOptions = [
  { value: 'RETURN_REFUND', label: '退货退款' },
  { value: 'EXCHANGE', label: '换货' },
  { value: 'REFUND_ONLY', label: '仅退款' },
]
const statusOptions = [
  { value: 'PENDING', label: '待审核' },
  { value: 'PROCESSING', label: '处理中' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'REJECTED', label: '已拒绝' },
]

const typeMap = { RETURN_REFUND: '退货退款', EXCHANGE: '换货', REFUND_ONLY: '仅退款' }
const statusMap = { PENDING: '待审核', PROCESSING: '处理中', COMPLETED: '已完成', REJECTED: '已拒绝' }
function typeLabel(v) { return typeMap[v] || v }
function statusLabel(v) { return statusMap[v] || v }

function typeStyle(v) {
  const s = { RETURN_REFUND: { background: 'rgba(108,92,231,0.1)', color: '#6c5ce7' }, EXCHANGE: { background: 'rgba(253,203,110,0.15)', color: '#e67e22' }, REFUND_ONLY: { background: 'rgba(225,112,85,0.1)', color: '#e17055' } }
  return s[v] || {}
}
function statusClass(v) {
  return { PENDING: 'draft', PROCESSING: 'on', COMPLETED: 'on', REJECTED: 'off' }[v] || ''
}

onMounted(() => { load(); loadStats() })

async function load() {
  loading.value = true
  try {
    const res = await getAfterSales({ page: page.value, size: size.value })
    list.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch {} finally { loading.value = false }
}

async function loadStats() {
  try { const res = await getAfterSaleStats(); if (res.data) stats.value = res.data } catch {}
}

function resetForm() {
  editId.value = null
  form.value = { afterSaleNo: '', orderNo: '', customer: '', type: 'RETURN_REFUND', reason: '', amount: 0, status: 'PENDING' }
}
function openAdd() { resetForm(); dialogVisible.value = true }
function openEdit(row) {
  editId.value = row.id
  form.value = { ...row, amount: Number(row.amount) || 0 }
  dialogVisible.value = true
}

async function save() {
  if (!form.value.afterSaleNo.trim()) { ElMessage.warning('请输入售后单号'); return }
  saving.value = true
  try {
    if (editId.value) { await updateAfterSale(editId.value, form.value); ElMessage.success('更新成功') }
    else { await createAfterSale(form.value); ElMessage.success('创建成功') }
    dialogVisible.value = false
    load(); loadStats()
  } catch {} finally { saving.value = false }
}

async function approve(row) {
  try { await updateAfterSale(row.id, { ...row, status: 'PROCESSING' }); row.status = 'PROCESSING'; ElMessage.success('已审核通过'); loadStats() } catch {}
}
async function reject(row) {
  try { await updateAfterSale(row.id, { ...row, status: 'REJECTED' }); row.status = 'REJECTED'; ElMessage.warning('已拒绝'); loadStats() } catch {}
}
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除售后单「${row.afterSaleNo}」？`, '删除', { type: 'warning', confirmButtonText: '确定' })
    await deleteAfterSale(row.id); ElMessage.success('已删除'); load(); loadStats()
  } catch {}
}
</script>

<style scoped>
/* ===== Layout ===== */
.service-page { max-width: 100%; }
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

/* ===== Card ===== */
.card { background: #fff; border-radius: 12px; border: 1px solid #e0e3e8; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.card-body { padding: 20px; }

/* ===== Table ===== */
.order-id { color: #6c5ce7; font-weight: 500; }

/* Status Badge — compact to avoid wrapping */
.status-badge {
  display: inline-block; padding: 2px 8px; border-radius: 20px;
  font-size: 11px; font-weight: 500; white-space: nowrap; line-height: 1.6;
}
.status-badge.on { background: rgba(0,184,148,0.1); color: #00b894; }
.status-badge.off { background: rgba(225,112,85,0.1); color: #e17055; }
.status-badge.draft { background: rgba(253,203,110,0.15); color: #e67e22; }

/* Action buttons */
.table-actions { display: flex; gap: 4px; justify-content: center; }
.btn-icon {
  width: 30px; height: 30px; padding: 0; border-radius: 6px;
  background: transparent; border: 1px solid #e0e3e8; color: #909399;
  cursor: pointer; transition: all .2s; display: flex; align-items: center; justify-content: center;
}
.btn-icon:hover { border-color: #6c5ce7; color: #6c5ce7; background: rgba(108,92,231,0.08); }
.btn-icon.danger:hover { border-color: #e17055; color: #e17055; background: rgba(225,112,85,0.08); }

/* Pagination */
.pagination {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 16px; border-top: 1px solid #e0e3e8;
}
.pagination-info { font-size: 12px; color: #909399; }
</style>
