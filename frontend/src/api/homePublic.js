// 前台首页 —— 公开接口封装
//
// 【为什么要单独一个文件】
// 首页是免登录页面，走的是 /public/** 白名单接口，和后台管理那些 api 不是一类。
// 项目里已有 api/bannerPublic.js 也是这个套路。

import request from './request'

/**
 * 首页聚合数据
 * 一次返回 4 样：{ carousels, types, timeGoods, salesGoods }
 *
 * silent: true —— request.js 的「静默模式」：接口失败时不弹红色错误提示。
 * 首页是公开页面，偶发失败不该拿红字糊用户脸。
 */
export function getHomeData() {
    return request.get('/public/home', { silent: true })
}
