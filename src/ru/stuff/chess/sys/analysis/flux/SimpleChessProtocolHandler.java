package ru.stuff.chess.sys.analysis.flux;

import com.fluxchess.jcpi.commands.*;
import com.fluxchess.jcpi.protocols.IProtocolHandler;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.BiConsumer;

/**
 * Created by mark on 17.02.15.
 */
public class SimpleChessProtocolHandler implements IProtocolHandler {
    //analysisInfo заполняется по мере поступления результатов анализа от движка, потом обнуляется
    private LinkedList<ProtocolInformationCommand> analysisInfo = new LinkedList<>();

    private Collection<BiConsumer<ProtocolBestMoveCommand, ProtocolInformationCommand>> analysisDoneListeners
            = new LinkedList<>();

    private int maxDepth;

    public SimpleChessProtocolHandler(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    @Override
    public IEngineCommand receive() throws IOException {
        return null;
    }

    @Override
    public void send(ProtocolInitializeAnswerCommand protocolInitializeAnswerCommand) {

    }

    @Override
    public void send(ProtocolReadyAnswerCommand protocolReadyAnswerCommand) {

    }

    @Override
    public void send(ProtocolBestMoveCommand protocolBestMoveCommand) {
        ProtocolInformationCommand deeperMainLineAnalysis = null;
        System.out.println("BestMoveCommand received" + protocolBestMoveCommand.bestMove);
        int maxMoveChainLen = 0;
        for (ProtocolInformationCommand infoCmd : analysisInfo) {
            if ((infoCmd.getMoveList() != null) &&
                    (infoCmd.getMoveList().get(0).equals(protocolBestMoveCommand.bestMove) &&
                            (infoCmd.getDepth().equals(maxDepth) || infoCmd.getMate() != null))) {
                if (infoCmd.getMoveList().size() >= maxMoveChainLen) {
                    deeperMainLineAnalysis = infoCmd;
                    maxMoveChainLen = infoCmd.getMoveList().size();
                }

            }
        }
        if (deeperMainLineAnalysis != null)
            fireAnalysisDone(protocolBestMoveCommand, deeperMainLineAnalysis);

        analysisInfo.clear();
    }

    @Override
    public void send(ProtocolInformationCommand protocolInformationCommand) {
        analysisInfo.add(protocolInformationCommand);
    }

    public void addAnalysisDoneListener(BiConsumer<ProtocolBestMoveCommand, ProtocolInformationCommand> consumer) {
        analysisDoneListeners.add(consumer);
    }

    public void removeAnalysisDoneListener(BiConsumer<ProtocolBestMoveCommand, ProtocolInformationCommand> consumer) {
        analysisDoneListeners.remove(consumer);
    }

    private void fireAnalysisDone(ProtocolBestMoveCommand bm, ProtocolInformationCommand info) {
        analysisDoneListeners.forEach(c -> {
            c.accept(bm, info);
        });
        analysisDoneListeners.clear(); //Удаляем всех подписчиков.
    }
}
