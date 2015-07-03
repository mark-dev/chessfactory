package ru.stuff.chess.sys.analysis.flux;

import com.fluxchess.jcpi.commands.ProtocolBestMoveCommand;
import com.fluxchess.jcpi.commands.ProtocolInformationCommand;
import com.fluxchess.jcpi.models.GenericBoard;
import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.models.IllegalNotationException;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import ru.stuff.chess.sys.analysis.engine.ChessEngine;
import ru.stuff.chess.sys.analysis.engine.MoveAnalysis;
import ru.stuff.chess.sys.analysis.engine.UciInfo;
import ru.stuff.chess.sys.analysis.stockfish.StockFishChessEngine;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * Created by mark on 19.02.15.
 */
@Component
public class FluxChessEngine implements ChessEngine {
    private static final Logger log = Logger.getLogger(FluxChessEngine.class);

    private ExecutorService fluxWrapperService = Executors.newFixedThreadPool(2, FluxWrapper::new);


    public void scheduleAnalyseMove(GenericBoard position,
                                    GenericMove move,
                                    BiConsumer<ProtocolBestMoveCommand, ProtocolInformationCommand> callBack) {
        fluxWrapperService.submit(() -> {
            if (Thread.currentThread() instanceof FluxWrapper) {
                FluxWrapper selfFlux = (FluxWrapper) Thread.currentThread();
                selfFlux.analysisMove(position, move, (bm, info) -> {
                    callBack.accept(bm, info);
                    selfFlux.setTaskCompleted(true);
                });
                selfFlux.lockUntilTaskIsCompleted();
            }
        });
    }


    @PreDestroy
    public void clean() {
        fluxWrapperService.shutdownNow();
    }

    @Override
    public void findBestMove(String fen, Duration clock, Consumer<UciInfo> callback) {
        try {
            GenericBoard board = new GenericBoard(fen);
            fluxWrapperService.submit(() -> {
                if (Thread.currentThread() instanceof FluxWrapper) {
                    FluxWrapper selfFlux = (FluxWrapper) Thread.currentThread();
                    selfFlux.findBestMove(board, clock, (bm, info) -> {
                        UciInfo result = new UciInfo();

                        if (info.getMate() != null) {
                            result.setMate(true);
                            result.setScore(info.getMate());
                        } else {
                            result.setScore(info.getCentipawns());
                        }

                        //Convert bestMoveSequence(Flux's GenericMove collection) to String array (Algebraic moves notation)
                        Stream<String> algebraicMoves = info.getMoveList().stream().map(new Function<GenericMove, String>() {
                            @Override
                            public String apply(GenericMove move) {
                                return move.toString();
                            }
                        });
                        Collection<String> moves = new LinkedList<String>();
                        Collections.addAll(moves, algebraicMoves.toArray(String[]::new));
                        result.setMoves(moves);

                        callback.accept(result);
                        selfFlux.setTaskCompleted(true);
                    });
                    selfFlux.lockUntilTaskIsCompleted();
                }
            });
        } catch (IllegalNotationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void analysisGame(String initialPos,
                             Collection<String> moves,
                             Consumer<List<MoveAnalysis>> callback,
                             IntConsumer progress)
    {
        log.warn("flux game analysis not implemented yet.");
    }

    @Override
    public boolean isSupported() {
        return true;
    }

}

