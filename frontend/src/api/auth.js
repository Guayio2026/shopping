import request from './request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function getCurrentUser() {
  // 路由守卫内校验 token 时自行处理跳转，避免 401 拦截器按「当前页」误跳到用户端登录
  return request.get('/auth/me', { skipAuthRedirect: true, silent: true })
}

export function updateProfile(data) {
  return request.put('/auth/profile', data)
}

export function changePassword(data) {
  return request.post('/auth/password', data)
}
// 账户充值：POST /api/auth/recharge，返回充值后的最新资料（含新余额）
export function recharge(data) {
  return request.post('/auth/recharge', data)
}

