package com.krackjack.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.context.annotation.Bean;
import com.krackjack.handler.InterviewWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    private final InterviewWebSocketHandler interviewWebSocketHandler;

    public WebSocketConfig(InterviewWebSocketHandler interviewWebSocketHandler) {
        this.interviewWebSocketHandler = interviewWebSocketHandler;
        logger.info("WebSocketConfig initialized with InterviewWebSocketHandler");
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        logger.info("Registering WebSocket handler for /interview endpoint");
        registry.addHandler(interviewWebSocketHandler, "/interview").setAllowedOrigins("*");
        logger.info("WebSocket handler registered successfully");
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(64 * 1024); // 64KB
        container.setMaxBinaryMessageBufferSize(1024 * 1024); // 1MB
        logger.info("WebSocket container configured with increased buffer sizes");
        return container;
    }
}