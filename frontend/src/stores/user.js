import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { getCurrentUser, login as loginApi, register as registerApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || '')

  const isLoggedIn = computed(() => !!token.value)
  const displayName = computed(() => user.value?.nickname || user.value?.username || '同学')
  const isAdmin = computed(() => user.value?.accountType === 'ADMIN' || user.value?.role === 'ADMIN')

  async function login(form) {
    const data = await loginApi(form)
    applySession(data)
    return data
  }

  async function register(form) {
    const data = await registerApi(form)
    applySession(data)
    return data
  }

  function applySession(data) {
    token.value = data.token
    localStorage.setItem('token', data.token)
    user.value = {
      id: data.userId ?? data.id,
      username: data.username,
      nickname: data.nickname,
      role: data.role,
      avatar: data.avatar,
      accountType: data.accountType || (data.role === 'ADMIN' ? 'ADMIN' : 'USER')
    }
  }

  async function fetchUser() {
    const profile = await getCurrentUser()
    user.value = {
      ...profile,
      accountType: profile.accountType || (profile.role === 'ADMIN' ? 'ADMIN' : 'USER')
    }
    return user.value
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
  }

  return {
    user,
    token,
    isLoggedIn,
    displayName,
    isAdmin,
    login,
    register,
    fetchUser,
    logout
  }
})
