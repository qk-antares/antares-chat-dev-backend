package com.antares.chatdev.utils;

import java.io.File;

import com.antares.chatdev.constant.AppConstant;
import com.antares.chatdev.exception.BusinessException;
import com.antares.chatdev.exception.ErrorCode;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebScreenshotUtils {
    public static int DEFAULT_WIDTH = 1280;
    public static int DEFAULT_HEIGHT = 720;
    public static String imageFormat = "webp";

    /**
     * 生成网页截图
     *
     * @param webUrl 网页URL
     * @return 压缩后的截图文件路径，失败返回null
     */
    public static String saveWebPageScreenshot(String webUrl) {
        if (StrUtil.isBlank(webUrl)) {
            log.error("网页URL不能为空");
            return null;
        }
        try {
            String imageFileName = RandomUtil.randomString(6) + "." + imageFormat;
            // 原始截图文件路径
            String imageSavePath = AppConstant.COVER_IMAGE_DIR + File.separator + imageFileName;
            // 构造 API 请求 URL
            String apiUrl = StrUtil.format(
                "https://screenshotsnap.com/api/screenshot?url={}&format={}&width={}&height={}",
                webUrl, imageFormat, DEFAULT_WIDTH, DEFAULT_HEIGHT
            );
            // 发起 HTTP 请求获取图片
            byte[] imageBytes = HttpUtil.downloadBytes(apiUrl);
            if (imageBytes == null || imageBytes.length == 0) {
                log.error("截图 API 返回空数据: {}", apiUrl);
                return null;
            }
            // 保存图片
            saveImage(imageBytes, imageSavePath);
            log.info("原始截图保存成功: {}", imageSavePath);
            return imageFileName;
        } catch (Exception e) {
            log.error("网页截图失败: {}", webUrl, e);
            return null;
        }
    }

    /**
     * 保存图片到文件
     */
    private static void saveImage(byte[] imageBytes, String imagePath) {
        try {
            FileUtil.writeBytes(imageBytes, imagePath);
        } catch (Exception e) {
            log.error("保存图片失败: {}", imagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }
}
