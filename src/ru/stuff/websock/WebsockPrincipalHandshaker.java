package ru.stuff.websock;

import org.apache.log4j.Logger;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import ru.stuff.chess.sys.users.UserInfo;
import ru.stuff.chess.sys.users.WebSocketSessionPrincipal;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by mark on 26.12.14.
 */
@Component
public class WebsockPrincipalHandshaker extends DefaultHandshakeHandler {
    private static final Logger log = Logger.getLogger(WebsockPrincipalHandshaker.class);
    private static AtomicLong sessionId = new AtomicLong(0);

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        log.debug("Determine user...");
        Principal p = request.getPrincipal();
        if (p == null) return null;
        //Проставляем уникальный ID для каждого соединения.
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) request.getPrincipal();
        UserInfo userInfo = (UserInfo) token.getPrincipal();

       return new WebSocketSessionPrincipal(userInfo, sessionId.incrementAndGet());

    }
}
