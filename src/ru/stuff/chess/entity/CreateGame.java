package ru.stuff.chess.entity;

import ru.stuff.chess.sys.game.GameRoles;

/**
 * Created by mark on 27.12.14.
 */
public class CreateGame {
    private GameRoles role;
    private int clockControl;
    private long additionalSeconds;
    private boolean vsAI;
    private String fen;

    public String getFen() {
        return fen;
    }

    public void setFen(String fen) {
        this.fen = fen;
    }

    public boolean isVsAI() {
        return vsAI;
    }

    public void setVsAI(boolean vsAI) {
        this.vsAI = vsAI;
    }

    public long getAdditionalSeconds() {
        return additionalSeconds;
    }

    public void setAdditionalSeconds(int additionalSeconds) {
        this.additionalSeconds = additionalSeconds;
    }

    public GameRoles getRole() {
        return role;
    }

    public void setRole(GameRoles role) {
        this.role = role;
    }

    public int getClockControl() {
        return clockControl;
    }

    public void setClockControl(int clockControl) {
        this.clockControl = clockControl;
    }
}
