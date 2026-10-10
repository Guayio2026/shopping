// 商品分类 —— 前端接口封装
//
// 【这个文件的作用】
// 把"后端有哪些分类接口"集中写在一处，页面只调用这里的方法，
// 不直接在页面里写 URL。以后接口地址变了，只改这个文件。
//
// 【链路】Types.vue → 【本文件】 → request.js（自动加 /api 前缀和 token）→ 后端 TypeController
//
// 【对照后端】下面的 URL 和 backend 的 TypeController 一一对应：
//   本文件              后端 TypeController            完整地址
//   getTypePage   ↔  @GetMapping("/page")     →  GET  /api/type/page
//   getTypeList   ↔  @GetMapping("/list")     →  GET  /api/type/list
//   createType    ↔  @PostMapping             →  POST /api/type
//   updateType    ↔  @PutMapping("/{id}")     →  PUT  /api/type/{id}
//   deleteType    ↔  @DeleteMapping("/{id}")  →  DELETE /api/type/{id}

import request from './request'

/** 分页查询分类（分类管理页面列表用这个）。params 形如 { keyword, page, size } */
export function getTypePage(params) {
  return request.get('/type/page', { params })
}

/** 查询全部分类（不分页，下拉选择框之类的地方用） */
export function getTypeList() {
  return request.get('/type/list')
}

/** 新增分类。data 形如 { name: '图书', sortOrder: 0 } */
export function createType(data) {
  return request.post('/type', data)
}

/** 修改分类。id 是被改的那条记录的 id，data 是要改的内容 */
export function updateType(id, data) {
  return request.put(`/type/${id}`, data)
}

/** 删除分类 */
export function deleteType(id) {
  return request.delete(`/type/${id}`)
}
