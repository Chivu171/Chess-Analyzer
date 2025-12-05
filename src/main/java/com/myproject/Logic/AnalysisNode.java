package com.myproject.Logic;
import com.github.bhlangonijr.chesslib.move.Move;
import java.util.ArrayList;
import java.util.List;

public class AnalysisNode {
    private Move move;
    private int winCount =0;
    private int lossCount =0;
    private int drawCount = 0;
    private List<AnalysisNode> children = new ArrayList<>();

    public AnalysisNode (Move move){
        this.move = move;
        this.children = new ArrayList<>();
    }
    public void addChildren(AnalysisNode child){
        this.children.add(child);
        this.winCount+= child.winCount;
        this.lossCount+= child.lossCount;
        this.drawCount+= child.drawCount;
    }
    public Move getMove() { return move; }
    public List<AnalysisNode> getChildren() { return children; }
    public int getWinCount() { return winCount; }
    public int getLossCount() { return lossCount; }
    public int getDrawCount() { return drawCount; }
    public int getTotalCases() { return winCount + lossCount + drawCount; }
    
    public void setStats(int w, int l, int d) {
        this.winCount = w;
        this.lossCount = l;
        this.drawCount = d;
    }

    public double getWinRate() {
        if (getTotalCases() == 0) return 0.0;
        return (double) winCount / getTotalCases() * 100.0;
    }
    @Override
    public String toString() {
        // Hiển thị text trên cây: "e2e4 (W:40% D:10% L:50%)"
        if (move == null) return "Start";
        return String.format("%s (W:%d L:%d D:%d)", move.toString(), winCount, lossCount, drawCount);
    }
}

