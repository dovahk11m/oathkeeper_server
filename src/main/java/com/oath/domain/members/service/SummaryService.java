package com.oath.domain.members.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.oath.domain.members.dto.AdminRequest;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SummaryService {

    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${huggingface.api.key}")
    private String apiKey;

    @Value("${huggingface.api.url}")
    private String apiUrl;  // 예: "https://api-inference.huggingface.co/models/facebook/bart-large-cnn"

    public SummaryService(CloseableHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public String summarizeChats(List<String> messages) {
        String result = "";

        HttpPost request = new HttpPost(apiUrl);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Authorization", "Bearer " + apiKey);

        try {
            // 메시지들을 하나의 문자열로 합치기
            String inputText = String.join("\n", messages);

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("inputs", inputText);

            request.setEntity(new StringEntity(objectMapper.writeValueAsString(requestBody)));

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                // Hugging Face 요약 응답은 [{"summary_text": "..."}] 형식
                JsonNode rootNode = objectMapper.readTree(responseBody);

                if (rootNode.isArray() && rootNode.size() > 0) {
                    JsonNode summaryNode = rootNode.get(0);
                    if (summaryNode.has("summary_text") && !summaryNode.get("summary_text").isNull()) {
                        result = summaryNode.get("summary_text").asText();
                    }
                }
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return result;
    }
}

