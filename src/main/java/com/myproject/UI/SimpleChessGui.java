package com.myproject.UI;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.myproject.Logic.AnalysisNode;
import com.myproject.Logic.GameController;
import com.myproject.Logic.TreeAnalyzer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;

public class SimpleChessGui extends JFrame {

    private final GameController gameController;
    private final JButton[][] squares = new JButton[8][8];
    private final JTextField inputField;
    private final JTextArea logArea;

    private Square selectedSquare = null; 

    private final Color lightColor = new Color(240, 217, 181);
    private final Color darkColor = new Color(181, 136, 99);
    private final Color selectedColor = new Color(255, 255, 51); 
    
    private final JTextField fenInputField; 
    
    // *** KHAI BÁO BIẾN MỚI ĐỂ LƯU FEN GỐC PHÂN TÍCH ***
    private String analysisStartFen = null; 
    // ***************************************************

    public SimpleChessGui() {
        this.gameController = new GameController();
        
        setTitle("My Chess App (Simple GUI)");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. Panel bàn cờ
        JPanel boardPanel = new JPanel(new GridLayout(8, 8));
        initializeBoard(boardPanel);
        add(boardPanel, BorderLayout.CENTER);

        // 2. TẠO PANEL CHÍNH PHÍA DƯỚI (SOUTH PANEL) 
        JPanel southPanel = new JPanel(new GridLayout(2, 1)); 

        // A. Panel nhập FEN (Hàng 1)
        JPanel fenPanel = new JPanel(new BorderLayout());
        fenInputField = new JTextField();
        fenInputField.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JButton btnLoadFen = new JButton("Load FEN");
        
        fenPanel.add(new JLabel(" Nhập FEN: "), BorderLayout.WEST);
        fenPanel.add(fenInputField, BorderLayout.CENTER);
        fenPanel.add(btnLoadFen, BorderLayout.EAST);
        
        // B. Panel nhập nước đi (Hàng 2)
        JPanel moveInputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        inputField.setFont(new Font("Monospaced", Font.BOLD, 14));
        
        JButton btnReset = new JButton("Reset Game");
        
        moveInputPanel.add(new JLabel(" Nhập nước đi (UCI): "), BorderLayout.WEST);
        moveInputPanel.add(inputField, BorderLayout.CENTER);
        moveInputPanel.add(btnReset, BorderLayout.EAST);
        
        JButton btnPredict = new JButton("🔮 Dự đoán");
        moveInputPanel.add(btnPredict, BorderLayout.NORTH); 
        
        // Thêm vào South Panel
        southPanel.add(fenPanel); 
        southPanel.add(moveInputPanel);
        add(southPanel, BorderLayout.SOUTH);
        

        // 3. Panel Log
        logArea = new JTextArea(20, 15);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(logArea), BorderLayout.EAST);

