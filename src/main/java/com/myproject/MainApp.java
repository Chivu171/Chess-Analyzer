package com.myproject;

import com.myproject.Logic.GameController;

/**
 * MainApp: Class dùng để chạy thử nghiệm (Test) các chức năng của GameController.
 */
public class MainApp {
    public static void main(String[] args) {
        System.out.println("=== BẮT ĐẦU TEST INPUT ===");

        // 1. Khởi tạo Controller
        GameController game = new GameController();
        System.out.println("Trạng thái ban đầu (FEN): " + game.getCurrentFen());

        // ---------------------------------------------------------
        // TEST 1: Input Hướng 1 (Load danh sách nước đi từ Text)
        // ---------------------------------------------------------
        System.out.println("\n--- Test 1: Load danh sách nước đi (e2e4 e7e5) ---");
        String moveList = "e2e4 e7e5";
        game.loadMoveList(moveList);
        
        // In ra FEN để kiểm tra xem tốt đã lên chưa
        System.out.println("FEN sau khi load: " + game.getCurrentFen());
        // FEN đúng nên là: rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2

        // ---------------------------------------------------------
        // TEST 2: Input Hướng 2 (Tương tác từng nước)
        // ---------------------------------------------------------
        System.out.println("\n--- Test 2: Đi thêm một nước lẻ (g1f3 - Mã trắng lên) ---");
        boolean isLegal = game.doMove("g1f3");
        
        if (isLegal) {
            System.out.println("-> Nước đi hợp lệ!");
            System.out.println("FEN hiện tại: " + game.getCurrentFen());
        } else {
            System.out.println("-> Nước đi lỗi!");
        }

        // ---------------------------------------------------------
        // TEST 3: Thử nước đi sai luật (Ví dụ: Tốt đen đi lùi hoặc đi sai ô)
        // ---------------------------------------------------------
        System.out.println("\n--- Test 3: Thử nước đi sai luật (a7a8 - Tốt đen chưa đến lượt hoặc đi sai) ---");
        // Lưu ý: Lúc này đang là lượt ĐEN đi (sau g1f3). 
        // a7a8 là nước đi hợp lệ về mặt vật lý nhưng sai luật nếu chưa phong cấp hoặc sai lượt.
        // Thử một nước sai rõ ràng: Tốt trắng e4 đi sang e5 (đã có quân) hoặc đi quân sai lượt.
        
        // Thử đi quân Trắng (h1h2) trong khi đang là lượt Đen
        boolean isLegalBad = game.doMove("h1h2"); 
        if (!isLegalBad) {
            System.out.println("-> Hệ thống đã bắt được lỗi nước đi sai luật (Đúng mong đợi).");
        } else {
            System.out.println("-> Lỗi: Hệ thống cho phép nước đi sai!");
        }

        System.out.println("\n=== KẾT THÚC TEST ===");
    }
}
