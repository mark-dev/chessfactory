package ru.stuff.chess.entity.outgoing;

/**
 * Created by mark on 08.05.15.
 */

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.stuff.chess.entity.jsonutils.SimpleJsonSerializer;

import java.util.Collection;


@JsonSerialize(using = SimpleJsonSerializer.class)
public class AnalysisReport {
    public String startpos;
    public Collection<AnalysisReportEntity> analysis;

    public AnalysisReport(String startpos, Collection<AnalysisReportEntity> analysis) {
        this.startpos = startpos;
        this.analysis = analysis;
    }

    public String getStartpos() {
        return startpos;
    }

    public void setStartpos(String startpos) {
        this.startpos = startpos;
    }

    public Collection<AnalysisReportEntity> getAnalysis() {
        return analysis;
    }

    public void setAnalysis(Collection<AnalysisReportEntity> analysis) {
        this.analysis = analysis;
    }
}
