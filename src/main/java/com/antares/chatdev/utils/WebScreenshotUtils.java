package com.antares.chatdev.utils;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;

import com.antares.chatdev.constant.AppConstant;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebScreenshotUtils {
    /**
     * 生成网页截图
     *
     * @param webUrl 网页URL
     * @return 压缩后的截图文件路径，失败返回null
     */
    public static boolean saveWebPageScreenshot(String webUrl, String imageSavePath) {
        if (StrUtil.isBlank(webUrl)) {
            log.error("网页URL不能为空");
            return false;
        }
        try {

            // 构造 API 请求 URL
            String apiUrl = StrUtil.format(
                    "https://screenshotsnap.com/api/screenshot?url={}&format={}&width={}&height={}",
                    webUrl, AppConstant.IMAGE_FORMAT, AppConstant.DEFAULT_WIDTH, AppConstant.DEFAULT_HEIGHT);
            // 发起 HTTP 请求获取图片
            byte[] imageBytes = HttpUtil.downloadBytes(apiUrl);
            if (imageBytes == null || imageBytes.length < 2048) {
                log.error("截图 API 返回空数据: {}", apiUrl);
                return false;
            }

            // 读取为 BufferedImage
            BufferedImage original = ImgUtil.read(new ByteArrayInputStream(imageBytes));
            if (original == null) {
                log.error("无法解析图片数据");
                return false;
            }

            // 裁剪区域：左上角 (0,0)，宽度减去右侧 20px，高度减去底部 20px
            Rectangle rect = new Rectangle(
                    0,
                    0,
                    AppConstant.DEFAULT_WIDTH - AppConstant.CROP_WIDTH,
                    AppConstant.DEFAULT_HEIGHT - AppConstant.CROP_WIDTH
            );

            // 执行裁剪并保存
            File destFile = new File(imageSavePath);
            ImgUtil.cut(original, destFile, rect);

            log.info("截图保存成功: {}", imageSavePath);
            return true;
        } catch (Exception e) {
            log.error("网页截图失败: {}", webUrl, e);
            return false;
        }
    }

    public static void main(String[] args) {
        String destPath = "/root/workplace/java/antares-chat-dev-backend/tmp/screenshots/EwmFAx_cropped.webp";

        String testUrl = "https://chatdev.fffu.fun:44480/demo/EwmFAx/";
        boolean success = saveWebPageScreenshot(testUrl, destPath);
        System.out.println("截图成功: " + success);
    }

}
