# 三湘秒杀

![](https://img.shields.io/badge/building-passing-green.svg)![GitHub](https://img.shields.io/badge/license-MIT-yellow.svg)![jdk](https://img.shields.io/static/v1?label=oraclejdk&message=8&color=blue)

项目致力于打造一个分布式架构秒杀平台，采用现阶段流行技术来实现，采用前后端分离编写。



## 项目介绍

包括前台系统以及后台管理系统，基于 SpringCloud、SpringCloud Alibaba、MyBatis Plus实现。前台商城系统包括：用户登录、注册、银行产品搜索、产品详情、订单、秒杀活动等模块。后台管理系统包括：系统管理、产品系统、库存系统、订单系统、用户系统、等模块。




## 项目演示（待续）

### 前台商品系统

#### 首页

#### 商品检索

#### 认证

#### 产品详情

### 后台管理系统

#### 登录

![](https://i.loli.net/2021/02/18/6KVEbFZMrgnUet3.png)

#### 产品系统

**产品管理**

**活动管理**

**库存管理**




## 组织结构

```
mapple-kill
├── mapple-common -- 工具类及通用代码
├── mapple-admin -- 后台管理
├── renren-generator -- 项目的代码生成器
├── mapple-coupon -- 产品，活动服务
├── mapple-gateway -- 统一配置网关
├── mapple-order -- 订单服务
├── mapple-search -- 检索服务
├── mapple-seckill -- 秒杀服务
└── mapple-xxx -- (待续)
```

## 技术选型

### 后端技术

|        技术        |       说明       |                      官网                       |
| :----------------: | :--------------: | :---------------------------------------------: |
|     SpringBoot     |   容器+MVC框架   |     https://spring.io/projects/spring-boot      |
|    SpringCloud     |    微服务架构    |     https://spring.io/projects/spring-cloud     |
| SpringCloudAlibaba |    一系列组件    | https://spring.io/projects/spring-cloud-alibaba |
|    MyBatis-Plus    |     ORM框架      |             https://mp.baomidou.com             |
|  renren-generator  | 项目的代码生成器 |   https://gitee.com/renrenio/renren-generator   |
|      RabbitMQ      |     消息队列     |            https://www.rabbitmq.com             |
|      Redisson      |     分布式锁     |      https://github.com/redisson/redisson       |
|       Docker       |   应用容器引擎   |             https://www.docker.com              |
|        OSS         |    对象云存储    |  https://github.com/aliyun/aliyun-oss-java-sdk  |

### 前端技术

|  技术   |    说明    |           官网           |
| :-----: | :--------: | :----------------------: |
|   Vue   |  前端框架  |    https://vuejs.org     |
| Element | 前端UI框架 | https://element.eleme.io |
| node.js | 服务端的js |  https://nodejs.org/en   |

## 架构图（待续）

### 系统架构图

### 业务架构图

## 环境搭建

### 开发工具

|     工具      |        说明         |                      官网                       |
| :-----------: | :-----------------: | :---------------------------------------------: |
|     IDEA      |    开发Java程序     |     https://www.jetbrains.com/idea/download     |
| RedisDesktop  | redis客户端连接工具 |        https://redisdesktop.com/download        |
|  SwitchHosts  |    本地host管理     |       https://oldj.github.io/SwitchHosts        |
|    X-shell    |  Linux远程连接工具  | http://www.netsarang.com/download/software.html |
|    Navicat    |   数据库连接工具    |       http://www.formysql.com/xiazai.html       |
| PowerDesigner |   数据库设计工具    |             http://powerdesigner.de             |
|    Postman    |   API接口调试工具   |             https://www.postman.com             |
|    Jmeter     |    性能压测工具     |            https://jmeter.apache.org            |
|    Typora     |   Markdown编辑器    |                https://typora.io                |

### 开发环境

|   工具   | 版本号 |                             下载                             |
| :------: | :----: | :----------------------------------------------------------: |
|   JDK    |  1.8   | https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html |
|  Mysql   |  5.7   |                    https://www.mysql.com                     |
|  Redis   | Redis  |                  https://redis.io/download                   |
| RabbitMQ | 3.8.5  |            http://www.rabbitmq.com/download.html             |
|  Nginx   | 1.1.6  |              http://nginx.org/en/download.html               |



### 部署待续

- 修改Linux中Nginx的配置文件

```shell
1、在nginx.conf中添加负载均衡的配置   
upstream mapple{
	# 网关的地址
	server 192.168.56.1:88;
}    
2、在gulimall.conf中添加如下配置
server {
	# 监听以下域名地址的80端口
    listen       80;
    server_name  mapple.com;

    #charset koi8-r;
    #access_log  /var/log/nginx/log/host.access.log  main;

    #配置静态资源分离
    location /static/ {
        root   /usr/share/nginx/html;
    }

    #支付异步回调的一个配置
    location /payed/ {
        proxy_set_header Host order.gulimall.com;        #不让请求头丢失
        proxy_pass http://mapple;
    }

    location / {
        #root   /usr/share/nginx/html;
        #index  index.html index.htm;
        proxy_set_header Host $host;        #不让请求头丢失
        proxy_pass http://mapple;
    }
```




