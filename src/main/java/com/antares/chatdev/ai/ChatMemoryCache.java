package com.antares.chatdev.ai;

import java.time.Duration;

import org.springframework.stereotype.Component;

import com.antares.chatdev.service.ChatHistoryService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import com.antares.chatdev.ai.memory.SelectiveWindowChatMemoryStore;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ChatMemoryCache implements ChatMemoryProvider {
    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;
    @Resource
    private ChatHistoryService chatHistoryService;

    private final Cache<Long, ChatMemory> memoryCache = Caffeine.newBuilder()
            .maximumSize(1000) // 最大缓存1000个会话
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10)) // 10分钟不访问则过期
            .removalListener((key, value, cause) -> {
                log.debug("appId-{}的ChatMemory实例被移除，原因：{}", key, cause);
            })
            .build();

    @Override
    public ChatMemory get(Object memoryId) {
        return memoryCache.get((Long) memoryId, appId -> {
            log.debug("为appId-{}创建新的ChatMemory实例", appId);
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
            .id(appId)
            // 使用自定义窗口策略：核心消息限制 20，工具消息不计数（内部在 getMessages 时裁剪）
            .chatMemoryStore(new SelectiveWindowChatMemoryStore(redisChatMemoryStore, 20))
            // 这里给一个较大的 maxMessages，避免框架内部再次裁剪（我们自己控制）
            .maxMessages(10000)
            .build();
            chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
            return chatMemory;
        });
    }

    public boolean addSystemMessage(Long memoryId, String message) {
        ChatMemory chatMemory = get(memoryId);
        if (chatMemory != null) {
            chatMemory.add(SystemMessage.from(message));
            return true;
        }
        return false;
    }

}
