<template>
  <div class="marketing-settings">
    <h2>营销设置</h2>
    <el-card style="max-width:600px;margin-top:16px" v-loading="loading">
      <el-form label-width="140px">
        <el-form-item label="促销弹窗">
          <el-switch v-model="form.marketing_popup_enabled" active-value="true" inactive-value="false" />
        </el-form-item>
        <el-form-item label="弹窗标题"><el-input v-model="popupConfig.title" /></el-form-item>
        <el-form-item label="弹窗内容"><el-input v-model="popupConfig.content" type="textarea" :rows="3" /></el-form-item>
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

const form = reactive({ marketing_popup_enabled: 'false' })
const popupConfig = reactive({ title: '新用户优惠', content: '注册即享首单9折优惠！' })
const loading = ref(false)
const saving = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getSiteConfigs()
    if (res.data) {
      res.data.forEach(c => {
        if (c.configKey === 'marketing_popup_enabled') form.marketing_popup_enabled = c.configValue || 'false'
        if (c.configKey === 'marketing_popup_content') {
          try { Object.assign(popupConfig, JSON.parse(c.configValue)) } catch {}
        }
      })
    }
  } catch {} finally { loading.value = false }
})

async function save() {
  saving.value = true
  try {
    await updateSiteConfigs({
      configs: [
        { key: 'marketing_popup_enabled', value: form.marketing_popup_enabled },
        { key: 'marketing_popup_content', value: JSON.stringify(popupConfig) },
      ]
    })
    ElMessage.success('保存成功')
  } catch {} finally { saving.value = false }
}
</script>
