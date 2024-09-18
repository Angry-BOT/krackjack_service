package com.krackjack.services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@ComponentScan(basePackages = {"com.krackjack.services", "com.krackjack.config", "com.krackjack.handler"})
public class KrackjackServicesApplication {

	private static final Logger logger = LoggerFactory.getLogger(KrackjackServicesApplication.class);

	public static void main(String[] args) {
		logger.info("Starting KrackjackServicesApplication");
		SpringApplication.run(KrackjackServicesApplication.class, args);
		logger.info("KrackjackServicesApplication started successfully");
	}
}