package com.scaffold.controller;

import com.scaffold.common.AuthContext;
import com.scaffold.common.Result;
import com.scaffold.dto.AccountProfile;
import com.scaffold.dto.LoginRequest;
import com.scaffold.dto.LoginResponse;
import com.scaffold.dto.PasswordChangeRequest;
import com.scaffold.dto.ProfileUpdateRequest;
import com.scaffold.dto.RegisterRequest;
import com.scaffold.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @PostMapping("/auth/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(authService.register(request));
    }

    @GetMapping("/auth/me")
    public Result<AccountProfile> me(HttpServletRequest request) {
        Long accountId = AuthContext.getUserId(request);
        String accountType = AuthContext.getAccountType(request);
        return Result.ok(authService.getCurrentUser(accountId, accountType));
    }

    @PutMapping("/auth/profile")
    public Result<AccountProfile> updateProfile(HttpServletRequest request,
                                                @RequestBody ProfileUpdateRequest body) {
        Long accountId = AuthContext.getUserId(request);
        String accountType = AuthContext.getAccountType(request);
        return Result.ok(authService.updateProfile(accountId, accountType, body));
    }

    @PostMapping("/auth/password")
    public Result<Void> changePassword(HttpServletRequest request,
                                       @Valid @RequestBody PasswordChangeRequest body) {
        Long accountId = AuthContext.getUserId(request);
        String accountType = AuthContext.getAccountType(request);
        authService.changePassword(accountId, accountType, body);
        return Result.ok();
    }
}
