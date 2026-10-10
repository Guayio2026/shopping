package com.scaffold.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysConfig {

    private String configKey;
    private String configValue;
    private String remark;
    private LocalDateTime updatedAt;
}
