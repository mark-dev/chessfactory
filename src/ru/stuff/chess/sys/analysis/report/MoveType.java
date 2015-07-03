package ru.stuff.chess.sys.analysis.report;

/**
 * Created by mark on 08.05.15.
 */
public enum MoveType {
    REGULAR, // OK
    BLUNDER, //Centipwans diff
    BLUNDER_MU, //Mate unavoidable now
    MISTAKE,
    INACCURACY,
    MATE_DELAYED,
    MATE_LOST
}
