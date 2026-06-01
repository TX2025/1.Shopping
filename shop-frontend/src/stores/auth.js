import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const token = ref(localStorage.getItem('token') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const username = computed(() => user.value?.username || '')

  function setAuth(userData, accessToken, refreshTokenVal) {
    user.value = userData
    token.value = accessToken
    refreshToken.value = refreshTokenVal
    localStorage.setItem('user', JSON.stringify(userData))
    localStorage.setItem('token', accessToken)
    localStorage.setItem('refreshToken', refreshTokenVal)
  }

  function logout() {
    user.value = null
    token.value = ''
    refreshToken.value = ''
    localStorage.removeItem('user')
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
  }

  return { user, token, refreshToken, isLoggedIn, isAdmin, username, setAuth, logout }
})
