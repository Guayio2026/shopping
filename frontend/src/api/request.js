import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

function clearAuth() {
  localStorage.removeItem('token')
  try {
    useUserStore().logout()
  } catch {
    // pinia 可能尚未就绪
  }
}

function redirectToLogin() {
  const path = router.currentRoute.value.path || ''
  if (path === '/admin/login' || path === '/login' || path === '/register') return
  const loginPath = path.startsWith('/user') || path === '/login' || path === '/register' ? '/login' : '/admin/login'
  router.replace({ path: loginPath, query: { redirect: router.currentRoute.value.fullPath } })
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
    const result = response.data
    const silent = !!response.config?.silent
    if (result.code !== 200) {
      if (result.code === 401) {
        clearAuth()
        if (!response.config?.skipAuthRedirect) {
          redirectToLogin()
        }
      }
      if (!silent) {
        ElMessage.error(result.message || '请求失败')
      }
      return Promise.reject(new Error(result.message || '请求失败'))
    }
    return result.data
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络错误'
    const silent = !!error.config?.silent
    const skipAuthRedirect = !!error.config?.skipAuthRedirect

    if (status === 401) {
      clearAuth()
      if (!skipAuthRedirect) {
        redirectToLogin()
      }
    }

    if (!silent) {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

export default request
