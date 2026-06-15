package com.dev.mcp.server.weather.service;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class WeatherToolService {

    private final BrightSkyWeatherService brightSkyWeatherService;
    private final WeatherCacheService cacheService;

    public WeatherToolService(BrightSkyWeatherService brightSkyWeatherService, WeatherCacheService cacheService) {
        this.brightSkyWeatherService = brightSkyWeatherService;
        this.cacheService = cacheService;
    }

    public Map<String, Object> getWeather(String dwdStationId, Double lat, Double lon) {
        boolean hasStation = dwdStationId != null && !dwdStationId.isBlank();
        boolean hasLatLon = lat != null || lon != null;

        if (hasStation && hasLatLon) {
            return inputError("Provide either dwd_station_id OR lat/lon, not both.");
        }
        if (!hasStation && (lat == null || lon == null)) {
            return inputError("Provide dwd_station_id or both lat and lon.");
        }

        if (hasStation) {
            var cached = cacheService.getStation(dwdStationId);
            if (cached != null) {
                return withMeta(cached, "HIT", "station");
            }
            var live = brightSkyWeatherService.fetchByStationId(dwdStationId);
            cacheService.putStation(dwdStationId, live);
            return withMeta(live, "MISS", "station");
        }

        var cached = cacheService.getCoordinates(lat, lon);
        if (cached != null) {
            return withMeta(cached, "HIT", "coordinates");
        }
        var live = brightSkyWeatherService.fetchByCoordinates(lat, lon);
        cacheService.putCoordinates(lat, lon, live);
        return withMeta(live, "MISS", "coordinates");
    }

    private static Map<String, Object> inputError(String message) {
        var error = new LinkedHashMap<String, Object>();
        error.put("success", false);
        error.put("error_code", "INVALID_INPUT");
        error.put("error_message", message);
        return error;
    }

    private static Map<String, Object> withMeta(Map<String, Object> body, String cacheStatus, String mode) {
        var result = new LinkedHashMap<String, Object>();
        result.put("success", true);
        result.put("cache", cacheStatus);
        result.put("lookup_mode", mode);
        result.put("source", "brightsky");
        result.put("data", body);
        return result;
    }
}
