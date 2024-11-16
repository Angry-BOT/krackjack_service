package com.krackjack.config;

import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.io.File;

@Configuration
public class LoggingConfig {

        @PostConstruct
        public void init() {
                // Create logs directory if it doesn't exist
                File logsDir = new File("logs");
                if (!logsDir.exists()) {
                        logsDir.mkdirs();
                }

                File archivedDir = new File("logs/archived");
                if (!archivedDir.exists()) {
                        archivedDir.mkdirs();
                }
        }
}