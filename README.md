# 数据齿轮

##官网

[http://www.datagear.tech](http://www.datagear.tech)

## 依赖

	Java 6+
   
## 编译

	mvn clean package

## 版本号升级

1. 修改`pom.xml`文件中的`version`标签值；

2. 执行如下maven命令：

	mvn -N versions:update-child-modules antrun:run

