package ru.stuff.chess.entity.outgoing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.stuff.chess.entity.jsonutils.SimpleJsonSerializer;

/**
 * Created by mark on 14.01.15.
 */
@JsonSerialize(using = SimpleJsonSerializer.class)
public class UndoMoveServerRequest {
}
