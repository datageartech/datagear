# DataGear

DataGear是一款开源免费的数据可视化分析平台，使用Java语言开发，采用浏览器/服务器架构，支持SQL、CSV、Excel、HTTP接口、JSON等多种数据源，
主要功能包括数据管理、SQL工作台、数据导入/导出、数据集管理、图表管理、看板管理等。

## [DataGear 2.1.0 已发布，欢迎官网下载使用！](http://www.datagear.tech)

## [DataGear 大屏看板模板，持续更新中...](https://gitee.com/datagear/DataGearDashboardTemplate)

## 系统特点

- 可管理数据库驱动
<br>可通过驱动程序管理功能添加数据库驱动程序，无需重启，即可支持连接新数据库

- 多种格式的数据集
<br>支持SQL、CSV、Excel、HTTP接口、JSON等多种格式的数据集

- 多数据集聚合图表
<br>一个图表可添加多个不同格式的数据集，将它们聚合展示

- 插件式图表类型
<br>每一种类型的图表都以图表插件形式提供，并内置了大量图表插件，管理员也可上传自定义图表插件，丰富系统图表类型

- 可自由编辑的HTML看板模板
<br>看板使用原生的HTML网页作为模板，可自由编辑、绑定、异步加载图表，并支持将任意HTML网页导入为看板

- 丰富的看板API
<br>看板页面内置了大量的页面端API，可用于个性化扩展看板功能

## 架构图

![https://gitee.com/datagear/datagear/raw/master/screenshot/architecture.png](https://gitee.com/datagear/datagear/raw/master/screenshot/architecture.png)

## 官网

[http://www.datagear.tech](http://www.datagear.tech)

## 文档

[http://www.datagear.tech/documentation](http://www.datagear.tech/documentation)

## 示例

[https://my.oschina.net/u/4035217](https://my.oschina.net/u/4035217)

## 源码

Gitee：[https://gitee.com/datagear/datagear](https://gitee.com/datagear/datagear)

Github：[https://github.com/datageartech/datagear](https://github.com/datageartech/datagear)

## 交流

QQ群：[916083747（已满）](https://jq.qq.com/?_wv=1027&k=ODxiKOOy)、[1128360199](https://jq.qq.com/?_wv=1027&k=XkQ4ARMY)

留言板：[http://www.datagear.tech/messageboard](http://www.datagear.tech/messageboard/)

## 界面

数据管理

![https://gitee.com/datagear/datagear/raw/master/screenshot/datamanage.png](https://gitee.com/datagear/datagear/raw/master/screenshot/datamanage.png)

SQL工作台

![https://gitee.com/datagear/datagear/raw/master/screenshot/sqlpad.png](https://gitee.com/datagear/datagear/raw/master/screenshot/sqlpad.png)

图表

![https://gitee.com/datagear/datagear/raw/master/screenshot/chart.png](https://gitee.com/datagear/datagear/raw/master/screenshot/chart.png)

图表-数据集参数

![https://gitee.com/datagear/datagear/raw/master/screenshot/chart-interaction.png](https://gitee.com/datagear/datagear/raw/master/screenshot/chart-interaction.png)

看板

![https://gitee.com/datagear/datagear/raw/master/screenshot/template-002-dg.png](https://gitee.com/datagear/datagear/raw/master/screenshot/template-002-dg.png)

![https://gitee.com/datagear/datagear/raw/master/screenshot/template-005-dg.png](https://gitee.com/datagear/datagear/raw/master/screenshot/template-005-dg.png)

![https://gitee.com/datagear/datagear/raw/master/screenshot/template-006-dg.png](https://gitee.com/datagear/datagear/raw/master/screenshot/template-006-dg.png)

看板-图表联动

![https://gitee.com/datagear/datagear/raw/master/screenshot/dashboard-map-chart-link.gif](https://gitee.com/datagear/datagear/raw/master/screenshot/dashboard-map-chart-link.gif)

看板-时序图表

![https://gitee.com/datagear/datagear/raw/master/screenshot/dashboard-time-series-chart.gif](https://gitee.com/datagear/datagear/raw/master/screenshot/dashboard-time-series-chart.gif)

看板-钻取

![https://gitee.com/datagear/datagear/raw/master/screenshot/dashboard-map-chart-hierarchy.gif](https://gitee.com/datagear/datagear/raw/master/screenshot/dashboard-map-chart-hierarchy.gif)

看板-表单

![https://gitee.com/datagear/datagear/raw/master/screenshot/dashboard-form.gif](https://gitee.com/datagear/datagear/raw/master/screenshot/dashboard-form.gif)

看板-联动异步加载图表

![https://gitee.com/datagear/datagear/raw/master/screenshot/dashboard-link-load-chart.gif](https://gitee.com/datagear/datagear/raw/master/screenshot/dashboard-link-load-chart.gif)

## 模块介绍

- datagear-analysis
  <br>数据分析底层模块，定义数据集、图表、看板API

- datagear-connection
  <br>数据库连接支持模块，定义可从指定目录加载JDBC驱动、新建连接的API

- datagear-dataexchange
  <br>数据导入/导出底层模块，定义导入/导出指定数据源数据的API

- datagear-management
  <br>系统业务服务模块，定义数据源、数据分析等功能的服务层API

- datagear-meta
  <br>数据源元信息底层模块，定义解析指定数据源表结构的API

- datagear-persistence
  <br>数据源数据管理底层模块，定义读取、编辑、查询数据源表数据的API

- datagear-util
  <br>系统常用工具集模块

- datagear-web
  <br>系统web模块，定义web控制器、操作页面

## 依赖

	Java 8+
	Servlet 3.1+

## 编译

	（执行单元测试编译，需要预先配置单元测试环境）
	mvn clean package


	（不执行单元测试编译，无需预先配置单元测试环境）
	mvn clean package -DskipTests

编译完成后，将在`datagear-web/target/datagear-[version]-packages/`内生成程序包。

## 调试
	
	1. 将datagear以maven工程导入至IDE工具；
	2. 以调试模式运行datagear-web模块的：org.datagear.web.DataGearApplication
	3. 打开浏览器，输入：http://localhost:50401
	
## 调试注意

在调试开发分支前（`dev-*`），建议先备份DataGear工作目录（`[用户主目录]/.datagear`），
因为开发分支程序启动时会修改DataGear工作目录，可能会导致先前使用的正式版程序、以及后续发布的正式版程序无法正常启动。

系统启动时会根据当前版本号自动升级内置数据库（Derby），且成功后下次启动时不再自动执行，如果调试时遇到数据库异常，需要查看

	datagear-management/src/main/resources/org/datagear/management/ddl/datagear.sql

文件，从中查找需要更新的SQL语句，手动执行。

然后，手动执行下面更新系统版本号的SQL语句：

	UPDATE DATAGEAR_VERSION SET VERSION_MAJOR='主版本号', VERSION_MINOR='次版本号', VERSION_REVISION = '修订版本号'
	
例如，对于`2.0.0`版本，应执行：

	UPDATE DATAGEAR_VERSION SET VERSION_MAJOR='2', VERSION_MINOR='0', VERSION_REVISION = '0'

系统自带了一个可用于为内置数据库执行SQL语句的简单工具类`org.datagear.web.util.DerbySqlClient`，可以在IDE中直接运行。注意：运行前需要先停止DataGear程序。
