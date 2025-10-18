package com.antares.chatdev.controller;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.antares.chatdev.annotation.AuthCheck;
import com.antares.chatdev.common.BaseResponse;
import com.antares.chatdev.common.ResultUtils;
import com.antares.chatdev.constant.UserConstant;
import com.antares.chatdev.exception.BusinessException;
import com.antares.chatdev.exception.ErrorCode;
import com.antares.chatdev.exception.ThrowUtils;
import com.antares.chatdev.model.dto.chathistory.ChatHistoryQueryRequest;
import com.antares.chatdev.model.entity.ChatHistory;
import com.antares.chatdev.model.entity.User;
import com.antares.chatdev.service.ChatHistoryService;
import com.antares.chatdev.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 对话历史 控制层。
 *
 * @author root
 * @since 2025-09-16
 */
@RestController
@RequestMapping("/chatHistory")
@Slf4j
public class ChatHistoryController {
    @Resource
    private ChatHistoryService chatHistoryService;
    @Resource
    private UserService userService;

    /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * @param appId          应用ID
     * @param pageSize       页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param request        请求
     * @return 对话历史分页
     */
    @GetMapping("/app/{appId}")
    public BaseResponse<Page<ChatHistory>> listAppChatHistory(@PathVariable Long appId,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) LocalDateTime lastCreateTime,
            HttpServletRequest request) {
        User loginUser = null;
        try {
            loginUser = userService.getLoginUser(request);
        } catch (BusinessException e) {
            log.info("匿名用户查看对话历史");
        }
        Page<ChatHistory> result = chatHistoryService.listAppChatHistoryByPage(appId, pageSize, lastCreateTime,
                loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 管理员分页查询所有对话历史
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 对话历史分页
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> listAllChatHistoryByPageForAdmin(
            @RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = chatHistoryQueryRequest.getPageNum();
        long pageSize = chatHistoryQueryRequest.getPageSize();
        // 查询数据
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> result = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(result);
    }

}
