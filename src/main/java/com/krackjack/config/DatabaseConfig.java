package com.krackjack.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class DatabaseConfig {

        private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

        @Bean
        CommandLineRunner checkDbConnection(DataSource dataSource) {
                return args -> {
                        logger.info("Database URL: " + dataSource.getConnection().getMetaData().getURL());
                        logger.info("Database Username: " + dataSource.getConnection().getMetaData().getUserName());
                        logger.info("Database Product Name: "
                                        + dataSource.getConnection().getMetaData().getDatabaseProductName());
                        logger.info("Database Product Version: "
                                        + dataSource.getConnection().getMetaData().getDatabaseProductVersion());
                };
        }
}