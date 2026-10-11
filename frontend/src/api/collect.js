// 收藏 —— 前端接口封装
//
// 【这个文件的作用】把「后端有哪些收藏接口」集中写在一处，
// 页面只调用这里的方法，不直接在页面里写 URL。以后接口地址变了，只改这个文件。
//
// 【注意】这 5 个接口都在登录后面（不在 /public/** 白名单里），
// 所以不要加 silent: true —— 出错就该给用户看到提示。
// （silent 是给 goodsPublic.js 那种「游客页面」用的，避免弹一堆红字）
//
// 【对照后端】CollectController
//   本文件                       后端                            完整地址
//   toggleCollect()  ↔  POST   @PostMapping("/toggle")    →  POST   /api/collect/toggle
//   checkCollect()   ↔  GET    @GetMapping("/check")      →  GET    /api/collect/check?goodsId=8
//   getMyCollect()   ↔  GET    @GetMapping("/my")         →  GET    /api/collect/my
//   getCollectPage() ↔  GET    @GetMapping("/page")       →  GET    /api/collect/page?page=1&size=10
//   deleteCollect()  ↔  DELETE @DeleteMapping("/{id}")    →  DELETE /api/collect/8
//
// ⚠️ URL 里不要再写 /api —— request.js 的 baseURL 已经配了，写了会变成 /api/api/...

import request from './request'

/**
 * 收藏 / 取消收藏（一个按钮来回切）
 *
 * 【为什么不用「add + 抛 201」那一套】request.js 把 code !== 200 一律当错误弹红字，
 * 用非 200 传递正常业务状态会让用户看到不该出现的报错。
 * 所以后端改成 toggle：永远返回 200，用返回值表达「现在收藏了没有」。
 *
 * @param {number|string} goodsId 商品 id
 * @returns {Promise<boolean>} true = 现在「已收藏」；false = 现在「已取消」
 */
export function toggleCollect(goodsId) {
  return request.post('/collect/toggle', { goodsId })
}

/**
 * 当前用户是否收藏了该商品（详情页进来时问一次，决定按钮显示「收藏」还是「已收藏」）
 *
 * 【为什么单独一个接口】详情页的商品数据走 /public/goods/{id}（白名单接口），
 * 那个接口拿不到用户身份，算不出 isCollect。所以收藏状态要单独问这个要登录的接口。
 *
 * @param {number|string} goodsId 商品 id
 * @returns {Promise<boolean>} true / false
 */
export function checkCollect(goodsId) {
  return request.get('/collect/check', { params: { goodsId } })
}

/** 我的收藏列表（每条带一个 goods 对象：name / cover / price / descr…） */
export function getMyCollect() {
  return request.get('/collect/my')
}

/** 后台分页。params 形如 { page, size }，返回 { total, list } */
export function getCollectPage(params) {
  return request.get('/collect/page', { params })
}

/** 删除（后台删记录 / 前台「取消收藏」共用）。id 是这条收藏记录的 id，不是商品 id */
export function deleteCollect(id) {
  return request.delete(`/collect/${id}`)
}
