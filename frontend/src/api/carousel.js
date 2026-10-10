// 轮播图2 —— 前端接口封装
//
// 【这个文件的作用】
// 把「后端有哪些轮播图接口」集中写在一处，页面只调用这里的方法，
// 不直接在页面里写 URL。以后接口地址变了，只改这个文件。
//
// 【链路】Carousels.vue → 【本文件】 → request.js（自动加 /api 前缀和 token）→ 后端 CarouselController
//
// 【对照后端】下面的 URL 和 backend 的 CarouselController 一一对应：
//   本文件                    后端 CarouselController            完整地址
//   --------------------------------------------------------------------------------------------
//   getCarouselPage   ↔  @GetMapping("/page")        →  GET    /api/carousel/page
//   getCarouselList   ↔  @GetMapping("/list")        →  GET    /api/carousel/list
//   getCarouselById   ↔  @GetMapping("/{id}")        →  GET    /api/carousel/{id}
//   createCarousel    ↔  @PostMapping                →  POST   /api/carousel
//   updateCarousel    ↔  @PutMapping("/{id}")        →  PUT    /api/carousel/{id}
//   deleteCarousel    ↔  @DeleteMapping("/{id}")     →  DELETE /api/carousel/{id}
//
// 【和教程的对照】教程前端的 api 调用是直接写在页面里的（this.$request.get("/carousel/selectPage",...)），
// 本项目多抽出一个 api 文件，页面只管调函数。路径也换成了 RESTful 风格：
//   教程 /carousel/selectPage    ↔  本文件 /carousel/page
//   教程 /carousel/add           ↔  本文件 POST /carousel
//   教程 /carousel/update        ↔  本文件 PUT  /carousel/{id}
//   教程 /carousel/delete?id=1   ↔  本文件 DELETE /carousel/{id}
//   教程 /carousel/selectAll     ↔  本文件 /carousel/list
//   教程 /carousel/selectById?id ↔  本文件 /carousel/{id}

import request from './request'

/**
 * 分页查询轮播图（轮播图2 页面列表用这个）
 *
 * params 形如 { keyword, page, size }
 *   keyword —— 轮播图名称关键字（可选，不传就不按名字筛）
 *   page / size —— 第几页 / 每页几条
 */
export function getCarouselPage(params) {
  return request.get('/carousel/page', { params })
}

/** 查询全部轮播图（不分页，下拉选择框之类的地方用） */
export function getCarouselList() {
  return request.get('/carousel/list')
}

/** 根据 id 查一条（编辑前回查详情用） */
export function getCarouselById(id) {
  return request.get(`/carousel/${id}`)
}

/** 新增轮播图。data 形如 { name: '首页首屏推荐', cover: '/uploads/a.png', goodsId: 4 } */
export function createCarousel(data) {
  return request.post('/carousel', data)
}

/** 修改轮播图。id 是被改的那条记录的 id，data 是要改的内容 */
export function updateCarousel(id, data) {
  return request.put(`/carousel/${id}`, data)
}

/** 删除轮播图 */
export function deleteCarousel(id) {
  return request.delete(`/carousel/${id}`)
}
