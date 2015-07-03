package ru.stuff.chess.entity.outgoing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.stuff.chess.entity.jsonutils.SimpleJsonSerializer;

/**
 * Created by mark on 14.01.15.
 */
@JsonSerialize(using = SimpleJsonSerializer.class)
public class UndoMoveReply {
    public boolean isAccepted;
    public boolean fullMoveUndo = false;

    public UndoMoveReply(boolean isAccepted, boolean fullMoveUndo) {
        this.fullMoveUndo = fullMoveUndo;
        this.isAccepted = isAccepted;
    }

    public UndoMoveReply() {
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean isAccepted) {
        this.isAccepted = isAccepted;
    }
}
