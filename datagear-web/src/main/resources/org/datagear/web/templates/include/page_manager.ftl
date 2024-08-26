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
管理页JS片段。

变量：
//操作
String action

-->
<#assign AbstractController=statics['org.datagear.web.controller.AbstractController']>
<script>
(function(po)
{
	po.action = "${requestAction!AbstractController.REQUEST_ACTION_MANAGE}";
	po.isManageAction = (po.action.indexOf("${AbstractController.REQUEST_ACTION_MANAGE}") == 0);
	po.isSelectAction = (po.action.indexOf("${AbstractController.REQUEST_ACTION_SELECT}") == 0);
	po.isMultipleSelect = ("${(isMultipleSelect!false)?string('true','false')}" == "true");
	po.isReadonlyAction = ("${(isReadonlyAction!false)?string('true','false')}" == "true");
	
	po.i18n.pleaseSelectOnlyOne = "<@spring.message code='pleaseSelectOnlyOne' />";
	po.i18n.pleaseSelectAtLeastOne = "<@spring.message code='pleaseSelectAtLeastOne' />";
	
	$.inflatePageManager(po);
})
(${pid});
</script>
