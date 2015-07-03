package ru.stuff.chess.entity;

import ru.stuff.chess.sys.game.GameInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by mark on 28.01.15.
 */
public class GameInfoContainer {
    public String whitePlayer;
    public String blackPlayer;
    public Collection<String> spectators;
    public int gameId;
    public int clock; //Начальное время на игру. в минутах
    public long additionalSeconds;
    public String fen;
    public long whiteTimer;
    public long blackTimer;
    public boolean isStarted;
    public boolean isPaused;

    public static GameInfoContainer make(GameInfo info) {
        return new GameInfoContainer(info);
    }

    public GameInfoContainer(GameInfo info) {
        this.clock = (int) info.getClockControl().toMinutes();
        this.gameId = info.getGameId();
        this.whitePlayer = info.getWhitePlayer() != null ? info.getWhitePlayer().getUser().getUsername() : "";
        this.blackPlayer = info.getBlackPlayer() != null ? info.getBlackPlayer().getUser().getUsername() : "";
        this.spectators = new LinkedList<>();
        this.fen = info.getFen();
        whiteTimer = info.getWhiteClock().toMillis();
        blackTimer = info.getBlackClock().toMillis();
        additionalSeconds = info.getAdditionalSeconds().getSeconds();
        this.isStarted = info.isStarted();
        this.isPaused = info.isPaused();
    }

    public GameInfoContainer(int gameId, String whitePlayer) {
        this.gameId = gameId;
        this.whitePlayer = whitePlayer;
    }

    public GameInfoContainer(String whitePlayer) {
        this.whitePlayer = whitePlayer;
    }

    public GameInfoContainer(String whitePlayer, String blackPlayer) {
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        spectators = Collections.emptyList();
    }

    public GameInfoContainer(String whitePlayer, String blackPlayer, Collection<String> spectators) {
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        this.spectators = spectators;
    }
}
