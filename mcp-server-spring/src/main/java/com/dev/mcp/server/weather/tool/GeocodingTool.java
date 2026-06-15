package com.dev.mcp.server.weather.tool;

import com.dev.mcp.server.weather.service.GeocodingException;
import com.dev.mcp.server.weather.service.NominatimGeocodingService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class GeocodingTool {

    private final NominatimGeocodingService geocodingService;

    public GeocodingTool(NominatimGeocodingService geocodingService) {
        this.geocodingService = geocodingService;
    }

    @McpTool(name = "geocode_city", description = "Convert a city name to latitude and longitude using OpenStreetMap Nominatim")
    public Map<String, Object> geocodeCity(
            @McpToolParam(description = "City name (e.g., 'Berlin', 'New York')", required = true) String cityName
    ) {
        if (!StringUtils.hasText(cityName)) {
            var error = new LinkedHashMap<String, Object>();
            error.put("success", false);
            error.put("error_code", "INVALID_INPUT");
            error.put("error_message", "City name is required and cannot be empty");
            return error;
        }

        try {
            return geocodingService.geocodeCity(cityName);
        } catch (GeocodingException ex) {
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
