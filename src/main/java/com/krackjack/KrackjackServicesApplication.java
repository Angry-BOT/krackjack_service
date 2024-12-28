package com.krackjack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.krackjack")
@EnableJpaRepositories("com.krackjack.repository")
@EntityScan("com.krackjack.entity")
public class KrackjackServicesApplication {
    public static void main(String[] args) {
        SpringApplication.run(KrackjackServicesApplication.class, args);
    }
}