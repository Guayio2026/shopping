package com.scaffold.controller;

import com.scaffold.common.PageResult;
import com.scaffold.common.Result;
import com.scaffold.entity.Goods;
import com.scaffold.entity.HomeBanner;
import com.scaffold.entity.Type;
import com.scaffold.service.HomeBannerService;
import com.scaffold.service.SysConfigService;
import com.scaffold.service.CarouselService;
import com.scaffold.service.TypeService;
import com.scaffold.service.GoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {

    private final SysConfigService sysConfigService;
    private final HomeBannerService homeBannerService;
    private final CarouselService carouselService;
    private final TypeService typeService;
    private final GoodsService goodsService;
    @GetMapping("/system-config")
    public Result<Map<String, String>> systemConfig() {
        return Result.ok(sysConfigService.getPublicConfig());
    }
    /**
     * 【首页聚合数据】GET /api/public/home
     *
     * 谁在调：views/user/Home.vue 的 onMounted → api/homePublic.js 的 getHomeData()
     *
     * 【为什么要有这个接口，而不是让首页调 4 个接口】
     * 前台首页 /user/home 是「免登录」页面（路由 meta.public = true），游客能直接打开。
     * 但 /carousel/list、/type/list、/goods/times、/goods/sales 都在 JWT 拦截器后面，
     * 游客调它们会拿到 401 → 首页一片空白。
     * 所以把首页要的 4 样数据「打包」成一个公开接口返回，路由在 WebMvcConfig 的
     * 白名单里（/public/**），游客也能拿到。
     *
     * 【额外好处】首页从「发 4 个请求」变成「发 1 个请求」。
     *
     * 【为什么用 Map 而不是新建一个 DTO 类】数据是临时打包用的，字段固定且只有 4 个，
     * Map 够用且不用多建文件。以后字段多起来，可以换成专门的 HomeVO 类。
     */
    @GetMapping("/home")
    public Result<Map<String, Object>> home() {
        // LinkedHashMap 保证 JSON 里的字段顺序和 put 的顺序一致（好调试，看着舒服）
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("carousels", carouselService.listAll());  // 轮播图（全部）
        data.put("types", typeService.listAll());          // 分类（全部）
        data.put("timeGoods", goodsService.times());       // 新品上架 4 条
        data.put("salesGoods", goodsService.sales());      // 热销商品 4 条
        return Result.ok(data);
    }

    /**
     * 【前台商品分页】GET /api/public/goods?keyword=坚果&typeId=12&page=1&size=8
     *
     * 谁在调：views/user/GoodsList.vue 的 loadGoods() → api/goodsPublic.js 的 getPublicGoods()
     *
     * 【为什么挂 /public 而不是复用 /goods/page】
     * /user/goods 是免登录页面（路由 meta.public = true），游客能直接打开。
     * 而 /goods/page 在 JWT 拦截器后面 —— 游客调它拿到的是 401，页面一片空白。
     * /public/** 在 WebMvcConfig 的白名单里，游客也能访问。
     *
     * 【和 /goods/page 的另一处不同】这里走的是 pageOnSale()，
     * 只返回「上架」商品 —— 顾客不该看到下架商品。
     *
     * 【参数说明】typeId 用 Integer 不用 int：不筛分类时前端不传这个参数，
     * 不传就是 null（int 不能是 null）。
     */
    @GetMapping("/goods")
    public Result<PageResult<Goods>> goods(@RequestParam(defaultValue = "") String keyword,
                                           @RequestParam(required = false) Integer typeId,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "8") int size) {
        return Result.ok(goodsService.pageOnSale(keyword, typeId, page, size));
    }

    /**
     * 【前台分类列表】GET /api/public/types
     *
     * 谁在调：views/user/GoodsList.vue 的 loadTypes()（渲染页面顶部那排分类按钮）
     * 返回：全部分类数组 [{id:10, name:'数码'}, ...]
     *
     * 【为什么不用现成的 /type/list】那个接口在 JWT 拦截器后面，游客调会 401。
     */
    @GetMapping("/types")
    public Result<List<Type>> types() {
        return Result.ok(typeService.listAll());
    }

    @GetMapping("/banners")
    public Result<List<HomeBanner>> banners() {
        return Result.ok(homeBannerService.listEnabled());
    }
    @GetMapping("/goods/{id}")
    public Result<Goods> goodsDetail(@PathVariable Integer id) {
        return Result.ok(goodsService.getOnSale(id));
    }

}
