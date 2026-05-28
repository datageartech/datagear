/*
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
 */

/**
 * 页面JS对象函数集。
 * 
 * 依赖:
 * jquery.js
 */

(function($, undefined)
{

$.vueComponents = function()
{
	var components =
	{
		"p-tabmenu": primevue.tabmenu,
		"p-button": primevue.button,
		"p-datatable": primevue.datatable,
		"p-column": primevue.column,
		"p-inputtext": primevue.inputtext,
		"p-inputnumber": primevue.inputnumber,
		"p-inputswitch": primevue.inputswitch,
		"p-checkbox": primevue.checkbox,
		"p-textarea": primevue.textarea,
		"p-card": primevue.card,
		"p-dialog": primevue.dialog,
		"p-password": primevue.password,
		"p-divider": primevue.divider,
		"p-selectbutton": primevue.selectbutton,
		"p-dropdown": primevue.dropdown,
		"p-togglebutton": primevue.togglebutton,
		"p-radiobutton": primevue.radiobutton,
		"p-splitbutton": primevue.splitbutton,
		"p-tree": primevue.tree,
		"p-tabview": primevue.tabview,
		"p-tabpanel": primevue.tabpanel,
		"p-menu": primevue.menu,
		"p-menubar": primevue.menubar,
		"p-tieredmenu": primevue.tieredmenu,
		"p-chip": primevue.chip,
		"p-fileupload": primevue.fileupload,
		"p-inlinemessage": primevue.inlinemessage,
		"p-steps": primevue.steps,
		"p-dataview": primevue.dataview,
		"p-overlaypanel": primevue.overlaypanel,
		"p-panel": primevue.panel,
		"p-fieldset": primevue.fieldset,
		"p-listbox": primevue.listbox,
		"p-colorpicker": primevue.colorpicker,
		"p-splitter": primevue.splitter,
        "p-splitterpanel": primevue.splitterpanel,
        "p-progressbar": primevue.progressbar,
		"p-multiselect": primevue.multiselect,
		"p-treeselect": primevue.treeselect,
		"p-breadcrumb": primevue.breadcrumb,
		"p-badge": primevue.badge,
		"p-accordion": primevue.accordion,
		"p-accordion-tab": primevue.accordiontab,
		"p-treetable": primevue.treetable,
		"p-tag": primevue.tag
	};
	
	return components;
};

$.paletteColors =
[
	/* PrimeFlex颜色表 */
	/*
	{
		group: "bluegray",
		colors: [ "#f8f9fb", "#e0e4ea", "#c7ced9" , "#aeb9c8" , "#95a3b8" , "#7c8ea7" , "#69798e" , "#576375" , "#444e5c" , "#323943" ]
	},
	{
		group: "gray",
		colors: [ "#f9fafb", "#f8f9fa", "#e9ecef" , "#dee2e6" , "#ced4da" , "#adb5bd" , "#6c757d" , "#495057" , "#343a40" , "#212529" ]
	},
	{
		group: "red",
		colors: [ "#fdf5f6", "#f7cfd2", "#f0a8af" , "#e9828c" , "#e35b68" , "#dc3545" , "#bb2d3b" , "#9a2530" , "#791d26" , "#58151c" ]
	},
	{
		group: "orange",
		colors: [ "#fff9f3", "#ffe0c7", "#fec89a" , "#feaf6d" , "#fd9741" , "#fd7e14" , "#d76b11" , "#b1580e" , "#8b450b" , "#653208" ]
	},
	{
		group: "yellow",
		colors: [ "#fffcf3", "#fff0c3", "#ffe494" , "#ffd965" , "#ffcd36" , "#ffc107" , "#d9a406" , "#b38705" , "#8c6a04" , "#664d03" ]
	},
	{
		group: "green",
		colors: [ "#f4f9f6", "#c8e2d6", "#9ccbb5" , "#70b595" , "#459e74" , "#198754" , "#157347" , "#125f3b" , "#0e4a2e" , "#0a3622" ]
	},
	{
		group: "teal",
		colors: [ "#f4fcfa", "#c9f2e6", "#9fe8d2" , "#75debf" , "#4ad3ab" , "#20c997" , "#1bab80" , "#168d6a" , "#126f53" , "#0d503c" ]
	},
	{
		group: "cyan",
		colors: [ "#f3fcfe", "#c5f2fb", "#97e8f9" , "#69def6" , "#3bd4f3" , "#0dcaf0" , "#0baccc" , "#098da8" , "#076f84" , "#055160" ]
	},
	{
		group: "primary",
		colors: [ "#e6f6ff", "#ccedff", "#99daff" , "#66c8ff" , "#33b5ff" , "#00a3ff" , "#0082cc" , "#006299" , "#004166" , "#002133" ]
	},
	{
		group: "blue",
		colors: [ "#f3f8ff", "#c5dcff", "#97c1fe" , "#69a5fe" , "#3b8afd" , "#0d6efd" , "#0b5ed7" , "#094db1" , "#073d8b" , "#052c65" ]
	},
	{
		group: "indigo",
		colors: [ "#f7f3fe", "#dac6fc", "#bd98f9" , "#a06bf7" , "#833df4" , "#6610f2" , "#570ece" , "#470ba9" , "#380985" , "#290661" ]
	},
	{
		group: "purple",
		colors: [ "#f8f6fc", "#dcd2f0", "#c1aee4" , "#a68ad9" , "#8a66cd" , "#6f42c1" , "#5e38a4" , "#4e2e87" , "#3d246a" , "#2c1a4d" ]
	},
	{
		group: "pink",
		colors: [ "#fdf5f9", "#f5cee1", "#eda7ca" , "#e681b3" , "#de5a9b" , "#d63384" , "#b62b70" , "#96245c" , "#761c49" , "#561435" ]
	}
	*/
	/* tailwindcss颜色表 */
	{
		group: "Slate",
		colors:[ "#f8fafc", "#f1f5f9", "#e2e8f0", "#cbd5e1", "#94a3b8", "#64748b", "#475569", "#334155", "#1e293b", "#0f172a", "#020617" ]
	},
	{
		group: "Gray",
		colors:[ "#f9fafb", "#f3f4f6", "#e5e7eb", "#d1d5db", "#9ca3af", "#6b7280", "#4b5563", "#374151", "#1f2937", "#111827", "#030712" ]
	},
	/*这俩颜色与Stone相近，无需启用
	{
		group: "Zinc",
		colors:[ "#fafafa", "#f4f4f5", "#e4e4e7", "#d4d4d8", "#a1a1aa", "#71717a", "#52525b", "#3f3f46", "#27272a", "#18181b", "#09090b" ]
	},
	{
		group: "Neutral",
		colors:[ "#fafafa", "#f5f5f5", "#e5e5e5", "#d4d4d4", "#a3a3a3", "#737373", "#525252", "#404040", "#262626", "#171717", "#0a0a0a" ]
	},
	*/
	{
		group: "Stone",
		colors:[ "#fafaf9", "#f5f5f4", "#e7e5e4", "#d6d3d1", "#a8a29e", "#78716c", "#57534e", "#44403c", "#292524", "#1c1917", "#0c0a09" ]
	},
	{
		group: "Red",
		colors:[ "#fef2f2", "#fee2e2", "#fecaca", "#fca5a5", "#f87171", "#ef4444", "#dc2626", "#b91c1c", "#991b1b", "#7f1d1d", "#450a0a" ]
	},
	{
		group: "Orange",
		colors:[ "#fff7ed", "#ffedd5", "#fed7aa", "#fdba74", "#fb923c", "#f97316", "#ea580c", "#c2410c", "#9a3412", "#7c2d12", "#431407" ]
	},
	{
		group: "Amber",
		colors:[ "#fffbeb", "#fef3c7", "#fde68a", "#fcd34d", "#fbbf24", "#f59e0b", "#d97706", "#b45309", "#92400e", "#78350f", "#451a03" ]
	},
	{
		group: "Yellow",
		colors:[ "#fefce8", "#fef9c3", "#fef08a", "#fde047", "#facc15", "#eab308", "#ca8a04", "#a16207", "#854d0e", "#713f12", "#422006" ]
	},
	{
		group: "Lime",
		colors:[ "#f7fee7", "#ecfccb", "#d9f99d", "#bef264", "#a3e635", "#84cc16", "#65a30d", "#4d7c0f", "#3f6212", "#365314", "#1a2e05" ]
	},
	{
		group: "Green",
		colors:[ "#f0fdf4", "#dcfce7", "#bbf7d0", "#86efac", "#4ade80", "#22c55e", "#16a34a", "#15803d", "#166534", "#14532d", "#052e16" ]
	},
	{
		group: "Emerald",
		colors:[ "#ecfdf5", "#d1fae5", "#a7f3d0", "#6ee7b7", "#34d399", "#10b981", "#059669", "#047857", "#065f46", "#064e3b", "#022c22" ]
	},
	{
		group: "Teal",
		colors:[ "#f0fdfa", "#ccfbf1", "#99f6e4", "#5eead4", "#2dd4bf", "#14b8a6", "#0d9488", "#0f766e", "#115e59", "#134e4a", "#042f2e" ]
	},
	{
		group: "Cyan",
		colors:[ "#ecfeff", "#cffafe", "#a5f3fc", "#67e8f9", "#22d3ee", "#06b6d4", "#0891b2", "#0e7490", "#155e75", "#164e63", "#083344" ]
	},
	{
		group: "Sky",
		colors:[ "#f0f9ff", "#e0f2fe", "#bae6fd", "#7dd3fc", "#38bdf8", "#0ea5e9", "#0284c7", "#0369a1", "#075985", "#0c4a6e", "#082f49" ]
	},
	{
		group: "Blue",
		colors:[ "#eff6ff", "#dbeafe", "#bfdbfe", "#93c5fd", "#60a5fa", "#3b82f6", "#2563eb", "#1d4ed8", "#1e40af", "#1e3a8a", "#172554" ]
	},
	{
		group: "Indigo",
		colors:[ "#eef2ff", "#e0e7ff", "#c7d2fe", "#a5b4fc", "#818cf8", "#6366f1", "#4f46e5", "#4338ca", "#3730a3", "#312e81", "#1e1b4b" ]
	},
	{
		group: "Violet",
		colors:[ "#f5f3ff", "#ede9fe", "#ddd6fe", "#c4b5fd", "#a78bfa", "#8b5cf6", "#7c3aed", "#6d28d9", "#5b21b6", "#4c1d95", "#2e1065" ]
	},
	{
		group: "Purple",
		colors:[ "#faf5ff", "#f3e8ff", "#e9d5ff", "#d8b4fe", "#c084fc", "#a855f7", "#9333ea", "#7e22ce", "#6b21a8", "#581c87", "#3b0764" ]
	},
	{
		group: "Fuchsia",
		colors:[ "#fdf4ff", "#fae8ff", "#f5d0fe", "#f0abfc", "#e879f9", "#d946ef", "#c026d3", "#a21caf", "#86198f", "#701a75", "#4a044e" ]
	},
	{
		group: "Pink",
		colors:[ "#fdf2f8", "#fce7f3", "#fbcfe8", "#f9a8d4", "#f472b6", "#ec4899", "#db2777", "#be185d", "#9d174d", "#831843", "#500724" ]
	},
	{
		group: "Rose",
		colors:[ "#fff1f2", "#ffe4e6", "#fecdd3", "#fda4af", "#fb7185", "#f43f5e", "#e11d48", "#be123c", "#9f1239", "#881337", "#4c0519" ]
	}
];

//填充page_obj.ftl里JS对象的静态逻辑
$.inflatePageObj = function(po)
{
	//获取父页面JS对象
	po.parent = function()
	{
		var parentPage = (this.ppid ? window[this.ppid] : null);
		//父页面DOM元素可能会在回调过程中被删除，这里加一层元素判断
		return (!parentPage || parentPage.element().length == 0 ? null : parentPage);
	};
	
	//获取页面内的元素
	po.element = function(selector, parent)
	{
		return (selector == null ? $("#"+this.pid) : (parent ? $(selector, parent) : $(selector, $("#"+this.pid))));
	};
	
	//获取页面内指定id的元素
	po.elementOfId = function(id, parent)
	{
		return this.element("#"+id, parent);
	};
	
	//获取页面内以页面ID为前缀的元素
	po.elementOfPidPrefix = function(idSuffix, parent)
	{
		return this.element("#"+po.concatPid(idSuffix), parent);
	};
	
	po.concatPid = function(suffix)
	{
		return (this.pid + suffix);
	};
	
	//获取页面内指定name的元素
	po.elementOfName = function(name, parent)
	{
		return this.element("[name='"+name+"']", parent);
	};
	
	//打开URL
	po.open = function(url, options)
	{
		options = $.extend({ fullUrl: false }, (options || {}));
		
		if(options.fullUrl !== true)
			url = this.concatContextPath(url);
		
		if(options.target !== '_blank' || (options.target === '_blank' && options.addParentPid === true))
			url = $.addParam(url, this.ppidParamName, this.pid);
		
		$.open(url, (options || {}));
	};
	
	//打开表格对话框
	po.openTableDialog = function(url, options)
	{
		options = $.extend({ width: "80vw" }, options);
		this.open(url, options);
	};
	
	//关闭此页面
	po.close = function()
	{
		$.closeDialog(this.element());
	};
	
	po.beforeClose = function(name, callback)
	{
		$.bindBeforeCloseDialogCallback(this.element(), name, callback);
	};
	
	po.getJson = function(url, data, success)
	{
		var args = $.makeArray(arguments);
		args[0] = this.concatContextPath(url);
		$.getJSON.apply($, args);
	};
	
	po.post = function(url, data, success)
	{
		var args = $.makeArray(arguments);
		args[0] = this.concatContextPath(url);
		$.post.apply($, args);
	};
	
	po.ajaxJson = function(url, options)
	{
		url = this.concatContextPath(url);
		$.ajaxJson(url, options);
	};
	
	po.ajax = function(url, options)
	{
		url = this.concatContextPath(url);
		options = (options || {});
		
		if(options.data && !options.type)
			options = $.extend({ type: "POST" }, options);
		
		$.ajax(url, options);
	};
	
	//页面是否在对话框内
	po.isInDialog = function()
	{
		return $.isInDialog(this.element());
	};
	
	/**
	 * 获取页面参数对象。
	 * @param name 可选，页面参数对象属性名
	 */
	po.pageParam = function(name)
	{
		var ppo = $.pageParam(this.element());
		return (name == null ? ppo : (ppo ? ppo[name] : null));
	};
	
	/**
	 * 调用页面参数对象指定函数。
	 * @param functionName 必选
	 * @param arg,... 可选，函数参数
	 */
	po.pageParamCall = function(functionName, arg)
	{
		var argArray = (arg == undefined ? undefined : $.makeArray(arguments).slice(1));
		return $.pageParamCall(this.element(), functionName, argArray);
	};
	
	//打开确认对话框
	po.confirm = function(options)
	{
		options = $.extend(
		{
			acceptLabel : this.i18n.confirm,
			rejectLabel : this.i18n.cancel,
			header : this.i18n.operationConfirm
		},
		options);
		
		$.confirm(options);
	};
	
	//删除操作确认
	po.confirmDelete = function(acceptHandler, rejectHandler)
	{
		var msg = this.i18n.confirmDelSelectedAsk;
		this.confirm({ message: msg, accept: acceptHandler, reject: rejectHandler });
	};
	
	//连接应用根路径
	po.concatContextPath = function(path)
	{
		return (path.charAt(0) == "/" ? this.contextPath + path : path);
	};
	
	po.attr = function(name, value)
	{
		var attrs = (this._attrs || (this._attrs = {}));
		
		if(value === undefined)
			return attrs[name];
		else
			attrs[name] = value;
	};
	
	//获取/填充并返回vue页面模型，在vue页面中可以"pm.*"访问模型中的属性
	po.vuePageModel = function(obj)
	{
		return this.vueReactive("pm", obj);
	};
	
	//获取/填充并返回vue的setup响应式对象（自动reactive），对象格式必须为：{...}
	po.vueReactive = function(name, obj)
	{
		if(obj === undefined)
			return this._vueSetup[name];
		else
		{
			var rtvObj = (this._vueSetup[name] || (this._vueSetup[name] = Vue.reactive({})));
			
			for(var p in obj)
				rtvObj[p] = obj[p];
			
			return rtvObj;
		}
	};
	
	//获取/设置（自动ref）vue的setup引用值
	po.vueRef = function(name, value)
	{
		var obj = this._vueSetup[name];
		
		if(value === undefined)
			return obj;
		else
		{
			if(obj == null)	
				this._vueSetup[name] = Vue.ref(value);
			else
				obj.value = value;
		}
	};
	
	//设置vue的setup函数
	po.vueMethod = function(name, method)
	{
		var methodsObj = {};
		
		// ({ a: Function, b: Function)
		if(arguments.length == 1)
			methodsObj = name;
		// (name, Function)
		else if(arguments.length == 2)
			methodsObj[name] = method;
		
		for(var p in methodsObj)
			this._vueSetup[p] = methodsObj[p];
	};
	
	//设置vue的计算属性
	po.vueComputed = function(name, handler)
	{
		var computedObj = {};
		
		// ({ a: Function, b: Function)
		if(arguments.length == 1)
			computedObj = name;
		// (name, Function)
		else if(arguments.length == 2)
			computedObj[name] = handler;
		
		for(var p in computedObj)
			this._vueComputed[p] = computedObj[p];
	};
	
	//获取/设置vue组件
	po.vueComponent = function(name, value)
	{
		if(value === undefined)
			return this._vueComponents[name];
		else
			this._vueComponents[name] = value;
	};
	
	//自定义Vue组件
	po.vueDefineComponent = function(component)
	{
		var name = component.name;
		var cmp = Vue.defineComponent(component);
		this.vueComponent(name, cmp);
		
		return cmp;
	};
	
	//设置vue监听
	po.vueWatch = function(target, callback)
	{
		this._vueWatch.push({ target: target, callback: callback });
	};
	
	//设置vue挂在后回调函数
	po.vueMounted = function(callback)
	{
		this._vueMounted.push(callback);
	};

	//获取指定名称对象的unref()结果
	po.vueUnref = function(name)
	{
		var obj = this._vueSetup[name];
		return Vue.unref(obj);
	};
	
	//获取toRaw()结果对象
	po.vueRaw = function(reactiveObj)
	{
		if($.isArray(reactiveObj))
		{
			var re = [];
			$.each(reactiveObj, function(idx, item)
			{
				re.push(Vue.toRaw(item));
			});
			
			return re;
		}
		else
			return Vue.toRaw(reactiveObj);
	};
	
	//执行vue的$nextTick操作
	po.vueNextTick = function(callback)
	{
		po.vueApp().$nextTick(callback);
	};
	
	//vue的setup对象
	po._vueSetup = {};
	//vue的watch对象
	po._vueWatch = [];
	//vue的watch对象
	po._vueComputed = {};
	//vue的mounted回调函数
	po._vueMounted = [];
	//vue组件
	po._vueComponents = $.vueComponents();
	
	//vue挂载
	po.vueMount = function(app)
	{
		const setupObj = this._vueSetup;
		const watchObj = this._vueWatch;
		const computedObj = this._vueComputed;
		const mountedObj = this._vueMounted;
		const componentsObj = this._vueComponents;
		
		app = $.extend((app || {}),
		{
			setup()
			{
				$.each(watchObj, function(idx, wt)
				{
					Vue.watch(wt.target, wt.callback);
				});
				
				for(var cpn in computedObj)
				{
					setupObj[cpn] = Vue.computed(computedObj[cpn]);
				}
				
				return setupObj;
			},
			mounted()
			{
				po._vueApp = this;
				
				$.each(mountedObj, function(idx, callback)
				{
					callback();
				});
				
				$.initGlobalTip();
				$.initGlobalConfirm();
			},
			components: componentsObj
		});
		
		var vueApp = Vue.createApp(app).use(primevue.config.default).directive("tooltip", primevue.tooltip).mount("#"+this.pid);
		return vueApp;
	};
	
	//获取挂载后的vue实例
	po.vueApp = function()
	{
		return this._vueApp;
	};
};

//填充page_manager.ftl里JS对象的静态逻辑
$.inflatePageManager = function(po)
{
	po.refresh = function(){ /*需实现*/ };
	po.getSelectedEntities = function(){ /*需实现*/ };
	
	po.setupAction = function()
	{
		po.vuePageModel(
		{
			action: po.action,
			isManageAction: po.isManageAction,
			isSelectAction: po.isSelectAction,
			isMultipleSelect: po.isMultipleSelect,
			isReadonlyAction: po.isReadonlyAction
		});
	};
	
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
		
		var myClose = po.pageParamCall("select", selected);
		
		if(myClose === false)
			return;
		
		if(close)
			po.close();
	};
	
	po.inflateFormActionPageParam = function(action)
	{
		action.options = $.extend(
		{
			pageParam:
			{
				submitSuccess: function()
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
		
		if(action.options && $.CONTENT_TYPE_JSON == action.options.contentType)
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
			{
				if(action.options && action.options.appendIdToPath)
				{
					var idPathNode = (action.url.charAt(action.url.length - 1) == '/' ? "" : "/") + encodeURIComponent(id);
					action.url += idPathNode;
				}
				else
					action.url = $.addParam(action.url, po.inflateEntityActionIdParamName, id);
			}
		}
	};
	
	po.inflateEntityActionIdPropName = "id";
	po.inflateEntityActionIdParamName = "id";
};

//填充page_table.ftl里JS对象的静态逻辑
$.inflatePageTable = function(po)
{
	//获取页面表格组件元素
	po.tableElement = function()
	{
		return po.element("p-datatable");
	};
	
	//重写搜索表单提交处理函数
	po.search = function(formData, resetPage)
	{
		resetPage = (resetPage == null ? po.searchResetPage : resetPage);
		
		if(resetPage)
			formData = $.extend({ page: 1 }, formData);
		else
			formData = $.extend({}, formData);
		
		//每次应重置
		if(!po.searchResetPage)
			po.searchResetPage = true;
		
		po.ajaxTableQuery(formData);
		po.loadAjaxTable();
	};
	
	po.searchResetPage = true;
	
	po.refresh = function()
	{
		//兼容搜索表单集成
		if(po.submitSearchForm)
		{
			po.searchResetPage = false;
			po.submitSearchForm();
		}
		else
			po.loadAjaxTable();
	};
	
	po.getSelectedEntities = function()
	{
		var pm = po.vuePageModel();
		return $.wrapAsArray(po.vueRaw(pm.selectedItems));
	};
	
	po.rowsPerPageOptions = [10, 20, 50, 100, 200];
	po.rowsPerPage = po.rowsPerPageOptions[1];
	
	po.ajaxTableAttr = function(obj)
	{
		return po.attr("ajaxTableAttr", obj);
	};
	
	po.setupAjaxTable = function(url, options)
	{
		options = $.extend({ multiSortMeta: [], initData: true }, options);
		
		po.setupAction();
		var selectionMode = (po.isManageAction || po.isMultipleSelect ? "multiple" : "single");
		
		//统一设置表格特性
		var tableEle = po.tableElement();
		tableEle.attr(":meta-key-selection", "pm.metaKeySelection");
		
		var pm = po.vuePageModel(
		{
			items: [],
			paginator: true,
			pageRecordIndex: 0,
			paginatorTemplate: "CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown",
			pageReportTemplate: "{first}-{last} / {totalRecords}",
			rowsPerPage: po.rowsPerPage,
			rowsPerPageOptions: po.rowsPerPageOptions,
			totalRecords: 0,
			loading: false,
			selectionMode: selectionMode,
			metaKeySelection: (selectionMode == "multiple"),
			multiSortMeta: options.multiSortMeta,
			selectedItems: null
		});
		
		po.vueMethod(
		{
			onPaginator: function(e)
			{
				var query = { page: e.page+1, pageSize: e.rows };
				$.extend(query, po.buildQueryForOrder(e.multiSortMeta));
				
				po.ajaxTableQuery(query);
				po.loadAjaxTable();
			},
			onSort: function(e)
			{
				po.ajaxTableQuery(po.buildQueryForOrder(e.multiSortMeta));
				po.loadAjaxTable();
			}
		});
		
		var query = { page: 1, pageSize: po.rowsPerPage };
		$.extend(query, po.buildQueryForOrder(options.multiSortMeta));
		
		po.ajaxTableAttr(
		{
			url: url,
			query: query
		});
		
		if(options.initData)
		{
			po.vueMounted(function()
			{
				po.refresh();
			});
		}
		
		return pm;
	};
	
	po.isMultipleQueryOrder = function(){ return true; };
	
	po.buildQueryForOrder = function(multiSortMeta)
	{
		var qo = {};
		
		if(po.isMultipleQueryOrder())
		{
			qo.orders = po.sortMetaToOrders(multiSortMeta);
		}
		else
		{
			var orders = po.sortMetaToOrders(multiSortMeta);
			qo.order = (orders ? orders[0] : undefined);
		}
		
		return qo;
	};
	
	po.ajaxTableQuery = function(query)
	{
		var ajaxTableAttr = po.ajaxTableAttr();
		
		if(query === undefined)
			return ajaxTableAttr.query;
		else
			$.extend(ajaxTableAttr.query, query);
	};
	
	po.loadAjaxTable = function(options)
	{
		options = (options || {});
		
		var ajaxTableAttr = po.ajaxTableAttr();
		var pm = po.vuePageModel();
		pm.loading = true;
		
		options = $.extend(
		{
			data: ajaxTableAttr.query,
			success: function(response)
			{
				po.setAjaxTableData(response);
			},
			complete: function()
			{
				pm.loading = false;
			}
		},
		options);
		
		po.ajaxJson(ajaxTableAttr.url, options);
	};
	
	po.sortMetaToOrders = function(sortMeta)
	{
		if(sortMeta == null)
		{
			var pm = po.vuePageModel();
			sortMeta = pm.multiSortMeta;
		}
		
		var orders = [];
		
		$.each(sortMeta, function(idx, sm)
		{
			orders.push({ name: sm.field, type: (sm.order > 0 ? "ASC" : "DESC") });
		});
		
		return orders;
	};
	
	po.setAjaxTableData = function(data)
	{
		var isPagingData = (data.items != null && data.total != null);
		var pm = po.vuePageModel();
		
		pm.items = (isPagingData ? data.items : data);
		pm.totalRecords = (isPagingData ? data.total : data.length);
		pm.pageRecordIndex = (isPagingData ? data.startIndex : 0);
		pm.selectedItems = null;
	};
};

//填充page_form.ftl里JS对象的静态逻辑
$.inflatePageForm = function(po)
{
	//获取/填充并返回vue表单模型，在vue页面中可以"fm.*"访问模型中的属性
	po.vueFormModel = function(obj)
	{
		return po.vueReactive("fm", obj);
	};
	
	po.setupForm = function(data, ajaxOptions, validateOptions)
	{
		data = (data || {});
		ajaxOptions = (ajaxOptions || {});
		validateOptions = (validateOptions || {});
		
		po.vuePageModel(
		{
			action: po.action,
			isAddAction: po.isAddAction,
			isEditAction: po.isEditAction,
			isViewAction: po.isViewAction,
			isCopyAction: po.isCopyAction,
			isReadonlyAction: po.isReadonlyAction
		});
		
		var fm = po.vueFormModel(data);
		
		po.vueMounted(function()
		{
			po.initValidationMessagesIfNon();
			
			//当需要在options中返回DOM元素时，应定义为函数，因为vue挂载前元素可能不必配
			if($.isFunction(ajaxOptions))
				ajaxOptions = ajaxOptions();
			if($.isFunction(validateOptions))
				validateOptions = validateOptions();
			
			validateOptions = $.extend(
			{
				submitHandler: function(form)
				{
					var submitUrl = ($.isFunction(po.submitUrl) ? po.submitUrl() : po.submitUrl);
					return po.submitForm(submitUrl, ajaxOptions);
				}
			},
			validateOptions);
			
			po.form().validateForm(fm, validateOptions);
		});
		
		return fm;
	};
	
	po.submitForm = function(url, options)
	{
		options = $.extend(
		{
			defaultSuccessCallback: true,
			closeAfterSubmit: true,
			ignoreIfViewAction: true
		},
		options);
		
		if(options.ignoreIfViewAction && (po.isViewAction || url == "#"))
			return;
		
		options = $.extend(true, options, { data: po.prepareSubmitData() });
		
		var successHandlers = (options.success ? [].concat(options.success) : []);
		successHandlers.push(function(response)
		{
			if(options.defaultSuccessCallback && po.defaultSubmitSuccessCallback)
				po.defaultSubmitSuccessCallback(response, options.closeAfterSubmit);
		});
		options.success = successHandlers;
		
		var action = { url: url, options: options };
		
		if(po.beforeSubmitForm(action) !== false)
		{
			var jsonSubmit = (action.options.contentType == null || action.options.contentType == $.CONTENT_TYPE_JSON);
			
			if(jsonSubmit)
				po.ajaxJson(action.url, action.options);
			else
				po.ajax(action.url, action.options);
		}
		
		return false;
	};
	
	po.prepareSubmitData = function()
	{
		var fm = po.vueFormModel();
		//默认深度复制，确保后续修改不会影响表单效果
		var data = $.extend(true, {}, po.vueRaw(fm));
		return data;
	};
	
	//返回false会阻止表单提交
	po.beforeSubmitForm = function(action){};
	
	po.defaultSubmitSuccessCallback = function(response, close)
	{
		close = (close == null ? true : close);
		
		var myClose = po.pageParamCallSubmitSuccess(response);
		
		if(myClose === false)
			return;
		
		if(close)
			po.close();
	};
	
	po.pageParamCallSubmitSuccess = function(response)
	{
		po.pageParamCall("submitSuccess", (response.data ? response.data : response));
	};
	
	po.handleOpenSelectAction = function(url, callback, options)
	{
		options = $.extend(
		{
			width: "55vw",
			position: "right",
			closeOnEscape: true
		},
		(options || {}));
		
		options = $.extend(
		{
			pageParam:
			{
				select: callback
			}
		},
		options);
		
		po.openTableDialog(url, options);
	};
};

//填充page_code_editor.ftl里JS对象的静态逻辑
$.inflatePageCodeEditor = function(po)
{
	//停止输入这些毫秒数后才进行提示，避免干扰用户输入
	po.codeEditorHintingDelay = 500;
	
	po.createCodeEditor = function(dom, options)
	{
		dom = $(dom);
		options = (options || {});
		
		dom.on("keydown,keypress,keyup", function(e)
		{
			//阻止TAB键切换焦点
			if(e.keyCode == $.keyCode.TAB)
				e.stopPropagation();
		});
		
		//采用系统切换主题功能模式
		options.theme = "custom";
		
		if(options.lineNumbers == null)
			options.lineNumbers = true;
		
		if(options.smartIndent == null)
			options.smartIndent = false;
		
		if(options.indentWithTabs == null)
			options.indentWithTabs = true;
		
		//强制禁用completeSingle选项，因为如果编辑器hint使用change事件中触发的话，
		//如果这里为true，可能会导致hint死循环，且会导致退格操作无效
		if(options.hintOptions)
			options.hintOptions.completeSingle = false;
		
		//if(options.hintOptions)
		//	options.hintOptions.closeOnUnfocus = false;
		
		var codeEditor = CodeMirror(dom[0], options);
		
		if(options.hintOptions && !options.readOnly)
		{
			codeEditor.on("keyup", function(codeEditor, e)
			{
				if(e.keyCode == $.keyCode.ESCAPE || e.keyCode == $.keyCode.UP
						|| e.keyCode == $.keyCode.DOWN)
				{
					return;
				}
				
				if(codeEditor._timeoutIdForHinting != null)
					clearTimeout(codeEditor._timeoutIdForHinting);
				
				codeEditor._timeoutIdForHinting = setTimeout(function()
				{
					codeEditor.showHint();
					codeEditor._timeoutIdForHinting = null;
				},
				po.codeEditorHintingDelay);
			});
		}
		
		return codeEditor;
	};
	
	po.evalCodeModeByName = function(name)
	{
		var mode = undefined;
		
		if($.isHtmlFile(name))
			mode = "htmlmixed";
		else if($.isJsFile(name))
			mode = "javascript";
		else if($.isCssFile(name))
			mode = "css";
		
		return mode;
	};
	
	po.getCodeText = function(codeEditor)
	{
		var doc = codeEditor.getDoc();
		return doc.getValue();
	};

	po.setCodeText = function(codeEditor, text)
	{
		var doc = codeEditor.getDoc();
		doc.setValue(text || "");
	};
	
	po.setCodeTextTimeout = function(codeEditor, text, focus)
	{
		focus = (focus == null ? false : focus);
		
		//在对话框时，直接初始化代码编辑器会出现行号错位的情况，使用这种方式可解决
		setTimeout(function()
		{
			po.setCodeText(codeEditor, text);
			if(focus)
				codeEditor.focus();
		},
		200);
	};
	
	po.getSelectedCodeText = function(codeEditor)
	{
		var doc = codeEditor.getDoc();
		return (doc.getSelection() || "");
	};
	
	po.getSelectedCodeInfo = function(codeEditor)
	{
		var doc = codeEditor.getDoc();
		var selCodes = doc.getSelections();
		var selRanges = doc.listSelections();
		
		var selText = (selCodes && selCodes[0] ? (selCodes[0] || "") : "");
		var from = (selRanges && selRanges[0] ? selRanges[0].anchor : null);
		var to = (selRanges && selRanges[0] ? selRanges[0].head : null);
		
		if(from && to)
		{
			var swap = ((from.line > to.line) || (from.line == to.line && from.ch > to.ch));
			if(swap)
			{
				var fromTmp = from;
				from = to;
				to = fromTmp;
			}
		}
		
		return { text: selText, from: from, to: to };
	};
	
	po.insertCodeText = function(codeEditor, cursor, text)
	{
		//(codeEditor, text)
		if(arguments.length == 2)
		{
			text = cursor;
			cursor = undefined;
		}
		
		var doc = codeEditor.getDoc();
		cursor = (cursor == null ? doc.getCursor() : cursor);
		
		doc.replaceRange(text, cursor);
	};
	
	//查找补全列表
	//completions : { name: "...", ?value: "...", ?displayName: "...", ?displayComment: "...", ?categories: [ "小写字符串", ... ] }
	po.findCompletionList = function(completions, namePrefix, category)
	{
		var re = [];
		
		if(!completions)
			return re;
		
		namePrefix = (namePrefix ? namePrefix.toLowerCase() : namePrefix);
		category = (category ? category.toLowerCase() : category);
		
		for(var i=0; i<completions.length; i++)
		{
			var comp = completions[i];
			
			var nameLower = comp.name.toLowerCase();
			
			if(namePrefix && nameLower.indexOf(namePrefix) != 0)
				continue;
			
			if(!category || (category && comp.categories && $.inArray(category, comp.categories) > -1))
			{
				re.push(
				{
					text: (comp.value ? comp.value : comp.name),
					displayText: (comp.displayName ? comp.displayName : comp.name),
					displayComment: comp.displayComment,
					render: po.renderCompletionItem
				});
			}
		}
		
		return re;
	};
	
	po.renderCompletionItem = function(element, self, data)
	{
		//$(element).addClass("code-completion-item");
		
		$("<span class='code-completion-item' />").text(data.displayText ? data.displayText : data.text).appendTo(element);
		if(data.displayComment)
			$("<span class='code-completion-comment' />").text(data.displayComment ? data.displayComment : "").appendTo(element);
	};
	
	po.findPrevTokenOfType = function(codeEditor, doc, cursor, cursorToken, tokenType)
	{
		var tokenInfo = po.findPrevTokenInfoOfType(codeEditor, doc, cursor, cursorToken, tokenType);
		return (tokenInfo ? tokenInfo.token : undefined);
	};
	
	po.findPrevTokenInfoOfType = function(codeEditor, doc, cursor, cursorToken, tokenType)
	{
		return po.findPrevTokenInfo(codeEditor, doc, cursor, cursorToken, function(token){ return (token.type == tokenType); });
	};
	
	po.findPrevTokenInfo = function(codeEditor, doc, cursor, cursorToken, predicate)
	{
		doc = (doc ? doc : codeEditor.getDoc());
		cursor = (cursor ? cursor : doc.getCursor());
		cursorToken = (cursorToken ? cursorToken : (codeEditor.getTokenAt(cursor) || {}));
		var minLine = (cursor.line-100 <= 0 ? 0 : cursor.line-100);
		
		for(var line=cursor.line; line >=minLine; line--)
		{
			var tokens = codeEditor.getLineTokens(line);
			for(var i=tokens.length-1; i>=0; i--)
			{
				var token = tokens[i];
				
				if(line == cursor.line && token.start >= cursorToken.start)
					continue;
				
				if(predicate(token) == true)
					return { token: token, line: line };
			}
		}
		
		return null;
	};
	
	po.findNextTokenInfoOfType = function(codeEditor, doc, cursor, cursorToken, tokenType)
	{
		return po.findNextTokenInfo(codeEditor, doc, cursor, cursorToken, function(token){ return (token.type == tokenType); });
	};
	
	po.findNextTokenInfo = function(codeEditor, doc, cursor, cursorToken, predicate)
	{
		doc = (doc ? doc : codeEditor.getDoc());
		var lastLine = doc.lastLine();
		cursor = (cursor ? cursor : doc.getCursor());
		cursorToken = (cursorToken ? cursorToken : (codeEditor.getTokenAt(cursor) || {}));
		
		for(var line=cursor.line; line<=lastLine; line++)
		{
			var tokens = codeEditor.getLineTokens(line);
			for(var i=0; i<tokens.length; i++)
			{
				var token = tokens[i];
				
				if(line == cursor.line && token.start <= cursorToken.start)
					continue;
				
				if(predicate(token) == true)
					return { token: token, line: line };
			}
		}
		
		return null;
	};
};

//填充page_sql_editor.ftl里JS对象的静态逻辑
$.inflatePageSqlEditor = function(po)
{
	//获取数据源ID
	po.getSqlEditorDtbsSourceId = function(){ /*需实现*/ };
	
	//SQL提示缓存
	po.sqlHintCache =
	{
		//表名 -> 列名
		tableColumnCompletions: {},
		tableNameCompletions: [],
		tableNameCompletionsLoaded: false,
		ajaxRunning: false
	};
	
	po.createSqlEditor = function(dom, options)
	{
		options = po.inflateSqlEditorOptions(options);
		return po.createCodeEditor(dom, options);
	};
	
	po.inflateSqlEditorOptions = function(options)
	{
		options = (options || {});
		options.mode = "sql";
		
		if(!options.readOnly)
		{
			options.hintOptions = (options.hintOptions || {});
			options.hintOptions.hint = po.sqlEditorHintHandler;
			options.hintOptions.hint.async = true;
		}
		
		return options;
	};
	
	po.sqlEditorHintTableAjaxOptions = function(dtbsSourceId)
	{
		var options = { url: po.concatContextPath("/dtbsSourceSqlEditor/"+dtbsSourceId+"/findTableNames") };
		return options;
	};
	
	po.sqlEditorHintColumnAjaxOptions = function(dtbsSourceId, tableName)
	{
		var options =
		{
			url: po.concatContextPath("/dtbsSourceSqlEditor/"+dtbsSourceId+"/findColumns"),
			data: { table: tableName }
		};
		
		return options;
	};
	
	po.sqlEditorHintHandler = function(codeEditor, callback)
	{
		var doc = codeEditor.getDoc();
		var cursor = doc.getCursor();
		var mode = (codeEditor.getModeAt(cursor) || {});
		var token = (codeEditor.getTokenAt(cursor) || {});
		
		var dtbsSourceId = po.getSqlEditorDtbsSourceId();
		
		//关键字token、分号token不应提示
		if(!dtbsSourceId || token.type == "keyword" || po.isTokenSemicolonOrAfter(codeEditor, doc, cursor, token))
		{
			callback();
			return;
		}
		
		var hintInfo = po.resolveSqlHintInfo(codeEditor, doc, cursor, token);
		
		if(!hintInfo || (hintInfo.type != "table" &&  hintInfo.type != "column"))
		{
			callback();
			return;
		}
		
		var namePrefix = hintInfo.namePrefix;
		
		if(hintInfo.type == "table")
		{
			if(po.sqlHintCache.tableNameCompletionsLoaded)
			{
				var completions =
				{
					list: po.findCompletionList(po.sqlHintCache.tableNameCompletions, namePrefix),
					from: CodeMirror.Pos(cursor.line, (namePrefix ? token.start : token.end)),
					to: CodeMirror.Pos(cursor.line, token.end)
				};
				
				callback(completions);
			}
			else
			{
				if(po.sqlHintCache.ajaxRunning)
					callback();
				else
				{
					po.sqlHintCache.ajaxRunning = true;
					
					var ajaxOptions = $.extend(
					{
						type : "POST",
						success: function(names)
						{
							names = (names || []);
							
							var tableNameCompletions = [];
							
							for(var i=0; i<names.length; i++)
								tableNameCompletions[i] = { name: names[i] };
							
							po.sqlHintCache.tableNameCompletions = tableNameCompletions;
							po.sqlHintCache.tableNameCompletionsLoaded = true;
							
							var completions =
							{
								list: po.findCompletionList(po.sqlHintCache.tableNameCompletions, namePrefix),
								from: CodeMirror.Pos(cursor.line, (namePrefix ? token.start : token.end)),
								to: CodeMirror.Pos(cursor.line, token.end)
							};
							
							callback(completions);
							po.sqlHintCache.ajaxRunning = false;
						},
						error: function()
						{
							callback();
							po.sqlHintCache.ajaxRunning = false;
						}
					},
					po.sqlEditorHintTableAjaxOptions(dtbsSourceId));
					
					$.ajax(ajaxOptions);
				}
			}
		}
		else if(hintInfo.type == "column" && hintInfo.tableName)
		{
			if(po.sqlHintCache.tableColumnCompletions[hintInfo.tableName])
			{
				var completions =
				{
					list: po.findCompletionList(po.sqlHintCache.tableColumnCompletions[hintInfo.tableName], namePrefix),
					from: CodeMirror.Pos(cursor.line, (namePrefix ? token.start : token.end)),
					to: CodeMirror.Pos(cursor.line, token.end)
				};
				
				callback(completions);
			}
			else
			{
				if(po.sqlHintCache.ajaxRunning)
					callback();
				else
				{
					po.sqlHintCache.ajaxRunning = true;
					
					var ajaxOptions = $.extend(
					{
						type : "POST",
						success: function(columns)
						{
							var columnCompletions = $.toSqlEditorColumnCompletions(hintInfo.tableName, columns);
							po.sqlHintCache.tableColumnCompletions[hintInfo.tableName] = columnCompletions;
							
							var completions =
							{
								list: po.findCompletionList(po.sqlHintCache.tableColumnCompletions[hintInfo.tableName], namePrefix),
								from: CodeMirror.Pos(cursor.line, (namePrefix ? token.start : token.end)),
								to: CodeMirror.Pos(cursor.line, token.end)
							};
							
							callback(completions);
							po.sqlHintCache.ajaxRunning = false;
						},
						error: function()
						{
							callback();
							po.sqlHintCache.ajaxRunning = false;
						}
					},
					po.sqlEditorHintColumnAjaxOptions(dtbsSourceId, hintInfo.tableName));
					
					$.ajax(ajaxOptions);
				}
			}
		}
		else
			callback();
	};
	
	//token是否是分号，或者除空格外的下一个
	po.isTokenSemicolonOrAfter = function(codeEditor, doc, cursor, token)
	{
		if(!token)
			return false;
		
		var scReg = /\;\s*$/;
		
		if(token.string && scReg.test(token.string))
			return true;
		
		var blankReg = /^\s*$/;
		
		var foundTokenInfo = po.findPrevTokenInfo(codeEditor, doc, cursor, token, function(token)
		{
			if(token.string != null && !blankReg.test(token.string))
				return true;
		});
		
		if(!foundTokenInfo || !foundTokenInfo.token || !foundTokenInfo.token.string)
			return false;
		
		return scReg.test(foundTokenInfo.token.string);
	};
	
	po.resolveSqlHintInfo = function(codeEditor, doc, cursor, cursorToken)
	{
		var info = null;
		
		var tokenInfo = null;
		var cursorTmp = cursor;
		var cursorTokenTmp = cursorToken;
		
		while((tokenInfo = po.findPrevTokenInfoOfType(codeEditor, doc, cursorTmp, cursorTokenTmp, "keyword")) != null)
		{
			var keywordToken = tokenInfo.token;
			var keyword = (keywordToken.string || "").toUpperCase();
			
			if(po.sqlKeywords.all[keyword])
			{
				if(po.sqlKeywords.nextIsTable[keyword])
					info = { type: "table", namePrefix: (po.isNormalSqlNameTokenType(cursorToken.type) ? ($.trim(cursorToken.string) || "") : "") };
				else if(po.sqlKeywords.nextIsColumn[keyword])
					info = { type: "column" };
				
				break;
			}
			
			cursorTmp = CodeMirror.Pos(tokenInfo.line, keywordToken.start);
			cursorTokenTmp = keywordToken;
		}
		
		//查找表名
		if(info && info.type == "column" && tokenInfo)
		{
			var columnInfoStr = po.resolveSqlColumnInfoString(codeEditor, doc, cursor, cursorToken);
			
			if(columnInfoStr)
			{
				var columnInfoStrs = columnInfoStr.split(".");
				info.namePrefix = (columnInfoStrs.length > 1 ? columnInfoStrs[1] : columnInfoStrs[0]);
				info.tableName = (columnInfoStrs.length > 1 ? columnInfoStrs[0] : null);
			}
			
			//向上直到SQL语句开头
			while(tokenInfo != null)
			{
				var myToken = tokenInfo.token;
				var myString = (myToken.string || "").toUpperCase();
				
				if(po.sqlKeywords.start[myString])
					break;
				
				tokenInfo = po.findPrevTokenInfoOfType(codeEditor, doc, CodeMirror.Pos(tokenInfo.line, myToken.start), myToken, "keyword");
			}
			
			//向下查找表名的前置关键字token
			while(tokenInfo != null)
			{
				var myToken = tokenInfo.token;
				var myString = (myToken.string || "").toUpperCase();
				
				if(po.sqlKeywords.nextIsTable[myString])
					break;
				
				tokenInfo = po.findNextTokenInfoOfType(codeEditor, doc, CodeMirror.Pos(tokenInfo.line, myToken.start), myToken, "keyword");
			}
			
			//向下解析表名
			if(tokenInfo)
			{
				var prevTokenType = null, prevTokenString = null;
				var prevPrevTokenType = null, prevPrevTokenString = null;
				tokenInfo = po.findNextTokenInfo(codeEditor, doc, CodeMirror.Pos(tokenInfo.line, tokenInfo.token.start), tokenInfo.token,
				function(token)
				{
					//如果有括号，说明是复杂语句，暂不解析
					if(token.type == "bracket")
						return true;
					
					var myString = ($.trim(token.string) || "");
					
					if(!myString)
						return false;
					
					if(po.isNormalSqlNameTokenType(token.type))
					{
						//如果没有表别名，则使用第一个作为表名
						if(!info.tableName)
						{
							info.tableName = myString;
							return true;
						}
						else
						{
							//判断是否表别名
							if(myString == info.tableName)
							{
								//表名 AS 别名
								if(prevTokenType == "keyword" && /as/i.test(prevTokenString)
										&& po.isNormalSqlNameTokenType(prevPrevTokenType) && prevPrevTokenString)
								{
									info.tableName = prevPrevTokenString;
								}
								//表名 别名
								else if(po.isNormalSqlNameTokenType(prevTokenType) && prevTokenString)
								{
									info.tableName = prevTokenString;
								}
								
								return true;
							}
						}
					}
					
					prevPrevTokenType = prevTokenType;
					prevPrevTokenString = prevTokenString;
					prevTokenType = token.type;
					prevTokenString = myString;
				});
			}
		}
		
		return info;
	};
	
	po.resolveSqlColumnInfoString = function(codeEditor, doc, cursor, cursorToken)
	{
		var columnInfoString = "";
		
		if(po.isSqlColumnInputStringPart(cursorToken))
		{
			columnInfoString = cursorToken.string;
			
			po.findPrevTokenInfo(codeEditor, doc, cursor, cursorToken, function(token)
			{
				if(po.isSqlColumnInputStringPart(token))
					columnInfoString = token.string + columnInfoString;
				else
					return true;
			});
		}
		
		return columnInfoString;
	};
	
	po.isSqlColumnInputStringPart = function(cursorToken)
	{
		var str = cursorToken.string;
		
		//","、"("、"空白" 不是列相关输入字符串
		if(/^[\(\,]$/.test(str) || /^\s*$/.test(str))
			return false;
		
		return true;
	};
	
	po.isNormalSqlNameTokenType = function(tokenType)
	{
		return (tokenType == null);
	};
	
	po.sqlKeywords =
	{
		//全部，会由下面关键字合并而得
		all: {},
		
		//SQL语句开始关键字*（必须大写）
		start:
		{
			"SELECT" : true, "INSERT" : true, "UPDATE" : true, "DELETE" : true,
			"ALTER" : true, "DROP" : true, "CREATE" : true, "REPLACE" : true, "MERGE" : true,
			"GRANT" : true
		},
		
		//下一个Token是表名（必须大写）
		nextIsTable:
		{
			"FROM" : true,
			"JOIN" : true,
			"UPDATE" : true,
			"INTO" : true,
			"TABLE" : true
		},
		
		//下一个Token是列名（必须大写）
		nextIsColumn:
		{
			"SELECT" : true,
			"WHERE" : true,
			"ON" : true,
			"BY" : true,
			"SET" : true
		}
	};
	
	po.sqlKeywords.all = $.extend(po.sqlKeywords.all, po.sqlKeywords.start,
									po.sqlKeywords.nextIsTable, po.sqlKeywords.nextIsColumn);
};

//初始化page_palette.ftl页面对象
$.inflatePagePalette = function(po)
{
	po.cssColorToHexStr = function(cssColor)
	{
		if(!cssColor)
			return "";
		else
			return chartFactory.colorToHexStr(cssColor, true);
	};

	po.cssColorsToHexStrs = function(cssColors)
	{
		if(!cssColors)
			return [];
		
		var re = [];
		
		$.each(cssColors, function(i, cssColor)
		{
			re.push(po.cssColorToHexStr(cssColor));
		});
		
		return re;
	};
	
	po.hexStrToCssColor = function(hexStr, dftCssColor)
	{
		if(!hexStr)
			return (dftCssColor || "");
		else if(hexStr.charAt(0) != '#')
			return "#" + hexStr;
		else
			return hexStr;
	};
	
	po.showPalettePanel = function(e, modelObj, modelProp)
	{
		var pm = po.vuePageModel();
		
		pm.palette.modelObj = modelObj;
		pm.palette.modelProp = modelProp;
		
		if(pm.palette.modelObj != null && pm.palette.modelProp != null)
		{
			pm.palette.value = pm.palette.modelObj[pm.palette.modelProp];
			pm.palette.pickerValue = po.cssColorToHexStr(pm.palette.value);
		}
		
		po.vueUnref(po.concatPid("palettePanelEle")).show(e);
	}
	
	po.setupPalette = function()
	{
		po.vueRef(po.concatPid("palettePanelEle"), null);
		
		var pm = po.vuePageModel();
		
		po.vuePageModel(
		{
			palette:
			{
				colors: $.paletteColors,
				pureColors: [ "#FFFFFF", "#FF0000", "#00FF00", "#0000FF", "#FF00FF", "#FFFF00", "#00FFFF", "#000000" ],
				modelObj: null,
				modelProp: null,
				value: null,
				pickerValue: null
			}
		});
		
		po.vueMethod(
		{
			showPalettePanel: function(e, modelObj, modelProp)
			{
				po.showPalettePanel(e, modelObj, modelProp);
			},
			
			onPalettePanelShow: function(e){},
			
			onSelectPaletteColor: function(color, close)
			{
				close = (close === undefined ? false : close);
				
				if(pm.palette.modelObj != null && pm.palette.modelProp != null)
				{
					pm.palette.value = color;
					pm.palette.pickerValue = po.cssColorToHexStr(color);
					pm.palette.modelObj[pm.palette.modelProp] = color;
				}
				
				if(close)
				{
					po.vueUnref(po.concatPid("palettePanelEle")).hide();
				}
			},
			
			onSelectPaletteColorPicker: function(e)
			{
				this.onSelectPaletteColor(po.hexStrToCssColor(pm.palette.pickerValue));
			}
		});
	};
};

//初始化page_tabview.ftl页面对象
$.inflatePageTabView = function(po)
{
	po.tabviewTabActive = function(tabViewModel, tabId)
	{
		var items = tabViewModel.items;
		var idx = $.inArrayById(items, tabId);
		
		if(idx >= 0)
			tabViewModel.activeIndex = idx;
		
		return idx;
	};
	
	po.tabviewTabIndex = function(tabViewModel, tabId)
	{
		var items = tabViewModel.items;
		var idx = $.inArrayById(items, tabId);
		return idx;
	};
	
	po.tabviewTab = function(tabViewModel, tabId)
	{
		var items = tabViewModel.items;
		var idx = $.inArrayById(items, tabId);
		
		if(idx < 0)
			return null;
		
		return items[idx];
	};
	
	po.tabviewClose = function(tabViewModel, tabId)
	{
		var items = tabViewModel.items;
		var idx = $.inArrayById(items, tabId);
		
		if(idx < 0)
			return;
		
		po.removeTabItems(tabViewModel, idx, 1);
		
		if(idx <= tabViewModel.activeIndex)
			tabViewModel.activeIndex = (tabViewModel.activeIndex > 0 ? tabViewModel.activeIndex - 1 : 0);
	};
	
	po.tabviewCloseOther = function(tabViewModel, tabId)
	{
		po.tabviewCloseLeft(tabViewModel, tabId);
		po.tabviewCloseRight(tabViewModel, tabId);
		
		tabViewModel.activeIndex = 0;
	};
	
	po.tabviewCloseRight = function(tabViewModel, tabId)
	{
		var items = tabViewModel.items;
		var idx = $.inArrayById(items, tabId);
		
		if(idx < 0)
			return;
		
		var count = ((items.length - idx - 1) > 0 ? (items.length - idx - 1) : 0);
		po.removeTabItems(tabViewModel, idx+1, count);
		
		tabViewModel.activeIndex = idx;
	};
	
	po.tabviewCloseLeft = function(tabViewModel, tabId)
	{
		var items = tabViewModel.items;
		var idx = $.inArrayById(items, tabId);
		
		if(idx < 0)
			return;
		
		po.removeTabItems(tabViewModel, 0, idx);
		tabViewModel.activeIndex = tabViewModel.activeIndex - idx;
	};
	
	po.tabviewCloseAll = function(tabViewModel)
	{
		po.removeTabItems(tabViewModel, 0, tabViewModel.items.length);
		tabViewModel.activeIndex = 0;
	};
	
	po.tabviewOpenInNewWindow = function(tabViewModel, tabId)
	{
		var items = tabViewModel.items;
		var idx = $.inArrayById(items, tabId);
		
		if(idx < 0)
			return;
		
		if(items[idx] && items[idx].url)
			window.open(items[idx].url);
	};
	
	po.removeTabItems = function(tabViewModel, index, count)
	{
		var items = tabViewModel.items;
		
		var removeIdx = index;
		for(var i=0; i<count; i++)
		{
			var item = items[removeIdx];
			if(item.closeable !== false)
				items.splice(removeIdx, 1);
			else
				removeIdx += 1;
		}
	};
	
	po.tabviewIndexesOfClose = function(tabViewModel, tabId)
	{
		var items = tabViewModel.items;
		var idx = $.inArrayById(items, tabId);
		return ( idx < 0 ? [] : [ idx ]);
	};
	
	po.tabviewIndexesOfCloseOther = function(tabViewModel, tabId)
	{
		var items = tabViewModel.items;
		var idx = $.inArrayById(items, tabId);
		
		var re = [];
		
		if(idx < 0)
			return re;
		
		for(var i=0; i<items.length; i++)
		{
			if(i != idx)
				re.push(i);
		}
		
		return re;
	};
	
	po.tabviewIndexesOfCloseRight = function(tabViewModel, tabId)
	{
		var items = tabViewModel.items;
		var idx = $.inArrayById(items, tabId);
		
		var re = [];
		
		if(idx < 0)
			return re;
		
		for(var i=idx+1; i<items.length; i++)
		{
			re.push(i);
		}
		
		return re;
	};
	
	po.tabviewIndexesOfCloseLeft = function(tabViewModel, tabId)
	{
		var items = tabViewModel.items;
		var idx = $.inArrayById(items, tabId);
		
		var re = [];
		
		if(idx < 0)
			return re;
		
		for(var i=0; i<idx; i++)
		{
			re.push(i);
		}
		
		return re;
	};
};

//初始化chart_config_values_form.ftl页面对象
$.inflateChartConfigValuesForm = function(po)
{
	var avo = (po.avo || (po.avo = {}));
	
	avo.REF_ID_NAME = "$refId";
	avo.DISABLEIF_NAME = "disableIf";
	avo.ENABLEIF_NAME = "enableIf";
	avo.EVAL_ENABLED_FUNC_ROOT_DATA_ARG = "$root";
	avo.EVAL_ENABLED_FUNC_THIS_DATA_ARG = "$this";
	
	avo.isPropertiesAware = function(prop)
	{
		return (prop != null && (prop.type == avo.FormPropertyType.OBJECT || prop.properties !== undefined));
	};
	
	avo.toGroupTrimPluginConfigForm = function(pluginConfigForm)
	{
		if(pluginConfigForm == null)
			return pluginConfigForm;
		
		if(pluginConfigForm.doneGroupTrim)
			return pluginConfigForm;
		
		var re = $.extend(true, {}, pluginConfigForm);
		
		for(var i=0; i<re.properties.length; i++)
		{
			avo.toTrimProperty(re.properties[i], pluginConfigForm);
		}
		
		avo.groupProperties(re);
		re.doneGroupTrim = true;
		
		return re;
	};
	
	//将org.datagear.analysis.form.FormProperty转换为标准格式
	avo.toTrimProperty = function(prop, pluginConfigForm)
	{
		pluginConfigForm = (pluginConfigForm === undefined ? prop : pluginConfigForm);
		
		if(prop == null)
			return prop;
		
		var re = prop;
		
		avo.doTrimProperty(re, pluginConfigForm);
		
		if(avo.isPropertiesAware(re) && !$.isEmpty(re.properties))
		{
			for(var i=0; i<re.properties.length; i++)
			{
				avo.toTrimProperty(re.properties[i], pluginConfigForm);
			}
		}
		
		return re;
	};
	
	avo.propertyDomIdIndex = 0;
	
	avo.doTrimProperty = function(prop, pluginConfigForm)
	{
		prop.domId = po.concatPid("avoprop_"+ (avo.propertyDomIdIndex++));
		prop.nameLabel = (prop.nameLabel == null ? {} : prop.nameLabel);
		prop.nameLabel.value = ($.isEmpty(prop.nameLabel.value) ? prop.name : prop.nameLabel.value);
		prop.nameLabel.value = ($.isEmpty(prop.nameLabel.value) ? po.i18n.unnamed : prop.nameLabel.value);
		avo.buildEvalEnabledFunc(prop);
		
		if(avo.isPropertiesAware(prop))
		{
			if(prop.groups != null)
			{
				for(var i=0; i<prop.groups.length; i++)
					avo.buildEvalEnabledFunc(prop.groups[i]);
			}
			
			return;
		}
		
		var rootAdditions = (pluginConfigForm ? pluginConfigForm.additions : null);
		
		//布尔型默认作为RADIO处理
		if(prop.type == avo.FormPropertyType.BOOLEAN)
		{
			if(!prop.inputType)
				prop.inputType = avo.FormPropertyInputType.RADIO;
			
			if(!prop.inputPayload)
			{
				var pm = po.vuePageModel();
				prop.inputPayload = $.extend(true, [], po.vueRaw(pm.booleanOptions));
			}
		}
		
		var inputType = prop.inputType;
		
		//下拉框、单选、复选框：将inputPayload转换为{multiple: ..., options: [{name: ..., value: ...}, ...]}格式
		if(inputType == avo.FormPropertyInputType.SELECT
				|| inputType == avo.FormPropertyInputType.RADIO
				|| inputType == avo.FormPropertyInputType.CHECKBOX)
		{
			//处理inputPayload中的引用
			if(prop.inputPayload != null)
			{
				//处理inputPayload格式：{ "$refId": ... }
				if(prop.inputPayload[avo.REF_ID_NAME] !== undefined)
				{
					var refId = prop.inputPayload[avo.REF_ID_NAME];
					prop.inputPayload = (rootAdditions ? $.deepClonePlain(rootAdditions[refId]) : null);
				}
				//处理inputPayload.options格式：{ "$refId": ... }
				else if(prop.inputPayload.options && prop.inputPayload.options[avo.REF_ID_NAME] !== undefined)
				{
					var refId = prop.inputPayload.options[avo.REF_ID_NAME];
					prop.inputPayload.options = (rootAdditions ? $.deepClonePlain(rootAdditions[refId]) : null);
				}
			}
			
			var inputPayload = (prop.inputPayload || []);
			
			//数组、"DG_MAP"：转换为{ multiple: false, options: ... }格式
			if($.isArray(inputPayload) || (inputPayload == avo.FormPropertyInputPayload.DG_MAP))
				inputPayload = { multiple: false, options: inputPayload };
			
			//{ options: "DG_MAP" }：转换为实际地图数据options
			avo.trimPropertyInputPayloadIfMap(prop, inputPayload);
			
			//默认multiple为false
			inputPayload.multiple = (inputPayload.multiple == null ? false : inputPayload.multiple);
			avo.trimPropertyInputOptions(prop, inputPayload);
			
			if(inputType == avo.FormPropertyInputType.RADIO)
			{
				inputPayload.multiple = false;
			}
			else if(inputType == avo.FormPropertyInputType.CHECKBOX)
			{
				inputPayload.multiple = true;
			}
			
			prop.inputPayload = inputPayload;
		}
		//颜色框
		else if(inputType == avo.FormPropertyInputType.COLOR)
		{
			var inputPayload = prop.inputPayload;
			
			//将5.5.0旧版inputPayload格式{ multiple: true }、"multiple"转换为prop.array=true格式
			if(inputPayload != null)
			{
				if(inputPayload.multiple == true)
				{
					prop.array = true;
					inputPayload.multiple = false;
				}
				else if(inputPayload == avo.FormPropertyInputPayload.MULTIPLE)
				{
					prop.array = true;
					prop.inputPayload = null;
				}
			}
		}
		
		//将5.5.0旧版的{inputPayload: {multiple: "repeat"}}格式转换为6.0新版的{array: true, inputPayload: {multiple: false}}
		if(prop.inputPayload && prop.inputPayload.multiple == avo.FormPropertyInputPayload.MultipleRepeat)
		{
			prop.array = true;
			prop.inputPayload.multiple = false;
		}
	};
	
	avo.trimPropertyInputPayloadIfMap = function(inputProp, inputPayload)
	{
		var options = inputPayload.options;
		
		//内置地图
		if(options == avo.FormPropertyInputPayload.DG_MAP)
		{
			//只有下拉列表才使用树形结构，单选框、复选框只能使用平铺数组
			if(inputPayload.treeSelect == null
					&& inputProp.inputType == avo.FormPropertyInputType.SELECT)
			{
				inputPayload.treeSelect = true;
			}
			
			inputPayload.options = avo.propertyInputOptionsForMap(inputPayload.treeSelect);
		}
	};
	
	avo.trimPropertyInputOptions = function(inputProp, inputPayload)
	{
		if(!inputPayload.options)
			inputPayload.options = [];
		
		//支持非数组格式
		if(!$.isArray(inputPayload.options))
			inputPayload.options = [ inputPayload.options ];
		
		var options = inputPayload.options;
		
		//转换为标准的[ {name: ..., value: ...}, ... ]格式
		$.each(options, function(i, io)
		{
			//支持元素为基本类型
			if(io == null || $.isTypeString(io) || $.isTypeNumber(io) || $.isTypeBoolean(io))
			{
				options[i] = { name: io, value: io };
			}
			
			//支持{value: ...}格式的元素
			if(io.name == null)
				io.name = (io.value == null ? "null" : io.value);
		});
	};
	
	avo.propertyInputOptionsForMap = function(asTree)
	{
		if(po.getChartConfigInputOptionsForMap != null)
			return po.getChartConfigInputOptionsForMap(asTree);
		
		//树
		if(asTree)
		{
			var listener =
			{
				added: function(node, parent, rootArray)
				{
					//转换为UI组件所需的结构
					node.key = node.mapName;
					node.label = node.mapLabel;
					if(parent && !parent.children)
						parent.children = parent.mapChildren;
				}
			};
			
			return dashboardFactory.getStdBuiltinMapTree(listener);
		}
		//数组
		else
		{
			var listener =
			{
				added: function(node, rootArray)
				{
					//转换为UI组件所需的结构
					node.value = node.mapName;
					node.name = node.mapLabel;
				}
			};
			
			return dashboardFactory.getStdBuiltinMapArray(listener);
		}
	};
	
	//将org.datagear.analysis.form.GroupFormProperties.properties分组整理至groupProps中
	avo.groupProperties = function(gfpObj)
	{
		if(gfpObj == null)
			return;
		
		if(gfpObj.isGrouped)
			return;
		
		gfpObj.isGrouped = true;
		
		if($.isEmpty(gfpObj.properties))
			return;
		
		var oldGroups = avo.resolveOldGroup(gfpObj.properties);
		if(oldGroups.length > 0)
		{
			gfpObj.groups = (gfpObj.groups == null ? [] : gfpObj.groups);
			gfpObj.groups = gfpObj.groups.concat(oldGroups);
		}
		
		var groupProps = [];
		var groups = (gfpObj.groups || []);
		var props = gfpObj.properties;
		
		for(var i=0; i<props.length; i++)
		{
			var prop = props[i];
			var myGroup = null;
			var groupIdx = avo.findGroupIdxByPropName(groupProps, prop.name);
			
			if(groupIdx >= 0)
				myGroup = groupProps[groupIdx];
			else
			{
				groupIdx = avo.findGroupIdxByPropName(groups, prop.name);
				if(groupIdx >= 0)
				{
					myGroup = groups[groupIdx];
					groupProps.push(myGroup);
				}
			}
			
			if(myGroup == null)
			{
				if(groupProps.length > 0 && groupProps[groupProps.length-1].virtual)
				{
					myGroup = groupProps[groupProps.length-1];
				}
				else
				{
					myGroup = { nameLabel: { value: po.i18n.ungrouped }, virtual: true };
					groupProps.push(myGroup);
				}
			}
			
			myGroup.isGroup = true;
			myGroup.nameLabel = (myGroup.nameLabel == null ? {} : myGroup.nameLabel);
			myGroup.nameLabel.value = ($.isEmpty(myGroup.nameLabel.value) ? po.i18n.unnamed : myGroup.nameLabel.value);
			myGroup.properties = (myGroup.properties == null ? [] : myGroup.properties);
			myGroup.properties.push(prop);
			
			if(avo.isPropertiesAware(prop))
				avo.groupProperties(prop);
		}
		
		for(var i=0; i<groupProps.length; i++)
		{
			avo.buildEvalEnabledFunc(groupProps[i]);
			groupProps[i].groupCollapsed = (i != 0);
		}
		
		gfpObj.groupProps = groupProps;
	};
	
	avo.findGroupIdxByPropName = function(groups, propName)
	{
		if(groups == null)
			return -1;
		
		for(var i=0; i<groups.length; i++)
		{
			if(groups[i].names && groups[i].names.indexOf(propName) > -1)
				return i;
		}
		
		return -1;
	};
	
	//兼容处理5.5.0版本的org.datagear.analysis.ChartPluginAttribute.group
	avo.resolveOldGroup = function(props)
	{
		var groups = [];
		
		for(var i=0; i<props.length; i++)
		{
			var prop = props[i];
			
			if(!prop || !prop.additions || !prop.additions[avo.INPUT_PROPERTY_ADDITION_OLD_GROUP])
				continue;
			
			var oldGroup = prop.additions[avo.INPUT_PROPERTY_ADDITION_OLD_GROUP];
			
			var group =
			{
				nameLabel: { value: "" }, names: []
			};
			
			if(!$.isEmpty(oldGroup.name))
				group.nameLabel.value = oldGroup.name;
			else if(oldGroup.nameLabel && !$.isEmpty(oldGroup.nameLabel.value))
				group.nameLabel.value = oldGroup.nameLabel.value;
			
			//无分组名称标签的，只在末尾分组相同时才使用，否则新建
			if($.isEmpty(group.nameLabel.value))
			{
				group.nameLabel.value = po.i18n.ungrouped;
				var groupTail = (groups.length > 0 ? groups[groups.length - 1] : null);
				
				if(groupTail && groupTail.nameLabel && groupTail.nameLabel.value == group.nameLabel.value)
				{
					group = groupTail;
				}
				else
				{
					groups.push(group);
				}
			}
			//有分组名称标签的，查找或新建
			else
			{
				var existIdx = -1;
				
				for(var j=0; j<groups.length; j++)
				{
					if(groups[j].nameLabel && groups[j].nameLabel.value == group.nameLabel.value)
					{
						existIdx = j;
						break;
					}
				}
				
				if(existIdx >= 0)
				{
					group = groups[existIdx];
				}
				else
				{
					groups.push(group);
				}
			}
			
			group.names.push(prop.name);
		}
		
		return groups;
	};
	
	avo.buildEvalEnabledFunc = function(additionsAware)
	{
		if(additionsAware == null || additionsAware.additions == null)
			return;
		
		var additions = additionsAware.additions;
		var disableIf = additions[avo.DISABLEIF_NAME];
		var enableIf = additions[avo.ENABLEIF_NAME];
		
		if($.isEmpty(disableIf) && $.isEmpty(enableIf))
			return;
		
		var funcBody = "try{ ";
		
		//仅允许disableIf和enableIf的其中之一有效，且disableIf优先
		if(!$.isEmpty(disableIf))
		{
			funcBody += "if(("+disableIf+") === true) { return false; } else { return true; }";
		}
		else if(!$.isEmpty(enableIf))
		{
			funcBody += "if(("+enableIf+") === true) { return true; } else { return false; }";
		}
		
		funcBody += " } catch(e) { return true; }";
		
		additionsAware.evalEnabledFunc = new Function(avo.EVAL_ENABLED_FUNC_ROOT_DATA_ARG, avo.EVAL_ENABLED_FUNC_THIS_DATA_ARG, funcBody);
	};
	
	avo.ctrlPropName = po.concatPid("avoctlprop");
	avo.unrelatedPropName = po.concatPid("unrelatedProp");
	
	//图表配置值对象转换为org.datagear.analysis.ChartPluginConfigForm的表单数据模型
	avo.configValuesToFormData = function(configValues, formData, pluginConfigForm, initDftValue)
	{
		configValues = (configValues || {});
		formData = (formData || {});
		initDftValue = (initDftValue === undefined ? true : initDftValue);
		
		if(pluginConfigForm == null)
		{
			formData = $.extend(true, formData, configValues);
		}
		else
		{
			pluginConfigForm = avo.toGroupTrimPluginConfigForm(pluginConfigForm);
			formData = $.extend(true, formData, configValues);
			avo.doConfigValuesToFormData(formData, pluginConfigForm, initDftValue);
		}
		
		return formData;
	};
	
	avo.doConfigValuesToFormData = function(configValues, propertiesAware, initDftValue)
	{
		initDftValue = (initDftValue === undefined ? true : initDftValue);
		
		if(configValues == null || propertiesAware == null || $.isEmpty(propertiesAware.properties))
			return;
		
		var data = configValues;
		var props = propertiesAware.properties;
		
		data[avo.ctrlPropName] = { propEnableds: {}, propCollapseds: {}, propViewValues: {}, propBakValues: {}, propEnableIfs: {} };
		var propEnableds = data[avo.ctrlPropName].propEnableds;
		var propCollapseds = data[avo.ctrlPropName].propCollapseds;
		var propViewValues = data[avo.ctrlPropName].propViewValues;
		
		for(var i=0; i<props.length; i++)
		{
			var prop = props[i];
			var v = data[prop.name];
			
			if(avo.isPropertiesAware(prop))
			{
				//null必填项应在这里初始化，即使在avo.evalEnableIf()函数中有相关逻辑，不然avo.clearFormData()逻辑不对
				if(v == null && prop.required)
					v = (prop.array ? [] : {});
				
				if(v != null)
				{
					if(prop.array)
					{
						if(!$.isArray(v))
							v = [ v ];
						
						for(var j=0; j<v.length; j++)
							avo.doConfigValuesToFormData(v[j], prop, initDftValue);
					}
					else
					{
						avo.doConfigValuesToFormData(v, prop, initDftValue);
					}
				}
				
				propEnableds[prop.name] = (v != null);
				propCollapseds[prop.name] = !prop.required;
			}
			else
			{
				//为null且非数组时，使用默认值
				if(initDftValue && v == null && !prop.array)
					v = avo.clonePropDefaultValue(prop);
				
				v = avo.trimChartConfigValueArray(prop, v);
				
				//不能采用直接转换v的方式，因为enableIf/disableIf功能需要访问标准数据结构
				if(avo.isPropTreeSelectInput(prop))
				{
					propViewValues[prop.name] = avo.encodeConfigValueTreeModel(prop, v);
				}
			}
			
			data[prop.name] = v;
		};
		
		return data;
	};
	
	avo.clonePropDefaultValue = function(prop)
	{
		var dftValue = prop.defaultValue;
		return $.deepClonePlain(dftValue);
	};
	
	avo.isPropTreeSelectInput = function(prop)
	{
		return (prop && prop.inputPayload && prop.inputPayload.treeSelect == true);
	};
	
	//图表配置值转换为树组件Model
	// "v0" -> { v0: true }
	// [ "v0", "v1", ... ] -> { v0: true, v1: true, ... }、[ { v0: true }, { v1: true }, ... ]
	// [ [ "v0", "v1" ], ... ] -> [ { v0: true, v1: true, ... }, ... ]
	avo.encodeConfigValueTreeModel = function(inputProp, value, handleArrayProp)
	{
		handleArrayProp = (handleArrayProp === undefined ? true : handleArrayProp);
		
		if(value == null)
			return value;
		
		var isTreeSelect = avo.isPropTreeSelectInput(inputProp);
		
		if(!isTreeSelect)
			return value;
		
		value = ($.isArray(value) ? value : [ value ]);
		
		var re;
		
		if(handleArrayProp && inputProp.array)
		{
			re = [];
			
			value.forEach((vi) =>
			{
				if(vi == null)
					return;
				
				var rei = {};
				
				if($.isArray(vi))
				{
					vi.forEach((vii) =>
					{
						if(vii != null)
							rei[vii] = true;
					});
				}
				else
				{
					rei[vi] = true;
				}
				
				re.push(rei);
			});
		}
		else
		{
			re = {};
			
			value.forEach((vi) =>
			{
				if(vi != null)
					re[vi] = true;
			});
		}
		
		return re;
	};
	
	//将由avo.configValuesToFormData()函数生成的表单数据转换为图表配置值对象，执行类型转换、选项值限定等
	avo.formDataToConfigValues = function(formData, pluginConfigForm, strictMode, retainNull)
	{
		formData = (formData || {});
		
		var re;
		
		if(pluginConfigForm == null)
			pluginConfigForm = { properties: [] };
		
		pluginConfigForm = avo.toGroupTrimPluginConfigForm(pluginConfigForm);
		re = $.extend(true, {}, formData);
		avo.doFormDataToConfigValues(re, pluginConfigForm, strictMode, retainNull);
		
		return re;
	};
	
	avo.doFormDataToConfigValues = function(formData, propertiesAware, strictMode, retainNull)
	{
		if(formData == null)
			return formData;
		
		var data = formData;
		var props = (propertiesAware == null ? null : propertiesAware.properties);
		props = (props == null ? [] : props);
		var propNameMap = {};
		
		var propEnableIfs = (data[avo.ctrlPropName] ? data[avo.ctrlPropName].propEnableIfs : null);
		
		for(var i=0; i<props.length; i++)
		{
			var prop = props[i];
			var propName = prop.name;
			var v = data[propName];
			
			propNameMap[propName] = true;
			
			if(v == null)
			{
			}
			else if(avo.isPropertiesAware(prop))
			{
				if($.isArray(v))
				{
					for(var j=0; j<v.length; j++)
						avo.doFormDataToConfigValues(v[j], prop, true, false);
				}
				else
				{
					avo.doFormDataToConfigValues(v, prop, true, false);
				}
			}
			else
			{
				v = avo.trimChartConfigValueArray(prop, v);
				v = avo.toChartConfigTypeValue(prop, v);
			}
			
			if(propEnableIfs != null && propEnableIfs[propName] === false)
				v = null;
			
			if(v == null)
			{
				if(retainNull)
				{
					if(v === undefined)
						v = null;
					
					data[propName] = v;
				}
				else
				{
					//不必要时删除null值，以免占用空间
					delete data[propName];
				}
			}
			else
			{
				data[propName] = v;
			}
		};
		
		//严格模式，删除未定义的属性值
		if(strictMode)
		{
			for(var p in data)
			{
				if(propNameMap[p] !== true)
					delete data[p];
			}
		}
		
		delete data[avo.ctrlPropName];
	};
	
	//树组件Model转换为图表配置值，另参考avo.encodeConfigValueTreeModel()函数
	avo.decodeConfigValueTreeModel = function(inputProp, value)
	{
		if(value == null)
			return value;
		
		var isTreeSelect = avo.isPropTreeSelectInput(inputProp);
		
		if(!isTreeSelect)
			return value;
		
		var isArray = inputProp.array;
		var isMultiple = (inputProp.inputPayload && inputProp.inputPayload.multiple == true);
		
		if($.isPlainObject(value))
			value = [ value ];
		
		var re;
		
		if($.isArray(value))
		{
			re = [];
			
			value.forEach((vi) =>
			{
				if(vi == null)
					return;
				
				if($.isPlainObject(vi))
				{
					var rei = [];
					
					for(var vip in vi)
					{
						if(vip != null)
							rei.push(vip);
					}
					
					if(rei.length > 0)
					{
						if(isMultiple)
							re.push(rei);
						else
						{
							re.push(rei[0]);
						}
					}
				}
				else
				{
					re.push(vi);
				}
			});
			
			if(!isArray)
				re = re[0];
		}
		else
			re = value;
		
		return re;
	};
	
	avo.trimChartConfigValueArray = function(inputProp, value)
	{
		if(value == null)
			return value;
		
		if(!$.isArray(value))
		{
			if(inputProp.inputPayload && inputProp.inputPayload.multiple == true)
				value = [ value ];
			
			if(inputProp.array)
				value = [ value ];
		}
		
		return value;
	};
	
	avo.toChartConfigTypeValue = function(inputProp, value)
	{
		var type = inputProp.type;
		
		//对于UI输入框，空字符串应视为null
		if(value === "")
			value = null;
		
		if(value == null)
		{
			return value;
		}
		else if($.isArray(value))
		{
			var re = [];
			
			value.forEach((vi) =>
			{
				vi = avo.toChartConfigTypeValue(inputProp, vi);
				//数组中的null元素应保留
				re.push(vi);
			});
			
			//对于UI输入框，空数组应视为null
			return (re.length > 0 ? re : null);
		}
		else
		{
			if(type == avo.FormPropertyType.BOOLEAN)
			{
				value = (value == true || value === "true" || value === "1" ? true : false);
			}
			else if(type == avo.FormPropertyType.INTEGER)
			{
				value = $.parseIntWithDefault(value, null);
			}
			else if(type == avo.FormPropertyType.NUMBER)
			{
				value = $.parseToNumber(value);
				value = (isNaN(value) ? null : value);
			}
			
			if(value != null)
			{
				//应将值限定为待选值集合内，比如图表插件升级后inputPayload有所删减，那么这里的旧值应删除
				var inputPayload = inputProp.inputPayload;
				var payloadOptions = (inputPayload && inputPayload.options ? inputPayload.options : null);
				var isTreeSelect = avo.isPropTreeSelectInput(inputProp);
				
				if(payloadOptions != null && $.isArray(payloadOptions))
				{
					if(isTreeSelect)
					{
						if($.findTreeArrayById(payloadOptions, value, "key") == null)
							value = null;
					}
					else
					{
						if($.inArrayById(payloadOptions, value, "value") < 0)
							value = null;
					}
				}
			}
			
			return value;
		}
	};
	
	avo.setFormConfigValues = function(configValues, initDftValue)
	{
		var pm = po.vuePageModel();
		var pluginConfigForm = pm.avoModel.pluginConfigForm;
		var formData = pm.avoModel.formData;
		
		for(var p in formData)
			delete formData[p];
		
		avo.configValuesToFormData(configValues, formData, pluginConfigForm, initDftValue);
	};
	
	avo.clearFormData = function()
	{
		avo.setFormConfigValues({}, false);
	};
	
	avo.clearConfigValuesIfNoConfigForm = function(configValues, pluginConfigForm)
	{
		var re = (configValues || {});
		
		if(pluginConfigForm == null || $.isEmpty(pluginConfigForm.properties))
			re = {};
		
		return re;
	};
	
	avo.moveUpArrayValEle = function(formData, prop, idx)
	{
		var array = formData[prop.name];
		
		if(idx > 0)
		{
			var me = array[idx];
			var prev = array[idx-1];
			array[idx-1] = me;
			array[idx] = prev;
		}
	},
	
	avo.moveDownArrayValEle = function(formData, prop, idx)
	{
		var array = formData[prop.name];
		
		if(idx < (array.length -1))
		{
			var me = array[idx];
			var next = array[idx+1];
			array[idx+1] = me;
			array[idx] = next;
		}
	},
	
	avo.insertArrayValEle = function(formData, prop, idx)
	{
		var array = formData[prop.name];
		
		if(array == null)
			formData[prop.name] = (array = []);
		
		var ele = null;
		var isTreeSelect = false;
		
		if(avo.isPropertiesAware(prop))
		{
			ele = avo.doConfigValuesToFormData({}, prop);
		}
		else
		{
			ele = avo.clonePropDefaultValue(prop);
			isTreeSelect = avo.isPropTreeSelectInput(prop);
		}
		
		//undefined应置为null，因为数组的undefined元素表示空槽，不符合这里的需求
		if(ele === undefined)
			ele = null;
		
		if(idx == null)
			array.push(ele);
		else
			array.splice(idx, 0, ele);
		
		if(isTreeSelect)
		{
			var propViewValues = formData[avo.ctrlPropName].propViewValues;
			var viewArray = propViewValues[prop.name];
			
			if(viewArray == null)
				propViewValues[prop.name] = (viewArray = []);
			
			var treeEle = avo.encodeConfigValueTreeModel(prop, ele, false);
			
			if(idx == null)
				viewArray.push(treeEle);
			else
				viewArray.splice(idx, 0, treeEle);
		}
	},
	
	avo.removeArrayValEle = function(formData, prop, idx)
	{
		var array = formData[prop.name];
		
		if(array == null)
			return;
		
		if(avo.isPropertiesAware(prop))
		{
			po.confirm(
			{
				message: po.i18n.confirmDeleteThisDataAsk,
				accept: function()
				{
					array.splice(idx, 1);
				}
			});
		}
		else
		{
			array.splice(idx, 1);
			
			var isTreeSelect = avo.isPropTreeSelectInput(prop);
			if(isTreeSelect)
			{
				var propViewValues = formData[avo.ctrlPropName].propViewValues;
				var viewArray = propViewValues[prop.name];
				
				if(viewArray != null)
					viewArray.splice(idx, 1);
			}
		}
	};
	
	avo.concatPropNamePath = function(propNamePath, name)
	{
		if($.isTypeNumber(name))
			return (propNamePath ? propNamePath : "") + "["+name+"]";
		else
			return (propNamePath ? propNamePath+"." : "") + $.escapePropPathEle(name);
	};
	
	avo.evalEnableIf = function(rootFormData, formData, enableHandler, parentEnableHandler)
	{
		var enable = avo.doEvalEnableIfWithParent(rootFormData, formData, enableHandler, parentEnableHandler);
		
		var ctrlPropName = this.ctrlPropName;
		var propEnableIfs = formData[ctrlPropName].propEnableIfs;
		var propEnableIfsRaw = null;
		var props = null;
		
		//同步propEnableIfs信息
		//注意：不要在这里执行其他写响应式模型的操作，会导致死循环
		
		//分组
		if(enableHandler.isGroup == true)
		{
			props = (enableHandler.properties || []);
			
			for(var i=0; i<props.length; i++)
			{
				var prop = props[i];
				var propName = prop.name;
				
				if(propEnableIfs[propName] !== enable)
				{
					//必须转化为raw后再执行写操作，不然可能导致响应式死循环
					if(propEnableIfsRaw == null)
						propEnableIfsRaw = po.vueRaw(propEnableIfs);
					
					propEnableIfsRaw[propName] = enable;
				}
			}
		}
		//属性
		else if(enableHandler.name !== undefined)
		{
			var propName = enableHandler.name;
			
			if(propEnableIfs[propName] !== enable)
			{
				//必须转化为raw后再执行写操作，不然可能导致响应式UI死循环
				if(propEnableIfsRaw == null)
					propEnableIfsRaw = po.vueRaw(propEnableIfs);
				
				propEnableIfsRaw[propName] = enable;
			}
		}
		
		return enable;
	};
	
	avo.doEvalEnableIfWithParent = function(rootFormData, formData, enableHandler, parentEnableHandler)
	{
		var enabled = true;
		
		if(parentEnableHandler != null)
			enabled = avo.doEvalEnableIf(rootFormData, formData, parentEnableHandler);
		
		if(enabled)
			enabled = avo.doEvalEnableIf(rootFormData, formData, enableHandler);
		
		return enabled;
	};
	
	avo.doEvalEnableIf = function(rootFormData, formData, enableHandler)
	{
		if(enableHandler == null || enableHandler.evalEnabledFunc == null)
			return true;
		
		//函数体里的this应指向formData
		var re = (enableHandler.evalEnabledFunc.call(formData, rootFormData, formData) !== false);
		return re;
	};
	
	avo.handleEnableObjProp = function(formData, objProp)
	{
		var propName = objProp.name;
		var ctrlPropName = this.ctrlPropName;
		var ctrlObj = formData[ctrlPropName];
		var propEnableds = ctrlObj.propEnableds;
		var propCollapseds = ctrlObj.propCollapseds;
		var propBakValues = ctrlObj.propBakValues;
		
		if(propEnableds[propName])
		{
			if(propBakValues[propName] != null)
				formData[propName] = propBakValues[propName];
			else
				formData[propName] = (objProp.array ? [] : avo.doConfigValuesToFormData({}, objProp));
			
			propCollapseds[propName] = false;
		}
		else
		{
			avo.delAndBakPropValue(formData, propName, propBakValues);
			propEnableds[propName] = false;
			propCollapseds[propName] = true;
		}
	};
	
	avo.delAndBakPropValue = function(formData, propName, propBakValues)
	{
		var value = formData[propName];
		
		if(value !== undefined)
		{
			if(value != null)
				propBakValues[propName] = value;
			
			delete formData[propName];
		}
	};
	
	avo.validateConfigValuesRequired = function(configValues, pluginConfigForm)
	{
		pluginConfigForm = avo.toGroupTrimPluginConfigForm(pluginConfigForm);
		
		var formData = configValues;
		formData = (formData == null ? {} : formData);
		
		var groupProps = (pluginConfigForm.groupProps || []);
		
		for(var i=0; i<groupProps.length; i++)
		{
			var group = groupProps[i];
			var props = (group.properties || []);
			
			for(var j=0; j<props.length; j++)
			{
				var prop = props[j];
				var enable = avo.doEvalEnableIfWithParent(formData, formData, prop, group);
				
				if(enable && prop.required && $.isEmpty(formData[prop.name]))
					return false;
			}
		}
		
		return true;
	};
	
	po.setupChartConfigValuesForm = function(pluginConfigForm, configValues, options)
	{
		options = $.extend(
		{
			submitHandler: null,
			showClearBtn: true,
			buttons: [],
			readonly: false,
			strictSubmitData: false,
			retainDataNullProp: false
		},
		options);
		
		pluginConfigForm = avo.toGroupTrimPluginConfigForm(pluginConfigForm);
		
		var pm = po.vuePageModel();
		pm.avoModel.pluginConfigForm = pluginConfigForm;
		pm.avoModel.buttons = options.buttons;
		pm.avoModel.readonly = options.readonly;
		pm.avoModel.showClearBtn = options.showClearBtn;
		avo.setFormConfigValues(configValues);
		
		var form = po.elementOfId(avo.chartConfigValuesFormEleId, document.body);
		po.setupSimpleForm(form, pm.avoModel.formData,
		{
			submitHandler: function()
			{
				if(options && options.submitHandler)
				{
					var pluginConfigForm = pm.avoModel.pluginConfigForm;
					var data = po.vueRaw(pm.avoModel.formData);
					var configValues = avo.formDataToConfigValues(data, pluginConfigForm, options.strictSubmitData, options.retainDataNullProp);
					options.submitHandler(configValues);
				}
			}
		});
	};
	
	po.vuePageModel(
	{
		avoModel:
		{
			FormPropertyType: avo.FormPropertyType,
			FormPropertyInputType: avo.FormPropertyInputType,
			pluginConfigForm: { groupProps: [] },
			formData: {},
			showClearBtn: true,
			readonly: false,
			buttons: [],
			i18n: po.i18n,
			ctrlPropName: avo.ctrlPropName,
			enableOptions:
			[
				{name: po.i18n.enable, value: true},
				{name: po.i18n.disable, value: false}
			]
		}
	});
	
	po.vueMethod(
	{
		onClearChartConfigValuesForm: function()
		{
			po.confirm(
			{
				message: po.i18n.confirmClearAllChartConfig,
				accept: function()
				{
					avo.clearFormData();
				} 
			});
		}
	});
	
	var inputPropFieldCmp = po.vueDefineComponent(
	{
		name: "dg-input-prop-field",
		props:
		{
			inputProp: { type: Object },
			propNamePath: { type: String },
			formData: { type: Object },
			rootFormData: { type: Object },
			readonly: { type: Boolean },
			propTypeDef: { type: Object },
			propInputTypeDef: { type: Object },
			i18n: { type: Object },
			ctrlPropName: { type: String }
		},
		template:
		`
		<div class="field grid">
			<label :for="inputProp.domId+'.'+propNamePath+'.'+inputProp.name" class="field-label col-12 mb-2"
				:class="{'required-label': inputProp.required}"
				:title="inputProp.descLabel && inputProp.descLabel.value ? inputProp.descLabel.value : null">
				<span>{{inputProp.nameLabel.value}}</span>
				<span class="text-color-secondary text-sm ml-1">{{inputProp.name}}</span>
			</label>
			<div class="field-input col-12">
				<div v-if="inputProp.inputType == propInputTypeDef.SELECT">
					<div class="input p-inputtext p-component flex flex-column gap-1" v-if="inputProp.array">
						<div v-for="(vi, viIdx) in formData[inputProp.name]" class="flex gap-2">
							<div class="flex-grow-1 flex" v-if="inputProp.inputPayload.multiple">
								<p-treeselect v-model="formData[ctrlPropName].propViewValues[inputProp.name][viIdx]" :options="inputProp.inputPayload.options"
									selection-mode="multiple" class="w-full" :placeholder="i18n.none" @change="onTreeSelectChange($event, formData, inputProp)"
									v-if="inputProp.inputPayload.treeSelect == true">
								</p-treeselect>
								<p-multiselect v-model="formData[inputProp.name][viIdx]" :options="inputProp.inputPayload.options"
									option-label="name" option-value="value" :show-clear="true" class="w-full"
									v-else>
								</p-multiselect>
							</div>
							<div class="flex-grow-1 flex" v-else>
								<p-treeselect v-model="formData[ctrlPropName].propViewValues[inputProp.name][viIdx]" :options="inputProp.inputPayload.options"
									class="w-full" :placeholder="i18n.none" @change="onTreeSelectChange($event, formData, inputProp)"
									v-if="inputProp.inputPayload.treeSelect == true">
								</p-treeselect>
								<p-dropdown v-model="formData[inputProp.name][viIdx]" :options="inputProp.inputPayload.options"
									option-label="name" option-value="value" class="flex-grow-1"
									v-else>
								</p-dropdown>
							</div>
							<div class="flex align-items-center gap-1" v-if="!readonly">
								<p-button type="button" icon="pi pi-plus" severity="secondary" outlined
									@click="insertArrayValEle(formData, inputProp, viIdx)">
								</p-button>
								<p-button type="button" icon="pi pi-minus" severity="danger" outlined 
									@click="removeArrayValEle(formData, inputProp, viIdx)">
								</p-button>
							</div>
						</div>
						<div v-if="!readonly">
							<p-button type="button" icon="pi pi-plus" severity="secondary" outlined
								@click="insertArrayValEle(formData, inputProp)">
							</p-button>
						</div>
					</div>
					<div v-else-if="inputProp.inputPayload.multiple">
						<p-treeselect v-model="formData[ctrlPropName].propViewValues[inputProp.name]" :options="inputProp.inputPayload.options"
							selection-mode="multiple" class="input w-full" :placeholder="i18n.none" @change="onTreeSelectChange($event, formData, inputProp)"
							v-if="inputProp.inputPayload.treeSelect == true">
						</p-treeselect>
						<p-multiselect v-model="formData[inputProp.name]" :options="inputProp.inputPayload.options"
							option-label="name" option-value="value" :show-clear="true" class="input w-full"
							v-else>
						</p-multiselect>
					</div>
					<div v-else>
						<p-treeselect v-model="formData[ctrlPropName].propViewValues[inputProp.name]" :options="inputProp.inputPayload.options"
							class="input w-full" :placeholder="i18n.none" @change="onTreeSelectChange($event, formData, inputProp)"
							 v-if="inputProp.inputPayload.treeSelect == true">
						</p-treeselect>
						<p-dropdown v-model="formData[inputProp.name]" :options="inputProp.inputPayload.options"
							option-label="name" option-value="value" :show-clear="!inputProp.required" class="input w-full"
							v-else>
						</p-dropdown>
					</div>
				</div>
				<div v-else-if="inputProp.inputType == propInputTypeDef.COLOR">
					<div class="input p-inputtext p-component flex flex-column gap-1" v-if="inputProp.array">
						<div v-for="(vi, viIdx) in formData[inputProp.name]" class="flex gap-2">
							<div class="flex-grow-1 flex gap-1">
								<p-inputtext v-model="formData[inputProp.name][viIdx]" type="text"
									class="flex-grow-1" maxlength="200">
								</p-inputtext>
								<p-button type="button" :style="{'background-color': formData[inputProp.name][viIdx]}"
									class="palette-btn surface-border"
									@click="showPalettePanel($event, formData[inputProp.name], viIdx)"></p-button>
							</div>
							<div class="flex align-items-center gap-1" v-if="!readonly">
								<p-button type="button" icon="pi pi-plus" severity="secondary" outlined
									@click="insertArrayValEle(formData, inputProp, viIdx)">
								</p-button>
								<p-button type="button" icon="pi pi-minus" severity="danger" outlined
									@click="removeArrayValEle(formData, inputProp, viIdx)">
								</p-button>
							</div>
						</div>
						<div v-if="!readonly">
							<p-button type="button" icon="pi pi-plus" severity="secondary" outlined
								@click="insertArrayValEle(formData, inputProp)">
							</p-button>
						</div>
					</div>
					<div class="flex gap-1" v-else>
						<p-inputtext v-model="formData[inputProp.name]" type="text"
							class="input flex-grow-1" maxlength="200">
						</p-inputtext>
						<p-button type="button" :style="{'background-color': formData[inputProp.name]}" class="palette-btn surface-border"
							@click="showPalettePanel($event, formData, inputProp.name)"></p-button>
					</div>
				</div>
				<div v-else-if="inputProp.inputType == propInputTypeDef.RADIO || inputProp.inputType == propInputTypeDef.CHECKBOX">
					<div class="input p-inputtext p-component flex flex-column gap-1" v-if="inputProp.array">
						<div v-for="(vi, viIdx) in formData[inputProp.name]" class="flex gap-2">
							<div class="flex-grow-1 p-inputtext p-component p-2 flex gap-3">
								<div v-for="(opt, optIdx) in inputProp.inputPayload.options" class="inline-flex align-items-center gap-1">
									<p-radiobutton :input-id="inputProp.domId+'.'+propNamePath+'.'+inputProp.name+'.'+optIdx+'.'+viIdx"
										:value="opt.value" v-model="formData[inputProp.name][viIdx]"
										 v-if="inputProp.inputType == propInputTypeDef.RADIO">
									</p-radiobutton>
									<p-checkbox :input-id="inputProp.domId+'.'+propNamePath+'.'+inputProp.name+'.'+optIdx+'.'+viIdx"
										:value="opt.value" v-model="formData[inputProp.name][viIdx]"
										v-else>
									</p-checkbox>
									<label :for="inputProp.domId+'.'+propNamePath+'.'+inputProp.name+'.'+optIdx+'.'+viIdx">{{opt.name}}</label>
								</div>
							</div>
							<div class="flex align-items-center gap-1" v-if="!readonly">
								<p-button type="button" icon="pi pi-plus" severity="secondary" outlined
									@click="insertArrayValEle(formData, inputProp, viIdx)">
								</p-button>
								<p-button type="button" icon="pi pi-minus" severity="danger" outlined
									@click="removeArrayValEle(formData, inputProp, viIdx)">
								</p-button>
							</div>
						</div>
						<div v-if="!readonly">
							<p-button type="button" icon="pi pi-plus" severity="secondary" outlined
								@click="insertArrayValEle(formData, inputProp)">
							</p-button>
						</div>
					</div>
					<div class="input p-inputtext p-component p-2 flex gap-3" v-else>
						<div v-for="(opt, optIdx) in inputProp.inputPayload.options" class="inline-flex align-items-center gap-1">
							<p-radiobutton :input-id="inputProp.domId+'.'+propNamePath+'.'+inputProp.name+'.'+optIdx"
								:value="opt.value" v-model="formData[inputProp.name]"
								 v-if="inputProp.inputType == propInputTypeDef.RADIO">
							</p-radiobutton>
							<p-checkbox :input-id="inputProp.domId+'.'+propNamePath+'.'+inputProp.name+'.'+optIdx"
								:value="opt.value" v-model="formData[inputProp.name]"
								v-else>
							</p-checkbox>
							<label :for="inputProp.domId+'.'+propNamePath+'.'+inputProp.name+'.'+optIdx">{{opt.name}}</label>
						</div>
					</div>
				</div>
				<div v-else>
					<div class="input p-inputtext p-component flex flex-column gap-1" v-if="inputProp.array">
						<div v-for="(vi, viIdx) in formData[inputProp.name]" class="flex gap-2">
							<p-inputnumber v-model="formData[inputProp.name][viIdx]" :use-grouping="false" class="flex-grow-1"
								v-if="inputProp.type == propTypeDef.INTEGER">
							</p-inputnumber>
							<p-inputnumber v-model="formData[inputProp.name][viIdx]" :use-grouping="false" class="flex-grow-1"
								:min-fraction-digits="0" :max-fraction-digits="20"
								v-else-if="inputProp.type == propTypeDef.NUMBER">
							</p-inputnumber>
							<p-textarea v-model="formData[inputProp.name][viIdx]" class="flex-grow-1"
								v-else-if="inputProp.inputType == propInputTypeDef.TEXTAREA">
							</p-textarea>
							<p-inputtext v-model="formData[inputProp.name][viIdx]" type="text" class="flex-grow-1"
								v-else>
							</p-inputtext>
							<div class="flex align-items-center gap-1" v-if="!readonly">
								<p-button type="button" icon="pi pi-plus" severity="secondary" outlined
									@click="insertArrayValEle(formData, inputProp, viIdx)">
								</p-button>
								<p-button type="button" icon="pi pi-minus" severity="danger" outlined
									@click="removeArrayValEle(formData, inputProp, viIdx)">
								</p-button>
							</div>
						</div>
						<div v-if="!readonly">
							<p-button type="button" icon="pi pi-plus" severity="secondary" outlined
								@click="insertArrayValEle(formData, inputProp)">
							</p-button>
						</div>
					</div>
					<p-inputnumber :input-id="inputProp.domId+'.'+propNamePath+'.'+inputProp.name"
						v-model="formData[inputProp.name]" :use-grouping="false" class="input w-full"
						v-else-if="inputProp.type == propTypeDef.INTEGER">
					</p-inputnumber>
					<p-inputnumber :input-id="inputProp.domId+'.'+propNamePath+'.'+inputProp.name"
						v-model="formData[inputProp.name]" :use-grouping="false" class="input w-full"
						:min-fraction-digits="0" :max-fraction-digits="20"
						v-else-if="inputProp.type == propTypeDef.NUMBER">
					</p-inputnumber>
					<p-textarea :id="inputProp.domId+'.'+propNamePath+'.'+inputProp.name"
						v-model="formData[inputProp.name]" class="input w-full"
						v-else-if="inputProp.inputType == propInputTypeDef.TEXTAREA">
					</p-textarea>
					<p-inputtext :id="inputProp.domId+'.'+propNamePath+'.'+inputProp.name"
						v-model="formData[inputProp.name]" type="text" class="input w-full"
						v-else>
					</p-inputtext>
				</div>
	        	<div class="validate-msg">
	        		<input :name="concatPropNamePath(propNamePath, inputProp.name)" type="text" class="validate-proxy"
	        			:detailrequired="inputProp.required ? 'true' : null"
	        			:integer="inputProp.type == propTypeDef.INTEGER ? 'true' : null"
	        			:number="inputProp.type == propTypeDef.NUMBER ? 'true' : null"
	        			:minlength="inputProp.type == propTypeDef.STRING && inputProp.additions && inputProp.additions['minLength'] != null ? inputProp.additions['minLength'] : null"
	        			:maxlength="inputProp.type == propTypeDef.STRING && inputProp.additions && inputProp.additions['maxLength'] != null ? inputProp.additions['maxLength'] : null"
	        			:min="(inputProp.type == propTypeDef.INTEGER || inputProp.type == propTypeDef.NUMBER) && inputProp.additions && inputProp.additions['min'] != null ? inputProp.additions['min'] : null"
	        			:max="(inputProp.type == propTypeDef.INTEGER || inputProp.type == propTypeDef.NUMBER) && inputProp.additions && inputProp.additions['max'] != null ? inputProp.additions['max'] : null"
	        			:minsize="inputProp.array && inputProp.additions && inputProp.additions['minSize'] != null ? inputProp.additions['minSize'] : null"
	        			:maxsize="inputProp.array && inputProp.additions && inputProp.additions['maxSize'] != null ? inputProp.additions['maxSize'] : null" />
	        	</div>
			</div>
		</div>
		`,
		
		methods:
		{
			concatPropNamePath: function(propNamePath, name)
			{
				return avo.concatPropNamePath(propNamePath, name);
			},
			insertArrayValEle: function(formData, prop, idx)
			{
				avo.insertArrayValEle(formData, prop, idx);
			},
			removeArrayValEle: function(formData, prop, idx)
			{
				avo.removeArrayValEle(formData, prop, idx);
			},
			showPalettePanel: function(e, modelObj, modelProp)
			{
				po.showPalettePanel(e, modelObj, modelProp);
			},
			onTreeSelectChange: function(e, formData, prop)
			{
				var propName = prop.name;
				var ctrlPropName = this.ctrlPropName;
				var ctrlObj = formData[ctrlPropName];
				var propViewValues = ctrlObj.propViewValues;
				formData[propName] = avo.decodeConfigValueTreeModel(prop, propViewValues[propName]);
			}
		},
		
		components: $.vueComponents()
	});
	
	var objPropFieldCmpDepends = $.vueComponents();
	objPropFieldCmpDepends["dg-input-prop-field"] = inputPropFieldCmp;

	po.vueDefineComponent(
	{
		name: "dg-obj-prop-field",
		props:
		{
			objProp: { type: Object },
			propNamePath: { type: String },
			formData: { type: Object },
			rootFormData: { type: Object },
			readonly: { type: Boolean },
			propTypeDef: { type: Object },
			propInputTypeDef: { type: Object },
			i18n: { type: Object },
			ctrlPropName: { type: String },
			enableOptions: { type: Object }
		},
		template:
		`
		<div class="flex flex-column">
			<p-panel :toggleable="false" v-for="(group, groupIdx) in objProp.groupProps" class="sm-header-y-padding panel-icon-align-center invalid-indicator"
				:class="{'disable-p-panel': group.virtual, 'mb-3': !group.virtual, 'hide-panel-content': (!group.virtual && group.groupCollapsed), 'hidden': (evalEnableIf(rootFormData, formData, group) == false)}">
				<template #header>
					<label class='font-bold color-for-invalid'>{{group.nameLabel.value}}</label>
				</template>
				<template #icons>
					<div class="inline-flex align-items-center gap-3 text-sm">
						<p-button type="button" :icon="group.groupCollapsed ? 'pi pi-angle-down' : 'pi pi-angle-up'"
							severity="secondary" text rounded @click="onToggleGroupPanel(group)" v-if="!group.virtual">
						</p-button>
					</div>
				</template>
				<div>
					<div v-for="(prop, propIdx) in group.properties">
						<div v-if="evalEnableIf(rootFormData, formData, prop, group)">
							<div class="mb-3" v-if="prop.type == propTypeDef.OBJECT && prop.array">
								<p-panel :toggleable="false"
									class="sm-header-y-padding panel-icon-align-center invalid-indicator" :class="{'hide-panel-content': formData[ctrlPropName].propCollapseds[prop.name]}">
									<template #header>
										<label :class="{'required-label': prop.required, 'opacity-60': formData[prop.name] == null}" class="color-for-invalid">
											<span class="font-bold">{{prop.nameLabel.value}}</span>
											<span class="text-color-secondary text-sm ml-1">
												{{formData[prop.name] == null ? '' : formData[prop.name].length}}
											</span>
										</label>
									</template>
									<template #icons>
										<div class="inline-flex align-items-center gap-3 text-sm">
											<p-inputswitch v-model="formData[ctrlPropName].propEnableds[prop.name]" :title="i18n.activeOrClear"
												@change="onEnableObjProp(formData, prop)" v-if="!readonly && !prop.required">
											</p-inputswitch>
											<p-button type="button" :icon="formData[ctrlPropName].propCollapseds[prop.name] ? 'pi pi-angle-down' : 'pi pi-angle-up'"
												severity="secondary" text rounded @click="onToggleObjPropPanel(formData, prop)" :disabled="formData[prop.name] == null">
											</p-button>
										</div>
									</template>
									<div class="flex flex-column gap-3 mb-2"  v-if="formData[prop.name] != null">
										<p-panel v-for="(propDataEle, propDataEleIdx) in formData[prop.name]"
											:toggleable="true" class="sm-header-y-padding panel-icon-align-center invalid-indicator">
											<template #header>
												<label class="color-for-invalid">
													<span class="font-bold">{{prop.nameLabel.value}}</span>
													<span class="text-color-secondary text-sm ml-1">
														{{(propDataEleIdx+1)+'/'+formData[prop.name].length}}
													</span>
												</label>
											</template>
											<template #icons>
												<div class="inline-flex gap-1 mx-2 text-sm" v-if="!readonly">
													<p-button type="button" severity="secondary" icon="pi pi-arrow-up" :title="i18n.moveUp"
														@click="moveUpArrayValEle(formData, prop, propDataEleIdx)">
													</p-button>
													<p-button type="button" severity="secondary" icon="pi pi-arrow-down" :title="i18n.moveDown"
														@click="moveDownArrayValEle(formData, prop, propDataEleIdx)">
													</p-button>
													<p-button type="button" severity="secondary" icon="pi pi-plus" :title="i18n.insert"
														@click="insertArrayValEle(formData, prop, propDataEleIdx)">
													</p-button>
													<p-button type="button" severity="danger" icon="pi pi-minus" :title="i18n.del"
														@click="removeArrayValEle(formData, prop, propDataEleIdx)">
													</p-button>
												</div>
											</template>
											<dg-obj-prop-field :obj-prop="prop" :prop-name-path="concatPropNamePath(concatPropNamePath(propNamePath, prop.name), propDataEleIdx)"
												:form-data="propDataEle" :root-form-data="rootFormData" :prop-type-def="propTypeDef" :prop-input-type-def="propInputTypeDef" :i18n="i18n"
												:ctrl-prop-name="ctrlPropName" :enable-options="enableOptions"
												:readonly="readonly">
											</dg-obj-prop-field>
										</p-panel>
										<div>
											<div class="text-sm" v-if="!readonly">
												<p-button type="button" icon="pi pi-plus" :label="prop.nameLabel.value"
													severity="secondary" @click="insertArrayValEle(formData, prop)">
												</p-button>
											</div>
											<div class="field-input" v-if="group.required">
									        	<div class="validate-msg">
									        		<input :name="prop.name" required type="text" class="validate-proxy" />
									        	</div>
								        	</div>
							        	</div>
						        	</div>
						        </p-panel>
							</div>
							<div class="mb-3" v-else-if="prop.type == propTypeDef.OBJECT">
								<p-panel :toggleable="false" class="sm-header-y-padding panel-icon-align-center invalid-indicator"
									:class="{'hide-panel-content': formData[ctrlPropName].propCollapseds[prop.name]}">
									<template #header>
										<label class="font-bold color-for-invalid" :class="{'required-label': prop.required, 'opacity-60': formData[prop.name] == null}">
											{{prop.nameLabel.value}}
										</label>
									</template>
									<template #icons>
										<div class="inline-flex align-items-center gap-3 text-sm">
											<p-inputswitch v-model="formData[ctrlPropName].propEnableds[prop.name]" :title="i18n.activeOrClear"
												@change="onEnableObjProp(formData, prop)" v-if="!readonly && !prop.required">
											</p-inputswitch>
											<p-button type="button" :icon="formData[ctrlPropName].propCollapseds[prop.name] ? 'pi pi-angle-down' : 'pi pi-angle-up'"
												severity="secondary" text rounded @click="onToggleObjPropPanel(formData, prop)" :disabled="formData[prop.name] == null">
											</p-button>
										</div>
									</template>
									<dg-obj-prop-field :obj-prop="prop" :prop-name-path="concatPropNamePath(propNamePath, prop.name)"
										:form-data="formData[prop.name]" :root-form-data="rootFormData"
										:prop-type-def="propTypeDef" :prop-input-type-def="propInputTypeDef" :i18n="i18n"
										:ctrl-prop-name="ctrlPropName" :enable-options="enableOptions"
										:readonly="readonly" v-if="formData[prop.name] != null">
									</dg-obj-prop-field>
								</p-panel>
							</div>
							<div v-else>
								<dg-input-prop-field :input-prop="prop" :prop-name-path="propNamePath"
									:form-data="formData" :root-form-data="rootFormData"
									:prop-type-def="propTypeDef" :prop-input-type-def="propInputTypeDef" :i18n="i18n"
									:ctrl-prop-name="ctrlPropName" :readonly="readonly">
								</dg-input-prop-field>
							</div>
						</div>
					</div>
				</div>
			</p-panel>
		</div>
		`,
		
		methods:
		{
			concatPropNamePath: function(propNamePath, name)
			{
				return avo.concatPropNamePath(propNamePath, name);
			},
			moveUpArrayValEle: function(formData, prop, idx)
			{
				avo.moveUpArrayValEle(formData, prop, idx);
			},
			moveDownArrayValEle: function(formData, prop, idx)
			{
				avo.moveDownArrayValEle(formData, prop, idx);
			},
			insertArrayValEle: function(formData, prop, idx)
			{
				avo.insertArrayValEle(formData, prop, idx);
			},
			removeArrayValEle: function(formData, prop, idx)
			{
				avo.removeArrayValEle(formData, prop, idx);
			},
			onEnableObjProp: function(formData, objProp)
			{
				avo.handleEnableObjProp(formData, objProp);
			},
			onToggleObjPropPanel: function(formData, prop)
			{
				var ctrlPropName = this.ctrlPropName;
				var ctrlObj = formData[ctrlPropName];
				var propCollapseds = ctrlObj.propCollapseds;
				propCollapseds[prop.name] = !propCollapseds[prop.name];
			},
			onToggleGroupPanel: function(group)
			{
				group.groupCollapsed = !group.groupCollapsed;
			},
			evalEnableIf: function(rootFormData, formData, enableHandler, parentEnableHandler)
			{
				return avo.evalEnableIf(rootFormData, formData, enableHandler, parentEnableHandler);
			}
		},
		
		components: objPropFieldCmpDepends
	});
};

//填充chart_form.ftl
$.inflateChartForm = function(po)
{
	//内置简化数据标记字段匹配器定义
	//匹配器结构规范：
	//{ inTypes: null、"..."、[ "...", ... ], notInTypes: null、"..."、[ "...", ... ], array: null、true、false, evaluated: null、true、false }
	//只要其中任一属性不匹配，则表示匹配器不通过，null表示此属性匹配通过
	po.DATASIGN_FIELD_MATCHERS =
	{
		//类型
		"s": { inTypes: [ po.DataSetFieldType.STRING ] },
		"n": { inTypes: [ po.DataSetFieldType.NUMBER ] },
		"i": { inTypes: [ po.DataSetFieldType.INTEGER ] },
		"d": { inTypes: [ po.DataSetFieldType.DATE ] },
		"t": { inTypes: [ po.DataSetFieldType.TIME ] },
		"ts": { inTypes: [ po.DataSetFieldType.TIMESTAMP ] },
		"b": { inTypes: [ po.DataSetFieldType.BOOLEAN ] },
		"o": { inTypes: [ po.DataSetFieldType.OBJECT ] },
		"u": { inTypes: [ po.DataSetFieldType.UNKNOWN ] },
		//类型：基本类
		"P": { notInTypes: [ po.DataSetFieldType.OBJECT, po.DataSetFieldType.UNKNOWN ] },
		//类型：数值类
		"N": { inTypes: [ po.DataSetFieldType.NUMBER, po.DataSetFieldType.INTEGER ] },
		//类型：日期类
		"D": { inTypes: [ po.DataSetFieldType.DATE, po.DataSetFieldType.TIME, po.DataSetFieldType.TIMESTAMP ] },
		//数组
		"array": { array: true },
		//计算字段
		"evaluated": { evaluated: true }
	};
	
	po.inSaveAndShowAction = function(val)
	{
		if(val === undefined)
			return (po._inSaveAndShowAction == true);
		
		po._inSaveAndShowAction = val;
	};
	
	po.prepareSubmitData = function()
	{
		var fm = po.vueFormModel();
		var data = $.extend({}, po.vueRaw(fm));
		
		for(var p in data)
		{
			var v = data[p];
			
			if(p == "dataSetBindVOs")
			{
				var dsbs = $.extend([], v);
				v = dsbs;
				
				for(var i=0; i<dsbs.length; i++)
				{
					dsbs[i] = $.extend({}, dsbs[i]);
					
					po.restoreDataSetBind(dsbs[i], data.pluginVo);
					dsbs[i].summaryDataSetEntity = dsbs[i].dataSet;
					dsbs[i].dataSet = undefined;
				}
			}
			else
				v = $.deepClonePlain(v);
			
			data[p] = v;
		}

		var pm = po.vuePageModel();
		if(pm.enableResultDataFormat)
			data.resultDataFormat = po.vueRaw(pm.resultDataFormat);
		else
			data.resultDataFormat = undefined;
		
		data.configValues = po.avo.clearConfigValuesIfNoConfigForm(data.configValues, data.pluginVo.configForm);
		data.pluginVo = (data.pluginVo ? { id: data.pluginVo.id } : null);
		
		return data;
	};
	
	po.beforeSubmitForm = function(action)
	{
		action.options.saveAndShowAction = po.inSaveAndShowAction();
	};
	
	po.isEmptyPluginConfigForm = function(plugin)
	{
		return (!plugin || !plugin.configForm || !plugin.configForm.properties || plugin.configForm.properties.length==0);
	};
	
	po.validateDataSetBindDataSign = function(chart)
	{
		var chartPlugin = chart.pluginVo;

		if(chartPlugin == null || $.isEmpty(chartPlugin.dataSigns))
			return true;
		
		var dataSetBinds = (chart.dataSetBindVOs || []);
		
		var requiredDataSetSigns = po.findCandidateDataSignsForDataSet(chartPlugin);
		requiredDataSetSigns = po.getRequiredDataSigns(requiredDataSetSigns);
		
		for(var i=0; i<requiredDataSetSigns.length; i++)
		{
			var requiredSign = requiredDataSetSigns[i];
			var contains = false;
			
			for(var j=0; j<dataSetBinds.length; j++)
			{
				var dsb = dataSetBinds[j];
				
				if(dsb.attachment == true)
					continue;
				
				if($.inArrayById(dsb.bindDataSigns, requiredSign.fullname, "fullname") > -1)
				{
					contains = true;
					break;
				}
			}
			
			if(!contains)
			{
				var invalidInfo = { type: "dataset", dataSign: requiredSign };
				return invalidInfo;
			}
		}
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dsb = dataSetBinds[i];
			
			if(dsb.attachment == true)
				continue;
			
			var invalidInfo = po.validateDataSetFieldDataSign(chartPlugin, dsb, dsb.fieldNodes);
			
			if(invalidInfo !== true)
				return invalidInfo;
		}
		
		return true;
	};
	
	po.validateDataSetFieldDataSign = function(chartPlugin, dataSetBind, dataSetFieldNodes)
	{
		if(chartPlugin == null || $.isEmpty(chartPlugin.dataSigns) || dataSetBind == null)
		{
			return true;
		}
		
		var fieldParentNode = ($.isEmpty(dataSetFieldNodes) ? null : dataSetFieldNodes[0].parentNode);
		var requiredDataSigns = po.findCandidateDataSignsForField(chartPlugin, dataSetBind, fieldParentNode);
		requiredDataSigns = po.getRequiredDataSigns(requiredDataSigns);
		
		for(var i=0; i<requiredDataSigns.length; i++)
		{
			var requiredDataSign = requiredDataSigns[i];
			var bind = false;
			
			for(var j=0; j<dataSetFieldNodes.length; j++)
			{
				var dataSetFieldNode = dataSetFieldNodes[j];
				var bindDataSigns = (dataSetFieldNode.bindDataSigns || []);
				
				if($.inArrayById(bindDataSigns, requiredDataSign.fullname, "fullname") >= 0)
				{
					bind = true;
					break;
				}
			}
			
			if(!bind)
			{
				var invalidInfo = {  type: "field", dataSet: dataSetBind.dataSet, dataSign: requiredDataSign };
				return invalidInfo;
			}
		}
		
		for(var i=0; i<dataSetFieldNodes.length; i++)
		{
			var dataSetFieldNode = dataSetFieldNodes[i];

			if(dataSetFieldNode.children)
			{
				var invalidInfo = po.validateDataSetFieldDataSign(chartPlugin, dataSetBind, dataSetFieldNode.children);

				if(invalidInfo !== true)
					return invalidInfo;
			}
		}
		
		return true;
	};
	
	po.getRequiredDataSigns = function(dataSigns)
	{
		var re = [];
		
		if(!dataSigns)
			return re;
		
		$.each(dataSigns, function(idx, dataSign)
		{
			if(dataSign.required == true)
				re.push(dataSign);
		});
		
		return re;
	};
	
	po.hasDataSetSigned = function(dataSetBinds, dataSign)
	{
		dataSetBinds = (dataSetBinds == null ? [] : dataSetBinds);
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var bindDataSigns = dataSetBinds[i].bindDataSigns;
			
			if($.inArrayById(bindDataSigns, dataSign.fullname, "fullname") > -1)
				return true;
		}
		
		return false;
	};
	
	po.hasSubDataSetFieldSigned = function(parentNode, dataSign)
	{
		var subNodes = null;
		
		//parentNode是数据集绑定
		if(parentNode.fieldNodes != null)
			subNodes = parentNode.fieldNodes;
		//parentNode是数据集字段节点
		else if(parentNode.children != null)
			subNodes = parentNode.children;
		
		if(subNodes == null)
			return false;
		
		for(var i=0; i<subNodes.length; i++)
		{
			var subNode = subNodes[i];
			if($.inArrayById(subNode.bindDataSigns, dataSign.fullname, "fullname") > -1)
				return true;
		}
		
		return false;
	};
	
	po.assembleDataSetBinds = function(chart)
	{
		var dsbs = (chart.dataSetBindVOs || []);
		$.each(dsbs, function(idx, dsb)
		{
			po.assembleDataSetBind(dsb, chart.pluginVo);
		});
	};
	
	po.assembleDataSetBind = function(dataSetBind, chartPlugin)
	{
		var fields = (dataSetBind.dataSet ? dataSetBind.dataSet.fields : []);
		var dataSigns = (chartPlugin && chartPlugin.dataSigns ? chartPlugin.dataSigns : []);
		
		dataSetBind.fieldNodes = (po.dataSetFieldsToTreeNodes(fields, dataSetBind, chartPlugin) || []);
		dataSetBind.bindDataSigns = [];
		
		if(dataSetBind.dataSetSigns)
		{
			$.each(dataSetBind.dataSetSigns, function(idx, signName)
			{
				var dataSign = po.findDataSignByFullname(dataSigns, signName, false);
				if(dataSign != null)
					dataSetBind.bindDataSigns.push(dataSign);
			});
		}
	};
	
	po.dataSetFieldsToTreeNodes = function(fields, dataSetBind, chartPlugin, parentNode)
	{
		if(fields == null)
			return null;
		
		var re = [];
		
		for(var i=0; i<fields.length; i++)
		{
			var field = fields[i];
			re[i] = po.dataSetFieldToTreeNode(field, dataSetBind, chartPlugin, parentNode);
		}
		
		return re;
	};
	
	po.dataSetFieldToTreeNode = function(field, dataSetBind, chartPlugin, parentNode)
	{
		var dataSigns = (chartPlugin && chartPlugin.dataSigns ? chartPlugin.dataSigns : []);
		
		var node =
		{
			key: field.fullname, data: field, leaf: po.isLeafDataSetField(field), parentNode: parentNode,
			fullname: field.fullname,
			bindDataSigns: [], alias: (dataSetBind.fieldAliases ? dataSetBind.fieldAliases[field.fullname] : null),
			order: (dataSetBind.fieldOrders ? dataSetBind.fieldOrders[field.fullname] : null)
		};
		
		var bindDataSigns = [];
		
		var fieldSigns = (dataSetBind.fieldSigns ? (dataSetBind.fieldSigns[field.fullname] || []) : []);
		$.each(fieldSigns, function(fsIdx, signName)
		{
			var dataSign = po.findDataSignByFullname(dataSigns, signName);
			if(dataSign != null && po.isDataSignMatchesField(dataSign, field))
				bindDataSigns.push(dataSign);
		});
		
		node.bindDataSigns = bindDataSigns;
		
		if(!node.leaf)
			node.children = po.dataSetFieldsToTreeNodes(field.fields, dataSetBind, chartPlugin, node);
		
		return node;
	};
	
	po.isLeafDataSetField = function(field)
	{
		return $.isEmpty(field.fields);
	};

	po.restoreDataSetBinds = function(chart)
	{
		var dsbs = (chart.dataSetBindVOs || []);
		$.each(dsbs, function(idx, dsb)
		{
			po.restoreDataSetBind(dsb, chart.pluginVo);
		});
	};
	
	po.restoreDataSetBind = function(dataSetBind, chartPlugin)
	{
		po.restoreDataSetFields(dataSetBind.fieldNodes, dataSetBind, chartPlugin);
		
		var dataSigns = (chartPlugin && chartPlugin.dataSigns ? chartPlugin.dataSigns : []);
		dataSetBind.dataSetSigns = [];
		
		if(dataSetBind.bindDataSigns)
		{
			$.each(dataSetBind.bindDataSigns, function(idx, bindDataSign)
			{
				var dataSign = po.findDataSignByFullname(dataSigns, bindDataSign.fullname, false);
				if(dataSign != null)
					dataSetBind.dataSetSigns.push(bindDataSign.fullname);
			});
		}
		
		dataSetBind.fieldNodes = undefined;
		dataSetBind.bindDataSigns = undefined;
	};
	
	po.restoreDataSetFields = function(fieldNodes, dataSetBind, chartPlugin)
	{
		if(fieldNodes == null)
			return;
		
		var dataSigns = (chartPlugin && chartPlugin.dataSigns ? chartPlugin.dataSigns : []);
		dataSetBind.fieldSigns = (dataSetBind.fieldSigns || {});
		dataSetBind.fieldAliases = (dataSetBind.fieldAliases || {});
		dataSetBind.fieldOrders = (dataSetBind.fieldOrders || {});
		
		$.each(fieldNodes, function(idx, fieldNode)
		{
			var bindDataSigns = (fieldNode.bindDataSigns || []);
			
			var fieldSigns = [];
			$.each(bindDataSigns, function(fsIdx, bindDataSign)
			{
				if(po.findDataSignByFullname(dataSigns, bindDataSign.fullname) != null)
					fieldSigns.push(bindDataSign.fullname);
			});
			
			if(fieldSigns.length > 0)
				dataSetBind.fieldSigns[fieldNode.fullname] = fieldSigns;
			else
				dataSetBind.fieldSigns[fieldNode.fullname] = undefined;
			
			if(!$.isEmpty(fieldNode.alias))
				dataSetBind.fieldAliases[fieldNode.fullname] = fieldNode.alias;
			
			if(!$.isEmpty(fieldNode.order))
			{
				var order = parseInt(fieldNode.order);
				dataSetBind.fieldOrders[fieldNode.fullname] = (isNaN(order) ? undefined : order);
			}
			
			if(fieldNode.children)
				po.restoreDataSetFields(fieldNode.children, dataSetBind, chartPlugin);
		});
	};
	
	po.assemblePlugin = function(plugin)
	{
		if(!plugin)
			return;
		
		plugin.dataSigns = po.assemblePluginDataSigns(plugin.dataSigns);
	};
	
	po.assemblePluginDataSigns = function(dataSigns, parent)
	{
		dataSigns = (dataSigns ? dataSigns : []);
		parent = (parent == null ? null : parent);
		
		for(var i=0; i<dataSigns.length; i++)
		{
			var dsn = dataSigns[i];
			dsn.extLabel = (parent && parent.extLabel ?
					(parent.extLabel + "." + po.formatDataSignLabel(dsn)) : po.formatDataSignLabel(dsn));
			
			if(dsn.children)
				po.assemblePluginDataSigns(dsn.children, dsn);
		}
		
		return dataSigns;
	};
	
	po.pluginHasDataSetSign = function(plugin)
	{
		var dataSigns = (plugin ? plugin.dataSigns : null);
		
		if(!dataSigns)
			return false;
		
		for(var i=0; i<dataSigns.length; i++)
		{
			if(po.isDataSignTargetDataSet(dataSigns[i]))
			{
				return true;
			}
		}
		
		return false;
	};
	
	po.findDataSignByFullname = function(dataSigns, fullname, deepSearch)
	{
		deepSearch = (deepSearch == null ? true : deepSearch);
		
		if(!dataSigns)
			return null;
		
		//应该先广度搜索、再深度搜索
		for(var i=0; i<dataSigns.length; i++)
		{
			if(dataSigns[i].fullname == fullname)
				return dataSigns[i];
		}
		
		for(var i=0; i<dataSigns.length; i++)
		{
			if(dataSigns[i].children)
			{
				var d = po.findDataSignByFullname(dataSigns[i].children, fullname);
				if(d != null)
					return d;
			}
		}
		
		return null;
	};

	po.findCandidateDataSignsForDataSet = function(chartPlugin)
	{
		var re = [];
		var dataSigns = (chartPlugin ? chartPlugin.dataSigns : null);
		
		if(!dataSigns)
			return re;
		
		for(var i=0; i<dataSigns.length; i++)
		{
			var dsi = dataSigns[i];
			
			if(po.isDataSignTargetDataSet(dsi))
			{
				re.push(dsi);
			}
		}
		
		return re;
	};
	
	po.findCandidateDataSignsForField = function(chartPlugin, dataSetBind, fieldParentNode)
	{
		var re = [];
		
		var parentBindDataSigns = null;
		
		//子级字段只能使用父字段绑定数据标记的子级数据标记
		if(fieldParentNode != null)
		{
			parentBindDataSigns = fieldParentNode.bindDataSigns;
		}
		//顶级字段可以使用插件的顶级数据标记、以及所属数据集绑定的数据标记的子级数据标记
		else
		{
			parentBindDataSigns = dataSetBind.bindDataSigns;
			
			var pluginDataSigns = (chartPlugin ? chartPlugin.dataSigns : null);
			if(pluginDataSigns != null)
			{
				for(var i=0; i<pluginDataSigns.length; i++)
				{
					if(po.isDataSignTargetField(pluginDataSigns[i]))
						re.push(pluginDataSigns[i]);
				}
			}
		}
		
		if(parentBindDataSigns != null)
		{
			for(var i=0; i<parentBindDataSigns.length; i++)
			{
				var children = parentBindDataSigns[i].children;
				if(children != null)
					re = re.concat(children);
			}
		}
		
		return re;
	};
	
	po.evalDataSignMatchInfoForDataSet = function(chartPlugin, dataSetBind)
	{
		var dataSigns = po.findCandidateDataSignsForDataSet(chartPlugin);
		
		var re = [];
		
		if($.isEmpty(dataSigns))
			return re;
		
		for(var i=0; i<dataSigns.length; i++)
		{
			var dataSign = dataSigns[i];
			var rei = { dataSign: dataSign, matches: true };
			re.push(rei);
		}
		
		return re;
	};
	
	po.evalDataSignMatchInfoForField = function(chartPlugin, dataSetBind, dataSetFieldNode)
	{
		var dataSigns = po.findCandidateDataSignsForField(chartPlugin, dataSetBind, dataSetFieldNode.parentNode);
		
		var re = [];
		
		if($.isEmpty(dataSigns))
			return re;
		
		var dataSetField = dataSetFieldNode.data;
		
		for(var i=0; i<dataSigns.length; i++)
		{
			var dataSign = dataSigns[i];
			var rei = { dataSign: dataSign, matches: po.isDataSignMatchesField(dataSign, dataSetField) };
			re.push(rei);
		}
		
		return re;
	};
	
	/**
	 * 判断数据标记是否匹配给定字段。
	 * dataSign.fieldMatcher支持如下格式：
	 * matcher
	 * [ matcher, ... ] 它们之间是【或】关系
	 * 其中，matcher支持如下格式：
	 * "..."		比如："(s || d) && !array"，字符含义参考po.DATASIGN_FIELD_MATCHERS
	 * { ... }		格式参考po.DATASIGN_FIELD_MATCHERS
	 * [ ... ]		元素可以是："..."、{ ... }，它们之间是【且】的关系
	 */
	po.isDataSignMatchesField = function(dataSign, dataSetField)
	{
		var fieldMatcher = dataSign.fieldMatcher;
		
		//未定义时应返回true
		if($.isEmpty(fieldMatcher))
			return true;
		
		fieldMatcher = ($.isArray(fieldMatcher) ? fieldMatcher : [ fieldMatcher ]);
		
		for(var i=0; i<fieldMatcher.length; i++)
		{
			//只要任一匹配即认为匹配
			if(po.isDataSignMatchersMatchesField(dataSetField, fieldMatcher[i]))
				return true;
		}
		
		return false;
	};
	
	po.isDataSignMatchersMatchesField = function(dataSetField, matchers)
	{
		//此时未定义的应返回false
		if($.isEmpty(matchers))
			return false;
		
		matchers = ($.isArray(matchers) ? matchers : [ matchers ]);
		
		for(var i=0; i<matchers.length; i++)
		{
			//只要任一不匹配即认为不匹配
			if(!po.isDataSignMatcherMatchesField(dataSetField, matchers[i]))
				return false;
		}
		
		return true;
	}
	
	po.isDataSignMatcherMatchesField = function(dataSetField, matcher)
	{
		//此时未定义的应返回false
		if($.isEmpty(matcher))
			return false;
		
		if($.isTypeString(matcher))
			return po.isDataSignMatcherStrMatchesField(dataSetField, matcher);
		else
			return po.isDataSignMatcherObjMatchesField(dataSetField, matcher);
	};
	
	po.isDataSignMatcherStrMatchesField = function(dataSetField, matcher)
	{
		//此时未定义的应返回false
		if($.isEmpty(matcher))
			return false;
		
		var matchesObj = {};
		var fnBody = "";
		
		for(var n in po.DATASIGN_FIELD_MATCHERS)
		{
			matchesObj[n] = po.isDataSignMatcherObjMatchesField(dataSetField, po.DATASIGN_FIELD_MATCHERS[n]);
			fnBody += "var " + n + " = matchesObj['" + n + "'];\n";
		}
		
		fnBody += "return ("+matcher+");";
		
		try
		{
			var func = new Function("matchesObj", fnBody);
			return func(matchesObj);
		}
		catch(e)
		{
			chartFactory.logException(e);
			return false;
		}
	};
	
	po.isDataSignMatcherObjMatchesField = function(dataSetField, matcher)
	{
		//此时未定义的应返回false
		if($.isEmpty(matcher))
			return false;
		
		var fieldType = dataSetField.type;
		var fieldArray = dataSetField.array;
		var fieldEvaluated = dataSetField.evaluated;
		
		if(matcher.inTypes != null)
		{
			var inTypes = ($.isArray(matcher.inTypes) ? matcher.inTypes : [ matcher.inTypes ]);
			if($.inArray(fieldType, inTypes) < 0)
				return false;
		}
		
		if(matcher.notInTypes != null)
		{
			var notInTypes = ($.isArray(matcher.notInTypes) ? matcher.notInTypes : [ matcher.notInTypes ]);
			if($.inArray(fieldType, notInTypes) >= 0)
				return false;
		}
		
		if(matcher.array != null)
		{
			if(fieldArray != matcher.array)
				return false;
		}
		
		if(matcher.evaluated != null)
		{
			if(fieldEvaluated != matcher.evaluated)
				return false;
		}
		
		return true;
	};
	
	po.isDataSignTargetField = function(dataSign)
	{
		var targets = dataSign.targets;
		
		//兼容旧版逻辑
		if(targets == null || targets.length == 0)
			return true;
		
		return ($.inArray(po.DS_TARGET_FIELD, targets) > -1);
	};
	
	po.isDataSignTargetDataSet = function(dataSign)
	{
		var targets = dataSign.targets;
		
		//兼容旧版逻辑
		if(targets == null || targets.length == 0)
			return false;
		
		return ($.inArray(po.DS_TARGET_DATASET, targets) > -1);
	};
	
	po.formatDataSignLabel = function(dataSign)
	{
		if(dataSign.nameLabel && dataSign.nameLabel.value)
			return dataSign.nameLabel.value;
		else
			return dataSign.name;
	};
	
	po.removeBindDataSign = function(node, fullname)
	{
		if(node == null || fullname == null || node.bindDataSigns == null)
			return;
		
		var bindDataSigns = node.bindDataSigns;
		var removed = $.removeById(bindDataSigns, fullname, "fullname");
		
		//子节点也要同步删除
		if(removed && removed.children)
		{
			var subDataSigns = removed.children;
			var subNodes = null;
			
			//node是数据集绑定
			if(node.fieldNodes != null)
				subNodes = node.fieldNodes;
			//node是数据集字段节点
			else if(node.children != null)
				subNodes = node.children;
			
			if(subNodes != null)
			{
				for(var i =0; i<subNodes.length; i++)
				{
					var subNode = subNodes[i];
					
					for(var j=0; j<subDataSigns.length; j++)
					{
						po.removeBindDataSign(subNode, subDataSigns[j].fullname, "fullname");
					}
				}
			}
		}
	};
	
	po.inflateParamPanel = function(dataSetBind)
	{
		var wrapper = $(".paramvalue-form-wrapper", po.elementOfId(po.concatPid("paramPanel"), document.body));
		var pm = po.vuePageModel();
		
		if(!dataSetBind.query)
			dataSetBind.query = {};
		
		var formOptions =
		{
			submitText: po.i18n.confirm,
			yesText: po.i18n.yes,
			noText: po.i18n.no,
			paramValues: po.vueRaw(dataSetBind.query.paramValues),
			readonly: pm.isReadonlyAction,
			rendered: function(form)
			{
				$(".dg-dspform-input:not([type='radio'],[type='checkbox']), .dg-dspform-inputs-wrapper", this).addClass("p-inputtext p-component w-full");
				$(".dg-date-widget-inputs select", this).addClass("p-inputtext p-component");
				$(".dg-dspform-content button", this).addClass("p-button p-button-secondary p-component");
				$(".dg-dspform-foot button", this).addClass("p-button p-component");
				$.focusOnFirstInput(this);
			},
			submit: function(formData)
			{
				dataSetBind.query.paramValues = formData;
				po.vueUnref(po.concatPid("paramPanelEle")).hide();
			}
		};
		
		wrapper.empty();
		
		var params = $.extend(true, [], po.vueRaw(dataSetBind.dataSet.params));
		chartFactory.chartTool.renderDataSetParamForm(wrapper[0], params, formOptions);
	};
	
	$.validator.addMethod("dataSetSignRequired", function(context, element)
	{
		var po = context.po;
		var chart = context.chart;
		var re = po.validateDataSetBindDataSign(chart);
		
		if(re == true)
		{
			$(element).removeData("invalidMsg");
			return true;
		}
		else
		{
			var msg = "Unknown";
			
			if(re.type == "dataset")
			{
				msg = $.validator.format(po.i18n["chart.dataSetSign.required"], re.dataSign.extLabel);
			}
			else if(re.type == "field")
			{
				msg = $.validator.format(po.i18n["chart.fieldSign.required"], re.dataSet.name, re.dataSign.extLabel);
			}
			
			$(element).data("invalidMsg", msg);
			return false;
		}
	});

	$.validator.addMethod("validateDataSetRange", function(context, element)
	{
		var po = context.po;
		var chart = context.chart;
		
		var re = true;
		
		var dsr = (chart.pluginVo ? chart.pluginVo.dataSetRange : null);
		var dsbs = (chart.dataSetBindVOs || []);
		var mainCount = 0;
		var attachmentCount = 0;
		
		$.each(dsbs, function(i, dsb)
		{
			if(dsb.attachment)
				attachmentCount++;
			else
				mainCount++;
		});
		
		var msg = "";
		var minMsg = po.i18n.noLimit;
		var maxMsg = po.i18n.noLimit;
		
		if(re && dsr && dsr.main)
		{
			if(dsr.main.min != null)
			{
				minMsg = dsr.main.min;
				re = (re ? (mainCount >= dsr.main.min) : false);
			}
			
			if(dsr.main.max != null)
			{
				maxMsg = dsr.main.max;
				re = (re ? (mainCount <= dsr.main.max) : false);
			}
			
			if(!re)
				msg = $.validator.format(po.i18n["chart.validateDataSetRange.main"], minMsg, maxMsg, mainCount);
		}
		
		if(re && dsr && dsr.attachment)
		{
			if(dsr.attachment.min != null)
			{
				minMsg = dsr.attachment.min;
				re = (re ? (attachmentCount >= dsr.attachment.min) : false);
			}
			
			if(dsr.attachment.max != null)
			{
				maxMsg = dsr.attachment.max;
				re = (re ? (attachmentCount <= dsr.attachment.max) : false);
			}
			
			if(!re)
				msg = $.validator.format(po.i18n["chart.validateDataSetRange.attachment"], minMsg, maxMsg, attachmentCount);
		}
		
		if(re)
			$(element).removeData("invalidMsg");
		else
			$(element).data("invalidMsg", msg);
		
		return re;
	});
	
	$.validator.addMethod("validateChartConfigValues", function(context)
	{
		var po = context.po;
		var chart = context.chart;
		var configForm = (chart.pluginVo ? chart.pluginVo.configForm : null);
		
		if(configForm == null)
			return true;
		
		var fm = po.vueFormModel();
		
		return po.avo.validateConfigValuesRequired(fm.configValues, configForm);
	});
	
	var formModel = po.formModel;
	formModel.pluginVo = (formModel.pluginVo == null ? {} : formModel.pluginVo);
	po.assemblePlugin(formModel.pluginVo);
	formModel.analysisProject = (formModel.analysisProject == null ? {} : formModel.analysisProject);
	formModel.dataSetBindVOs = (formModel.dataSetBindVOs == null ? [] : formModel.dataSetBindVOs);
	formModel.plugin = undefined;
	formModel.dataSetBinds = undefined;
	formModel.configValues = (formModel.configValues || {});
	po.assembleDataSetBinds(formModel);
	
	po.setupForm(formModel,
	{
		success : function(response)
		{
			var fm = po.vueFormModel();
			var chart = response.data;
			
			fm.id = chart.id;
			
			var options = this;
			if(options.saveAndShowAction)
				window.open(po.concatContextPath("/cv/"+encodeURIComponent(chart.id)+"/"), "show-chart-"+chart.id);
		}
	},
	{
		rules:
		{
			updateInterval: {"integer": true},
			dataSetSignCheckVal: { "dataSetSignRequired": true },
			validateDataSetRangeVal: { "validateDataSetRange": true },
			chartConfigValuesCheckVal: { "validateChartConfigValues": true }
		},
		customNormalizers:
		{
			dataSetSignCheckVal: function()
			{
				var context = { po: po, chart: po.vueFormModel() };
				return context;
			},
			validateDataSetRangeVal: function()
			{
				var context = { po: po, chart: po.vueFormModel() };
				return context;
			},
			chartConfigValuesCheckVal: function()
			{
				var context = { po: po, chart: po.vueFormModel() };
				return context;
			}
		},
		messages:
		{
			dataSetSignCheckVal:
			{
				dataSetSignRequired: function(val, element)
				{
					return $(element).data("invalidMsg");
				}
			},
			validateDataSetRangeVal:
			{
				validateDataSetRange: function(val, element)
				{
					return $(element).data("invalidMsg");
				}
			},
			chartConfigValuesCheckVal: po.i18n["chart.configValues.editRequired"]
		}
	});
	
	po.vuePageModel(
	{
		disableSaveShow: po.disableSaveShow,
		pluginHasDataSetSign: po.pluginHasDataSetSign(formModel.pluginVo),
		candidateDataSignInfos: [],
		candidateDataSignTarget: "",
		dataSignDetail: { label: "", detail: "" },
		dataSignDetailShown: false,
		dataSignTarget: "field",
		dataSetBindForSign: null,
		dataSetFieldNodeForSign: null,
		updateIntervalType: (formModel.updateInterval > -1 ? "interval" : "none"),
		updateIntervalTypeOptions: po.updateIntervalTypeOptions,
		resultDataFormat: po.initResultDataFormat,
		enableResultDataFormat: po.enableResultDataFormat,
		dateOrTimeTypeOptions: po.dateOrTimeTypeOptions,
		optionsFormModel: { options: "" },
		configValuesPanelShown: false
	});
	
	po.vueRef(po.concatPid("dataSignsPanelEle"), null);
	po.vueRef(po.concatPid("dataSignDetailPanelEle"), null);
	po.vueRef(po.concatPid("paramPanelEle"), null);
	po.vueRef(po.concatPid("dataFormatPanelEle"), null);
	po.vueRef(po.concatPid("pluginVoDescEle"), null);
	po.vueRef(po.concatPid("optionsPanelEle"), null);
	po.vueRef(po.concatPid("fieldMorePanelEle"), null);
	
	po.vueMethod(
	{
		formatChartPlugin: function(chartPlugin)
		{
			return $.toChartPluginHtml(chartPlugin, po.contextPath,
						{
							justifyContent: "start", showVersion:true, showApiVersion:true, showPlatformVersion: true,
							apiVersionDesc: po.i18n["chartPlugin.apiVersion.desc"],
							platformVersionDesc: po.i18n["chartPlugin.platformVersion.desc"]
						});
		},
		
		formatChartPluginDesc: function(chartPlugin)
		{
			if(chartPlugin && chartPlugin.descLabel && chartPlugin.descLabel.value)
				return chartPlugin.descLabel.value;
			else
				return po.i18n.emptyDesc;
		},
		
		formatDataSetFieldType: function(type)
		{
			return $.findNameByValue(po.dataSetFieldTypeOptions, type);
		},
		
		isDataSignTargetField: function(dataSign)
		{
			return po.isDataSignTargetField(dataSign);
		},
		
		isDataSignTargetDataSet: function(dataSign)
		{
			return po.isDataSignTargetDataSet(dataSign);
		},
		
		isEmptyPluginConfigForm: function(plugin)
		{
			return po.isEmptyPluginConfigForm(plugin);
		},
		
		dsbParamValuesCount: function(dataSetBind)
		{
			var re = 0;
			var pvs = (dataSetBind.query ? dataSetBind.query.paramValues : null);
			
			if(pvs != null)
			{
				for(var p in pvs)
					re++;
			}
			
			return re;
		},
		
		onDeleteAnalysisProject: function()
		{
			var fm = po.vueFormModel();
			fm.analysisProject = {};
		},
		
		onSelectAnalysisProject: function()
		{
			po.handleOpenSelectAction("/analysisProject/select", function(analysisProject)
			{
				var fm = po.vueFormModel();
				fm.analysisProject = analysisProject;
			});
		},
		
		onSelectChartPlugin: function()
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			
			var oldPluginId = (fm.pluginVo ? fm.pluginVo.id : "");
			
			po.handleOpenSelectAction("/chartPlugin/select", function(plugin)
			{
				po.getJson("/chartPlugin/detailValue/"+encodeURIComponent(plugin.id), function(plugin)
				{
					po.assemblePlugin(plugin);
					
					fm.pluginVo = plugin;
					po.restoreDataSetBinds(fm);
					po.assembleDataSetBinds(fm);
					pm.pluginHasDataSetSign = po.pluginHasDataSetSign(fm.pluginVo);
					
					po.bakPluginConfigValuesMap[oldPluginId] = fm.configValues;
					
					if(po.bakPluginConfigValuesMap[plugin.id] != null)
						fm.configValues = po.bakPluginConfigValuesMap[plugin.id];
					else
						fm.configValues = {};
					
					if(plugin && po.DashboardApiVersion.LATEST_VERSION != plugin.apiVersion)
					{
						var msg = $.validator.format(po.i18n["chart.plugin.apiVersion.deprecated"],
								plugin.apiVersion, po.DashboardApiVersion.LATEST_VERSION);
						$.tipWarn(msg);
					}
				});
			});
		},
		
		onAddDataSet: function()
		{
			po.handleOpenSelectAction("/dataSet/select?multiple", function(dataSets)
			{
				var data = $.propertyValueParam(dataSets, "id");
				
				po.getJson("/dataSet/getProfileDataSetByIds", data, function(dataSets)
				{
					var fm = po.vueFormModel();
					
					$.each(dataSets, function(idx, dataSet)
					{
						var dsb =
						{
							dataSet: dataSet,
							dataSetSigns: [],
							fieldSigns: {},
							fieldAliases: {},
							fieldOrders: {},
							attachment: false
						};
						
						po.assembleDataSetBind(dsb);
						fm.dataSetBindVOs.push(dsb);
					});
				});
			});
		},
		
		onMoveUpDataSetBind: function(e, dsbIdx)
		{
			var fm = po.vueFormModel();
			if(dsbIdx > 0)
			{
				var prev = fm.dataSetBindVOs[dsbIdx - 1];
				fm.dataSetBindVOs[dsbIdx - 1] = fm.dataSetBindVOs[dsbIdx];
				fm.dataSetBindVOs[dsbIdx] = prev;
			}
		},
		
		onMoveDownDataSetBind: function(e, dsbIdx)
		{
			var fm = po.vueFormModel();
			if((dsbIdx + 1) < fm.dataSetBindVOs.length)
			{
				var next = fm.dataSetBindVOs[dsbIdx + 1];
				fm.dataSetBindVOs[dsbIdx + 1] = fm.dataSetBindVOs[dsbIdx];
				fm.dataSetBindVOs[dsbIdx] = next;
			}
		},
		
		onDeleteDataSetBind: function(e, dsbIdx)
		{
			po.confirm(
			{
				message: po.i18n["chart.confirmDelThisDsb"],
				accept: function()
				{
					var fm = po.vueFormModel();
					fm.dataSetBindVOs.splice(dsbIdx, 1);
				}
			});
		},
		
		onShowDataSignPanel: function(e, dataSetBind, dataSetFieldNode)
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			var chartPlugin = fm.pluginVo;
			
			//直接show会导致面板还停留在上一个元素上
			po.vueUnref(po.concatPid("dataSignsPanelEle")).hide();
			po.vueNextTick(function()
			{
				pm.dataSignTarget = (dataSetFieldNode != null ? "field" : "dataset");
				pm.dataSetBindForSign = dataSetBind;
				pm.dataSetFieldNodeForSign = (dataSetFieldNode != null ? dataSetFieldNode : null);
				
				if(dataSetFieldNode != null)
				{
					pm.candidateDataSignInfos = po.evalDataSignMatchInfoForField(chartPlugin, dataSetBind, dataSetFieldNode);
					pm.candidateDataSignTarget = "field";
				}
				else
				{
					pm.candidateDataSignInfos = po.evalDataSignMatchInfoForDataSet(chartPlugin, dataSetBind);
					pm.candidateDataSignTarget = "dataset";
				}
				
				po.vueUnref(po.concatPid("dataSignsPanelEle")).show(e);
			});
		},
		
		onShowDataSignDetail: function(e, dataSign)
		{
			var pm = po.vuePageModel();
			
			//直接show会导致面板还停留在上一个元素上
			po.vueUnref(po.concatPid("dataSignDetailPanelEle")).hide();
			po.vueNextTick(function()
			{
				pm.dataSignDetail.label = dataSign.extLabel;
				pm.dataSignDetail.detail = (dataSign.descLabel ? (dataSign.descLabel.value || "") : "");
				
				po.vueUnref(po.concatPid("dataSignDetailPanelEle")).show(e);
			});
		},
		
		onDataSignDetailPanelShow: function(e)
		{
			var pm = po.vuePageModel();
			pm.dataSignDetailShown = true;
		},
		
		onDataSignDetailPanelHide: function(e)
		{
			var pm = po.vuePageModel();
			pm.dataSignDetailShown = false;
		},
		
		onUpdateDataSignDetailPanel: function(e, dataSign)
		{
			var pm = po.vuePageModel();
			if(pm.dataSignDetailShown)
			{
				pm.dataSignDetail.label = dataSign.extLabel;
				pm.dataSignDetail.detail = (dataSign.descLabel ? (dataSign.descLabel.value || "") : "");
			}
		},
		
		onAddDataSign: function(e, dataSign)
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			
			if(pm.dataSignTarget == "dataset")
			{
				if(pm.dataSetBindForSign)
				{
					if(!dataSign.multiple && po.hasDataSetSigned(fm.dataSetBindVOs, dataSign))
					{
						var msg = $.validator.format(po.i18n["chart.dataSetWithSignExist"], dataSign.extLabel);
						$.tipWarn(msg);
						return;
					}
					
					var bindDataSigns = pm.dataSetBindForSign.bindDataSigns;
					
					if($.inArrayById(bindDataSigns, dataSign.fullname, "fullname") < 0)
						bindDataSigns.push(dataSign);
					
					po.vueUnref(po.concatPid("dataSignsPanelEle")).hide();
				}
			}
			else if(pm.dataSignTarget == "field")
			{
				if(pm.dataSetBindForSign && pm.dataSetFieldNodeForSign)
				{
					var parentNode = (pm.dataSetFieldNodeForSign.parentNode != null ? pm.dataSetFieldNodeForSign.parentNode : pm.dataSetBindForSign);
					if(!dataSign.multiple && po.hasSubDataSetFieldSigned(parentNode, dataSign))
					{
						var msg = $.validator.format(po.i18n["chart.fieldWithSignExist"],
								pm.dataSetBindForSign.dataSet.name, dataSign.extLabel);
						$.tipWarn(msg);
						return;
					}
					
					var bindDataSigns = pm.dataSetFieldNodeForSign.bindDataSigns;
					
					if($.inArrayById(bindDataSigns, dataSign.fullname, "fullname") < 0)
						bindDataSigns.push(dataSign);
					
					po.vueUnref(po.concatPid("dataSignsPanelEle")).hide();
				}
			}
		},
		
		onRemoveDataSetSign: function(dataSetBind, dataSigName)
		{
			po.removeBindDataSign(dataSetBind, dataSigName);
		},
		
		onRemoveDataSetFieldSign: function(dataSetFieldNode, dataSigName)
		{
			po.removeBindDataSign(dataSetFieldNode, dataSigName);
		},
		
		onUpdateIntervalTypeChange: function(e)
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			
			if(e.value == "none")
			{
				po._updateIntervalBackup = fm.updateInterval;
				fm.updateInterval = -1;
			}
			else if(e.value == "interval")
			{
				if(po._updateIntervalBackup != null && po._updateIntervalBackup > -1)
					fm.updateInterval = po._updateIntervalBackup;
				else
					fm.updateInterval = 1000;
			}
		},
		
		onShowParamPanel: function(e, dataSetBind)
		{
			po._currentDataSetBindForParam = dataSetBind;
			po.vueUnref(po.concatPid("paramPanelEle")).toggle(e);
		},
		
		onParamPanelShow: function(e)
		{
			if(po._currentDataSetBindForParam)
				po.inflateParamPanel(po._currentDataSetBindForParam);
		},
		
		onParamPanelHide: function(e)
		{
			var wrapper = $(".paramvalue-form-wrapper", po.elementOfId(po.concatPid("paramPanel"), document.body));
			chartFactory.chartTool.destroyDataSetParamForm(wrapper[0]);
		},
		
		onClearParamValues: function(e, dataSetBind)
		{
			dataSetBind.query.paramValues = {};
		},
		
		onShowDataFormatPanel: function(e)
		{
			po.vueUnref(po.concatPid("dataFormatPanelEle")).toggle(e);
		},
		
		onShowChartPluginDesc: function(e)
		{
			po.vueUnref(po.concatPid("pluginVoDescEle")).toggle(e);
		},
		
		onShowChartPluginManual: function()
		{
			var fm = po.vueFormModel();
			if(fm.pluginVo && fm.pluginVo.id && fm.pluginVo.hasManual)
			{
				po.open("/chartPlugin/manual/" + encodeURIComponent(fm.pluginVo.id), { target: "_blank" });
			}
		},
		
		onShowConfigValuesPanel: function(e)
		{
			var pm = po.vuePageModel();
			pm.configValuesPanelShown = true;
		},
		
		onConfigValuesPanelShow: function()
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			var pluginConfigForm = po.vueRaw(fm.pluginVo ? fm.pluginVo.configForm : {});
			var configValues = po.vueRaw(fm.configValues);
			po.setupChartConfigValuesForm(pluginConfigForm, configValues,
			{
				submitHandler: function(avs)
				{
					fm.configValues = avs;
					pm.configValuesPanelShown = false;
				},
				readonly: pm.isReadonlyAction,
				//此时不允许自由编辑图表配置，因此应是严格数据模式
				strictSubmitData: true,
				//此时不存在继承复制场景，不必保留null值
				retainDataNullProp: false
			});
		},
		
		onShowOptionsPanel: function(e)
		{
			po.vueUnref(po.concatPid("optionsPanelEle")).toggle(e);
		},
		
		onOptionsPanelShow: function()
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			var options = po.vueRaw(fm.options);
			
			var form = po.elementOfId(po.concatPid("optionsForm"), document.body);
			var codeEditorEle = po.elementOfId(po.concatPid("optionsContentCodeEditor"), form);
			
			var editorOptions =
			{
				value: "",
				matchBrackets: true,
				autoCloseBrackets: true,
				mode: {name: "javascript", json: true}
			};
			
			codeEditorEle.empty();
			var codeEditor = po.createCodeEditor(codeEditorEle, editorOptions);
			po.setCodeTextTimeout(codeEditor, (options || ""), true);
			
			po.setupSimpleForm(form, pm.optionsFormModel, function()
			{
				pm.optionsFormModel.options = po.getCodeText(codeEditor);
				fm.options = pm.optionsFormModel.options;
				po.vueUnref(po.concatPid("optionsPanelEle")).hide();
			});
		},
		
		onShowFieldMorePanel: function(e, dataSetFieldNode)
		{
			var pm = po.vuePageModel();
			
			//直接show会导致面板还停留在上一个元素上
			po.vueUnref(po.concatPid("fieldMorePanelEle")).hide();
			po.vueNextTick(function()
			{
				pm.dataSetFieldNodeForSign = dataSetFieldNode;
				po.vueUnref(po.concatPid("fieldMorePanelEle")).show(e);
			});
		},
		
		onFieldMorePanelShow: function()
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			
			var form = po.elementOfId(po.concatPid("fielMoreForm"), document.body);
			
			po.setupSimpleForm(form, pm.dataSetFieldNodeForSign, function()
			{
				po.vueUnref(po.concatPid("fieldMorePanelEle")).hide();
			});
		},
		
		onSaveAndShow: function(e)
		{
			try
			{
				po.inSaveAndShowAction(true);
				po.form().submit();
			}
			finally
			{
				po.inSaveAndShowAction(false);
			}
		}
	});
	
	po.setupPalette();
};

})
(jQuery);