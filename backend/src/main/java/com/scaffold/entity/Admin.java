package com.scaffold.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Admin {

    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String phone;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
