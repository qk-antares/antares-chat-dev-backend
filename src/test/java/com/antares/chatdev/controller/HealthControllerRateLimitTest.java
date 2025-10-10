package com.antares.chatdev.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;


@SpringBootTest
@AutoConfigureMockMvc
public class HealthControllerRateLimitTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testRateLimit() throws Exception {
        // 前5次请求应通过
        for (int i = 0; i < 5; i++) {
            ResultActions perform = mockMvc.perform(get("/health/"));
            System.out.println(perform.andReturn().getResponse().getContentAsString());
        }
        // 第6次请求应被限流
        ResultActions perform = mockMvc.perform(get("/health/"));
        System.out.println(perform.andReturn().getResponse().getContentAsString());
    }
    
}
