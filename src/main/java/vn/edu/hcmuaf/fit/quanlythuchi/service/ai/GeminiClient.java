package vn.edu.hcmuaf.fit.quanlythuchi.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.model}")
    private String model;

    @Value("${gemini.temperature:0.3}")
    private Double temperature;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiClient(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public String generateContent(String systemPrompt, String dataPrompt) {
        String url = apiUrl + "/" + model + ":generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Build the request body for Gemini API
        Map<String, Object> systemInstruction = new HashMap<>();
        systemInstruction.put("parts", List.of(Map.of("text", systemPrompt)));

        Map<String, Object> content = new HashMap<>();
        content.put("role", "user");
        content.put("parts", List.of(Map.of("text", dataPrompt)));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", temperature);
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.put("responseSchema", buildAIInsightResponseSchema());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("systemInstruction", systemInstruction);
        requestBody.put("contents", List.of(content));
        requestBody.put("generationConfig", generationConfig);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> firstCandidate = candidates.get(0);
                    Map<String, Object> contentMap = (Map<String, Object>) firstCandidate.get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
                    if (!parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error calling Gemini API: " + e.getMessage());
        }
        return null;
    }

    /**
     * Định nghĩa tường minh JSON schema khớp 1-1 với AIInsightResponseDTO.
     * Dùng responseSchema (structured output) thay vì chỉ dựa vào mô tả
     * bằng lời trong system prompt, để Gemini luôn trả đúng tên field,
     * đúng kiểu dữ liệu, tránh trường hợp field bị thiếu/null khi map
     * sang DTO (ví dụ cashFlowNarrative, cashFlowStatus bị rớt mất).
     */
    private Map<String, Object> buildAIInsightResponseSchema() {
        Map<String, Object> spendingSpikeSchema = new HashMap<>();
        spendingSpikeSchema.put("type", "OBJECT");
        spendingSpikeSchema.put("properties", Map.of(
                "category", Map.of("type", "STRING"),
                "overagePercent", Map.of("type", "NUMBER"),
                "comment", Map.of("type", "STRING")));
        spendingSpikeSchema.put("required", List.of("category", "overagePercent", "comment"));

        Map<String, Object> liquidityRiskSchema = new HashMap<>();
        liquidityRiskSchema.put("type", "OBJECT");
        liquidityRiskSchema.put("properties", Map.of(
                "hasRisk", Map.of("type", "BOOLEAN"),
                "message", Map.of("type", "STRING")));
        liquidityRiskSchema.put("required", List.of("hasRisk", "message"));

        Map<String, Object> cashFlowStatusSchema = new HashMap<>();
        cashFlowStatusSchema.put("type", "STRING");
        cashFlowStatusSchema.put("enum", List.of("HEALTHY", "WARNING", "CRITICAL"));

        Map<String, Object> spendingSpikesArraySchema = new HashMap<>();
        spendingSpikesArraySchema.put("type", "ARRAY");
        spendingSpikesArraySchema.put("items", spendingSpikeSchema);

        Map<String, Object> recommendationsArraySchema = new HashMap<>();
        recommendationsArraySchema.put("type", "ARRAY");
        recommendationsArraySchema.put("items", Map.of("type", "STRING"));

        Map<String, Object> rootProperties = new HashMap<>();
        rootProperties.put("cashFlowNarrative", Map.of("type", "STRING"));
        rootProperties.put("cashFlowStatus", cashFlowStatusSchema);
        rootProperties.put("spendingSpikes", spendingSpikesArraySchema);
        rootProperties.put("recommendations", recommendationsArraySchema);
        rootProperties.put("liquidityRisk", liquidityRiskSchema);

        Map<String, Object> rootSchema = new HashMap<>();
        rootSchema.put("type", "OBJECT");
        rootSchema.put("properties", rootProperties);
        rootSchema.put("required", List.of(
                "cashFlowNarrative", "cashFlowStatus", "spendingSpikes",
                "recommendations", "liquidityRisk"));

        return rootSchema;
    }
}