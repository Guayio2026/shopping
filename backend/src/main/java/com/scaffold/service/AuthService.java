package com.scaffold.service;

import com.scaffold.common.BusinessException;
import com.scaffold.dto.AccountProfile;
import com.scaffold.dto.LoginRequest;
import com.scaffold.dto.LoginResponse;
import com.scaffold.dto.PasswordChangeRequest;
import com.scaffold.dto.ProfileUpdateRequest;
import com.scaffold.dto.RegisterRequest;
import com.scaffold.entity.Admin;
import com.scaffold.entity.User;
import com.scaffold.mapper.AdminMapper;
import com.scaffold.mapper.UserMapper;
import com.scaffold.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.scaffold.dto.RechargeRequest;

@Service
@RequiredArgsConstructor
public class AuthService {

    public static final String ACCOUNT_ADMIN = "ADMIN";
    public static final String ACCOUNT_USER = "USER";

    private final AdminMapper adminMapper;
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final SysLogService sysLogService;

    public LoginResponse login(LoginRequest request) {
        Admin admin = adminMapper.findByUsername(request.getUsername());
        if (admin != null) {
            if (!admin.getPassword().equals(request.getPassword())) {
                throw new BusinessException("用户名或密码错误");
            }
            if (admin.getStatus() != null && admin.getStatus() == 0) {
                throw new BusinessException("账号已被禁用");
            }
            sysLogService.log(admin.getId(), admin.getUsername(), "LOGIN", "后台认证", "管理员登录", "登录成功");
            return buildAdminLoginResponse(admin);
        }

        User user = userMapper.findByUsername(request.getUsername());
        if (user == null || !user.getPassword().equals(request.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        sysLogService.log(user.getId(), user.getUsername(), "LOGIN", "用户认证", "用户登录", "登录成功");
        return buildUserLoginResponse(user);
    }

    public LoginResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        if (adminMapper.findByUsername(username) != null || userMapper.findByUsername(username) != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(request.getPassword());
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname().trim() : username);
        user.setPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : null);
        user.setRole(ACCOUNT_USER);
        user.setStatus(1);
        user.setBalance(java.math.BigDecimal.ZERO);
        userMapper.insert(user);

        sysLogService.log(user.getId(), user.getUsername(), "REGISTER", "用户认证", "用户注册", "注册成功");
        return buildUserLoginResponse(userMapper.findById(user.getId()));
    }

