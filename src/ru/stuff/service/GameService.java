package ru.stuff.service;

import org.springframework.stereotype.Component;
import ru.stuff.chess.sys.game.GameInfo;
import ru.stuff.chess.sys.game.GameMember;
import ru.stuff.chess.sys.game.GameRoles;
import ru.stuff.chess.sys.users.WebSocketSessionPrincipal;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Created by mark on 27.12.14.
 */
@Component
public class GameService {

    private final AtomicInteger gameId = new AtomicInteger(1);

    //Index(gameId,GameInfo)
    private final Map<Integer, GameInfo> games = new TreeMap<>();
    //Index (session,[games]), in fact one session -> one game, refactor it.
    private final Map<WebSocketSessionPrincipal, Collection<GameInfo>> gamePlayers = new TreeMap<>();
    //Used by ClockManager to tickClocks
    private final Collection<GameInfo> activeGamesIndex = new TreeSet<>();

    private final Collection<Consumer<GameInfo>> timeClockListeners = new LinkedList<>();
    private final Collection<Consumer<GameInfo>> gameDeletedListeners = new LinkedList<>();

    //Returns random player role (if avaliable) OR free player role OR spectator.
    public GameRoles getPreferableRole(GameInfo game) {
        if (game != null) {
            if (game.getWhitePlayer() == null && game.getBlackPlayer() == null) {
                return Math.random() > 0.5 ? GameRoles.WHITE : GameRoles.BLACK;
            }
            if (game.getWhitePlayer() == null)
                return GameRoles.WHITE;
            if (game.getBlackPlayer() == null)
                return GameRoles.BLACK;
            return GameRoles.SPECTATOR;
        } else
            return null;
    }


    public void timeElapsed(GameInfo gi) {
        timeClockListeners.forEach((l) -> {
            l.accept(gi);
        });
    }


    public Optional<GameInfo> gameById(int gameId) {
        return Optional.ofNullable(games.get(gameId));
    }

    public Collection<GameInfo> getActiveGames() {
        return activeGamesIndex;
    }

    public GameInfo createGame(Duration clockControl, Duration additionalSeconds, boolean vsAI, String fen) {
        int newGameId = gameId.getAndIncrement();
        GameInfo game = new GameInfo(newGameId, clockControl, additionalSeconds, vsAI, fen);
        games.put(newGameId, game);
        return game;
    }

    private void putPlayerToGameIndex(WebSocketSessionPrincipal session, GameInfo game) {
        Collection<GameInfo> agames = gamePlayers.get(session);
        if (agames == null) {
            agames = new LinkedList<>();
        }
        agames.add(game);
        gamePlayers.put(session, agames);
    }

    public Collection<GameInfo> getExistedGames() {
        LinkedList<GameInfo> nonDeletedGames = new LinkedList<>();
        games.forEach((id, info) -> {
            if (!info.isRemoved())
                nonDeletedGames.add(info);
        });
        return nonDeletedGames;
    }

    public void hideGame(GameInfo info) {
        info.setRemoved(true);
        activeGamesIndex.remove(info);
        fireGameDeleted(info);
    }

    public Optional<GameRoles> joinGame(GameInfo game, GameRoles role, WebSocketSessionPrincipal session) {
        if (game.addMember(new GameMember(session, role))) {
            putPlayerToGameIndex(session, game);
            return Optional.of(role);
        }
        return Optional.empty();
    }

    public Optional<GameRoles> joinGame(GameInfo game, WebSocketSessionPrincipal session) {
        GameRoles role = getPreferableRole(game);

        return joinGame(game, role, session);
    }

    public void leaveGame(GameInfo game, WebSocketSessionPrincipal session) {
        if (game.removeUser(session)) {
            gamePlayers.remove(session);
            if (game.isEmpty()) {
                activeGamesIndex.remove(game);
                games.remove(game.getGameId());
                fireGameDeleted(game);
            }
        }
    }


    public void addGameDeletedListener(Consumer<GameInfo> listener) {
        gameDeletedListeners.add(listener);
    }

    private void fireGameDeleted(GameInfo game) {
        gameDeletedListeners.forEach((l) -> {
            l.accept(game);
        });
    }


    public void startGame(GameInfo game) {
        game.startGame();
        activeGamesIndex.add(game);

    }

    public void pauseGame(GameInfo game) {
        game.pauseGame();
        activeGamesIndex.remove(game);
    }


    public boolean exists(int gameId) {
        return games.keySet().contains(gameId);
    }

    public void removePlayerFromAllGames(WebSocketSessionPrincipal session) {
        Collection<GameInfo> affectedGames = gamePlayers.get(session);
        if (affectedGames != null) {
            affectedGames.forEach((game) -> {
                leaveGame(game, session);
            });
        }
    }

    public void addClockWatchListener(Consumer<GameInfo> listener) {
        timeClockListeners.add(listener);
    }

    public Optional<Collection<GameInfo>> getPlayerGames(WebSocketSessionPrincipal session) {
        return Optional.ofNullable(gamePlayers.get(session));
    }


}
