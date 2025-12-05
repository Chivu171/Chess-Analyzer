package com.myproject.UI;


import com.myproject.Logic.AnalysisNode;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

public class TreeDialog extends JDialog {

    public TreeDialog(Frame owner, AnalysisNode rootData) {
        super(owner, "Dự đoán Chiến Lược (Cây Biến Thể)", true);
        setSize(600, 500);
        setLocationRelativeTo(owner);

        // 1. Chuyển đổi dữ liệu từ AnalysisNode sang TreeNode của Swing
        DefaultMutableTreeNode guiRoot = createGuiTree(rootData);

        // 2. Tạo JTree
        JTree tree = new JTree(guiRoot);
        tree.setFont(new Font("Monospaced", Font.PLAIN, 14));
        
        // 3. Thêm vào giao diện
        add(new JScrollPane(tree), BorderLayout.CENTER);
        
        // Thêm nhãn thống kê tổng quát
        JLabel lblStats = new JLabel(String.format(" TỔNG QUAN: Thắng %d | Thua %d | Hòa %d", 
                rootData.getWinCount(), rootData.getLossCount(), rootData.getDrawCount()));
        lblStats.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblStats.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(lblStats, BorderLayout.NORTH);
    }

    // Hàm đệ quy để chuyển dữ liệu sang JTree
    private DefaultMutableTreeNode createGuiTree(AnalysisNode nodeData) {
        // Text hiển thị trên mỗi dòng cây
        String display = nodeData.toString(); 
        
        DefaultMutableTreeNode guiNode = new DefaultMutableTreeNode(display);

        // Đệ quy cho con
        for (AnalysisNode childData : nodeData.getChildren()) {
            guiNode.add(createGuiTree(childData));
        }
        return guiNode;
    }
    
}
