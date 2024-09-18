package com.krackjack.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SpeechToTextService {

    private static final Logger logger = LoggerFactory.getLogger(SpeechToTextService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    private final RestTemplate restTemplate;

    public SpeechToTextService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String transcribe(byte[] audioData) {
        logger.info("Transcribing audio data of size: {} bytes", audioData.length);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        JSONObject requestBody = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        content.put("role", "user");
        JSONArray parts = new JSONArray();
        JSONObject textPart = new JSONObject();
        textPart.put("text", "Transcribe the following audio");
        JSONObject audioPart = new JSONObject();
        audioPart.put("inline_data", new JSONObject()
                .put("mime_type", "audio/wav")
                .put("data", Base64.getEncoder().encodeToString(audioData)));
        parts.put(textPart);
        parts.put(audioPart);
        content.put("parts", parts);
        contents.put(content);
        requestBody.put("contents", contents);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

        logger.debug("Sending request to Gemini API for transcription");

        String response = restTemplate.postForObject(GEMINI_API_URL, request, String.class);
        logger.debug("Received response from Gemini API: {}", response);

        JSONObject jsonResponse = new JSONObject(response);

        String transcription = jsonResponse.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");

        logger.info("Transcription result: {}", transcription);
        return transcription;
    }
}