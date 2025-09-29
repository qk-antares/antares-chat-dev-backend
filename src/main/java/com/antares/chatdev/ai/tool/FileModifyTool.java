package com.antares.chatdev.ai.tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.antares.chatdev.constant.AppConstant;

import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;

/**
 * 文件修改工具
 * 支持 AI 通过工具调用的方式修改文件内容
 */
/**
 * 文件修改工具
 * 支持 AI 通过工具调用的方式修改文件内容
 */
@Slf4j
@Component
public class FileModifyTool extends BaseTool {

    // 核心方法不变，此处省略
    
    @Override
    public String getToolName() {
        return "modifyFile";
    }

    @Override
    public String getDisplayName() {
        return "修改文件";
    }

    @Override
    public String generateToolExecutedMsg(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        String oldContent = arguments.getStr("oldContent");
        String newContent = arguments.getStr("newContent");
        // 显示对比内容
        return String.format("""
                [%s] %s
                
                替换前：
                ```
                %s
                ```
                
                替换后：
                ```
                %s
                ```
                """, getDisplayName(), relativeFilePath, oldContent, newContent);
    }

    @Tool("修改文件内容，用新内容替换指定的旧内容")
    public String modifyFile(
            @P("文件的相对路径")
            String relativeFilePath,
            @P("要替换的旧内容")
            String oldContent,
            @P("替换后的新内容")
            String newContent,
            @ToolMemoryId Long appId
    ) {
        try {
            Path path = Paths.get(relativeFilePath);
            if (!path.isAbsolute()) {
                String projectDirName = "vue_project_" + appId;
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
                path = projectRoot.resolve(relativeFilePath);
            }
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                return "错误：文件不存在或不是文件 - " + relativeFilePath;
            }
            String originalContent = Files.readString(path);
            String modifiedContent;
            boolean fuzzyUsed = false;

            if (oldContent == null || oldContent.isBlank()) {
                return "错误：oldContent 不能为空";
            }

            if (originalContent.contains(oldContent)) {
                modifiedContent = originalContent.replace(oldContent, newContent);
            } else {
                // 尝试忽略缩进的匹配
                String attempt = replaceIgnoringIndent(originalContent, oldContent, newContent);
                if (attempt != null) {
                    modifiedContent = attempt;
                    fuzzyUsed = true;
                } else {
                    // 再尝试仅按去掉行首所有空白后的文本匹配（无缩进版本）
                    String normalizedOld = normalizeIndentBlock(oldContent);
                    String attempt2 = replaceByNormalizedComparison(originalContent, normalizedOld, newContent);
                    if (attempt2 != null) {
                        modifiedContent = attempt2;
                        fuzzyUsed = true;
                    } else {
                        return "警告：未找到要替换的内容（精确与忽略缩进匹配均失败），文件未修改 - " + relativeFilePath;
                    }
                }
            }

            if (originalContent.equals(modifiedContent)) {
                return "信息：替换后文件内容未发生变化 - " + relativeFilePath;
            }
            Files.writeString(path, modifiedContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("成功修改文件: {}", path.toAbsolutePath());
            return (fuzzyUsed ? "文件修改成功（使用忽略缩进/模糊匹配）: " : "文件修改成功: ") + relativeFilePath;
        } catch (IOException e) {
            String errorMessage = "修改文件失败: " + relativeFilePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    /**
     * 忽略每行前置空白进行块匹配替换（保持原文件中原始缩进和换行）。
     * @return 替换后的文本；未匹配返回 null
     */
    private String replaceIgnoringIndent(String original, String oldBlock, String newBlock) {
        List<String> origLines = splitToLines(original);
        List<String> targetLines = splitToLines(oldBlock);
        if (targetLines.isEmpty()) return null;

        List<String> trimmedTarget = targetLines.stream()
                .map(this::ltrim)
                .collect(Collectors.toList());

        // 预计算每行起始字符索引，便于切割 substring
        int[] lineStartIdx = new int[origLines.size()+1];
        int acc = 0;
        for (int i = 0; i < origLines.size(); i++) {
            lineStartIdx[i] = acc;
            acc += origLines.get(i).length();
        }
        lineStartIdx[origLines.size()] = acc;

        for (int i = 0; i <= origLines.size() - targetLines.size(); i++) {
            boolean match = true;
            for (int j = 0; j < targetLines.size(); j++) {
                if (!ltrim(origLines.get(i + j)).equals(trimmedTarget.get(j))) {
                    match = false;
                    break;
                }
            }
            if (match) {
                // 计算原始文本中对应的 substring 区间
                String originalJoined = String.join("", origLines); // 没有新增换行，因为 split 保留了换行
                int start = lineStartIdx[i];
                int end = lineStartIdx[i + targetLines.size()];
                return originalJoined.substring(0, start) + newBlock + originalJoined.substring(end);
            }
        }
        return null;
    }

    /**
     * 以规范化（去前导空白）后的块再尝试匹配。
     */
    private String replaceByNormalizedComparison(String original, String normalizedOld, String newBlock) {
        List<String> origLines = splitToLines(original);
        List<String> normalizedOrig = origLines.stream().map(this::ltrim).collect(Collectors.toList());
        List<String> targetLines = splitToLines(normalizedOld);
        if (targetLines.isEmpty()) return null;

        int[] lineStartIdx = new int[origLines.size()+1];
        int acc = 0;
        for (int i = 0; i < origLines.size(); i++) {
            lineStartIdx[i] = acc;
            acc += origLines.get(i).length();
        }
        lineStartIdx[origLines.size()] = acc;

        for (int i = 0; i <= normalizedOrig.size() - targetLines.size(); i++) {
            boolean match = true;
            for (int j = 0; j < targetLines.size(); j++) {
                if (!normalizedOrig.get(i + j).equals(targetLines.get(j))) {
                    match = false;
                    break;
                }
            }
            if (match) {
                String originalJoined = String.join("", origLines);
                int start = lineStartIdx[i];
                int end = lineStartIdx[i + targetLines.size()];
                return originalJoined.substring(0, start) + newBlock + originalJoined.substring(end);
            }
        }
        return null;
    }

    private List<String> splitToLines(String text) {
        // 保留换行符：按 \n 分割再加回去，便于准确 substring
        List<String> lines = new ArrayList<>();
        String[] arr = text.split("\n", -1); // 包含尾部空行
        for (int i = 0; i < arr.length; i++) {
            // 还原丢失的换行（除了最后一行）
            if (i < arr.length - 1) {
                lines.add(arr[i] + "\n");
            } else {
                lines.add(arr[i]);
            }
        }
        return lines;
    }

    private String ltrim(String s) {
        int idx = 0;
        while (idx < s.length() && (s.charAt(idx) == ' ' || s.charAt(idx) == '\t')) {
            idx++;
        }
        return s.substring(idx);
    }

    private String normalizeIndentBlock(String block) {
        return splitToLines(block).stream().map(this::ltrim).collect(Collectors.joining());
    }
}
