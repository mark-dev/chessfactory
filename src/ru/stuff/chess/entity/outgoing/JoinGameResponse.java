package ru.stuff.chess.entity.outgoing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.stuff.chess.entity.GameInfoContainer;
import ru.stuff.chess.entity.jsonutils.SimpleJsonSerializer;
import ru.stuff.chess.sys.game.GameRoles;

/**
 * Created by mark on 27.12.14.
 */
@JsonSerialize(using = SimpleJsonSerializer.class)
public class JoinGameResponse {
    public GameRoles role;
    public GameInfoContainer gameInfo;

    public JoinGameResponse(GameRoles role, GameInfoContainer gameInfo) {
        this.role = role;
        this.gameInfo = gameInfo;
    }
}
