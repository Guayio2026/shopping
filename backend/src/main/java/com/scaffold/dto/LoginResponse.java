package com.scaffold.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String username;
    private String nickname;
    private String role;
    private String avatar;
    private Long userId;
    /** ADMIN | USER */
    private String accountType;
}
