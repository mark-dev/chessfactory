package ru.stuff.chess.sys.analysis.engine;

import java.util.LinkedList;

/**
 * Created by mark on 06.05.15.
 */
public class MoveAnalysis {
    private String move;
    private LinkedList<String> bestMoveSan;
    private UciInfo bestMove;
    private UciInfo played;

    public MoveAnalysis(String move, LinkedList<String> bestMoveSan, UciInfo bestMove, UciInfo played) {
        this.move = move;
        this.bestMoveSan = bestMoveSan;
        this.bestMove = bestMove;
        this.played = played;
    }

    public LinkedList<String> getBestMoveSeqSan() {
        return bestMoveSan;
    }

    public UciInfo getBestMove() {
        return bestMove;
    }

    public UciInfo getPlayed() {
        return played;
    }

    public String getMove() {
        return move;
    }
}
