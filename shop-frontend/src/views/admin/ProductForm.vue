<template>
  <div class="product-form">
    <h2>{{ isEdit ? '编辑产品' : '新增产品' }}</h2>
    <el-card style="max-width:700px;margin-top:16px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="产品名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述" prop="description"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="价格" prop="price"><el-input v-model="form.price" /></el-form-item>
        <el-form-item label="原价" prop="originalPrice"><el-input v-model="form.originalPrice" /></el-form-item>
        <el-form-item label="库存" prop="stock"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="选择分类">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图"><el-input v-model="form.coverImage" placeholder="图片URL" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" active-value="ON" inactive-value="OFF" /></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit" :loading="saving">保存</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAdminCategories, getAdminProducts, createProduct, updateProduct } from '../../api/admin'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id
const formRef = ref(null)
const saving = ref(false)
const categories = ref([])
const form = reactive({
  name: '', description: '', price: '', originalPrice: '', stock: 0, categoryId: null, coverImage: '', status: 'ON',
})
const rules = {
  name: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
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
      if (p) Object.assign(form, { name: p.name, description: p.description || '', price: p.price, originalPrice: p.originalPrice || '', stock: p.stock, categoryId: p.categoryId, coverImage: p.coverImage || '', status: p.status })
    } catch {}
  }
})

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const data = { ...form, price: parseFloat(form.price), originalPrice: parseFloat(form.originalPrice) || null }
    if (isEdit) await updateProduct(route.params.id, data)
    else await createProduct(data)
    ElMessage.success(isEdit ? '更新成功' : '创建成功')
    router.push('/admin/products')
  } catch {} finally { saving.value = false }
}
</script>
