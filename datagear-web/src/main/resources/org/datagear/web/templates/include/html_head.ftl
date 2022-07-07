<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<meta charset="utf-8" />
<meta name="description" content="DataGear是一款开源免费的数据可视化分析平台，自由制作任何您想要的数据看板，支持接入SQL、CSV、Excel、HTTP接口、JSON等多种数据源。" />
<meta name="keywords" content="开源免费的数据可视化平台, 开源免费的数据可视化系统, 开源免费的数据可视化分析平台, 开源免费的数据可视化分析系统, 数据可视化平台, 数据可视化系统, 数据可视化分析平台, 数据可视化分析系统, 数据可视化, 商业智能, 图表, 看板, data visualization, data analysis, BI, chart, dashboard" />
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<meta name="viewport" content="width=device-width,initial-scale=1.0" />
<#if !isAjaxRequest>
<#assign _hh_Version=statics['org.datagear.util.Global'].VERSION>
<script type="text/javascript">
var contextPath="${contextPath}";
</script>
<link href="${contextPath}/static/image/logo/icon-80x80/logo-red.png" type="images/x-icon" rel="shortcut icon" />
<link href="${contextPath}/static/lib/primevue@3.15.0/resources/themes/saga-blue/theme.css" rel="stylesheet" />
<link href="${contextPath}/static/lib/primevue@3.15.0/resources/primevue.min.css" rel="stylesheet" />
<link href="${contextPath}/static/lib/primeicons@5.0.0/primeicons.css" rel="stylesheet" />
<link href="${contextPath}/static/lib/primeflex@3.1.2/primeflex.min.css" rel="stylesheet" />
<link href="${contextPath}/static/css/style.css" rel="stylesheet" />

<script src="${contextPath}/static/lib/jquery-3.6.0/jquery-3.6.0.min.js"></script>
<script src="${contextPath}/static/lib/jquery-validation-1.19.3/jquery.validate.min.js"></script>
<script src="${contextPath}/static/lib/jquery-validation-1.19.3/additional-methods.min.js"></script>
<script src="${contextPath}/static/lib/vue@3.2.36/vue.global.prod.js"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/core/core.min.js"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/tabmenu/tabmenu.min.js"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/card/card.min.js"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/datatable/datatable.min.js"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/column/column.min.js"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/contextmenu/contextmenu.min.js"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/dialog/dialog.min.js"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/textarea/textarea.min.js"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/toast/toast.min.js"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/toastservice/toastservice.min.js"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/password/password.min.js"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/divider/divider.min.js"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/selectbutton/selectbutton.min.js"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/confirmdialog/confirmdialog.min.js"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/confirmationservice/confirmationservice.min.js"></script>

<script src="${contextPath}/static/script/util.js"></script>
</#if>