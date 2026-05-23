<template>
  <div class="product-form">
    <h2>{{ isEdit ? '编辑产品' : '新增产品' }}</h2>
    <el-card style="max-width:800px;margin-top:16px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="110px">
        <el-form-item label="产品名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述" prop="description"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="价格" prop="price"><el-input v-model="form.price" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="原价"><el-input v-model="form.originalPrice" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="库存" prop="stock"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="选择分类">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="封面图">
          <div class="upload-wrap">
            <el-upload
              :action="uploadAction"
              :headers="uploadHeaders"
              :show-file-list="false"
              :before-upload="beforeUploadImage"
              :on-success="onCoverUpload"
              accept=".jpg,.jpeg,.png,.gif"
            >
              <img v-if="form.coverImage" :src="form.coverImage" class="cover-preview" />
              <el-button v-else><el-icon><Plus /></el-icon> 上传封面图</el-button>
            </el-upload>
            <el-button v-if="form.coverImage" size="small" type="danger" plain @click="form.coverImage = ''" style="margin-left:10px">移除</el-button>
          </div>
        </el-form-item>

        <el-form-item label="产品图片">
          <el-upload
            :action="uploadAction"
            :headers="uploadHeaders"
            list-type="picture-card"
            :file-list="imageFileList"
            :before-upload="beforeUploadImage"
            :on-success="onImageUpload"
            :on-remove="onImageRemove"
            accept=".jpg,.jpeg,.png,.gif"
          >
            <el-icon :size="28"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="产品视频">
          <el-upload
            :action="uploadAction"
            :headers="uploadHeaders"
            :file-list="videoFileList"
            :before-upload="beforeUploadVideo"
            :on-success="onVideoUpload"
            :on-remove="onVideoRemove"
            accept=".mp4"
            drag
          >
            <el-icon :size="32"><UploadFilled /></el-icon>
            <div style="margin-top:8px">拖拽或点击上传 MP4 视频</div>
          </el-upload>
          <div v-if="videoPreviewList.length" style="margin-top:8px;display:flex;gap:10px;flex-wrap:wrap">
            <div v-for="(v, i) in videoPreviewList" :key="i" style="position:relative">
              <video :src="v" controls muted style="width:160px;height:90px;object-fit:cover;border-radius:6px" />
            </div>
          </div>
        </el-form-item>

        <el-form-item label="状态">
          <el-switch v-model="form.status" active-value="ON" inactive-value="OFF" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit" :loading="saving">保存</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAdminCategories, getAdminProducts, createProduct, updateProduct } from '../../api/admin'
import { useAuthStore } from '../../stores/auth'
import { ElMessage } from 'element-plus'
import { Plus, UploadFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id
const formRef = ref(null)
const saving = ref(false)
const categories = ref([])
const form = reactive({
  name: '', description: '', price: '', originalPrice: '', stock: 0, categoryId: null,
  coverImage: '', status: 'ON', images: [], videos: [],
})
const rules = {
  name: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
}

const authStore = useAuthStore()
const uploadAction = '/api/admin/upload'
const uploadHeaders = computed(() => ({
  Authorization: authStore.token ? `Bearer ${authStore.token}` : ''
}))

const imageFileList = computed(() =>
  (form.images || []).map((url, i) => ({ uid: i, name: `image-${i}`, url, status: 'success' }))
)
const videoFileList = computed(() =>
  (form.videos || []).map((url, i) => ({ uid: i, name: `video-${i}`, url, status: 'success' }))
)
const videoPreviewList = computed(() => form.videos || [])

function beforeUploadImage(file) {
  const ext = file.name.split('.').pop().toLowerCase()
  if (!['jpg', 'jpeg', 'png', 'gif'].includes(ext)) {
    ElMessage.error('仅支持 jpg/jpeg/png/gif 格式图片')
    return false
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

function beforeUploadVideo(file) {
  const ext = file.name.split('.').pop().toLowerCase()
  if (ext !== 'mp4') {
    ElMessage.error('仅支持 mp4 格式视频')
    return false
  }
  if (file.size > 50 * 1024 * 1024) {
    ElMessage.error('视频大小不能超过 50MB')
    return false
  }
  return true
}

function onCoverUpload(res) {
  if (res.url) form.coverImage = res.url
}

function onImageUpload(res, file) {
  if (res.url) {
    if (!form.images) form.images = []
    form.images.push(res.url)
  }
}

function onImageRemove(file) {
  const idx = form.images.indexOf(file.url)
  if (idx > -1) form.images.splice(idx, 1)
}

function onVideoUpload(res, file) {
  if (res.url) {
    if (!form.videos) form.videos = []
    form.videos.push(res.url)
  }
}

function onVideoRemove(file) {
  const idx = form.videos.indexOf(file.url)
  if (idx > -1) form.videos.splice(idx, 1)
}

onMounted(async () => {
  try {
    const res = await getAdminCategories()
    if (res.data) {
      const flat = []
      function walk(items) { items.forEach(i => { flat.push(i); if (i.children) walk(i.children) }) }
      walk(res.data)
      categories.value = flat
    }
  } catch {}
  if (isEdit) {
    try {
      const res = await getAdminProducts({ page: 1, size: 100 })
      const p = (res.data?.list || []).find(p => p.id == route.params.id)
      if (p) {
        Object.assign(form, {
          name: p.name, description: p.description || '',
          price: p.price, originalPrice: p.originalPrice || '',
          stock: p.stock, categoryId: p.categoryId,
          coverImage: p.coverImage || '', status: p.status,
          images: parseJsonField(p.images),
          videos: parseJsonField(p.videos),
        })
      }
    } catch {}
  }
})

function parseJsonField(val) {
  if (!val) return []
  if (Array.isArray(val)) return val
  try { return JSON.parse(val) } catch { return [] }
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const data = {
      ...form,
      price: parseFloat(form.price),
      originalPrice: parseFloat(form.originalPrice) || null,
      images: form.images?.length ? JSON.stringify(form.images) : null,
      videos: form.videos?.length ? JSON.stringify(form.videos) : null,
    }
    if (isEdit) await updateProduct(route.params.id, data)
    else await createProduct(data)
    ElMessage.success(isEdit ? '更新成功' : '创建成功')
    router.push('/admin/products')
  } catch {} finally { saving.value = false }
}
</script>

<style scoped>
.upload-wrap { display: flex; align-items: center; }
.cover-preview {
  width: 120px; height: 120px; object-fit: cover; border-radius: 8px; border: 1px solid #e8e8e8;
}
</style>
