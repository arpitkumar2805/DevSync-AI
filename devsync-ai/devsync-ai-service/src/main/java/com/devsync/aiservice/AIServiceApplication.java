package com.devsync.aiservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {"com.devsync.aiservice", "com.devsync.common"})
@EnableDiscoveryClient
@EnableJpaAuditing
public class AIServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AIServiceApplication.class, args);
    }
}

