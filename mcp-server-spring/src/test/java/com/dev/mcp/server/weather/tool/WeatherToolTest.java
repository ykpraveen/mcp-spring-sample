package com.dev.mcp.server.weather.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.dev.mcp.server.weather.service.WeatherToolException;
import com.dev.mcp.server.weather.service.WeatherToolService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeatherToolTest {

    @Mock
    private WeatherToolService weatherToolService;

    @InjectMocks
    private WeatherTool weatherTool;

    @Test
    void returnsServiceResultOnSuccess() {
        when(weatherToolService.getWeather("1048", null, null)).thenReturn(Map.of("success", true));

        var result = weatherTool.getCurrentWeather("1048", null, null);

        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void returnsStructuredErrorOnKnownFailure() {
        var errorBody = Map.<String, Object>of("success", false, "error_code", "UPSTREAM_ERROR");
        when(weatherToolService.getWeather("1048", null, null)).thenThrow(new WeatherToolException("boom", errorBody));

        var result = weatherTool.getCurrentWeather("1048", null, null);

        assertFalse((Boolean) result.get("success"));
        assertEquals("UPSTREAM_ERROR", result.get("error_code"));
    }
}
