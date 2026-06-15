package com.dev.mcp.agent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DirectGeminiChatService {

    private static final Logger log = LoggerFactory.getLogger(DirectGeminiChatService.class);

    private final RestClient restClient;
    private final SessionMemoryStore memoryStore;
    private final String geminiApiKey;
    private final String geminiModel;

    public DirectGeminiChatService(SessionMemoryStore memoryStore,
                                   @Value("${spring.ai.google.genai.api-key:}") String geminiApiKey,
                                   @Value("${spring.ai.google.genai.chat.model:gemini-3.5-flash}") String geminiModel) {
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
        this.memoryStore = memoryStore;
        this.geminiApiKey = geminiApiKey;
        this.geminiModel = geminiModel;
    }

    public String reply(String sessionId, String userMessage) {
        if (!StringUtils.hasText(geminiApiKey)) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "GEMINI_API_KEY is not configured.");
        }

        List<SessionMemoryStore.ConversationTurn> history = memoryStore.history(sessionId);
        String prompt = buildPrompt(history, userMessage);

        try {
            GeminiResponse response = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent", geminiModel)
                    .header("x-goog-api-key", geminiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new GeminiRequest(List.of(new GeminiContent(List.of(new GeminiPart(prompt))))))
                    .retrieve()
                    .body(GeminiResponse.class);

            if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini returned no candidates.");
            }

            GeminiCandidate first = response.candidates().getFirst();
            if (first.content() == null || first.content().parts() == null || first.content().parts().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini returned an empty response.");
            }

            String text = first.content().parts().stream()
                    .map(GeminiPart::text)
                    .filter(StringUtils::hasText)
                    .findFirst()
                    .orElse(null);

            if (!StringUtils.hasText(text)) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini returned no text content.");
            }

            memoryStore.appendTurn(sessionId, userMessage, text);
            return text;
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Direct Gemini request failed: {}", ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Gemini request failed: " + ex.getMessage());
        }
    }

    private String buildPrompt(List<SessionMemoryStore.ConversationTurn> history, String userMessage) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a helpful chat agent.\n");
        if (!history.isEmpty()) {
            prompt.append("\nConversation history:\n");
            for (SessionMemoryStore.ConversationTurn turn : history) {
                prompt.append("User: ").append(turn.userMessage()).append('\n');
                prompt.append("Assistant: ").append(turn.assistantMessage()).append('\n');
            }
        }
        prompt.append("\nUser: ").append(userMessage);
        return prompt.toString();
    }

    private record GeminiRequest(List<GeminiContent> contents) {
    }

    private record GeminiResponse(List<GeminiCandidate> candidates) {
    }

    private record GeminiCandidate(GeminiContent content) {
    }

    private record GeminiContent(List<GeminiPart> parts) {
    }

    private record GeminiPart(String text) {
    }
}
