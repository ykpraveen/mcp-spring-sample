package com.dev.mcp.server.weather.tool;

import com.dev.mcp.server.weather.service.WeatherToolException;
import com.dev.mcp.server.weather.service.WeatherToolService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class WeatherTool {

    private final WeatherToolService weatherToolService;

    public WeatherTool(WeatherToolService weatherToolService) {
        this.weatherToolService = weatherToolService;
    }

    @McpTool(name = "get_current_weather", description = "Get current weather by dwd_station_id or by lat/lon")
    public Map<String, Object> getCurrentWeather(
            @McpToolParam(description = "DWD station ID", required = false) String dwd_station_id,
            @McpToolParam(description = "Latitude", required = false) Double lat,
            @McpToolParam(description = "Longitude", required = false) Double lon
    ) {
        try {
            return weatherToolService.getWeather(dwd_station_id, lat, lon);
        } catch (WeatherToolException ex) {
            return ex.getErrorBody();
        } catch (Exception ex) {
            var error = new LinkedHashMap<String, Object>();
            error.put("success", false);
            error.put("error_code", "INTERNAL_ERROR");
            error.put("error_message", ex.getMessage());
            return error;
        }
    }
}
