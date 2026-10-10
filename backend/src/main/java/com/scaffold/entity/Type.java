package com.scaffold.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品分类 实体类（对应数据库表 type）
 *
 * ================== 这个类是前后端的「数据契约」 ==================
 * 它的字段名，就是前端收到的 JSON 字段名。举例：
 *   数据库列          Java 字段        返回给前端的 JSON      前端页面里怎么用
 *   ------------------------------------------------------------------
 *   id          →   id          →   "id"          →  row.id
 *   name        →   name        →   "name"        →  row.name
 *   sort_order  →   sortOrder   →   "sortOrder"   →  row.sortOrder
 *   created_at  →   createdAt   →   "createdAt"   →  row.createdAt
 * 所以 Types.vue 里 <el-table-column prop="sortOrder"> 的 prop 必须和这里的字段名一致，
 * 不然表格会显示空白。改这里字段名 = 前后端一起改。
 * ================================================================
 *
 * 【在链路中的位置】
 * 前端页面 → TypeController → TypeService → TypeMapper.xml → 数据库
 * 本类就是数据库里的"一行数据"，在整条链上被传来传去：
 *   数据库查出来 → 装进本类 → 变成 JSON 给前端
 *   前端提交 JSON → 变成本类 → 写进数据库
 *
 * 【注意】本项目用的是原生 MyBatis，所以：
 * - 不需要 @TableName 注解（表名写在 resources/mapper/TypeMapper.xml 里）
 * - 不需要 @TableId 注解（主键自增在数据库表里已定义）
 * 教程里的那些 MyBatis-Plus 注解，在本项目里都不用写。
 */
@Data // Lombok：自动生成 getter/setter，所以下面不用手写
public class Type {

    /** 主键 id（type 表列：id，自增）→ 前端 row.id */
    private Long id;

    /** 分类名称，如"数码""服装"（type 表列：name）→ 前端 row.name */
    private String name;

    /** 排序值，越小越靠前（type 表列：sort_order，下划线转驼峰）→ 前端 row.sortOrder */
    private Integer sortOrder;

    /** 创建时间（type 表列：created_at）→ 前端 row.createdAt，被 formatTime() 格式化后显示 */
    private LocalDateTime createdAt;
}
