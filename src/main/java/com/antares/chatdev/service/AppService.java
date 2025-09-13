package com.antares.chatdev.service;

import java.util.List;

import com.antares.chatdev.model.dto.AppQueryRequest;
import com.antares.chatdev.model.entity.App;
import com.antares.chatdev.model.entity.User;
import com.antares.chatdev.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;

import reactor.core.publisher.Flux;

/**
 * 应用 服务层。
 *
 * @author root
 * @since 2025-09-13
 */
public interface AppService extends IService<App> {
    /**
     * 获取查询条件
     *
     * @param appQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 分页获取应用封装
     *
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 获取应用封装
     *
     * @param app
     * @return
     */
    AppVO getAppVO(App app);

    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

}
