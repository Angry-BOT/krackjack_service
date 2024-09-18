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

@Component
public class InterviewWebSocketHandler extends BinaryWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(InterviewWebSocketHandler.class);

    private final SpeechToTextService speechToTextService;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;
    private final Map<WebSocketSession, String> sessionContexts = new ConcurrentHashMap<>();

    public InterviewWebSocketHandler(SpeechToTextService speechToTextService,
                                     GeminiService geminiService,
                                     ObjectMapper objectMapper) {
        this.speechToTextService = speechToTextService;
        this.geminiService = geminiService;
        this.objectMapper = objectMapper;
        logger.info("InterviewWebSocketHandler initialized");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            logger.info("Received text message from session: {}", session.getId());
            Map<String, String> setupData = objectMapper.readValue(message.getPayload(), Map.class);
            if ("setup".equals(setupData.get("type"))) {
                String context = setupData.get("jobDescription") + " | " + setupData.get("intervieweeBackground");
                sessionContexts.put(session, context);
                logger.info("Setup completed for session: {}", session.getId());
                session.sendMessage(new TextMessage("{\"type\":\"setup_complete\"}"));
                logger.debug("Sent setup_complete message to session: {}", session.getId());
            }
        } catch (IOException e) {
            logger.error("Error handling text message for session: {}", session.getId(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws IOException {
        logger.info("Received binary message from session: {}", session.getId());
        byte[] audioData = message.getPayload().array();
        logger.debug("Audio data size: {} bytes", audioData.length);

        String transcription = speechToTextService.transcribe(audioData);
        logger.info("Transcription completed for session: {}", session.getId());

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                Map.of("type", "transcription", "content", transcription)
        )));
        logger.debug("Sent transcription to session: {}", session.getId());

        String context = sessionContexts.get(session);
        String geminiResponse = geminiService.generateResponse(transcription, context);
        logger.info("Gemini response generated for session: {}", session.getId());

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                Map.of("type", "assistant_response", "content", geminiResponse)
        )));
        logger.debug("Sent assistant response to session: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        sessionContexts.remove(session);
        logger.info("WebSocket connection closed for session: {}. Status: {}", session.getId(), status);
    }
}