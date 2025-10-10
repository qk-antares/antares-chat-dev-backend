package com.antares.chatdev.ai.service;

import com.antares.chatdev.model.enums.CodeGenTypeEnum;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

/**
 * AI代码生成类型智能路由服务
 * 使用结构化输出直接返回枚举类型
 */
@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "openAiChatModel")
public interface AiCodeGenTypeRoutingService {
    /**
     * 根据用户需求智能选择代码生成类型
     *
     * @param userPrompt 用户输入的需求描述
     * @return 推荐的代码生成类型
     */
    @SystemMessage(fromResource = "prompt/codegen-routing-system-prompt.txt")
    CodeGenTypeEnum routeCodeGenType(String userPrompt);
}

