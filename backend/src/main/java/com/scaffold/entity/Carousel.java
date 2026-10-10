package com.scaffold.entity;

import lombok.Data;

/**
 * 轮播图 实体类（对应数据库表 carousel）
 *
 * ================== 这个类是前后端的「数据契约」 ==================
 * 它的字段名，就是前端收到的 JSON 字段名。举例：
 *   数据库列       Java 字段        返回给前端的 JSON      前端页面里怎么用
 *   ------------------------------------------------------------------
 *   id        →   id        →   "id"        →  row.id
 *   name      →   name      →   "name"      →  row.name
 *   cover     →   cover     →   "cover"     →  row.cover
 *   goods_id  →   goodsId   →   "goodsId"   →  row.goodsId
 * 所以 Carousels.vue 里 <el-table-column prop="name"> 的 prop 必须和这里的字段名一致，
 * 不然表格会显示空白。改这里字段名 = 前后端一起改。
 * ================================================================
 *
 * 【在链路中的位置】
 * 前端页面 → CarouselController → CarouselService → CarouselMapper.xml → 数据库
 * 本类就是数据库里的「一行数据」，在整条链上被传来传去：
 *   数据库查出来 → 装进本类 → 变成 JSON 给前端
 *   前端提交 JSON → 变成本类 → 写进数据库
 *
 * 【注意】本项目用的是原生 MyBatis，所以：
 * - 不需要 @TableName("carousel") 注解 —— 表名写在 resources/mapper/CarouselMapper.xml 里
 * - 不需要 @TableId(type = IdType.AUTO) 注解 —— 主键自增在数据库表里已定义
 * - 不需要 @TableField(exist = false) 注解 —— 非表字段只要 XML 里不映射即可
 * 教程里的那些 MyBatis-Plus 注解，在本项目里都不要写（写了会编译报错：找不到符号）。
 *
 * ⚠️ 【教程自带的坑】教程的实体类只写了 4 个字段（id/name/cover/goodsId），
 * 但它的 Service 里却有 carousel.setGoodsName(...)，前端页面也用了 prop="goodsName"。
 * 说明教程这一段漏写了 goodsName —— 照抄会编译不过。
 * 下面已经补齐（连同详情抽屉要用的 goodsCover）。
 */
@Data // Lombok：自动生成 getter/setter，所以下面不用手写
public class Carousel {

    /** 主键 id（carousel 表列：id，自增）→ 前端 row.id */
    private Integer id;

    /** 轮播图名称（carousel 表列：name）→ 前端 row.name */
    private String name;

    /** 封面图（carousel 表列：cover，存图片路径/URL）→ 前端 row.cover */
    private String cover;

    /** 关联的商品 id（carousel 表列：goods_id，下划线转驼峰）→ 前端 row.goodsId */
    private Integer goodsId;

    /**
     * 商品名称 —— 【不是 carousel 表的字段】
     *
     * 列表页要显示「这张轮播图推荐的是哪个商品」，但 carousel 表里只存了 goodsId（一个数字）。
     * 所以查询时用 JOIN 从 goods 表把名字带出来，装进这个字段。
     * 原生 MyBatis 的做法：在 CarouselMapper.xml 的 resultMap 里加一行
     *     <result property="goodsName" column="goods_name"/>
     * 并在 SQL 里写 LEFT JOIN goods gd ON c.goods_id = gd.id，取 gd.name AS goods_name
     *
     * 【注意】新增/修改轮播图时，这个字段不需要前端传，Service 会忽略它。
     */
    private String goodsName;

    /**
     * 商品封面 —— 【不是 carousel 表的字段】
     *
     * 详情抽屉里顺便把商品的图也显示出来，方便一眼确认「选的是不是这个商品」。
     * 同样靠 XML 里 gd.cover AS goods_cover 带出来。
     */
    private String goodsCover;
}
