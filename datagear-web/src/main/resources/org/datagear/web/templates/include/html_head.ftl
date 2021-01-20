<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="description" content="DataGear是一款数据可视化分析平台，使用Java语言开发，采用浏览器/服务器架构，支持SQL、CSV、Excel、HTTP接口、JSON等多种数据源，主要功能包括数据管理、SQL工作台、数据导入/导出、数据集管理、图表管理、看板管理等。" />
<meta name="keywords" content="DataGear, 数据可视化, data visualization, 数据分析, data analysis, 商业智能, BI, 图表, chart, 看板, dashboard" />
<#if !isAjaxRequest>
<#assign _hh_Version=statics['org.datagear.util.Global'].VERSION>
<script type="text/javascript">
var contextPath="${contextPath}";
</script>
<link id="css_jquery_ui" href="${contextPath}/static/theme/<@spring.theme code='theme' />/jquery-ui-1.12.1/jquery-ui.css" type="text/css" rel="stylesheet" />
<link id="css_jquery_ui_theme" href="${contextPath}/static/theme/<@spring.theme code='theme' />/jquery-ui-1.12.1/jquery-ui.theme.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/jquery.layout-1.4.0/css/jquery.layout.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/jstree-3.3.7/themes/default/style.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/DataTables-1.10.18/css/datatables.min.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/jQuery-File-Upload-9.21.0/css/jquery.fileupload.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/jquery.steps-1.1.0/css/jquery.steps.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/lib/jquery-datetimepicker-2.5.20/jquery.datetimepicker.min.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/ace-21.02.19/ace.css?v=${_hh_Version}" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/datagear-pagination.css?v=${_hh_Version}" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/common.css?v=${_hh_Version}" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/analysis.css?v=${_hh_Version}" type="text/css" rel="stylesheet" />
<link id="css_ace" href="${contextPath}/static/theme/<@spring.theme code='theme' />/ace-21.02.19/ace.css?v=${_hh_Version}" type="text/css" rel="stylesheet" />
<link id="css_common" href="${contextPath}/static/theme/<@spring.theme code='theme' />/common.css?v=${_hh_Version}" type="text/css" rel="stylesheet" />

<script src="${contextPath}/static/lib/jquery-1.12.4/jquery-1.12.4.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jquery-ui-1.12.1/jquery-ui.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jquery.layout-1.4.0/js/jquery.layout.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jstree-3.3.7/jstree.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/DataTables-1.10.18/js/datatables.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jquery.form-3.51.0/jquery.form.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jQuery-File-Upload-9.21.0/js/jquery.iframe-transport.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jQuery-File-Upload-9.21.0/js/jquery.fileupload.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jquery.cookie-1.4.1/jquery.cookie.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jquery-validation-1.17.0/jquery.validate.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jquery-validation-1.17.0/additional-methods.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jquery.steps-1.1.0/js/jquery.steps.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/textarea-helper-0.3.1/textarea-helper.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/ace-21.02.19/ace.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/ace-21.02.19/mode-sql.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/ace-21.02.19/mode-html.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/ace-21.02.19/mode-json.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/ace-21.02.19/mode-javascript.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/ace-21.02.19/mode-css.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/ace-21.02.19/ext-language_tools.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/clipboard-2.0.4/clipboard.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/lib/jquery-datetimepicker-2.5.20/jquery.datetimepicker.full.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/datagear-pagination.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/datagear-meta.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/datagear-util.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/datagear-jquery-override.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/datagear-tableform.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/datagear-schema-url-builder.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/datagear-chartFactory.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/datagear-chartForm.js?v=${_hh_Version}" type="text/javascript"></script>
</#if>