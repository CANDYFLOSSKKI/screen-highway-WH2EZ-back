package com.wut.screenwebsx.Context;

import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;

public class WebSocketSessionContext {
    private static final HashMap<String, WebSocketSession> SESSION_POOL = new HashMap<>();

    public static void addSession(String key, WebSocketSession session) {
        SESSION_POOL.put(key, session);
    }

    public static WebSocketSession removeSession(String key) {
        return SESSION_POOL.remove(key);
    }

    public static WebSocketSession getSession(String key) {
        return SESSION_POOL.get(key);
    }

    public static void removeAndClose(String key) {
        var session = SESSION_POOL.remove(key);
        if (session != null) {
            try { session.close(); }
            catch (IOException e) { e.printStackTrace(); }
        }
    }

}
