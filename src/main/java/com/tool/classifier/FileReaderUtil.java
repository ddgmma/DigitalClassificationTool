package com.tool.classifier;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileReaderUtil {
    /**
     * 读取文件全部内容，返回字符串（保留换行符）
     * 使用 UTF-8 编码，避免乱码
     * @param file 要读取的文件
     * @return 文件内容
     * @throws IOException 读取失败时抛出
     */
    public static String readFileToString(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
}