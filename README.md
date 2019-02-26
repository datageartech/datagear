# 数据齿轮 DataGear

数据齿轮（DataGear）是一款数据库管理系统，使用Java语言开发，采用浏览器/服务器架构，以数据管理为核心功能，支持多种数据库。

它的数据模型并不是原始的数据库表，而是融合了数据库表及表间关系，更偏向于领域模型的数据模型，能够更友好、方便、快速地查询和维护数据。

它采用JDBC规范与数据库进行连接和通信，能够支持所有遵循JDBC规范的数据库，这包括MySQL、Oracle、PostgreSQL、SQL Server等主流数据库。

## 官网

[http://www.datagear.tech](http://www.datagear.tech)

## 文档

[http://www.datagear.tech/documentation](http://www.datagear.tech/documentation)

## 界面

![界面图片](http://www.datagear.tech/static/images/main.png)

## 依赖

	Java 6+
	
## 构建

### 编译

	mvn clean package

### 版本号升级

1. 修改`pom.xml`文件中的`version`标签值；

2. 执行如下maven命令：

		mvn -N versions:update-child-modules antrun:run

