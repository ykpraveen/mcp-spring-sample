package com.dev.mcp.server.weather.service;

import java.util.Map;

public class WeatherToolException extends RuntimeException {

    private final Map<String, Object> errorBody;

    public WeatherToolException(String message, Map<String, Object> errorBody) {
        super(message);
        this.errorBody = errorBody;
    }

    public Map<String, Object> getErrorBody() {
        return errorBody;
    }
}
