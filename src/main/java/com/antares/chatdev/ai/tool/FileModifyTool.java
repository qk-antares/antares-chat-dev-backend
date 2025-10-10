package com.antares.chatdev.ai.tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

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
        // 预处理：对模型可能出现的双重转义内容进行一次反转义，例如 \" -> " , \n -> 换行
        String oldContent = preprocessArgument(arguments.getStr("oldContent"));
        String newContent = preprocessArgument(arguments.getStr("newContent"));
        // 显示对比内容
        return String.format("""
                [工具调用] %s %s
                
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
            @P("文件的相对路径") String relativeFilePath,
            @P("要替换的旧内容") String oldContent,
            @P("替换后的新内容") String newContent,
            @ToolMemoryId Long appId) {
        try {
            // 预处理：对模型可能出现的双重转义内容进行一次反转义，例如 \" -> " , \n -> 换行
            oldContent = preprocessArgument(oldContent);
            newContent = preprocessArgument(newContent);

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

            if (oldContent == null || oldContent.isBlank()) {
                return "错误：oldContent 不能为空";
            }

            if (originalContent.contains(oldContent)) {
                modifiedContent = originalContent.replace(oldContent, newContent);
            } else {
                // 缩进不敏感+自动重排新块缩进 匹配
                String attempt = replaceIgnoringIndent(originalContent, oldContent, newContent);
                if (attempt != null) {
                    modifiedContent = attempt;
                } else {
                    return "警告：未找到要替换的内容，文件未修改 - " + relativeFilePath + "\n尝试重新读取该文件，oldContent是要替换的旧内容";
                }
            }

            if (originalContent.equals(modifiedContent)) {
                return "信息：替换后文件内容未发生变化 - " + relativeFilePath;
            }

            Files.writeString(path, modifiedContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("成功修改文件: {}", path.toAbsolutePath());
            return "文件修改成功: " + relativeFilePath;
        } catch (IOException e) {
            String errorMessage = "修改文件失败: " + relativeFilePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    /**
     * 更健壮的缩进不敏感替换：
     * 1. 去掉 oldBlock 头尾空白行；对其内部行仅移除前导空白用于匹配。
     * 2. 在 original 中滑动窗口，按行忽略前导空白比较。
     * 3. 命中后对 newBlock 做“公共缩进剥离 + 重新应用原文件匹配块首行缩进”重排。
     * 4. 保留 newBlock 内部相对缩进结构。
     * 5. 仅替换第一处命中。
     * 
     * @return 替换后的字符串；未匹配返回 null
     */
    private String replaceIgnoringIndent(String original, String oldBlock, String newBlock) {
        if (oldBlock == null || oldBlock.isBlank())
            return null;
        List<String> oldLinesRaw = splitToLines(oldBlock);
        oldLinesRaw = trimEdgeBlankLines(oldLinesRaw); // 去掉首尾完全空白行
        if (oldLinesRaw.isEmpty())
            return null;

        // 生成用于比较的规范化行（去掉全部前导空白）
        List<String> oldNormalized = oldLinesRaw.stream()
                .map(this::stripLineEnding)
                .map(this::ltrim)
                .toList();

        List<String> origLines = splitToLines(original);
        if (origLines.size() < oldLinesRaw.size())
            return null;

        // 预计算偏移
        int[] lineStartIdx = new int[origLines.size() + 1];
        int acc = 0;
        for (int i = 0; i < origLines.size(); i++) {
            lineStartIdx[i] = acc;
            acc += origLines.get(i).length();
        }
        lineStartIdx[origLines.size()] = acc;

        int windowSize = oldLinesRaw.size();
        outer: for (int i = 0; i <= origLines.size() - windowSize; i++) {
            for (int j = 0; j < windowSize; j++) {
                String cand = stripLineEnding(origLines.get(i + j));
                if (!ltrim(cand).equals(oldNormalized.get(j))) {
                    continue outer;
                }
            }
            // 命中
            int start = lineStartIdx[i];
            int end = lineStartIdx[i + windowSize];
            String baseIndent = leadingWhitespace(origLines.get(i));
            String reIndented = reindentNewBlock(newBlock, baseIndent);
            String before = original.substring(0, start);
            String after = original.substring(end);
            return before + reIndented + after;
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

    private List<String> trimEdgeBlankLines(List<String> lines) {
        int left = 0;
        int right = lines.size() - 1;
        while (left <= right && stripLineEnding(lines.get(left)).trim().isEmpty())
            left++;
        while (right >= left && stripLineEnding(lines.get(right)).trim().isEmpty())
            right--;
        if (left == 0 && right == lines.size() - 1)
            return lines; // 无需裁剪
        return lines.subList(left, right + 1);
    }

    private String stripLineEnding(String line) {
        if (line.endsWith("\n"))
            return line.substring(0, line.length() - 1);
        return line;
    }

    private String leadingWhitespace(String line) {
        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);
            if (c != ' ' && c != '\t')
                break;
            i++;
        }
        return line.substring(0, i);
    }

    private String reindentNewBlock(String newBlock, String baseIndent) {
        List<String> newLines = splitToLines(newBlock);
        // 计算公共缩进（忽略空行）
        String common = commonIndent(newLines);
        StringBuilder sb = new StringBuilder(newBlock.length() + baseIndent.length() * newLines.size());
        for (String lineWithNl : newLines) {
            boolean hasNl = lineWithNl.endsWith("\n");
            String core = hasNl ? lineWithNl.substring(0, lineWithNl.length() - 1) : lineWithNl;
            if (core.trim().isEmpty()) {
                // 空行直接输出空行（不强加 baseIndent，防止产生多余空白）
                sb.append(core);
            } else {
                if (core.startsWith(common))
                    core = core.substring(common.length());
                sb.append(baseIndent).append(core);
            }
            if (hasNl)
                sb.append('\n');
        }
        return sb.toString();
    }

    private String commonIndent(List<String> lines) {
        String indent = null;
        for (String lineWithNl : lines) {
            String line = stripLineEnding(lineWithNl);
            if (line.trim().isEmpty())
                continue; // 忽略空行
            String lead = leadingWhitespace(line);
            if (indent == null)
                indent = lead;
            else {
                // 截取公共前缀
                int k = 0;
                int max = Math.min(indent.length(), lead.length());
                while (k < max && indent.charAt(k) == lead.charAt(k))
                    k++;
                indent = indent.substring(0, k);
                if (indent.isEmpty())
                    break;
            }
        }
        return indent == null ? "" : indent;
    }

    /**
     * 处理模型可能出现的双重转义
     */
    private String preprocessArgument(String s) {
        if (s == null || s.isEmpty())
            return s;
        int len = s.length();
        // 预估不会增长，初始容量用原长
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < len) {
                char n = s.charAt(i + 1);
                switch (n) {
                    case 'n':
                        sb.append('\n');
                        i++; // 跳过 n
                        continue;
                    case 't':
                        sb.append('\t');
                        i++;
                        continue;
                    case 'r':
                        // 支持 \r\n -> 换行
                        if (i + 2 < len && s.charAt(i + 2) == 'n') {
                            sb.append('\n');
                            i += 2; // 跳过 r n
                        } else {
                            sb.append('r');
                            i++; // 只消费 r
                        }
                        continue;
                    case '"':
                        sb.append('"');
                        i++;
                        continue;
                    case '\\':
                        // 处理 \\ -> \
                        sb.append('\\');
                        i++;
                        continue;
                    default:
                        // 未识别的转义，原样输出两个字符，避免误处理
                        sb.append('\\').append(n);
                        i++;
                        continue;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
