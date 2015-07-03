package ru.stuff.chess.sys.analysis.flux;

import com.fluxchess.flux.Flux;
import com.fluxchess.jcpi.commands.*;
import com.fluxchess.jcpi.models.GenericBoard;
import com.fluxchess.jcpi.models.GenericMove;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.BiConsumer;

/**
 * Created by mark on 17.02.15.
 */
public class FluxWrapper extends Thread {
    private Flux fluxEngine;
    private SimpleChessProtocolHandler handler;
    private static final int MAX_DEPTH = 5;
    private volatile boolean isTaskCompleted = true;

    public boolean isTaskCompleted() {
        return isTaskCompleted;
    }

    public void lockUntilTaskIsCompleted() {
        while (!isTaskCompleted()) {
            if (isTaskCompleted) {
                break;
            }
        }
    }


    public FluxWrapper(Runnable r) {
        super(r);
        System.out.println("Flux created!");
        handler = new SimpleChessProtocolHandler(MAX_DEPTH);
        fluxEngine = new Flux(handler);
    }

    public void setTaskCompleted(boolean isTaskCompleted) {
        this.isTaskCompleted = isTaskCompleted;
    }

    //Здесь важно не подавать одновременно несколько findBestMove
    //Т.е. подали 1 - подождали пока consumer отработает, подали 2.. итп
    public void findBestMove(GenericBoard pos, Duration clock, BiConsumer<ProtocolBestMoveCommand, ProtocolInformationCommand> consumer) {
        isTaskCompleted = false;
        EngineAnalyzeCommand analyze = new EngineAnalyzeCommand(pos, Collections.emptyList());
        EngineStartCalculatingCommand calc = new EngineStartCalculatingCommand();
        calc.setDepth(MAX_DEPTH);
        calc.setClock(pos.getActiveColor(), clock.toMillis());

        handler.addAnalysisDoneListener(consumer);
        fluxEngine.receive(new EngineNewGameCommand());
        fluxEngine.receive(analyze);
        fluxEngine.receive(calc);
    }

    public void analysisMove(GenericBoard pos, GenericMove move,
                             BiConsumer<ProtocolBestMoveCommand, ProtocolInformationCommand> consumer) {
        isTaskCompleted = false;
        System.out.println("Analyze move: " + move.toString());
        EngineAnalyzeCommand analyze = new EngineAnalyzeCommand(pos, Collections.emptyList());
        EngineStartCalculatingCommand calc = new EngineStartCalculatingCommand();
        calc.setDepth(MAX_DEPTH);
        calc.setSearchMoveList(Arrays.asList(move));

        handler.addAnalysisDoneListener(consumer);

        fluxEngine.receive(new EngineNewGameCommand());
        fluxEngine.receive(analyze);
        fluxEngine.receive(calc);

    }
}
