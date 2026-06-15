package com.dev.mcp.agent.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SessionMemoryStore {

    public record ConversationTurn(String userMessage, String assistantMessage) {
    }

    private final int maxTurns;
    private final Map<String, ArrayDeque<ConversationTurn>> sessions;

    public SessionMemoryStore(int maxSessions, int maxTurns) {
        this.maxTurns = maxTurns;
        this.sessions = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, ArrayDeque<ConversationTurn>> eldest) {
                return size() > maxSessions;
            }
        };
    }

    public synchronized List<ConversationTurn> history(String sessionId) {
        ArrayDeque<ConversationTurn> turns = sessions.get(sessionId);
        if (turns == null) {
            return List.of();
        }
        return new ArrayList<>(turns);
    }

    public synchronized void appendTurn(String sessionId, String userMessage, String assistantMessage) {
        ArrayDeque<ConversationTurn> turns = sessions.computeIfAbsent(sessionId, key -> new ArrayDeque<>());
        turns.addLast(new ConversationTurn(userMessage, assistantMessage));
        while (turns.size() > maxTurns) {
            turns.removeFirst();
        }
    }
}
