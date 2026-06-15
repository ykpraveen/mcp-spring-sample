package com.dev.mcp.agent.config;

import com.dev.mcp.agent.service.SessionMemoryStore;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.client.transport.customizer.McpSyncHttpClientRequestCustomizer;
import org.springframework.ai.mcp.customizer.McpClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class AgentConfiguration {

    @Bean
    SessionMemoryStore sessionMemoryStore(AgentProperties properties) {
        return new SessionMemoryStore(properties.getMemoryMaxSessions(), properties.getMemoryMaxTurns());
    }

    @Bean
    McpClientCustomizer<HttpClientStreamableHttpTransport.Builder> streamableHttpTransportCustomizer(AgentProperties properties) {
        McpSyncHttpClientRequestCustomizer requestCustomizer =
                (builder, method, uri, body, context) -> {
                    if (StringUtils.hasText(properties.getMcpApiKey())) {
                        builder.header("X-API-Key", properties.getMcpApiKey());
                    }
                };
        return (name, builder) -> builder.httpRequestCustomizer(requestCustomizer);
    }
}
