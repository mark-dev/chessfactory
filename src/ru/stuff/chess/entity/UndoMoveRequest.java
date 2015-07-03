package ru.stuff.chess.entity;

/**
 * Created by mark on 14.01.15.
 */
public class UndoMoveRequest {
    public int gameId;

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }
}
