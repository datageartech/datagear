<#--
数据表单操作页面公用代码
依赖：
data_page_obj.ftl

依赖变量：
//初始数据，由主页面定义，允许为null
po.data = undefined;
//初始表单数据是否是客户端数据
po.isClientPageData = undefined;
-->
<#include "../../include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	po.formLabels =
	{
		add : "<@spring.message code='add' />",
		edit : "<@spring.message code='edit' />",
		del : "<@spring.message code='delete' />",
		view : "<@spring.message code='view' />",
		select : "<@spring.message code='select' />",
		//如果页面参数里定义了提交回调函数，则submit标签改为“确定”
		submit : (po.pageParam("submit") ? "<@spring.message code='confirm' />" : "<@spring.message code='save' />"),
		reset : "<@spring.message code='reset' />",
		batchSet :
		{
			batchSetSwitchTitle : "<@spring.message code='batchSet.batchSetSwitchTitle' />",
			batchCount : "<@spring.message code='batchSet.batchCount' />",
			batchHandleErrorMode : "<@spring.message code='batchSet.batchHandleErrorMode' />",
			batchHandleErrorModeEnum : ["<@spring.message code='batchSet.batchHandleErrorMode.ignore' />", "<@spring.message code='batchSet.batchHandleErrorMode.abort' />", "<@spring.message code='batchSet.batchHandleErrorMode.rollback' />"]
		},
		validation :
		{
			required : "<@spring.message code='validation.required' />"
		}
	};
	
	po.selectColumnValue = function(table, column, value)
	{
		var importKey = $.meta.columnImportKey(table, column);
		if(!importKey)
			return;
		
		var ptable = importKey.primaryTableName;
		var options =
		{
			pageParam :
			{
				submit : function(data)
				{
				}
			}
		};
		$.setGridPageHeightOption(options);
		po.open(po.url(ptable, "select"), options);
	};
	
	po.viewColumnValue = function(table, column, value)
	{
		
	};
	
	po.downloadColumnValue = function(table, column, value)
	{
		var url;
		var options = {target: "_file"};
		
		if(po.isClientPageData)
		{
			url = "${contextPath}/data/downloadFile";
			options.data = {file : value};
		}
		else
			url = po.url("downloadColumnValue");
		
		po.open(url, options);
	};
	
	po.refreshParent = function()
	{
		var poParent = po.parent();
		if(poParent && poParent.refresh && $.isFunction(poParent.refresh))
		{
			try
			{
				poParent.refresh();
			}
			catch(e){}
		}
	};
})
(${pageId});
</script>
