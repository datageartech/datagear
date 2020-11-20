<#--页面JS对象块 -->
<script type="text/javascript">
var ${pageId} =
{
	//父页面对象ID
	parentPageId : "${parentPageId}",
	
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
	 * @param parent 可选，父元素
	 */
	element : function(selector, parent)
	{
		if(!selector)
			return $("#${pageId}");
		else
		{
			if(parent)
				return $(selector, parent);
			else
				return $(selector, $("#${pageId}"));
		}
	},
	
	/**
	 * 打开给定URL的页面。
	 * 具体参数说明参考datagear-util.js中$.open。
	 */
	open : function(url, options)
	{
		url = $.addParam(url, "parentPageId", this.pageId);
		
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
	
	//页面是否在对话框内
	isInDialog: function()
	{
		var myDialog = $.getInDialog(this.element());
		return (myDialog && myDialog.length > 0);
	},
	
	/*页面所在的对话框是否钉住*/
	isDialogPinned: function()
	{
		var myDialog = $.getInDialog(this.element());
		return (myDialog.length < 1 ? false : $.isDialogPinned(myDialog));
	},
	
	/**
	 * 获取此页面参数对象。
	 * @param name 可选，页面参数对象属性名
	 */
	pageParam : function(name)
	{
		var ppo = $.pageParam(this.element());
		
		if(name == undefined)
			return ppo;
		else
		{
			return (ppo ? ppo[name] : undefined);
		}
	},
	
	/**
	 * 调用页面参数对象指定函数。
	 * @param functionName 必选
	 * @param arg,... 可选，函数参数
	 */
	pageParamCall : function(functionName, arg)
	{
		var argArray = (arg == undefined ? undefined : $.makeArray(arguments).slice(1));
		return $.pageParamCall(this.element(), functionName, argArray);
	},
	
	/**
	 * 调用页面参数对象指定函数。
	 * @param functionName 必选
	 * @param argArray 可选，函数参数
	 */
	pageParamApply : function(functionName, argArray)
	{
		return $.pageParamCall(this.element(), functionName, argArray);
	},
	
	/**
	 * 打开确认对话框。
	 * @param content 显示内容
	 * @param options 参数选项，参考datagear-util.js的$.confirm(...)函数说明
	 */
	confirm : function(content, options)
	{
		options = (options || {});
		options = $.extend({}, options, {confirmText : "<@spring.message code='confirm' />", cancelText : "<@spring.message code='cancel' />", title : "<@spring.message code='operationConfirm' />"});
		$.confirm(content, options);
	},
	
	/**
	 * 调用页面参数对象的"select"函数。
	 * @param closeDefault 默认是否关闭
	 * @param arg... 可选，函数参数
	 */
	pageParamCallSelect : function(closeDefault, arg)
	{
		var close = this.pageParamApply("select", $.makeArray(arguments).slice(1));
		if(close !== true && close !== false)
			close = closeDefault;
		
		if(close && !this.isDialogPinned())
			this.close();
		
		return close;
	},
	
	/**
	 * 调用页面参数对象的"afterSave"函数。
	 * @param closeDefault 默认是否关闭
	 * @param arg... 可选，函数参数
	 */
	pageParamCallAfterSave : function(closeDefault, arg)
	{
		if(this.refreshParent)
			this.refreshParent();
		
		var close = this.pageParamApply("afterSave", $.makeArray(arguments).slice(1));
		if(close !== true && close !== false)
			close = closeDefault;
		
		if(close && !this.isDialogPinned())
			this.close();
		
		return close;
	},
	
	/**
	 * 连接contextPath路径。
	 * @param pathNode 可变路径节点，不需要加“/”
	 */
	concatContextPath : function(pathNode)
	{
		var path = "${contextPath}";
		
		for(var i=0; i< arguments.length; i++)
		{
			if(path == "" || path.charAt(path.length - 1) != "/")
				path += "/";
			
			path += encodeURIComponent(arguments[i]);
		}
		
		return path;
	}
};
</script>
