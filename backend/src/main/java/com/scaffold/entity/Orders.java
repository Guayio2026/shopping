package com.scaffold.entity;

import lombok.Data;

/**
 * 订单 实体类（对应数据库表 orders）
 *
 * 【什么是「快照」】name / price / user_name / user_phone / user_address
 * 都是下单那一刻的副本：商品后来改名涨价、用户后来改资料，
 * 都不影响已经生成的订单 —— 订单必须保持"当时的样子"。
 *
 * 【本项目是原生 MyBatis】所以：
 * - 不写 @TableName("orders") / @TableId / @TableField(exist=false)
 * - 表名和列名全靠 resources/mapper/OrdersMapper.xml 里的 SQL 和 resultMap 决定
 *
 * 【user_id 是 bigint】user.id 是 bigint(20)，AuthContext.getUserId() 返回 Long，
 * 所以这里用 Long，和整个项目保持一致（Collect 用 Integer 是因为当时图省事，别学它）。
 */
@Data // Lombok 自动生成 getter/setter，下面不用手写
public class Orders {

    /** 主键（表列 id，自增） */
    private Integer id;

    /** 商品名称快照（表列 name）→ 前端 row.name */
    private String name;

    /** 订单号（表列 order_no）→ 前端 row.orderNo */
    private String orderNo;

    /** 商品 id（表列 goods_id）→ 前端 row.goodsId */
    private Integer goodsId;

    /** 订单总价 = 单价 × 数量（表列 price）→ 前端 row.price */
    private Double price;

    /** 购买数量（表列 nums）→ 前端 row.nums */
    private Integer nums;

    /** 收货人（表列 user_name）→ 前端 row.userName */
    private String userName;

    /** 联系电话（表列 user_phone）→ 前端 row.userPhone */
    private String userPhone;

    /** 收货地址（表列 user_address）→ 前端 row.userAddress */
    private String userAddress;

    /** 下单时间，字符串（表列 time），格式 yyyy-MM-dd HH:mm:ss */
    private String time;

    /** 订单状态（表列 state），默认"待付款" → 前端 row.state */
    private String state;

    /** 下单用户 id（表列 user_id，bigint）→ 前端 row.userId */
    private Long userId;

    /**
     * 商品信息 ——【不是 orders 表的列】
     * 后台订单列表要显示商品封面图（goods.cover），
     * 靠 OrdersMapper.xml 的 <association> 从 goods 表映射进来。
     */
    private Goods goods;

    /**
     * 用户信息 ——【不是 orders 表的列】
     * 后台订单列表要显示下单人的昵称（user.nickname），
     * 靠 <association> 从 user 表映射进来。
     */
    private User user;
}
