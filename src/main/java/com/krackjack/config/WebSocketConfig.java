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
        // Increase text buffer for larger JSON payloads
        container.setMaxTextMessageBufferSize(128 * 1024); // 128KB
        // Increase binary buffer for audio data
        container.setMaxBinaryMessageBufferSize(2048 * 1024); // 2MB
        // Add timeouts
        container.setAsyncSendTimeout(5000L);
        container.setMaxSessionIdleTimeout(600000L);
        return container;
    }
}