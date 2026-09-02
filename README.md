# pangu

基于spring-boot、spring-cloud、spring-cloud-alibaba搭建项目的通用基础框架包

# 如何选择版本？

> https://start.aliyun.com

参考下面链接说明

* [SpringCloudAlibaba](https://github.com/alibaba/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E)
* https://start.spring.io/actuator/info

# 本地安装

项目严重依赖https://github.com/dongfangding/ddf-common，需要先安装到本地，包括本模块的安装。

由于使用了publish相关插件，安装可能会有一些麻烦，如果只是安装到本地，可以跳过这些步骤。

```shell
mvn clean install -Dmaven.javadoc.skip=true -Dgpg.skip=true
```

# 版本依赖

## Spring Cloud Alibaba组件

Spring Cloud Alibaba的版本选择的是2025.0.0.0， 如果要使用Spring Cloud Alibaba相关组件，遵循如下版本

| 依赖               | 版本    | 
|------------------|-------|
| Nacos Version    | 3.0.3 |
| Sentinel Version | 1.8.9 |
| RocketMQ Version | 5.3.1 |
| Seata Version    | 2.5.0 |
    				
                                                                  
