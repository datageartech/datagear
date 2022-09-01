<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
表单JS片段。

变量：
String action

-->
<#assign AbstractController=statics['org.datagear.web.controller.AbstractController']>
<#include "page_validation_msg.ftl">
<script>
(function(po)
{
	po.action = "${requestAction!''}";
	po.isAddAction = (po.action.indexOf("${AbstractController.REQUEST_ACTION_ADD}") == 0);
	po.isEditAction = (po.action.indexOf("${AbstractController.REQUEST_ACTION_EDIT}") == 0);
	po.isViewAction = (po.action.indexOf("${AbstractController.REQUEST_ACTION_VIEW}") == 0);
	po.isCopyAction = (po.action.indexOf("${AbstractController.REQUEST_ACTION_COPY}") == 0);
	po.isReadonlyAction = (po.isViewAction);
	po.submitAction = "${submitAction!'#'}";
	
	/*需实现，字符串、函数*/
	po.submitUrl = "#";
	
	po.form = function()
	{
		return po.elementOfId("${pid}form");
	};
	
	$.inflatePageForm(po);
})
(${pid});
</script>
