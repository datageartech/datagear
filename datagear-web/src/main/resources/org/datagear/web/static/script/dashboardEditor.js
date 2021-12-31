/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 看板可视编辑器。
 * 全局变量名：window.dashboardFactory.dashboardEditor
 * 
 * 加载时依赖：
 *   dashboardFactory.js
 * 
 * 运行时依赖:
 *   jquery.js
 *   dashboardFactory.js
 *   chartFactory.js
 */
(function(global)
{
	/**看板工厂*/
	var dashboardFactory = (global.dashboardFactory || (global.dashboardFactory = {}));
	var editor = (dashboardFactory.dashboardEditor || (dashboardFactory.dashboardEditor = {}));
	
	//参考org.datagear.web.controller.DashboardController.DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_EDIT_HTML_INFO
	editor.DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_EDIT_HTML_INFO = "DG_EDIT_HTML_INFO";
	
	dashboardFactory._initSuperByDashboardEditor = dashboardFactory.init;
	dashboardFactory.init = function(dashboard)
	{
		dashboardFactory._initSuperByDashboardEditor(dashboard);
		editor.dashboard = dashboard;
		editor.init();
	};
	
	/**
	 * 初始化可视编辑器。
	 */
	editor.init = function()
	{
		var editHtmlInfo = this._editHtmlInfo();
		
		//只使用bodyHTML，因为渲染ifarme页面时beforeBodyHtml、afterBodyHtml如果有不合规的元素，
		//可能会被渲染至<body></body>内，导致【结果HTML】还原不匹配
		var editHtml = "<html><head></head>" + editHtmlInfo.bodyHtml + "</html>";
		this._editHtmlIframe(editHtml);
	};
	
	editor.insertChart = function(chartWidgets)
	{
		if(!chartWidgets || chartWidgets.length == 0)
			return;
		
		if(!$.isArray(chartWidgets))
			chartWidgets = [ chartWidgets ];
		
		var iframeDoc = this._iframeDocument();
		
		for(var i=0; i<chartWidgets.length; i++)
		{
			var chartWidget = chartWidgets[i];
			
			$("<div class='dg-chart' />").attr(chartFactory.elementAttrConst.WIDGET, chartWidget.id).appendTo(document.body);
			$("<div class='dg-chart' />").attr(chartFactory.elementAttrConst.WIDGET, chartWidget.id).appendTo(iframeDoc.body);
		}
		
		this.dashboard.loadUnsolvedCharts();
	};
	
	//获取当前编辑HTML
	editor.editedHtml = function()
	{
		var editHtmlInfo = this._editHtmlInfo();
		var editedHtml = editHtmlInfo.beforeBodyHtml + this._editBodyHtml() + editHtmlInfo.afterBodyHtml;
		
		var placeholderSources = (editHtmlInfo.placeholderSources || {});
		for(var placeholder in placeholderSources)
		{
			var source = placeholderSources[placeholder];
			editedHtml = editedHtml.replace(placeholder, source);
		}
		
		return this._unescapeEditHtml(editedHtml);
	};
	
	//获取编辑HTML的<body>...</body>内容
	editor._editBodyHtml = function()
	{
		var iframeDoc = this._iframeDocument();
		return $(iframeDoc.body).prop("outerHTML");
	};
	
	//获取编辑HTML信息
	//结构参考：org.datagear.web.controller.DashboardController.DashboardShowForEdit.EditHtmlInfo
	editor._editHtmlInfo = function()
	{
		return this.dashboard.renderContextAttr(editor.DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_EDIT_HTML_INFO);
	};
	
	//反转义编辑HTML（转义操作由后台执行）
	editor._unescapeEditHtml = function(editHtml)
	{
		return (editHtml ? editHtml.replaceAll("<\\/", "</") : editHtml);
	};
	
	/**
	 * 获取编辑HTML的iframe对象，也可设置其编辑HTML。
	 */
	editor._editHtmlIframe = function(editHtml)
	{
		var id = (this._editHtmlIframeId != null ? this._editHtmlIframeId
					: (this._editHtmlIframeId = chartFactory.nextElementId()));
		
		var iframe = $("#" + id);
		
		if(iframe.length == 0)
		{
			iframe = $("<iframe class='dg-edit-html-ifm' style='display:none;' />")
				.attr("name", id).attr("id", id).appendTo(document.body);
		}
		
		iframe = iframe[0];
		
		if(editHtml != null)
		{
			editHtml = this._unescapeEditHtml(editHtml);
			
			var iframeDoc = this._iframeDocument(iframe);
			iframeDoc.write(editHtml);
		}
		
		return iframe;
	};
	
	/**
	 * 获取iframe的document对象。
	 */
	editor._iframeDocument = function(iframe)
	{
		iframe = (iframe == null ? this._editHtmlIframe() : iframe);
		return (iframe.contentDocument || iframe.contentWindow.document);
	};
	
	editor._evalTopWindowSize = function()
	{
		var topWindow = window;
		while(topWindow.parent  && topWindow.parent != topWindow)
			topWindow = topWindow.parent;
		
		var size =
		{
			width: $(topWindow).width(),
			height: $(topWindow).height()
		};
		
		return size;
	};
	
})
(this);