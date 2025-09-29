package com.antares.chatdev.core.handler;

import org.springframework.stereotype.Component;

import com.antares.chatdev.model.enums.ChatHistoryMessageTypeEnum;
import com.antares.chatdev.service.ChatHistoryService;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 简单文本流处理器
 * 处理 HTML 和 MULTI_FILE 类型的流式响应
 */
@Slf4j
@Component
public class SimpleTextStreamHandler {

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * 处理传统流（HTML, MULTI_FILE）
     * 直接收集完整的文本响应
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @return 处理后的流
     */
    public Flux<String> handle(Flux<String> originFlux, Long appId, Long userId) {
        // 7. 收集AI响应内容并在完成后记录到对话历史
        StringBuilder aiResponseBuilder = new StringBuilder();
        return originFlux.doOnNext(chunk -> {
            aiResponseBuilder.append(chunk);
        }).doOnComplete(() -> {
            // 流式响应完成后，添加AI消息到对话历史
            String aiResponse = aiResponseBuilder.toString();
            chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), userId);
            chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.TOOL_CALL_AI.getValue(), userId);
        }).doOnError(error -> {
            // AI回复失败，记录错误信息
            String errorMessage = "AI回复失败：" + error.getMessage();
            chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), userId);
            chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.TOOL_CALL_AI.getValue(), userId);
        });
    }
}
