<template>
  <div class="service-page">
    <div class="page-header">
      <h2>询盘管理</h2>
      <div class="page-header-actions">
        <el-button plain><el-icon :size="14"><Download /></el-icon> 导出询盘</el-button>
        <el-button type="primary" @click="openAdd"><el-icon :size="14"><Plus /></el-icon> 新增询盘</el-button>
      </div>
    </div>

    <!-- Stats -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <div class="stat-card"><div class="stat-value" style="color:#E6A23C">{{ stats.pending||0 }}</div><div class="stat-label">待回复</div></div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card"><div class="stat-value" style="color:#409EFF">{{ stats.replied||0 }}</div><div class="stat-label">已回复</div></div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card"><div class="stat-value" style="color:#67C23A">{{ stats.converted||0 }}</div><div class="stat-label">已转化</div></div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card"><div class="stat-value" style="color:#6c5ce7">{{ stats.conversionRate||0 }}%</div><div class="stat-label">转化率</div></div>
      </el-col>
    </el-row>

    <!-- Table -->
    <div class="card">
      <div class="card-body" style="padding:0">
        <el-table :data="list" stripe v-loading="loading" row-key="id" size="small">
          <el-table-column label="询盘编号" min-width="130">
            <template #default="{row}"><span class="order-id">{{ row.inquiryNo }}</span></template>
          </el-table-column>
          <el-table-column prop="customer" label="客户" min-width="110" />
          <el-table-column prop="source" label="来源" width="90" />
          <el-table-column prop="interest" label="产品兴趣" min-width="130" show-overflow-tooltip />
          <el-table-column prop="summary" label="内容摘要" min-width="180" show-overflow-tooltip />
          <el-table-column label="时间" width="160">
            <template #default="{row}">{{ row.createTime?.substring(0,16).replace('T',' ') }}</template>
          </el-table-column>
          <el-table-column label="状态" width="95">
            <template #default="{row}">
              <span class="status-badge" :class="statusClass(row.status)">{{ statusLabel(row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="110" fixed="right">
            <template #default="{row}">
              <div class="table-actions">
                <el-tooltip v-if="row.status==='PENDING'" content="回复" placement="top">
                  <el-button class="btn-icon" @click="reply(row)"><el-icon :size="14"><ChatDotRound /></el-icon></el-button>
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
        <el-empty v-if="!loading && list.length===0" description="暂无询盘记录" :image-size="64" style="padding:40px 0" />
      </div>
      <div class="pagination" v-if="total>0">
        <span class="pagination-info">显示 {{ (page-1)*size+1 }}-{{ Math.min(page*size, total) }} 共 {{ total }} 条</span>
        <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="prev, pager, next" @current-change="load" background small />
      </div>
    </div>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="editId?'编辑询盘':'新增询盘'" width="520px" destroy-on-close>
      <el-form :model="form" label-width="80px" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="询盘编号"><el-input v-model="form.inquiryNo" placeholder="自动生成" :disabled="!!editId" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="来源">
            <el-select v-model="form.source" style="width:100%">
              <el-option v-for="o in sourceOptions" :key="o" :label="o" :value="o" />
            </el-select>
          </el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="客户"><el-input v-model="form.customer" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态">
            <el-select v-model="form.status" style="width:100%">
              <el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="产品兴趣"><el-input v-model="form.interest" placeholder="客户感兴趣的产品" /></el-form-item>
        <el-form-item label="内容摘要"><el-input v-model="form.summary" type="textarea" :rows="3" placeholder="询盘内容摘要..." /></el-form-item>
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
import { getInquiries, createInquiry, updateInquiry, deleteInquiry, getInquiryStats } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, ChatDotRound, EditPen, Delete } from '@element-plus/icons-vue'

const list = ref([])
const loading = ref(false)
const stats = ref({})
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const editId = ref(null)
const saving = ref(false)
const form = ref({ inquiryNo: '', customer: '', source: '官网', interest: '', summary: '', status: 'PENDING' })

const sourceOptions = ['官网', 'WhatsApp', '邮件', '电话', '展会', '其他']
const statusOptions = [
  { value: 'PENDING', label: '待回复' },
  { value: 'REPLIED', label: '已回复' },
  { value: 'CONVERTED', label: '已转化' },
]

const statusMap = { PENDING: '待回复', REPLIED: '已回复', CONVERTED: '已转化' }
function statusLabel(v) { return statusMap[v] || v }
function statusClass(v) {
  return { PENDING: 'draft', REPLIED: 'on', CONVERTED: 'on' }[v] || ''
}

onMounted(() => { load(); loadStats() })

async function load() {
  loading.value = true
  try {
    const res = await getInquiries({ page: page.value, size: size.value })
    list.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch {} finally { loading.value = false }
}

async function loadStats() {
  try { const res = await getInquiryStats(); if (res.data) stats.value = res.data } catch {}
}

function resetForm() {
  editId.value = null
  form.value = { inquiryNo: '', customer: '', source: '官网', interest: '', summary: '', status: 'PENDING' }
}
function openAdd() { resetForm(); dialogVisible.value = true }
function openEdit(row) {
  editId.value = row.id
  form.value = { ...row }
  dialogVisible.value = true
}

async function save() {
  if (!form.value.inquiryNo.trim()) { ElMessage.warning('请输入询盘编号'); return }
  saving.value = true
  try {
    if (editId.value) { await updateInquiry(editId.value, form.value); ElMessage.success('更新成功') }
    else { await createInquiry(form.value); ElMessage.success('创建成功') }
    dialogVisible.value = false
    load(); loadStats()
  } catch {} finally { saving.value = false }
}

async function reply(row) {
  try { await updateInquiry(row.id, { ...row, status: 'REPLIED' }); row.status = 'REPLIED'; ElMessage.success('已标记为已回复'); loadStats() } catch {}
}
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除询盘「${row.inquiryNo}」？`, '删除', { type: 'warning', confirmButtonText: '确定' })
    await deleteInquiry(row.id); ElMessage.success('已删除'); load(); loadStats()
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
