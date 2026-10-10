package com.scaffold.service;

import com.scaffold.common.BusinessException;
import com.scaffold.common.PageResult;
import com.scaffold.entity.Admin;
import com.scaffold.mapper.AdminMapper;
import com.scaffold.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminMapper adminMapper;
    private final UserMapper userMapper;

    public PageResult<Admin> page(String keyword, int page, int size) {
        int offset = (page - 1) * size;
        long total = adminMapper.countPage(keyword);
        List<Admin> list = adminMapper.findPage(keyword, offset, size);
        list.forEach(a -> a.setPassword(null));
        return PageResult.of(total, list);
    }

    public Admin getById(Long id) {
        Admin admin = adminMapper.findById(id);
        if (admin == null) {
            throw new BusinessException(404, "管理员不存在");
        }
        admin.setPassword(null);
        return admin;
    }

    public Admin create(Admin admin) {
        if (!StringUtils.hasText(admin.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        if (!StringUtils.hasText(admin.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        if (adminMapper.findByUsername(admin.getUsername()) != null
                || userMapper.findByUsername(admin.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        if (admin.getStatus() == null) {
            admin.setStatus(1);
        }
        adminMapper.insert(admin);
        return getById(admin.getId());
    }

    public Admin update(Long id, Admin admin) {
        getById(id);
        admin.setId(id);
        adminMapper.update(admin);
        return getById(id);
    }

    public void delete(Long id) {
        getById(id);
        long total = adminMapper.countAll();
        if (total <= 1) {
            throw new BusinessException("至少保留一名管理员");
        }
        adminMapper.deleteById(id);
    }
}
