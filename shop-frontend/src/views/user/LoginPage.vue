<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2>登录</h2>
      <el-tabs v-model="loginMode" class="login-tabs">
        <!-- 密码登录 -->
        <el-tab-pane label="密码登录" name="pwd">
          <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-position="top">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="pwdForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="pwdForm.password" type="password" placeholder="请输入密码" show-password @keyup.enter="handlePwdLogin" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handlePwdLogin" :loading="loading" style="width:100%">登录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <!-- 短信登录 -->
        <el-tab-pane label="短信登录" name="sms">
          <el-form :model="smsForm" :rules="smsRules" ref="smsFormRef" label-position="top">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="smsForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="验证码" prop="code">
              <div style="display:flex; gap:8px; width:100%">
                <el-input v-model="smsForm.code" placeholder="请输入验证码" style="flex:1" @keyup.enter="handleSmsLogin" />
                <el-button :disabled="smsCountdown > 0" @click="sendSmsCode('LOGIN')" style="min-width:120px">
                  {{ smsCountdown > 0 ? smsCountdown + 's' : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSmsLogin" :loading="loading" style="width:100%">登录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <p class="link">还没有账号？<router-link to="/register">立即注册</router-link></p>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { login, sendCode, loginBySms } from '../../api/auth'
import { useAuthStore } from '../../stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

function redirectAfterLogin(role) {
  const redirect = route.query.redirect
  if (redirect) {
    router.push(redirect)
  } else {
    router.push(role === 'ADMIN' ? '/admin' : '/')
  }
}
const loginMode = ref('pwd')
const loading = ref(false)

// 密码登录
const pwdFormRef = ref(null)
const pwdForm = reactive({ username: '', password: '' })
const pwdRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}
async function handlePwdLogin() {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await login(pwdForm)
    const d = res.data
    auth.setAuth({ id: d.userId, username: d.username, role: d.role }, d.accessToken, d.refreshToken)
    ElMessage.success('登录成功')
    redirectAfterLogin(d.role)
  } catch {} finally { loading.value = false }
}

// 短信登录
const smsFormRef = ref(null)
const smsForm = reactive({ phone: '', code: '' })
const smsRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
}
const smsCountdown = ref(0)
let smsTimer = null

async function sendSmsCode(type) {
  if (!/^1[3-9]\d{9}$/.test(smsForm.phone)) {
    ElMessage.warning('请先输入正确的手机号')
    return
  }
  try {
    const res = await sendCode({ phone: smsForm.phone, type })
    ElMessage.success('验证码：' + res.data + '（有效期5分钟）')
    smsCountdown.value = 60
    smsTimer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0) {
        clearInterval(smsTimer)
        smsTimer = null
      }
    }, 1000)
  } catch {}
}

async function handleSmsLogin() {
  const valid = await smsFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await loginBySms({ phone: smsForm.phone, code: smsForm.code })
    const d = res.data
    auth.setAuth({ id: d.userId, username: d.username, role: d.role }, d.accessToken, d.refreshToken)
    ElMessage.success('登录成功')
    redirectAfterLogin(d.role)
  } catch {} finally { loading.value = false }
}
</script>

<style scoped>
.login-page { display: flex; justify-content: center; align-items: center; min-height: 80vh; }
.login-card { width: 420px; }
.login-card h2 { text-align: center; margin-bottom: 10px; }
.login-tabs :deep(.el-tabs__header) { margin-bottom: 0; }
.link { text-align: center; margin-top: 16px; color: #666; }
.link a { color: #409EFF; }
</style>
