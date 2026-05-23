<template>
  <div class="product-manage">
    <div class="toolbar">
      <h2>产品管理</h2>
      <el-button type="primary" @click="$router.push('/admin/products/add')">新增产品</el-button>
    </div>
    <el-table :data="products" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="封面" width="100">
        <template #default="{row}">
          <div class="cover-cell">
            <video v-if="isVideo(coverMedia(row))" :src="coverMedia(row)" muted class="cover-thumb" />
            <img v-else-if="coverMedia(row)" :src="coverMedia(row)" class="cover-thumb" />
            <span v-else class="cover-placeholder"><el-icon :size="24"><PictureFilled /></el-icon></span>
            <span class="cover-badge" v-if="vidCount(row)"><el-icon :size="10"><VideoCameraFilled /></el-icon></span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column prop="price" label="价格" width="100"><template #default="{row}">¥{{ row.price }}</template></el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="sales" label="销量" width="80" />
      <el-table-column label="媒体" width="110">
        <template #default="{row}">
          <div class="media-cell">
            <span v-if="imgCount(row)" class="media-badge"><el-icon :size="14"><PictureFilled /></el-icon> {{ imgCount(row) }}</span>
            <span v-if="vidCount(row)" class="media-badge video"><el-icon :size="14"><VideoCameraFilled /></el-icon> {{ vidCount(row) }}</span>
            <span v-if="!imgCount(row) && !vidCount(row)" style="color:#ccc;font-size:13px">-</span>
          </div>
        </template>
      </el-table-column>
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
import { PictureFilled, VideoCameraFilled } from '@element-plus/icons-vue'

const products = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)

onMounted(() => load())

function parseJsonField(val) {
  if (!val) return []
  if (Array.isArray(val)) return val
  try { return JSON.parse(val) } catch { return [] }
}

function isVideo(url) { return url && /\.mp4$/i.test(url) }
function imgCount(row) {
  let count = parseJsonField(row.images).length
  if (row.coverImage && !isVideo(row.coverImage)) count++
  return count
}

function vidCount(row) { return parseJsonField(row.videos).length }

function coverMedia(row) {
  const videos = parseJsonField(row.videos)
  if (videos.length > 0) return videos[0]
  const imgs = parseJsonField(row.images)
  if (imgs.length > 0) return imgs[0]
  return row.coverImage || null
}

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

.cover-cell { position: relative; width: 64px; height: 64px; margin: 0 auto; }
.cover-thumb { width: 64px; height: 64px; object-fit: cover; border-radius: 6px; background: #f5f5f5; }
.cover-placeholder {
  width: 64px; height: 64px; display: flex; align-items: center; justify-content: center;
  background: #f5f5f5; border-radius: 6px; color: #ccc;
}
.cover-badge {
  position: absolute; top: 2px; right: 2px; background: rgba(0,0,0,0.55); color: #fff;
  border-radius: 3px; padding: 1px 4px; display: flex; align-items: center; font-size: 10px;
  font-weight: 600; line-height: 1;
}
.media-cell { display: flex; gap: 10px; align-items: center; }
.media-badge {
  display: inline-flex; align-items: center; gap: 3px; font-size: 13px;
  color: #409EFF; font-weight: 600;
}
.media-badge.video { color: #E6A23C; }
</style>
