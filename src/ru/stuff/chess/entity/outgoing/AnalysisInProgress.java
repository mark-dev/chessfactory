package ru.stuff.chess.entity.outgoing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.stuff.chess.entity.jsonutils.SimpleJsonSerializer;

/**
 * Created by mark on 14.05.15.
 */
@JsonSerialize(using = SimpleJsonSerializer.class)
public class AnalysisInProgress {
    public int value = 0;

    public AnalysisInProgress(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
