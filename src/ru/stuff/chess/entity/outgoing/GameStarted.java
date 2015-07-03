package ru.stuff.chess.entity.outgoing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.stuff.chess.entity.GameInfoContainer;
import ru.stuff.chess.entity.jsonutils.SimpleJsonSerializer;

/**
 * Created by mark on 27.12.14.
 */
@JsonSerialize(using = SimpleJsonSerializer.class)
public class GameStarted {
    public GameInfoContainer container;

    public GameStarted() {
    }

    public GameStarted(GameInfoContainer container) {

        this.container = container;
    }
    
    public GameInfoContainer getGameInfoContainer(){
	return container;
    }

}
