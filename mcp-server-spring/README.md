# mcp-server-spring - MCP Tool Server

Spring Boot MCP server exposing tools: weather (Bright Sky), geocoding (OpenStreetMap).

## Setup

```bash
export MCP_API_KEY=test-key-123
mvn spring-boot:run
```

Runs on: `http://localhost:7170`

## Tools

**get_current_weather** - Weather data
- Input: `dwd_station_id` OR `lat` + `lon`
- Output: Weather data with cache status

**geocode_city** - City to coordinates
- Input: `city` (string)
- Output: Latitude, longitude, display name

## Configuration

Edit `src/main/resources/application.yml`:
- `server.port: 7170` - Port
- `weather.cache.ttl: 10m` - Cache duration
- `weather.brightsky.api-url` - Weather API endpoint
- `mcp.security.api-key` - X-API-Key validation

## Features

- TTL-based caching (separate per lookup mode)
- Micrometer metrics integration
- Structured error responses
- X-API-Key authentication

For more details, see [parent README](../README.md).
