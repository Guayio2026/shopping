package com.scaffold.controller.admin;

import com.scaffold.common.AuthContext;
import com.scaffold.common.Result;
import com.scaffold.service.SysConfigService;
import com.scaffold.service.SysLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/system-config")
@RequiredArgsConstructor
public class AdminSystemConfigController {

    private final SysConfigService sysConfigService;
    private final SysLogService sysLogService;

    @GetMapping
    public Result<Map<String, String>> get(HttpServletRequest request) {
        AuthContext.requireAdmin(request);
        return Result.ok(sysConfigService.getPublicConfig());
    }

    @PutMapping
    public Result<Map<String, String>> update(HttpServletRequest request, @RequestBody Map<String, String> body) {
        AuthContext.requireAdmin(request);
        String systemName = body == null ? null : body.get("systemName");
        String systemLogo = body == null ? null : body.get("systemLogo");
        Map<String, String> updated = sysConfigService.updatePublicConfig(systemName, systemLogo);
        sysLogService.logOperation(request, "系统设置", "更新配置",
                "系统名称=" + updated.get("systemName")
                        + "；Logo=" + (updated.get("systemLogo") == null || updated.get("systemLogo").isBlank()
                        ? "默认" : "已自定义"));
        return Result.ok(updated);
    }
}
