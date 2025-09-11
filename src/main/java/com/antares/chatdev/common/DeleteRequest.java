package com.antares.chatdev.common;

import java.io.Serializable;

import lombok.Data;

/**
 * 删除请求包装类
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}