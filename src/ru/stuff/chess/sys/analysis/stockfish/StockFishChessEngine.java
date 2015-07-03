package ru.stuff.chess.sys.analysis.stockfish;

import chesspresso.Chess;
import chesspresso.move.IllegalMoveException;
import chesspresso.position.Position;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import ru.stuff.chess.sys.analysis.engine.ChessEngine;
import ru.stuff.chess.sys.analysis.engine.MoveAnalysis;
import ru.stuff.chess.sys.analysis.engine.UciInfo;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Created by mark on 05.05.15.
 */
@Component
public class StockFishChessEngine implements ChessEngine {
    private static final Logger log = Logger.getLogger(StockFishChessEngine.class);

    private static boolean isSupported = true;

    private final ExecutorService stockfishPool = Executors.newFixedThreadPool(2, r -> {
        StockfishProcess sf = new StockfishProcess(r);
        //If any init errors -> this engine not supported
        try {
            sf.init();
        } catch (Exception e) {
            isSupported = false;
            log.error("StockFish chess engine disabled ...",e);
        }
        return sf;
    });

    @Override
    public void findBestMove(String fen, Duration clock, Consumer<UciInfo> callback) {
        stockfishPool.submit(() -> {
            if (Thread.currentThread() instanceof StockfishProcess) {
                StockfishProcess sf = (StockfishProcess) (Thread.currentThread());
                try {
                    UciInfo info = sf.getBestMove(fen, new Position(fen).getToPlay() == Chess.WHITE, clock.toMillis());
                    callback.accept(info);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void analysisGame(String initialPos,
                             Collection<String> moves,
                             Consumer<List<MoveAnalysis>> callback,
                             IntConsumer progressCallback) {
        stockfishPool.submit(() -> {
            if (Thread.currentThread() instanceof StockfishProcess) {
                StockfishProcess sf = (StockfishProcess) (Thread.currentThread());
                try {
                    log.info("analysis started!");
                    List<MoveAnalysis> result = sf.analysisGame(initialPos, moves, progressCallback);
                    log.info("analysis done!");
                    callback.accept(result);

                } catch (IOException | IllegalMoveException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public boolean isSupported() {
        return isSupported;
    }

    @PreDestroy
    public void clean() {
        stockfishPool.shutdownNow();
    }
}
