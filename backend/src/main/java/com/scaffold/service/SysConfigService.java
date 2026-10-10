package com.scaffold.service;

import com.scaffold.common.BusinessException;
import com.scaffold.entity.SysConfig;
import com.scaffold.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SysConfigService {

    public static final String KEY_SYSTEM_NAME = "system_name";
    public static final String KEY_SYSTEM_LOGO = "system_logo";
    public static final String DEFAULT_SYSTEM_NAME = "Admin Scaffold";

    private final SysConfigMapper sysConfigMapper;

    public Map<String, String> getPublicConfig() {
        Map<String, String> map = new HashMap<>();
        map.put("systemName", getSystemName());
        map.put("systemLogo", getSystemLogo());
        return map;
    }

    public String getSystemName() {
        SysConfig config = sysConfigMapper.findByKey(KEY_SYSTEM_NAME);
        if (config == null || !StringUtils.hasText(config.getConfigValue())) {
            return DEFAULT_SYSTEM_NAME;
        }
        return config.getConfigValue().trim();
    }

    public String getSystemLogo() {
        SysConfig config = sysConfigMapper.findByKey(KEY_SYSTEM_LOGO);
        if (config == null || !StringUtils.hasText(config.getConfigValue())) {
            return "";
        }
        return config.getConfigValue().trim();
    }

    public Map<String, String> updatePublicConfig(String systemName, String systemLogo) {
        if (!StringUtils.hasText(systemName)) {
            throw new BusinessException("系统名称不能为空");
        }
        String name = systemName.trim();
        if (name.length() > 40) {
            throw new BusinessException("系统名称不能超过 40 个字符");
        }
        upsert(KEY_SYSTEM_NAME, name, "系统名称");

        String logo = systemLogo == null ? "" : systemLogo.trim();
        if (logo.length() > 512) {
            throw new BusinessException("系统 Logo 地址过长");
        }
        upsert(KEY_SYSTEM_LOGO, logo, "系统 Logo");

        return getPublicConfig();
    }

    /** @deprecated use updatePublicConfig */
    public Map<String, String> updateSystemName(String systemName) {
        return updatePublicConfig(systemName, getSystemLogo());
    }

    private void upsert(String key, String value, String remark) {
        SysConfig existing = sysConfigMapper.findByKey(key);
        if (existing == null) {
            sysConfigMapper.insert(key, value, remark);
        } else {
            sysConfigMapper.updateValue(key, value, remark);
        }
    }
}
