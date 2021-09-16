# DataGear

DataGear是一款开源免费的数据可视化分析平台，可自由制作任何您想要的数据可视化看板，支持接入SQL、CSV、Excel、HTTP接口、JSON等多种数据源。

系统基于Spring Boot、Jquery、ECharts等技术开发。

## [DataGear 2.8.0 已发布，欢迎官网下载使用！](http://www.datagear.tech)

## [DataGear 大屏看板模板，持续更新中...](https://gitee.com/datagear/DataGearDashboardTemplate)

## 系统特点

- 动态接入多种数据源
<br>支持动态接入任意提供JDBC驱动的数据库，包括MySQL、Oracle、PostgreSQL、SQL Server等关系数据库，以及Elasticsearch、ClickHouse、Hive等大数据引擎

- 支持多种格式的数据集
<br>支持创建SQL、CSV、Excel、HTTP接口、JSON数据集，可将数据集定义为动态参数化数据集，可添加文本框、下拉框、日期框、时间框等类型的数据集参数，为构建动态可交互图表提供支持

- 丰富强大的图表功能
<br>图表可聚合多个不同格式的数据集，轻松构建同比、环比数据图表，内置折线图、柱状图、饼图、地图、雷达图、漏斗图、散点图、K线图、桑基图等50+开箱即用的图表，并且支持自定义图表配置项，支持编写和上传自定义图表插件

- 可自由编辑的数据可视化页面
<br>可视化页面采用原生的HTML网页作为模板，可自由编辑页面内容，支持导入任意HTML网页，为元素添加扩展属性即可绑定和配置图表，页面内置丰富的API，可构建图表联动、数据钻取、异步加载、交互表单等个性化的数据可视化页面

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

QQ群：[916083747（已满）](https://jq.qq.com/?_wv=1027&k=ODxiKOOy)、[1128360199（已满）](https://jq.qq.com/?_wv=1027&k=XkQ4ARMY)、[541252568](https://jq.qq.com/?_wv=1027&k=F7dwDVLO)

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

### 准备单元测试环境

1. 安装 MySQL-8.0 数据库，并将`root`用户的密码设置为：`root`（或者修改`test/config/jdbc.properties`配置）

2. 新建测试数据库，名称取为：`dg_test`

3. 使用`test/sql/test-sql-script-mysql.sql`脚本初始化`dg_test`库

### 执行编译命令

	mvn clean package

或者，也可不准备单元测试环境，直接执行如下编译命令：

	mvn clean package -DskipTests

编译完成后，将在`datagear-web/target/datagear-[version]-packages/`内生成程序包。

## 调试
	
1. 将`datagear`以maven工程导入至IDE工具

2. 以调试模式运行datagear-web模块的启动类：`org.datagear.web.DataGearApplication`

3. 打开浏览器，输入：`http://localhost:50401`
	
## 调试注意

在调试开发分支前（`dev-*`），建议先备份DataGear工作目录（`[用户主目录]/.datagear`），
因为开发分支程序启动时会修改DataGear工作目录，可能会导致先前使用的正式版程序、以及后续发布的正式版程序无法正常启动。

系统启动时会根据当前版本号自动升级内置数据库（Derby数据库，位于`[用户主目录]/.datagear/derby`目录下），且成功后下次启动时不再自动执行，如果调试时遇到数据库异常，需要查看

	datagear-management/src/main/resources/org/datagear/management/ddl/datagear.sql

文件，从中查找需要更新的SQL语句，手动执行。

然后，手动执行下面更新系统版本号的SQL语句：

	UPDATE DATAGEAR_VERSION SET VERSION_VALUE='当前版本号'
	
例如，对于`2.2.0`版本，应执行：

	UPDATE DATAGEAR_VERSION SET VERSION_VALUE='2.2.0'

系统自带了一个可用于为内置数据库执行SQL语句的简单工具类`org.datagear.web.util.DerbySqlClient`，可以在IDE中直接运行。注意：运行前需要先停止DataGear程序。
