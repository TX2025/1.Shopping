<template>
  <div class="site-settings">
    <h2>站点设置</h2>
    <el-card style="margin-top:16px">
      <el-tabs v-model="activeTab" v-loading="loading">
        <el-tab-pane label="基本信息" name="general">
          <el-form :model="form" label-width="120px">
            <el-form-item label="网站名称"><el-input v-model="form.site_name" /></el-form-item>
            <el-form-item label="Logo URL"><el-input v-model="form.site_logo" /></el-form-item>
            <el-form-item label="联系电话"><el-input v-model="form.contact_phone" /></el-form-item>
            <el-form-item label="联系邮箱"><el-input v-model="form.contact_email" /></el-form-item>
            <el-form-item label="客服时间"><el-input v-model="form.contact_hours" /></el-form-item>
            <el-form-item label="页面头部电话"><el-input v-model="form.header_phone" /></el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="促销栏" name="promo">
          <el-form label-width="120px">
            <el-form-item label="启用">
              <el-switch v-model="form.promo_bar_enabled" active-value="true" inactive-value="false" />
            </el-form-item>
            <el-form-item label="消息列表 (JSON)">
              <el-input v-model="form.promo_bar_messages" type="textarea" :rows="6" placeholder="JSON数组" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="首页轮播" name="hero">
          <el-form label-width="120px">
            <el-form-item label="启用">
              <el-switch v-model="form.hero_enabled" active-value="true" inactive-value="false" />
            </el-form-item>
            <el-form-item label="轮播间隔(ms)"><el-input v-model="form.hero_interval" /></el-form-item>
            <el-form-item label="幻灯片 (JSON)">
              <el-input v-model="form.hero_slides" type="textarea" :rows="8" placeholder="JSON数组，每项: {image, link, title, sort}" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="热门推荐" name="popular">
          <el-form label-width="120px">
            <el-form-item label="区块标题"><el-input v-model="form.popular_title" /></el-form-item>
            <el-form-item label="展示数量"><el-input v-model="form.popular_count" /></el-form-item>
            <el-form-item label="排序方式">
              <el-select v-model="form.popular_sort">
                <el-option label="销量优先" value="sales" />
                <el-option label="最新" value="newest" />
                <el-option label="价格从低到高" value="price_asc" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="信任区" name="trust">
          <el-form label-width="120px">
            <el-form-item label="启用">
              <el-switch v-model="form.trust_enabled" active-value="true" inactive-value="false" />
            </el-form-item>
            <el-form-item label="区块标题"><el-input v-model="form.trust_title" /></el-form-item>
            <el-form-item label="特性列表 (JSON)">
              <el-input v-model="form.trust_features" type="textarea" :rows="8" placeholder="JSON数组，每项: {icon, title, description}" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="Footer" name="footer">
          <el-form label-width="140px">
            <el-form-item label="关于文字"><el-input v-model="form.footer_about" type="textarea" :rows="2" /></el-form-item>
            <el-form-item label="订阅引导语"><el-input v-model="form.footer_subscribe_text" /></el-form-item>
            <el-form-item label="栏目列 (JSON)">
              <el-input v-model="form.footer_columns" type="textarea" :rows="8" placeholder="JSON数组，每项: {title, links: [{label, url}]}" />
            </el-form-item>
            <el-form-item label="社交图标 (JSON)">
              <el-input v-model="form.footer_social" type="textarea" :rows="4" placeholder="JSON数组，每项: {platform, url, icon}" />
            </el-form-item>
            <el-form-item label="支付图标 (JSON)">
              <el-input v-model="form.footer_payment_icons" type="textarea" :rows="4" placeholder="JSON数组，每项: {name, icon}" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div style="margin-top:20px;border-top:1px solid #ebeef5;padding-top:16px">
        <el-button type="primary" @click="save" :loading="saving">保存全部设置</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getSiteConfigs, updateSiteConfigs } from '../../api/admin'
import { ElMessage } from 'element-plus'

const activeTab = ref('general')
const loading = ref(false)
const saving = ref(false)
const form = reactive({})

onMounted(async () => {
  loading.value = true
  try {
    const res = await getSiteConfigs()
    if (res.data) {
      Object.entries(res.data).forEach(([key, value]) => {
        form[key] = value || ''
      })
    }
  } catch {} finally { loading.value = false }
})

async function save() {
  saving.value = true
  try {
    await updateSiteConfigs({ ...form })
    ElMessage.success('保存成功')
  } catch {} finally { saving.value = false }
}
</script>
