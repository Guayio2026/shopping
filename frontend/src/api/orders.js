// 订单 —— 前端接口封装
//
// 【这个文件的作用】把「后端有哪些订单接口」集中写在一处，
// 页面只调用这里的方法，不直接在页面里写 URL。以后接口地址变了，只改这个文件。
//
// 【对照后端】OrdersController
//   本文件                       后端                              完整地址
//   createOrder()    ↔  POST   @PostMapping            →  POST   /api/orders
//   getMyOrders()    ↔  GET    @GetMapping("/my")      →  GET    /api/orders/my?page=1&size=10
//   getOrderPage()   ↔  GET    @GetMapping("/page")    →  GET    /api/orders/page?page=1&size=10
//   deleteOrder()    ↔  DELETE @DeleteMapping("/{id}") →  DELETE /api/orders/8
//
// ⚠️ URL 里不要再写 /api —— request.js 的 baseURL 已经配了，写了会变成 /api/api/...
//
// ⚠️ 这几个接口都要登录（不在 /public/** 白名单里），所以【不要】加 silent: true，
//    出错就该给用户看到提示。

import request from './request'

/**
 * 下单
 *
 * 【只能传这 5 个字段】goodsId / nums / userName / userPhone / userAddress。
 * userId 和 price 后端会自己确定（从 token、从商品表算），
 * 前端就算传了也会被后端覆盖 —— 这是防篡改的设计，别想着「帮后端省事」多传。
 *
 * @param {Object} data { goodsId, nums, userName, userPhone, userAddress }
 */
export function createOrder(data) {
  return request.post('/orders', data)
}

/**
 * 我的订单（只返回当前登录用户的）
 * @param {Object} params { page, size }
 * @returns {Promise<{total: number, list: Array}>} 注意是 list，不是 records
 */
export function getMyOrders(params) {
  return request.get('/orders/my', { params })
}

/**
 * 后台订单分页（要管理员账号，普通用户调会 403）
 * @param {Object} params { page, size, name, orderNo }
 */
export function getOrderPage(params) {
  return request.get('/orders/page', { params })
}

/**
 * 删除订单
 * 前台删自己的（后端会校验归属），后台删任意单。
 * @param {number|string} id 订单 id
 */
export function deleteOrder(id) {
  return request.delete(`/orders/${id}`)
}
