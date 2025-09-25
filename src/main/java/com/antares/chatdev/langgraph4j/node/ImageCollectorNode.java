package com.antares.chatdev.langgraph4j.node;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import com.antares.chatdev.langgraph4j.WorkflowContext;
import com.antares.chatdev.langgraph4j.ai.ImageCollectionPlanService;
import com.antares.chatdev.langgraph4j.model.ImageCollectionPlan;
import com.antares.chatdev.langgraph4j.model.ImageResource;
import com.antares.chatdev.langgraph4j.tools.ImageSearchTool;
import com.antares.chatdev.langgraph4j.tools.UndrawIllustrationTool;
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
            List<ImageResource> collectedImages = context.getImageList();
            try {
                // 1. 获取图片收集计划
                ImageCollectionPlanService imageCollectionPlanService = SpringContextUtil
                        .getBean(ImageCollectionPlanService.class);
                ImageCollectionPlan plan = imageCollectionPlanService.planImageCollection(originalPrompt);
                log.info("获取到图片收集计划，开始并发执行");

                // 2.并发执行各种图片收集任务
                List<CompletableFuture<List<ImageResource>>> futures = new ArrayList<>();
                // 并发执行内容图片搜索
                ImageSearchTool imageSearchTool = SpringContextUtil.getBean(ImageSearchTool.class);
                for (ImageCollectionPlan.ImageSearchTask task : plan.getContentImageTasks()) {
                    futures.add(CompletableFuture.supplyAsync(() -> imageSearchTool.searchContentImages(task.query())));
                }

                // 并发执行插画图片搜索
                UndrawIllustrationTool illustrationTool = SpringContextUtil.getBean(UndrawIllustrationTool.class);
                for (ImageCollectionPlan.IllustrationTask task : plan.getIllustrationTasks()) {
                    futures.add(CompletableFuture.supplyAsync(() -> illustrationTool.searchIllustrations(task.query())));
                }

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                //收集所有结果
                for (CompletableFuture<List<ImageResource>> future : futures) {
                    List<ImageResource> imageList = future.get();
                    if (imageList != null) {
                        collectedImages.addAll(imageList);
                    }
                }
                log.info("图片收集完成，收集到 {} 张图片", collectedImages.size());
            } catch (Exception e) {
                log.error("图片收集失败: {}", e.getMessage(), e);
            }

            // 更新状态
            context.setCurrentStep("图片收集");
            context.setImageList(collectedImages);

            return WorkflowContext.saveContext(context);
        });
    }
}
