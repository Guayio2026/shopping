// 商品 —— 前端接口封装
//
// 【这个文件的作用】
// 把"后端有哪些商品接口"集中写在一处，页面只调用这里的方法，
// 不直接在页面里写 URL。以后接口地址变了，只改这个文件。
//
// 【链路】Goods.vue → 【本文件】 → request.js（自动加 /api 前缀和 token）→ 后端 GoodsController
//
// 【对照后端】下面的 URL 和 backend 的 GoodsController 一一对应：
//   本文件               后端 GoodsController                 完整地址
//   getGoodsPage   ↔  @GetMapping("/page")      →  GET  /api/goods/page
//   getGoodsList   ↔  @GetMapping("/list")      →  GET  /api/goods/list
//   getGoodsById   ↔  @GetMapping("/{id}")      →  GET  /api/goods/{id}
//   createGoods    ↔  @PostMapping              →  POST /api/goods
//   updateGoods    ↔  @PutMapping("/{id}")      →  PUT  /api/goods/{id}
//   deleteGoods    ↔  @DeleteMapping("/{id}")   →  DELETE /api/goods/{id}

import request from './request'

/**
 * 分页查询商品
 *
 * params 形如 { keyword, typeId, page, size }
 *   keyword —— 商品名关键字（可选，不传就不按名字筛）
 *   typeId  —— 分类 id（可选，不传就不按分类筛）
 *   page / size —— 第几页 / 每页几条
 */
export function getGoodsPage(params) {
  return request.get('/goods/page', { params })
}

/** 查询全部商品（不分页，下拉选择框之类的地方用） */
export function getGoodsList() {
  return request.get('/goods/list')
}

/** 根据 id 查一条（编辑前回查详情用） */
export function getGoodsById(id) {
  return request.get(`/goods/${id}`)
}

/** 新增商品。data 形如 { name: '手机', price: 5999, store: 100, typeId: 10 } */
export function createGoods(data) {
  return request.post('/goods', data)
}

/** 修改商品。id 是被改的那条记录的 id，data 是要改的内容 */
export function updateGoods(id, data) {
  return request.put(`/goods/${id}`, data)
}

/** 删除商品 */
export function deleteGoods(id) {
  return request.delete(`/goods/${id}`)
}

/**
 * 商品销量统计（后台首页 ECharts 饼图用）
 *
 * 返回：[{ name: '商品名', value: 128 }, ...]，按销量倒序前 10 条
 * 对应后端 GoodsController.echarts()，完整地址 GET /api/goods/echarts
 */
export function getGoodsSalesStat() {
  return request.get('/goods/echarts')
}
