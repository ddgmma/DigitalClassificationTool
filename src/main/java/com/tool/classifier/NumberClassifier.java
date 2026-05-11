package com.tool.classifier;

import java.util.ArrayList;
import java.util.Collections;

public class NumberClassifier {
    // 分类结果存储类
    public static class ClassificationResult {
        public ArrayList<Double> positiveInts = new ArrayList<>();
        public ArrayList<Double> negativeInts = new ArrayList<>();
        public ArrayList<Double> positiveDecimals = new ArrayList<>();
        public ArrayList<Double> negativeDecimals = new ArrayList<>();
        public ArrayList<Double> zeros = new ArrayList<>();
        public int invalidCount = 0;
    }

    /**
     * 对文件内容进行数字分类
     * @param fileContent 整个文件内容（可以包含换行）
     * @return 分类结果对象
     */
    public static ClassificationResult classify(String fileContent) {
        // 将换行替换为逗号，并将中文逗号替换为英文逗号
        String unified = fileContent.replaceAll("\\r?\\n", ",");
        unified = unified.replace('，', ',');   // 中文逗号转英文

        String[] parts = unified.split(",");
        ClassificationResult result = new ClassificationResult();

        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            // 检查是否包含多个小数点（如 "1.2.3"）
            long dotCount = trimmed.chars().filter(ch -> ch == '.').count();
            if (dotCount > 1) {
                result.invalidCount++;
                continue;
            }

            double value;
            try {
                value = Double.parseDouble(trimmed);
            } catch (NumberFormatException e) {
                result.invalidCount++;
                continue;
            }

            // 判断零（包括 -0.0, 0.0 等）
            if (Math.abs(value) < 1e-9) {
                result.zeros.add(value);
                continue;
            }

            boolean isDecimal = trimmed.contains(".");
            if (value > 0) {
                if (isDecimal) {
                    result.positiveDecimals.add(value);
                } else {
                    result.positiveInts.add(value);
                }
            } else { // value < 0
                if (isDecimal) {
                    result.negativeDecimals.add(value);
                } else {
                    result.negativeInts.add(value);
                }
            }
        }

        // 排序
        Collections.sort(result.positiveInts);
        Collections.sort(result.negativeInts);
        Collections.sort(result.positiveDecimals);
        Collections.sort(result.negativeDecimals);
        Collections.sort(result.zeros);

        return result;
    }
}