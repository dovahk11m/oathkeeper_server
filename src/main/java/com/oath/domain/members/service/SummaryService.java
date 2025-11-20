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
    private String apiUrl;

    public SummaryService(CloseableHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public String summarizeChats(List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return "요약할 채팅 내용이 없습니다.";
        }

        String inputText = String.join("\n", messages);

        if (inputText.isBlank()) {
            return "요약할 채팅 내용이 없습니다.";
        }

        // API가 처리할 수 있는 최대 길이를 1024자로 제한 (KoBART 모델 권장 길이)
        final int MAX_LENGTH = 1024;
        if (inputText.length() > MAX_LENGTH) {
            inputText = inputText.substring(0, MAX_LENGTH);
        }
        String result = "";

        HttpPost request = new HttpPost(apiUrl);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Authorization", "Bearer " + apiKey);

        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("inputs", inputText);

            request.setEntity(new StringEntity(objectMapper.writeValueAsString(requestBody)));

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int status = response.getCode(); // 5.x에서는 getCode() 사용
                String reason = response.getReasonPhrase(); // 상태 메시지
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (status != 200) {
                    // 200 OK가 아니면 JSON으로 파싱하지 않고 에러 처리
                    throw new RuntimeException("API 요청 실패: " + status + " / " + responseBody);
                }

                // Hugging Face 요약 응답은 [{"summary_text": "..."}] 형식
                JsonNode rootNode = objectMapper.readTree(responseBody);

                if (rootNode.isArray() && rootNode.size() > 0) {
                    JsonNode summaryNode = rootNode.get(0);
                    if (summaryNode.has("summary_text") && !summaryNode.get("summary_text").isNull()) {
                        result = summaryNode.get("summary_text").asText();
                    }
                }
                System.out.println("결과=================" + responseBody);
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return result;
    }
}
