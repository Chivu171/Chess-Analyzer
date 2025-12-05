package com.myproject.Logic;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.Arrays;
import java.util.List;

public class GameController {
    private Board board;

    public GameController(){
        this.board= new Board();
    }
    // lay chuoi fen hien tai cua ban co
    public String getCurrentFen(){
         return board.getFen();
    }



    // Input theo huong 2 thao tac tung buoc
    public boolean doMove (String uciMove){
        try{
        String fromPosition = uciMove.substring(0, 2).toUpperCase();
        String toPosition = uciMove.substring(2,4).toUpperCase();
        Square from = Square.fromValue(fromPosition);
        Square to = Square.fromValue(toPosition);
        List<Move> legalMoves = board.legalMoves();
            
            for (Move legalMove : legalMoves) {
                // Kiểm tra xem nước đi người dùng nhập có khớp với nước hợp lệ nào không
                if (legalMove.getFrom() == from && legalMove.getTo() == to) {
                    // Tìm thấy! Thực hiện nước đi này (để tự động xử lý phong cấp nếu có)
                    board.doMove(legalMove);
                    return true;
                }
            }

            // Nếu chạy hết vòng lặp mà không thấy khớp -> Nước đi sai luật
            System.err.println("Nước đi không hợp lệ (Không nằm trong legalMoves): " + uciMove);
            return false;
        }
        catch (Exception e){
            System.err.println("Lỗi khi thực hiện nước đi: " + e.getMessage());
            return false;

        }

    }

    //Input theo huong 1
    public void loadMoveList (String moveList){
        this.board = new Board();
        if (moveList.isEmpty()||moveList ==null){
            return;
        }
        String[] moves = moveList.trim().split("\\s+"); // Tách bằng khoảng trắng

        for (String x : moves){
            boolean success = doMove(x);
            if (!success) {
                System.out.println("Dừng load tại nước đi lỗi: " + x);
                break; // Dừng lại nếu gặp nước đi sai
            }
        }
    }
    public void resetGame() {
        this.board = new Board();
    }

    /**
     * Lấy đối tượng Board của chesslib (để các thuật toán sử dụng nếu cần)
     */
    public Board getBoard() {
        return this.board;
    }
}

