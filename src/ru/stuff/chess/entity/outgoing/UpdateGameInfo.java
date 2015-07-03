package ru.stuff.chess.entity.outgoing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.stuff.chess.entity.GameInfoContainer;
import ru.stuff.chess.entity.jsonutils.SimpleJsonSerializer;

/**
 * Created by mark on 05.02.15.
 */
@JsonSerialize(using = SimpleJsonSerializer.class)
public class UpdateGameInfo {
    public GameInfoContainer container;

    public UpdateGameInfo(GameInfoContainer container) {
        this.container = container;
    }
}
