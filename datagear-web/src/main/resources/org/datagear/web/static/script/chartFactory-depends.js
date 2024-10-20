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
	 * 加载库，并在加载后执行回调函数。
	 * 库对象结构为：
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
	 *     //库源URL，应是可直接加载的URL
	 *     url: "lib0/b.css",
	 *     //可选，库源类型，自动识别JS、CSS
	 *     type: "css"
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
	 * @param lib 库对象、数组
	 * @param callback 加载完成回调函数
	 * @param contextLibs 上下文库数组，对于相同名称的库，将在contextLibs中加载最新版本那个
	 */
	chartFactory.loadLib = function(lib, callback, contextLibs)
	{
		if(!lib)
		{
			callback();
		}
		
		if(!$.isArray(lib))
			lib = [ lib ];
		
		var unloadeds = [];
		chartFactory.inflateUnloadedLibs(contextLibs, lib, unloadeds);
		
		if(unloadeds.length == 0)
		{
			callback();
		}
		else
		{
			//TODO
		}
	};
	
	//填充所有待加载库，填充后，unloadeds中都是最新版本库，且都包含依赖库
	chartFactory.inflateUnloadedLibs = function(contextLibs, libs, unloadeds)
	{
		for(var i=0; i<libs.length; i++)
		{
			var lib = libs[i];
			
			if(chartFactory.isLibLoadedInEnv(lib))
			{
				continue;
			}
			
			var stateObj = chartFactory.libState(lib);
			if(stateObj && stateObj.loaded)
			{
				continue;
			}
			
			var latestLib = chartFactory.findLatestLib(contextLibs, lib);
			
			if(latestLib !== lib)
			{
				if(chartFactory.isLibLoadedInEnv(latestLib))
				{
					continue;
				}
				
				stateObj = chartFactory.libState(latestLib);
				if(stateObj && stateObj.loaded)
				{
					continue;
				}
			}
			
			if(chartFactory.libIndex(unloadeds, latestLib.name) > -1)
				continue;
			
			unloadeds.push(latestLib);
			
			//处理依赖
			if(latestLib.depends)
			{
				var depends = latestLib.depends;
				var dependLibs = [];
				
				if(!$.isArray(depends))
					depends = [ depends ];
				
				for(var j=0; j<depends.length; j++)
				{
					var dependName = depends[j];
					
					if(chartFactory.libIndex(unloadeds, dependName) > -1)
						continue;
					
					if(chartFactory.libIndex(libs, dependName) > -1)
						continue;
					
					var libIdx = chartFactory.libIndex(contextLibs, dependName);
					
					if(libIdx > -1)
					{
						dependLibs.push(contextLibs[libIdx]);
					}
					else
					{
						chartFactory.logException("No lib found with name '"+dependName+"'");
					}
				}
				
				if(dependLibs.length > 0)
				{
					chartFactory.inflateUnloadedLibs(contextLibs, dependLibs, unloadeds);
				}
			}
		}
	};
	
	//查找最新版的库
	chartFactory.findLatestLib = function(contextLibs, lib)
	{
		var latestLib = lib;
		
		for(var i=0; i<contextLibs.length; i++)
		{
			var contextLib = contextLibs[i];
			var name = chartFactory.resolveSameLibName(latestLib.name, contextLib.name);
			
			if(name != null && chartFactory.compareVersion(name, latestLib.version, contextLib.version) < 0)
			{
				latestLib = contextLib;
			}
		}
		
		return latestLib;
	};
	
	//查找第一个同名的库索引
	chartFactory.libIndex = function(libs, name)
	{
		for(var i=0; i<libs.length; i++)
		{
			if(chartFactory.resolveSameLibName(libs[i].name, name))
				return i;
		}
		
		return -1;
	};
	
	//当前环境是否已加载了指定库
	chartFactory.isLibLoadedInEnv = function(lib)
	{
		if(lib.loaded != null)
		{
			return lib.loaded();
		}
		else
		{
			if(chartFactory.isString(lib.name))
			{
				return (window[lib.name] != null);
			}
			else
			{
				for(var i=0; i<lib.name.length; i++)
				{
					if(window[lib.name[i]] != null)
					{
						return true;
					}
				}
			}
			
			return false;
		}
	};
	
	//解析库名称交集第一个，返回null表示无交集
	chartFactory.resolveSameLibName = function(baseLibName, compareLibName)
	{
		if(baseLibName == null || baseLibName.length == 0
			|| compareLibName == null || compareLibName.length == 0)
		{
			return null;
		}
		
		var baseNameArray = (!chartFactory.isString(baseLibName));
		
		if(baseLibName === compareLibName)
		{
			if(!baseNameArray)
				return baseLibName;
			else
				return baseLibName[0];
		}
		
		var compareNameArray = (!chartFactory.isString(compareLibName));
		
		if(!baseNameArray && !compareNameArray)
		{
			return null;
		}
		else if(!baseNameArray)
		{
			var idx = chartFactory.indexInArray(compareLibName, baseLibName);
			return (idx > -1 ? baseLibName : null);
		}
		else if(!compareNameArray)
		{
			var idx = chartFactory.indexInArray(baseLibName, compareLibName);
			return (idx > -1 ? compareLibName : null);
		}
		else
		{
			for(var i=0; i<baseLibName; i++)
			{
				var idx = chartFactory.indexInArray(compareLibName, baseLibName[i]);
				if(idx > -1)
				{
					return baseLibName[i];
				}
			}
			
			return null;
		}
	};
	
	/**
	 * 获取/设置库状态。
	 * 库状态结构如下：
	 * {
	 * 	 //同chartFactory.loadLib()函数的库对象结构
	 * 	 lib: 库对象,
	 *   //库是否已加载
	 *   loaded: true、false,
	 *   //库加载完成后的回调函数
	 * 	 loadedDeferred: $.Deferred
	 * }
	 * 
	 * @param lib 库对象
	 * @param loaded 可选，设置库状态。
	 */
	chartFactory.libState = function(lib, loaded)
	{
		var states = chartFactory._LIB_STATES;
		
		if(loaded === undefined)
		{
			if(chartFactory.isString(lib.name))
			{
				return states[lib.name];
			}
			else
			{
				for(var i=0; i<lib.name.length; i++)
				{
					if(states[lib.name[i]])
					{
						return states[lib.name[i]];
					}
				}
			}
			
			return null;
		}
		else
		{
			if(chartFactory.isString(lib.name))
			{
				var stateObj = states[lib.name];
				
				//对于已存在的，不允许将loaded状态改为false
				if(stateObj)
				{
					if(loaded === true)
						stateObj.loaded = loaded;
				}
				else
				{
					states[lib.name] = { lib: lib, loaded: loaded, loadedDeferred: $.Deferred() };
				}
			}
			else
			{
				var stateObj = { lib: lib, loaded: loaded, loadedDeferred: $.Deferred() };
				
				for(var i=0; i<lib.name.length; i++)
				{
					var name = lib.name[i];
					var stateObj = states[name];
				
					//对于已存在的，不允许将loaded状态改为false
					if(stateObj)
					{
						if(loaded === true)
							stateObj.loaded = loaded;
					}
					else
					{
						states[name] = stateObj;
					}
				}
			}
		}
	};
	
	//库及其状态，这些库是从chartFactory.loadLib()函数的contextLibs中选定的最新版库，键值结构：库名 -> 库信息。
	//其中，
	chartFactory._LIB_STATES = {};
	
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