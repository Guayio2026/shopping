package com.scaffold.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysMenu {

    private Long id;
    private Long parentId;
    private String title;
    private String path;
    private String icon;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
}
