<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="description" content="DataGear是一款数据管理与可视化分析平台，使用Java语言开发，采用浏览器/服务器架构，支持多种数据库，主要功能包括数据管理、SQL工作台、数据导入/导出、数据集管理、图表管理、看板管理等。" />
<meta name="keywords" content="DataGear, 数据管理, data management, 可视化分析, BI, 数据库管理, database management" />
<#if !isAjaxRequest>
<#assign _hh_Version=statics['org.datagear.util.Global'].VERSION>
<script type="text/javascript">
var contextPath="${contextPath}";
</script>
<link id="css_jquery_ui" href="${contextPath}/static/theme/<@spring.theme code='theme' />/jquery-ui-1.12.1/jquery-ui.css?v=${_hh_Version}" type="text/css" rel="stylesheet" />
<link id="css_jquery_ui_theme" href="${contextPath}/static/theme/<@spring.theme code='theme' />/jquery-ui-1.12.1/jquery-ui.theme.css?v=${_hh_Version}" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/jquery.layout-1.4.0/jquery.layout.css?v=${_hh_Version}" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/jstree-3.3.7/style.css?v=${_hh_Version}" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/DataTables-1.10.18/datatables.min.css?v=${_hh_Version}" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/jQuery-File-Upload-9.21.0/jquery.fileupload.css?v=${_hh_Version}" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/jquery.steps-1.1.0/jquery.steps.css?v=${_hh_Version}" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/ace-21.02.19/ace.css?v=${_hh_Version}" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/script/jquery-datetimepicker-2.5.20/jquery.datetimepicker.min.css?v=${_hh_Version}" type="text/css" rel="stylesheet" />
<link id="css_ace" href="${contextPath}/static/theme/<@spring.theme code='theme' />/ace-21.02.19/ace.css?v=${_hh_Version}" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/datagear-pagination.css?v=${_hh_Version}" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/common.css?v=${_hh_Version}" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/analysis.css?v=${_hh_Version}" type="text/css" rel="stylesheet" />
<link id="css_common" href="${contextPath}/static/theme/<@spring.theme code='theme' />/common.css?v=${_hh_Version}" type="text/css" rel="stylesheet" />

<script src="${contextPath}/static/script/jquery/jquery-1.12.4.min.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/jquery-ui-1.12.1/jquery-ui.min.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/jquery.layout-1.4.0/jquery.layout.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/jstree-3.3.7/jstree.min.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/DataTables-1.10.18/datatables.min.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/jquery.form-3.51.0/jquery.form.min.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/jQuery-File-Upload-9.21.0/jquery.iframe-transport.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/jQuery-File-Upload-9.21.0/jquery.fileupload.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/jquery.cookie-1.4.1/jquery.cookie.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/jquery-validation-1.17.0/jquery.validate.min.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/jquery-validation-1.17.0/additional-methods.min.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/jquery.steps-1.1.0/jquery.steps.min.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/textarea-helper-0.3.1/textarea-helper.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/ace-21.02.19/ace.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/ace-21.02.19/mode-sql.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/ace-21.02.19/mode-html.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/ace-21.02.19/ext-language_tools.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/cometd-2.9.1/cometd.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/cometd-2.9.1/AckExtension.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/cometd-2.9.1/ReloadExtension.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/cometd-2.9.1/jquery.cometd.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/cometd-2.9.1/jquery.cometd-ack.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/cometd-2.9.1/jquery.cometd-reload.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/clipboard-2.0.4/clipboard.min.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/datagear-pagination.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/datagear-meta.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/datagear-util.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/datagear-jquery-override.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/datagear-tableform.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/datagear-schema-url-builder.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/datagear-chartFactory.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/datagear-chartForm.js?v=${_hh_Version}" type="text/javascript"></script>
<script src="${contextPath}/static/script/jquery-datetimepicker-2.5.20/jquery.datetimepicker.full.min.js?v=${_hh_Version}" type="text/javascript"></script>
</#if>