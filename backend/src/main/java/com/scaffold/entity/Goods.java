package com.scaffold.entity;

import lombok.Data;

/**
 * 商品 实体类（对应数据库表 goods）
 *
 * ================== 这个类是前后端的「数据契约」 ==================
 * 它的字段名，就是前端收到的 JSON 字段名。举例：
 *   数据库列       Java 字段        返回给前端的 JSON      前端页面里怎么用
 *   ------------------------------------------------------------------
 *   id        →   id        →   "id"        →  row.id
 *   name      →   name      →   "name"      →  row.name
 *   user_id   →   userId    →   "userId"    →  row.userId
 *   type_id   →   typeId    →   "typeId"    →  row.typeId
 * 所以 Goods.vue 里 <el-table-column prop="typeName"> 的 prop 必须和这里的字段名一致，
 * 不然表格会显示空白。改这里字段名 = 前后端一起改。
 * ================================================================
 *
 * 【在链路中的位置】
 * 前端页面 → GoodsController → GoodsService → GoodsMapper.xml → 数据库
 * 本类就是数据库里的"一行数据"，在整条链上被传来传去：
 *   数据库查出来 → 装进本类 → 变成 JSON 给前端
 *   前端提交 JSON → 变成本类 → 写进数据库
 *
 * 【注意】本项目用的是原生 MyBatis，所以：
 * - 不需要 @TableName("goods") 注解 —— 表名写在 resources/mapper/GoodsMapper.xml 里
 * - 不需要 @TableId(type = IdType.AUTO) 注解 —— 主键自增在数据库表里已定义
 * - 不需要 @TableField(exist = false) 注解 —— 非表字段只要 XML 里不映射即可
 * 教程里的那些 MyBatis-Plus 注解，在本项目里都不要写（写了会编译报错，找不到符号）。
 */
@Data // Lombok：自动生成 getter/setter，所以下面不用手写
public class Goods {

    /** 主键 id（goods 表列：id，自增）→ 前端 row.id */
    private Integer id;

    /** 商品名称（goods 表列：name）→ 前端 row.name */
    private String name;

    /** 商品描述/简介（goods 表列：descr）→ 前端 row.descr */
    private String descr;

    /** 商品详情正文（goods 表列：content，text 类型）→ 前端 row.content */
    private String content;

    /** 商品封面图（goods 表列：cover，存图片路径/URL）→ 前端 row.cover */
    private String cover;

    /** 价格（goods 表列：price，double(10,2)）→ 前端 row.price */
    private Double price;

    /** 库存（goods 表列：store）→ 前端 row.store */
    private Integer store;

    /** 所属用户 id（goods 表列：user_id，下划线转驼峰）→ 前端 row.userId */
    private Integer userId;

    /** 上架日期（goods 表列：date）→ 前端 row.date */
    private String date;

    /** 所属分类 id（goods 表列：type_id）→ 前端 row.typeId，关联 type 表的 id */
    private Integer typeId;

    /** 商品状态，如"上架"/"下架"（goods 表列：state）→ 前端 row.state */
    private String state;

    /** 销量（goods 表列：sales）→ 前端 row.sales */
    private Integer sales;

    /**
     * 分类名称 —— 【不是 goods 表的字段】
     *
     * 列表页要显示"这个商品属于哪个分类"，但 goods 表里只存了 typeId。
     * 所以查询时用 JOIN 从 type 表把名字带出来，装进这个字段。
     * 原生 MyBatis 的做法：在 GoodsMapper.xml 的 resultMap 里加一行
     *     <result property="typeName" column="type_name"/>
     * 并在 SQL 里写 JOIN type t ON g.type_id = t.id，取 t.name AS type_name
     *
     * 【注意】新增/修改商品时，这个字段不需要前端传，Service 会忽略它。
     */
    private String typeName;

    /**
     * 用户名 —— 【不是 goods 表的字段】
     *
     * 同上：goods 表只存了 userId，要显示"发布者是谁"就得 JOIN user 表。
     * 在 GoodsMapper.xml 的 resultMap 里加 <result property="userName" column="user_name"/>
     */
    private String userName;
}
