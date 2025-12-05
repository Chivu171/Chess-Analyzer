package com.myproject.Logic;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.Piece; 
import com.github.bhlangonijr.chesslib.Square;

import java.util.List;

public class TreeAnalyzer {

    private static final int MAX_DEPTH = 3; // Độ sâu phân tích
    
    private Side analyzingSide; 

    public AnalysisNode buildGameTree(Board board) {
        this.analyzingSide = board.getSideToMove();
        AnalysisNode root = new AnalysisNode(null);
        expandNode(root, board, MAX_DEPTH);
        return root;
    }   

    private void expandNode(AnalysisNode parentNode, Board board, int depth) {
        // Điều kiện dừng
        if (depth == 0 || board.isMated() || board.isDraw()) { 
            evaluateLeafNode(parentNode, board);
            return;
        }
        // Lấy tất cả nước đi hợp lệ
        List<Move> legalMoves = board.legalMoves();
        
        // Trường hợp hết nước đi (Stalemate - Hòa cờ)
        if (legalMoves.isEmpty()) {
            evaluateLeafNode(parentNode, board);
            return;
        }

        // Duyệt từng nhánh (Từng nước đi có thể)
        for (Move move : legalMoves) {
            board.doMove(move); 
            
            AnalysisNode childNode = new AnalysisNode(move);
            expandNode(childNode, board, depth - 1);
            // Thêm node con vào node cha
            parentNode.addChildren(childNode); 
            board.undoMove(); // Hoàn tác để thử nước khác (Backtracking)
        }
    }

    private void evaluateLeafNode(AnalysisNode node, Board board) {
        if (board.isMated()) {
            // Bên bị chiếu hết là bên đang có lượt đi (getSideToMove)
            // Nếu bên bị chiếu hết KHÁC với phe ta -> Phe ta thắng
            if (board.getSideToMove() != this.analyzingSide) {
                node.setStats(1, 0, 0); // Thắng
            } else {
                node.setStats(0, 1, 0); // Thua
            }
            return;
        }

        if (board.isDraw()) {
            node.setStats(0, 0, 1);
            return;
        }

        // 3. Xử lý Điểm số ước lượng (Heuristic)
        int score = simpleEvaluate(board);
        int threshold = 200; // Ngưỡng 200 điểm (khoảng 2 quân Tốt)
        
        // Logic tính điểm tùy thuộc vào phe ta là Trắng hay Đen
        if (this.analyzingSide == Side.WHITE) {
            // Nếu ta là Trắng: Điểm càng Dương càng tốt
            if (score > threshold) {
                node.setStats(1, 0, 0); // Dự đoán Thắng
            } else if (score < -threshold) {
                node.setStats(0, 1, 0); // Dự đoán Thua
            } else {
                node.setStats(0, 0, 1); // Dự đoán Hòa
            }
        } else {
            // Nếu ta là Đen: Điểm càng ÂM càng tốt (ví dụ -500 là Đen đang thắng thế)
            if (score < -threshold) {
                node.setStats(1, 0, 0); // Dự đoán Thắng (cho Đen)
            } else if (score > threshold) {
                node.setStats(0, 1, 0); // Dự đoán Thua (vì Trắng điểm cao quá)
            } else {
                node.setStats(0, 0, 1); // Dự đoán Hòa
            }
        }
    }

    private int simpleEvaluate(Board board) {
        int score = 0;
        for (Square sq : Square.values()) {
            if (sq == Square.NONE) continue;
            Piece p = board.getPiece(sq);
            if (p == Piece.NONE) continue;
            
            int value = getPieceValue(p);
            if (p.getPieceSide() == Side.WHITE) score += value;
            else score -= value;
        }
        return score;
    }

    private int getPieceValue(Piece p) {
        String type = p.getPieceType().name(); 
        switch (type) {
            case "PAWN": return 100;
            case "KNIGHT": return 300;
            case "BISHOP": return 320;
            case "ROOK": return 500;
            case "QUEEN": return 900;
            case "KING": return 20000;
            default: return 0;
        }
    }
}