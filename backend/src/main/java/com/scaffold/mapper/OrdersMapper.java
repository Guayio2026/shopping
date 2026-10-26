package com.scaffold.mapper;

import com.scaffold.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单 Mapper 接口（数据访问层）
 *
 * 【在链路中的位置】
 * 前端页面 → OrdersController → OrdersService → 【本接口】→ OrdersMapper.xml → MySQL
 *
 * 【为什么只有声明、没有实现】
 * 方法对应的 SQL 全写在 resources/mapper/OrdersMapper.xml 里，
 * 两边靠「方法名 == SQL 的 id」配对（如 findMine() ↔ <select id="findMine">）。
 * 实现类由 MyBatis 在启动时自动生成（靠类上的 @Mapper 注解）。
 *
 * 【和 CollectMapper 的区别】
 *   1. findMine / countMine 多收一个 userId —— 「我的订单」只查自己的
 *   2. findPage 多两个参数 name / orderNo —— 后台要按商品名、订单号搜索
 */
@Mapper // 告诉 MyBatis：这是 Mapper 接口，启动时为它自动生成实现类
public interface OrdersMapper {

    /** 新增订单（useGeneratedKeys 会把自增 id 回填到 orders.id） */
    int insert(Orders orders);

    /** 按 id 查一条订单（详情、删除前校验归属用） */
    Orders findById(@Param("id") Integer id);

    /**
     * 我的订单（只查这个用户的，带商品封面图）
     * @param userId 当前登录用户 id，由 Controller 从 token 里取，【不是前端传的】
     * @param offset 跳过的条数，Service 算好：(page-1)*size
     * @param limit  每页几条
     */
    List<Orders> findMine(@Param("userId") Long userId,
                          @Param("offset") int offset,
                          @Param("limit") int limit);

    /** 我的订单总条数（分页用） */
    long countMine(@Param("userId") Long userId);

    /**
     * 后台分页：全部订单，带商品信息 + 用户昵称
     * @param name    商品名关键字，空字符串 = 不筛
     * @param orderNo 订单号关键字，空字符串 = 不筛
     */
    List<Orders> findPage(@Param("name") String name,
                          @Param("orderNo") String orderNo,
                          @Param("offset") int offset,
                          @Param("limit") int limit);

    /** 后台总条数（筛选条件必须和 findPage 保持一致，不然分页页数会算错） */
    long countPage(@Param("name") String name,
                   @Param("orderNo") String orderNo);

    /** 按 id 删除订单 */
    int deleteById(@Param("id") Integer id);
    // 把订单标记为已支付：条件更新，只有当前还是「待付款」才改
    // 返回 1 = 改成功；返回 0 = 这单已经付过了（或状态被改过）
    int markPaid(@Param("id") Integer id);

}
