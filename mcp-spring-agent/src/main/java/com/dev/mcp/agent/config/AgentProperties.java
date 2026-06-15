package com.dev.mcp.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "agent")
public class AgentProperties {

    private int memoryMaxTurns = 20;
    private int memoryMaxSessions = 500;
    private Duration geminiTimeout = Duration.ofMinutes(3);
    private String mcpApiKey;

    public int getMemoryMaxTurns() {
        return memoryMaxTurns;
    }

    public void setMemoryMaxTurns(int memoryMaxTurns) {
        this.memoryMaxTurns = memoryMaxTurns;
    }

    public int getMemoryMaxSessions() {
        return memoryMaxSessions;
    }

    public void setMemoryMaxSessions(int memoryMaxSessions) {
        this.memoryMaxSessions = memoryMaxSessions;
    }

    public Duration getGeminiTimeout() {
        return geminiTimeout;
    }

    public void setGeminiTimeout(Duration geminiTimeout) {
        this.geminiTimeout = geminiTimeout;
    }

    public String getMcpApiKey() {
        return mcpApiKey;
    }

    public void setMcpApiKey(String mcpApiKey) {
        this.mcpApiKey = mcpApiKey;
    }
}
