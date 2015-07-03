package ru.stuff.chess.entity.outgoing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.stuff.chess.entity.jsonutils.SimpleJsonSerializer;

/**
 * Created by mark on 29.04.15.
 */
@JsonSerialize(using = SimpleJsonSerializer.class)
public class FenPositionReply {
    public String fen;

    public FenPositionReply() {
    }

    public FenPositionReply(String fen) {
        this.fen = fen;
    }

    public String getFen() {
        return fen;
    }

    public void setFen(String fen) {
        this.fen = fen;
    }
}
