package ru.stuff.chess.entity.outgoing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.stuff.chess.entity.jsonutils.OptionalField;
import ru.stuff.chess.entity.jsonutils.SimpleJsonSerializer;
import ru.stuff.chess.sys.events.ChatEventsTypes;
import ru.stuff.chess.sys.game.GameRoles;

/**
 * Created by mark on 29.12.14.
 */
@JsonSerialize(using = SimpleJsonSerializer.class)
public class ChatMessageFlow {

    @OptionalField
    public String username;

    public String payload;

    public ChatEventsTypes type;

    public void setType(ChatEventsTypes type) {
        this.type = type;
    }

    public ChatMessageFlow(String username, String payload) {
        this.username = username;
        this.payload = payload;
    }

    public ChatMessageFlow(String payload, ChatEventsTypes type) {
        this.payload = payload;
        this.type = type;
    }

    public ChatMessageFlow(String username, String payload, ChatEventsTypes type) {
        this.username = username;
        this.payload = payload;
        this.type = type;
    }

    public ChatMessageFlow() {
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
