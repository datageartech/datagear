<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--页面JS对象块 -->
<script type="text/javascript">
var ${pageId} =
{
	//父页面对象ID
	parentPageId : "${parentPageId}",
	
	//当前页面ID
	pageId : "${pageId}",
	
	//获取父页面JS对象
	parent : function()
	{
		var parentPage = (this.parentPageId ? window[this.parentPageId] : null);
		//父页面DOM元素可能会在回调过程中被删除，这里加一层元素判断
		return (!parentPage || parentPage.element().length == 0 ? null : parentPage);
	},
	
	//获取页面内的元素
	element : function(selector, parent)
	{
		return (selector == null ? $("#${pageId}") : (parent ? $(selector, parent) : $(selector, $("#${pageId}"))));
	},
	
	//获取页面内指定id的元素
	elementOfId: function(id, parent)
	{
		return this.element("#"+id, parent);
	},
	
	//获取页面内指定name的元素
	elementOfName: function(name, parent)
	{
		return this.element("[name='"+name+"']", parent);
	},
	
	//打开URL
	open : function(url, options)
	{
		if(url.charAt(0) == "/")
			url = this.concatContextPath(url);
		
		url = $.addParam(url, "parentPageId", this.pageId);
		$.open(url, (options || {}));
	},
	
	//关闭此页面
	close : function()
	{
		var myDialog = $.getInDialog(this.element());
		
		if(myDialog && myDialog.length > 0)
			$.closeDialog(myDialog);
		else
		{
			//XXX 打开新窗口后不应该自动关闭
			//window.close();
		}
	},
	
	//页面是否在对话框内
	isInDialog: function()
	{
		var myDialog = $.getInDialog(this.element());
		return (myDialog && myDialog.length > 0);
	},
	
	//页面所在的对话框是否钉住
	isDialogPinned: function()
	{
		var myDialog = $.getInDialog(this.element());
		return (myDialog.length < 1 ? false : $.isDialogPinned(myDialog));
	},
	
	/**
	 * 获取页面参数对象。
	 * @param name 可选，页面参数对象属性名
	 */
	pageParam : function(name)
	{
		var ppo = $.pageParam(this.element());
		return (name == null ? ppo : (ppo ? ppo[name] : null));
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
	 * 打开确认对话框。
	 * @param content 显示内容
	 * @param options 参数选项，参考util.js的$.confirm(...)函数说明
	 */
	confirm : function(content, options)
	{
		options = (options || {});
		options = $.extend({}, options, {confirmText : "<@spring.message code='confirm' />", cancelText : "<@spring.message code='cancel' />", title : "<@spring.message code='operationConfirm' />"});
		$.confirm(content, options);
	},
	
	//连接应用根路径
	concatContextPath : function(path)
	{
		return "${contextPath}" + path;
	},
	
	//获取（自动unref）/设置（自动ref）vue的setup的ref
	vueRef: function(name, value)
	{
		if(value === undefined)
			return Vue.unref(this.vueSetup(name));
		else
			this.vueSetup(name, Vue.ref(value));
	},
	
	//获取/设置vue的setup对象
	vueSetup: function(name, value)
	{
		if(value === undefined)
			return this._vueSetup[name];
		else
			this._vueSetup[name] = value;
	},
	
	//获取/设置vue组件
	vueComponent: function(name, value)
	{
		if(value === undefined)
			return this._vueComponents[name];
		else
			this._vueComponents[name] = value;
	},
	
	//vue的setup对象
	_vueSetup: {},
	
	//vue组件
	_vueComponents:
	{
		"p-tabmenu": primevue.tabmenu,
		"p-contextmenu": primevue.contextmenu,
		"p-button": primevue.button,
		"p-datatable": primevue.datatable,
		"p-column": primevue.column,
		"p-inputtext": primevue.inputtext,
		"p-textarea": primevue.textarea,
		"p-card": primevue.card,
		"p-dialog": primevue.dialog
	},
	
	//vue挂载
	vueMount: function()
	{
		const setupObj = this._vueSetup;
		const componentsObj = this._vueComponents;
		
		const app =
		{
			setup() { return setupObj },
			components: componentsObj
		};
		
		Vue.createApp(app).use(primevue.config.default).mount("#"+this.pageId);
	}
};
</script>
