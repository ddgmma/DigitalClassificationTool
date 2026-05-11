package com.tool.classifier;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        // 设置外观为 Nimbus
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        JFrame frame = new JFrame("数字分类工具");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 600);
        frame.setLocationRelativeTo(null);
        frame.setMinimumSize(new Dimension(600, 500));

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(240, 248, 255));

        // 顶部标题
        JLabel titleLabel = new JLabel("数字分类工具", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 102, 204));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 中间区域
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setOpaque(false);
        JButton selectButton = new JButton("选择文件");
        selectButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        selectButton.setBackground(new Color(70, 130, 180));
        selectButton.setForeground(Color.WHITE);
        selectButton.setFocusPainted(false);
        selectButton.setBorder(new LineBorder(new Color(60, 120, 170), 1, true));
        selectButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        buttonPanel.add(selectButton);
        centerPanel.add(buttonPanel, BorderLayout.NORTH);

        // 文件路径显示区域
        JPanel pathPanel = new JPanel(new BorderLayout(5, 5));
        pathPanel.setOpaque(false);
        pathPanel.setBorder(new EmptyBorder(5, 0, 5, 0));
        JLabel pathLabel = new JLabel("未选择文件");
        pathLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        pathLabel.setForeground(Color.DARK_GRAY);
        JScrollPane pathScroll = new JScrollPane(pathLabel);
        pathScroll.setBorder(BorderFactory.createTitledBorder("当前文件路径"));
        pathScroll.setOpaque(false);
        pathScroll.setViewportBorder(null);
        pathPanel.add(pathScroll, BorderLayout.CENTER);
        centerPanel.add(pathPanel, BorderLayout.CENTER);

        // 结果显示区域（修改字体为支持中文的宋体）
        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("宋体", Font.PLAIN, 14));  // 改为宋体，支持中文
        resultArea.setBackground(new Color(255, 255, 245));
        resultArea.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        resultArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        resultScrollPane.setBorder(BorderFactory.createTitledBorder("分类结果"));
        resultScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        centerPanel.add(resultScrollPane, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 底部状态栏
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
        statusPanel.setBackground(new Color(220, 240, 255));
        JLabel statusLabel = new JLabel(" 就绪");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(0, 102, 0));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);

        // 按钮事件
        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 使用 Windows 原生文件对话框
                FileDialog fileDialog = new FileDialog(frame, "选择文件", FileDialog.LOAD);
                fileDialog.setDirectory(System.getProperty("user.dir"));
                fileDialog.setVisible(true);

                String fileName = fileDialog.getFile();
                String directory = fileDialog.getDirectory();
                if (fileName != null) {
                    File selectedFile = new File(directory, fileName);
                    pathLabel.setText(selectedFile.getAbsolutePath());
                    statusLabel.setText(" 正在读取文件...");
                    statusLabel.setForeground(Color.BLUE);
                    selectButton.setEnabled(false);

                    SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                        @Override
                        protected String doInBackground() throws Exception {
                            String fileContent;
                            try {
                                fileContent = FileReaderUtil.readFileToString(selectedFile);
                            } catch (IOException ex) {
                                return "读取文件失败: " + ex.getMessage();
                            }
                            NumberClassifier.ClassificationResult classification = NumberClassifier.classify(fileContent);
                            return buildDisplayString(classification);
                        }

                        @Override
                        protected void done() {
                            try {
                                String display = get();
                                resultArea.setText(display);
                                statusLabel.setText(" 处理完成");
                                statusLabel.setForeground(new Color(0, 102, 0));
                            } catch (Exception ex) {
                                resultArea.setText("处理出错: " + ex.getMessage());
                                statusLabel.setText(" 出错");
                                statusLabel.setForeground(Color.RED);
                            } finally {
                                selectButton.setEnabled(true);
                            }
                        }
                    };
                    worker.execute();
                } else {
                    resultArea.setText("未选择任何文件");
                    statusLabel.setText(" 就绪");
                    statusLabel.setForeground(new Color(0, 102, 0));
                }
            }
        });

        frame.setVisible(true);
    }

    private static String buildDisplayString(NumberClassifier.ClassificationResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("===== 数字分类结果 =====\n\n");

        sb.append("正整数（共 ").append(result.positiveInts.size()).append(" 个）：\n");
        if (result.positiveInts.isEmpty()) {
            sb.append("无\n");
        } else {
            for (Double num : result.positiveInts) {
                sb.append(num.intValue()).append(" ");
            }
            sb.append("\n");
        }

        sb.append("\n负整数（共 ").append(result.negativeInts.size()).append(" 个）：\n");
        if (result.negativeInts.isEmpty()) {
            sb.append("无\n");
        } else {
            for (Double num : result.negativeInts) {
                sb.append(num.intValue()).append(" ");
            }
            sb.append("\n");
        }

        sb.append("\n正小数（共 ").append(result.positiveDecimals.size()).append(" 个）：\n");
        if (result.positiveDecimals.isEmpty()) {
            sb.append("无\n");
        } else {
            for (Double num : result.positiveDecimals) {
                sb.append(num).append(" ");
            }
            sb.append("\n");
        }

        sb.append("\n负小数（共 ").append(result.negativeDecimals.size()).append(" 个）：\n");
        if (result.negativeDecimals.isEmpty()) {
            sb.append("无\n");
        } else {
            for (Double num : result.negativeDecimals) {
                sb.append(num).append(" ");
            }
            sb.append("\n");
        }

        sb.append("\n零（共 ").append(result.zeros.size()).append(" 个）：\n");
        if (result.zeros.isEmpty()) {
            sb.append("无\n");
        } else {
            sb.append("0");
            if (result.zeros.size() > 1) {
                sb.append(" (共 ").append(result.zeros.size()).append(" 个)");
            }
            sb.append("\n");
        }

        sb.append("\n非数字（共 ").append(result.invalidCount).append(" 个）\n");
        return sb.toString();
    }
}