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
 * 图表工厂，用于初始化图表对象，为图表对象添加功能函数。
 */
(function(global)
{
	/**图表工厂*/
	var chartFactory = (global.chartFactory || (global.chartFactory = {}));
	
	/**
	 * 加载指定图表插件渲染器依赖库（plugin.renderer.depends），并在加载后执行回调函数。
	 * 插件渲染器结构：
	 * {
	 *   depends: 依赖库对象、[ 依赖库对象, ... ],
	 *   //其他渲染器属性
	 *   ...
	 * }
	 * 其中，依赖库对象结构为：
	 * {
	 *   //库名称，应尽量使用库本身定义的全局名称
	 *   name: "..."、[ "...", ... ],
	 *   //版本号，应符合语义化版本规范："X.Y.Z"、"X.Y.Z-BUILD"
	 *   version: "...",
	 *   //库源
	 *   source:
	 *   //库源URL
	 *   "..."、
	 *   //库源对象
	 *   {
	 *     //库源URL
	 *     //以"/"开头表示应用内路径
	 *     //以"http://"、"https://"开头表示绝对路径
	 *     //否则，表示插件资源路径
	 *     url: "lib0/b.css",
	 *     //可选，库源类型，自动识别JS、CSS
	 *     type: "css",
	 *     //可选，在此依赖库范围内加载顺序，越小越先加载，相同顺序的并行加载，默认为：0、或者所在数组的下标
	 *     order: 数值
	 *   }、
	 *   //库源URL/对象数组
	 *   [ "...", { ... }, ... ],
	 *   //可选，依赖库名称/数组
	 *   depends: "..."、[ "..."、... ],
	 *   //可选，检查当前环境是否已经加载了这个名称的库，返回值：true 是；其他 否。
	 *   //默认值是：如果this.name已在window下存在，返回true；否则，返回false。
	 *   loaded: function(){ ... },
	 *   //可选，校验已加载此名称库的版本是否兼容此图表渲染器，返回false或者抛出异常表明不兼容
	 *   //默认值是：function(version, lib){ return true; }
	 *   check: function(version, lib){ ... }
	 * }
	 * 
	 * @param chart
	 * @param callback 加载完成回调函数
	 * @param contextCharts 可变参数，上下文图表数组，对于相同名称的库，将在chart、contextCharts中加载最新版本那个
	 */
	chartFactory.loadDepends = function(chart, callback, contextCharts)
	{
		var plugin = chart.plugin;
		var libs = (plugin && plugin.renderer ? plugin.renderer.depends : null);
		
		if(libs == null || libs.length == 0)
		{
			callback();
		}
		
		if(chartFactory.pluginDependsLoaded(plugin))
		{
			callback();
		}
		
		var callback1 = function()
		{
			chartFactory.pluginDependsLoaded(plugin, true);
			callback();
		};
		
		if(!$.isArray(libs))
			libs = [ libs ];
		
		var unloads = [];
		
		for(var i=0; i<libs.length; i++)
		{
			if(!chartFactory.pluginLibLoaded(libs[i]))
			{
				unloads.push(libs[i]);
			}
		}
		
		if(unloads.length == 0)
		{
			callback1();
		}
		else
		{
			for(var i=2; i<arguments.length; i++)
			{
				contextCharts = arguments[i];
				
				if(!$.isArray(contextCharts))
					contextCharts = [ contextCharts ];
				
				chartFactory.inflateLatestLib(unloads, contextCharts);
			}
			
			//TODO
		}
	};
	
	//将libs数组中的库替换为最新版本
	chartFactory.inflateLatestLib = function(libs, charts)
	{
		for(var i=0; i<libs; i++)
		{
			for(var j=0; j<charts.length; j++)
			{
				var plugin = charts[i].plugin;
				var myLib = (plugin && plugin.renderer ? plugin.renderer.depends : null);
				
				if(myLib != null)
				{
					if(!$.isArray(myLib))
					{
						var name = chartFactory.resolveSameLibName(libs[i], myLib);
						
						if(name != null && chartFactory.compareVersion(name, libs[i].version, myLib.version) < 0)
						{
							libs[i] = myLib;
						}
					}
					else
					{
						for(var k=0; k<myLib.length; k++)
						{
							var name = chartFactory.resolveSameLibName(libs[i], myLib[k]);
						
							if(name != null && chartFactory.compareVersion(name, libs[i].version, myLib[k].version) < 0)
							{
								libs[i] = myLib[k];
							}
						}
					}
				}
			}
		}
	};
	
	//解析插件依赖库的name交集第一个，返回null表示无交集
	chartFactory.resolveSameLibName = function(baseLib, compareLib)
	{
		if(baseLib.name == null || baseLib.name.length == 0
			|| compareLib.name == null || compareLib.name.length == 0)
		{
			return null;
		}
		
		if(baseLib.name === compareLib.name)
		{
			if(chartFactory.isString(baseLib.name))
				return baseLib.name;
			else
				return baseLib.name[0];
		}
		
		var baseNameArray = (!chartFactory.isString(baseLib.name));
		var compareNameArray = (!chartFactory.isString(compareLib.name));
		
		if(!baseNameArray && !compareNameArray)
		{
			return null;
		}
		else if(!baseNameArray)
		{
			var idx = chartFactory.indexInArray(compareLib.name, baseLib.name);
			return (idx > -1 ? baseLib.name : null);
		}
		else if(!compareNameArray)
		{
			var idx = chartFactory.indexInArray(baseLib.name, compareLib.name);
			return (idx > -1 ? compareLib.name : null);
		}
		else
		{
			for(var i=0; i<baseLib.name; i++)
			{
				var idx = chartFactory.indexInArray(compareLib.name, baseLib.name[i]);
				if(idx > -1)
				{
					return baseLib.name[i];
				}
			}
			
			return null;
		}
	};
	
	//获取/设置插件所有依赖库是否都已加载
	chartFactory.pluginDependsLoaded = function(plugin, loaded)
	{
		var loadeds = chartFactory._PLUGIN_DEPENDS_LOADEDS;
		
		if(loaded === undefined)
		{
			return (loadeds[plugin.id] == true);
		}
		else
		{
			loadeds[plugin.id] = loaded;
		}
	};
	
	//获取/设置插件指定依赖库是否已加载
	chartFactory.pluginLibLoaded = function(lib, loaded)
	{
		var loadeds = chartFactory._PLUGIN_LIB_LOADEDS;
		
		if(loaded === undefined)
		{
			if(chartFactory.isString(lib.name))
			{
				return loadeds[lib.name];
			}
			else
			{
				for(var i=0; i<lib.name.length; i++)
				{
					if(loadeds[lib.name[i]])
					{
						return loadeds[lib.name[i]];
					}
				}
			}
			
			return false;
		}
		else
		{
			if(chartFactory.isString(lib.name))
			{
				loadeds[lib.name] = loaded;
			}
			else
			{
				for(var i=0; i<lib.name.length; i++)
				{
					loadeds[lib.name[i]] = loaded;
				}
			}
		}
	};
	
	chartFactory._PLUGIN_DEPENDS_LOADEDS = {};
	chartFactory._PLUGIN_LIB_LOADEDS = {};
	
	/**
	 * 在数组中查找元素，返回其索引
	 * 
	 * @param array
	 * @param value
	 * @returns 索引数值，-1 表示没有找到
	 */
	chartFactory.indexInArray = function(array, value)
	{
		if(array == null)
			return -1;
		
		for(var i=0; i<array.length; i++)
		{
			if(array[i] == value)
			{
				return i;
			}
		}
		
		return -1;
	};
	
	/**
	 * 比较版本号。
	 * 
	 * @param name
	 * @param va
	 * @param vb
	 * @returns -1 va低于vb；0 va等于vb；1 va高于vb
	 */
	chartFactory.compareVersion = function(name, va, vb)
	{
		//TODO
	};
})
(this);