package com.scaffold.service;

import com.scaffold.common.PageResult;
import com.scaffold.entity.SysLog;
import com.scaffold.mapper.SysLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class SysLogService {

    private final SysLogMapper sysLogMapper;

    public PageResult<SysLog> page(String keyword, String type, int page, int size) {
        int offset = (page - 1) * size;
        long total = sysLogMapper.countPage(keyword, type);
        return PageResult.of(total, sysLogMapper.findPage(keyword, type, offset, size));
    }

    public void log(Long userId, String username, String type, String module, String action, String detail) {
        SysLog log = new SysLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setType(type);
        log.setModule(module);
        log.setAction(action);
        log.setDetail(detail);
        log.setIp(resolveClientIp());
        sysLogMapper.insert(log);
    }

    public void logOperation(HttpServletRequest request, String module, String action, String detail) {
        Long userId = null;
        String username = null;
        if (request != null) {
            Object id = request.getAttribute("userId");
            if (id instanceof Long longId) {
                userId = longId;
            } else if (id instanceof Integer intId) {
                userId = intId.longValue();
            }
            Object name = request.getAttribute("username");
            if (name instanceof String s) {
                username = s;
            }
        }
        log(userId, username, "OPERATION", module, action, detail);
    }

    public void delete(Long id) {
        sysLogMapper.deleteById(id);
    }

    public void ensureDemoLogs() {
        if (sysLogMapper.countPage(null, null) > 0) {
            return;
        }
        insertDemo("admin", "LOGIN", "认证中心", "管理员登录", "演示：后台账号登录成功");
        insertDemo("admin", "OPERATION", "用户管理", "新增用户", "演示：创建学员账号 student");
        insertDemo("teacher", "OPERATION", "课程管理", "发布课程", "演示：发布《Vue3 企业级后台实战课》");
        insertDemo("student", "LOGIN", "认证中心", "学员登录", "演示：前台学员登录成功");
        insertDemo("admin", "OPERATION", "系统设置", "更新配置", "演示：调整站点基础配置");
    }

    private void insertDemo(String username, String type, String module, String action, String detail) {
        SysLog log = new SysLog();
        log.setUsername(username);
        log.setType(type);
        log.setModule(module);
        log.setAction(action);
        log.setDetail(detail);
        log.setIp("127.0.0.1");
        sysLogMapper.insert(log);
    }

    private String resolveClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isBlank()) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception ignored) {
            // ignore
        }
        return "unknown";
    }
}
