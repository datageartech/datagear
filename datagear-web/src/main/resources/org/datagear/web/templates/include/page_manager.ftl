<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
管理页JS片段。

变量：
//操作
String action

-->
<#assign AbstractController=statics['org.datagear.web.controller.AbstractController']>
<script>
(function(po)
{
	po.action = "${requestAction!AbstractController.REQUEST_ACTION_QUERY}";
	po.isQueryAction = (po.action.indexOf("${AbstractController.REQUEST_ACTION_QUERY}") == 0);
	po.isSelectAction = (po.action.indexOf("${AbstractController.REQUEST_ACTION_SELECT}") == 0);
	po.isMultipleSelect = ("${(isMultipleSelect!false)?string('true','false')}" == "true");
	po.isReadonlyAction = ("${(isReadonlyAction!false)?string('true','false')}" == "true");
	po.isReadonlyAction = (po.isReadonlyAction || po.isSelectAction);
	
	po.i18n.pleaseSelectOnlyOne = "<@spring.message code='pleaseSelectOnlyOne' />";
	po.i18n.pleaseSelectAtLeastOne = "<@spring.message code='pleaseSelectAtLeastOne' />";
	
	$.inflatePageManager(po);
})
(${pid});
</script>
