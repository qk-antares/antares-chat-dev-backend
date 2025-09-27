package com.antares.chatdev.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebScreenshotUtilsTest {

    @Test
    void saveWebPageScreenshot() {
        String testUrl = "https://chatdev-demo.fffu.fun:44480/YICPqQ/";
        boolean success = WebScreenshotUtils.saveWebPageScreenshot(testUrl, "YICPqQ.webp");
        Assertions.assertTrue(success);
    }
}

