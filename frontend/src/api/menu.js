import request from './request'

export function getMenuPage(params) {
  return request.get('/admin/menus', { params })
}

export function getAllMenus() {
  return request.get('/admin/menus/all').catch(() => request.get('/admin/menus/tree'))
}

export function getMenu(id) {
  return request.get(`/admin/menus/${id}`)
}

export function createMenu(data) {
  return request.post('/admin/menus', data)
}

export function updateMenu(id, data) {
  return request.put(`/admin/menus/${id}`, data)
}

export function deleteMenu(id) {
  return request.delete(`/admin/menus/${id}`)
}
