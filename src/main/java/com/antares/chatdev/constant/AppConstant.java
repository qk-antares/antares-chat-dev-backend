package com.antares.chatdev.constant;

public interface AppConstant {

    /**
     * 精选应用的优先级
     */
    Integer GOOD_APP_PRIORITY = 99;

    /**
     * 默认应用优先级
     */
    Integer DEFAULT_APP_PRIORITY = 0;

    /**
     * 后端项目的home目录
     */
    // String PROJECT_HOME = System.getProperty("user.dir");
    String PROJECT_HOME = "/software/app/backend/antares-chat-dev-backend";

    /**
     * 应用生成目录
     */
    String CODE_OUTPUT_ROOT_DIR = PROJECT_HOME + "/tmp/code_output";

    /**
     * 应用部署目录
     */
    String CODE_DEPLOY_ROOT_DIR = PROJECT_HOME + "/tmp/code_deploy";

    /**
     * 封面图目录
     */
    String COVER_IMAGE_DIR = PROJECT_HOME + "/tmp/screenshots";

    int DEFAULT_WIDTH = 1296;
    int CROP_WIDTH = 16;
    int DEFAULT_HEIGHT = 736;
    String IMAGE_FORMAT = "webp";
}
