package com.scaffold.controller.admin;

import com.scaffold.common.AuthContext;
import com.scaffold.common.PageResult;
import com.scaffold.common.Result;
import com.scaffold.entity.Admin;
import com.scaffold.service.AdminService;
import com.scaffold.service.SysLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/admins")
@RequiredArgsConstructor
public class AdminAdminController {

    private final AdminService adminService;
    private final SysLogService sysLogService;

    @GetMapping
    public Result<PageResult<Admin>> page(HttpServletRequest request,
                                          @RequestParam(defaultValue = "") String keyword,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        AuthContext.requireAdmin(request);
        return Result.ok(adminService.page(keyword, page, size));
    }

    @GetMapping("/{id}")
    public Result<Admin> detail(HttpServletRequest request, @PathVariable Long id) {
        AuthContext.requireAdmin(request);
        return Result.ok(adminService.getById(id));
    }

    @PostMapping
    public Result<Admin> create(HttpServletRequest request, @RequestBody Admin admin) {
        AuthContext.requireAdmin(request);
        Admin created = adminService.create(admin);
        sysLogService.logOperation(request, "管理员管理", "新增管理员",
                "创建管理员：" + created.getUsername() + "（ID=" + created.getId() + "）");
        return Result.ok(created);
    }

    @PutMapping("/{id}")
    public Result<Admin> update(HttpServletRequest request,
                                @PathVariable Long id,
                                @RequestBody Admin admin) {
        AuthContext.requireAdmin(request);
        Admin updated = adminService.update(id, admin);
        sysLogService.logOperation(request, "管理员管理", "修改管理员",
                "更新管理员：" + updated.getUsername() + "（ID=" + id + "）");
        return Result.ok(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        AuthContext.requireAdmin(request);
        Admin existing = adminService.getById(id);
        adminService.delete(id);
        sysLogService.logOperation(request, "管理员管理", "删除管理员",
                "删除管理员：" + existing.getUsername() + "（ID=" + id + "）");
        return Result.ok();
    }
}
