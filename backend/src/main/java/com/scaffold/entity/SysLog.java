package com.scaffold.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysLog {

    private Long id;
    private Long userId;
    private String username;
    private String type;
    private String module;
    private String action;
    private String ip;
    private String detail;
    private LocalDateTime createdAt;
}
