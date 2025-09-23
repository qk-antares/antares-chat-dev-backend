package com.antares.chatdev.langgraph4j.model;

import java.io.Serial;
import java.io.Serializable;

import com.antares.chatdev.langgraph4j.model.enums.ImageCategoryEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图片资源对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResource implements Serializable {

    /**
     * 图片类别
     */
    private ImageCategoryEnum category;

    /**
     * 图片描述
     */
    private String description;

    /**
     * 图片地址
     */
    private String url;

    @Serial
    private static final long serialVersionUID = 1L;
}
