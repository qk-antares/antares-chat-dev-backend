package com.antares.chatdev;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.antares.chatdev.mapper")
@EnableCaching
public class ChatDevApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatDevApplication.class, args);
    }
}
