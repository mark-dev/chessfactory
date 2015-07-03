package ru.stuff.chess.entity.outgoing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.stuff.chess.entity.jsonutils.SimpleJsonSerializer;
import ru.stuff.chess.sys.game.GameOverReasons;
import ru.stuff.chess.sys.game.GameRoles;

/**
 * Created by mark on 17.01.15.
 */
@JsonSerialize(using = SimpleJsonSerializer.class)
public class GameTerminated {
    public GameOverReasons reason;
    public GameRoles winner;

    public GameTerminated(GameOverReasons reason, GameRoles winner) {
        this.reason = reason;
        this.winner = winner;
    }


    public GameTerminated() {
    }

    public GameOverReasons getReason() {
        return reason;
    }

    public void setReason(GameOverReasons reason) {
        this.reason = reason;
    }

    public static GameTerminated draw() {
        return new GameTerminated(GameOverReasons.DRAW, null);
    }
}
