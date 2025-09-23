package com.antares.chatdev.langgraph4j.node;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import com.antares.chatdev.langgraph4j.WorkflowContext;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PromptEnhancerNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 提示词增强");
            
            // 实际执行提示词增强逻辑
            String originalPrompt = context.getOriginalPrompt();
            String imageListStr = context.getImageListStr();
            // 构建增强后的提示词
            StringBuilder enhancedPromptBuilder = new StringBuilder();
            enhancedPromptBuilder.append(originalPrompt);
            
            if (StrUtil.isNotBlank(imageListStr)) {
                enhancedPromptBuilder.append("\n\n## 可用素材资源\n");
                enhancedPromptBuilder.append("请在生成网站使用以下图片资源，将这些图片合理地嵌入到网站的相应位置中。\n");
                enhancedPromptBuilder.append(imageListStr);
            }
            String enhancedPrompt = enhancedPromptBuilder.toString();

            // 更新状态
            context.setCurrentStep("提示词增强");
            context.setEnhancedPrompt(enhancedPrompt);
            log.info("提示词增强完成");
            return WorkflowContext.saveContext(context);
        });
    }
}

