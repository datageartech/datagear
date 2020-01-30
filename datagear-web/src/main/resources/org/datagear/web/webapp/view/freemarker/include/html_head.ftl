<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="description" content="数据齿轮（DataGear）是一款数据库管理系统，使用Java语言开发，采用浏览器/服务器架构，以数据管理为核心功能，支持多种数据库。它的数据模型并不是原始的数据库表，而是融合了数据库表及表间关系，更偏向于领域模型的数据模型，能够更友好、方便、快速地查询和维护数据" />
<meta name="keywords" content="数据齿轮, Data Gear, 数据, data, 数据库, database, 数据管理 , data management, 数据库管理, database management, 浏览器/服务器, B/S" />
<#if !isAjaxRequest>
<script type="text/javascript">
var contextPath="${contextPath}";
</script>
<link id="css_jquery_ui" href="${contextPath}/static/theme/<@spring.theme code='theme' />/jquery-ui-1.12.1/jquery-ui.css" type="text/css" rel="stylesheet" />
<link id="css_jquery_ui_theme" href="${contextPath}/static/theme/<@spring.theme code='theme' />/jquery-ui-1.12.1/jquery-ui.theme.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/jquery.layout-1.4.0/jquery.layout.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/jstree-3.3.7/style.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/DataTables-1.10.18/datatables.min.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/jQuery-File-Upload-9.21.0/jquery.fileupload.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/jquery.steps-1.1.0/jquery.steps.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/ace-21.02.19/ace.css" type="text/css" rel="stylesheet" />
<link id="css_ace" href="${contextPath}/static/theme/<@spring.theme code='theme' />/ace-21.02.19/ace.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/datagear-pagination.css" type="text/css" rel="stylesheet" />
<link href="${contextPath}/static/css/common.css" type="text/css" rel="stylesheet" />
<link id="css_common" href="${contextPath}/static/theme/<@spring.theme code='theme' />/common.css" type="text/css" rel="stylesheet" />

<script src="${contextPath}/static/script/jquery/jquery-1.12.4.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/jquery-ui-1.12.1/jquery-ui.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/jquery.layout-1.4.0/jquery.layout.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/jstree-3.3.7/jstree.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/DataTables-1.10.18/datatables.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/jquery.form-3.51.0/jquery.form.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/jQuery-File-Upload-9.21.0/jquery.iframe-transport.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/jQuery-File-Upload-9.21.0/jquery.fileupload.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/jquery.cookie-1.4.1/jquery.cookie.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/jquery-validation-1.17.0/jquery.validate.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/jquery-validation-1.17.0/additional-methods.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/jquery.steps-1.1.0/jquery.steps.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/textarea-helper-0.3.1/textarea-helper.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/ace-21.02.19/ace.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/ace-21.02.19/mode-sql.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/ace-21.02.19/mode-html.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/ace-21.02.19/ext-language_tools.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/cometd-2.9.1/cometd.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/cometd-2.9.1/AckExtension.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/cometd-2.9.1/ReloadExtension.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/cometd-2.9.1/jquery.cometd.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/cometd-2.9.1/jquery.cometd-ack.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/cometd-2.9.1/jquery.cometd-reload.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/clipboard-2.0.4/clipboard.min.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/datagear-pagination.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/datagear-model.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/datagear-modelcache.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/datagear-util.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/datagear-jquery-override.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/datagear-modelform.js" type="text/javascript"></script>
<script src="${contextPath}/static/script/datagear-schema-url-builder.js" type="text/javascript"></script>
</#if>