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


    // THAY THẾ TOÀN BỘ PHƯƠNG THỨC NÀY
    public boolean doMove (String uciMove){
        try{
            String moveCheck = uciMove.toLowerCase(); 
            List<Move> legalMoves = board.legalMoves();
            
            // 1. KIỂM TRA TRƯỜNG HỢP ĐẶC BIỆT: PHONG CẤP (5 ký tự: e7e8q)
            if (moveCheck.length() == 5) {
                for (Move legalMove : legalMoves) {
                    if (legalMove.toString().equals(moveCheck)) { 
                        board.doMove(legalMove);
                        return true;
                    }
                }
            } 
            // 2. KIỂM TRA TRƯỜNG HỢP THÔNG THƯỜNG (4 ký tự: c7c1)
            else if (moveCheck.length() == 4) {
                String fromPosition = moveCheck.substring(0, 2).toUpperCase();
                String toPosition = moveCheck.substring(2, 4).toUpperCase();
                Square from = Square.fromValue(fromPosition);
                Square to = Square.fromValue(toPosition);
                
                for (Move legalMove : legalMoves) {
                    if (legalMove.getFrom() == from && legalMove.getTo() == to) {
                        board.doMove(legalMove); 
                        return true;
                    }
                }
            }
            
            // Nếu không hợp lệ hoặc độ dài sai
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
        // Không reset ở đây nữa, vì đã được SimpleChessGui xử lý bằng cách tải FEN gốc
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
    
    public boolean loadFen(String fen) {
        try {
            this.board.loadFromFen(fen);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi tải FEN: " + e.getMessage());
            return false;
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