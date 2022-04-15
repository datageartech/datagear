<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<meta charset="utf-8" />
<meta name="description" content="DataGear是一款开源免费的数据可视化分析平台，可自由制作任何您想要的数据可视化看板，支持接入SQL、CSV、Excel、HTTP接口、JSON等多种数据源。" />
<meta name="keywords" content="开源免费的数据可视化平台, 开源免费的数据可视化系统, 开源免费的数据可视化分析平台, 开源免费的数据可视化分析系统, 数据可视化平台, 数据可视化系统, 数据可视化分析平台, 数据可视化分析系统, 数据可视化, 商业智能, 图表, 看板, data visualization, data analysis, BI, chart, dashboard" />
<#if !isAjaxRequest>
<#assign _hh_Version=statics['org.datagear.util.Global'].VERSION>
<script type="text/javascript">
var contextPath="${contextPath}";
</script>
<link href="${contextPath}/static/image/logo/icon-80x80/logo-red.png" type="images/x-icon" rel="shortcut icon" />
<link id="css_jquery_ui" href="${contextPath}/static/theme/<@spring.theme code='theme' />/jquery-ui-1.12.1/jquery-ui.css" type="text/css" rel="stylesheet" />
<link id="css_jquery_ui_theme" href="${contextPath}/static/theme/<@spring.theme code='theme' />/jquery-ui-1.12.1/jquery-ui.theme.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/jquery.layout-1.9.0/css/layout-default.min.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/jstree-3.3.7/themes/default/style.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/DataTables-1.11.3/css/datatables.min.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/jQuery-File-Upload-9.21.0/css/jquery.fileupload.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/jquery.steps-1.1.0/css/jquery.steps.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/jquery-datetimepicker-2.5.20/jquery.datetimepicker.min.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/codemirror-5.64.0/lib/codemirror.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/codemirror-5.64.0/addon/hint/show-hint.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/codemirror-5.64.0/addon/fold/foldgutter.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/style.css?v=${_hh_Version}" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/analysis.css?v=${_hh_Version}" type="text/css" rel="stylesheet" />
<link id="css_codemirror" href="${contextPath}/static/theme/<@spring.theme code='theme' />/codemirror-5.64.0/custom.css?v=${_hh_Version}" type="text/css" rel="stylesheet" />
<link id="css_common" href="${contextPath}/static/theme/<@spring.theme code='theme' />/style.css?v=${_hh_Version}" type="text/css" rel="stylesheet" />

<script src="${contextPath}/static/lib/jquery-3.6.0/jquery-3.6.0.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jquery-ui-1.12.1/jquery-ui.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jquery.layout-1.9.0/js/jquery.layout_and_plugins.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jstree-3.3.7/jstree.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/DataTables-1.11.3/js/datatables.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jQuery-File-Upload-9.21.0/js/jquery.iframe-transport.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jQuery-File-Upload-9.21.0/js/jquery.fileupload.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jquery.cookie-1.4.1/jquery.cookie.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jquery-validation-1.19.3/jquery.validate.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jquery-validation-1.19.3/additional-methods.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jquery.steps-1.1.0/js/jquery.steps.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/lib/codemirror.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/addon/hint/show-hint.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/addon/search/searchcursor.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/addon/fold/xml-fold.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/addon/fold/foldcode.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/addon/fold/foldgutter.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/addon/edit/matchbrackets.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/addon/edit/matchtags.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/addon/edit/closetag.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/mode/xml/xml.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/mode/css/css.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/mode/htmlmixed/htmlmixed.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/mode/javascript/javascript.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/codemirror-5.64.0/mode/sql/sql.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/clipboard-2.0.4/clipboard.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jquery-datetimepicker-2.5.20/jquery.datetimepicker.full.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/tableMeta.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/util.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/tableform.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/schemaUrlBuilder.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/chartFactory.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/chartSetting.js?v=${_hh_Version}" type="text/javascript"></script>
</#if>