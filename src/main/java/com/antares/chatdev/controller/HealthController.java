package com.antares.chatdev.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.antares.chatdev.annotation.RateLimit;
import com.antares.chatdev.common.BaseResponse;
import com.antares.chatdev.common.ResultUtils;
import com.antares.chatdev.model.enums.RateLimitType;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/")
    @RateLimit(limitType = RateLimitType.API, rate = 5, rateInterval = 60, message = "AI 对话请求过于频繁，请稍后再试")
    public BaseResponse<String> healthCheck() {
        return ResultUtils.success("ok");
    }
}
