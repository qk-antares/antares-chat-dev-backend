package com.antares.chatdev.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.antares.chatdev.model.entity.User;

import jakarta.annotation.Resource;

@SpringBootTest
public class AppServiceTest {
    @Resource
    UserService userService;
    @Resource
    AppService appService;

    @Test
    public void testChatToGenCode() {
        User loginUser = userService.getById(329162542038454272L);
        // 获取返回的 Flux
        var flux = appService.chatToGenCode(329511373691203584L,
                "将Header改为深灰色背景，宽度占据整个页面。将导航栏的字体增大加粗，字体颜色改为白色 选中元素信息： - 页面路径: #/ - 标签: nav - 选择器: div#app > nav:nth-child(1) - 当前内容: 首页关于我",
                loginUser);
        // 收集并阻塞等待全部生成（设置超时时间防止无限等待）
        var contents = flux.collectList().block(java.time.Duration.ofSeconds(120));
        // 打印输出
        if (contents != null) {
            contents.forEach(System.out::println);
        }
        // 简单断言：至少有内容
        org.junit.jupiter.api.Assertions.assertTrue(contents != null && !contents.isEmpty(), "生成内容为空");
    }
}
