package com.devsync.monolith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {"com.devsync.monolith", "com.devsync.common"})
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableCaching
@EnableAsync
public class DevSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevSyncApplication.class, args);
    }
}
