package com.finai.ai;

import com.finai.dto.CategorizationResultDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Primary AI Categorization Coordinator.
 * Orchestrates cloud LLM categorization (Google Gemini) and resilient rule-based heuristic fallback.
 * Conforms to Section 3, Section 6, and Section 18 of the project specification.
 */
@Service
public class AiCategorizationService {

    private static final Logger log = LoggerFactory.getLogger(AiCategorizationService.class);

    private final GeminiAiService geminiAiService;
    private final RuleBasedFallbackService fallbackService;

    public AiCategorizationService(GeminiAiService geminiAiService, RuleBasedFallbackService fallbackService) {
        this.geminiAiService = geminiAiService;
        this.fallbackService = fallbackService;
    }

    /**
     * Categorizes a single transaction description.
     * Automatically attempts Gemini Cloud AI first, falling back to rule-based engine on any error.
     */
    public CategorizationResultDTO categorize(String description) {
        if (description == null || description.trim().isEmpty()) {
            return fallbackService.categorize("");
        }

        if (geminiAiService.isConfigured()) {
            try {
                log.info("Attempting cloud AI categorization for: '{}'", description);
                return geminiAiService.categorize(description);
            } catch (Exception ex) {
                log.warn("Gemini Cloud AI failed ({}). Gracefully falling back to rule-based engine.", ex.getMessage());
                CategorizationResultDTO fallback = fallbackService.categorize(description);
                fallback.setExplanation(fallback.getExplanation() + " [AI unavailable: fallback applied]");
                fallback.setFallbackUsed(true);
                return fallback;
            }
        } else {
            log.debug("Gemini API key not configured. Using rule-based categorization (demo mode).");
            return fallbackService.categorize(description);
        }
    }

    /**
     * Categorizes a batch of transaction descriptions.
     * Tries Gemini batch API first; falls back to heuristic engine if unavailable.
     */
    public List<CategorizationResultDTO> categorizeBatch(List<String> descriptions) {
        if (descriptions == null || descriptions.isEmpty()) {
            return List.of();
        }

        if (geminiAiService.isConfigured()) {
            try {
                log.info("Attempting batch cloud AI categorization for {} descriptions", descriptions.size());
                return geminiAiService.categorizeBatch(descriptions);
            } catch (Exception ex) {
                log.warn("Gemini batch AI call failed ({}). Falling back to rule-based heuristic batch.", ex.getMessage());
                List<CategorizationResultDTO> fallbacks = fallbackService.categorizeBatch(descriptions);
                for (CategorizationResultDTO res : fallbacks) {
                    res.setExplanation(res.getExplanation() + " [AI unavailable: fallback applied]");
                    res.setFallbackUsed(true);
                }
                return fallbacks;
            }
        } else {
            log.debug("Gemini API key not configured. Using rule-based batch categorization.");
            return fallbackService.categorizeBatch(descriptions);
        }
    }

    /**
     * Check current AI status for settings / UI telemetry.
     */
    public boolean isGeminiConfigured() {
        return geminiAiService.isConfigured();
    }
}
