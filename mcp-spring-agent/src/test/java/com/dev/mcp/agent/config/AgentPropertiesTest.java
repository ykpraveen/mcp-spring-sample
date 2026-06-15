package com.dev.mcp.agent.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentPropertiesTest {

    @Test
    void geminiTimeoutDefaultsToThreeMinutes() {
        AgentProperties properties = new AgentProperties();

        assertEquals(Duration.ofMinutes(3), properties.getGeminiTimeout());
    }
}
