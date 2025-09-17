package com.antares.chatdev;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.antares.chatdev.mapper")
public class ChatDevApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatDevApplication.class, args);
    }
}
