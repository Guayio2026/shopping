package com.scaffold.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HomeBanner {

    private Long id;
    private String title;
    private String subtitle;
    private String cover;
    private String linkUrl;
    private String ctaText;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
