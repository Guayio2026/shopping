// 前台商城 —— 公开接口封装（不需要登录就能调）
//
// 【为什么单独一个文件，而不是加进 api/goods.js】
// api/goods.js 里的接口（/goods/page、/goods/list 等）都在 JWT 拦截器后面，
// 游客调会拿到 401。前台页面（首页 / 全部商品页）是免登录的，
// 只能调 /public/** 这些白名单接口。两者用途不同，分开放最清楚。
//
// 【链路】views/user/GoodsList.vue → 【本文件】 → request.js（自动加 /api 前缀）→ PublicController
//
// 【对照后端】
//   本文件                          后端 PublicController              完整地址
//   getPublicGoods()  ↔  @GetMapping("/goods")  →  GET /api/public/goods
//   getPublicTypes()  ↔  @GetMapping("/types")  →  GET /api/public/types
//
// ⚠️ URL 里不要再写 /api —— request.js 的 baseURL 已经配了，写了会变成 /api/api/...

import request from './request'

/**
 * 前台商品分页
 *
 * params 形如 { keyword, typeId, page, size }
 *   keyword —— 商品名关键字（可选，不传就不按名字筛）
 *   typeId  —— 分类 id（可选；「全部」时**不要传 0**，直接不传这个字段）
 *   page / size —— 第几页 / 每页几条（后端默认 1 / 8）
 *
 * silent: true → 请求失败时不弹红字提示条。
 *   公开页是给游客看的，弹一堆报错很难看。和 bannerPublic.js / homePublic.js 一致。
 *   ⚠️ silent 必须和 params 同级（都是 axios config 的字段），别塞进 params 里。
 */
export function getPublicGoods(params) {
  return request.get('/public/goods', { params, silent: true })
}
/**
 * 前台商品详情
 * @param {number|string} id 商品 id
 * 对应后端：GET /api/public/goods/{id}
 */
export function getPublicGoodsById(id) {
  return request.get(`/public/goods/${id}`, { silent: true })
}

/** 前台分类列表（页面顶部那排分类按钮用） */
export function getPublicTypes() {
  return request.get('/public/types', { silent: true })
}
