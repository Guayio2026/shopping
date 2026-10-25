package com.scaffold.service;

import com.scaffold.common.BusinessException;
import com.scaffold.common.PageResult;
import com.scaffold.entity.Goods;
import com.scaffold.entity.Orders;
import com.scaffold.mapper.GoodsMapper;
import com.scaffold.mapper.OrdersMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单 业务层
 *
 * 【本项目不用「接口 + impl」写法，直接一个 @Service 类】照 CollectService 的风格。
 * 【本类不碰 HTTP】当前登录用户 id 由 Controller 从 token 里取好，当参数传进来。
 */
@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersMapper ordersMapper;
    private final GoodsMapper goodsMapper;

    /** 下单时间格式：和 goods.date / collect.time 保持一致 */
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 订单号前半段：年月日时分秒（教程写的 "yyyyMMddHHmmsss" 末尾多写了两个 s，是错的） */
    private static final DateTimeFormatter ORDER_NO_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 下单 —— 本章核心
     *
     * @param orders  前端提交的订单信息（只有 商品id / 数量 / 收货人 / 电话 / 地址 是可信的）
     * @param userId  当前登录用户 id，由 Controller 从 token 里取，【绝不能信前端传的】
     *
     * 【@Transactional 的作用】扣库存（update）和插订单（insert）是两次写操作，
     * 必须同生共死：后面任何一步抛异常，前面已经改了的库存会自动回滚。
     * 不加这个注解的话，一旦 insert 失败，库存就白白少了一份。
     */
    @Transactional
    public void create(Orders orders, Long userId) {

        // ===== 1. 校验参数（空的和非法的直接拒，别等写进数据库才发现）=====
        if (orders.getGoodsId() == null) {
            throw new BusinessException(400, "商品不能为空");
        }
        if (orders.getNums() == null || orders.getNums() <= 0) {
            throw new BusinessException(400, "购买数量必须大于 0");
        }
        if (orders.getUserName() == null || orders.getUserName().trim().isEmpty()) {
            throw new BusinessException(400, "收货人不能为空");
        }
        if (orders.getUserPhone() == null || orders.getUserPhone().trim().isEmpty()) {
            throw new BusinessException(400, "联系电话不能为空");
        }
        if (orders.getUserAddress() == null || orders.getUserAddress().trim().isEmpty()) {
            throw new BusinessException(400, "收货地址不能为空");
        }

        // ===== 2. userId 用 token 里的值覆盖掉前端传来的 =====
        // 教程让前端把 userId 放在请求体里一起 POST 过来 —— 那是漏洞：
        // 用浏览器 F12 改个数字，就能给别人的账号下单。
        orders.setUserId(userId);

        // ===== 3. 查商品：不存在 / 已下架都不给下单 =====
        Goods goods = goodsMapper.findById(orders.getGoodsId());
        if (goods == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (!"上架".equals(goods.getState())) {
            throw new BusinessException(400, "商品已下架，暂不能购买");
        }

        // ===== 4. 扣库存 + 加销量（一条 SQL 原子完成）=====
        // SQL 里有 AND store >= #{nums}：库存不够时 MySQL 直接不更新，返回 0 行
        // 「先查出来在内存里减，再 update 写回」的写法在两人同时下单时会超卖，别用
        int rows = goodsMapper.deductStoreAndAddSales(orders.getGoodsId(), orders.getNums());
        if (rows == 0) {
            // 商品名要在这里报出来，用户才知道是哪件买不了
            throw new BusinessException(400, goods.getName() + " 商品库存不足");
        }

        // ===== 5. 后端说了算的字段，全部覆盖前端传的值 =====
        orders.setName(goods.getName());                          // 商品名快照
        orders.setPrice(goods.getPrice() * orders.getNums());     // 总价后端重算，不信前端的 price
        orders.setOrderNo(newOrderNo());                          // 订单号
        orders.setTime(LocalDateTime.now().format(TIME_FORMAT));  // 下单时间
        // state 不设：建表时给了 DEFAULT '待付款'

        // ===== 6. 插订单（useGeneratedKeys 会把自增 id 回填到 orders.id）=====
        ordersMapper.insert(orders);
    }

    /**
     * 订单号 = 14 位时间戳 + 4 位随机数
     * 教程只用到秒，同一秒内下两单会重名；加 4 位随机数后重复概率可以忽略。
     */
    private String newOrderNo() {
        return LocalDateTime.now().format(ORDER_NO_FORMAT)
                + ThreadLocalRandom.current().nextInt(1000, 10000); // [1000, 9999]
    }

    /** 我的订单（只查这个用户的） */
    public PageResult<Orders> mine(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        long total = ordersMapper.countMine(userId);
        List<Orders> list = ordersMapper.findMine(userId, offset, size);
        return PageResult.of(total, list);
    }

    /** 后台分页（全部订单，可按商品名 / 订单号搜索） */
    public PageResult<Orders> page(String name, String orderNo, int page, int size) {
        int offset = (page - 1) * size;
        long total = ordersMapper.countPage(name, orderNo);
        List<Orders> list = ordersMapper.findPage(name, orderNo, offset, size);
        return PageResult.of(total, list);
    }

    /** 按 id 查一条（详情抽屉用） */
    public Orders getById(Integer id) {
        Orders orders = ordersMapper.findById(id);
        if (orders == null) {
            throw new BusinessException(404, "订单不存在");
        }
        return orders;
    }

    /**
     * 删除订单
     *
     * @param admin  当前账号是不是管理员。true = 后台入口（谁的单都能删）；
     *                false = 前台入口（只能删自己的）
     */
    public void delete(Integer id, Long userId, boolean admin) {
        Orders orders = ordersMapper.findById(id);
        if (orders == null) {
            throw new BusinessException(404, "订单不存在");
        }
        // 先查再删，就是为了这一刻：确认这单确实是本人下的
        // 判断 userId == null 是防历史脏数据直接 NPE
        if (!admin && (orders.getUserId() == null || !orders.getUserId().equals(userId))) {
            throw new BusinessException(403, "只能删除自己的订单");
        }
        ordersMapper.deleteById(id);
    }
}
