package com.scaffold.config;

import com.scaffold.service.SysLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SysLogSeedRunner implements ApplicationRunner {

    private final SysLogService sysLogService;

    @Override
    public void run(ApplicationArguments args) {
        sysLogService.ensureDemoLogs();
    }
}
