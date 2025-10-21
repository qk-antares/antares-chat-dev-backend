package com.antares.chatdev.config;

import org.apache.ibatis.logging.stdout.StdOutImpl;

import com.mybatisflex.core.mybatis.FlexConfiguration;
import com.mybatisflex.spring.boot.ConfigurationCustomizer;

// 关闭日志
// @Configuration
public class MybatisFlexConfig implements ConfigurationCustomizer{
    @Override
    public void customize(FlexConfiguration configuration) {
        configuration.setLogImpl(StdOutImpl.class);
    }
}
