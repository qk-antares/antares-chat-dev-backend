package com.antares.chatdev.langgraph4j.node;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import com.antares.chatdev.langgraph4j.WorkflowContext;
import com.antares.chatdev.langgraph4j.ai.ImageCollectionService;
import com.antares.chatdev.langgraph4j.utils.SpringContextUtil;

import lombok.extern.slf4j.Slf4j;

/*
 * 图片收集节点
 * 
 * 每个节点的工作流程分为3步：
 * 1. 从 state 中获取 我们定义的WorkflowContext，其中保存了工作流的所有状态信息
 * 2. 执行节点的核心逻辑（这里是图片收集）
 * 3. 更新 WorkflowContext 并保存回 state
 */
@Slf4j
public class ImageCollectorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);

            // 实际执行图片收集逻辑
            String originalPrompt = context.getOriginalPrompt();
            String imageListStr = "";
            try {
                // 获取AI图片收集服务
                ImageCollectionService imageCollectionService = SpringContextUtil.getBean(ImageCollectionService.class);
                // 使用 AI 服务进行智能图片收集
                imageListStr = imageCollectionService.collectImages(originalPrompt);
            } catch (Exception e) {
                log.error("图片收集失败: {}", e.getMessage(), e);
            }

            // 更新状态
            context.setCurrentStep("图片收集");
            context.setImageListStr(imageListStr);

            return WorkflowContext.saveContext(context);
        });
    }
}
