package com.dev.mcp.agent;

import com.dev.mcp.agent.config.AgentProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.util.StringUtils;

@SpringBootApplication
@EnableConfigurationProperties(AgentProperties.class)
public class McpSpringAgentApplication {

    private static final Logger log = LoggerFactory.getLogger(McpSpringAgentApplication.class);

    @Value("${spring.ai.google.genai.api-key:}")
    private String geminiApiKey;

    public static void main(String[] args) {
        SpringApplication.run(McpSpringAgentApplication.class, args);
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onStartup() {
        if (!StringUtils.hasText(geminiApiKey)) {
            log.error("===================================================");
            log.error("GEMINI_API_KEY is not set! Requests will fail.");
            log.error("Set it in mcp-spring-agent/.env: GEMINI_API_KEY=...");
            log.error("===================================================");
        } else {
            log.info("GEMINI_API_KEY loaded (starts with: {}...)", geminiApiKey.substring(0, Math.min(6, geminiApiKey.length())));
        }
    }
}
