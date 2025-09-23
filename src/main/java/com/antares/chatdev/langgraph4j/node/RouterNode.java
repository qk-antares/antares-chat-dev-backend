package com.antares.chatdev.langgraph4j.node;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import com.antares.chatdev.ai.service.AiCodeGenTypeRoutingService;
import com.antares.chatdev.langgraph4j.WorkflowContext;
import com.antares.chatdev.langgraph4j.utils.SpringContextUtil;
import com.antares.chatdev.model.enums.CodeGenTypeEnum;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RouterNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 智能路由");
            
            // 实际执行智能路由逻辑
            CodeGenTypeEnum generationType;
            try {
                // 获取AI路由服务
                AiCodeGenTypeRoutingService routingService = SpringContextUtil.getBean(AiCodeGenTypeRoutingService.class);
                // 根据原始提示词进行智能路由
                generationType = routingService.routeCodeGenType(context.getOriginalPrompt());
                log.info("AI智能路由完成，选择类型: {} ({})", generationType.getValue(), generationType.getText());
            } catch (Exception e) {
                log.error("AI智能路由失败，使用默认HTML类型: {}", e.getMessage());
                generationType = CodeGenTypeEnum.HTML;
            }

            // 更新状态
            context.setCurrentStep("智能路由");
            context.setGenerationType(generationType);
            log.info("路由决策完成，选择类型: {}", generationType.getText());
            return WorkflowContext.saveContext(context);
        });
    }
}


