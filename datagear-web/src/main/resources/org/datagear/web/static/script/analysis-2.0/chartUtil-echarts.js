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
 * ECharts图表工具函数库。
 * 全局变量名：window.chartUtil.echarts
 * 
 * 加载时依赖：
 *   无
 * 
 * 运行时依赖:
 *   echarts
 */
(function(global)
{

var chartUtil = (global.chartUtil || (global.chartUtil = {}));
var echartsUtil = (chartUtil.echarts || (chartUtil.echarts = {}));

/**
 * 初始化图表的ECharts主题名。
 * 此函数依次从图表元素、<body>元素的elementAttrConst.ECHARTS_THEME属性获取ECharts主题名。
 */
echartsUtil._initEchartsThemeName = function()
{
	var themeName = CF.eleAttr(this.element(), elementAttrConst.ECHARTS_THEME);
	if(!themeName)
		themeName = CF.eleAttr(document.body, elementAttrConst.ECHARTS_THEME);
	
	this.echartsThemeName(themeName);
};


/**
 * ECharts图表支持函数：获取/设置图表的ECharts主题名。
 * 此函数用于为ECharts图表提供支持，如果不是ECharts图表，则不必设置此项。
 * 
 * 图表初始化时会使用图表元素的"dg-echarts-theme"属性值执行设置操作。
 * 
 * 图表渲染器实现相关：
 * 图表渲染器应使用此函数获取并应用ECharts主题。
 * 
 * @param themeName 可选，要设置的且已注册的ECharts主题名，没有则执行获取操作
 */
echartsUtil.echartsThemeName = function(themeName)
{
	if(themeName === undefined)
		return this._echartsThemeName;
	else
		this._echartsThemeName = themeName;
};


/**
 * ECharts图表支持函数：将图表初始化为ECharts图表，设置其选项。
 * 此函数会自动应用chartBase.echartsGetThemeName()至初始化的ECharts图表。
 * 此函数会自动调用chartBase.internal()将初始化的ECharts实例对象设置为图表底层组件。
 * 
 * @param options 要设置的ECharts选项，为null表示不设置
 * @param opts 可选，ECharts的init函数附加参数，具体参考ECharts.init()函数的opts参数
 * @returns ECharts实例对象
 */
chartBase.echartsInit = function(options, opts)
{
	var instance = echarts.init(this.element(), this.echartsGetThemeName(), opts);
	this.internal(instance);
	
	if(options != null)
	{
		instance.setOption(options);
	}
	
	return instance;
};

/**
 * ECharts图表支持函数：设置图表的ECharts实例的选项。
 * 
 * @param options
 * @param opts 可选，ECharts的setOption函数附加参数，具体参考ECharts.setOption()函数的opts参数
 */
chartBase.echartsOptions = function(options, opts)
{
	var internal = this.internal();
	
	if(!this._isEchartsInstance(internal))
		throw new Error("chart is not ECharts");
	
	internal.setOption(options, opts);
};

/**
 * 给定对象是否是ECharts实例。
 */
chartBase._isEchartsInstance = function(obj)
{
	return (obj && obj.setOption && obj.isDisposed && obj.dispose && obj.off);
};

/**
 * ECharts图表支持函数：判断指定名称的ECharts地图是否已经注册过而无需再加载。
 * 
 * @param name ECharts地图名称
 */
chartBase.echartsMapRegistered = function(name)
{
	return (echarts.getMap(name) != null);
};

/**
 * ECharts图表支持函数：加载并注册指定名称的ECharts地图（GeoJSON、SVG），并在注册完成后执行回调函数。
 * 注意：如果地图图表插件的render/update函数中调用此函数，应该首先设置插件的asyncRender/asyncUpdate，
 * 并在callback中调用chart.statusRendered(true)/chart.statusUpdated(true)，具体参考此文件顶部的注释。
 * 
 * @param name 地图名称
 * @param callback 可选，加载并注册完成后的回调函数，格式为：function(name, map, jqXHR){ ... }，或者也可以是JQuery的ajax配置项：{...}
 */
chartBase.echartsLoadMap = function(name, callback)
{
	var registerMap = function(name, map, jqXHR)
	{
		var contentType = (jqXHR.getResponseHeader("Content-Type") || "");
		
		//SVG地图
		if(/svg/i.test(contentType))
		{
			echarts.registerMap(name, {svg: map});
		}
		//其他都认为是GeoJSON地图
		else
		{
			echarts.registerMap(name, {geoJSON: map});
		}
	};
	
	if(callback == null)
		;
	else if(CF.isFunction(callback))
	{
		var originalCallback = callback;
		callback = function(name, map, jqXHR)
		{
			registerMap(name, map, jqXHR);
			originalCallback.call(this, name, map, jqXHR);
		};
	}
	//ajax配置项：{...}
	else
	{
		var settings = CF.extend({}, callback);
		var originalCallback = settings.success;
		settings.success = function(map, textStatus, jqXHR)
		{
			registerMap(name, map, jqXHR);
			
			if(originalCallback)
				originalCallback.call(this, map, textStatus, jqXHR);
		};
		
		callback = settings;
	}
	
	this.loadMap(name, callback);
};

/**
 * ECharts图表支持函数：解绑指定图表事件处理函数。
 * ECharts相关的图表渲染器可以在其off函数中调用此函数，以实现底层事件解绑功能。
 * 
 * @param eventType 图表事件类型
 * @param eventHanlder 可选，图表事件处理函数，格式为：function(chartEvent){ ... }，不设置则解绑所有此类型的图表事件处理函数
 * @returns 同chartBase.removeEventHandlerDelegation返回值
 */
chartBase.echartsOffEventHandler = function(eventType, eventHanlder)
{
	var internal = this.internal();
	
	return this.removeEventHandlerDelegation(eventType, eventHanlder, function(et, eh, ehd)
	{
		if(internal)
			internal.off(et, ehd);
	});
};


/**
 * ECharts图表支持函数：获取可用于此图表的且已注册的ECharts主题名。
 * 此函数优先返回chartBase.echartsThemeName()函数的结果，
 * 当其为null时，则使用chartBase.theme()构建和注册ECharts主题（仅第一次），并返回注册后的主题名。
 * 
 * @since 2.11.0
 */
chartBase.echartsGetThemeName = function()
{
	var themeName = this.echartsThemeName();
	
	//从ChartTheme构建ECharts主题
	if(!themeName)
	{
		var theme = this.theme();
		themeName = theme[CF._KEY_REGISTERED_ECHARTS_THEME_NAME];
		
		if(!themeName)
		{
			themeName = (theme[CF._KEY_REGISTERED_ECHARTS_THEME_NAME] = CF.uid());
			
			var echartsTheme = CF.buildEchartsTheme(theme);
			echarts.registerTheme(themeName, echartsTheme);
		}
	}
	
    return themeName;
};

	
})(this);
