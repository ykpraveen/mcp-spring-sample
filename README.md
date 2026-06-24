# MCP Spring Sample

A complete AI chat application with Spring Boot backend and React frontend, powered by Spring AI and Model Context Protocol (MCP).

Published article: [Building an AI Chat Agent with MCP, Spring AI](https://dev.to/ykpraveen/building-an-ai-chat-agent-with-mcp-spring-ai-f0n)

```
┌──────────────────────────┐
│   mcp-ui (React, 3000)   │  ← Chat UI
└────────────┬─────────────┘
             │ HTTP
             ▼
┌──────────────────────────────────────────┐
│   mcp-spring-agent (Spring, 7171)       │  ← Chat Agent (Gemini 3.5)
└─────────────┬──────────────────────────────┘
              │ MCP Protocol
              ▼
┌──────────────────────────────────────────┐
│   mcp-server-spring (Spring, 7170)      │  ← MCP Server (Tools)
│   - get_current_weather (Bright Sky)    │
│   - geocode_city (OpenStreetMap)        │
└──────────────────────────────────────────┘
```

## Features

✅ **AI Chat Interface** - Modern React UI with streaming chat  
✅ **Spring AI Integration** - Uses Gemini 3.5-flash with Spring AI patterns  
✅ **MCP Tools** - Weather queries & city geocoding via tools  
✅ **Session Management** - In-memory conversation history per session  
✅ **Metrics & Health** - Spring Actuator endpoints  
✅ **Java 21** - Modern Java with virtual threads support  

## Tech Stack

| Module | Tech | Port |
|--------|------|------|
| **mcp-ui** | React 18 + Vite | 3000 |
| **mcp-spring-agent** | Spring Boot 4.1 + Spring AI | 7171 |
| **mcp-server-spring** | Spring Boot 4.1 + Spring AI MCP | 7170 |

**Shared**: Java 21, Maven 3.9.16, Spring AI BOM 2.0.0

## Quick Start

### Prerequisites

- **Java 21+** (required)
- **Maven 3.9.16+** (for backend)
- **Node.js 16+** (for frontend)
- **API Keys**:
  - `GEMINI_API_KEY` - Google Gemini API key
  - `MCP_API_KEY` - Security token for MCP endpoints (any value, e.g., `test-key-123`)

### 1. Start MCP Server (port 7170)

```bash
export MCP_API_KEY=test-key-123  # (Windows: set MCP_API_KEY=test-key-123)
cd mcp-server-spring
mvn spring-boot:run
```

Server runs at: `http://localhost:7170/actuator`

**Tools Available**:
- `get_current_weather` - Weather by station ID or coordinates
- `geocode_city` - Convert city name to lat/lon

### 2. Start Chat Agent (port 7171)

```bash
export GEMINI_API_KEY=your_gemini_key
export MCP_API_KEY=test-key-123
cd mcp-spring-agent
mvn spring-boot:run
```

Agent API: `http://localhost:7171/chat`

**Endpoints**:
- `POST /chat` - Spring AI path (recommended)
- `POST /chat/direct` - Direct Gemini REST (for comparison)

### 3. Start React UI (port 3000)

```bash
cd mcp-ui
npm install
npm run dev
```

UI opens at: `http://localhost:3000`

## Module Details

### [mcp-ui](./mcp-ui/README.md) - React Chat Frontend

- Vite-based React app
- Real-time chat interface
- Auto-proxies API calls to agent
- No backend code needed

### [mcp-spring-agent](./mcp-spring-agent/README.md) - Chat Agent

- Gemini chat via Spring AI ChatClient
- Optional MCP tool support (auto-initialized on weather keywords)
- Dual endpoints for testing (Spring AI vs direct REST)
- Session memory with LRU eviction

### [mcp-server-spring](./mcp-server-spring/README.md) - MCP Tool Server

- Exposes weather tool (Bright Sky API)
- Exposes geocoding tool (OpenStreetMap Nominatim)
- In-memory caching with TTL
- Metrics via Micrometer

## Configuration

### mcp-ui
Edit `vite.config.js`:
- `port: 3000` - Dev server port
- `target: 'http://localhost:7171'` - Agent URL

### mcp-spring-agent
Edit `src/main/resources/application.yml`:
- `server.port: 7171` - Agent port
- `spring.ai.google.genai.chat.model: gemini-3.5-flash` - Model
- `agent.memory-max-turns: 20` - Conversation history length
- `agent.memory-max-sessions: 500` - Max concurrent sessions

### mcp-server-spring
Edit `src/main/resources/application.yml`:
- `server.port: 7170` - Server port
- `weather.cache.ttl: 10m` - Cache duration
- `weather.brightsky.api-url` - Weather API endpoint

## Example Usage

**Query**: "What's the weather in Berlin right now?"

1. UI sends message to agent (`/api/chat`)
2. Agent detects "weather" keyword → initializes MCP client
3. Agent calls `get_current_weather` tool with coordinates from `geocode_city`
4. MCP Server queries Nominatim API for Berlin coordinates
5. MCP Server queries Bright Sky API for current weather
6. Agent formats response: "The weather in Berlin is..."
7. UI displays response

## Development

### Build All Modules

```bash
mvn clean package
```

### Run Tests

```bash
mvn -q test
```

### View Metrics (Agent)

```bash
curl http://localhost:7171/actuator/metrics
curl http://localhost:7171/actuator/health
```

### View Metrics (Server)

```bash
curl http://localhost:7170/actuator/metrics
curl http://localhost:7170/actuator/health
```

## Debugging

### Agent can't reach MCP Server
- Check `http://localhost:7170/actuator/health`
- Verify `MCP_API_KEY` matches on both sides
- Check firewall/network access

### UI can't reach Agent
- Check `http://localhost:7171/actuator/health`
- Verify Vite proxy config in `mcp-ui/vite.config.js`
- Check browser console (F12) for CORS errors

### MCP Tools not being called
- UI should show tool names in logs
- Agent auto-initializes MCP on keywords: weather, temperature, forecast, rain, wind, humidity, cloud, snow
- Try: "What's the weather in Paris?"

## Production Deployment

### Build Production UI

```bash
cd mcp-ui
npm run build
# dist/ folder ready for hosting (Vercel, Netlify, etc.)
```

### Build Production JARs

```bash
mvn clean package -DskipTests
# mcp-server-spring/target/mcp-server-spring-0.0.1-SNAPSHOT.jar
# mcp-spring-agent/target/mcp-spring-agent-0.0.1-SNAPSHOT.jar
```

### Run as JARs

```bash
java -jar mcp-server-spring-0.0.1-SNAPSHOT.jar
java -jar mcp-spring-agent-0.0.1-SNAPSHOT.jar
```

## Architecture Decisions

- **Spring AI for standardization**: Abstracts Gemini details, easier to swap models
- **MCP Protocol**: Standardized tool interface, tools run in separate service
- **In-memory session store**: Simple for testing, use database for production
- **TTL caching**: Reduces API calls, respects tool rate limits
- **React + Vite**: Fast development, no backend overhead for UI

## Troubleshooting

| Issue | Solution |
|-------|----------|
| ObjectMapper bean not found | Restart mcp-server-spring |
| GEMINI_API_KEY invalid | Verify key at [Google AI Studio](https://aistudio.google.com/app/apikey) |
| MCP tools not executing | Check keywords in agent logs (weather, temperature, etc.) |
| Port already in use | Change port in application.yml or vite.config.js |
| npm install fails | Try `npm install --legacy-peer-deps` |
| Tests timeout | Increase Maven timeout: `mvn test -Dorg.slf4j.simpleLogger.defaultLogLevel=debug` |

## Next Steps

- Add authentication (OAuth2 for UI)
- Persist sessions to PostgreSQL
- Add more MCP tools (translation, news, etc.)
- WebSocket support for real-time streaming
- Docker compose for easy deployment
- Kubernetes manifests

## License

MIT
