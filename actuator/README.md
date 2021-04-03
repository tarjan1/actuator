# actuator
>An actuator is a manufacturing term that refers to a mechanical device for moving or controlling something. 
>Actuators can generate a large amount of motion from a small change.

## 功能
> 模仿spring-actuator开发一种支持低版本（Spring 2+，jvm6+）的服务健康信息、基于git的版本信息的**公共功能jar**。

## 使用对象

> 运维&开发

## 思考

- 外围来拉取数据的接口的结构是怎样的？
   ```/json
   {
   	"status": "UP",
   	"components": {
   		"db": {
   			"status": "UP",
   			"details": {
   				"database": "Oracle",
   				"result": "Hello",
   				"validationQuery": "SELECT 'Hello' from DUAL"
   			}
   		},
   		"diskSpace": {
   			"status": "UP",
   			"details": {
   				"total": 32196526080,
   				"free": 5349879808,
   				"threshold": 1073741824
   			}
   		},
   		"ping": {
   			"status": "UP"
   		}
   	}
   }
   ```
  类似这样，当所有`components`均`up`时，`status`才为`up`；所有组件都是`components`的一部分，均有`status`；

- 某一个关注的组件是怎么实现的？（接口为interface）

  - 定义一个接口，用于一个组件状态的实现
  - 把这个实现作为一个Spring Bean（或者使用SPI）
  - 利用Spring 提供的接口功能，获取到实现了这个接口的所有的Bean，顺序调用，拿到数据，拼装返回


## 项目中集成

### 添加依赖
```xml
        <dependency>
            <groupId>com.huawei.gd</groupId>
            <artifactId>actuator</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
```

### 健康检查（/actuator/health）

- 开放组件检查

默认已有`磁盘空间`、`数据库`、`redis`组件,可根据自己关注的组件，配置bean；[参考](https://gitlab.huawei.com/NGCRM/Infrastructure/actuator/blob/master/src/test/resources/actuator.xml)

- 开启web接口方式（三种方式均可）

  - 在web.xml中配置filter[参考](https://gitlab.huawei.com/NGCRM/Infrastructure/actuator/blob/master/src/main/java/com/huawei/gd/actuator/health/servlet/HealthFilter.java)
  - 在web.xml中配置servlet[参考](https://gitlab.huawei.com/NGCRM/Infrastructure/actuator/blob/master/src/main/java/com/huawei/gd/actuator/health/spring/HealthRequestHandler.java)
  - 基于spring3+，已有（/actuator/health）的Controller，只需要配置bean

- springboot快捷集成

  - 引入依赖，在application（启动类）添加```@ImportResource("classpath:actuator.xml")```

    可[参考](https://gitlab.huawei.com/NGCRM/ONU-SG/ONU/merge_requests/541/diffs)；
    
  - 注意将配置的bean配置进扫描范围 ，可在actuatpr.xml或`@SpringBootApplication(scanBasePackages = { "com.huawei.gd" })`
  
- 自定义组件

  实现集成AbstractHealthIndicator 类，实现其方法即可，可参考项目中indicaor下面的类
###  应用信息（/actuator/info）

- 添加git.properties文件需要编译时实时使用maven插件生成，需要集成[git-commit-id-plugin](https://gitlab.huawei.com/NGCRM/ESOPCentral/ESOPCentral/blob/master/ESOPCentralBase/ESOPCommon/pom.xml)；需要将配置文件放置在classpath下
- 开启web接口方式（三种方式均可）
  - 在web.xml中配置filter[参考](https://gitlab.huawei.com/NGCRM/Infrastructure/actuator/blob/master/src/main/java/com/huawei/gd/actuator/info/servlet/GitInfoFilter.java)
  - 在web.xml中配置servlet[参考](https://gitlab.huawei.com/NGCRM/Infrastructure/actuator/blob/master/src/main/java/com/huawei/gd/actuator/info/spring/GitInfoRequestHandler.java)
  - 基于spring3+，自动序列化json，默认已有（/actuator/info）的Controller，不需要配置

