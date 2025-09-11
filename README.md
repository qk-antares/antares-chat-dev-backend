### Knife4j

Swagger UI 是标准的 API 文档界面，展示和测试接口；
Knife4j 是在 Swagger UI 基础上进行功能增强和界面优化的国产开源项目，提供更多文档管理和交互体验。
简单来说，Knife4j 是对 Swagger UI 的扩展和增强

#### Spring Boot3 集成 Knife4j
1. 添加依赖

```xml
<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
    <version>4.4.0</version>
</dependency>
```

2. yml 配置

```yml
# springdoc-openapi项目配置
springdoc:
  group-configs:
    - group: 'default'
      packages-to-scan: com.antares.chatdev.controller
knife4j:
  enable: true
  setting:
    language: zh_cn
```


