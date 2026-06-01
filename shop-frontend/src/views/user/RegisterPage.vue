<template>
  <div class="register-page">
    <el-card class="register-card">
      <h2>注册</h2>
      <el-tabs v-model="regMode" class="reg-tabs">
        <!-- 用户名注册 -->
        <el-tab-pane label="用户名注册" name="user">
          <el-form :model="userForm" :rules="userRules" ref="userFormRef" label-position="top">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="userForm.username" placeholder="3-20位字符" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="userForm.password" type="password" placeholder="6-20位字符" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="password2">
              <el-input v-model="userForm.password2" type="password" placeholder="请再次输入密码" show-password />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="userForm.email" placeholder="选填" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="userForm.phone" placeholder="选填" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUserReg" :loading="loading" style="width:100%">注册</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <!-- 手机号注册 -->
        <el-tab-pane label="手机号注册" name="phone">
          <el-form :model="phoneForm" :rules="phoneRules" ref="phoneFormRef" label-position="top">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="phoneForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="验证码" prop="code">
              <div style="display:flex; gap:8px; width:100%">
                <el-input v-model="phoneForm.code" placeholder="请输入验证码" style="flex:1" />
                <el-button :disabled="smsCountdown > 0" @click="sendRegCode" style="min-width:120px">
                  {{ smsCountdown > 0 ? smsCountdown + 's' : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="设置密码" prop="password">
              <el-input v-model="phoneForm.password" type="password" placeholder="6-20位字符" show-password />
              <div class="pwd-bar">
                <div class="pwd-bar-inner" :class="pwdStrength.class" :style="{ width: pwdStrength.width }"></div>
              </div>
              <span class="pwd-tip">{{ pwdStrength.text }}</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handlePhoneReg" :loading="loading" style="width:100%">注册</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <p class="link">已有账号？<router-link to="/login">去登录</router-link></p>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { register, sendCode, registerBySms } from '../../api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const regMode = ref('user')
const loading = ref(false)

// 用户名注册
const userFormRef = ref(null)
const userForm = reactive({ username: '', password: '', password2: '', email: '', phone: '' })
const validatePass2 = (_rule, value, callback) => {
  if (value !== userForm.password) callback(new Error('两次密码不一致'))
  else callback()
}
const userRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 3, max: 20, message: '长度3-20位', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '至少6位', trigger: 'blur' }],
  password2: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: validatePass2, trigger: 'blur' }],
}
async function handleUserReg() {
  const valid = await userFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await register({ username: userForm.username, password: userForm.password, email: userForm.email, phone: userForm.phone })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch {} finally { loading.value = false }
}

// 手机号注册
const phoneFormRef = ref(null)
const phoneForm = reactive({ phone: '', code: '', password: '' })
const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '至少6位', trigger: 'blur' }],
}
const smsCountdown = ref(0)
let smsTimer = null

const pwdStrength = computed(() => {
  const v = phoneForm.password || ''
  let score = 0
  if (v.length >= 6) score++
  if (v.length >= 10) score++
  if (/[a-zA-Z]/.test(v) && /[0-9]/.test(v)) score++
  if (/[^a-zA-Z0-9]/.test(v)) score++
  const map = ['', '弱', '中', '强']
  const cls = ['', 'weak', 'medium', 'strong']
  return { text: map[score], class: cls[score], width: (score / 4 * 100) + '%' }
})

async function sendRegCode() {
  if (!/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    ElMessage.warning('请先输入正确的手机号')
    return
  }
  try {
    const res = await sendCode({ phone: phoneForm.phone, type: 'REGISTER' })
    ElMessage.success('验证码：' + res.data + '（有效期5分钟）')
    smsCountdown.value = 60
    smsTimer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0) {
        clearInterval(smsTimer)
        smsTimer = null
      }
    }, 1000)
  } catch { /* error handled by interceptor */ }
}

async function handlePhoneReg() {
  const valid = await phoneFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await registerBySms({ phone: phoneForm.phone, code: phoneForm.code, password: phoneForm.password })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch {} finally { loading.value = false }
}
</script>

<style scoped>
.register-page { display: flex; justify-content: center; align-items: center; min-height: 80vh; }
.register-card { width: 420px; }
.register-card h2 { text-align: center; margin-bottom: 10px; }
.reg-tabs :deep(.el-tabs__header) { margin-bottom: 0; }
.link { text-align: center; margin-top: 16px; color: #666; }
.link a { color: #409EFF; }
.pwd-bar { height: 4px; background: #eee; border-radius: 2px; margin-top: 6px; }
.pwd-bar-inner { height: 100%; border-radius: 2px; transition: width 0.3s; }
.pwd-bar-inner.weak { background: #E6A23C; }
.pwd-bar-inner.medium { background: #409EFF; }
.pwd-bar-inner.strong { background: #67C23A; }
.pwd-tip { font-size: 12px; color: #999; }
</style>
