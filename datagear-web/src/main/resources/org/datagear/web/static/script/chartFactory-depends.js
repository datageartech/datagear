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
			chartFactory.sortLibsByDepends(unloadeds);
			chartFactory.loadLibInner(unloadeds, callback);
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
			if(stateObj && stateObj.state == chartFactory.LIB_STATE_LOADED)
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
				if(stateObj && stateObj.state == chartFactory.LIB_STATE_LOADED)
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
	
	//根据依赖优先级排序库，被依赖库靠前
	chartFactory.sortLibsByDepends = function(libs)
	{
		//TODO
	};
	
	chartFactory.loadLibInner = function(libs, callback)
	{
		var deferreds = [];
		
		for(var i=0; i<libs.length; i++)
		{
			var stateObj = chartFactory.libState(libs[i], true);
			
			if(stateObj.state === chartFactory.LIB_STATE_INIT)
			{
				var source = stateObj.lib.source;
				var srcDfds = stateObj.sourceLoadedDeferreds;
				
				if(source != null)
				{
					if(!$.isArray(source))
						source = [ source ];
					
					for(var j=0; j<source.length; j++)
					{
						chartFactory.loadSingleLibSource(source[j], srcDfds[j]);
					}
				}
			}
			
			deferreds.push(stateObj.loadedDeferred);
		}
		
		$.when.apply($, deferreds).always(function(){ callback(); });
	};
	
	chartFactory.loadSingleLibSource = function(source, deferred)
	{
		if(deferred.state != "pending")
			return;
		
		if(chartFactory.isString(source))
		{
			source = { url: source, type: chartFactory.resolveLibSourceType(source) };
		}
		
		var ele;
		
		if(source.type == "js")
		{
			ele = $("<script src=\""+source.url+"\" type=\"text/javascript\"></script>");
		}
		else if(source.type == "css")
		{
			ele = $("<link href=\""+source.url+"\" type=\"text/css\" rel=\"stylesheet\" />");
		}
		else
		{
			chartFactory.logException("Unknown lib source type '"+source.type+"'");
		}
		
		var target = (document.head ? document.head : document.body);
		ele.on("load", function(){ deferred.resolve(); }).appendTo(target);
	};
	
	chartFactory.resolveLibSourceType = function(url)
	{
		var qsIdx = url.indexOf("?");
		if(qsIdx < 0)
			qsIdx = url.indexOf("#");
		
		if(qsIdx > 0)
			url = url.substring(0, qsIdx);
		
		var type = "";
		
		if(chartFactory.LIB_JS_SOURCE_REGEX.test(url))
		{
			type = "js";
		}
		else if(chartFactory.LIB_CSS_SOURCE_REGEX.test(url))
		{
			type = "css";
		}
		else
		{
			var didx = url.lastIndexOf(".");
			
			if(didx > -1 && didx < url.length - 1)
				type = url.substring(didx+1);
		}
		
		return type;
	};
	
	chartFactory.LIB_JS_SOURCE_REGEX = /\.(js)$/i;
	chartFactory.LIB_CSS_SOURCE_REGEX = /\.(css)$/i;
	
	//查找最新版的库
	chartFactory.findLatestLib = function(contextLibs, lib)
	{
		var latestLib = lib;
		
		for(var i=0; i<contextLibs.length; i++)
		{
			var contextLib = contextLibs[i];
			var name = chartFactory.resolveSameLibName(latestLib.name, contextLib.name);
			
			if(name != null && chartFactory.compareLibVersion(name, latestLib.version, contextLib.version) < 0)
			{
				latestLib = contextLib;
			}
		}
		
		return latestLib;
	};
	
	/**
	 * 比较版本号。
	 * 
	 * @param name
	 * @param va
	 * @param vb
	 * @returns -1 va低于vb；0 va等于vb；1 va高于vb
	 */
	chartFactory.compareLibVersion = function(name, va, vb)
	{
		//TODO
		return 0;
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
	 * 获取库状态信息。
	 * 
	 * @param lib 库对象
	 * @param nonNull 可选，是否返回非null。
	 */
	chartFactory.libState = function(lib, nonNull)
	{
		var states = chartFactory._LIB_STATES;
		
		if(nonNull !== true)
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
			var stateObj = chartFactory.libState(lib);
			
			if(stateObj == null)
			{
				stateObj = chartFactory.createLibState(lib);
				
				if(chartFactory.isString(lib.name))
				{
					states[lib.name] = stateObj;
				}
				else
				{
					for(var i=0; i<lib.name.length; i++)
					{
						states[lib.name[i]] = stateObj;
					}
				}
			}
			
			return stateObj;
		}
	};
	
	chartFactory.createLibState = function(lib, state)
	{
		state = (state == null ? chartFactory.LIB_STATE_INIT : state);
		
		//无论state是何状态，都应设置loadedDeferred、sourceLoadedDeferreds，
		//确保其在异步调用中结构完整
		var stateObj =
		{
			//同chartFactory.loadLib()函数的库对象结构
			lib: lib,
			//库状态，参考：chartFactory.LIB_STATE_*
			state: state,
			//库加载完成后的回调函数
			loadedDeferred: $.Deferred(),
			//库中source对应的加载完成后回调函数
			sourceLoadedDeferreds: []
		};
		
		stateObj.loadedDeferred.always(function()
		{
			stateObj.state = chartFactory.LIB_STATE_LOADED;
		});
		
		var source = stateObj.lib.source;
		var sourceLen = (source == null ? 0 : ($.isArray(source) ? source.length : 1));
		
		if(sourceLen == 0)
		{
			stateObj.state = chartFactory.LIB_STATE_LOADED;
			stateObj.loadedDeferred.resolve();
		}
		else
		{
			for(var i=0; i<sourceLen; i++)
			{
				stateObj.sourceLoadedDeferreds[i] = $.Deferred();
			}
			
			$.when.apply($, stateObj.sourceLoadedDeferreds).always(function(){ stateObj.loadedDeferred.resolve(); });
		}
		
		return stateObj;
	};
	
	//库及其状态，键值结构：库名 -> 库信息。
	chartFactory._LIB_STATES = {};
	
	//库状态：初始化
	chartFactory.LIB_STATE_INIT = "init";
	//库状态：加载中
	chartFactory.LIB_STATE_LOADING = "loading";
	//库状态：加载完成
	chartFactory.LIB_STATE_LOADED = "loaded";
})
(this);