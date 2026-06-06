<template>
  <div class="ct-page">
    <div class="page-header">
      <div>
        <h2><el-icon :size="20" color="#6c5ce7" style="margin-right:8px"><Phone /></el-icon>联系方式</h2>
        <div class="page-subtitle">管理商城联系信息与社交媒体账号</div>
      </div>
      <div class="page-header-actions">
        <button class="btn btn-primary" @click="saveAll" :disabled="saving"><el-icon :size="14"><Check /></el-icon> {{ saving?'保存中...':'保存设置' }}</button>
      </div>
    </div>

    <!-- Row 1: Basic Contact + Social -->
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;margin-bottom:16px">
      <div class="card">
        <div class="card-header"><h3><el-icon :size="16" color="#6c5ce7" style="margin-right:8px"><OfficeBuilding /></el-icon>基本联系方式</h3></div>
        <div class="card-body">
          <div class="form-group"><label class="form-label">公司名称</label><input class="form-input" v-model="form.site_name" /></div>
          <div class="form-row"><div class="form-group"><label class="form-label">联系电话</label><input class="form-input" v-model="form.contact_phone" /></div><div class="form-group"><label class="form-label">传真号码</label><input class="form-input" v-model="form.fax" placeholder="选填" /></div></div>
          <div class="form-row"><div class="form-group"><label class="form-label">邮箱地址</label><input class="form-input" v-model="form.contact_email" /></div><div class="form-group"><label class="form-label">备用邮箱</label><input class="form-input" v-model="form.backup_email" placeholder="选填" /></div></div>
          <div class="form-group"><label class="form-label">公司地址</label><input class="form-input" v-model="form.company_address" /></div>
          <div class="form-row"><div class="form-group"><label class="form-label">营业时间</label><input class="form-input" v-model="form.contact_hours" /></div><div class="form-group"><label class="form-label">客服热线</label><input class="form-input" v-model="form.service_hotline" /></div></div>
        </div>
      </div>
      <div class="card">
        <div class="card-header"><h3><el-icon :size="16" color="#00b894" style="margin-right:8px"><Share /></el-icon>社交媒体</h3></div>
        <div class="card-body">
          <div class="form-group"><label class="form-label"><span class="social-icon" style="color:#07c160">微</span>微信公众号</label><input class="form-input" v-model="form.wechat_oa" /></div>
          <div class="form-group"><label class="form-label"><span class="social-icon" style="color:#e6162d">微</span>微博账号</label><input class="form-input" v-model="form.weibo" /></div>
          <div class="form-row"><div class="form-group"><label class="form-label"><span class="social-icon">抖</span>抖音号</label><input class="form-input" v-model="form.tiktok" /></div><div class="form-group"><label class="form-label"><span class="social-icon" style="color:#ff2442">红</span>小红书</label><input class="form-input" v-model="form.xiaohongshu" /></div></div>
          <div class="form-row"><div class="form-group"><label class="form-label"><span class="social-icon" style="color:#25d366">W</span>WhatsApp</label><input class="form-input" v-model="form.whatsapp" placeholder="+86..." /></div><div class="form-group"><label class="form-label"><span class="social-icon" style="color:#1877f2">f</span>Facebook</label><input class="form-input" v-model="form.facebook" placeholder="选填" /></div></div>
          <div class="form-row"><div class="form-group"><label class="form-label"><span class="social-icon" style="color:#e4405f">I</span>Instagram</label><input class="form-input" v-model="form.instagram" placeholder="选填" /></div><div class="form-group"><label class="form-label"><span class="social-icon" style="color:#ff0000">Y</span>YouTube</label><input class="form-input" v-model="form.youtube" placeholder="选填" /></div></div>
        </div>
      </div>
    </div>

    <!-- Row 2: Store + Map + Service -->
    <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:16px">
      <div class="card">
        <div class="card-header"><h3><el-icon :size="16" color="#e17055" style="margin-right:8px"><Location /></el-icon>门店信息</h3></div>
        <div class="card-body">
          <div class="form-group"><label class="form-label">门店名称</label><input class="form-input" v-model="form.store_name" /></div>
          <div class="form-group"><label class="form-label">门店地址</label><input class="form-input" v-model="form.store_address" /></div>
          <div class="form-group"><label class="form-label">联系电话</label><input class="form-input" v-model="form.store_phone" /></div>
        </div>
      </div>
      <div class="card">
        <div class="card-header"><h3><el-icon :size="16" color="#fdcb6e" style="margin-right:8px"><MapLocation /></el-icon>地图坐标</h3></div>
        <div class="card-body">
          <div class="form-row"><div class="form-group"><label class="form-label">纬度</label><input class="form-input" v-model="form.map_lat" /></div><div class="form-group"><label class="form-label">经度</label><input class="form-input" v-model="form.map_lng" /></div></div>
          <div class="form-group"><label class="form-label">地图缩放级别</label><input class="form-input" type="number" v-model="form.map_zoom" /></div>
        </div>
      </div>
      <div class="card">
        <div class="card-header"><h3><el-icon :size="16" color="#74b9ff" style="margin-right:8px"><Headset /></el-icon>在线客服</h3></div>
        <div class="card-body">
          <div class="form-group"><label class="form-label">客服系统</label>
            <select class="form-select" v-model="form.cs_system">
              <option value="">请选择</option>
              <option>美洽</option><option>智齿客服</option><option>网易七鱼</option>
            </select>
          </div>
          <div class="form-group"><label class="form-label">App ID</label><input class="form-input" v-model="form.cs_appid" placeholder="输入客服系统 App ID" /></div>
          <div class="form-group" style="display:flex;align-items:center;gap:10px;margin-top:8px">
            <el-switch v-model="form.cs_enabled" active-value="true" inactive-value="false" />
            <span style="font-size:13px">启用在线客服浮窗</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSiteConfigs, updateSiteConfigs } from '../../api/admin'
