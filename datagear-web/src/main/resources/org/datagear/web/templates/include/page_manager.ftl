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
	po.isQueryAction = (po.action == "${AbstractController.REQUEST_ACTION_QUERY}");
	po.isSelectAction = (po.action == "${AbstractController.REQUEST_ACTION_SELECT}");
	po.isMultipleSelect = ("${(isMultipleSelect!false)?string('true','false')}" == "true");
	
	po.refresh = function(){ /*需实现*/ };
	po.getSelectedEntities = function(){ /*需实现*/ };
	
	po.i18n.pleaseSelectOnlyOne = "<@spring.message code='pleaseSelectOnlyOne' />";
	po.i18n.pleaseSelectAtLeastOne = "<@spring.message code='pleaseSelectAtLeastOne' />";
	
	//单选处理函数
	po.executeOnSelect = function(callback)
	{
		var selected = po.getSelectedEntities();
		
		if(!selected || selected.length != 1)
		{
			$.tipInfo(po.i18n.pleaseSelectOnlyOne);
			return;
		}
		
		callback.call(po, selected[0]);
	};
	
	//多选处理函数
	po.executeOnSelects = function(callback)
	{
		var selected = po.getSelectedEntities();
		
		if(!selected || selected.length < 1)
		{
			$.tipInfo(po.i18n.pleaseSelectAtLeastOne);
			return;
		}
		
		callback.call(po, selected);
	};
	
	po.handleAddAction = function(url, options)
	{
		var action = { url: url, options: options };
		po.inflateFormActionPageParam(action);
		po.open(action.url, action.options);
	};
	
	po.handleOpenOfAction = function(url, options)
	{
		po.executeOnSelect(function(entity)
		{
			po.doOpenOfAction(url, entity, options);
		});
	};
	
	po.doOpenOfAction = function(url, entity, options)
	{
		var action = { url: url, options: options };
		po.inflateFormActionPageParam(action);
		po.inflateEntityAction(action, entity);
		po.open(action.url, action.options);
	};
	
	po.handleOpenOfsAction = function(url, options)
	{
		po.executeOnSelects(function(entities)
		{
			po.doOpenOfsAction(url, entities, options);
		});
	};
	
	po.doOpenOfsAction = function(url, entities, options)
	{
		var action = { url: url, options: options };
		po.inflateFormActionPageParam(action);
		po.inflateEntityAction(action, entities);
		po.open(action.url, action.options);
	};
	
	po.handleDeleteAction = function(url, options)
	{
		po.executeOnSelects(function(entities)
		{
			po.confirmDelete(function()
			{
				options = $.extend(
				{
					contentType: $.CONTENT_TYPE_JSON,
					success: function(){ po.refresh(); }
				},
				options);
				
				var action = { url: url, options: options };
				po.inflateEntityAction(action, entities);
				
				po.ajaxJson(action.url, action.options);
			});
		});
	};
	
	po.handleSelectAction = function()
	{
		if(po.isMultipleSelect)
		{
			po.executeOnSelects(function(entities)
			{
				po.pageParamCallSelect(entities);
			});
		}
		else
		{
			po.executeOnSelect(function(entity)
			{
				po.pageParamCallSelect(entity);
			});
		}
	};
	
	//调用页面参数对象的"select"函数
	po.pageParamCallSelect = function(selected, close)
	{
		close = (close == null ? true : close);
		
		var myClose = this.pageParamCall("select", selected);
		
		if(myClose === false)
			return;
		
		if(close)
			this.close();
	};
	
	po.inflateFormActionPageParam = function(action)
	{
		action.options = $.extend(
		{
			pageParam:
			{
				submitSuccess: function(response)
				{
					po.refresh();
				}
			}
		},
		action.options);
	};
	
	//将单行或多行数据对象转换为操作请求数据
	po.inflateEntityAction = function(action, entityOrArray)
	{
		var id = $.propertyValue(entityOrArray, po.inflateEntityActionIdPropName);
		
		if($.CONTENT_TYPE_JSON == action.options.contentType)
		{
			var options = action.options;
			if(options.data == null)
				options.data = id;
			else
			{
				var data = {};
				data[po.inflateEntityActionIdParamName] = id;
				options.data = $.extend(data, options.data);
			}
		}
		else
		{
			if($.isArray(id))
			{
				for(var i=0; i<id.length; i++)
					action.url = $.addParam(action.url, po.inflateEntityActionIdParamName, id[i], true);
			}
			else
				action.url = $.addParam(action.url, po.inflateEntityActionIdParamName, id);
		}
	};
	
	po.inflateEntityActionIdPropName = "id";
	po.inflateEntityActionIdParamName = "id";
})
(${pid});
</script>
