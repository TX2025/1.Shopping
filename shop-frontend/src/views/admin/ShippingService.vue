<template>
  <div class="service-page">
    <div class="page-header">
      <h2>配送服务</h2>
      <el-button type="primary" @click="openAdd"><el-icon :size="14"><Plus /></el-icon> 添加配送方式</el-button>
    </div>

    <el-card shadow="never">
      <el-table :data="list" stripe v-loading="loading" :header-cell-style="{background:'#fafafa',color:'#606266',fontWeight:600,fontSize:'13px'}">
        <el-table-column label="配送方式" min-width="140">
          <template #default="{row}"><el-icon :size="16" color="#6c5ce7"><Van /></el-icon> <span style="font-weight:500">{{ row.name }}</span></template>
        </el-table-column>
        <el-table-column prop="region" label="配送区域" min-width="120" />
        <el-table-column label="运费" width="100"><template #default="{row}">¥{{ Number(row.fee).toFixed(2) }}</template></el-table-column>
        <el-table-column label="免费门槛" width="140">
          <template #default="{row}">{{ row.freeThreshold ? '满¥'+Number(row.freeThreshold).toFixed(2)+'免运费' : '无' }}</template>
        </el-table-column>
        <el-table-column prop="estimate" label="预计时效" width="140" />
        <el-table-column label="状态" width="90">
          <template #default="{row}">
            <el-switch :model-value="row.status==='ON'" @change="val=>toggleStatus(row,val)" active-color="#6c5ce7" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link size="small" style="color:#e17055" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && list.length===0" description="暂无配送方式" :image-size="64" style="padding:40px 0" />
    </el-card>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="editId?'编辑配送方式':'添加配送方式'" width="500px" destroy-on-close>
      <el-form :model="form" label-width="100px" label-position="top">
        <el-row :gutter="16">
          <el-col :span="14"><el-form-item label="配送方式名称"><el-input v-model="form.name" placeholder="如 标准快递" /></el-form-item></el-col>
          <el-col :span="10"><el-form-item label="状态">
            <el-switch v-model="form.status" active-value="ON" inactive-value="OFF" inline-prompt active-text="启用" inactive-text="停用" />
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="配送区域"><el-input v-model="form.region" placeholder="如 全国 / 一二线城市 / 海外" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="运费 (¥)"><el-input-number v-model="form.fee" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="免费门槛 (¥)"><el-input-number v-model="form.freeThreshold" :min="0" :precision="2" style="width:100%" placeholder="无" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="预计时效"><el-input v-model="form.estimate" placeholder="如 3-5个工作日" /></el-form-item>
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
import { getShippingMethods, createShippingMethod, updateShippingMethod, deleteShippingMethod, updateShippingStatus } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Van } from '@element-plus/icons-vue'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editId = ref(null)
const saving = ref(false)
const form = ref({ name: '', region: '', fee: 0, freeThreshold: null, estimate: '', status: 'ON' })

onMounted(() => load())

async function load() {
  loading.value = true
  try {
    const res = await getShippingMethods()
    list.value = res.data || []
  } catch {} finally { loading.value = false }
}

function resetForm() {
  editId.value = null
  form.value = { name: '', region: '', fee: 0, freeThreshold: null, estimate: '', status: 'ON' }
}

function openAdd() { resetForm(); dialogVisible.value = true }
function openEdit(row) {
  editId.value = row.id
  form.value = { ...row }
  dialogVisible.value = true
}

async function save() {
  if (!form.value.name.trim()) { ElMessage.warning('请输入配送方式名称'); return }
  saving.value = true
  try {
    if (editId.value) {
      await updateShippingMethod(editId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createShippingMethod(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    load()
  } catch {} finally { saving.value = false }
}

async function toggleStatus(row, val) {
  const st = val ? 'ON' : 'OFF'
  try {
    await updateShippingStatus(row.id, st)
    row.status = st
    ElMessage.success(val ? '已启用' : '已停用')
  } catch {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.name}」？`, '删除', { type: 'warning', confirmButtonText: '确定' })
    await deleteShippingMethod(row.id)
    ElMessage.success('已删除')
    load()
  } catch {}
}
</script>

<style scoped>
.service-page { max-width: 100%; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 20px; font-weight: 600; margin: 0; }
</style>
