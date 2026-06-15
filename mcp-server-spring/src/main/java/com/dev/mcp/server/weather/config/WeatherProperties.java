package com.dev.mcp.server.weather.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "weather")
public class WeatherProperties {

    private final BrightSky brightsky = new BrightSky();
    private final Cache cache = new Cache();

    public BrightSky getBrightsky() {
        return brightsky;
    }

    public Cache getCache() {
        return cache;
    }

    public static class BrightSky {
        private String apiUrl = "https://api.brightsky.dev/current_weather";

        public String getApiUrl() {
            return apiUrl;
        }

        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }
    }

    public static class Cache {
        private boolean enabled = true;
        private Duration ttl = Duration.ofMinutes(5);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }
    }
}
