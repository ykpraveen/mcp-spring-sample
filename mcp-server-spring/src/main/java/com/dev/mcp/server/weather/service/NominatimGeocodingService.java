package com.dev.mcp.server.weather.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NominatimGeocodingService {

    private static final String NOMINATIM_API = "https://nominatim.openstreetmap.org/search";
    private static final String USER_AGENT = "mcp-spring-sample/1.0.0 (GitHub Copilot MCP)";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Autowired
    public NominatimGeocodingService(ObjectMapper objectMapper) {
        this(objectMapper, HttpClient.newHttpClient());
    }

    NominatimGeocodingService(ObjectMapper objectMapper, HttpClient httpClient) {
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    public Map<String, Object> geocodeCity(String cityName) {
        try {
            var uri = buildUri(cityName);
            var request = HttpRequest.newBuilder(uri)
                    .GET()
                    .header("User-Agent", USER_AGENT)
                    .timeout(Duration.ofSeconds(10))
                    .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() > 299) {
                throw new GeocodingException(
                        "Nominatim returned HTTP " + response.statusCode(),
                        errorBody("UPSTREAM_ERROR", "Nominatim returned HTTP " + response.statusCode(), response.statusCode())
                );
            }

            List<Map<String, Object>> results = objectMapper.readValue(
                    response.body(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            if (results == null || results.isEmpty()) {
                throw new GeocodingException(
                        "City not found",
                        errorBody("NOT_FOUND", "No results found for city: " + cityName, 404)
                );
            }

            Map<String, Object> firstResult = results.get(0);
            var result = new LinkedHashMap<String, Object>();
            result.put("success", true);
            result.put("city", cityName);
            result.put("lat", Double.parseDouble(firstResult.get("lat").toString()));
            result.put("lon", Double.parseDouble(firstResult.get("lon").toString()));
            result.put("display_name", firstResult.get("display_name"));
            result.put("source", "nominatim");
            return result;
        } catch (GeocodingException ex) {
            return ex.getErrorBody();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return errorBody("UPSTREAM_CALL_FAILED", ex.getMessage(), 502);
        } catch (IOException ex) {
            return errorBody("UPSTREAM_CALL_FAILED", ex.getMessage(), 502);
        } catch (Exception ex) {
            return errorBody("INTERNAL_ERROR", ex.getMessage(), 500);
        }
    }

    private URI buildUri(String cityName) {
        var encoded = URLEncoder.encode(cityName, StandardCharsets.UTF_8);
        return URI.create(NOMINATIM_API + "?q=" + encoded + "&format=json&limit=1");
    }

    private static Map<String, Object> errorBody(String code, String message, int status) {
        var error = new LinkedHashMap<String, Object>();
        error.put("success", false);
        error.put("error_code", code);
        error.put("error_message", message);
        error.put("status", status);
        error.put("source", "nominatim");
        return error;
    }
}
