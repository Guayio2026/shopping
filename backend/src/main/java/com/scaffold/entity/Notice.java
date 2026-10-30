package com.scaffold.entity;

import lombok.Data;

@Data
public class Notice {

    private Integer id;
    private String name;
    private String content;
    private String time;
    private Integer userId;
}
