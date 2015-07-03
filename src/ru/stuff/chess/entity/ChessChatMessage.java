package ru.stuff.chess.entity;

/**
 * Created by mark on 29.12.14.
 */
public class ChessChatMessage {
    String payload;
    int gameId;

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }
}
