package com.scaffold.entity;

import lombok.Data;

@Data
public class Collect {

    // ↓↓↓ 这 7 个字段你写 ↓↓↓
    /** 主键 */
    private Integer id;

    /** 用户 id（表列 user_id） */
    private Integer userId;

    /** 商品 id（表列 goods_id） */
    private Integer goodsId;

    /** 收藏时间 */
    private String time;

    /** 【不是表字段】前台「我的收藏」要显示商品图/名/价 → 由 XML 的 <association> 映射 */
    private Goods goods;

    /** 【不是表字段】后台列表显示商品名 → 由 XML 的 LEFT JOIN goods 映射 */
    private String goodsName;

    /** 【不是表字段】后台列表显示用户名 → 由 XML 的 LEFT JOIN user 映射 */
    private String userName;

}

