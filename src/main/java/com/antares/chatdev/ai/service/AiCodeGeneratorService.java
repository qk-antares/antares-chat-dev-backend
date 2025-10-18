package com.antares.chatdev.ai.service;

import com.antares.chatdev.ai.guardrail.PromptSafetyInputGuardrail;
import com.antares.chatdev.ai.model.HtmlCodeResult;
import com.antares.chatdev.ai.model.MultiFileCodeResult;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "openAiChatModel", chatMemoryProvider = "chatMemoryCache")
public interface AiCodeGeneratorService {

    /**
     * 生成 HTML 代码
     *
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    @InputGuardrails({PromptSafetyInputGuardrail.class})
    HtmlCodeResult generateHtmlCode(@MemoryId Long appId, @UserMessage String userMessage);

    /**
     * 生成多文件代码
     *
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(@MemoryId Long appId, @UserMessage String userMessage);
}
