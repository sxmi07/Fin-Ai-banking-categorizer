package com.finai.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finai.ai.prompt.CategorizationPromptTemplate;
import com.finai.dto.CategorizationResultDTO;
import com.finai.model.TransactionCategory;
import com.finai.model.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

/**
 * Cloud LLM Service interacting with Google Gemini API using structured JSON output.
 * Conforms to Section 6 and Section 7 of the project specification.
 */
@Service
public class GeminiAiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiService.class);
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={key}";

    @Value("${finai.ai.gemini.api-key:}")
    private String apiKey;

    @Value("${finai.ai.gemini.model:gemini-1.5-flash}")
    private String modelName;

    private final RestClient restClient;
    private final CategorizationPromptTemplate promptTemplate;
    private final ObjectMapper objectMapper;

    public GeminiAiService(CategorizationPromptTemplate promptTemplate, ObjectMapper objectMapper) {
        this.promptTemplate = promptTemplate;
        this.objectMapper = objectMapper;

        // Configure client with 12s connection and read timeouts
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(15000);

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    /**
     * Checks if the Gemini API key is configured.
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.equals("your_gemini_api_key_here");
    }

    /**
     * Categorize a single transaction using Gemini Cloud LLM.
     */
    public CategorizationResultDTO categorize(String rawDescription) {
        List<CategorizationResultDTO> results = categorizeBatch(List.of(rawDescription));
        if (!results.isEmpty()) {
            return results.get(0);
        }
        throw new IllegalStateException("No categorization result returned from Gemini API");
    }

    /**
     * Categorize a batch of transactions using Gemini Cloud LLM.
     */
    public List<CategorizationResultDTO> categorizeBatch(List<String> rawDescriptions) {
        if (!isConfigured()) {
            throw new IllegalStateException("Gemini API key is not configured.");
        }
        if (rawDescriptions == null || rawDescriptions.isEmpty()) {
            return Collections.emptyList();
        }

        String prompt = promptTemplate.buildBatchPrompt(rawDescriptions);
        log.info("Sending {} transaction(s) to Gemini Cloud AI ({})", rawDescriptions.size(), modelName);

        Map<String, Object> requestBody = buildGeminiRequestBody(prompt);

        try {
            String rawJsonResponse = restClient.post()
                    .uri(GEMINI_API_URL, modelName, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return parseGeminiResponse(rawJsonResponse, rawDescriptions);
        } catch (Exception ex) {
            log.error("Gemini API call failed: {}", ex.getMessage());
            throw new RuntimeException("Gemini API invocation failed: " + ex.getMessage(), ex);
        }
    }

    private Map<String, Object> buildGeminiRequestBody(String prompt) {
        Map<String, Object> root = new LinkedHashMap<>();

        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        root.put("contents", List.of(content));

        Map<String, Object> generationConfig = new LinkedHashMap<>();
        generationConfig.put("temperature", 0.1);
        generationConfig.put("response_mime_type", "application/json");
        root.put("generationConfig", generationConfig);

        return root;
    }

    private List<CategorizationResultDTO> parseGeminiResponse(String responseJson, List<String> originalDescriptions) {
        List<CategorizationResultDTO> results = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode candidates = root.path("candidates");
            if (candidates.isEmpty()) {
                throw new IllegalStateException("Gemini response contained no candidates");
            }

            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (parts.isEmpty()) {
                throw new IllegalStateException("Gemini response contained no text content parts");
            }

            String contentText = parts.get(0).path("text").asText();
            String cleanedJson = cleanJsonContent(contentText);

            List<Map<String, Object>> parsedList = objectMapper.readValue(cleanedJson, new TypeReference<>() {});

            for (int i = 0; i < parsedList.size(); i++) {
                Map<String, Object> item = parsedList.get(i);
                String original = (i < originalDescriptions.size()) ? originalDescriptions.get(i) :
                        String.valueOf(item.getOrDefault("originalDescription", ""));

                String merchant = String.valueOf(item.getOrDefault("merchantName", "Unknown"));
                String catStr = String.valueOf(item.getOrDefault("category", "Other"));
                String subcat = String.valueOf(item.getOrDefault("subcategory", "General"));
                String typeStr = String.valueOf(item.getOrDefault("transactionType", "DEBIT"));

                Double confidence = 0.90;
                Object confObj = item.get("confidence");
                if (confObj instanceof Number num) {
                    confidence = num.doubleValue();
                }

                String explanation = String.valueOf(item.getOrDefault("explanation", "Categorized by Gemini AI"));

                CategorizationResultDTO dto = new CategorizationResultDTO(
                        original,
                        merchant,
                        TransactionCategory.fromString(catStr),
                        subcat,
                        TransactionType.fromString(typeStr),
                        confidence,
                        explanation,
                        "Google Gemini (" + modelName + ")",
                        false
                );
                results.add(dto);
            }
        } catch (Exception e) {
            log.error("Failed to parse Gemini structured JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse Gemini structured response: " + e.getMessage(), e);
        }

        return results;
    }

    private String cleanJsonContent(String rawText) {
        String clean = rawText.trim();
        if (clean.startsWith("```json")) {
            clean = clean.substring(7);
        } else if (clean.startsWith("```")) {
            clean = clean.substring(3);
        }
        if (clean.endsWith("```")) {
            clean = clean.substring(0, clean.length() - 3);
        }
        return clean.trim();
    }
}