    public AccountProfile getCurrentUser(Long accountId, String accountType) {
        if (ACCOUNT_ADMIN.equals(accountType)) {
            Admin admin = adminMapper.findById(accountId);
            if (admin == null) {
                throw new BusinessException(404, "管理员不存在");
            }
            return toAdminProfile(admin);
        }
        User user = userMapper.findById(accountId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return toUserProfile(user);
    }

    public AccountProfile updateProfile(Long accountId, String accountType, ProfileUpdateRequest request) {
        if (ACCOUNT_ADMIN.equals(accountType)) {
            Admin admin = adminMapper.findById(accountId);
            if (admin == null) {
                throw new BusinessException(404, "管理员不存在");
            }
            if (StringUtils.hasText(request.getNickname())) {
                admin.setNickname(request.getNickname());
            }
            if (request.getAvatar() != null) {
                admin.setAvatar(request.getAvatar());
            }
            if (request.getPhone() != null) {
                admin.setPhone(request.getPhone());
            }
            adminMapper.update(admin);
            Admin saved = adminMapper.findById(accountId);
            sysLogService.log(saved.getId(), saved.getUsername(), "OPERATION", "后台认证", "更新资料", "管理员更新个人资料");
            return toAdminProfile(saved);
        }

        User user = userMapper.findById(accountId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        userMapper.update(user);
        User savedUser = userMapper.findById(accountId);
        sysLogService.log(savedUser.getId(), savedUser.getUsername(), "OPERATION", "用户认证", "更新资料", "用户更新个人资料");
        return toUserProfile(savedUser);
    }

    public void changePassword(Long accountId, String accountType, PasswordChangeRequest request) {
        if (ACCOUNT_ADMIN.equals(accountType)) {
            Admin admin = adminMapper.findById(accountId);
            if (admin == null) {
                throw new BusinessException(404, "管理员不存在");
            }
            if (!admin.getPassword().equals(request.getOldPassword())) {
                throw new BusinessException("原密码错误");
            }
            adminMapper.updatePassword(accountId, request.getNewPassword());
            sysLogService.log(admin.getId(), admin.getUsername(), "OPERATION", "后台认证", "修改密码", "管理员修改登录密码");
            return;
        }

        User user = userMapper.findById(accountId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!user.getPassword().equals(request.getOldPassword())) {
            throw new BusinessException("原密码错误");
        }
        userMapper.updatePassword(accountId, request.getNewPassword());
        sysLogService.log(user.getId(), user.getUsername(), "OPERATION", "用户认证", "修改密码", "用户修改登录密码");
    }
    /**
     * 账户充值（模拟充值：不接真实支付网关）
     *
     * @param accountId   当前登录账号 id —— Controller 从 token 取
     * @param accountType 账号类型（ADMIN / USER）—— 同样来自 token
     * @param request     充值请求体（金额 + 支付方式）
     * @return 充值后的最新资料（含新余额），前端直接拿去刷新显示
     */
    public AccountProfile recharge(Long accountId, String accountType, RechargeRequest request) {

        // 1. 管理员账号没有余额（admin 表没这个字段），不允许充值
        if (ACCOUNT_ADMIN.equals(accountType)) {
            throw new BusinessException("管理员账号不支持充值");
        }

        // 2. 金额校验（第二道关：第一道是 Controller 的 @Valid）
        Double amount = request.getAmount();
        if (amount == null || amount <= 0) {
            throw new BusinessException("充值金额必须大于 0");
        }
        if (amount > 10000) {
            throw new BusinessException("单次充值不能超过 10000 元");
        }

        // 3. 确认用户真的存在
        User user = userMapper.findById(accountId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 4. 原子加钱：UPDATE user SET account = account + ? WHERE id = ?
        userMapper.addAccount(accountId, amount);

        // 5. 记审计日志（try-catch：钱已到账，日志挂了不能让用户以为失败又充一次）
        try {
            sysLogService.log(user.getId(), user.getUsername(), "RECHARGE", "用户认证", "账户充值",
                    String.format("充值 %.2f 元（%s）", amount,
                            request.getType() == null ? "未指定" : request.getType()));
        } catch (Exception ignored) {
            // 日志表出问题也不能拦住用户充钱
        }

        // 6. 重查一次：addAccount 只改了数据库，内存里的 user 还是旧值
        return toUserProfile(userMapper.findById(accountId));
    }

    private LoginResponse buildAdminLoginResponse(Admin admin) {
        String token = jwtUtils.generateToken(admin.getId(), admin.getUsername(), ACCOUNT_ADMIN, ACCOUNT_ADMIN);
        return new LoginResponse(
                token,
                admin.getUsername(),
                admin.getNickname(),
                ACCOUNT_ADMIN,
                admin.getAvatar(),
                admin.getId(),
                ACCOUNT_ADMIN
        );
    }

    private LoginResponse buildUserLoginResponse(User user) {
        String role = StringUtils.hasText(user.getRole()) ? user.getRole() : ACCOUNT_USER;
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), role, ACCOUNT_USER);
        return new LoginResponse(
                token,
                user.getUsername(),
                user.getNickname(),
                role,
                user.getAvatar(),
                user.getId(),
                ACCOUNT_USER
        );
    }

    private AccountProfile toAdminProfile(Admin admin) {
        AccountProfile profile = new AccountProfile();
        profile.setId(admin.getId());
        profile.setUsername(admin.getUsername());
        profile.setNickname(admin.getNickname());
        profile.setAvatar(admin.getAvatar());
        profile.setPhone(admin.getPhone());
        profile.setRole(ACCOUNT_ADMIN);
        profile.setAccountType(ACCOUNT_ADMIN);
        profile.setStatus(admin.getStatus());
        profile.setCreatedAt(admin.getCreatedAt());
        return profile;
    }

    private AccountProfile toUserProfile(User user) {
        AccountProfile profile = new AccountProfile();
        profile.setId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setNickname(user.getNickname());
        profile.setAvatar(user.getAvatar());
        profile.setPhone(user.getPhone());
        profile.setRole(user.getRole());
        profile.setAccountType(ACCOUNT_USER);
        profile.setStatus(user.getStatus());
        profile.setCreatedAt(user.getCreatedAt());
        profile.setAccount(user.getAccount());
        return profile;
    }
}
