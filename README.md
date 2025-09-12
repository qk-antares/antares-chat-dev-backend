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

#### 与前端openapi2ts 配合使用

1. 安装 openapi2ts

```bash
pnpm add -D openapi2ts
pnpm add -D tslib
```
2. 在 package.json 中添加脚本

```json
"scripts": {
  "openapi": "openapi2ts"
}
```

3. 在项目根目录创建 openapi2ts.config.ts 文件，内容如下

```ts
// 根据后端接口生成前端请求和 TS 模型代码
export default {
  requestLibPath: "import request from '@/request'",
  schemaPath: 'http://127.0.0.1:8014/api/v3/api-docs',
  serversPath: './src',
}
```


### Mybatis Flex

