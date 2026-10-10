package com.scaffold.common;

import jakarta.servlet.http.HttpServletRequest;

public final class AuthContext {

    private AuthContext() {
    }

    public static Long getUserId(HttpServletRequest request) {
        Object value = request.getAttribute("userId");
        if (value instanceof Long id) {
            return id;
        }
        if (value instanceof Integer id) {
            return id.longValue();
        }
        return null;
    }

    public static String getUsername(HttpServletRequest request) {
        return (String) request.getAttribute("username");
    }

    public static String getRole(HttpServletRequest request) {
        return (String) request.getAttribute("role");
    }

    public static String getAccountType(HttpServletRequest request) {
        return (String) request.getAttribute("accountType");
    }

    public static void requireAdmin(HttpServletRequest request) {
        if (!"ADMIN".equals(getAccountType(request)) && !"ADMIN".equals(getRole(request))) {
            throw new BusinessException(403, "无管理员权限");
        }
    }
}
