package ru.stuff.chess.entity;

/**
 * Created by mark on 27.12.14.
 */
public class Move {
    public int gameId;
    public String algebraic;
    public String move;
    public boolean isDraw = false;

    public boolean isDraw() {
        return isDraw;
    }

    public void setDraw(boolean isDraw) {
        this.isDraw = isDraw;
    }

    public String getMove() {
        return move;
    }

    public void setMove(String move) {
        this.move = move;
    }

    public Move(int gameId, String move,  String algebraic) {
        this.gameId = gameId;
        this.algebraic = algebraic;
        this.move = move;
    }

    public String getAlgebraic() {

        return algebraic;
    }

    public void setAlgebraic(String algebraic) {
        this.algebraic = algebraic;
    }

    public Move() {
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }


}
