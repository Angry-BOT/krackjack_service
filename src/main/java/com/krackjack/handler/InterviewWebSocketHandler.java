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

    public String setContext(WebSocketSession session, String jobDescription, String intervieweeBackground) {
        logger.info("Setting context for session: {}", session.getId());
        String context = String.format("Job Description: %s | Interviewee Background: %s", jobDescription,
                intervieweeBackground);
        try {
            sessionContexts.put(session, context);
            session.sendMessage(new TextMessage("{\"type\":\"context_set\"}"));
            logger.info("Context set successfully for session: {}", session.getId());
        } catch (IOException e) {
            logger.error("Error setting context for session: {}", session.getId(), e);
            throw new RuntimeException("Failed to set context", e);
        }
        return context;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            logger.info("Received text message from session: {}", session.getId());
            Map<String, String> setupData = objectMapper.readValue(message.getPayload(), Map.class);
            if ("setup".equals(setupData.get("type"))) {
                setContext(session, setupData.get("jobDescription"), setupData.get("intervieweeBackground"));
                logger.info("Setup completed for session: {}", session.getId());
            }
        } catch (IOException e) {
            logger.error("Error handling text message for session: {}", session.getId(), e);
            try {
                session.sendMessage(
                        new TextMessage("{\"type\":\"error\",\"message\":\"Failed to process setup message\"}"));
            } catch (IOException sendError) {
                logger.error("Failed to send error message to client", sendError);
            }
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        try {
            logger.info("Received binary message from session: {}", session.getId());
            byte[] audioData = message.getPayload().array();
            logger.debug("Audio data size: {} bytes", audioData.length);

            String transcription = speechToTextService.transcribe(audioData);
            logger.info("Transcription completed for session: {}", session.getId());

            if (!speechToTextService.isValidTranscription(transcription)) {
                sendMessageToClient(session, "error", "Could not process audio due to disturbance or silence");
                return;
            }

            sendMessageToClient(session, "transcription", transcription);

            String context = sessionContexts.get(session);
            if (context == null) {
                logger.warn("No context found for session: {}. Using empty context.", session.getId());
                context = "";
            }
            String geminiResponse = geminiService.generateResponse(transcription, context);
            logger.info("Gemini response generated for session: {}", session.getId());

            sendMessageToClient(session, "assistant_response", geminiResponse);
        } catch (Exception e) {
            logger.error("Error processing binary message for session: {}", session.getId(), e);
            try {
                session.sendMessage(
                        new TextMessage("{\"type\":\"error\",\"message\":\"Failed to process audio data\"}"));
            } catch (IOException sendError) {
                logger.error("Failed to send error message to client", sendError);
            }
        }
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