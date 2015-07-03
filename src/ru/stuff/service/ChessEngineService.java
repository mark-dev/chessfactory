package ru.stuff.service;

import chesspresso.Chess;
import chesspresso.position.Position;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.stuff.chess.sys.analysis.engine.MoveAnalysis;
import ru.stuff.chess.sys.analysis.engine.UciInfo;
import ru.stuff.chess.sys.analysis.flux.FluxChessEngine;
import ru.stuff.chess.sys.analysis.stockfish.StockFishChessEngine;
import ru.stuff.chess.sys.game.GameInfo;

import java.time.Duration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Created by mark on 05.05.15.
 */
@Component
public class ChessEngineService {
    private static final Logger log = Logger.getLogger(ChessEngineService.class);

    @Autowired
    private FluxChessEngine flux;
    @Autowired
    private StockFishChessEngine stockfish;

    public void scheduleBestMove(String fen, Duration clock, Consumer<UciInfo> callback) {
        if (stockfish.isSupported()) {
            log.info(String.format("scheduleBestMove(%s) -> stockfish", fen));
            stockfish.findBestMove(fen, clock, callback);
        } else if (flux.isSupported()) {
            flux.findBestMove(fen, clock, callback);
        }
    }

    public void scheduleAnalysis(GameInfo game, Consumer<List<MoveAnalysis>> callback, IntConsumer progress) {
        Position startPos = game.getInitialPosition();
        boolean isWhiteTurn = startPos.getToPlay() == Chess.WHITE;

        //Construct List<String> - move sequence for analysis
        List<String> whiteMoves = game.getWhiteMoves();
        List<String> blackMoves = game.getBlackMoves();
        Iterator<String> firstIt = whiteMoves.iterator();
        Iterator<String> secondIt = blackMoves.iterator();

        if (!isWhiteTurn) {
            secondIt = whiteMoves.iterator();
            firstIt = blackMoves.iterator();
        }
        List<String> moves = new LinkedList<>();

        //size(firstIt) - size(secondIt) == 1 | 0
        while (firstIt.hasNext()) {
            moves.add(firstIt.next());
            if (secondIt.hasNext())
                moves.add(secondIt.next());
        }

        if (stockfish.isSupported())
            stockfish.analysisGame(startPos.getFEN(), moves, callback, progress);
        else if (flux.isSupported())
            flux.analysisGame(startPos.getFEN(), moves, callback, progress);
    }

}
