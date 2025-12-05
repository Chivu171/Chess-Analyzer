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

public class SimpleChessGui extends JFrame {

    private final GameController gameController;
    private final JButton[][] squares = new JButton[8][8];
    private final JTextField inputField;
    private final JTextArea logArea;

    // --- BIẾN MỚI THÊM ĐỂ XỬ LÝ CLICK CHUỘT ---
    private Square selectedSquare = null; // Lưu vị trí ô đang được chọn (ví dụ: e2)

    // Màu bàn cờ (giống Chess.com)
    private final Color lightColor = new Color(240, 217, 181);
    private final Color darkColor = new Color(181, 136, 99);
    private final Color selectedColor = new Color(255, 255, 51); // Màu vàng để highlight khi chọn

    public SimpleChessGui() {
        this.gameController = new GameController();
        
        setTitle("My Chess App (Simple GUI)");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. Panel bàn cờ (Center)
        JPanel boardPanel = new JPanel(new GridLayout(8, 8));
        initializeBoard(boardPanel);
        add(boardPanel, BorderLayout.CENTER);

        // 2. Panel điều khiển (South)
        JPanel controlPanel = new JPanel(new BorderLayout());
        
        inputField = new JTextField();
        inputField.setFont(new Font("Monospaced", Font.BOLD, 14));
        inputField.setToolTipText("Nhập nước đi (ví dụ: e2e4) rồi nhấn Enter");
        
        JButton btnReset = new JButton("Reset Game");
        
        controlPanel.add(new JLabel(" Nhập nước đi (UCI): "), BorderLayout.WEST);
        controlPanel.add(inputField, BorderLayout.CENTER);
        controlPanel.add(btnReset, BorderLayout.EAST);
        
        add(controlPanel, BorderLayout.SOUTH);
        JButton btnPredict = new JButton("🔮 Dự đoán");
controlPanel.add(btnPredict, BorderLayout.NORTH); // Hoặc vị trí bạn muốn

// Xử lý sự kiện bấm nút
btnPredict.addActionListener(e -> {
    // Chạy trong luồng riêng để không đơ giao diện khi tính toán lâu
    new Thread(() -> {
        btnPredict.setEnabled(false);
        btnPredict.setText("Đang tính...");
        
        try {
            TreeAnalyzer analyzer = new TreeAnalyzer();
            
            // Lấy clone bàn cờ hiện tại để không ảnh hưởng bàn cờ chính
            // (Chesslib board không có clone deep dễ dàng, 
            // nên tạo board mới và load FEN)
            Board analysisBoard = new Board();
            analysisBoard.loadFromFen(gameController.getBoard().getFen());

            // Bắt đầu vét cạn (Phần nặng nhất)
            AnalysisNode rootResult = analyzer.buildGameTree(analysisBoard);

            // Hiển thị lên giao diện (phải quay về luồng UI)
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

        // 3. Panel Log (East) - Để xem lịch sử
        logArea = new JTextArea(20, 15);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(logArea), BorderLayout.EAST);

        // --- SỰ KIỆN ---
        
        // Khi nhấn Enter trong ô nhập liệu
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String moveText = inputField.getText().trim();
                if (!moveText.isEmpty()) {
                    processMove(moveText);
                    inputField.setText(""); // Xóa ô nhập sau khi đi
                }
            }
        });

        // Khi nhấn nút Reset
        btnReset.addActionListener(e -> {
            gameController.resetGame();
            selectedSquare = null; // Reset cả lựa chọn
            updateBoardUI();
            logArea.setText("Game reset!\n");
        });

        // Vẽ bàn cờ lần đầu
        updateBoardUI();
        logArea.append("Game started.\n");
        setVisible(true);
    }

    // Khởi tạo lưới 8x8 ô cờ
    private void initializeBoard(JPanel boardPanel) {
        for (int rank = 7; rank >= 0; rank--) { // Hàng 8 xuống 1
            for (int file = 0; file < 8; file++) { // Cột A đến H
                JButton btn = new JButton();
                btn.setFont(new Font("Serif", Font.PLAIN, 50)); // Font to để hiển thị quân cờ
                btn.setFocusPainted(false);
                
                // --- THÊM SỰ KIỆN CLICK CHO TỪNG Ô ---
                // Cần biến final để dùng trong lambda
                int finalRank = rank; 
                int finalFile = file;

                btn.addActionListener(e -> handleSquareClick(finalRank, finalFile));
                // -------------------------------------

                // Tô màu ô cờ
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

    // --- HÀM MỚI: XỬ LÝ CLICK CHUỘT ---
    private void handleSquareClick(int rank, int file) {
        // Chuyển đổi tọa độ (rank, file) thành Square của chesslib (ví dụ: rank 1, file 4 -> E2)
        // Lưu ý: Rank trong mảng chạy từ 0-7, nhưng Square.squareAt mong đợi rank 0 là hàng 1.
        // Trong vòng lặp initializeBoard, rank 0 là hàng 1, nên map thẳng sang được.
        Square clickedSquare = Square.squareAt(rank * 8 + file); 

        // TRƯỜNG HỢP 1: Chưa có ô nào được chọn -> Đây là click đầu tiên (Chọn quân)
        if (selectedSquare == null) {
            Piece piece = gameController.getBoard().getPiece(clickedSquare);
            // Chỉ cho chọn nếu ô đó có quân cờ
            if (piece != Piece.NONE) {
                selectedSquare = clickedSquare;
                // Highlight ô vừa chọn (Tô màu vàng)
                squares[rank][file].setBackground(selectedColor);
                logArea.append("Selected: " + clickedSquare + "\n");
            }
        } 
        // TRƯỜNG HỢP 2: Đã có ô chọn trước đó -> Đây là click thứ hai (Đi quân)
        else {
            // Nếu click lại vào chính ô đang chọn -> Hủy chọn
            if (selectedSquare == clickedSquare) {
                selectedSquare = null;
                updateBoardUI(); // Vẽ lại để mất màu vàng
                return;
            }

            // Tạo chuỗi nước đi UCI (ví dụ: e2 + e4 -> "e2e4")
            String moveStr = selectedSquare.value() + clickedSquare.value();
            
            // Gửi đi xử lý
            processMove(moveStr);
            
            // Sau khi đi xong (hoặc lỗi), reset lựa chọn
            selectedSquare = null;
            
            // Cập nhật lại bàn cờ (để xóa màu vàng và cập nhật vị trí quân)
            updateBoardUI(); 
        }
    }

    // Cập nhật giao diện dựa trên trạng thái hiện tại của bàn cờ
    private void updateBoardUI() {
        Board board = gameController.getBoard(); // Lấy đối tượng Board từ Controller

        for (Square sq : Square.values()) {
            if (sq == Square.NONE) continue;

            // Chuyển đổi Square của chesslib sang tọa độ mảng [row][col]
            int file = sq.getFile().ordinal(); 
            int rank = sq.getRank().ordinal(); 

            // Cập nhật ký tự quân cờ
            Piece piece = board.getPiece(sq);
            String symbol = getPieceSymbol(piece);
            squares[rank][file].setText(symbol);
            
            // Reset màu nền (Xóa highlight màu vàng nếu có)
            if ((rank + file) % 2 != 0) {
                squares[rank][file].setBackground(lightColor);
            } else {
                squares[rank][file].setBackground(darkColor);
            }

            // Tô màu chữ
            if (piece.getPieceSide() != null) {
                 squares[rank][file].setForeground(Color.BLACK);
            }
        }
        
        // Nếu đang có ô được chọn, tô lại màu vàng cho nó (tránh bị updateBoardUI xóa mất)
        if (selectedSquare != null) {
            int file = selectedSquare.getFile().ordinal();
            int rank = selectedSquare.getRank().ordinal();
            squares[rank][file].setBackground(selectedColor);
        }
    }

    // Xử lý nước đi khi người dùng nhập (hoặc click)
    private void processMove(String moveStr) {
        String[] moves = moveStr.trim().split("\\s+"); // Tách bằng khoảng trắng

        // Gọi GameController để đi
        for (String x : moves){
            boolean isLegal = gameController.doMove(x);
            
            if (isLegal) {
                logArea.append("Move: " + x + "\n");
                // updateBoardUI(); // Không cần gọi ở đây nữa vì handleSquareClick sẽ gọi
            } else {
                logArea.append("Invalid: " + x + "\n");
                // JOptionPane.showMessageDialog(this, "Nước đi không hợp lệ: " + x); 
                // Tạm tắt popup để đỡ phiền khi click nhầm
            }
        }
        updateBoardUI(); // Cập nhật lại bàn cờ sau khi đi
    }

    // Chuyển đổi quân cờ sang ký tự Unicode đẹp mắt
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