package com.scaffold.controller;

import com.scaffold.common.AuthContext;
import com.scaffold.common.PageResult;
import com.scaffold.common.Result;
import com.scaffold.entity.Notice;
import com.scaffold.service.NoticeService;
import com.scaffold.service.SysLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final NoticeService noticeService;
    private final SysLogService sysLogService;

    @GetMapping
    public Result<PageResult<Notice>> page(HttpServletRequest request,
                                           @RequestParam(defaultValue = "") String keyword,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        AuthContext.requireAdmin(request);
        return Result.ok(noticeService.page(keyword, page, size));
    }

    @PostMapping
    public Result<Notice> create(HttpServletRequest request, @RequestBody Notice notice) {
        AuthContext.requireAdmin(request);
        Long adminId = AuthContext.getUserId(request);
        Integer userId = adminId == null ? null : adminId.intValue();

        Notice created = noticeService.create(notice, userId);
        sysLogService.logOperation(request, "公告管理", "新增公告",
                "发布公告：" + created.getName() + "（ID=" + created.getId() + "）");
        return Result.ok(created);
    }

    @PutMapping("/{id}")
    public Result<Notice> update(HttpServletRequest request, @PathVariable Integer id,
                                 @RequestBody Notice notice) {
        AuthContext.requireAdmin(request);
        Notice updated = noticeService.update(id, notice);
        sysLogService.logOperation(request, "公告管理", "修改公告",
                "更新公告：" + updated.getName() + "（ID=" + id + "）");
        return Result.ok(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Integer id) {
        AuthContext.requireAdmin(request);
        Notice existing = noticeService.getById(id);   // 先查出来记标题
        noticeService.delete(id);
        sysLogService.logOperation(request, "公告管理", "删除公告",
                "删除公告：" + existing.getName() + "（ID=" + id + "）");
        return Result.ok();
    }
}
