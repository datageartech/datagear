/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 工具函数集。
 * 
 * 依赖:
 * jquery.js
 * jquery-ui.js
 * datagear-model.js
 */

(function($, undefined)
{
	$.extend(
	{
		/**
		 * 打开给定URL页面。
		 * 
		 * @param url 请求的URL。
		 * @param options 选项，格式如下：
		 * 			{
		 * 				//可选，打开目标：document.body 页面内jquery对话框；"_blank" 新网页；"_file" 文件下载
		 * 				target : document.body,
		 * 				
		 *				//可选，传递给新页面的参数，可以在目标页面通过$.pageParam(dom)获取
		 * 				pageParam : undefined,
		 * 
		 * 				//是否在titlebar添加pin按钮
		 * 				pinTitleButton : false || true,
		 * 				
		 * 				//其他$.ui.dialog参数
		 * 				...
		 * 
		 * 				//其他$.ajax参数
		 * 				...
		 * 			}
		 */
		open : function(url, options)
		{
			if(options && (options.target == "_blank" || options.target == "_file"))
			{
				if(options.target == "_blank")
				{
					//使用window.open()如果options.data很大的话会使URL超长导致请求失败，因而改为postOnForm
					
					//url = url + (options.data ? "?" + $.param(options.data) : "");
					//window.open(url);
					
					$.postOnForm(url, {"data" : options.data, "target" : "_blank"});
				}
				else
				{
					//使用<a>标签如果options.data很大的话会使URL超长，因而改为postOnForm()方案
					
					/*
					url = url + (options.data ? "?" + $.param(options.data) : "");
					
					//对于文件下载，采用window.open会使浏览器闪烁，而采用<a>标签不会
					var $fileLink = $("#_file_download_link");
					if($fileLink.length == 0)
					{
						$fileLink = $("<a />").attr("id", "_file_download_link")
							.css("width", "0px").css("height", "0px").appendTo(document.body);
					}
					
					$fileLink.attr("href", url);
					$fileLink[0].click();//$fileLink.click()无法触发下载动作
					*/
					
					$.postOnForm(url, {"data" : options.data});
				}
			}
			else
			{
				options = $.extend(
					{
						title : undefined, 
						target : document.body,
						pageParam : undefined,
						pinTitleButton : false,
						modal : true,
						classes: { "ui-dialog": "ui-corner-all ui-widget-shadow" }
					}, options);
				
				var ajaxOptions =
				{
					data : (options.data ? options.data : undefined),
					success : function(data, textStatus, jqXHR)
					{
						var container=$(options.target ? options.target : document.body);
						
						var $dialog = $("<div id='dialog-"+new Date().getTime()+"' class='dialog-content-container'></div>").appendTo(container);
						
						if(options.pageParam)
							$.pageParam($dialog, options.pageParam);
						
						$._dialog($dialog, options);
						$dialog.html(data);
						
						if(!options.title)
						{
							var title = $("> title", $dialog).text();
							$dialog.dialog( "option", "title", title);
						}
					},
					type : "POST"
				};
				
				$.ajax(url, ajaxOptions);
			}
		},
		
		/*用于支持$.pageParam函数的元素CSS类名*/
		PAGE_PARAM_BINDER_CLASS : "page-param-binder",
		
		/**
		 * 获取/设置页面参数，设置页面参数，使页面在加载完成后可以在内部获取此参数。
		 * 
		 * @param $dom 必选，任意dom元素
		 * @param param 可选，要设置的参数
		 */
		pageParam : function($dom, param)
		{
			if(param == undefined)
			{
				var dcc = $dom.closest("." + $.PAGE_PARAM_BINDER_CLASS);
				return dcc.data("pageParam");
			}
			else
			{
				$dom.addClass($.PAGE_PARAM_BINDER_CLASS);
				$dom.data("pageParam", param);
			}
		},
		
		/**
		 * 调用页面参数函数。
		 * 如果没有页面参数或者指定的函数，返回undefined。
		 * 
		 * @param $dom 必选，任意dom元素
		 * @param args... 可选，页面参数是函数时，函数参数；页面参数是对象时，[函数名, 函数参数]
		 */
		pageParamCall : function($dom, args)
		{
			var pageParam = $.pageParam($dom);
			
			//没有页面参数
			if(!pageParam)
			{
				return undefined;
			}
			//页面参数是函数
			else if($.isFunction(pageParam))
			{
				var pargs = $.makeArray(arguments).slice(1);
				
				pageParam.apply(window, pargs);
			}
			//页面参数是对象
			else
			{
				if(arguments.length < 2)
					throw new Error("The function name in the page param object to be call should be set");
				
				var fun = pageParam[arguments[1]];
				
				if(fun == undefined)
					return undefined;
				
				var pargs = $.makeArray(arguments).slice(2);
				
				return fun.apply(pageParam, pargs);
			}
		},
		
		/**
		 * 构建与此组件关联的对话框。
		 * 
		 * @param options 对话框选项集。
		 */
		_dialog : function(element, options)
		{
			options = $.extend(
			{
				//是否
				pin : true,
				
				close : function(event)
				{
					if(!event.isHideDialog)
						$(this).dialog("destroy").remove();
				},
				
				position: {my: "center top", at: "center top+6%"},
				
				width : "80%"
			},
			options);
			
			//处理百分比高度
			if(options.height && typeof(options.height)=="string" && options.height.charAt(options.height.length - 1)=="%")
				options.height = parseInt(options.height.substr(0, options.height.length - 1)) * $(window).height() / 100;
			
			element.dialog(options);
			
			if(options.pinTitleButton)
			{
				var titlebar = $(".ui-dialog-titlebar", $(element).dialog("widget"));
				
				var pinButton = $("<button type='button'></button>")
				.button({
					label: $( "<a>" ).text( "pin" ).html(),
					icon: "ui-icon-pin-s",
					showLabel: false
				})
				.appendTo( titlebar )
				.addClass("dialog-titlebar-pin stated-active")
				.click(function(event)
				{
					var $this = $(this);
					
					if($this.hasClass("ui-state-active"))
						$(this).removeClass("ui-state-active");
					else
						$(this).addClass("ui-state-active");
				});
			}
		},
		
		/**
		 * 判断给定dom元素是否在对话框中或者将要在对话框中显示。
		 * 
		 * @param dom 任意DOM元素
		 */
		isInDialog : function(dom)
		{
			var $dialogFlag = $(dom).closest(".dialog-content-container");
			
			return ($dialogFlag && $dialogFlag.length > 0);
		},
		
		/**
		 * 获取元素所处的对话框DOM对象，如果不在对话框中，返回一个空的Jquery对象（长度为0）。
		 */
		getInDialog : function(dom)
		{
			var dialog = $(dom).closest(".dialog-content-container");
			
			return dialog;
		},
		
		/**
		 * 关闭并销毁对话框。
		 */
		closeDialog : function($dialog)
		{
			$dialog.dialog("destroy").remove();
		},
		
		/**
		 * 对话框是否设置为钉住。
		 */
		isDialogPinned : function($dialog)
		{
			var titlebar = $(".ui-dialog-titlebar", $dialog.dialog("widget"));
			
			var pinButton = titlebar.find(".dialog-titlebar-pin");
			
			if(pinButton.length > 0 && pinButton.hasClass("ui-state-active"))
				return true;
			else
				return false;
		},
		
		/**
		 * 转换为表单提交。
		 * 此方法会把数据写入表单参数，然后提交表单。
		 * 
		 * @param url
		 * @param options
		 * 			{
		 * 				//表单参数对象
		 * 				data : undefined,
		 * 				
		 * 				//表单提交目标
		 * 				target : undefined
		 * 			}
		 */
		postOnForm : function(url, options)
		{
			options = $.extend({ data : {}, target : ""}, options);
			
			var $form = $("#___post_on_form___");
			if($form.length == 0)
			{
				$form = $("<form />").attr("id", "___post_on_form___")
					.attr("method", "POST")
					.css("width", "0px").css("height", "0px").appendTo(document.body);
			}
			else
				$form.empty();
			
			$form.attr("action", url).attr("target", options.target);
			
			var param = $.param(options.data);
			
			//XXX $.param会将" "转换为"+"，而这里的decodeURIComponent并不会将"+"恢复为" "，因此需要在这里预先转换
			param = param.replace(/\+/g, " ");
			
			var paramArray = param.split("&");
			
			for(var i=0; i<paramArray.length; i++)
			{
				var paramPair = paramArray[i].split("=");
				
				var name = decodeURIComponent(paramPair[0]);
				var value = decodeURIComponent(paramPair[1]);

				$("<input type='hidden' />").attr("name", name).attr("value", value).appendTo($form);
			}
			
			$form[0].submit();
		},
		
		/**
		 * 打开确认对话框。
		 * @param content 显示内容
		 * @param options
		 * 			{
		 * 				//可选，确定回调函数
		 * 				confirm : function(){},
		 * 
		 * 				//可选，取消回调函数
		 * 				cancel : function(){},
		 * 
		 * 				//可选，确定按钮文本
		 * 				confirmText : "Confirm",
		 * 
		 * 				//可选，取消按钮文本
		 * 				cancelText : "Cancel",
		 * 
		 * 				//可选，取消按钮文本
		 * 				title : "Confirm"
		 * 			}
		 * @param yesCallback 确定回调函数
		 * @param noCallback 取消回调函数
		 */
		confirm : function(content, options)
		{
			options = (options || {});
			options = $.extend({confirmText : "Confirm", cancelText : "Cancel", title : "Confirm"}, options);
			
			var $confirmDialog = $("#dialog-confirm");
			
			if($confirmDialog.length == 0)
			{
				$confirmDialog = $("<div id='dialog-confirm' class='dialog-confirm' />").appendTo(document.body);
				var $p = $("<p><span class='ui-icon ui-icon-alert'></span></p>").appendTo($confirmDialog);
				$("<div class='confirm-content' />").appendTo($p);
			}
			
			$(".confirm-content", $confirmDialog).html(content);
			
			var buttons={};
			buttons[options.confirmText] = function()
			{
				if(options.confirm)
					options.confirm();
				
				$(this).dialog("close");
	        };
			buttons[options.cancelText] = function()
			{
				if(options.cancel)
					options.cancel();
				
				$(this).dialog("close");
	        };
			
			$confirmDialog.dialog(
			{
				resizable: false,
				height: "auto",
				width: "auto",
				position: {my: "center top", at: "center top+15%"},
				modal: true,
				buttons: buttons,
				title: options.title,
				close : function(event)
				{
					$(this).dialog("destroy").remove();
				}
			});
		},
		
		/**
		 * 初始化指定元素内的所有按钮。
		 */
		initButtons : function($parent)
		{
			$("input:submit, input:button, input:reset, button", $parent).button();
		},
		
		/**
		 * 构建resizable组件，但是阻止resize事件冒泡，不然会触发window的所有resize事件，而影响resizable组件的响应
		 */
		resizableStopPropagation : function($element, options)
		{
			$element.resizable(options);
			
			$element.on("resize", function(event)
			{
				event.stopPropagation();
			});
		},
		
		/**
		 * 提示成功。
		 */
		tipSuccess : function(content, delayMs)
		{
			content = "<span class='tooltip-icon ui-icon ui-icon-circle-check'></span>"
					+"<div class='content-value'>" + content +"</div>";
			return $._tip("ui-state-default", content, (delayMs || 2500));
		},
		
		/**
		 * 提示错误。
		 */
		tipError : function(content, delayMs)
		{
			content = "<span class='tooltip-icon ui-icon ui-icon-alert'></span>"
				+"<div class='content-value'>" + content +"</div>";
			return $._tip("ui-state-error", content, (delayMs || 4100));
		},
		
		/**
		 * 提示信息。
		 */
		tipInfo : function(content, delayMs)
		{
			content = "<span class='tooltip-icon ui-icon ui-icon-info'></span>"
				+"<div class='content-value'>" + content +"</div>";
			return $._tip("ui-state-highlight", content, (delayMs || 4100));
		},

		/**
		 * 关闭提示框。
		 * @param tooltipId 可选，待关闭的tooltip标识。如果不指定，将直接关闭；如果指定，则会检查关闭。
		 */
		closeTip : function(tooltipId)
		{
			var tooltip = $(".global-tooltip", document.body);
			
			if(tooltip.length > 0)
			{
				if(!tooltipId || tooltipId == tooltip.attr("id"))
					tooltip.tooltip("destroy").remove();
			}
		},
		
		/**
		 * 提示。
		 * 
		 * @param tooltipClass 必选；样式名。
		 * @param content 必选；提示内容。
		 * @param delayMs 可选；延迟关闭的毫秒数，小于0表示不自动关闭。
		 */
		_tip : function(tooltipClass, content, delayMs)
		{
			content = "<div class='content'>" + content +"</div>";
			
			var tooltip = $(".global-tooltip", document.body);
			if(tooltip.length > 0)
				tooltip.tooltip("destroy").remove();
			
			var preTooltipTimeoutCloseId = $._tooltipTimeoutCloseId();
			if(preTooltipTimeoutCloseId)
				window.clearTimeout(preTooltipTimeoutCloseId);
			
			var tooltipParent = document.body;
			var customTooltipParent = $(".tooltip-parent");
			if(customTooltipParent.length > 0)
				tooltipParent = customTooltipParent[0];
			
			var tooltipId = $.uid("tooltip");
			
			tooltip = $("<div class='global-tooltip' style='display:none;' title=''>").attr("id", tooltipId)
			.prependTo(tooltipParent).tooltip(
			{
				tooltipClass: tooltipClass,
				content: content,
				position: { my: "center top", at: "center top+3", of: tooltipParent, collision: "flipfit" },
				open: function(event, ui)
				{
					if(delayMs >= 0)
					{
						var tooltipTimeoutCloseId = window.setTimeout(function(){ $.closeTip(); }, delayMs);
						$._tooltipTimeoutCloseId(tooltipTimeoutCloseId);
						
						ui.tooltip.mouseover(function()
						{
							window.clearTimeout($._tooltipTimeoutCloseId());
						})
						.mouseleave(function()
						{
							var tooltipTimeoutCloseId = window.setTimeout(function(){ $.closeTip(); }, delayMs);
							$._tooltipTimeoutCloseId(tooltipTimeoutCloseId);
						});
					}
					
					var tw = this;
					$(".ui-icon-close", ui.tooltip).click(function()
					{
						$(tw).tooltip("destroy").remove();
					});
				}
			})
			.tooltip("open");
			
			return tooltipId;
		},
		
		_tooltipTimeoutCloseId : function(id)
		{
			if(id)
				$(document.body).attr("tooltip-timeout-close-id", id);
			else
			{
				return $(document.body).attr("tooltip-timeout-close-id");
			}
		},
		
		/**
		 * 生成一个唯一ID
		 * 
		 * @param prefix
		 * @returns
		 */
		uid : function(prefix)
		{
			var re=(prefix ? prefix : "id") + new Date().getTime();
			
			return re;
		},
		
		/**
		 * 给对象属性名加前缀，并返回新对象。
		 * 
		 * @param obj
		 * @param prefix
		 */
		namePrefix : function(obj, prefix)
		{
			if(! prefix)
				return obj;
			
			var re = {};
			
			for(var name in obj)
			{
				re [prefix + name] = obj[name];
			}
			
			return re;
		},
		
		/**
		 * 给URL添加参数。
		 * 
		 * @param url 待添加参数的URL
		 * @param name 待添加的参数名
		 * @param value 待添加的参数值
		 */
		addParam : function(url, name, value)
		{
			var f = (url.indexOf('?') < 0 ? "?" : "&" );
			
			url = url + f + name + "=" + value;
			
			return url;
		},
		
		/**
		 * 获取对象或者对象数组的属性值参数字符串，例如：“id=1&id=2&id=3”
		 */
		getPropertyParamString : function(objOrArray, propertyName)
		{
			var re = "";
			
			if(!$.isArray(objOrArray))
				objOrArray = [objOrArray];
			
			for(var i=0; i<objOrArray.length; i++)
			{
				var ele = objOrArray[i];
				
				var pv = (ele ? ele[propertyName] : null);
				
				if(pv == undefined || pv == null)
					pv = "";
				
				if(re != "")
					re += "&";
				
				re += propertyName + "=" + encodeURIComponent(pv);
			}
			
			return re;
		},
		
		/**
		 * 如果是字符串且超过指定长度，则将其截断。
		 * 
		 * @param str 必选，待截断的字符串
		 * @param suffix 可选，截断后缀，默认为“...”
		 * @param length 可选，截断长度，默认为47
		 */
		truncateIf : function(str, suffix, length)
		{
			if(suffix == undefined)
				suffix = "...";
			
			if(length == undefined)
				length = 47;
			
			if(typeof(str) == "string" && str.length > length)
				str = str.substr(0, length) + suffix;
			
			return str;
		},
		
		/**
		 * 转义HTML关键字。
		 * 
		 * @param text 要转义的文本
		 */
		escapeHtml : function(text)
		{
			return $.model.escapeHtml(text);
		},
		
		/**
		 * 将传入参数转义为路径。
		 * 
		 * @param elements 必选，路径元素
		 */
		toPath : function(elements)
		{
			var re="";
			
			for(var i=0; i< arguments.length; i++)
			{
				element = encodeURIComponent(arguments[i]);
				
				if(re.charAt(re.length - 1) != "/")
					re += "/";
				
				re += element;
			}
			
			return re;
		},
		
		/**
		 * 为DataTables转义属性名。
		 * 参考jquery.dataTables.js的_fnSplitObjNotation函数。
		 */
		escapePropertyNameForDataTables : function(propertyName)
		{
			return propertyName;
			
			/* 后台dbmodel已经限定了propertyName不会包含特殊字符，不再需要此逻辑
			var pn = "";
			
			for(var i=0; i<propertyName.length; i++)
			{
				var c = propertyName.charAt(i);
				
				if(c == '.')
				{
					pn += "\\" + c;
				}
				else
					pn += c;
			}
			
			return pn;
			*/
		},
		
		/**
		 * 反转义由escapePropertyNameForDataTables转义的属性名。
		 */
		unescapePropertyNameForDataTables : function(propertyName)
		{
			return propertyName;
			
			/* 后台dbmodel已经限定了propertyName不会包含特殊字符，不再需要此逻辑
			var pn = "";
			
			for(var i=0; i<propertyName.length; i++)
			{
				var c = propertyName.charAt(i);
				
				if(c == '\\')
				{
					var cin = ((i + 1) < propertyName.length ? propertyName.charAt(i + 1) : 0);
					
					if(cin == '.')
					{
						i += 1;
						pn += cin;
					}
					else
						pn += c;
				}
				else
					pn += c;
			}
			
			return pn;
			*/
		},
		
		/**
		 * 构建Datatables组件的“columns”选项值。
		 * 
		 * @param model 必选，模型
		 * @param options 选项，格式为：
		 * 			{
		 * 				//可选，忽略的属性名称
		 * 				ignorePropertyNames : undefined,
		 *				
		 * 				//可选，字符串最大显示长度
		 * 				stringDisplayThreshold : 47
		 * 			}
		 * @returns {Array}
		 */
		buildDataTablesColumns : function(model, options)
		{
			options = $.extend({ stringDisplayThreshold : 47 }, options);
			
			var properties = model.properties;
			
			var columns = [];
			for(var i=0; i<properties.length; i++)
			{
				var property = properties[i];
				var propName = property.name;
				
				if($.model.containsOrEquals(options.ignorePropertyNames, propName))
					continue;
				
				var notReadable = $.model.hasFeatureNotReadable(property);
				
				columns.push(
				{
					title: $.model.displayInfoHtml(property, "a"),
					data: $.escapePropertyNameForDataTables(propName),
					propertyIndex: i,
					stringDisplayThreshold : options.stringDisplayThreshold,
					render: function(data, type, row, meta)
					{
						var renderValue = "";
						
						var thisColumn = meta.settings.aoColumns[meta.col];
						
						var propertyIndex = thisColumn.propertyIndex;
						var property = model.properties[propertyIndex];
						
						renderValue = $.model.tokenProperty(property, data);
						renderValue = $.truncateIf(renderValue, "...", thisColumn.stringDisplayThreshold);
						
						//解决当所有属性值都为null时，行渲染会很细问题
						if(propertyIndex == 0 && renderValue == "")
							renderValue = " ";
						
						return $.escapeHtml(renderValue);
					},
					defaultContent: "",
					orderable: !notReadable,
					searchable: !notReadable,
					className: (notReadable ? "ui-state-disabled" : "")
				});
			}
			
			return columns;
		},
		
		/**
		 * 获取指定属性名的列号。
		 */
		getDataTableColumn : function(settings, propertyName)
		{
			var columnMetas = this.getDataTableColumnMetas(settings);
			
			var escapedName = $.escapePropertyNameForDataTables(propertyName);
			
			for(var i=0; i<columnMetas.length; i++)
			{
				var columnMeta = columnMetas[i];
				
				if(escapedName == columnMeta.data)
					return i;
			}
			
			return -1;
		},
		
		/**
		 * 获取DataTable列数。
		 */
		getDataTableColumnCount : function(dataTable, row)
		{
			if(row == undefined)
				row = dataTable.row(0);
			
			var count = $("> td", row.node()).length;
			
			return count;
		},
		
		/**
		 * 获取DataTable行数。
		 */
		getDataTableRowCount : function(dataTable, column)
		{
			if(column == undefined)
				column = dataTable.column(0);
			
			var count = column.nodes().length;
			
			return count;
		},
		
		/**
		 * 根据单元格索引获取对应的模型属性索引
		 */
		getDataTableCellPropertyIndex : function(settings, cellIndex)
		{
			var columnMetas = this.getDataTableColumnMetas(settings);
			
			var propertyIndex = columnMetas[cellIndex.column].propertyIndex;
			
			if(propertyIndex == undefined)
				throw new Error("Not valid column index ["+columnIndex+"] for getting column property");
			
			return propertyIndex;
		},
		
		/**
		 * 根据单元格索引获取对应的模型属性索引-单元格索引数组映射表。
		 */
		getDataTableCellPropertyIndexesMap : function(settings, cellIndexes)
		{
			var columnMetas = this.getDataTableColumnMetas(settings);
			
			var propertyIndexesMap = {};
			for(var i=0; i<cellIndexes.length; i++)
			{
				var index = cellIndexes[i];
				var propertyIndex = columnMetas[index.column].propertyIndex;
				
				if(propertyIndex == undefined)
					throw new Error("Not valid column index ["+columnIndex+"] for getting column property");
				
				var indexes = (propertyIndexesMap[propertyIndex] || (propertyIndexesMap[propertyIndex] = []));
				indexes.push(index);
			}
			
			return propertyIndexesMap;
		},
		
		/**
		 * 根据单元格索引获取对应的行号-单元格索引数组映射表。
		 */
		getDataTableRowIndexesMap : function(cellIndexes)
		{
			var rowIndexesMap = {};
			
			for(var i=0; i<cellIndexes.length; i++)
			{
				var index = cellIndexes[i];
				
				var rowIndexes = (rowIndexesMap[index.row] || (rowIndexesMap[index.row] = []));
				rowIndexes.push(index);
			}
			
			return rowIndexesMap;
		},
		
		/**
		 * 如果所有单元格都在一行内则获取行号，如果不在同一行将返回null
		 */
		getDataTableRowIfSingle : function(cellIndexes)
		{
			var row = null;
			
			for(var i=0; i<cellIndexes.length; i++)
			{
				var index = cellIndexes[i];
				var myRow = index.row;
				
				if(row == null)
					row = myRow;
				else if(myRow != row)
				{
					row = null;
					break;
				}
			}
			
			return row;
		},
		
		/**
		 * 获取DataTable的列元信息。
		 */
		getDataTableColumnMetas : function(settings)
		{
			var columnMetas = undefined;
			
			//column.render函数中的结构
			if(settings.aoColumns)
				columnMetas = settings.aoColumns;
			//.DataTable().settings()结构
			else if(settings[0])
				columnMetas = settings[0].aoColumns;
			
			return columnMetas;
		},
		
		/**
		 * 设置表格数据。
		 */
		setDataTableData : function(dataTable, data, notDraw)
		{
			var rows = dataTable.rows();
			var removeRowIndexes = [];
			var dataIndex = 0;
			
			rows.every(function(rowIndex)
			{
				if(dataIndex >= data.length)
					removeRowIndexes.push(rowIndex);
				else
					this.data(data[dataIndex]);
				
				dataIndex++;
			});
			
			for(; dataIndex<data.length; dataIndex++)
				var row = dataTable.row.add(data[dataIndex]);
			
			dataTable.rows(removeRowIndexes).remove();
			
			if(!notDraw)
				dataTable.draw();
		},
		
		/**
		 * 处理点击选中单元格事件。
		 */
		handleCellSelectionForClick : function(dataTable, clickEvent, $clickCell)
		{
			//多选
			if(clickEvent.ctrlKey)
			{
				if($clickCell.hasClass("selected"))
				{
					dataTable.cell($clickCell).deselect();
					dataTable.cells(dataTable.cells(".selected").indexes()).select();
				}
				else
				{
					var indexes = [];
					indexes = indexes.concat($.makeArray(dataTable.cells(".selected").indexes()));
					indexes = indexes.concat(dataTable.cell($clickCell).index());
					
					dataTable.cells(indexes).select();
				}
			}
			//区域选
			else if(clickEvent.shiftKey)
			{
				var indexes = [];
				indexes = indexes.concat($.makeArray(dataTable.cells(".selected").indexes()));
				indexes = indexes.concat(dataTable.cell($clickCell).index());
				
				indexes = $.evalCellIndexesForRange(indexes);
				
				dataTable.cells(indexes).select();
			}
			//单选
			else
			{
				var selectCells = dataTable.cells(".selected");
				
				if(selectCells.nodes().length == 1 && $clickCell.hasClass("selected"))
					dataTable.cell($clickCell).deselect();
				else
				{
					selectCells.deselect();
					dataTable.cell($clickCell).select();
				}
			}
		},
		
		/**
		 * 处理单元格上、下、左、右按键事件导航。
		 */
		handleCellNavigationForKeydown : function(dataTable, keydownEvent)
		{
			var selectedCells = dataTable.cells(".selected");
			
			if(!selectedCells || selectedCells.length == 0)
				return;
			else
			{
				var cellIndexes = selectedCells.indexes();
				var nextCellIndex = {};
				
				if(keydownEvent.keyCode == $.ui.keyCode.DOWN)
				{
					var cellIndex = cellIndexes[cellIndexes.length - 1];
					nextCellIndex.row = cellIndex.row + 1;
					nextCellIndex.column = cellIndex.column;
				}
				else if(keydownEvent.keyCode == $.ui.keyCode.UP)
				{
					var cellIndex = cellIndexes[0];
					nextCellIndex.row = cellIndex.row - 1;
					nextCellIndex.column = cellIndex.column;
				}
				else if(keydownEvent.keyCode == $.ui.keyCode.LEFT)
				{
					var cellIndex = cellIndexes[0];
					nextCellIndex.row = cellIndex.row;
					nextCellIndex.column = cellIndex.column - 1;
				}
				else if(keydownEvent.keyCode == $.ui.keyCode.RIGHT)
				{
					var cellIndex = cellIndexes[cellIndexes.length - 1];
					nextCellIndex.row = cellIndex.row;
					nextCellIndex.column = cellIndex.column + 1;
				}
				else
					nextCellIndex = null;
				
				if(nextCellIndex != null)
				{
					var maxColumnIndex = this.getDataTableColumnCount(dataTable) - 1;
					var maxRowIndex = this.getDataTableRowCount(dataTable) - 1;
					
					if(nextCellIndex.row > maxRowIndex)
						nextCellIndex.row = maxRowIndex;
					if(nextCellIndex.row < 0)
						nextCellIndex.row = 0;
					
					if(nextCellIndex.column > maxColumnIndex)
						nextCellIndex.column = maxColumnIndex;
					if(nextCellIndex.column < 1)
						nextCellIndex.column = 1;
					
					//多选
					if(keydownEvent.ctrlKey)
					{
						var indexes = [];
						indexes = indexes.concat($.makeArray(selectedCells.indexes()));
						indexes = indexes.concat(nextCellIndex);
						
						dataTable.cells(indexes).select();
					}
					//区域选
					else if(keydownEvent.shiftKey)
					{
						var indexes = [];
						indexes = indexes.concat($.makeArray(selectedCells.indexes()));
						indexes = indexes.concat(nextCellIndex);
						
						indexes = $.evalCellIndexesForRange(indexes);
						
						dataTable.cells(indexes).select();
					}
					//单选
					else
					{
						selectedCells.deselect();
						dataTable.cell(nextCellIndex).select();
					}
				}
			}
		},
		
		/**
		 * 计算范围选择单元格index数组。
		 */
		evalCellIndexesForRange : function(cellIndexes, minColumnThreashold)
		{
			if(minColumnThreashold == undefined)
				minColumnThreashold = 0;
				
			var minRow = Number.MAX_VALUE, minColumn = Number.MAX_VALUE, maxRow = 0, maxColumn = 0;
			
			for(var i=0; i< cellIndexes.length; i++)
			{
				var cellIndex = cellIndexes[i];
				var row = cellIndex.row;
				var column = cellIndex.column;
				
				if(minRow > row)
					minRow = row;
				
				if(maxRow < row)
					maxRow = row;
				
				if(minColumn > column)
					minColumn = column;
				
				if(maxColumn < column)
					maxColumn = column;
			}
			
			if(minColumn < minColumnThreashold)
				minColumn = minColumnThreashold;
			
			if(maxColumn< minColumnThreashold)
				maxColumn = minColumnThreashold;
			
			var indexes = [];
			
			for(var i=minRow; i<=maxRow; i++)
			{
				for(var j=minColumn; j<=maxColumn; j++)
				{
					indexes.push({ row : i, column : j });
				}
			}
			
			return indexes;
		},
		
		/**
		 * 设置表格对话框高度option。
		 */
		setGridPageHeightOption : function(options)
		{
			options.height = $(window).height() * 0.75;
		},
		
		updateDataTableHeight : function(dataTableElements, height, adjustWidth)
		{
			if(height == null && adjustWidth == null)
				return;
			
			for(var i=0; i<dataTableElements.length; i++)
			{
				var dataTable = $(dataTableElements[i]).DataTable();
				
				if(height != null)
				{
					var tableParent = $(dataTable.table().body()).parent().parent();
					tableParent.height(height);
				}
				
				if(adjustWidth)
					dataTable.columns.adjust();
				
				if(dataTable.init().fixedColumns)
					dataTable.fixedColumns().relayout();
			}
		},
		
		isResizeDataTableWhenShow : function($tabsPanel)
		{
			return ($tabsPanel.attr("resize-table-when-show") == "1");
		},
		
		setResizeDataTableWhenShow : function($tabsPanel)
		{
			$tabsPanel.attr("resize-table-when-show", "1");
		},
		
		clearResizeDataTableWhenShow : function($tabsPanel)
		{
			$tabsPanel.removeAttr("resize-table-when-show");
		},
		
		/**
		 * 为DataTable绑定window重设大小事件。
		 */
		bindResizeDataTableHandler : function(dataTableElements, calChangedDataTableHeightFunc)
		{
			var resizeHandler = function(event) 
			{
				var $dataTable0 = $(dataTableElements[0]);
				
				//忽略隐藏选项卡中的表格调整，仅在选项卡显示时才调整，
				//一是DataTables对隐藏表格的宽度计算有问题，另外，绑定太多处理函数会影响jquery.resizeable组件的效率
				
				var tabsPanel = $dataTable0.closest(".ui-tabs-panel");
				if(tabsPanel.is(":hidden"))
				{
					$.setResizeDataTableWhenShow(tabsPanel);
					return;
				}
				
				var changedHeight = calChangedDataTableHeightFunc();
				
				if(changedHeight != null)
				{
					clearTimeout($dataTable0.data("resizeTableTimer"));
					
					var timer = setTimeout(function()
					{
						$.updateDataTableHeight(dataTableElements, changedHeight);
					},
					250);
					
					$dataTable0.data("resizeTableTimer", timer);
				}
			};
			
			$(window).bind('resize', resizeHandler);
			
			//如果表格处于选项卡页中，则在选项卡显示时，调整表格大小
			var tabsPanel = $(dataTableElements[0]).closest(".ui-tabs-panel");
			if(tabsPanel.length > 0)
			{
				tabsPanel.data("showCallback", function($tabsPanel)
				{
					if(!$.isResizeDataTableWhenShow(tabsPanel))
						return;
					
					var changedHeight = calChangedDataTableHeightFunc();
					$.updateDataTableHeight(dataTableElements, changedHeight, true);
					
					$.clearResizeDataTableWhenShow(tabsPanel);
				});
			}
			
			return resizeHandler;
		},
		
		callTabsPanelShowCallback : function($tabsPanel)
		{
			var panelShowCallback = $tabsPanel.data("showCallback");
			if(panelShowCallback)
				panelShowCallback($tabsPanel);
			
			var subTabs = $tabsPanel.find(".ui-tabs");
			if(subTabs.length > 0)
			{
				subTabs.each(function()
				{
					var $this = $(this);
					
					var subTabsNav = $("> .ui-tabs-nav", $this);
					var subShowTab = $("> li.ui-tabs-tab.ui-state-active", subTabsNav);
					
					var subTabId = $("> a.ui-tabs-anchor", subShowTab).attr("href");
					if(subTabId.charAt(0) == "#")
						subTabId = subTabId.substr(1);
					
					var subTabPanel = $("> #"+subTabId, $this);
					
					$.callTabsPanelShowCallback(subTabPanel);
				});
			}
		},
		
		/**
		 * 构建查询条件Autocomplete组件的“source”选项值。
		 * 
		 * @param propertyPathDisplayNames
		 */
		buildSearchConditionAutocompleteSource : function(propertyPathDisplayNames)
		{
			var source = [];
			
			if(!propertyPathDisplayNames)
				return source;
			
			for(var i=0; i<propertyPathDisplayNames.length; i++)
			{
				var ppdn = propertyPathDisplayNames[i];
				
				source.push({label : ppdn.displayName, value : ppdn.displayName, propertyPath : ppdn.propertyPath});
			}
			
			return source;
		},
		
		/**
		 * 将展示名称查询条件字符串转换为属性路径查询条件字符串。
		 * 
		 * @param conditionSource 条件自动完成源数组，元素必须有"propertyPath"属性。
		 * @param condition 展示名称查询条件字符串
		 */
		convertToPropertyPathCondtion : function(conditionSource, condition)
		{
			if(!condition)
				return condition;
			
			var conditionSourceNew = [];
			conditionSourceNew = conditionSourceNew.concat(conditionSource);
			conditionSourceNew.sort(function(s0, s1)
			{
				if(s0.value.length > s1.value.length)
					return -1;
				else if(s0.value.length == s1.value.length)
					return 0;
				else
					return 1;
			});
			
			var re = condition;
			
			for(var i=0; i<conditionSourceNew.length; i++)
			{
				var cc = conditionSourceNew[i];
				
				re = re.replace(cc.value, cc.propertyPath);
			}
			
			return re;
		},
		
		/**
		 * 调用指定对象的指定函数。
		 * @param obj
		 * @param functionName 函数名称
		 * @param varArgs 可选，可变函数参数
		 */
		callOnObjIf : function(obj, functionName, varArgs)
		{
			if(!obj)
				return undefined;
			
			var fn = obj[functionName];
			
			if(!fn)
				return undefined;
			
			var args = [];
			
			if(arguments.length > 2)
			{
				for(var i=2; i<arguments.length; i++)
					args[i - 2] = arguments[i];
			}
			
			return fn.apply(obj, args);
		},
		
		/**
		 * 调用指定对象的指定函数。
		 * @param obj
		 * @param functionName 函数名称
		 * @param argArray 可选，函数参数数组
		 */
		applyOnObjIf : function(obj, functionName, argArray)
		{
			if(!obj)
				return undefined;
			
			var fn = obj[functionName];
			
			if(!fn)
				return undefined;
			
			return fn.apply(obj, argArray);
		},
		
		/**
		 * 友好格式化大小。
		 */
		prettySize : function(bytes)
		{
			var factor = ["B", "KB", "MB", "GB", "TB", "PB"];
			
			var value = bytes;
			var count = 0;
			
			while(value > 1024 && count < (factor.length-1))
			{
				value = value / 1024;
				count++;
			}
			
			return parseInt(value) + factor[count];
		},
		
		/**
		 * 使用POST方式的getJSON。
		 */
		getJSONOnPost : function(url, data, callback)
		{
			$.ajax(
			{
				url : url,
				data : data,
				success : callback,
				dataType : "json",
				type : "POST",
			});
		},
		
		/**
		 * 单文件上传成功（success）处理函数。
		 */
		fileuploadsuccessHandlerForUploadInfo : function($fileUploadInfo, empty)
		{
			$(".upload-percent", $fileUploadInfo).text("100%");
			
			if(empty)
				$fileUploadInfo.empty();
		},
		
		/**
		 * 单文件上传添加（fileuploadadd）处理函数。
		 */
		fileuploadaddHandlerForUploadInfo : function(e, data, $fileUploadInfo)
		{
			$fileUploadInfo.empty();
			
			var fileName = data.files[0].name;
			var prettySize = $.prettySize(data.files[0].size);

			$("<div class='upload-percent' />").text("0%").appendTo($fileUploadInfo);
			$("<div class='file-name' />").html((fileName.length > 13 ? fileName.substr(0, 10)+"..." : fileName))
				.attr("title", fileName)
				.appendTo($fileUploadInfo);
			$("<div class='file-size' />").html("("+prettySize+")").appendTo($fileUploadInfo);
		},
		
		/**
		 * 单文件上传进度（fileuploadprogressall）处理函数。
		 */
		fileuploadprogressallHandlerForUploadInfo : function (e, data, $fileUploadInfo)
		{
			var progress = parseInt(data.loaded / data.total * 100, 10);
			
			//文件大的话，上传完成后success回调会延迟一会，所以100%在success里设置
			if(progress <= 100 && progress >99)
				progress = 99;
			
			$(".upload-percent", $fileUploadInfo).text(progress +"%");
		},
		
		/**
		 * 获取映射表的关键字、值数组对象，并可选对KEY进行排序。
		 * 返回对象格式为：{ "keys" : [...], "values" : [...] }
		 * @param map 必选，映射表对象
		 * @param keySortfunction 可选，KEY排序函数
		 */
		getMapKeyValueArray : function(map, keySortfunction)
		{
			var keyArray = [];
			
			for(var key in map)
				keyArray.push(key);
			
			if(keySortfunction != undefined)
				keyArray.sort(keySortfunction);
			
			var valueArray = [];
			
			for(var i=0; i<keyArray.length; i++)
			{
				var value = map[keyArray[i]];
				valueArray.push(value);
			}
			
			return { "keys" : keyArray, "values" : valueArray };
		},
		
		/**
		 * 获取属性个数。
		 */
		getPropertyCount : function(obj)
		{
			if(!obj)
				return 0;
			
			var propertyCount = 0;
			
			for(var p in obj)
				propertyCount++;
			
			return propertyCount;
		},
		
		/**
		 * 如果对象仅包含一个属性，返回属性名；否则返回null。
		 */
		getPropertyNameIfSingle : function(obj)
		{
			if(!obj)
				return null;
			
			var keys = Object.keys(obj);
	    	var length = keys.length;
	    	
	    	if(length != 1)
	    		return null;
	    	
	    	return keys[0];
		},
		
		/**
		 * 判断两个对象是否深度相等。
		 */
		deepEquals : function(a, b, ignorePropertyNames, aStack, bStack)
		{
			if(a == b)
				return true;
			
			if(a == null || b == null)
				return false;
			
			var type = $.type(a);
			
			if($.type(b) != type)
				return false;
			
			//基本类型
			if(type != "array" && type != "object")
				return (a == b);
			
			aStack = aStack || [];
			bStack = bStack || [];
			var length = aStack.length;
			
			 //检查是否有循环引用的部分
		    while(length--)
		    {
		        if (aStack[length] == a)
		            return bStack[length] == b;
		    }
		    
		    aStack.push(a);
		    bStack.push(b);
		    
		    //数组
		    if(type == "array")
		    {
		    	length = a.length;
		    	
		    	if (b.length != length)
		    		return false;
		    	
		    	while(length--)
		    	{
		    		if (!$.deepEquals(a[length], b[length], ignorePropertyNames, aStack, bStack))
		    			return false;
		    	}
		    }
		    //对象
		    else
		    {
		    	var keys = Object.keys(a), key;
		    	length = keys.length;
		    	
		    	if (Object.keys(b).length != length)
		    		return false;
		    	
		    	while (length--)
		    	{
		    		key = keys[length];
		    		
		    		if($.model.containsOrEquals(ignorePropertyNames, key))
		    			continue;
		    		
		    		if (!(b.hasOwnProperty(key) && $.deepEquals(a[key], b[key], null, aStack, bStack)))
		    			return false;
		    	}
		    }
		    
		    aStack.pop();
		    bStack.pop();
		    
		    return true;
		},
	});
	
	$.fn.extend(
	{
		/**
		 * 两次点击执行。
		 * @param callback 必选，第二次点击处理函数
		 * @param  firstCallback 可选，第一次点击处理函数
		 */
		secondClick : function(callback, firstCallback)
		{
			$(this).click(function()
			{
				var __this = $(this);
				
				//第二次点击
				if(__this.hasClass("second-click-first"))
				{
					__this.removeClass("ui-state-active second-click-first");
					
					callback.call(__this);
				}
				else
				{
					var success = true;
					
					if(firstCallback)
						success = firstCallback.call(__this);
					
					if(success != false)
						__this.addClass("ui-state-active second-click-first");
				}
			})
			.hover(function(){}, 
			function()
			{
				var __this = $(this);
				
				if(__this.hasClass("second-click-first"))
					$(this).removeClass("ui-state-active second-click-first");
			});
		}
	});
	
	//定义fastjson输出的循环引用恢复函数
	$.extend(
	{
		//引用对象的引用属性名
		REF_NAME : "$ref",
		
		//引用根对象
		REF_VALUE_ROOT : "$",
		
		//引用上级对象
		REF_VALUE_PARENT : "..",
		
		//引用对象自身
		REF_VALUE_THIS : "@",
		
		//引用路径前缀
		REF_VALUE_PATH_PREFIX : "$",
		
		//$.unref处理标识
		UNREF_FLAG : "___DATA_GEAR_ZY_UNREF_FLAG___",
		
		/**
		 * 复制对象。
		 */
		deepClone : function(obj)
		{
			return this.unref(this.ref(obj));
		},
		
		/**
		 * 复制对象，并将对象内的重复引用替换为引用路径，格式为：{ $.REF_NAME : $.REF_VALUE... }。
		 * 
		 * param obj
		 */
		ref : function(obj)
		{
			return $._ref(null, null, obj, $.REF_VALUE_PATH_PREFIX, []);
		},
		
		/**
		 * 复制参数对象，并将参数值内的重复引用替换为“$ref”。
		 * 
		 * @param param 参数对象
		 */
		refParam : function(param)
		{
			if(typeof(param) == "object")
			{
				//数组
				if(Array.isArray(param))
				{
					var copyParam = [];
					
					for(var i=0; i<param.length; i++)
					{
						copyParam[i] = $.ref(param[i]);
					}
					
					param = copyParam;
				}
				else
				{
					var copyParam = {};
					
					for(var propName in param)
					{
						var propValue = param[propName];
						
						copyParam[propName] = $.ref(propValue);
					}
					
					param = copyParam;
				}
			}
			
			return param;
		},
		
		_ref : function(root, parent, obj, path, objPathArray)
		{
			var refObj = undefined;
			
			if(obj && typeof(obj) == "object" && !obj[$.REF_NAME])
			{
				if(obj == root)
				{
					refObj = {};
					refObj[$.REF_NAME] = $.REF_VALUE_ROOT;
				}
				else if(obj == parent)
				{
					refObj = {};
					refObj[$.REF_NAME] = $.REF_VALUE_PARENT;
				}
				else
				{
					for(var i=0; i<objPathArray.length; i++)
					{
						if(objPathArray[i].obj == obj)
						{
							refObj = {};
							refObj[$.REF_NAME] = objPathArray[i].path;
							break;
						}
					}
				}
				
				if(!refObj)
				{
					var myObjPath = { "obj" : obj, "path" : path };
					objPathArray.push(myObjPath);
					
					//数组
					if(Array.isArray(obj))
					{
						var copyArray = [];
						
						for(var i=0; i<obj.length; i++)
						{
							var element = obj[i];
							
							copyArray[i] = $._ref((root ? root : obj), obj, element, $.propertyPath.concatElementIndex(path, i), objPathArray);
						}
						
						refObj = copyArray;
					}
					else
					{
						var copyObj = {};
						
						for(var propName in obj)
						{
							var propValue = obj[propName];
							
							if(propValue == obj)
							{
								propValue = {};
								propValue[$.REF_NAME] = $.REF_VALUE_THIS;
							}
							else
								propValue = $._ref((root ? root : obj), obj, propValue, $.propertyPath.concatPropertyName(path, propName), objPathArray);
							
							copyObj[propName] = propValue;
						}
						
						refObj = copyObj;
					}
				}
			}
			
			return (refObj ? refObj : obj);
		},

		/**
		 * 判断给定对象是否包含重复引用。
		 */
		isRef : function(obj)
		{
			return $._isRef(obj, []);
		},
		
		_isRef : function(obj, objArray)
		{
			if(obj && typeof(obj) == "object")
			{
				for(var i=0; i<objArray.length; i++)
				{
					if(objArray[i] == obj)
						return true;
				}
				
				objArray.push(obj);
				
				//数组
				if(Array.isArray(obj))
				{
					for(var i=0; i<obj.length; i++)
					{
						var element = obj[i];
						
						if($._isRef(element, objArray))
							return true;
					}
				}
				else
				{
					for(var propName in obj)
					{
						var propValue = obj[propName];
						
						if($._isRef(propValue, objArray))
							return true;
					}
				}
			}
			
			return false;
		},
		
		/**
		 * 恢复由$.ref函数替换的的“$ref”引用。
		 * 
		 * @param obj
		 */
		unref: function(obj)
		{
			$._unref(obj, null, obj);
			$._removeUnrefFlag(obj);
			
			return obj;
		},
		
		/**
		 * 重定向对象中的fastjson关联（'$ref'属性值），这会在所有层级的对象上添加$.UNREF_FLAG属性。
		 * 
		 * @param root
		 * @param parent
		 * @param obj
		 */
		_unref: function(root, parent, obj)
		{
			if(root == undefined)
				return undefined;
			
			var objType=typeof(obj);
			
			if(obj && objType == "object")
			{
				//数组
				if(Array.isArray(obj))
				{
					for(var i=0; i<obj.length; i++)
					{
						var ele=obj[i];
						
						var ref=(ele ? ele[$.REF_NAME] : undefined);
						
						if(ref != undefined)
						{
							val = $._unrefValue(root, parent, obj, ref);
							obj[i] = val;
						}
						else
						{
							var newEle=$._unref(root, obj, ele);
							
							if(newEle != ele)
								obj[i] = newEle;
						}
					}
				}
				//对象
				else
				{
					if(obj[$.UNREF_FLAG])
						return;
					else
						obj[$.UNREF_FLAG]=true;
					
					for(var p in obj)
					{
						var val=obj[p];
						
						if(!val)
							continue;
						
						var ref=val[$.REF_NAME];
						
						if(ref != undefined)
						{
							val = $._unrefValue(root, parent, obj, ref);
							obj[p] = val;
						}
						else
						{
							var newVal=$._unref(root, obj, val);
							
							if(newVal != val)
								obj[p] = newVal;
						}
					}
				}
			}
			
			return obj;
		},
		
		/**
		 * 恢复“$ref”的值
		 */
		_unrefValue : function(root, parent, _this, ref)
		{
			var val = undefined;
			
			if(ref == $.REF_VALUE_ROOT)
			{
				val=root;
			}
			else if(ref == $.REF_VALUE_PARENT)
			{
				val = parent;
			}
			else if(ref == $.REF_VALUE_THIS)
			{
				val = _this;
			}
			else if(ref.indexOf($.REF_VALUE_PATH_PREFIX) == 0)
			{
				val=eval("root"+ref.substring(1));
			}
			else
				throw new Error("Unknown '"+$.REF_NAME+"' value '"+ref+"'");
			
			return val;
		},
		
		/**
		 * 移除对象中的$.UNREF_FLAG标记。
		 * 
		 * @param obj
		 */
		_removeUnrefFlag: function(obj)
		{
			if(obj == undefined)
				return;
			
			var objType=typeof(obj);
			
			if(objType == "object")
			{
				//数组
				if(obj.length != undefined)
				{
					for(var i=0; i<obj.length; i++)
					{
						$._removeUnrefFlag(obj[i]);
					}
				}
				//对象
				else
				{
					if(obj[$.UNREF_FLAG] == undefined)
						return;
					
					delete obj[$.UNREF_FLAG];
					
					for(var p in obj)
					{
						var val=obj[p];
						
						if(!val)
							continue;
						
						$._removeUnrefFlag(val);
					}
				}
			}
		}
	});
	
	/**
	 * 获取响应的JSON对象。
	 * 如果响应不是JSON格式，则返回null。
	 */
	$.getResponseJson = function(jqXHR)
	{
		if(jqXHR.responseJSON)
			return jqXHR.responseJSON;
		else
		{
			var responseContentType = (jqXHR.getResponseHeader("Content-Type") || "").toLowerCase();
			
			if(responseContentType.indexOf("json") > -1 && jqXHR.responseText)
			{
				var responseJSON = $.parseJSON(jqXHR.responseText);
				
				jqXHR.responseJSON = responseJSON;
				
				return responseJSON;
			}
			
			return null;
		}
	};

	$.handleAjaxOperationMessage = function(jqXHR, errorThrown)
	{
		if(!window._showAjaxOperationMessageDetail)
		{
			window._showAjaxOperationMessageDetail = function()
			{
				$.closeTip();
				
				var $operationMessageParent = $("#__operationMessageParent");
				
				var isSuccessMessage = ("true" == $operationMessageParent.attr("success"));
				
				var $dialog = $("<div id='dialog-"+new Date().getTime()+"' class='operation-message-dialog'></div>").appendTo(document.body);
				
				var $messageDetail = $("<div class='message-detail' />");
				if(!isSuccessMessage)
					$messageDetail.addClass("ui-state-error");
				$messageDetail.appendTo($dialog);
				$messageDetail.html($(".message-detail", $operationMessageParent).html());
				
				$._dialog($dialog,
						{
							title : $(".message", $operationMessageParent).text(),
							modal : true,
							height: "60%",
							position: {my: "center top", at: "center top+3"},
							classes:
							{
								"ui-dialog": "ui-corner-all ui-widget-shadow" + (isSuccessMessage ? "" : "ui-state-error")
							}
						});
				
				var $dialogWidget = $dialog.dialog("widget");
				
				$(".ui-dialog-title", $dialogWidget).prepend("<span class='ui-icon "+(isSuccessMessage ? "ui-icon-circle-check" : "ui-icon-alert")+"'></span>");
				
				if(!isSuccessMessage)
				{
					$dialogWidget.addClass("ui-state-error");
					$(".ui-dialog-titlebar", $dialogWidget).addClass("ui-state-error");
					$(".ui-dialog-titlebar-close", $dialogWidget).addClass("ui-state-error");
				}
			};
		}
		
		var isSuccessMessage = (jqXHR.status == 200);
		
		if(jqXHR.responseText)
		{
			var $operationMessageParent = $("#__operationMessageParent");
			if($operationMessageParent.length == 0)
				$operationMessageParent = $("<div id='__operationMessageParent' style='display:none;' />").appendTo(document.body);
			
			var operationMessage = $.getResponseJson(jqXHR);
			
			var hasMessage = false;
			
			//操作消息的JSON响应
			if(operationMessage && operationMessage.type && operationMessage.code && operationMessage.message)
			{
				$operationMessageParent.empty();
				
				var $omdiv = $("<div class='operation-message "+operationMessage.type+"' />").appendTo($operationMessageParent);
				var $mdiv = $("<div class='message' />").appendTo($omdiv).html(operationMessage.message);
				
				if(operationMessage.detail)
				{
					var $ddiv = $("<div class='message-detail' />").appendTo($omdiv);
					if(operationMessage.throwableDetail)
						$("<pre />").appendTo($ddiv).html(operationMessage.detail);
					else
						$("<div />").appendTo($ddiv).html(operationMessage.detail);
				}
				
				hasMessage = true;
			}
			else
			{
				if(isSuccessMessage)
					hasMessage = false;
				else
				{
					//操作消息的HTML响应
					$operationMessageParent.html(jqXHR.responseText);
					hasMessage = true;
				}
			}
			
			if(hasMessage)
			{
				$operationMessageParent.attr("success", isSuccessMessage);
				var message = $(".message", $operationMessageParent).html();
				
				if($(".message-detail", $operationMessageParent).length > 0)
					message += "<span class='ui-icon ui-icon-comment message-detail-icon' onclick='_showAjaxOperationMessageDetail();'></span>";
				
				if(isSuccessMessage)
					$.tipSuccess(message);
				else
					$.tipError(message);
			}
		}
		else
		{
			if(isSuccessMessage)
				;
			else
			{
				var msg = (jqXHR.statusText || "Error");
				
				if(errorThrown && errorThrown.message)
				{
					if(msg)
						msg += " : ";
					
					msg += errorThrown.message;
				}
				
				$.tipError(msg);
			}
		}
	};
	
	$.ajaxSetup(
	{
		global : true,
		cache : false,
		converters : 
		{
			"* text": window.String,
			"text html": true,
			"text json": function(data)
			{
				data=$.unref(jQuery.parseJSON(data));
				
				return data;
			},
			"text xml": jQuery.parseXML
		},
		
		//避免ajax深度复制参数数据
		flatOptions : {
			context : true,
			url : true,
			data : true,
			type : true,
			success : true
		}
	});
	
	$(document).ajaxError(function(event, jqXHR, ajaxSettings, thrownError)
	{
		$.handleAjaxOperationMessage(jqXHR, thrownError);
	});
	
	$(document).ajaxSuccess(function(event, jqXHR, ajaxSettings, thrownError)
	{
		$.handleAjaxOperationMessage(jqXHR, thrownError);
	});
})
(jQuery);