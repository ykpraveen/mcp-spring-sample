package com.dev.mcp.agent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.ai.model.chat=google-genai",
        "spring.ai.mcp.client.enabled=false",
        "spring.ai.google.genai.api-key=test-key",
        "agent.mcp-api-key=test-api-key"
})
class McpSpringAgentApplicationTests {

    @Test
    void contextLoads() {
    }
}
