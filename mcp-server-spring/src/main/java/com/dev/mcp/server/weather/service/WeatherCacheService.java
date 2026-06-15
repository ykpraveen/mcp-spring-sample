package com.dev.mcp.server.weather.service;

import com.dev.mcp.server.weather.config.WeatherProperties;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WeatherCacheService {

    private final Map<String, CacheEntry> stationCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry> coordinateCache = new ConcurrentHashMap<>();
    private final WeatherProperties properties;
    private final Clock clock;

    @Autowired
    public WeatherCacheService(WeatherProperties properties) {
        this(properties, Clock.systemUTC());
    }

    WeatherCacheService(WeatherProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public Map<String, Object> getStation(String stationId) {
        return get(stationCache, stationKey(stationId));
    }

    public void putStation(String stationId, Map<String, Object> payload) {
        put(stationCache, stationKey(stationId), payload);
    }

    public Map<String, Object> getCoordinates(double lat, double lon) {
        return get(coordinateCache, coordinateKey(lat, lon));
    }

    public void putCoordinates(double lat, double lon, Map<String, Object> payload) {
        put(coordinateCache, coordinateKey(lat, lon), payload);
    }

    private Map<String, Object> get(Map<String, CacheEntry> cache, String key) {
        if (!properties.getCache().isEnabled()) {
            return null;
        }
        var entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.expiresAt().isBefore(Instant.now(clock))) {
            cache.remove(key);
            return null;
        }
        return entry.payload();
    }

    private void put(Map<String, CacheEntry> cache, String key, Map<String, Object> payload) {
        if (!properties.getCache().isEnabled()) {
            return;
        }
        var expiresAt = Instant.now(clock).plus(properties.getCache().getTtl());
        cache.put(key, new CacheEntry(payload, expiresAt));
    }

    private static String stationKey(String stationId) {
        return "station:" + stationId;
    }

    private static String coordinateKey(double lat, double lon) {
        return "coord:%.6f:%.6f".formatted(lat, lon);
    }

    private record CacheEntry(Map<String, Object> payload, Instant expiresAt) {
    }
}
