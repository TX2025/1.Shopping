<template>
  <div class="category-manage">
    <div class="toolbar">
      <h2>分类管理</h2>
      <el-button type="primary" @click="openAdd()">新增分类</el-button>
    </div>
    <el-table :data="categories" v-loading="loading" border style="margin-top:16px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="封面" width="70">
        <template #default="{row}">
          <div class="cat-thumb-cell">
            <video v-if="firstVideo(row)" :src="firstVideo(row)" muted class="cat-thumb" />
            <img v-else-if="row.coverImage" :src="row.coverImage" class="cat-thumb" />
            <span v-else style="color:#ccc;font-size:18px">-</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" width="140" />
      <el-table-column prop="sortOrder" label="排序" width="70" />
      <el-table-column label="上级分类" width="130">
        <template #default="{row}">{{ parentName(row.parentId) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{row}">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="650px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="上级分类">
          <el-select v-model="form.parentId" placeholder="无（顶级分类）" clearable>
            <el-option v-for="c in rootCategories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图">
          <div style="display:flex;align-items:center;gap:12px">
            <div class="cat-cover-preview" @click="triggerCoverUpload">
              <video v-if="firstVideo(form)" :src="firstVideo(form)" muted class="cat-cover-media" />
              <img v-else-if="form.coverImage" :src="form.coverImage" class="cat-cover-media" />
              <div v-else class="cat-cover-empty"><el-icon :size="24"><Plus /></el-icon><span>上传</span></div>
            </div>
            <input type="file" ref="coverInput" accept="image/jpeg,image/png,image/gif,video/mp4" style="display:none" @change="handleCoverUpload" />
            <div>
              <el-button size="small" @click="triggerCoverUpload">选择文件</el-button>
              <el-input v-model="form.coverImage" placeholder="或输入URL" size="small" style="width:200px;margin-top:4px" />
            </div>
          </div>
        </el-form-item>
        <el-form-item label="视频URL">
          <el-input v-model="videoUrl" placeholder="输入视频URL（如 /uploads/xxx.mp4）" size="small" />
          <el-button size="small" style="margin-top:4px" @click="addVideoUrl">添加视频</el-button>
          <div style="margin-top:6px">
            <el-tag v-for="(v, i) in videoList" :key="i" closable @close="videoList.splice(i,1)" style="margin:2px" size="small">{{ v }}</el-tag>
          </div>
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
import { getAdminCategories, createCategory, updateCategory, deleteCategory, uploadFile } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const categories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const form = ref({ name: '', sortOrder: 0, parentId: null, coverImage: '', videos: '' })
const coverInput = ref(null)
const videoUrl = ref('')
const videoList = ref([])

const rootCategories = computed(() => categories.value.filter(c => !c.parentId))

function parseVideos(val) {
  if (!val) return []
  try { return typeof val === 'string' ? JSON.parse(val) : val } catch { return [] }
}

function firstVideo(row) {
  const v = parseVideos(row.videos)
  return v.length > 0 ? v[0] : null
}

function isVideo(url) { return url && /\.mp4$/i.test(url) }

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
  form.value = { name: '', sortOrder: 0, parentId: null, coverImage: '', videos: '' }
  videoList.value = []
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true; editingId.value = row.id
  form.value = { name: row.name, sortOrder: row.sortOrder || 0, parentId: row.parentId, coverImage: row.coverImage || '', videos: row.videos || '' }
  videoList.value = parseVideos(row.videos)
  dialogVisible.value = true
}

function triggerCoverUpload() { coverInput.value?.click() }

async function handleCoverUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return
  try {
    const res = await uploadFile(file)
    if (res.data?.url) form.value.coverImage = res.data.url
    ElMessage.success('上传成功')
  } catch { ElMessage.error('上传失败') }
  e.target.value = ''
}

function addVideoUrl() {
  if (videoUrl.value && !videoList.value.includes(videoUrl.value)) {
    videoList.value.push(videoUrl.value)
    videoUrl.value = ''
  }
}

async function save() {
  form.value.videos = videoList.value.length > 0 ? JSON.stringify(videoList.value) : ''
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
.cat-thumb-cell { width: 40px; height: 40px; margin: 0 auto; }
.cat-thumb { width: 40px; height: 40px; object-fit: cover; border-radius: 4px; }
.cat-cover-preview {
  width: 100px; height: 100px; background: #f0f2f5; border-radius: 6px;
  overflow: hidden; cursor: pointer; display: flex; align-items: center; justify-content: center;
  border: 2px dashed #d9d9d9; flex-shrink: 0;
}
.cat-cover-preview:hover { border-color: #409EFF; }
.cat-cover-media { width: 100%; height: 100%; object-fit: cover; }
.cat-cover-empty { text-align: center; color: #bbb; }
.cat-cover-empty span { display: block; font-size: 11px; }
</style>
