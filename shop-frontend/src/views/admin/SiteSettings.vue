<template>
  <div class="site-settings">
    <h2>联系方式设置</h2>
    <el-card style="max-width:600px;margin-top:16px">
      <el-form :model="form" label-width="120px" v-loading="loading">
        <el-form-item label="网站名称"><el-input v-model="form.site_name" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.contact_phone" /></el-form-item>
        <el-form-item label="联系邮箱"><el-input v-model="form.contact_email" /></el-form-item>
        <el-form-item label="客服时间"><el-input v-model="form.contact_hours" /></el-form-item>
        <el-form-item label="Logo URL"><el-input v-model="form.site_logo" /></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save" :loading="saving">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getSiteConfigs, updateSiteConfigs } from '../../api/admin'
import { ElMessage } from 'element-plus'

const form = reactive({ site_name: '', contact_phone: '', contact_email: '', contact_hours: '', site_logo: '' })
const loading = ref(false)
const saving = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getSiteConfigs()
    if (res.data) {
      res.data.forEach(c => {
        if (c.configKey in form) form[c.configKey] = c.configValue || ''
      })
    }
  } catch {} finally { loading.value = false }
})

async function save() {
  saving.value = true
  try {
    const configs = Object.entries(form).map(([key, value]) => ({ key, value }))
    await updateSiteConfigs({ configs })
    ElMessage.success('保存成功')
  } catch {} finally { saving.value = false }
}
</script>
