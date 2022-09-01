<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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

$.inflatePageObj(${pid});
</script>
