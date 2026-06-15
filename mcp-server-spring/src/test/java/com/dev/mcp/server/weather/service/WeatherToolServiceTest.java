package com.dev.mcp.server.weather.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeatherToolServiceTest {

    @Mock
    private BrightSkyWeatherService brightSkyWeatherService;

    @Mock
    private WeatherCacheService cacheService;

    @InjectMocks
    private WeatherToolService weatherToolService;

    @Test
    void returnsInputErrorWhenStationAndCoordinatesAreProvidedTogether() {
        var result = weatherToolService.getWeather("1048", 52.52, 13.405);

        assertFalse((Boolean) result.get("success"));
        assertEquals("INVALID_INPUT", result.get("error_code"));
        verify(brightSkyWeatherService, never()).fetchByStationId("1048");
    }

    @Test
    void returnsCachedStationWeatherWhenPresent() {
        var cached = Map.<String, Object>of("weather", "cached");
        when(cacheService.getStation("1048")).thenReturn(cached);

        var result = weatherToolService.getWeather("1048", null, null);

        assertTrue((Boolean) result.get("success"));
        assertEquals("HIT", result.get("cache"));
        assertEquals("station", result.get("lookup_mode"));
        verify(brightSkyWeatherService, never()).fetchByStationId("1048");
    }
}
