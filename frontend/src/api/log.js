import request from './request'

export function getLogPage(params) {
  return request.get('/admin/logs', { params })
}

export function deleteLog(id) {
  return request.delete(`/admin/logs/${id}`)
}
