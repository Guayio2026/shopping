import request from './request'

/** 启用中的首页轮播（公开接口） */
export function getPublicBanners() {
  return request.get('/public/banners', { silent: true })
}
