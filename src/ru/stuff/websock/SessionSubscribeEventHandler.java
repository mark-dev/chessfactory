package ru.stuff.websock;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

/**
 * Created by mark on 23.04.15.
 */
@Component
public class SessionSubscribeEventHandler implements ApplicationListener<SessionSubscribeEvent> {
    private static final Logger log = Logger.getLogger(SessionSubscribeEventHandler.class);

    @Override
    public void onApplicationEvent(SessionSubscribeEvent event) {
        log.info("subscribe event: " + event);
    }
}
