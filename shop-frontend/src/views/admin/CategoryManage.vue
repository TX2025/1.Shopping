<template>
  <div class="category-manage">
    <div class="toolbar">
      <h2>分类管理</h2>
      <el-button type="primary" @click="openAdd()">新增分类</el-button>
    </div>
    <el-table :data="categories" v-loading="loading" border style="margin-top:16px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="名称" width="150" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="上级分类" width="150">
        <template #default="{row}">{{ parentName(row.parentId) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="上级分类">
          <el-select v-model="form.parentId" placeholder="无（顶级分类）" clearable>
            <el-option v-for="c in rootCategories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getAdminCategories, createCategory, updateCategory, deleteCategory } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const categories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const form = ref({ name: '', sortOrder: 0, parentId: null })

const rootCategories = computed(() => categories.value.filter(c => !c.parentId))

onMounted(() => load())

async function load() {
  loading.value = true
  try {
    const res = await getAdminCategories()
    if (res.data) {
      const flat = []
      function walk(items) { items.forEach(i => { flat.push(i); if (i.children) walk(i.children) }) }
      walk(res.data)
      categories.value = flat
    }
  } catch {} finally { loading.value = false }
}

function parentName(id) {
  if (!id) return '-'
  const p = categories.value.find(c => c.id === id)
  return p ? p.name : '-'
}

function openAdd() {
  isEdit.value = false; editingId.value = null
  form.value = { name: '', sortOrder: 0, parentId: null }
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true; editingId.value = row.id
  form.value = { name: row.name, sortOrder: row.sortOrder || 0, parentId: row.parentId }
  dialogVisible.value = true
}

async function save() {
  try {
    if (isEdit.value) await updateCategory(editingId.value, form.value)
    else await createCategory(form.value)
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    load()
  } catch {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    load()
  } catch {}
}
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; }
</style>
