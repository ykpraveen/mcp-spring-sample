package com.dev.mcp.agent.api;

import com.dev.mcp.agent.service.ChatAgentService;
import com.dev.mcp.agent.service.DirectGeminiChatService;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ChatController {

    private final ChatAgentService chatAgentService;
    private final DirectGeminiChatService directGeminiChatService;

    public ChatController(ChatAgentService chatAgentService,
                          DirectGeminiChatService directGeminiChatService) {
        this.chatAgentService = chatAgentService;
        this.directGeminiChatService = directGeminiChatService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        if (!StringUtils.hasText(request.sessionId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sessionId is required.");
        }
        if (!StringUtils.hasText(request.message())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message is required.");
        }
        String reply = chatAgentService.reply(request.sessionId(), request.message());
        return new ChatResponse(reply);
    }

    @PostMapping("/chat/direct")
    public ChatResponse chatDirect(@RequestBody ChatRequest request) {
        if (!StringUtils.hasText(request.sessionId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sessionId is required.");
        }
        if (!StringUtils.hasText(request.message())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message is required.");
        }
        String reply = directGeminiChatService.reply(request.sessionId(), request.message());
        return new ChatResponse(reply);
    }
}
