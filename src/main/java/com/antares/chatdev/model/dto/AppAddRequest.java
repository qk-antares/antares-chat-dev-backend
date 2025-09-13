package com.antares.chatdev.model.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * 应用创建请求
 */
@Data
public class AppAddRequest implements Serializable {
    /**
     * 应用初始化的 prompt
     */
    private String initPrompt;

    private static final long serialVersionUID = 1L;
}