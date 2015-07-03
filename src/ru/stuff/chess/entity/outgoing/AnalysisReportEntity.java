package ru.stuff.chess.entity.outgoing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.stuff.chess.entity.jsonutils.OptionalField;
import ru.stuff.chess.entity.jsonutils.SimpleJsonSerializer;
import ru.stuff.chess.entity.jsonutils.WithoutEventType;
import ru.stuff.chess.sys.analysis.report.MoveType;

import java.util.Collection;

/**
 * Created by mark on 08.05.15.
 */
@JsonSerialize(using = SimpleJsonSerializer.class)
@WithoutEventType
public class AnalysisReportEntity {
    public String san;
    public float score;
    public MoveType type;

    @OptionalField
    public Integer mateIn;

    @OptionalField
    public Collection<String> best;

    public AnalysisReportEntity(String san, float score) {
        this.san = san;
        this.score = score;
        this.type = MoveType.REGULAR;
    }

    public AnalysisReportEntity(String san, float score, MoveType type, Collection<String> best) {
        this.san = san;
        this.score = score;
        this.best = best;
        this.type = type;
    }

    public String getSan() {
        return san;
    }

    public void setSan(String san) {
        this.san = san;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public Collection<String> getBest() {
        return best;
    }

    public void setBest(Collection<String> best) {
        this.best = best;
    }

    public MoveType getType() {
        return type;
    }

    public void setType(MoveType type) {
        this.type = type;
    }

    public Integer getMateIn() {
        return mateIn;
    }

    public void setMateIn(Integer mateIn) {
        this.mateIn = mateIn;
    }
}

