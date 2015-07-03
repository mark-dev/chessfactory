package ru.stuff.websock;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import ru.stuff.chess.sys.users.WebSocketSessionPrincipal;
import ru.stuff.service.SessionService;

/**
 * Created by mark on 30.12.14.
 */
@Component
public class SessionDisconnectEventHandler implements ApplicationListener<SessionDisconnectEvent> {
    private static final Logger log = Logger.getLogger(SessionDisconnectEventHandler.class);
    @Autowired
    private SessionService sessionService;

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        if (sha.getUser() != null) {
            if (sha.getUser() instanceof WebSocketSessionPrincipal)
                sessionService.offline((WebSocketSessionPrincipal) sha.getUser());
            else if (sha.getUser() instanceof UsernamePasswordAuthenticationToken) {
                //reconnect?
            }
        }
    }
}
