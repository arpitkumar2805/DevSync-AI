package com.devsync.orguser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {"com.devsync.orguser", "com.devsync.common"})
@EnableDiscoveryClient
@EnableJpaAuditing
public class OrgUserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrgUserServiceApplication.class, args);
    }
}
