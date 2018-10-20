<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String __parentPage = request.getParameter("__parentpage");
if(__parentPage == null)
	__parentPage = "";
%>
<%--页面JS对象块 --%>
<script type="text/javascript">
var ${pageId} =
{
	//父页面对象ID
	parentPageId : "<%=__parentPage%>",
	
	//当前页面ID
	pageId : "${pageId}",
	
	/**
	 * 获取父页面JS对象。
	 */
	parent : function()
	{
		if(!this.parentPageId)
			return undefined;
		
		var parentPage = window[this.parentPageId];
		
		if(!parentPage)
			return undefined;
		
		//父页面DOM元素可能会在回调过程中被删除，这里加一层元素判断
		if(parentPage.element().length == 0)
			return undefined;
		
		return parentPage;
	},
	
	/**
	 * 获取页面内的指定Jquery元素。
	 *
	 * @param selector 可选，选择器，默认返回当前页面元素
	 */
	element : function(selector)
	{
		if(!selector)
			return $("#${pageId}");
		else
			return $(selector, $("#${pageId}"));
	},
	
	/**
	 * 打开给定URL的页面。
	 * 具体参数说明参考datagear-util.js中$.open。
	 */
	open : function(url, options)
	{
		url = $.addParam(url, "__parentpage", this.pageId);
		
		options = (options || {});
		
		//将新对话框与当前对话框错开显示
		var myDialog = $.getInDialog(this.element());
		if(myDialog && myDialog.length > 0)
		{
			options = $.extend({}, options,
			{
				position: {my: "left top", at: "left top", of: myDialog}
			});
		}
		
		$.open(url, options);
	},
	
	/**
	 * 关闭此页面。
	 */
	close : function()
	{
		var ele = this.element();
		
		if($.isInDialog(ele))
		{
			var $dialog = $.getInDialog(ele);
			$.closeDialog($dialog);
		}
		else
		{
			//XXX 打开新窗口后不应该自动关闭
			//window.close();
		}
	},
	
	/**
	 * 设置页面关闭回调函数。
	 */
	beforeClose : function(callback)
	{
		var ele = this.element();
		
		if($.isInDialog(ele))
		{
			var $dialog = $.getInDialog(ele);
			
			$dialog.on("dialogbeforeclose", function(event, ui)
			{
				callback();
			});
		}
		else
		{
			window.close();
		}
	},
	
	/**
	 * 获取由open方法传递给此页面参数对象。
	 */
	pageParam : function(name)
	{
		var pageParamObj = $.pageParam(this.element());
		
		if(name == undefined)
			return pageParamObj;
		else
		{
			return (pageParamObj ? pageParamObj[name] : undefined);
		}
	},
	
	/**
	 * 打开确认对话框。
	 * @param content 显示内容
	 * @param options 参数选项，参考datagear-util.js的$.confirm(...)函数说明
	 */
	confirm : function(content, options)
	{
		options = (options || {});
		options = $.extend({}, options, {confirmText : "<fmt:message key='confirm' />", cancelText : "<fmt:message key='cancel' />", title : "<fmt:message key='operationConfirm' />"});
		$.confirm(content, options);
	}
};
</script>
