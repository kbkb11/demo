package com.scrapy.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class LlmReasonService {

    private final ObjectMapper objectMapper;

    @Value("${app.llm.enabled:false}")
    private boolean llmEnabled;

    @Value("${app.llm.proxy-url:http://localhost:9000/reason}")
    private String llmProxyUrl;

    @Value("${app.llm.prompt:请根据以下数据产出一句中文推荐理由。}")
    private String llmPrompt;

    public LlmReasonService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String buildRecommendationReason(Map<String, Object> context) {
        String fallback = fallbackReason(context);
        if (!llmEnabled || llmProxyUrl == null || llmProxyUrl.isBlank()) {
            return fallback;
        }
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            Map<String, Object> payload = new HashMap<>();
            payload.put("studentId", context.get("studentId"));
            payload.put("course", context.get("course"));
            payload.put("trend", context.get("trend"));
            payload.put("score", context.get("score"));
            payload.put("classAvg", context.get("classAvg"));
            payload.put("differenceWithClassAvg", context.get("differenceWithClassAvg"));
            Object override = context.get("promptOverride");
            payload.put("promptOverride", override == null ? llmPrompt : override);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(llmProxyUrl))
                    .timeout(Duration.ofSeconds(8))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return fallback;
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode reasonNode = root.path("reason");
            if (reasonNode.isMissingNode() || reasonNode.asText().isBlank()) {
                return fallback;
            }
            return reasonNode.asText().trim();
        } catch (Exception ex) {
            return fallback;
        }
    }

    private String fallbackReason(Map<String, Object> context) {
        String trend = String.valueOf(context.getOrDefault("trend", "稳定"));
        String course = String.valueOf(context.getOrDefault("course", "该科目"));
        Object diff = context.get("differenceWithClassAvg");
        double gap = diff instanceof Number ? ((Number) diff).doubleValue() : 0.0;
        if ("下降".equals(trend) || gap <= -8) {
            return "该生在" + course + "相对班级均值偏低且趋势走弱，建议优先补基础并做错题回顾。";
        }
        if ("上升".equals(trend) || gap >= 8) {
            return "该生在" + course + "表现持续提升，可安排进阶训练巩固优势。";
        }
        return "该生在" + course + "整体表现平稳，建议按周复盘保持节奏。";
    }
}
