package ru.stuff.chess.sys.game;

import chesspresso.Chess;
import chesspresso.game.Game;
import chesspresso.move.IllegalMoveException;
import chesspresso.position.Position;
import ru.stuff.chess.sys.users.WebSocketSessionPrincipal;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Created by mark on 27.12.14.
 */
public class GameInfo implements Comparable<GameInfo> {

    private final int gameId;
    private GameMember whitePlayer;
    private GameMember blackPlayer;
    private final List<GameMember> spectators;


    //Clocks stuff
    private final Duration clockControl;
    private Duration whiteClock;
    private Duration blackClock;

    private long lastBlackChessTick;
    private long lastWhiteChessTick;
    private final Duration additionalSeconds; //This duration will be added to player clock after move

    private boolean isRemoved = false;
    private boolean isStarted = false;
    private boolean isPaused = false;
    private boolean versusAI = false;
    private boolean isWhiteMove = true;


    private final Game game;
    private final Position initialPosition;
    private final LinkedList<String> whiteMoves = new LinkedList<>();
    private final LinkedList<String> blackMoves = new LinkedList<>();

    private int centipawns; //AI store position estimate here (to implement draw accept desicion)


    public GameInfo(int gameId, Duration clockControl, Duration additionalSeconds, boolean vsAI, String fen) {
        this.gameId = gameId;
        this.clockControl = clockControl;
        whiteClock = Duration.from(clockControl);
        blackClock = Duration.from(clockControl);
        this.additionalSeconds = additionalSeconds;
        this.versusAI = vsAI;
        spectators = new LinkedList<>();
        game = new Game();
        initialPosition = new Position(fen);
        game.getPosition().set(initialPosition);
        isWhiteMove = game.getPosition().getToPlay() == Chess.WHITE;
    }

