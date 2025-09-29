package com.antares.chatdev.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 代码生成类型枚举
 */
@Getter
public enum CodeGenTypeEnum {

    HTML("原生 HTML 模式", "html"),
    MULTI_FILE("原生多文件模式", "multi_file"),
    VUE_PROJECT("Vue 工程模式", "vue_project");

    private final String text;
    private final String value;

    CodeGenTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static CodeGenTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (CodeGenTypeEnum anEnum : CodeGenTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public static String getPromptFileName(CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            return null;
        }
        switch (codeGenTypeEnum) {
            case HTML:
                return "codegen-html-system-prompt.txt";
            case MULTI_FILE:
                return "codegen-multi-file-system-prompt.txt";
            case VUE_PROJECT:
                return "codegen-vue-project-system-prompt.txt";
            default:
                return null;
        }
    }

    public static String getEnhancePromptFileName(CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            return null;
        }
        switch (codeGenTypeEnum) {
            case HTML:
                return "codegen-html-enhance-prompt.txt";
            case MULTI_FILE:
                return "codegen-multi-file-enhance-prompt.txt";
            case VUE_PROJECT:
                return "codegen-vue-project-enhance-prompt.txt";
            default:
                return null;
        }
    }
}