import { ElMessage } from 'element-plus'
import { Check, Phone, OfficeBuilding, Share, Location, MapLocation, Headset } from '@element-plus/icons-vue'

const loading=ref(false);const saving=ref(false)
const form=ref({
  site_name:'',contact_phone:'',contact_email:'',contact_hours:'',
  fax:'',backup_email:'',company_address:'',service_hotline:'',
  wechat_oa:'',weibo:'',tiktok:'',xiaohongshu:'',whatsapp:'',facebook:'',instagram:'',youtube:'',
  store_name:'',store_address:'',store_phone:'',
  map_lat:'',map_lng:'',map_zoom:'15',
  cs_system:'',cs_appid:'',cs_enabled:'true',
})

onMounted(()=>load())

async function load(){
  loading.value=true
  try{const res=await getSiteConfigs();if(res.data){const m={};res.data.forEach(c=>{m[c.configKey]=c.configValue});form.value={...form.value,...m}}}
  catch{}finally{loading.value=false}
}

async function saveAll(){
  saving.value=true
  try{await updateSiteConfigs(form.value);ElMessage.success('保存成功');load()}
  catch{}finally{saving.value=false}
}
</script>

<style scoped>
.ct-page{max-width:100%}
.page-header{display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:20px}
.page-header h2{font-size:20px;font-weight:600;margin:0;display:flex;align-items:center}
.page-subtitle{font-size:12px;color:#909399;margin-top:3px}
.page-header-actions{display:flex;gap:8px}
.btn{padding:8px 16px;border:none;border-radius:8px;cursor:pointer;font-size:13px;font-weight:500;display:inline-flex;align-items:center;gap:6px;transition:all .2s;font-family:inherit}
.btn-primary{background:#6c5ce7;color:#fff}.btn-primary:hover{background:#5b4cdb;cursor:pointer}.btn-primary:disabled{opacity:.6;cursor:not-allowed}
.card{background:#fff;border-radius:12px;border:1px solid #e0e3e8;box-shadow:0 1px 4px rgba(0,0,0,.04)}
.card-header{padding:14px 16px;border-bottom:1px solid #e0e3e8;display:flex;align-items:center}
.card-header h3{font-size:14px;font-weight:600;margin:0;display:flex;align-items:center}
.card-body{padding:18px}

.form-group{margin-bottom:14px}
.form-group:last-child{margin-bottom:0}
.form-label{display:block;font-size:13px;font-weight:500;margin-bottom:5px;color:#303133}
.form-input,.form-select{width:100%;padding:8px 12px;border:1px solid #e0e3e8;border-radius:8px;font-size:13px;outline:none;background:#fff;font-family:inherit;transition:all .2s;box-sizing:border-box}
.form-input:focus,.form-select:focus{border-color:#6c5ce7;box-shadow:0 0 0 3px rgba(108,92,231,.1)}
.form-input::placeholder{color:#c0c4cc}
.form-row{display:grid;grid-template-columns:1fr 1fr;gap:12px}
.social-icon{display:inline-flex;align-items:center;justify-content:center;width:18px;height:18px;border-radius:4px;font-size:11px;font-weight:700;margin-right:4px;background:#f0f0f0;vertical-align:middle;flex-shrink:0}
</style>
