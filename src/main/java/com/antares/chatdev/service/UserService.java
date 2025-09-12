package com.antares.chatdev.service;

import java.util.List;

import com.antares.chatdev.model.dto.UserQueryRequest;
import com.antares.chatdev.model.entity.User;
import com.antares.chatdev.model.vo.LoginUserVO;
import com.antares.chatdev.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户 服务层。
 *
 * @author root
 * @since 2025-09-12
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    String getEncryptPassword(String userPassword);

    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    List<UserVO> getUserVOList(List<User> userList);

    UserVO getUserVO(User user);

}
