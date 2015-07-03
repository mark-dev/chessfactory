package ru.stuff.chess.entity;

/**
 * Created by mark on 27.12.14.
 */
public class SimpleGameIdContainer {
    int gameId;

    public SimpleGameIdContainer() {
    }

    public SimpleGameIdContainer(int gameId) {

        this.gameId = gameId;
    }


    public int getGameId() {
        return gameId;
    }



    public void setGameId(int gameId) {
        this.gameId = gameId;
    }
}
