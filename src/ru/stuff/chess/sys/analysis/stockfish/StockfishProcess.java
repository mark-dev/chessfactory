package ru.stuff.chess.sys.analysis.stockfish;

import chesspresso.Chess;
import chesspresso.move.IllegalMoveException;
import chesspresso.position.Position;
import org.apache.log4j.Logger;
import ru.stuff.chess.sys.analysis.engine.MoveAnalysis;
import ru.stuff.chess.sys.analysis.engine.UciInfo;

import java.io.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Created by mark on 05.05.15.
 */
public class StockfishProcess extends Thread {
    private static final Logger log = Logger.getLogger(StockFishChessEngine.class);
    private static final String COMMAND = "/usr/local/bin/stockfish";
    private Process stockfish;
    private BufferedReader out;
    private BufferedWriter in;

    public void init() throws Exception {
        log.info("try spawn stockfish process...");
        ProcessBuilder pb = new ProcessBuilder(COMMAND);
        stockfish = pb.start();
        out = new BufferedReader(new InputStreamReader(stockfish.getInputStream()));
        in = new BufferedWriter(new OutputStreamWriter(stockfish.getOutputStream()));
    }

    private void terminate() {
        log.info("terminate called..");
        stockfish.destroy();
    }

    public StockfishProcess(Runnable r) {
        super(r);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        terminate();
    }

    public void sendCommand(String cmd) throws IOException {
        log.info("sendCommand -> " + cmd);
        in.write(cmd);
        in.newLine();
        in.flush();
    }

    private void makeMove(Position pos, String amove) throws IllegalMoveException {
        short move = pos.getMove(Chess.strToSqi(amove.substring(0, 2)),
                Chess.strToSqi(amove.substring(2, 4)),
                amove.length() == 5 ? Chess.charToPiece(Character.toUpperCase(amove.charAt(4))) : Chess.NO_PIECE);
        pos.doMove(move);
    }

    public List<MoveAnalysis> analysisGame(String startpos,
                                           Collection<String> moves,
                                           IntConsumer progressCallback) throws IOException, IllegalMoveException {
        int depth = 12;
        sendCommand("ucinewgame");

        Position position;
        if (startpos.isEmpty()) {
            sendCommand("position startpos");
            position = Position.createInitialPosition();
        } else {
            sendCommand("position fen " + startpos);
            position = new Position(startpos);
        }

        LinkedList<MoveAnalysis> analysis = new LinkedList<>();
        int totalMoves = moves.size();
        int currentMove = 0;
        for (String s : moves) {

            //Search best move
            sendCommand("go depth " + depth);
            UciInfo bestMove = readDeeperAnalysis();

            //Got estimate for user move
            sendCommand(String.format("go depth %d searchmoves %s", depth, s));
            UciInfo userMove = readDeeperAnalysis();


            //Fetch best san move sequence
            LinkedList<String> bestMoveSeqSan = new LinkedList<>();
            if (!bestMove.getMoves().isEmpty()) {
                //Make all best move seq moves
                for (String bm : bestMove.getMoves()) {
                    makeMove(position, bm);
                    bestMoveSeqSan.add(position.getLastMove().getSAN());
                }
                //Undo all best move seq moves
                for (int i = 0; i < bestMove.getMoves().size(); i++) {
                    position.undoMove();
                }
            }
            //Make main line move
            makeMove(position, s);
            analysis.add(new MoveAnalysis(position.getLastMove().getSAN(), bestMoveSeqSan, bestMove, userMove));
            //sendCommand("ucinewgame");
            sendCommand("position fen " + position.getFEN());

            currentMove++;
            progressCallback.accept((currentMove * 100 / totalMoves));
        }
        return analysis;
    }

    public UciInfo getBestMove(String fen, boolean isWhiteTurn, long clock) throws IOException {
        sendCommand("ucinewgame");
        sendCommand("position fen " + fen);
        sendCommand(String.format("go depth %d %stime %d", 8, isWhiteTurn ? "w" : "b", clock));

        return readDeeperAnalysis();
    }

    private UciInfo readDeeperAnalysis() throws IOException {
        String infoDepth = "";
        String line;
        //Search line before line "bestmove  ..."
        do {
            line = out.readLine();
            if (line.startsWith("info depth"))
                infoDepth = line;
        } while (!line.startsWith("bestmove"));
        return UciInfo.fromInfoLine(infoDepth);
    }

}
