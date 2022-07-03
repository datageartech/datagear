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
		return (selector == null ? $("#"+this.pageId) : (parent ? $(selector, parent) : $(selector, $("#"+this.pageId))));
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
	
	attr: function(name, value)
	{
		var attrs = (this._attrs || (this._attrs = {}));
		
		if(value === undefined)
			return attrs[name];
		else
			attrs[name] = value;
	},
	
	//获取（自动unref）/设置（自动ref）vue的setup的ref值
	vueRef: function(name, value)
	{
		var obj = this.vueSetup(name);
		
		if(value === undefined)
			return Vue.unref(obj);
		else
		{
			if(obj == null)	
				this.vueSetup(name, Vue.ref(value));
			else
				obj.value = value;
		}
	},
	
	//获取（自动toRaw）/设置（自动reactive）vue的setup的reactive值
	vueReactive: function(name, value)
	{
		var obj = this.vueSetup(name);
		
		if(value === undefined)
			return Vue.toRaw(obj);
		else
		{
			this.vueSetup(name, Vue.reactive(value));
		}
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
	
	//设置vue挂在后回调函数
	vueMounted: function(callback)
	{
		this._vueMounted.push(callback);
	},
	
	//vue的setup对象
	_vueSetup: {},
	//vue的mounted回调函数
	_vueMounted: [],
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
	vueMount: function(app)
	{
		const setupObj = this._vueSetup;
		const mountedObj = this._vueMounted;
		const componentsObj = this._vueComponents;
		
		app = $.extend((app || {}),
		{
			setup()
			{
				Vue.onMounted(function()
				{
					mountedObj.forEach(function(callback)
					{
						callback();
					});
				});
				
				return setupObj;
			},
			components: componentsObj
		});
		
		Vue.createApp(app).use(primevue.config.default).mount("#"+this.pageId);
	}
};
</script>
