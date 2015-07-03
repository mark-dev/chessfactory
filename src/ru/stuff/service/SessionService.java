package ru.stuff.service;

import org.springframework.stereotype.Component;
import ru.stuff.chess.sys.users.UserInfo;
import ru.stuff.chess.sys.users.WebSocketSessionPrincipal;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Created by mark on 30.12.14.
 */
@Component
public class SessionService {
    private Map<UserInfo, List<WebSocketSessionPrincipal>> onlineUsers = new TreeMap<>();
    private List<BiConsumer<WebSocketSessionPrincipal, Boolean>> listeners = new LinkedList<>();

    public Set<UserInfo> getOnlineUsers() {
        return onlineUsers.keySet();
    }

    //called by session event handler when new websocket session created
    public void online(WebSocketSessionPrincipal session) {
        List<WebSocketSessionPrincipal> userSessions = onlineUsers.get(session.getUser());
        if (userSessions == null) {
            userSessions = new LinkedList<>();
        }
        userSessions.add(session);
        onlineUsers.put(session.getUser(), userSessions);
        notifyEvent(session, true);
    }

    //called by session event handler when websocket session disconnected
    public void offline(WebSocketSessionPrincipal session) {
        List<WebSocketSessionPrincipal> userSessions = onlineUsers.get(session.getUser());
        if (userSessions == null) {
            return;
        }
        userSessions.remove(session);

        if (userSessions.isEmpty()) {
            //Last session was closed
            onlineUsers.remove(session.getUser());
        } else {
            //Has more sessions..
            onlineUsers.put(session.getUser(), userSessions);
        }
        notifyEvent(session, false);
    }

    public List<WebSocketSessionPrincipal> getSessions(UserInfo user) {
        return onlineUsers.get(user);
    }

    public void addListener(BiConsumer<WebSocketSessionPrincipal, Boolean> listener) {
        listeners.add(listener);
    }

    //Fire up connect(disconnect) session event to listeners
    private void notifyEvent(WebSocketSessionPrincipal session, boolean isOnline) {
        listeners.forEach((fun) -> {
            fun.accept(session, isOnline);
        });
    }

}
