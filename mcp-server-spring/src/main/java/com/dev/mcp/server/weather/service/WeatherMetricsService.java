package com.dev.mcp.server.weather.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class WeatherMetricsService {

    private final Counter totalCalls;
    private final Counter successCalls;
    private final Counter failedCalls;
    private final MeterRegistry meterRegistry;

    public WeatherMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.totalCalls = Counter.builder("weather.api.calls.total")
                .description("Total Bright Sky API calls")
                .register(meterRegistry);
        this.successCalls = Counter.builder("weather.api.calls.success")
                .description("Successful Bright Sky API calls")
                .register(meterRegistry);
        this.failedCalls = Counter.builder("weather.api.calls.failed")
                .description("Failed Bright Sky API calls")
                .register(meterRegistry);
    }

    public Timer.Sample start() {
        totalCalls.increment();
        return Timer.start(meterRegistry);
    }

    public void recordSuccess(Timer.Sample sample) {
        successCalls.increment();
        sample.stop(Timer.builder("weather.api.response.time")
                .description("Bright Sky API response time")
                .tag("status", "success")
                .register(meterRegistry));
    }

    public void recordFailure(Timer.Sample sample) {
        failedCalls.increment();
        sample.stop(Timer.builder("weather.api.response.time")
                .description("Bright Sky API response time")
                .tag("status", "failed")
                .register(meterRegistry));
    }
}
