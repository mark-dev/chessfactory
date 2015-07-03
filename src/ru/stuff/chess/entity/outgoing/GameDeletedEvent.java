package ru.stuff.chess.entity.outgoing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.stuff.chess.entity.jsonutils.SimpleJsonSerializer;

/**
 * Created by mark on 24.01.15.
 */
@JsonSerialize(using = SimpleJsonSerializer.class)
public class GameDeletedEvent {
    public int gameId;

    public GameDeletedEvent(int gameId) {
        this.gameId = gameId;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public GameDeletedEvent() {
    }
}
