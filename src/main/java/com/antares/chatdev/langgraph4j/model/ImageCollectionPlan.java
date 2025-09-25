package com.antares.chatdev.langgraph4j.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 图片收集计划
 */
@Data
public class ImageCollectionPlan implements Serializable {
    
    /**
     * 内容图片搜索任务列表
     */
    private List<ImageSearchTask> contentImageTasks;
    
    /**
     * 插画图片搜索任务列表
     */
    private List<IllustrationTask> illustrationTasks;

    /**
     * 内容图片搜索任务
     * 对应 ImageSearchTool.searchContentImages(String query)
     */
    public record ImageSearchTask(String query) implements Serializable {}
    
    /**
     * 插画图片搜索任务
     * 对应 UndrawIllustrationTool.searchIllustrations(String query)
     */
    public record IllustrationTask(String query) implements Serializable {}
}