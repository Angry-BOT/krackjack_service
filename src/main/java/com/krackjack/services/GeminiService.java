package com.krackjack.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.json.JSONObject;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";

    @Autowired
    private RestTemplate restTemplate;

    public String generateResponse(String transcription, String context) {
        logger.info("Generating response for transcription: {}", transcription);
        logger.debug("Context: {}", context);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        JSONObject requestBody = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        content.put("role", "user");
        content.put("parts", new JSONArray().put(new JSONObject().put("text",
                String.format("Context: %s\n\nUser: %s\n\nAssistant:", context, transcription))));
        contents.put(content);
        requestBody.put("contents", contents);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

        logger.debug("Sending request to Gemini API: {}", requestBody);

        String response = restTemplate.postForObject(GEMINI_API_URL, request, String.class);
        logger.debug("Received response from Gemini API: {}", response);

        JSONObject jsonResponse = new JSONObject(response);

        String generatedText = jsonResponse.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");

        logger.info("Generated response: {}", generatedText);
        return generatedText;
    }
}