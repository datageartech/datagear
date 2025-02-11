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
<#--页面JS对象块 -->
<#assign WebUtils=statics['org.datagear.web.util.WebUtils']>
<script type="text/javascript">
var ${pid} =
{
	//当前页面ID
	pid: "${pid}",
	
	//父页面对象ID
	ppid: "${ppid}",
	
	//父页面对象ID参数名
	ppidParamName: "${WebUtils.KEY_PARENT_PAGE_ID}",
	
	//应用根路径
	contextPath: "${contextPath}",
	
	//是否ajax请求
	isAjaxRequest: ("${isAjaxRequest?string('true', 'false')}"  == "true"),
	
	i18n:
	{
		confirm: "<@spring.message code='confirm' />",
		cancel : "<@spring.message code='cancel' />",
		operationConfirm : "<@spring.message code='operationConfirm' />",
		confirmDeleteAsk: "<@spring.message code='confirmDeleteAsk' />",
	}
};

//page.js
$.inflatePageObj(${pid});
</script>
