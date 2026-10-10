import request from './request'

export function getSystemConfig() {
  return request.get('/public/system-config', { silent: true })
}

export function updateSystemConfig(data) {
  return request.put('/admin/system-config', data)
}
