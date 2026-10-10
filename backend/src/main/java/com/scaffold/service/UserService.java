package com.scaffold.service;

import com.scaffold.common.BusinessException;
import com.scaffold.common.PageResult;
import com.scaffold.entity.User;
import com.scaffold.mapper.AdminMapper;
import com.scaffold.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Set<String> ALLOWED_ROLES = Set.of("USER", "TEACHER");

    private final UserMapper userMapper;
    private final AdminMapper adminMapper;

    public PageResult<User> page(String keyword, String role, int page, int size) {
        int offset = (page - 1) * size;
        long total = userMapper.countPage(keyword, role);
        List<User> list = userMapper.findPage(keyword, role, offset, size);
        list.forEach(u -> u.setPassword(null));
        return PageResult.of(total, list);
    }

    public User getById(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    public User create(User user) {
        if (!StringUtils.hasText(user.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        if (!StringUtils.hasText(user.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        if (userMapper.findByUsername(user.getUsername()) != null
                || adminMapper.findByUsername(user.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        if (user.getRole() == null) {
            user.setRole("USER");
        }
        if (!ALLOWED_ROLES.contains(user.getRole())) {
            throw new BusinessException("用户角色无效");
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        if (user.getBalance() == null) {
            user.setBalance(BigDecimal.ZERO);
        }
        userMapper.insert(user);
        return getById(user.getId());
    }

    public User update(Long id, User user) {
        getById(id);
        if (StringUtils.hasText(user.getRole()) && !ALLOWED_ROLES.contains(user.getRole())) {
            throw new BusinessException("用户角色无效");
        }
        user.setId(id);
        userMapper.update(user);
        return getById(id);
    }

    public void delete(Long id) {
        getById(id);
        userMapper.deleteById(id);
    }
}
