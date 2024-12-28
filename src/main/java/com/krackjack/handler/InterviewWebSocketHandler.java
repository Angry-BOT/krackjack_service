package com.krackjack.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krackjack.services.SpeechToTextService;
import com.krackjack.services.GeminiService;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.krackjack.entity.ChatSession;
import com.krackjack.repository.ChatSessionRepository;
import com.krackjack.entity.ChatMessage;
import com.krackjack.repository.ChatMessageRepository;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.task.TaskExecutor;

@Component
@Transactional
public class InterviewWebSocketHandler extends BinaryWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(InterviewWebSocketHandler.class);

    private final SpeechToTextService speechToTextService;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final TaskExecutor webSocketTaskExecutor;
    private final Map<WebSocketSession, String> sessionContexts = new ConcurrentHashMap<>();

    public InterviewWebSocketHandler(SpeechToTextService speechToTextService,
            GeminiService geminiService,
            ObjectMapper objectMapper,
            ChatSessionRepository chatSessionRepository,
            ChatMessageRepository chatMessageRepository,
            TaskExecutor webSocketTaskExecutor) {
        this.speechToTextService = speechToTextService;
        this.geminiService = geminiService;
        this.objectMapper = objectMapper;
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.webSocketTaskExecutor = webSocketTaskExecutor;
        logger.info("InterviewWebSocketHandler initialized");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        webSocketTaskExecutor.execute(() -> {
            try {
                Map<String, String> setupData = objectMapper.readValue(message.getPayload(), Map.class);
                if ("setup".equals(setupData.get("type"))) {
                    ChatSession chatSession = new ChatSession();
                    chatSession.setCreatedAt(LocalDateTime.now());
                    chatSession.setJobDescription(setupData.get("jobDescription"));
                    chatSession.setIntervieweeBackground(setupData.get("intervieweeBackground"));
                    chatSessionRepository.save(chatSession);

                    session.getAttributes().put("sessionId", chatSession.getSessionId());
                }
            } catch (Exception e) {
                logger.error("Error in handleTextMessage", e);
            }
        });
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        webSocketTaskExecutor.execute(() -> {
            try {
                String sessionId = (String) session.getAttributes().get("sessionId");
                ChatSession chatSession = chatSessionRepository.findById(sessionId)
                        .orElseThrow(() -> new RuntimeException("Session not found"));

                // Process audio and save question
                String transcription = speechToTextService.transcribe(message.getPayload().array());

                // Send transcription to frontend first
                sendMessageToClient(session, "transcription", transcription);

                ChatMessage questionMessage = new ChatMessage();
                questionMessage.setChatSession(chatSession);
                questionMessage.setContent(transcription);
                questionMessage.setMessageType("QUESTION");
                questionMessage.setTimestamp(LocalDateTime.now());
                chatMessageRepository.save(questionMessage);

                // Generate and save response
                String response = geminiService.generateResponse(transcription,
                        chatSession.getJobDescription() + " | " + chatSession.getIntervieweeBackground());
                ChatMessage answerMessage = new ChatMessage();
                answerMessage.setChatSession(chatSession);
                answerMessage.setContent(response);
                answerMessage.setMessageType("ANSWER");
                answerMessage.setTimestamp(LocalDateTime.now());
                chatMessageRepository.save(answerMessage);

                // Send response back to client
                sendMessageToClient(session, "assistant_response", response);
            } catch (Exception e) {
                logger.error("Error in handleBinaryMessage", e);
                // Send error message to frontend
                sendMessageToClient(session, "error", "Failed to process audio message");
            }
        });
    }

    private void sendMessageToClient(WebSocketSession session, String type, String content) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                    Map.of("type", type, "content", content))));
            logger.debug("Sent {} to session: {}", type, session.getId());
        } catch (IOException e) {
            logger.error("Failed to send {} to client for session: {}", type, session.getId(), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        sessionContexts.remove(session);
        logger.info("WebSocket connection closed for session: {}. Status: {}", session.getId(), status);
    }
}