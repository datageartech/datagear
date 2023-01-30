<#--
 *
 * Copyright 2018-2023 datagear.tech
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
