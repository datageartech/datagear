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
<link href="${contextPath}/static/lib/primevue@3.15.0/resources/themes/<@spring.theme code='primevue.cssName' />/theme.css" type="text/css" rel="stylesheet" id="primevueCssLink" />
<link href="${contextPath}/static/lib/primevue@3.15.0/resources/primevue.min.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/primeicons@5.0.0/primeicons.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/primeflex@3.1.2/primeflex.min.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/codemirror-5.64.0/lib/codemirror.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/codemirror-5.64.0/addon/hint/show-hint.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/codemirror-5.64.0/addon/fold/foldgutter.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/style.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/codemirror-5.64.0/custom.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/theme/<@spring.theme code='styleName' />/style.css" type="text/css" rel="stylesheet" id="styleCssLink" />

<script src="${contextPath}/static/lib/jquery-3.6.0/jquery-3.6.0.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jquery-validation-1.19.3/jquery.validate.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jquery-validation-1.19.3/additional-methods.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/vue@3.2.36/vue.global.prod.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/core/core.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/tabmenu/tabmenu.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/card/card.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/datatable/datatable.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/column/column.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/contextmenu/contextmenu.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/dialog/dialog.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/checkbox/checkbox.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/textarea/textarea.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/toast/toast.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/toastservice/toastservice.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/password/password.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/divider/divider.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/selectbutton/selectbutton.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/confirmdialog/confirmdialog.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/confirmationservice/confirmationservice.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/togglebutton/togglebutton.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/splitbutton/splitbutton.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/tabview/tabview.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/tabpanel/tabpanel.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/menu/menu.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/chip/chip.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/fileupload/fileupload.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/inlinemessage/inlinemessage.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/steps/steps.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/primevue@3.15.0/dataview/dataview.min.js" type="text/javascript"></script>

<script src="${contextPath}/static/lib/codemirror-5.64.0/lib/codemirror.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/addon/hint/show-hint.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/addon/search/searchcursor.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/addon/fold/xml-fold.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/addon/fold/foldcode.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/addon/fold/foldgutter.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/addon/edit/matchbrackets.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/addon/edit/matchtags.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/addon/edit/closetag.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/addon/edit/closebrackets.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/mode/xml/xml.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/mode/css/css.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/mode/htmlmixed/htmlmixed.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/mode/javascript/javascript.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/mode/sql/sql.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/clipboard-2.0.4/clipboard.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/util.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/schemaUrlBuilder.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/tableMeta.js" type="text/javascript"></script>
</#if>