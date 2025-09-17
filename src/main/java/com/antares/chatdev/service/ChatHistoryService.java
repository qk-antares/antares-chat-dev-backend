package com.antares.chatdev.service;

import java.time.LocalDateTime;

import com.antares.chatdev.model.dto.chathistory.ChatHistoryQueryRequest;
import com.antares.chatdev.model.entity.ChatHistory;
import com.antares.chatdev.model.entity.User;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;

import dev.langchain4j.memory.ChatMemory;

/**
 * 对话历史 服务层。
 *
 * @author root
 * @since 2025-09-16
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    boolean deleteByAppId(Long appId);

    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize, LocalDateTime lastCreateTime, User loginUser);

    int loadChatHistoryToMemory(Long appId, ChatMemory chatMemory, int maxCount);

}
