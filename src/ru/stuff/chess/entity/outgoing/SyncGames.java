package ru.stuff.chess.entity.outgoing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.stuff.chess.entity.GameInfoContainer;
import ru.stuff.chess.entity.jsonutils.SimpleJsonSerializer;

import java.util.Collection;

/**
 * Created by mark on 13.01.15.
 */
@JsonSerialize(using = SimpleJsonSerializer.class)
public class SyncGames {
    public Collection<GameInfoContainer> games;

    public SyncGames(Collection<GameInfoContainer> games) {
        this.games = games;
    }

    public Collection<GameInfoContainer> getGames() {
        return games;
    }

    public void setGames(Collection<GameInfoContainer> games) {
        this.games = games;
    }
}
