package com.scaffold.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class User {

    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String phone;
    private String role;
    private Integer status;
    private String openid;
    private BigDecimal balance;
    /**
     * 账户余额（跟教程命名叫 account，默认值 0）
     *
     * 【和 balance 什么关系】本项目脚手架早就有一个 balance（BigDecimal）列，
     * 语义和这个字段重复。本模块按教程用 account 走「充值 / 支付」链路，
     * balance 暂不使用。真实项目里应该二选一，留两个余额字段是设计坏味道。
     *
     * 【为什么教程用 Double 是妥协】教程图省事用 double。double 是近似值，
     * 0.1 + 0.2 = 0.30000000000000004，金额稍大就会差几分钱。
     * 严谨做法是 BigDecimal（参考上面的 balance）。本模块保持教程写法，但在注释里标明。
     *
     * 【命名提醒】本项目已有一个概念叫 accountType（账号类型 ADMIN/USER），
     * 和这个「账户余额 account」不是一回事，看代码时别搞混。
     */
    private Double account;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
