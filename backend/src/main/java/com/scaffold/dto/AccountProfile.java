package com.scaffold.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AccountProfile {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    /** ADMIN for admin table accounts; USER/TEACHER for user table */
    private String role;
    /** ADMIN | USER */
    private String accountType;
    private Integer status;
    private LocalDateTime createdAt;
}
