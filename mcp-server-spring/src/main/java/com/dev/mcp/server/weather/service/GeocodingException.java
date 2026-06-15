package com.dev.mcp.server.weather.service;

import java.util.Map;

public class GeocodingException extends RuntimeException {

    private final Map<String, Object> errorBody;

    public GeocodingException(String message, Map<String, Object> errorBody) {
        super(message);
        this.errorBody = errorBody;
    }

    public Map<String, Object> getErrorBody() {
        return errorBody;
    }
}
