package ru.stuff.chess.entity.outgoing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.stuff.chess.entity.jsonutils.SimpleJsonSerializer;

/**
 * Created by mark on 27.12.14.
 */
@JsonSerialize(using = SimpleJsonSerializer.class)
public class NewMove {

    public int gameId;
    public String move;

    public NewMove() {
    }

    public NewMove(int gameId, String move) {

        this.gameId = gameId;
        this.move = move;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public String getMove() {
        return move;
    }

    public void setMove(String move) {
        this.move = move;
    }
}
