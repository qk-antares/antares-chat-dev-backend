package com.antares.chatdev.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.antares.chatdev.mapper.ChatHistoryMapper;
import com.antares.chatdev.model.entity.ChatHistory;
import com.antares.chatdev.model.enums.CodeGenTypeEnum;
import com.antares.chatdev.service.impl.ChatHistoryServiceImpl;
import com.mybatisflex.core.query.QueryWrapper;

import cn.hutool.core.lang.generator.SnowflakeGenerator;

@SpringBootTest
public class ChatHistoryTest {
    @Test
    public void testReadPrompt() {
        String prompt = ChatHistoryServiceImpl.readPrompt(CodeGenTypeEnum.VUE_PROJECT);
        System.out.println(prompt);
    }

    @Autowired
    private ChatHistoryMapper chatHistoryMapper;

    @Test
    public void copyAiToToolCallAi() {
        // 查询所有 messageType = 'ai' 的记录
        List<ChatHistory> aiList = chatHistoryMapper.selectListByQuery(
                QueryWrapper.create().eq(ChatHistory::getMessageType, "ai")
        );

        var gen = new SnowflakeGenerator();
        for (ChatHistory old : aiList) {
            ChatHistory copy = ChatHistory.builder()
                    .id(gen.next()) // 生成雪花id
                    .message(old.getMessage())
                    .messageType("tool_call_ai")
                    .appId(old.getAppId())
                    .userId(old.getUserId())
                    .createTime(old.getCreateTime())
                    .updateTime(old.getUpdateTime())
                    .build();
            chatHistoryMapper.insert(copy);
        }
    }
}
