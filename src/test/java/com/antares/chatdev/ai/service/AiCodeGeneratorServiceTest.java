package com.antares.chatdev.ai.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.antares.chatdev.ai.model.HtmlCodeResult;
import com.antares.chatdev.ai.model.MultiFileCodeResult;

import jakarta.annotation.Resource;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        // HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(1L, "做个情人节表白网站");
        // Assertions.assertNotNull(result);
        HtmlCodeResult result2 = aiCodeGeneratorService.generateHtmlCode(1L, "不要生成网站，告诉我你刚才做了什么？");
        Assertions.assertNotNull(result2);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult multiFileCode = aiCodeGeneratorService.generateMultiFileCode(2L, "做个程序员鱼皮的留言板");
        Assertions.assertNotNull(multiFileCode);
    }
}

