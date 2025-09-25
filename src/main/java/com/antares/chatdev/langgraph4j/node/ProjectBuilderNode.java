package com.antares.chatdev.langgraph4j.node;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

import java.io.File;

import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import com.antares.chatdev.core.builder.VueProjectBuilder;
import com.antares.chatdev.exception.BusinessException;
import com.antares.chatdev.exception.ErrorCode;
import com.antares.chatdev.langgraph4j.WorkflowContext;
import com.antares.chatdev.langgraph4j.utils.SpringContextUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProjectBuilderNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 项目构建");

            // 实际执行项目构建逻辑
            // 获取必要的参数
            String generatedCodeDir = context.getGeneratedCodeDir();
            String buildResultDir;
            // 一定是 Vue 项目类型：使用 VueProjectBuilder 进行构建
            try {
                VueProjectBuilder vueBuilder = SpringContextUtil.getBean(VueProjectBuilder.class);
                // 执行 Vue 项目构建（npm install + npm run build）
                boolean buildSuccess = vueBuilder.buildProject(generatedCodeDir);
                if (buildSuccess) {
                    // 构建成功，返回 dist 目录路径
                    buildResultDir = generatedCodeDir + File.separator + "dist";
                    log.info("Vue 项目构建成功，dist 目录: {}", buildResultDir);
                } else {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败");
                }
            } catch (Exception e) {
                log.error("Vue 项目构建异常: {}", e.getMessage(), e);
                buildResultDir = generatedCodeDir; // 异常时返回原路径
            }

            // 更新状态
            context.setCurrentStep("项目构建");
            context.setBuildResultDir(buildResultDir);
            log.info("项目构建完成，结果目录: {}", buildResultDir);
            return WorkflowContext.saveContext(context);
        });
    }
}
