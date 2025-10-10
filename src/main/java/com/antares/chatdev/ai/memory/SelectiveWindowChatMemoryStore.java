package com.antares.chatdev.ai.memory;

import java.util.ArrayList;
import java.util.List;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

/**
 * 自定义窗口策略：
 * 1. 始终保存所有 SYSTEM 消息（不参与窗口计数 & 永不裁剪）。
 * 2. 窗口限制只针对 USER 与 AI 两类消息（交互核心）。
 * 3. 工具执行相关消息（例如 TOOL_EXECUTION_RESULT）不计入窗口，也可被一并裁掉（其存在价值依赖于相邻的 USER/AI）。
 * 4. 当 USER+AI 总数超过 maxUserAiMessages 时，只保留“最近的 maxUserAiMessages 条 USER/AI 消息”及其之后的所有消息，
 *    再把被裁剪区间中的 SYSTEM 消息（如果有）前置补回，保证所有 SYSTEM 永远存在。
 * 5. SYSTEM 补回后无需再重新裁剪，因为它们不计入窗口。
 */
public class SelectiveWindowChatMemoryStore implements ChatMemoryStore {

    private final ChatMemoryStore delegate;
    private final int maxUserAiMessages;

    public SelectiveWindowChatMemoryStore(ChatMemoryStore delegate, int maxCoreMessages) {
        this.delegate = delegate;
        this.maxUserAiMessages = maxCoreMessages;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        List<ChatMessage> all = delegate.getMessages(memoryId);
        if (all == null || all.isEmpty() || countUserAi(all) <= maxUserAiMessages) return all;
       
        // 找到要保留的最后 maxCoreMessages 条 USER/AI 的起始位置
        int earliestRetainedIndex = 0;
        int need = maxUserAiMessages;
        int seen = 0;
        for (int i = all.size() - 1; i >= 0; i--) {
            ChatMessage m = all.get(i);
            if (isUserAi(m)) {
                seen++;
                if (seen == need) {
                    earliestRetainedIndex = i;
                    break;
                }
            }
        }

        // 基础裁剪结果（含工具消息）
        List<ChatMessage> trimmed = new ArrayList<>(all.subList(earliestRetainedIndex, all.size()));

        // 需求：SYSTEM 消息永不裁剪 & 不计数；收集被裁掉区域中的 SYSTEM 消息
        List<ChatMessage> systemToPreserve = new ArrayList<>();
        for (int i = 0; i < earliestRetainedIndex; i++) {
            ChatMessage m = all.get(i);
            if (m.type() == ChatMessageType.SYSTEM) {
                systemToPreserve.add(m);
            }
        }
        if (systemToPreserve.isEmpty()) {
            return trimmed; // 没有被裁掉的 SYSTEM，基于 USER/AI 窗口裁剪即可
        }

        // SYSTEM 不计数，直接前置并返回（无需再做窗口调整）
        List<ChatMessage> result = new ArrayList<>(systemToPreserve.size() + trimmed.size());
        result.addAll(systemToPreserve);
        result.addAll(trimmed);
        return result;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        delegate.updateMessages(memoryId, messages);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        delegate.deleteMessages(memoryId);
    }

    private int countUserAi(List<ChatMessage> messages) {
        int c = 0;
        for (ChatMessage m : messages) {
            if (isUserAi(m)) c++;
        }
        return c;
    }

    private boolean isUserAi(ChatMessage m) {
        ChatMessageType t = m.type();
        return t == ChatMessageType.USER || t == ChatMessageType.AI; // 只统计 USER & AI
    }
}
