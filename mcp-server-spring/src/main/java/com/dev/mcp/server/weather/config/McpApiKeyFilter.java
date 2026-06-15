package com.dev.mcp.server.weather.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class McpApiKeyFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private final String expectedApiKey;

    public McpApiKeyFilter(McpSecurityProperties properties) {
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new IllegalStateException("MCP_API_KEY must be set for MCP server API key authentication.");
        }
        this.expectedApiKey = properties.getApiKey();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.startsWith("/actuator") || path.equals("/error")) {
            return true;
        }
        return !(path.startsWith("/sse") || path.startsWith("/mcp"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String suppliedApiKey = request.getHeader(API_KEY_HEADER);
        if (!expectedApiKey.equals(suppliedApiKey)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API key.");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
