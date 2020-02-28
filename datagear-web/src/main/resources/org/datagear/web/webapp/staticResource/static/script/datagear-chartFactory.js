/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 图表工厂，用于初始化图表对象，为图表对象添加功能函数。
 * 全局变量名：window.chartFactory。
 * 
 * 加载时依赖：
 *   无
 * 
 * 运行时依赖:
 *   jquery.js
 *   echarts.js
 * 
 * 此模块和dashboardFactory.js一起可以支持异步图表插件，示例如下：
 * {
 *   //声明render函数是否为异步，默认为false
 *   asyncRender: true || false || function(chart){ return true || false },
 *   
 *   render: function(chart)
 *   {
 *     $.get("...", function()
 *     {
 *       ...;
 *       
 *       //将图表状态设置为已完成render
 *       chart.statusPreUpdate();
 *     });
 *   },
 *   
 *   //声明update函数是否为异步，默认为false
 *   asyncUpdate: true || false || function(chart, results){ return true || false },
 *   
 *   update: function(chart, results)
 *   {
 *     $.get("...", function()
 *     {
 *       ...;
 *       
 *       //将图表状态设置为已完成update
 *       chart.statusUpdated();
 *     });
 *   }
 * }
 */
(function(global)
{
	var chartFactory = (global.chartFactory || (global.chartFactory = {}));
	var chartBase = (chartFactory.chartBase || (chartFactory.chartBase = {}));
	
	/**
	 * echarts地图名称及其JSON地址映射表，如果页面有地图图表，应该设置这个对象。
	 * 例如：{"china" : "/map/china.json", "beijing" : "map/beijing.json", , "shanghai" : "../map/shanghai.json"}
	 * 此对象也可以定义一个名为"mapURL"的函数属性，用于获取没有名称对应的地址，格式为：function(name){ return "..."; }
	 * 具体参考下面的chartBase.echartsMapURL函数
	 */
	if(!chartFactory.echartsMapURLs)
		chartFactory.echartsMapURLs = {};
	
	/**
	 * 初始化指定图表对象。
	 * 
	 * @param chart 图表对象
	 */
	chartFactory.init = function(chart)
	{
		$.extend(chart, this.chartBase);
		
		chart.statusPreRender(true);
	};
	
	/**
	 * 渲染图表。
	 */
	chartBase.render = function()
	{
		this.statusRendering(true);
		
		var async = this.isAsyncRender();
		var re = this.plugin.chartRenderer.render(this);
		
		if(!async)
		{
			if(re == false)
				this.statusPreRender(true);
			else
				this.statusPreUpdate(true);
		}
	};
	
	/**
	 * 更新图表。
	 * 
	 * @param results 图表数据集结果
	 */
	chartBase.update = function(results)
	{
		this.statusUpdating(true);
		
		var async = this.isAsyncUpdate(results);
		var re = this.plugin.chartRenderer.update(this, results);
		
		if(!async)
		{
			if(re == false)
				this.statusPreUpdate(true);
			else
				this.statusUpdated(true);
		}
	};
	
	/**
	 * 图表的render方法是否是异步的。
	 */
	chartBase.isAsyncRender = function()
	{
		var chartRenderer = this.plugin.chartRenderer;
		
		if(chartRenderer.asyncRender == undefined)
			return false;
		
		if(typeof(chartRenderer.asyncRender) == "function")
			return chartRenderer.asyncRender(this);
		
		return (chartRenderer.asyncRender == true);
	};
	
	/**
	 * 图表的update方法是否是异步的。
	 * 
	 * @param results 图表数据集结果
	 */
	chartBase.isAsyncUpdate = function(results)
	{
		var chartRenderer = this.plugin.chartRenderer;
		
		if(chartRenderer.asyncUpdate == undefined)
			return false;
		
		if(typeof(chartRenderer.asyncUpdate) == "function")
			return chartRenderer.asyncUpdate(this, results);
		
		return (chartRenderer.asyncUpdate == true);
	};
	
	/**
	 * 图表是否可为/设置为：准备render。
	 * 
	 * @param set undefined时判断状态，否则，设置状态。
	 */
	chartBase.statusPreRender = function(set)
	{
		if(set == undefined)
			return (this.status() == chartFactory.STATUS_PRE_RENDER);
		else
			this.status(chartFactory.STATUS_PRE_RENDER);
	};
	
	/**
	 * 图表是否可为/设置为：正在render。
	 * 
	 * @param set undefined时判断状态，否则，设置状态。
	 */
	chartBase.statusRendering = function(set)
	{
		if(set == undefined)
			return (this.status() == chartFactory.STATUS_RENDERING);
		else
			this.status(chartFactory.STATUS_RENDERING);
	};
	
	/**
	 * 图表是否可为/设置为：准备update。
	 * 
	 * @param set undefined时判断状态，否则，设置状态。
	 */
	chartBase.statusPreUpdate = function(set)
	{
		if(set == undefined)
			return (this.status() == chartFactory.STATUS_PRE_UPDATE);
		else
			this.status(chartFactory.STATUS_PRE_UPDATE);
	};
	
	/**
	 * 图表是否可为/设置为：正在update。
	 * 
	 * @param set undefined时判断状态，否则，设置状态。
	 */
	chartBase.statusUpdating = function(set)
	{
		if(set == undefined)
			return (this.status() == chartFactory.STATUS_UPDATING);
		else
			this.status(chartFactory.STATUS_UPDATING);
	};
	
	/**
	 * 图表是否可为/设置为：完成update。
	 * 
	 * @param set undefined时判断状态，否则，设置状态。
	 */
	chartBase.statusUpdated = function(set)
	{
		if(set == undefined)
			return (this.status() == chartFactory.STATUS_UPDATED);
		else
			this.status(chartFactory.STATUS_UPDATED);
	};
	
	/**
	 * 获取/设置图表状态。
	 * 
	 * @param status 要设置的状态，可选，不设置则执行获取操作
	 */
	chartBase.status = function(status)
	{
		if(status == undefined)
			return (this._status || chartFactory.STATUS_PRE_RENDER);
		else
			this._status = (status || chartFactory.STATUS_PRE_RENDER);
	};
	
	/**
	 * 获取图表的DOM元素。
	 */
	chartBase.element = function()
	{
		return document.getElementById(this.elementId);
	};
	
	/**
	 * 获取图表的DOM元素的Jquery对象。
	 */
	chartBase.elementJquery = function()
	{
		return $("#" + this.elementId);
	};
	
	/**
	 * 获取图表名称。
	 */
	chartBase.nameNonNull = function()
	{
		return (this.name || "");
	};
	
	/**
	 * 获取图表的更新间隔。
	 */
	chartBase.updateIntervalNonNull = function()
	{
		if(this.updateInterval != null)
			return this.updateInterval;
		
		return -1;
	},

	/**
	 * 获取图表设置项。
	 * 它读取图表DOM元素和body元素上的"dg-chart-options"属性定义的图表设置项。
	 * 
	 * @param options 初始设置项，可选，默认为：{}
	 * @return {...}
	 */
	chartBase.options = function(options)
	{
		options = (options || {});
		
		var $ele = this.elementJquery();
		
		//元素属性选项
		var optsStr = $ele.attr("dg-chart-options");
		//全局选项
		var optsStrGlobal = $(document.body).attr("dg-chart-options");
		
		if(!optsStr && !optsStrGlobal)
			return options;
		
		var opts = chartFactory.evalSilently(optsStr, {});
		var optsGlobal = chartFactory.evalSilently(optsStrGlobal, {});
		
		options = $.extend(true, options, optsGlobal, opts);
		
		return options;
	};
	
	/**
	 * 获取图表主题，没有则返回undefined。
	 * 它读取body元素上的"dg-chart-theme"属性值作为自定义主题。
	 * 
	 * @return {...}
	 */
	chartBase.theme = function()
	{
		var chartTheme = this.renderContextAttr("chartTheme");
		var bodyTheme = $(document.body).attr("dg-chart-theme");
		
		if(!chartTheme && !bodyTheme)
			return undefined;
		
		if(!chartTheme)
			chartTheme = {};
		
		if(chartTheme._BODY_THEME == bodyTheme)
			return chartTheme;
		
		if(bodyTheme)
			$.extend(true, chartTheme, chartFactory.evalSilently(bodyTheme, {}));
		
		chartTheme._BODY_THEME = bodyTheme;
		
		return chartTheme;
	};
	
	/**
	 * 获取图表渲染风格，没有则返回undefined。
	 * 
	 * @return "..."
	 */
	chartBase.renderStyle = function()
	{
		return this.renderContextAttr("renderStyle");
	};
	
	/**
	 * 获取/设置图表渲染上下文的属性值。
	 * 
	 * @param attrName
	 * @param attrValue 要设置的属性值，可选，不设置则执行获取操作
	 */
	chartBase.renderContextAttr = function(attrName, attrValue)
	{
		var renderContext = this.renderContext;
		
		if(attrValue == undefined)
			return renderContext.attributes[attrName];
		else
			return renderContext.attributes[attrName] = attrValue;
	};
	
	/**
	 * 获取/设置图表属性值。
	 * 
	 * @param name 属性名
	 * @param value 要设置的属性值，可选，不设置则执行获取操作
	 */
	chartBase.propertyValue = function(name, value)
	{
		if(value == undefined)
			return (this.properties ? this.properties[name] : undefined);
		else
		{
			if(!this.properties)
				this.properties = {};
			this.properties[name] = value;
		}
	};

	/**
	 * 获取/设置图表参数值。
	 * 
	 * @param name 参数名
	 * @param value 要设置的参数值，可选，不设置则执行获取操作
	 */
	chartBase.paramValue = function(name, value)
	{
		if(value == undefined)
			return (this.params ? this.params[name] : undefined);
		else
		{
			if(!this.params)
				this.params = {};
			this.params[name] = value;
		}
	};
	
	/**
	 * 获取/设置扩展值。
	 * 图表插件渲染器可以用此函数在render()、update()间传递数据，避免与图表本身的属性命名冲突。
	 * 
	 * @param name 扩展名
	 * @param value 要设置的扩展值，可选，不设置则执行获取操作
	 */
	chartBase.extValue = function(name, value)
	{
		if(value == undefined)
			return (this._extValues ? this._extValues[name] : undefined);
		else
		{
			if(!this._extValues)
				this._extValues = {};
			this._extValues[name] = value;
		}
	};
	
	/**
	 * 获取图表数据集对象数组。
	 */
	chartBase.chartDataSetsNonNull = function()
	{
		return (this.chartDataSets || []);
	};
	
	/**
	 * 获取第一个图表数据集对象。
	 * 
	 * @param chart
	 * @return {...} 或  undefined
	 */
	chartBase.chartDataSetFirst = function()
	{
		return this.chartDataSetAt(0);
	};
	
	/**
	 * 获取指定索引的图表数据集对象，没有则返回undefined。
	 * 
	 * @param index
	 */
	chartBase.chartDataSetAt = function(index)
	{
		return (!this.chartDataSets || this.chartDataSets.length <= index ? undefined : this.chartDataSets[index]);
	};
	
	/**
	 * 获取指定图表数据集的名称，它不会返回null。
	 * 
	 * @param chartDataSet 图表数据集对象
	 */
	chartBase.dataSetName = function(chartDataSet)
	{
		if(!chartDataSet)
			return "";
		
		var dataSet = (chartDataSet.dataSet || chartDataSet);
		
		return (dataSet ? (dataSet.name || "") : "");
	};
	
	/**
	 * 获取指定标记的第一个数据集属性，没有则返回undefined。
	 * 
	 * @param chartDataSet 图表数据集对象
	 * @param dataSign 数据标记对象、标记名称
	 * @return {...}
	 */
	chartBase.dataSetPropertyOfSign = function(chartDataSet, dataSign)
	{
		var properties = this.dataSetPropertiesOfSign(chartDataSet, dataSign);
		
		return (properties.length > 0 ? properties[0] : undefined);
	};
	
	/**
	 * 获取指定标记的数据集属性数组。
	 * 
	 * @param chartDataSet 图表数据集对象
	 * @param dataSign 数据标记对象、标记名称
	 * @return [...]
	 */
	chartBase.dataSetPropertiesOfSign = function(chartDataSet, dataSign)
	{
		var re = [];
		
		if(!chartDataSet || !chartDataSet.dataSet || !dataSign)
			return re;
		
		dataSign = (dataSign.name || dataSign);
		var dataSetProperties = (chartDataSet.dataSet.properties || []);
		var propertySigns = (chartDataSet.propertySigns || {});
		
		var signPropertyNames = [];
		
		for(var pname in propertySigns)
		{
			var mySigns = (propertySigns[pname] || []);
			
			for(var i=0; i<mySigns.length; i++)
			{
				if(mySigns[i] == dataSign || mySigns[i].name == dataSign)
				{
					signPropertyNames.push(pname);
					break;
				}
			}
		}
		
		for(var i=0; i<dataSetProperties.length; i++)
		{
			for(var j=0; j<signPropertyNames.length; j++)
			{
				if(dataSetProperties[i].name == signPropertyNames[j])
					re.push(dataSetProperties[i]);
			}
		}
		
		return re;
	};
	
	/**
	 * 获取数据集属性标签，它不会返回null。
	 * 
	 * @param dataSetProperty
	 * @return "..."
	 */
	chartBase.dataSetPropertyLabel = function(dataSetProperty)
	{
		if(!dataSetProperty)
			return "";
		
		var label = (dataSetProperty.label ||  dataSetProperty.name);
		
		return (label || "");
	};

	/**
	 * 返回第一个数据集结果，没有则返回undefined。
	 * 
	 * @param results
	 */
	chartBase.resultFirst = function(results)
	{
		return this.resultAt(results, 0);
	};
	
	/**
	 * 返回指定索引的数据集结果，没有则返回undefined。
	 * 
	 * @param results
	 * @param index
	 */
	chartBase.resultAt = function(results, index)
	{
		return (!results || results.length <= index ? undefined : results[index]);
	};
	
	/**
	 * 将数据集结果的行对象按照指定properties顺序转换为行值数组。
	 * 
	 * @param result 数据集结果对象、对象数组
	 * @param properties 数据集属性对象数组、属性名数组、属性对象、属性名
	 * @param row 行索引，可选，默认为0
	 * @param count 获取的最多行数，可选，默认为全部
	 * @return properties为数组时：[[..., ...], ...]；properties非数组时：[..., ...]
	 */
	chartBase.resultRowArrays = function(result, properties, row, count)
	{
		var re = [];
		
		if(!result || !properties)
			return re;
		
		var datas = (result.length != undefined ? result : (result.datas || []));
		
		row = (row || 0);
		var getCount = datas.length;
		if(count != null && count < getCount)
			getCount = count;
		
		if(properties.length > 0)
		{
			for(var i=row; i< getCount; i++)
			{
				var rowObj = datas[i];
				var row = [];
				
				for(var j=0; j<properties.length; j++)
				{
					var p = properties[j];
					
					var name = (p ? (p.name || p) : undefined);
					if(!name)
						continue;
					
					row[j] = rowObj[name];
				}
				
				re.push(row);
			}
		}
		else
		{
			var name = (properties ? (properties.name || properties) : undefined);
			
			if(name)
			{
				for(var i=row; i< getCount; i++)
				{
					var rowObj = datas[i];
					re.push(rowObj[name]);
				}
			}
		}
		
		return re;
	};
	
	/**
	 * 将数据集结果的行对象按照指定properties顺序转换为列值数组。
	 * 
	 * @param result 数据集结果对象、对象数组
	 * @param properties 数据集属性对象数组、属性名数组、属性对象、属性名
	 * @param row 行索引，以0开始，可选，默认为0
	 * @param count 获取的最多行数，可选，默认为全部
	 * @return properties为数组时：[[..., ...], ...]；properties非数组时：[..., ...]
	 */
	chartBase.resultColumnArrays = function(result, properties, row, count)
	{
		var re = [];

		if(!result || !properties)
			return re;
		
		var datas = (result.length != undefined ? result : (result.datas || []));
		
		row = (row || 0);
		var getCount = datas.length;
		if(count != null && count < getCount)
			getCount = count;
		
		if(properties.length > 0)
		{
			for(var i=0; i<properties.length; i++)
			{
				var p = properties[i];
				
				var name = (p ? (p.name || p) : undefined);
				if(!name)
					continue;
				
				var column = [];
				
				for(var j=row; j< getCount; j++)
					column.push(datas[j][name]);
				
				re[i] = column;
			}
		}
		else
		{
			var name = (properties ? (properties.name || properties) : undefined);

			if(name)
			{
				for(var i=row; i< getCount; i++)
				{
					var rowObj = datas[i];
					re.push(rowObj[name]);
				}
			}
		}
		
		return re;
	};
	
	/**
	 * 获取数据集结果的名称/值对象数组。
	 * 
	 * @param result 数据集结果对象、对象数组
	 * @param nameProperty 名称属性对象、属性名
	 * @param valueProperty 值属性对象、属性名
	 * @param row 行索引，以0开始，可选，默认为0
	 * @param count 获取结果数据的最多行数，可选，默认为全部
	 * @return [{name: ..., value: ...}, ...]
	 */
	chartBase.resultNameValueObjects = function(result, nameProperty, valueProperty, row, count)
	{
		var re = [];
		
		if(!result || !nameProperty || !valueProperty)
			return re;
		
		var datas = (result.length != undefined ? result : (result.datas || []));
		
		row = (row || 0);
		var getCount = datas.length;
		if(count != null && count < getCount)
			getCount = count;
		
		nameProperty = (nameProperty.name || nameProperty);
		valueProperty = (valueProperty.name || valueProperty);
		
		for(var i=row; i< getCount; i++)
		{
			var obj =
			{
				"name" : datas[i][nameProperty],
				"value" : datas[i][valueProperty]
			};
			
			re.push(obj);
		}
		
		return re;
	};

	/**
	 * 获取数据集结果指定属性、指定行的单元格值，没有则返回undefined。
	 * 
	 * @param result 数据集结果对象
	 * @param property 数据集属性对象、属性名
	 * @param row 行索引，可选，默认为0
	 */
	chartBase.resultCell = function(result, property, row)
	{
		row = (row || 0);
		
		var re = this.resultRowArrays(result, property, row, 1);
		
		return (re.length > 0 ? re[0] : undefined);
	};
	
	/**
	 * 初始化Echarts对象。
	 * 
	 * @param options echarts设置项
	 * @param customized 是否为options扩展自定义设置项，可选，默认为true
	 * @return echarts实例对象
	 */
	chartBase.echartsInit = function(options, customized)
	{
		if(customized != false)
			options = this.options(options);
		
		var instance = echarts.init(this.element(), this.echartsThemeName());
		instance.setOption(options);
		
		this._echartsInstance = instance;
		
		return instance;
	};
	
	/**
	 * 获取echarts实例对象。
	 */
	chartBase.echartsInstance = function()
	{
		return this._echartsInstance;
	};
	
	/**
	 * 设置echarts实例的选项值。
	 * 
	 * @params options
	 */
	chartBase.echartsOptions = function(options)
	{
		var instance = this.echartsInstance();
		instance.setOption(options);
	};
	
	/**
	 * 获取与此图表主题对应的且已注册至echarts的主题名，如果没有，则返回undefined。
	 */
	chartBase.echartsThemeName = function()
	{
		var chartTheme = this.theme();
		
		if(!chartTheme)
			return undefined;
		
		var regThemeName = $(document.body).attr("dg-chart-registered-echarts-theme");
		
		if(regThemeName)
			return regThemeName;
		
		var themeName = "dg-echarts-theme";
		
		var theme = this.echartsBuildTheme(chartTheme);
		echarts.registerTheme(themeName, theme);
		$(document.body).attr("dg-chart-registered-echarts-theme", themeName);
	    
	    return themeName;
	};
	
	/**
	 * 构建echarts主题。
	 * 
	 * @param chartTheme 图表主题
	 */
	chartBase.echartsBuildTheme = function(chartTheme)
	{
		var theme =
		{
			"color" : chartTheme.graphColors,
			"backgroundColor" : chartTheme.backgroundColor,
			"textStyle" : {},
			"title" : {
		        left: "center",
				"textStyle" : {
					"color" : chartTheme.color
				},
				"subtextStyle" : {
					"color" : chartTheme.colorSecond
				}
			},
			"line" : {
				"itemStyle" : {
					"normal" : {
						"borderWidth" : 1
					}
				},
				"lineStyle" : {
					"normal" : {
						"width" : 2
					}
				},
				"symbolSize" : 4,
				"symbol" : "circle",
				"smooth" : false
			},
			"radar" : {
				"name" : { "textStyle" : { "color" : chartTheme.colorSecond } },
				"axisLine" : { "lineStyle" : { "color" : chartTheme.colorFourth } },
				"splitLine" : { "lineStyle" : { "color" : chartTheme.colorFourth } },
				"splitArea" : { "areaStyle" : { "color" : [ chartTheme.colorFifth, chartTheme.backgroundColor ] } },
				"itemStyle" : {
					"normal" : {
						"borderWidth" : 1
					}
				},
				"lineStyle" : {
					"normal" : {
						"width" : 2
					}
				},
				"symbolSize" : 4,
				"symbol" : "circle",
				"smooth" : false
			},
			"bar" : {
				"itemStyle" : {
					"barBorderWidth" : 0,
					"barBorderColor" : chartTheme.colorFourth
				},
				"emphasis" : {
					"itemStyle" : {
						"barBorderWidth" : 0,
						"shadowBlur" : 10,
						"shadowOffsetX" : 0,
				        "shadowOffsetY" : 0,
						"barBorderColor" : chartTheme.colorFifth
					}
				}
			},
			"pie" : {
				"itemStyle" : {
					"borderWidth" : 0,
					"borderColor" : chartTheme.colorFourth
				},
				"emphasis" :
				{
					"itemStyle":
					{
						"shadowBlur" : 10,
						"shadowOffsetX" : 0,
						"shadowColor" : chartTheme.colorThird,
						"borderWidth" : 0,
						"borderColor" : chartTheme.colorThird
					}
				}
			},
			"scatter" : {
				"itemStyle" : {
					"barBorderWidth" : 0,
					"barBorderColor" : chartTheme.colorFourth
				},
				"emphasis" : {
					"itemStyle" : {
						"barBorderWidth" : 0,
						"barBorderColor" : chartTheme.colorThird
					}
				}
			},
			"boxplot" : {
				"itemStyle" : {
					"borderWidth" : 0,
					"borderColor" : chartTheme.colorFourth
				},
				"emphasis" : {
					"itemStyle" : {
						"barBorderWidth" : 0,
						"barBorderColor" : chartTheme.colorThird
					}
				}
			},
			"parallel" : {
				"itemStyle" : {
					"borderWidth" : 0,
					"borderColor" : chartTheme.colorFourth
				},
				"emphasis" : {
					"itemStyle" : {
						"barBorderWidth" : 0,
						"barBorderColor" : chartTheme.colorThird
					}
				}
			},
			"sankey" : {
				"itemStyle" : {
					"borderWidth" : 0,
					"borderColor" : chartTheme.colorFourth
				},
				"emphasis" : {
					"itemStyle" : {
						"barBorderWidth" : 0,
						"barBorderColor" : chartTheme.colorThird
					}
				}
			},
			"funnel" : {
				"label" : {
					"color" : chartTheme.colorSecond,
					"show": true,
	                "position": "inside"
	            },
				"itemStyle" : {
					"borderColor" : chartTheme.colorFourth,
					"borderWidth" : 0
				},
				"emphasis" : {
					"label" : {
	                    "fontSize" : 20
	                },
					"itemStyle" : {
						"barBorderWidth" : 0,
						"barBorderColor" : chartTheme.colorThird
					}
				}
			},
			"gauge" : {
				"title" : { color : chartTheme.colorSecond },
				"itemStyle" : {
					"borderColor" : chartTheme.colorFourth,
					"borderWidth" : 0
				},
				"emphasis" : {
					"itemStyle" : {
						"barBorderWidth" : 0,
						"barBorderColor" : chartTheme.colorThird
					}
				}
			},
			"candlestick" : {
				"itemStyle" : {
					"normal" : {
						"color" : chartTheme.graphColors[0],
						"color0" : chartTheme.graphColors[1],
						"borderColor" : chartTheme.colorThird,
						"borderColor0" : chartTheme.colorFourth,
						"borderWidth" : 1
					}
				}
			},
			"graph" : {
				"itemStyle" : {
					"normal" : {
						"borderWidth" : 0,
						"borderColor" : chartTheme.colorThird
					}
				},
				"lineStyle" : {
					"normal" : {
						"width" : 1,
						"color" : chartTheme.colorFourth
					}
				},
				"symbolSize" : 4,
				"symbol" : "circle",
				"smooth" : false,
				"color" : chartTheme.graphColors,
				"label" : {
					"normal" : {
						"textStyle" : {
							"color" : chartTheme.colorSecond
						}
					}
				}
			},
			"map" : {
				"itemStyle" : {
					"areaColor" : chartTheme.colorFifth,
					"borderColor" : chartTheme.colorThird,
					"borderWidth" : 0.5
				},
				"label" : {
					"color" : chartTheme.colorSecond
				},
				"emphasis" :
				{
					"label":
					{
						"color" : chartTheme.highlightTheme.color
					},
					"itemStyle":
					{
						"areaColor" : chartTheme.highlightTheme.backgroundColor,
						"borderColor" : chartTheme.highlightTheme.borderColor,
						"borderWidth" : 1
					}
				}
			},
			"geo" : {
				"itemStyle" : {
					"areaColor" : chartTheme.colorFifth,
					"borderColor" : chartTheme.colorThird,
					"borderWidth" : 0.5
				},
				"label" : {
					"color" : chartTheme.colorSecond
				},
				"emphasis" :
				{
					"label":
					{
						"color" : chartTheme.highlightTheme.color
					},
					"itemStyle":
					{
						"areaColor" : chartTheme.highlightTheme.backgroundColor,
						"borderColor" : chartTheme.highlightTheme.borderColor,
						"borderWidth" : 1
					}
				}
			},
			"categoryAxis" : {
				"axisLine" : {
					"show" : true,
					"lineStyle" : {
						"color" : chartTheme.colorThird
					}
				},
				"axisTick" : {
					"show" : true,
					"lineStyle" : {
						"color" : chartTheme.colorThird
					}
				},
				"axisLabel" : {
					"show" : true,
					"textStyle" : {
						"color" : chartTheme.colorSecond
					}
				},
				"splitLine" : {
					"show" : true,
					"lineStyle" : {
						"type" : "dotted",
						"color" : [ chartTheme.colorFourth ]
					}
				},
				"splitArea" : {
					"show" : false,
					"areaStyle" : {
						"color" : [ chartTheme.colorFourth ]
					}
				}
			},
			"valueAxis" : {
				"axisLine" : {
					"show" : true,
					"lineStyle" : {
						"color" : chartTheme.colorThird
					}
				},
				"axisTick" : {
					"show" : true,
					"lineStyle" : {
						"color" : chartTheme.colorThird
					}
				},
				"axisLabel" : {
					"show" : true,
					"textStyle" : {
						"color" : chartTheme.colorSecond
					}
				},
				"splitLine" : {
					"show" : true,
					"lineStyle" : {
						"type" : "dotted",
						"color" : [ chartTheme.colorFourth ]
					}
				},
				"splitArea" : {
					"show" : false,
					"areaStyle" : {
						"color" : [ chartTheme.colorFourth ]
					}
				}
			},
			"logAxis" : {
				"axisLine" : {
					"show" : true,
					"lineStyle" : {
						"color" : chartTheme.colorThird
					}
				},
				"axisTick" : {
					"show" : true,
					"lineStyle" : {
						"color" : chartTheme.colorThird
					}
				},
				"axisLabel" : {
					"show" : true,
					"textStyle" : {
						"color" : chartTheme.colorSecond
					}
				},
				"splitLine" : {
					"show" : true,
					"lineStyle" : {
						"type" : "dotted",
						"color" : [ chartTheme.colorFourth ]
					}
				},
				"splitArea" : {
					"show" : false,
					"areaStyle" : {
						"color" : [ chartTheme.colorFourth ]
					}
				}
			},
			"timeAxis" : {
				"axisLine" : {
					"show" : true,
					"lineStyle" : {
						"color" : chartTheme.colorThird
					}
				},
				"axisTick" : {
					"show" : true,
					"lineStyle" : {
						"color" : chartTheme.colorThird
					}
				},
				"axisLabel" : {
					"show" : true,
					"textStyle" : {
						"color" : chartTheme.colorSecond
					}
				},
				"splitLine" : {
					"show" : true,
					"lineStyle" : {
						"type" : "dotted",
						"color" : [ chartTheme.colorFourth ]
					}
				},
				"splitArea" : {
					"show" : false,
					"areaStyle" : {
						"color" : [ chartTheme.colorFourth ]
					}
				}
			},
			"toolbox" : {
				"iconStyle" : {
					"normal" : {
						"borderColor" : chartTheme.colorFourth
					},
					"emphasis" : {
						"borderColor" : chartTheme.colorThird
					}
				}
			},
			"grid":
			{
				"left": 30,
				"right": 46,
				"top": 80,
				"bottom": 20,
				"containLabel": true
			},
			"legend" : {
				"orient": "horizontal",
				"top": 25,
				"textStyle" : {
					"color" : chartTheme.colorSecond
				},
				"inactiveColor" : chartTheme.colorThird
			},
			"tooltip" : {
				"backgroundColor" : chartTheme.tooltipTheme.backgroundColor,
				"textStyle" : { color: chartTheme.tooltipTheme.color },
				"axisPointer" : {
					"lineStyle" : {
						"color" : chartTheme.colorSecond,
						"width" : "1"
					},
					"crossStyle" : {
						"color" : chartTheme.colorSecond,
						"width" : "1"
					}
				}
			},
			"timeline" : {
				"lineStyle" : {
					"color" : chartTheme.colorThird,
					"width" : 1
				},
				"itemStyle" : {
					"normal" : {
						"color" : chartTheme.colorThird,
						"borderWidth" : 1
					},
					"emphasis" : {
						"color" : chartTheme.colorSecond
					}
				},
				"controlStyle" : {
					"normal" : {
						"color" : chartTheme.colorThird,
						"borderColor" : chartTheme.colorThird,
						"borderWidth" : 0.5
					},
					"emphasis" : {
						"color" : chartTheme.colorThird,
						"borderColor" : chartTheme.colorThird,
						"borderWidth" : 0.5
					}
				},
				"checkpointStyle" : {
					"color" : chartTheme.highlightTheme.backgroundColor,
					"borderColor" : chartTheme.highlightTheme.borderColor
				},
				"label" : {
					"normal" : {
						"textStyle" : {
							"color" : chartTheme.colorThird
						}
					},
					"emphasis" : {
						"textStyle" : {
							"color" : chartTheme.colorThird
						}
					}
				}
			},
			"visualMap" : {
				"inRange" :
				{
					"color" : ['#58A52D', '#FFD700', '#FF4500']
				},
				"backgroundColor" : chartTheme.backgroundColor,
				"textStyle" :
				{
					"color" : chartTheme.colorSecond
				}
			},
			"dataZoom" : {
				"backgroundColor" : "red",
				"dataBackgroundColor" : chartTheme.colorFourth,
				"fillerColor" : chartTheme.colorThird,
				"handleColor" : chartTheme.colorSecond,
				"handleSize" : "100%",
				"textStyle" : {
					"color" : chartTheme.colorSecond
				}
			},
			"markPoint" : {
				"label" : {
					"normal" : {
						"textStyle" : {
							"color" : chartTheme.colorThird
						}
					},
					"emphasis" : {
						"textStyle" : {
							"color" : chartTheme.colorThird
						}
					}
				}
			}
		};
		
		return theme;
	};
	
	/**
	 * 指定名称的echarts地图是否已经注册过而无需再加载。
	 * 
	 * @param name echarts地图名称
	 */
	chartBase.echartsMapRegistered = function(name)
	{
		return (echarts.getMap(name) != null);
	};
	
	/**
	 * 获取echarts指定名称的地图JSON地址，如果找不到，则直接将name作为地址返回。
	 * 
	 * @param name echarts地图名称
	 */
	chartBase.echartsMapURL = function(name)
	{
		var url = chartFactory.echartsMapURLs[name];
		
		if(!url && typeof(chartFactory.echartsMapURLs.mapURL) == "function")
			url = chartFactory.echartsMapURLs.mapURL(name);
		
		url = (url || name);
		
		var contextPath = this.renderContext.webContext.contextPath;
		
		if(contextPath && url.indexOf("/") == 0 && url.indexOf(contextPath) != 0)
			url = contextPath + url;
		
		return url;
	};
	
	/**
	 * 加载指定名称的echarts地图JSON，并在完成后执行回调函数。
	 * 注意：如果地图图表插件的render/update函数中调用此函数，应该首先设置插件的asyncRender/asyncUpdate，
	 * 并在callback中调用chart.statusPreUpdate()/chart.statusUpdated()，具体参考此文件顶部的注释。
	 * 
	 * @param name echarts地图名称
	 * @param callback 完成回调函数：function(){ ... }
	 */
	chartBase.echartsMapLoad = function(name, callback)
	{
		var url = this.echartsMapURL(name);
		$.getJSON(url, function(geoJson)
		{
			echarts.registerMap(name, geoJson);
			
			if(callback)
				callback();
		});
	};
	
	/**
	 * 执行JS代码。
	 * 
	 * @param str JS代码
	 * @param defaultValue 默认返回值，可选，默认为：undefined
	 */
	chartFactory.evalSilently = function(str, defaultValue)
	{
		var re = undefined;
		
		try
		{
			re = Function("return ("+str+");")();
		}
		catch(e)
		{
			this.logException(e);
		}
		
		return (re || defaultValue);
	};
	
	/**
	 * 解析JSON。
	 * 如果参数不合法，将返回空对象：{}。
	 */
	chartFactory.parseJSONSilently = function(str)
	{
		if(!str)
			return {};
		
		try
		{
			if(typeof($) != "undefined" && $.parseJSON)
				return $.parseJSON(str);
			else
				return JSON.parse(str);
		}
		catch(e)
		{
			this.logException(e);
		}
		
		return {};
	};
	
	/**
	 * 将不符合JSON规范的字符串定义规范化：属性名添加双引号、或将属性名单引号替换为双引号。
	 */
	chartFactory.trimJSONString = function(str)
	{
		if(!str)
			return str;
		
		//替换单引号为双引号
		var str1 = "";
		for(var i=0; i<str.length;i++)
		{
			var c = str.charAt(i);
			
			if(c == '\\')
			{
				str1 += c;
				i = i+1;
				str1 += str.charAt(i);
			}
			else if(c == '\'')
				str1 += '"';
			else
				str1 += c;
		}
		
		str = str1;
		
		//属性名匹配表达式
		var reg = /([{,]\s*)([^\:\s]*)(\s*:)/g;
		
		return str.replace(reg, function(token, prefix, name, suffix)
		{
			var len = name.length;
			
			if(len > 1 && name.charAt(0) == '"' && name.charAt(len-1) == '"')
				return token;
			else if(len > 1 && name.charAt(0) == '\'' && name.charAt(len-1) == '\'')
			{
				name = '"' + name.substring(1, len-1) + '"';
				return prefix + name + suffix;
			}
			else
				return prefix + '"' + name + '"' + suffix;
		});
	};
	
	/**
	 * 记录异常日志。
	 * 
	 * @param exception 异常对象
	 */
	chartFactory.logException = function(exception)
	{
		if(typeof console != "undefined")
		{
			if(console.error)
				console.error(exception);
			else if(console.warn)
				console.warn(exception);
			else if(console.info)
				console.info(exception);
		}
	};
	
	/**图表状态：准备render*/
	chartFactory.STATUS_PRE_RENDER = "PRE_RENDER";
	
	/**图表状态：正在render*/
	chartFactory.STATUS_RENDERING = "RENDERING";
	
	/**图表状态：准备update*/
	chartFactory.STATUS_PRE_UPDATE = "PRE_UPDATE";
	
	/**图表状态：正在update*/
	chartFactory.STATUS_UPDATING = "UPDATING";
	
	/**图表状态：完成update*/
	chartFactory.STATUS_UPDATED = "UPDATED";
})
(this);