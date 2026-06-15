package com.dev.mcp.agent.service;

import com.dev.mcp.agent.config.AgentProperties;
import io.modelcontextprotocol.client.McpSyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ChatAgentService {

    private static final Logger log = LoggerFactory.getLogger(ChatAgentService.class);
    private static final String[] TOOL_KEYWORDS = {
            "weather", "temperature", "forecast", "rain", "wind", "humidity", "cloud", "snow"
    };

    private final ChatClient plainChatClient;
    private final ObjectProvider<ToolCallbackProvider> toolsProvider;
    private final ObjectProvider<List<McpSyncClient>> mcpClientsProvider;
    private final SessionMemoryStore memoryStore;
    private final AgentProperties properties;
    private final AtomicBoolean mcpInitialized = new AtomicBoolean(false);
    private volatile ChatClient toolEnabledChatClient;

    public ChatAgentService(ChatClient.Builder chatClientBuilder,
                            ObjectProvider<ToolCallbackProvider> toolsProvider,
                            ObjectProvider<List<McpSyncClient>> mcpClientsProvider,
                            SessionMemoryStore memoryStore,
                    AgentProperties properties) {
        this.plainChatClient = chatClientBuilder.build();
        this.toolsProvider = toolsProvider;
        this.mcpClientsProvider = mcpClientsProvider;
        this.memoryStore = memoryStore;
        this.properties = properties;
    }

    public String reply(String sessionId, String userMessage) {
        List<SessionMemoryStore.ConversationTurn> history = memoryStore.history(sessionId);
        String prompt = buildPrompt(history, userMessage);
        boolean toolRequest = shouldUseTools(userMessage);
        log.debug("Invoking Gemini model with {} prior turns, prompt length {} chars, tools={}",
                history.size(), prompt.length(), toolRequest);
        ChatClient client = toolRequest ? toolEnabledClient() : plainChatClient;
        String answer = invokeModel(client, prompt);
        memoryStore.appendTurn(sessionId, userMessage, answer);
        return answer;
    }

    private String invokeModel(ChatClient client, String prompt) {
        long start = System.currentTimeMillis();
        var executor = Executors.newVirtualThreadPerTaskExecutor();
        try {
            var future = executor.submit(() -> {
                log.debug("Invoking ChatClient with prompt, client hash: {}", client.hashCode());
                String result = client.prompt().user(prompt).call().content();
                log.info("ChatClient response received, content length: {}", result.length());
                return result;
            });
            long timeoutSeconds = properties.getGeminiTimeout().toSeconds();
            String content = future.get(timeoutSeconds, TimeUnit.SECONDS);
            log.info("Gemini responded in {}ms", System.currentTimeMillis() - start);
            return content;
        } catch (TimeoutException ex) {
            log.error("Gemini did not respond within {}s", properties.getGeminiTimeout().toSeconds());
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT,
                "Gemini request timed out after " + properties.getGeminiTimeout().toSeconds()
                    + " seconds. Check Gemini API key validity, model access, and outbound connectivity to generativelanguage.googleapis.com.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("Gemini request interrupted", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Request interrupted.");
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            log.error("Gemini request failed: {}", cause.getMessage(), cause);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini request failed: " + cause.getMessage());
        } finally {
            executor.shutdownNow(); // interrupt the Gemini call if still running (e.g. on timeout)
        }
    }

    private ChatClient toolEnabledClient() {
        initializeMcpClientsIfNeeded();

        ChatClient existing = toolEnabledChatClient;
        if (existing != null) {
            log.info("Returning cached tool-enabled client");
            return existing;
        }

        synchronized (this) {
            if (toolEnabledChatClient != null) {
                log.info("Returning synchronized cached tool-enabled client");
                return toolEnabledChatClient;
            }

            ToolCallbackProvider tools = toolsProvider.getIfAvailable();
            if (tools == null) {
                log.warn("No ToolCallbackProvider available, using plain client");
                toolEnabledChatClient = plainChatClient;
                return plainChatClient;
            }

            log.info("Building tool-enabled client with ToolCallbackProvider: {}", tools);
            toolEnabledChatClient = plainChatClient.mutate()
                    .defaultTools((Object[]) tools.getToolCallbacks())
                    .build();
            log.info("Tool-enabled client built successfully with {} tools", 
                    tools.getToolCallbacks() != null ? tools.getToolCallbacks().length : 0);
            return toolEnabledChatClient;
        }
    }

    private void initializeMcpClientsIfNeeded() {
        if (mcpInitialized.get()) {
            return;
        }
        if (!StringUtils.hasText(properties.getMcpApiKey())) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "MCP_API_KEY is required for tool-enabled prompts.");
        }

        synchronized (mcpInitialized) {
            if (mcpInitialized.get()) {
                return;
            }

            List<McpSyncClient> clients = mcpClientsProvider.getIfAvailable();
            if (clients == null || clients.isEmpty()) {
                log.warn("No MCP clients available");
                mcpInitialized.set(true);
                return;
            }

            try {
                log.info("Initializing MCP clients, count: {}", clients.size());
                for (McpSyncClient client : clients) {
                    log.info("Initializing MCP client: {}", client);
                    client.initialize();
                    log.info("MCP client initialized successfully");
                }
                mcpInitialized.set(true);
                log.info("All MCP clients initialized");
            } catch (RuntimeException ex) {
                log.error("MCP client initialization failed", ex);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "MCP client initialization failed. Check MCP_API_KEY and MCP server availability.");
            }
        }
    }

    private static boolean shouldUseTools(String userMessage) {
        String normalized = userMessage.toLowerCase(Locale.ROOT);
        for (String keyword : TOOL_KEYWORDS) {
            if (normalized.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String buildPrompt(List<SessionMemoryStore.ConversationTurn> history, String userMessage) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a helpful chat agent. Use available MCP tools when they are relevant.\n");
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
}
