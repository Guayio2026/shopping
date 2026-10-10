package com.scaffold.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {

    private String nickname;
    private String avatar;
    private String phone;
}
