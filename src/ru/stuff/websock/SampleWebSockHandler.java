package ru.stuff.websock;

import org.apache.log4j.Logger;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

/**
 * Created by mark on 12.01.15.
 */
public class SampleWebSockHandler extends WebSocketHandlerDecorator {
    private static final Logger log = Logger.getLogger(SampleWebSockHandler.class);

    public SampleWebSockHandler(WebSocketHandler delegate) {
        super(delegate);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        log.info("handleMessage\t" + message.getPayload());
        super.handleMessage(session, message);
    }

}
