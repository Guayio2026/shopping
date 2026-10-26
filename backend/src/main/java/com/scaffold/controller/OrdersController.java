package com.scaffold.controller;

import com.scaffold.common.AuthContext;
import com.scaffold.common.BusinessException;
import com.scaffold.common.PageResult;
import com.scaffold.common.Result;
import com.scaffold.entity.Orders;
import com.scaffold.service.OrdersService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单 接口层
 *
 * 【前后端连接对照表】
 *  前端操作                        前端调的函数            打到本类              完整地址
 *  --------------------------------------------------------------------------------------
 *  详情页「立即购买」提交          → createOrder()      → create()    POST   /api/orders
 *  打开「我的订单」页 / 翻页       → getMyOrders()      → my()        GET    /api/orders/my
 *  订单列表点「删除」              → deleteOrder()      → delete()    DELETE /api/orders/8
 *  后台打开「订单管理」页 / 搜索   → getOrderPage()     → page()      GET    /api/orders/page
 *
 * 【权限】/orders/** 不在 WebMvcConfig 的白名单里 → JWT 拦截器自动要求登录。
 *         其中 /page 是后台专属，额外用 AuthContext.requireAdmin() 拦一道。
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;

    /**
     * 下单：POST /api/orders
     * 请求体只有 goodsId / nums / userName / userPhone / userAddress 是可信的，
     * userId 和 price 都由 Service 从 token / 商品表重新确定。
     */
    @PostMapping
    public Result<Void> create(@RequestBody Orders orders, HttpServletRequest request) {
        ordersService.create(orders, requireLogin(request));
        return Result.ok();
    }

    /** 我的订单：GET /api/orders/my?page=1&size=10（只返回本人的） */
    @GetMapping("/my")
    public Result<PageResult<Orders>> my(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         HttpServletRequest request) {
        Long userId = requireLogin(request);
        return Result.ok(ordersService.mine(userId, page, size));
    }

    /** 订单详情：GET /api/orders/{id} */
    @GetMapping("/{id}")
    public Result<Orders> detail(@PathVariable Integer id, HttpServletRequest request) {
        Orders orders = ordersService.getById(id);
        // 不是管理员，就只能看自己的单（不然改个数字就能看别人的收货信息）
        if (!isAdmin(request) && !orders.getUserId().equals(requireLogin(request))) {
            throw new BusinessException(403, "无权查看该订单");
        }
        return Result.ok(orders);
    }

    /**
     * 删除：DELETE /api/orders/{id}
     * 前台入口 admin=false（只能删自己的）；后台入口 admin=true（谁都能删）。
     * 用「当前账号是不是 ADMIN」来区分，而不是让前端传一个 admin 参数——那也能伪造。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id, HttpServletRequest request) {
        ordersService.delete(id, requireLogin(request), isAdmin(request));
        return Result.ok();
    }

    /**
     * 余额支付：POST /api/orders/8/pay
     * 只传订单 id；userId 由 requireLogin() 从 token 取（本类已有的私有方法）。
     * 不用 POST 请求体：支付这件事没有别的参数可传，
     * 金额由后端从订单里读（orders.price），前端传「付多少」那是给自己打折。
     */
    @PostMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Integer id, HttpServletRequest request) {
        ordersService.pay(id, requireLogin(request));
        return Result.ok();
    }

    /** 后台分页：GET /api/orders/page?page=1&size=10&name=&orderNo= */
    @GetMapping("/page")
    public Result<PageResult<Orders>> page(@RequestParam(defaultValue = "") String name,
                                           @RequestParam(defaultValue = "") String orderNo,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           HttpServletRequest request) {
        AuthContext.requireAdmin(request); // 普通用户调这个接口 → 403
        return Result.ok(ordersService.page(name, orderNo, page, size));
    }

    /** 从 request 里取当前登录用户 id（JWT 拦截器放进去的） */
    private Long requireLogin(HttpServletRequest request) {
        Long userId = AuthContext.getUserId(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录或 token 无效");
        }
        return userId;
    }

    /** 当前账号是不是管理员（决定能不能看全部订单、删别人的单） */
    private boolean isAdmin(HttpServletRequest request) {
        return "ADMIN".equals(AuthContext.getAccountType(request))
                || "ADMIN".equals(AuthContext.getRole(request));
    }
}
