<template>
  <div class="profile-page">
    <div class="container">
      <h2>个人信息</h2>
      <el-card>
        <el-form :model="form" label-width="80px">
          <el-form-item label="用户名"><el-input v-model="form.username" disabled /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
          <el-form-item label="角色"><el-tag>{{ form.role === 'ADMIN' ? '管理员' : '普通用户' }}</el-tag></el-form-item>
          <el-form-item label="等级">
            <span v-if="form.level" class="level-badge" :class="'lv-'+form.level">{{ form.level }}会员</span>
            <span v-else style="color:#909399">普通会员</span>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveProfile">保存</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <h2 style="margin-top:30px">收货地址</h2>
      <el-card>
        <div v-for="addr in addresses" :key="addr.id" class="address-item">
          <span>{{ addr.receiverName }} {{ addr.receiverPhone }} {{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}</span>
          <el-tag v-if="addr.isDefault" size="small" type="danger">默认</el-tag>
        </div>
        <el-empty v-if="!addresses.length" description="暂无地址" />
        <el-button style="margin-top:12px" @click="showAdd = true">+ 新增地址</el-button>
      </el-card>
    </div>

    <el-dialog v-model="showAdd" title="新增地址" width="500px">
      <el-form :model="newAddress" label-width="80px">
        <el-form-item label="收货人"><el-input v-model="newAddress.receiverName" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="newAddress.receiverPhone" /></el-form-item>
        <el-form-item label="省"><el-input v-model="newAddress.province" /></el-form-item>
        <el-form-item label="市"><el-input v-model="newAddress.city" /></el-form-item>
        <el-form-item label="区"><el-input v-model="newAddress.district" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="newAddress.detail" /></el-form-item>
        <el-form-item label="默认">
          <el-switch v-model="newAddress.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getProfile, updateProfile, getAddresses, addAddress } from '../../api/user'
import { ElMessage } from 'element-plus'

const form = reactive({ username: '', email: '', phone: '', role: '' })
const addresses = ref([])
const showAdd = ref(false)
const newAddress = reactive({ receiverName: '', receiverPhone: '', province: '', city: '', district: '', detail: '', isDefault: false })

onMounted(async () => {
  try {
    const res = await getProfile()
    Object.assign(form, res.data)
  } catch {}
  try {
    const res = await getAddresses()
    addresses.value = res.data || []
  } catch {}
})

async function saveProfile() {
  try {
    await updateProfile({ email: form.email, phone: form.phone })
    ElMessage.success('保存成功')
  } catch {}
}

async function saveAddress() {
  try {
    const res = await addAddress({ ...newAddress })
    addresses.value.push(res.data)
    showAdd.value = false
    Object.assign(newAddress, { receiverName: '', receiverPhone: '', province: '', city: '', district: '', detail: '', isDefault: false })
    ElMessage.success('添加成功')
  } catch {}
}
</script>

<style scoped>
.container { max-width: 700px; margin: 30px auto; padding: 0 20px; }
.level-badge { display:inline-block; padding:2px 10px; border-radius:10px; font-size:12px; font-weight:600 }
.lv-普通{background:#f5f5f5;color:#909399}.lv-银牌{background:rgba(162,155,254,.12);color:#6c5ce7}.lv-金牌{background:rgba(253,203,110,.15);color:#d68910}.lv-钻石{background:rgba(116,185,255,.15);color:#2980b9}
.address-item { display: flex; align-items: center; gap: 12px; padding: 8px 0; border-bottom: 1px solid #eee; }
</style>
