# 数据齿轮

浏览器/服务器架构的数据库管理系统。

## 官网

[http://www.datagear.tech](http://www.datagear.tech)

## 下载

[http://www.datagear.tech/download](http://www.datagear.tech/download/)

## 文档

[http://www.datagear.tech/documentation](http://www.datagear.tech/documentation)

## 依赖

	Java 6+
	
## 构建

### 编译

	mvn clean package

### 版本号升级

1. 修改`pom.xml`文件中的`version`标签值；

2. 执行如下maven命令：

		mvn -N versions:update-child-modules antrun:run

