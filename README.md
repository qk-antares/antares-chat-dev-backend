



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

- 提供了更灵活的QueryWrapper，支持关联查询、多表查询等
- 号称性能更高，比Mybatis Plus快指数级

1. 添加依赖

```xml
<dependency>
    <groupId>com.mybatis-flex</groupId>
    <artifactId>mybatis-flex-spring-boot3-starter</artifactId>
    <version>1.11.0</version>
</dependency>
<!-- 代码生成模块 -->
<dependency>
    <groupId>com.mybatis-flex</groupId>
    <artifactId>mybatis-flex-codegen</artifactId>
    <version>1.11.0</version>
</dependency>
<!-- 数据库连接池 -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>4.0.3</version>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

2. 配置数据源

```yml
spring:
  # mysql
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://172.17.0.1:3306/antares_chat_dev
    username: root
    password: 123456
```

3. 代码生成器

类似MybatisX插件的作用：通过读取数据库表结构，自动生成对应的实体类、Mapper接口、XML文件等代码，提高开发效率。

```java
package com.antares.chatdev.generator;

import java.util.Map;

import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.zaxxer.hikari.HikariDataSource;

import cn.hutool.core.lang.Dict;
import cn.hutool.setting.yaml.YamlUtil;

/**
 * MyBatis Flex 代码生成器
 */
public class MyBatisCodeGenerator {

    // 要生成的表名
    private static final String[] TABLE_NAMES = {"user"};

    public static void main(String[] args) {
        // 获取数据元信息
        Dict dict = YamlUtil.loadByPath("application.yml");
        Map<String, Object> dataSourceConfig = dict.getByPath("spring.datasource");
        String url = String.valueOf(dataSourceConfig.get("url"));
        String username = String.valueOf(dataSourceConfig.get("username"));
        String password = String.valueOf(dataSourceConfig.get("password"));
        // 配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        // 创建配置内容
        GlobalConfig globalConfig = createGlobalConfig();

        // 通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);

        // 生成代码
        generator.generate();
    }


    // 详细配置见：https://mybatis-flex.com/zh/others/codegen.html
    public static GlobalConfig createGlobalConfig() {
        // 创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        // 设置根包，建议先生成到一个临时目录下，生成代码之后，再移动到对应的项目目录
        globalConfig.getPackageConfig()
                .setBasePackage("com.antares.chatdev.generator");

        // 设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
        globalConfig.getStrategyConfig()
                .setGenerateTable(TABLE_NAMES)
                // 设置逻辑删除的默认字段名称
                .setLogicDeleteColumn("isDelete");

        // 设置生成 entity 并启用 Lombok
        globalConfig.enableEntity()
                .setWithLombok(true)
                .setJdkVersion(21);

        // 设置生成 mapper
        globalConfig.enableMapper();
        globalConfig.enableMapperXml();

        // 设置生成 Service
        globalConfig.enableService();
        globalConfig.enableServiceImpl();

        // 设置生成 Controller
        globalConfig.enableController();

        return globalConfig;
    }
}
```

#### 其他特性

1. 注解开启雪花ID
```java
@Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
private Long id;
```


### 设计模式

#### 门面模式
设计AiCodeGeneratorFacade来协调AiCodeGeneratorService、CodeFileSaver和CodeParser的调用，最终只给用户暴漏统一的接口：
- public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum)
- public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum)
用户无需关心不同的CodeGenTypeEnum对应的生成逻辑以及所依赖的其他类，只需调用门面类的统一接口即可完成代码生成和保存的功能。

#### 策略模式
不同的CodeGenTypeEnum对应不同的文本解析策略，设计CodeParser<T>接口来定义文本解析为代码的抽象层，不同的实现类，如HtmlCodeParser、MultiFileCodeParser分别用于从大模型返回的文本提取HTML代码片段和多文件代码片段，且易于拓展。此外这里用到了泛型，因为不同的解析策略解析的结果类型也不同。

#### 模板方法模式
不同的CodeGenTypeEnum在生成代码后的保存流程上是类似的，都是[1.验证输入->2.构建唯一目录->3.保存文件（具体实现交给子类）->4.返回文件目录对象]。设计CodeFileSaver抽象模板类来定义保存代码的模板方法`public final File saveCode(T result)`，该方法由`final`修饰不允许重写，而将具体的[1,2,3,4]步骤定义为protected方法（还可能是抽线的），不同的子类可以具体实现或者重写。这样可以复用公共的流程逻辑，同时允许子类定制具体的实现细节。

### Java

#### SSE与Flux
ServerSentEvent（SSE）是一种服务端向浏览器单向推送数据的协议，常用于实时消息推送。

Flux 是 Spring WebFlux 提供的响应式流（reactive stream）类型，可以表示0到N个异步数据项的流。

关系：  
在 Spring WebFlux 中，SSE 的数据推送通常就是通过返回 `Flux<T>`（如 `Flux<String>` 或 `Flux<ServerSentEvent<T>>`）实现的。  
也就是说，Flux 作为数据流，SSE 作为传输协议，二者结合可以实现服务端实时、分批地向前端推送数据。


#### `yield` 关键字
在 Java 的 switch 表达式中，yield 关键字用于返回一个值作为该 case 的结果。
它的作用是：在 switch 表达式的某个分支内，将一个值“产出”给整个 switch 表达式。
这样 switch 语句就可以像三元表达式一样有返回值，yield 后面跟的就是这个分支的结果。

### Redis

#### 旁路缓存模式

- 查询时先检查缓存，命中则直接返回
- 缓存未命中则查询数据库，并将结果写入缓存
- 设置合理的过期时间（根据数据的更新频率），**无需主动删除缓存**


### 测试

1. 用户登录

```
curl -X POST "http://localhost:8014/api/user/login" \
  -H "Content-Type: application/json" \
  -d '{
    "userAccount": "antares",
    "userPassword": "12345678"
  }' \
  -c cookies.txt
```

2. 调用生成代码接口（流式）

```
curl -G "http://localhost:8014/api/app/chat/gen/code" \
  --data-urlencode "appId=324296704651251712" \
  --data-urlencode "message=做一个贪吃蛇游戏网站" \
  -H "Accept: text/event-stream" \
  -H "Cache-Control: no-cache" \
  -b cookies.txt \
  --no-buffer
```

部署服务本质上就是将代码拷贝到nginx代理的一个目录下，从而提供对外访问能力

