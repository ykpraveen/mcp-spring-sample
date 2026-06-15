package com.dev.mcp.server.weather.service;

import com.dev.mcp.server.weather.config.WeatherProperties;
import io.micrometer.core.instrument.Timer;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class BrightSkyWeatherService {

    private final WeatherProperties properties;
    private final ObjectMapper objectMapper;
    private final WeatherMetricsService metricsService;
    private final HttpClient httpClient;

    @Autowired
    public BrightSkyWeatherService(
            WeatherProperties properties,
            ObjectMapper objectMapper,
            WeatherMetricsService metricsService
    ) {
        this(properties, objectMapper, metricsService, HttpClient.newHttpClient());
    }

    BrightSkyWeatherService(
            WeatherProperties properties,
            ObjectMapper objectMapper,
            WeatherMetricsService metricsService,
            HttpClient httpClient
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.metricsService = metricsService;
        this.httpClient = httpClient;
    }

    public Map<String, Object> fetchByStationId(String stationId) {
        return executeRequest("dwd_station_id=" + encode(stationId));
    }

    public Map<String, Object> fetchByCoordinates(double lat, double lon) {
        return executeRequest("lat=" + lat + "&lon=" + lon);
    }

    private Map<String, Object> executeRequest(String query) {
        var sample = metricsService.start();
        var uri = buildUri(query);
        var request = HttpRequest.newBuilder(uri)
                .GET()
                .timeout(Duration.ofSeconds(20))
                .build();

        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() > 299) {
                metricsService.recordFailure(sample);
                throw new WeatherToolException(
                        "Bright Sky returned non-success status",
                        errorBody("UPSTREAM_ERROR", "Bright Sky returned HTTP " + response.statusCode(), response.statusCode(), uri)
                );
            }

            Map<String, Object> parsed = objectMapper.readValue(response.body(), new TypeReference<>() {
            });
            metricsService.recordSuccess(sample);
            return parsed;
        } catch (InterruptedException ex) {
            metricsService.recordFailure(sample);
            Thread.currentThread().interrupt();
            throw new WeatherToolException(
                    "Failed to call Bright Sky API",
                    errorBody("UPSTREAM_CALL_FAILED", ex.getMessage(), 502, uri)
            );
        } catch (IOException ex) {
            metricsService.recordFailure(sample);
            throw new WeatherToolException(
                "Failed to call Bright Sky API",
                errorBody("UPSTREAM_CALL_FAILED", ex.getMessage(), 502, uri)
            );
        }
    }

    private URI buildUri(String query) {
        var base = properties.getBrightsky().getApiUrl();
        var separator = base.contains("?") ? "&" : "?";
        return URI.create(base + separator + query);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static Map<String, Object> errorBody(String code, String message, int status, URI uri) {
        var error = new LinkedHashMap<String, Object>();
        error.put("success", false);
        error.put("error_code", code);
        error.put("error_message", message);
        error.put("status", status);
        error.put("source", "brightsky");
        error.put("request_uri", uri.toString());
        return error;
    }
}
