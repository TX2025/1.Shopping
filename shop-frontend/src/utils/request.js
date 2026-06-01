import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

let isRefreshing = false
let refreshQueue = []

function processQueue(error, token) {
  refreshQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error)
    else resolve(token)
  })
  refreshQueue = []
}

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const data = response.data
    if (data.code !== 200) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message))
    }
    return data
  },
  async (error) => {
    const originalRequest = error.config
    const status = error.response?.status

    // 401 (token expired/invalid) → try refresh
    if (status === 401 && !originalRequest._retry) {
      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken) {
        originalRequest._retry = true
        if (!isRefreshing) {
          isRefreshing = true
          try {
            const res = await axios.post('/api/auth/refresh', { refreshToken })
            const d = res.data.data
            localStorage.setItem('token', d.accessToken)
            localStorage.setItem('refreshToken', d.refreshToken)
            localStorage.setItem('user', JSON.stringify({
              id: d.userId, username: d.username, role: d.role
            }))
            processQueue(null, d.accessToken)
            originalRequest.headers.Authorization = `Bearer ${d.accessToken}`
            return request(originalRequest)
          } catch (refreshError) {
            processQueue(refreshError, null)
            localStorage.removeItem('token')
            localStorage.removeItem('refreshToken')
            localStorage.removeItem('user')
            router.push('/login?redirect=' + encodeURIComponent(router.currentRoute.value.fullPath))
            ElMessage.error('登录已过期，请重新登录')
            return Promise.reject(refreshError)
          } finally {
            isRefreshing = false
          }
        } else {
          return new Promise((resolve, reject) => {
            refreshQueue.push({
              resolve: (token) => {
                originalRequest.headers.Authorization = `Bearer ${token}`
                resolve(request(originalRequest))
              },
              reject,
            })
          })
        }
      } else {
        router.push('/login?redirect=' + encodeURIComponent(router.currentRoute.value.fullPath))
        return Promise.reject(error)
      }
    }

    // 403 (authenticated endpoint without token) → redirect to login
    if (status === 403) {
      router.push('/login?redirect=' + encodeURIComponent(router.currentRoute.value.fullPath))
      ElMessage.warning('请先登录')
      return Promise.reject(error)
    }

    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