        // --- Xử lý sự kiện bấm nút Dự đoán ---
        btnPredict.addActionListener(e -> {
            new Thread(() -> {
                SwingUtilities.invokeLater(() -> {
                    btnPredict.setEnabled(false);
                    btnPredict.setText("Đang tính...");
                });
                
                try {
                    // *** BƯỚC 1: LƯU FEN GỐC TRƯỚC KHI PHÂN TÍCH ***
                    analysisStartFen = gameController.getBoard().getFen(); 
                    // ************************************************
                    
                    TreeAnalyzer analyzer = new TreeAnalyzer();
                    Board analysisBoard = new Board();
                    // Load trạng thái bàn cờ hiện tại để phân tích
                    analysisBoard.loadFromFen(analysisStartFen); 

                    AnalysisNode rootResult = analyzer.buildGameTree(analysisBoard);

                    SwingUtilities.invokeLater(() -> {
                        new TreeDialog(this, rootResult).setVisible(true); 
                    });
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        btnPredict.setEnabled(true);
                        btnPredict.setText("🔮 Dự đoán");
                    });
                }
            }).start();
        });
        
        // --- XỬ LÝ SỰ KIỆN LOAD FEN ---
        btnLoadFen.addActionListener(e -> {
            String fen = fenInputField.getText().trim();
            if (fen.isEmpty()) {
                logArea.append("FEN không được để trống!\n");
                return;
            }
            
            boolean success = gameController.loadFen(fen);
            if (success) {
                logArea.setText("FEN Loaded: " + fen + "\n");
                selectedSquare = null; 
                updateBoardUI();
            } else {
                 JOptionPane.showMessageDialog(this, 
                    "Chuỗi FEN không hợp lệ.", 
                    "Lỗi FEN", 
                    JOptionPane.ERROR_MESSAGE);
                 logArea.append("Lỗi tải FEN.\n");
            }
        });
        // --- Xử lý sự kiện Nhập nước đi (Enter) ---
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String moveText = inputField.getText().trim();
                if (!moveText.isEmpty()) {
                    processMove(moveText);
                    inputField.setText(""); 
                }
            }
        });

        // --- Xử lý sự kiện Reset ---
        btnReset.addActionListener(e -> {
            gameController.resetGame();
            selectedSquare = null; 
            updateBoardUI();
            logArea.setText("Game reset!\n");
        });

        updateBoardUI();
        logArea.append("Game started.\n");
        setVisible(true);
    }

    private void initializeBoard(JPanel boardPanel) {
        for (int rank = 7; rank >= 0; rank--) { 
            for (int file = 0; file < 8; file++) { 
                JButton btn = new JButton();
                btn.setFont(new Font("Serif", Font.PLAIN, 50)); 
                btn.setFocusPainted(false);
                
                int finalRank = rank; 
                int finalFile = file;

                btn.addActionListener(e -> handleSquareClick(finalRank, finalFile));

                if ((rank + file) % 2 != 0) {
                    btn.setBackground(lightColor);
                } else {
                    btn.setBackground(darkColor);
                }

                squares[rank][file] = btn;
                boardPanel.add(btn);
            }
        }
    }
    
    private void handleSquareClick(int rank, int file) {
        Square clickedSquare = Square.squareAt(rank * 8 + file); 

        // TRƯỜNG HỢP 1: Chọn quân (Click lần 1)
        if (selectedSquare == null) {
            Piece piece = gameController.getBoard().getPiece(clickedSquare);
            if (piece != Piece.NONE) {
                // Kiểm tra xem có phải lượt của quân đó không (Để tránh chọn quân đối phương)
                if (piece.getPieceSide() == gameController.getBoard().getSideToMove()) {
                    selectedSquare = clickedSquare;
                    squares[rank][file].setBackground(selectedColor); // Highlight
                } 
            }
        } 
        // TRƯỜNG HỢP 2: Đi quân (Click lần 2)
        else {
            // Nếu click lại chính ô đó -> Hủy chọn
            if (selectedSquare == clickedSquare) {
                selectedSquare = null;
                updateBoardUI(); 
                return;
            }
            
            // Nếu click vào một quân cùng phe khác -> Đổi lựa chọn sang quân mới
            Piece targetPiece = gameController.getBoard().getPiece(clickedSquare);
            if (targetPiece != Piece.NONE && 
                targetPiece.getPieceSide() == gameController.getBoard().getSideToMove()) {
                
                selectedSquare = clickedSquare; // Đổi ô chọn
                updateBoardUI(); // Vẽ lại để xóa highlight cũ và highlight mới
                return;
            }

            // Tạo nước đi
            String moveStr = selectedSquare.value() + clickedSquare.value();
            
            // Gửi đi xử lý
            processMove(moveStr);
            
            // Sau khi thử đi xong, luôn reset lựa chọn để bàn cờ sạch sẽ
            selectedSquare = null;
            updateBoardUI(); 
        }
    }

    private void updateBoardUI() {
        Board board = gameController.getBoard(); 

        for (Square sq : Square.values()) {
            if (sq == Square.NONE) continue;

            int file = sq.getFile().ordinal(); 
            int rank = sq.getRank().ordinal(); 

            Piece piece = board.getPiece(sq);
            String symbol = getPieceSymbol(piece);
            squares[rank][file].setText(symbol);
            
            // Reset màu nền
            if ((rank + file) % 2 != 0) {
                squares[rank][file].setBackground(lightColor);
            } else {
                squares[rank][file].setBackground(darkColor);
            }

            if (piece.getPieceSide() != null) {
                 squares[rank][file].setForeground(Color.BLACK);
            }
        }
        
        // Highlight lại ô đang chọn (nếu có)
        if (selectedSquare != null) {
            int file = selectedSquare.getFile().ordinal();
            int rank = selectedSquare.getRank().ordinal();
            squares[rank][file].setBackground(selectedColor);
        }
    }

    private void processMove(String moveStr) {
        String[] moves = moveStr.trim().split("\\s+"); 

        for (String x : moves){
            boolean isLegal = gameController.doMove(x);
            
            if (isLegal) {
                logArea.append("Move: " + x + "\n");
           
            } else {
                logArea.append("Invalid: " + x + "\n");
                JOptionPane.showMessageDialog(this, 
                    "Nước đi không hợp lệ: " + x + "\n(Do sai luật, bị chiếu, hoặc chắn đường)", 
                    "Lỗi Nước Đi", 
                    JOptionPane.WARNING_MESSAGE);
            }
            updateBoardUI();
        }
       
    }

    // Phương thức được TreeDialog gọi
    public void executeMoveListFromAnalysis(String moveList) {
        
        if (analysisStartFen == null) {
            logArea.append("Lỗi: Không tìm thấy FEN gốc để tải lại!\n");
            return;
        }
        
        gameController.loadFen(analysisStartFen); 
        
        gameController.loadMoveList(moveList); 
        
        // 3. Cập nhật giao diện và log
        selectedSquare = null; 
        updateBoardUI(); 
        logArea.setText("Loaded analysis line from FEN:\n" + analysisStartFen + "\nMoves: " + moveList + "\n");
    }

    private String getPieceSymbol(Piece piece) {
        switch (piece) {
            case WHITE_KING:   return "♔";
            case WHITE_QUEEN:  return "♕";
            case WHITE_ROOK:   return "♖";
            case WHITE_BISHOP: return "♗";
            case WHITE_KNIGHT: return "♘";
            case WHITE_PAWN:   return "♙";
            
            case BLACK_KING:   return "♚";
            case BLACK_QUEEN:  return "♛";
            case BLACK_ROOK:   return "♜";
            case BLACK_BISHOP: return "♝";
            case BLACK_KNIGHT: return "♞";
            case BLACK_PAWN:   return "♟";
            
            default:           return "";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SimpleChessGui());
    }
}