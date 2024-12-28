package com.krackjack.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.context.annotation.Bean;
import com.krackjack.handler.InterviewWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import com.krackjack.services.SpeechToTextService;
import com.krackjack.services.GeminiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krackjack.repository.ChatSessionRepository;
import com.krackjack.repository.ChatMessageRepository;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @Autowired
    private SpeechToTextService speechToTextService;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        logger.info("Registering WebSocket handler for /interview endpoint");
        registry.addHandler(interviewWebSocketHandler(), "/interview").setAllowedOrigins("*");
        logger.info("WebSocket handler registered successfully");
    }

    @Bean
    public InterviewWebSocketHandler interviewWebSocketHandler() {
        return new InterviewWebSocketHandler(
                speechToTextService,
                geminiService,
                objectMapper,
                chatSessionRepository,
                chatMessageRepository,
                webSocketTaskExecutor());
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(2097152);
        container.setMaxBinaryMessageBufferSize(2097152);
        container.setMaxSessionIdleTimeout(600000L);
        container.setAsyncSendTimeout(5000L);
        return container;
    }

    @Bean
    public TaskExecutor webSocketTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("WebSocket-");
        return executor;
    }
}