    /* Getter/Setter stuff */
    public boolean isRemoved() {
        return isRemoved;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setRemoved(boolean isRemoved) {
        this.isRemoved = isRemoved;
    }

    public GameMember getWhitePlayer() {
        return whitePlayer;
    }

    public GameMember getBlackPlayer() {
        return blackPlayer;
    }


    public Duration getWhiteClock() {
        return whiteClock;
    }

    public Duration getBlackClock() {
        return blackClock;
    }

    public boolean startsFromBlackMove() {
        return initialPosition.getToPlay() == Chess.BLACK;
    }

    public int getGameId() {
        return gameId;
    }

    public boolean isVersusAI() {
        return versusAI;
    }

    public Duration getAdditionalSeconds() {
        return additionalSeconds;
    }

    public String getFen() {
        return game.getPosition().toString();
    }

    public boolean isStarted() {
        return isStarted;
    }

    public Duration getClockControl() {
        return clockControl;
    }

    public boolean isWhiteMove() {
        return isWhiteMove;
    }

    public Position getInitialPosition() {
        return initialPosition;
    }

    public LinkedList<String> getWhiteMoves() {
        return whiteMoves;
    }

    public LinkedList<String> getBlackMoves() {
        return blackMoves;
    }

    /*
         * Members management
         */
    public boolean removeUser(WebSocketSessionPrincipal session) {
        if (whitePlayer != null && whitePlayer.getSession().equals(session)) {
            whitePlayer = null;
            return true;
        } else if (blackPlayer != null && blackPlayer.getSession().equals(session)) {
            blackPlayer = null;
            return true;
        }
        return spectators.remove(new GameMember(session));
    }

    //Do not allow one session be in multiple roles.
    public boolean addMember(GameMember m) {
        if (m.getRole().isBlack()) {
            //Multiple session check
            if (whitePlayer != null && whitePlayer.equals(m))
                return false;
            else {
                if (blackPlayer == null) {
                    blackPlayer = m;
                    if (versusAI) {
                        whitePlayer = new AIPlayer(GameRoles.WHITE);
                    }
                    return true;
                } else
                    return false; //Already has black player
            }
        } else if (m.getRole().isWhite()) {
            //Multiple session check
            if (blackPlayer != null && blackPlayer.equals(m))
                return false;
            else {
                if (whitePlayer == null) {
                    whitePlayer = m;
                    if (versusAI) {
                        blackPlayer = new AIPlayer(GameRoles.BLACK);
                    }
                    return true;
                } else
                    return false; //Already has white player
            }
        } else if (m.getRole().equals(GameRoles.SPECTATOR)) {
            return !spectators.contains(m) && spectators.add(m);
        }
        return false;
    }

    public GameMember getPlayer(GameRoles role) {
        if (!role.isPlayer())
            return null;
        else if (whitePlayer != null && whitePlayer.getRole().equals(role))
            return whitePlayer;
        else if (blackPlayer != null && blackPlayer.getRole().equals(role)) {
            return blackPlayer;
        }
        return null;
    }

    public Optional<GameRoles> getGameRole(WebSocketSessionPrincipal session) {
        if (whitePlayer != null && whitePlayer.getSession().equals(session))
            return Optional.of(GameRoles.WHITE);
        if (blackPlayer != null && blackPlayer.getSession().equals(session))
            return Optional.of(GameRoles.BLACK);
        for (GameMember gm : spectators) {
            if (gm.getSession().equals(session))
                return Optional.of(GameRoles.SPECTATOR);
        }
        return Optional.empty();
    }

    public GameMember getAIPlayer() {
        if (versusAI) {
            if (blackPlayer instanceof AIPlayer)
                return blackPlayer;
            else if (whitePlayer instanceof AIPlayer)
                return whitePlayer;
        }
        return null;
    }

    public GameMember getOpponent(WebSocketSessionPrincipal session) {
        if (whitePlayer != null && whitePlayer.getSession().equals(session)) return blackPlayer;
        if (blackPlayer != null && blackPlayer.getSession().equals(session)) return whitePlayer;
        return null;
    }

    /*Game flow management*/
    public boolean move(String algebraic) {
        try {
            doMove(algebraic);
            if (isWhiteMove) {
                whiteMoves.add(algebraic);
            } else {
                blackMoves.add(algebraic);
            }
            toggleMove(false);
            return true;
        } catch (IllegalMoveException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void doMove(String amove) throws IllegalMoveException {
        short move = game.getPosition().getMove(Chess.strToSqi(amove.substring(0, 2)),
                Chess.strToSqi(amove.substring(2, 4)),
                amove.length() == 5 ? Chess.charToPiece(Character.toUpperCase(amove.charAt(4))) : Chess.NO_PIECE);
        game.getPosition().doMove(move);
    }

    public boolean undoMove(boolean isWhiteAsk) {
        boolean fullMoveUndo = isWhiteAsk == isWhiteMove && blackMoves.size() >= 1 && whiteMoves.size() >= 1;
        if (isWhiteMove) {
            blackMoves.removeLast();
            if (fullMoveUndo)
                whiteMoves.removeLast();
        } else {
            whiteMoves.removeLast();
            if (fullMoveUndo)
                blackMoves.removeLast();
        }
        //Undo half-move
        game.getPosition().undoMove();
        //Full move undo if required
        if (fullMoveUndo) {
            game.getPosition().undoMove();
        } else {
            toggleMove(true);
        }

        return fullMoveUndo;
    }

    public void startGame() {
        if (isPaused) {
            isPaused = false;
        }
        isStarted = true;
        clearClockTicks();
    }

    public void pauseGame() {
        isStarted = false;
        isPaused = true;
        lastWhiteChessTick = 0;
        lastBlackChessTick = 0;
    }

    /*Misc*/
    public boolean readyToStart() {
        return (whitePlayer != null && blackPlayer != null) ||
                (versusAI && (whitePlayer != null || blackPlayer != null));
    }

    public boolean isEmpty() {
        return (blackPlayer == null || blackPlayer instanceof AIPlayer) &&
                (whitePlayer == null || whitePlayer instanceof AIPlayer) && spectators.isEmpty();
    }

    public boolean hasAnyMoves() {
        return !(whiteMoves.isEmpty() && blackMoves.isEmpty());
    }

    public boolean isMate() {
        return game.getPosition().isMate();
    }

    public int getMaterial() {
        return game.getPosition().getMaterial();
    }

    public double getDomination() {
        return game.getPosition().getDomination();
    }

    public int getCentipawns() {
        return centipawns;
    }

    public void setCentipawns(int centipawns) {
        this.centipawns = centipawns;
    }

    /* Public clock API */

    // Impotant! We should consider that scheduling with fixed rates has some mistakes
    // e.g. fixed rate 1000 ms can trigger with 1040ms 1030ms period etc, and those mistakes stacks
    // So in our calculations we should always check real rate, and keep previous trigger timing (tickTmp)
    private long tickTmp;

    public boolean tickWhiteClock() {
        if (lastWhiteChessTick == 0) {
            lastWhiteChessTick = System.currentTimeMillis();
        }
        tickTmp = System.currentTimeMillis();
        whiteClock = whiteClock.minus(Duration.ofMillis(tickTmp - lastWhiteChessTick));
        lastWhiteChessTick = tickTmp;
        return whiteClock.isNegative();
    }

    public boolean tickBlackClocks() {
        if (lastBlackChessTick == 0) {
            lastBlackChessTick = System.currentTimeMillis();
        }
        tickTmp = System.currentTimeMillis();
        blackClock = blackClock.minus(Duration.ofMillis(tickTmp - lastBlackChessTick));
        lastBlackChessTick = tickTmp;
        return blackClock.isNegative();
    }

    public void addClockTimeTo(GameRoles role, int secondAmount) {
        if (role.isBlack())
            incrementBlackClocks(Duration.ofSeconds(secondAmount));
        else if (role.isWhite())
            incrementWhiteClocks(Duration.ofSeconds(secondAmount));
    }

    /*Overrides*/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GameInfo gameInfo = (GameInfo) o;

        return gameId == gameInfo.gameId;

    }

    @Override
    public int hashCode() {
        return gameId;
    }

    @Override
    public int compareTo(GameInfo o) {
        return Integer.compare(gameId, o.getGameId());
    }

    /*Private CLOCK API*/
    private void incrementBlackClocks(Duration val) {
        blackClock = blackClock.plus(val);
    }

    private void incrementWhiteClocks(Duration val) {
        whiteClock = whiteClock.plus(val);
    }

    private void toggleMove(boolean withoutTimeAddition) {
        if (isWhiteMove) {
            tickWhiteClock();
            if (!withoutTimeAddition)
                incrementWhiteClocks(additionalSeconds);
        } else {
            tickBlackClocks();
            if (!withoutTimeAddition)
                incrementBlackClocks(additionalSeconds);
        }
        clearClockTicks();
        isWhiteMove = !isWhiteMove;
    }

    private void clearClockTicks() {
        lastWhiteChessTick = 0;
        lastBlackChessTick = 0;
    }
}
