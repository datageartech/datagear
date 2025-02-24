<#--
 *
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 *
-->
<#--
HTML头。

变量：
//应用根路径，不允许为null
String contextPath
//检测新版本结果，不允许为null
DetectResult detectNewVersionResult

-->
<meta charset="utf-8" />
<meta name="description" content="DataGear是一款开源免费的数据可视化分析平台，自由制作任何您想要的数据看板，支持接入SQL、CSV、Excel、HTTP接口、JSON等多种数据源。" />
<meta name="keywords" content="开源免费的数据可视化平台, 开源免费的数据可视化系统, 开源免费的数据可视化分析平台, 开源免费的数据可视化分析系统, 数据可视化平台, 数据可视化系统, 数据可视化分析平台, 数据可视化分析系统, 数据可视化, 商业智能, 图表, 看板, data visualization, data analysis, BI, chart, dashboard" />
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<meta name="viewport" content="width=device-width,initial-scale=1.0" />
<#if !isAjaxRequest>
<#assign Global=statics['org.datagear.util.Global']>
<script type="text/javascript">
var contextPath="${contextPath}";
</script>
<#-- 自定义应用根路径后，浏览器无法自动加载系统图标，所以这里明确指定 -->
<link href="${contextPath}/favicon.ico?v=${clientCacheCode}" type="images/x-icon" rel="shortcut icon" />
<link href="${contextPath}/static/lib/primevue@3.34.1/resources/themes/<@spring.theme code='primevue.cssName' />/theme.css" type="text/css" rel="stylesheet" id="primevueCssLink" />
<link href="${contextPath}/static/lib/primevue@3.34.1/resources/primevue.min.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/primeicons@6.0.1/primeicons.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/primeflex@3.3.1/primeflex.min.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/codemirror-5.64.0/lib/codemirror.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/codemirror-5.64.0/pkg/codemirror-bundle.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/jquery-datetimepicker-2.5.20/jquery.datetimepicker.min.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/analysis.css?v=${clientCacheCode}" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/style.css?v=${clientCacheCode}" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/codemirror-5.64.0/custom.css?v=${clientCacheCode}" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/theme/<@spring.theme code='styleName' />/style.css?v=${clientCacheCode}" type="text/css" rel="stylesheet" id="styleCssLink" />
<script src="${contextPath}/static/lib/jquery-3.7.1/jquery-3.7.1.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jquery-validation-1.19.3/jquery.validate.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jquery-validation-1.19.3/additional-methods.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jquery.cookie-1.4.1/jquery.cookie.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/vue@3.2.47/vue.global.prod.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.34.1/primevue.min.0.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.34.1/primevue.min.1.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.34.1/primevue.min.2.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.34.1/primevue.min.3.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/lib/codemirror.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/pkg/codemirror-bundle.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/clipboard-2.0.4/clipboard.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jquery-datetimepicker-2.5.20/jquery.datetimepicker.full.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/util.js?v=${clientCacheCode}" type="text/javascript"></script>
<script src="${contextPath}/static/script/chartFactory.js?v=${clientCacheCode}" type="text/javascript"></script>
<script src="${contextPath}/static/script/chartSetting.js?v=${clientCacheCode}" type="text/javascript"></script>
<script src="${contextPath}/static/script/dashboardFactory.js?v=${clientCacheCode}" type="text/javascript"></script>
<script src="${contextPath}/static/script/dashboardApiList.js?v=${clientCacheCode}" type="text/javascript"></script>
<script src="${contextPath}/static/script/pages/include/page.js?v=${clientCacheCode}" type="text/javascript"></script>
<script src="${contextPath}/static/script/pages/dashboard/include/dashboardDesign.js?v=${clientCacheCode}" type="text/javascript"></script>
${detectNewVersionResult.script?no_esc}
</#if>