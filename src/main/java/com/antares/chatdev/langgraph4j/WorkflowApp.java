package com.antares.chatdev.langgraph4j;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

import java.util.Map;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;

import com.antares.chatdev.langgraph4j.node.CodeGeneratorNode;
import com.antares.chatdev.langgraph4j.node.ImageCollectorNode;
import com.antares.chatdev.langgraph4j.node.ProjectBuilderNode;
import com.antares.chatdev.langgraph4j.node.PromptEnhancerNode;
import com.antares.chatdev.langgraph4j.node.RouterNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WorkflowApp {
    public static void main(String[] args) throws GraphStateException {
        // 创建工作流图
        CompiledGraph<MessagesState<String>> workflow = new MessagesStateGraph<String>()
                // 添加节点
                .addNode("image_collector", ImageCollectorNode.create())
                .addNode("prompt_enhancer", PromptEnhancerNode.create())
                .addNode("router", RouterNode.create())
                .addNode("code_generator", CodeGeneratorNode.create())
                .addNode("project_builder", ProjectBuilderNode.create())

                // 添加边
                .addEdge(START, "image_collector") // 开始 -> 图片收集
                .addEdge("image_collector", "prompt_enhancer") // 图片收集 -> 提示词增强
                .addEdge("prompt_enhancer", "router") // 提示词增强 -> 智能路由
                .addEdge("router", "code_generator") // 智能路由 -> 代码生成
                .addEdge("code_generator", "project_builder") // 代码生成 -> 项目构建
                .addEdge("project_builder", END) // 项目构建 -> 结束

                // 编译工作流
                .compile();

        // 初始化 WorkflowContext - 只设置基本信息
        log.info("开始执行工作流");
        WorkflowContext initialContext = WorkflowContext.builder()
                .originalPrompt("创建一个鱼皮的个人博客网站")
                .currentStep("初始化")
                .build();
        log.info("初始输入: {}", initialContext.getOriginalPrompt());
        log.info("开始执行工作流");

        // 显示工作流图
        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
        log.info("工作流图:\n{}", graph.content());

        // 执行工作流
        int stepCounter = 1;
        for (var step : workflow.stream(Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
            log.info("--- 第 {} 步完成 ---", stepCounter);
            // 显示当前状态
            WorkflowContext currentContext = WorkflowContext.getContext(step.state());
            if (currentContext != null) {
                log.info("当前步骤上下文: {}", currentContext);
            }
            stepCounter++;
        }
    }
}
