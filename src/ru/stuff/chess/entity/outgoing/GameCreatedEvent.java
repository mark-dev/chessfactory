package ru.stuff.chess.entity.outgoing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.stuff.chess.entity.GameInfoContainer;
import ru.stuff.chess.entity.jsonutils.SimpleJsonSerializer;

/**
 * Created by mark on 29.12.14.
 */
@JsonSerialize(using = SimpleJsonSerializer.class)
public class GameCreatedEvent {
    public GameInfoContainer container;

    public GameCreatedEvent() {
    }

    public GameCreatedEvent( GameInfoContainer container) {
        this.container = container;
    }


}
