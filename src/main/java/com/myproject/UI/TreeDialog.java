package com.myproject.UI;


import com.myproject.Logic.AnalysisNode;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath; // Cần import này
import java.awt.*;
import java.awt.event.MouseAdapter; // Cần import này
import java.awt.event.MouseEvent; // Cần import này

public class TreeDialog extends JDialog {

    // 1. Khai báo tham chiếu đến GUI cha
    private final SimpleChessGui ownerGui; 

    // 2. Sửa constructor để nhận SimpleChessGui
    public TreeDialog(SimpleChessGui owner, AnalysisNode rootData) {
        // Phải gọi super(owner, ...) với tham chiếu JDialog/JFrame
        super(owner, "Dự đoán Chiến Lược (Cây Biến Thể)", true); 
        this.ownerGui = owner; // Lưu tham chiếu
        
        setSize(600, 500);
        setLocationRelativeTo(owner);

        // 3. Chuyển đổi dữ liệu từ AnalysisNode sang TreeNode của Swing
        // *Sử dụng hàm tạo cây đã được sửa*
        DefaultMutableTreeNode guiRoot = createGuiTree(rootData); 

        // 4. Tạo JTree
        JTree tree = new JTree(guiRoot);
        tree.setFont(new Font("Monospaced", Font.PLAIN, 14));
        
        // --- THÊM XỬ LÝ SỰ KIỆN CLICK VÀO CÂY ---
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Xử lý double click
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        
                        StringBuilder moveListBuilder = new StringBuilder();
                        Object[] pathArray = path.getPath();
                        
                        // Bắt đầu từ Node thứ 1 (bỏ qua Node Gốc 'Start')
                        for (int i = 1; i < pathArray.length; i++) {
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode) pathArray[i];
                            
                            // *** KIỂM TRA ĐÚNG KIỂU DỮ LIỆU ĐÃ ĐƯỢC LƯU ***
                            if (node.getUserObject() instanceof AnalysisNode) {
                                AnalysisNode analysisNode = (AnalysisNode) node.getUserObject();
                                
                                if (analysisNode.getMove() != null) {
                                    // Dùng toString() để lấy chuỗi UCI (vì getUci/toUciString lỗi)
                                    moveListBuilder.append(analysisNode.getMove().toString()).append(" "); 
                                }
                            }
                        }
                        
                        String moveList = moveListBuilder.toString().trim();
                        
                        if (!moveList.isEmpty()) {
                            ownerGui.executeMoveListFromAnalysis(moveList); 
                        }
                        
                        dispose(); 
                    }
                }
            }
        });

        // 5. Thêm vào giao diện
        add(new JScrollPane(tree), BorderLayout.CENTER);
        
        // Thêm nhãn thống kê tổng quát
        JLabel lblStats = new JLabel(String.format(" TỔNG QUAN: Thắng %d | Thua %d | Hòa %d", 
                rootData.getWinCount(), rootData.getLossCount(), rootData.getDrawCount()));
        lblStats.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblStats.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(lblStats, BorderLayout.NORTH);
    }

    // *** 6. Sửa Hàm đệ quy để lưu đối tượng AnalysisNode ***
    private DefaultMutableTreeNode createGuiTree(AnalysisNode nodeData) {
        // LƯU Ý: LƯU TRỰC TIẾP ĐỐI TƯỢNG AnalysisNode vào UserObject.
        // JTree sẽ tự động gọi nodeData.toString() để hiển thị.
        DefaultMutableTreeNode guiNode = new DefaultMutableTreeNode(nodeData); 

        // Đệ quy cho con
        for (AnalysisNode childData : nodeData.getChildren()) {
            guiNode.add(createGuiTree(childData));
        }
        return guiNode;
    }
    
}