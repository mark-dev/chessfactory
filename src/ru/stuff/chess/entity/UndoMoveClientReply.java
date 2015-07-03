package ru.stuff.chess.entity;

/**
 * Created by mark on 14.01.15.
 */
public class UndoMoveClientReply {
    public boolean isAccepted;
    public int gameId;

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean isAccepted) {
        this.isAccepted = isAccepted;
    }
}
