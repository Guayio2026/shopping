package com.scaffold.controller;

import com.scaffold.common.AuthContext;
import com.scaffold.common.BusinessException;
import com.scaffold.common.PageResult;
import com.scaffold.common.Result;
import com.scaffold.entity.Collect;
import com.scaffold.service.CollectService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏 接口层
 *
 * 【前后端连接对照表】
 *  前端操作                      前端调的函数           打到本类             完整地址
 *  ---------------------------------------------------------------------------------
 *  详情页点「收藏/已收藏」      → toggleCollect()   → toggle()   POST   /api/collect/toggle
 *  详情页判断自己收藏没有        → checkCollect()    → check()    GET    /api/collect/check?goodsId=8
 *  打开「我的收藏」页            → getMyCollect()    → my()       GET    /api/collect/my
 *  后台打开「收藏管理」页        → getCollectPage()  → page()     GET    /api/collect/page?page=1&size=10
 *  点「删除」/「取消收藏」       → deleteCollect()   → delete()   DELETE /api/collect/8
 *
 * 【为什么 /collect/** 不用改 WebMvcConfig】它不在白名单里 → 自动被 JWT 拦截器保护 → 未登录 401。
 * 这正是「因人而异」的数据该有的样子。
 */
@RestController
@RequestMapping("/collect")
@RequiredArgsConstructor
public class CollectController {

    private final CollectService collectService;

    /** 收藏/取消：POST /api/collect/toggle  {"goodsId":8} → data: true(已收藏)/false(已取消) */
    @PostMapping("/toggle")
    public Result<Boolean> toggle(@RequestBody Collect collect, HttpServletRequest request) {
        Long userId = requireLogin(request);
        return Result.ok(collectService.toggle(userId, collect.getGoodsId()));
    }

    /** 是否已收藏：GET /api/collect/check?goodsId=8 → data: true/false */
    @GetMapping("/check")
    public Result<Boolean> check(@RequestParam Integer goodsId, HttpServletRequest request) {
        Long userId = requireLogin(request);
        return Result.ok(collectService.isCollected(userId, goodsId));
    }

    /** 我的收藏：GET /api/collect/my */
    @GetMapping("/my")
    public Result<List<Collect>> my(HttpServletRequest request) {
        Long userId = requireLogin(request);
        return Result.ok(collectService.listMine(userId));
    }

    /** 后台分页：GET /api/collect/page?page=1&size=10 */
    @GetMapping("/page")
    public Result<PageResult<Collect>> page(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(collectService.page(page, size));
    }

    /** 删除：DELETE /api/collect/{id}（后台删记录 / 前台「取消收藏」共用） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        collectService.delete(id);
        return Result.ok();
    }

    /** 从 request 里取当前登录用户 id；取不到说明没登录 */
    private Long requireLogin(HttpServletRequest request) {
        Long userId = AuthContext.getUserId(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录或 token 无效");
        }
        return userId;
    }
}
