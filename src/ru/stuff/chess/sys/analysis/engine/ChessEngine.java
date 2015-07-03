package ru.stuff.chess.sys.analysis.engine;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Created by mark on 05.05.15.
 */
public interface ChessEngine {
    public void findBestMove(String fen, Duration clock, Consumer<UciInfo> callback);

    public void analysisGame(String initialPos,
                             Collection<String> moves,
                             Consumer<List<MoveAnalysis>> callback,
                             IntConsumer progress
                             );

    public boolean isSupported();
}
