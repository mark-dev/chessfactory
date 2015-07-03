package ru.stuff.chess.sys.analysis.engine;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by mark on 06.05.15.
 */
public class UciInfo {
    private int depth;
    private int seldepth;
    private int multipv;
    private int score; //centipawns or mate value
    private boolean isMate = false; //If true -> score contains move number for mate
    private int nodes;
    private int nps;
    private int tbhits;
    private int time;
    private Collection<String> moves;

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getSeldepth() {
        return seldepth;
    }

    public void setSeldepth(int seldepth) {
        this.seldepth = seldepth;
    }

    public int getMultipv() {
        return multipv;
    }

    public void setMultipv(int multipv) {
        this.multipv = multipv;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isMate() {
        return isMate;
    }

    public void setMate(boolean isMate) {
        this.isMate = isMate;
    }

    public int getNodes() {
        return nodes;
    }

    public void setNodes(int nodes) {
        this.nodes = nodes;
    }

    public int getNps() {
        return nps;
    }

    public void setNps(int nps) {
        this.nps = nps;
    }

    public int getTbhits() {
        return tbhits;
    }

    public void setTbhits(int tbhits) {
        this.tbhits = tbhits;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public Collection<String> getMoves() {
        return moves;
    }

    public String getFirstMove() {
        return moves.iterator().next();
    }

    public void setMoves(Collection<String> moves) {
        this.moves = moves;
    }

    @Override
    public String toString() {
        return "UciInfo{" +
                "depth=" + depth +
                ", seldepth=" + seldepth +
                ", multipv=" + multipv +
                ", score=" + score +
                ", isMate=" + isMate +
                ", nodes=" + nodes +
                ", nps=" + nps +
                ", tbhits=" + tbhits +
                ", time=" + time +
                ", moves=" + moves +
                '}';
    }

    public static UciInfo fromInfoLine(String line) {
        UciInfo info = new UciInfo();
        String[] tokens = line.split(" ");
        //Skip first value ("info")
        for (int i = 1; i < tokens.length; i++) {
            switch (tokens[i]) {
                case "depth":
                    info.setDepth(Integer.parseInt(tokens[i + 1]));
                    i = i + 1;
                    break;
                case "seldepth":
                    info.setSeldepth(Integer.parseInt(tokens[i + 1]));
                    i = i + 1;
                    break;
                case "multipv":
                    info.setMultipv(Integer.parseInt(tokens[i + 1]));
                    i = i + 1;
                    break;
                case "nodes":
                    info.setNodes(Integer.parseInt(tokens[i + 1]));
                    i = i + 1;
                    break;
                case "nps":
                    info.setNps(Integer.parseInt(tokens[i + 1]));
                    i = i + 1;
                    break;
                case "tbhits":
                    info.setTbhits(Integer.parseInt(tokens[i + 1]));
                    i = i + 1;
                    break;
                case "time":
                    info.setTime(Integer.parseInt(tokens[i + 1]));
                    i = i + 1;
                    break;
                case "score":
                    //tokens[i+1] = cp | mate
                    info.setScore(Integer.parseInt(tokens[i + 2]));
                    info.setMate(tokens[i + 1].equals("mate"));
                    i = i + 2;
                    break;
                case "pv":
                    LinkedList<String> moves = new LinkedList<>();
                    moves.addAll(Arrays.asList(tokens).subList(i + 1, tokens.length));
                    info.setMoves(moves);
                    i = tokens.length; //Quit
                    break;
                default:
                    break;
            }
        }
        return info;
    }
}
