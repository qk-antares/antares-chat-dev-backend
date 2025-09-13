package com.antares.chatdev.model.dto.user;

import java.io.Serializable;

import lombok.Data;

/**
 * 用户登录
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;
}