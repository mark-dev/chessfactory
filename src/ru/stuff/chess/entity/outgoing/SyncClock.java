package ru.stuff.chess.entity.outgoing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.stuff.chess.entity.jsonutils.SimpleJsonSerializer;
import ru.stuff.chess.sys.game.GameInfo;

/**
 * Created by mark on 16.02.15.
 */
@JsonSerialize(using = SimpleJsonSerializer.class)
public class SyncClock {
    public long whiteTimer;
    public long blackTimer;

    public SyncClock(long whiteTimer, long blackTimer) {
        this.whiteTimer = whiteTimer;
        this.blackTimer = blackTimer;
    }

    public long getWhiteTimer() {
        return whiteTimer;
    }

    public void setWhiteTimer(long whiteTimer) {
        this.whiteTimer = whiteTimer;
    }

    public long getBlackTimer() {
        return blackTimer;
    }

    public void setBlackTimer(long blackTimer) {
        this.blackTimer = blackTimer;
    }

    public static SyncClock fromGame(GameInfo game) {
        return new SyncClock(game.getWhiteClock().toMillis(), game.getBlackClock().toMillis());
    }
}
