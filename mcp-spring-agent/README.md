# mcp-spring-agent - Chat Agent

Spring Boot Gemini chat agent with Spring AI and optional MCP tools.

## Setup

```bash
export GEMINI_API_KEY=your_gemini_key
export MCP_API_KEY=test-key-123
mvn spring-boot:run
```

Runs on: `http://localhost:7171`

## API

**POST /chat** - Spring AI path (recommended)
```json
{
  "sessionId": "abc-123",
  "message": "What's the weather in Berlin?"
}
```

**POST /chat/direct** - Direct Gemini REST (for comparison)

## Configuration

Edit `src/main/resources/application.yml`:
- `server.port: 7171` - Port
- `spring.ai.google.genai.chat.model: gemini-3.5-flash` - Model
- `agent.memory-max-turns: 20` - Conversation history
- `agent.memory-max-sessions: 500` - Max sessions

## Features

- Gemini 3.5-flash via Spring AI ChatClient
- Auto-initializes MCP on weather keywords
- Session memory with LRU eviction
- Dual endpoints for testing

For more details, see [parent README](../README.md).
