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
 * 数据库JDBC连接URL构建工具。
 * 
 * 依赖:
 * jquery.js
 */

(function($, undefined)
{
	var dtbsSourceUrlBuilder = ($.dtbsSourceUrlBuilder || ($.dtbsSourceUrlBuilder={}));
	var builders = (dtbsSourceUrlBuilder.builders || (dtbsSourceUrlBuilder.builders={}));
	
	dtbsSourceUrlBuilder.TEMPLATE_HOST="{host}";
	dtbsSourceUrlBuilder.TEMPLATE_PORT="{port}";
	dtbsSourceUrlBuilder.TEMPLATE_NAME="{name}";
	
	/**
	 * 列出所有构建器信息。
	 */
	dtbsSourceUrlBuilder.list = function()
	{
		var infoArray = [];
		
		var builders = $.dtbsSourceUrlBuilder.builders;
		
		for(var dbType in builders)
		{
			var builder = builders[dbType];
			
			infoArray.push({ "dbType" : dbType, "dbDesc" : (builder.dbDesc || dbType), "order" : builder.order });
		}
		
		infoArray.sort(function(a, b)
		{
			if(a.order != undefined && b.order != undefined)
				return a.order - b.order;
			else if(a.order != undefined)
				return -1;
			else
				return 1;
		});
		
		return infoArray;
	};
	
	/**
	 * 构建JDBC连接URL。
	 * 
	 * @param dbType 数据库类型标识
	 * @param value URL值对象
	 */
	dtbsSourceUrlBuilder.build = function(dbType, value)
	{
		var builder = $.dtbsSourceUrlBuilder.builders[dbType];
		
		if(!builder)
			return "";
		
		if(builder.build) 
			return builder.build(value);
		else if(builder.template)
			return $.dtbsSourceUrlBuilder._resolveUrl(builder.template, value);
		else
			return "";
	};
	
	/**
	 * 由JDBC连接URL解析连接信息。
	 * 返回对象格式：{ dbType : "", value : { host : "", port : "", name : "" } }。
	 * 如果无法解析，返回null。
	 * 
	 * @param url JDBC连接URL
	 */
	dtbsSourceUrlBuilder.extract = function(url)
	{
		var builders = $.dtbsSourceUrlBuilder.builders;
		
		for(var dbType in builders)
		{
			var builder = builders[dbType];
			
			var value = null;
			
			if(builder.extract) 
				value = builder.extract(url);
			else if(builder.template)
				value = $.dtbsSourceUrlBuilder._resolveValue(builder.template, url);
			
			if(value != null)
				return { dbType : dbType, value : value };
		}
		
		return null;
	};
	
	/**
	 * 获取数据库的默认URL值对象。
	 * 
	 * @param dbType 数据库类型标识
	 */
	dtbsSourceUrlBuilder.defaultValue = function(dbType)
	{
		var builder = $.dtbsSourceUrlBuilder.builders[dbType];
		
		if(!builder)
			return {};
		
		return builder.defaultValue;
	};
	
	/**
	 * 是否包含指定数据库类型标识的构建器。
	 * 
	 * @param dbType 数据库类型标识
	 */
	dtbsSourceUrlBuilder.contains = function(dbType)
	{
		return ($.dtbsSourceUrlBuilder.builders[dbType] != undefined);
	};
	
	/**
	 * 添加一个构建器。
	 * 
	 * @param builder 构建器，可以有两种格式：
	 * 1.
	 * {
	 *   //必选，数据库类型
	 *   dbType : "...",
	 *   
	 *   //必选，模板
	 *   template : "...{host}...{port}...{name}...",
	 *   
	 *   //可选，默认值
	 *   defaultValue : { host : "...", port : "...", name : "" },
	 *   
	 *   //可选，数据库描述
	 *   dbDesc : "...",
	 *   
	 *   //可选，展示排序
	 *   order : 9
	 * }
	 * 
	 * 2.
	 * {
	 *   //必选，数据库类型
	 *   dbType : "...",
	 *   
	 *   //必选，由{ host : "...", port : "...", name : "" }值对象构建URL的函数
	 *   build : function(value){ ... },
	 *   
	 *   //必选，由URL构建{ host : "...", port : "...", name : "" }值对象的函数
	 *   extract : function(url){ ... },
	 *   
	 *   //可选，默认值
	 *   defaultValue : { host : "...", port : "...", name : "" },
	 *   
	 *   //可选，数据库描述
	 *   dbDesc : "...",
	 *   
	 *   //可选，展示排序
	 *   order : 9
	 * }
	 */
	dtbsSourceUrlBuilder.add = function(builder)
	{
		var re = [];
		
		var order = 0;
		
		for(var i= 0; i<arguments.length; i++)
		{
			var ele = arguments[i];
			
			if(!$.isArray(ele))
				ele = [ ele ];
			
			for(var j=0; j<ele.length; j++)
			{
				var myBuilder = ele[j];
				
				if(myBuilder && myBuilder.dbType)
				{
					if(myBuilder.order == undefined)
						myBuilder.order = order;
					
					$.dtbsSourceUrlBuilder.builders[myBuilder.dbType] = myBuilder;
					
					re.push(myBuilder);
					order++;
				}
			}
		}
		
		return re;
	};
	
	/**
	 * 删除所有构建器。
	 */
	dtbsSourceUrlBuilder.clear = function()
	{
		var builders = $.dtbsSourceUrlBuilder.builders;
		
		var removed = [];
		
		for(var dbType in builders)
			removed.push(dbType);
		
		for(var i=0; i< removed.length; i++)
			delete builders[removed[i]];
	};
	
	dtbsSourceUrlBuilder.sortByDbType = function(builders)
	{
		if(!builders || builders.length == 0)
			return;
			
		builders.sort(function(ba, bb)
		{
			var baType = (ba ? ba.dbType : "");
			var bbType = (bb ? bb.dbType : "");
			
			if(baType < bbType)
				return -1;
			else if(baType > bbType)
				return 1;
			else
				return 0;
		});
	};
	
	/**
	 * 由数据库URL模板解析URL。
	 * 
	 * @param template 数据库URL模板
	 * @param value 要替换的值
	 */
	dtbsSourceUrlBuilder._resolveUrl = function(template, value)
	{
		return template.replace($.dtbsSourceUrlBuilder.TEMPLATE_HOST, value.host)
			.replace($.dtbsSourceUrlBuilder.TEMPLATE_PORT, value.port)
			.replace($.dtbsSourceUrlBuilder.TEMPLATE_NAME, value.name);
	};
	
	/**
	 * 由数据库URL解析URL值对象。
	 * 
	 * @param template 数据库URL模板
	 * @param url 数据库URL
	 */
	dtbsSourceUrlBuilder._resolveValue = function(template, url)
	{
		if(!url)
			return null;
		
		var varInfo = null;
		
		var varInfos = ($.dtbsSourceUrlBuilder.templateVarInfos || ($.dtbsSourceUrlBuilder.templateVarInfos = {}));
		varInfo = varInfos[template];
		
		if(!varInfo)
		{
			varInfo = $.dtbsSourceUrlBuilder._resolveVarInfo(template);
			varInfos[template] = varInfo;
		}
		
		var value = null;
		
		url = $.trim(url);
		
		for(var i=0; i<varInfo.length; i++)
		{
			if(!url)
				break;
			
			var varEle = varInfo[i];
			
			var varName = varEle.name;
			var prefix = varEle.prefix;
			var suffix = (!varEle.suffix && i<varInfo.length-1 ? varInfo[i+1].prefix : varEle.suffix);
			
			var varValue = null;
			
			if(prefix)
			{
				if(url.indexOf(prefix) != 0)
					return null;
				
				url = url.substring(prefix.length);
			}
			
			if(suffix)
			{
				var endIndex = url.indexOf(suffix);
				
				if(endIndex < 0)
					return null;
				
				varValue = url.substring(0, endIndex);
				url = url.substring(endIndex);
			}
			else
			{
				varValue = url;
				url = null;
			}
			
			if(varValue != null)
			{
				if(!value)
					value = {};
				
				value[varName] = varValue;
			}
		}
		
		return value;
	};
	
	/**
	 * 解析模板字符串中的变量信息。
	 */
	dtbsSourceUrlBuilder._resolveVarInfo = function(template)
	{
		var varInfo = [];
		
		var prefix = "";
		
		var i=0;
		for(; i<template.length; i++)
		{
			var c = template.charAt(i);
			
			if(c == '{')
			{
				var varName = "";
				
				var j=i+1;
				for(; j<template.length; j++)
				{
					var cj = template.charAt(j);
					
					if(cj == '}')
						break;
					
					varName += cj;
				}
				
				//仅处理{host}, {port}, {name}变量
				if(varName == "host" || varName == "port" || varName =="name")
				{
					varInfo.push({ name : varName, prefix : (prefix == "" ? null : prefix) });
					prefix = "";
				}
				else
				{
					prefix += template.substring(i, j+1);
				}
				
				i=j;
			}
			else
			{
				prefix += c;
			}
		}
		
		//最后的普通字符串作为最后一个元素的suffix
		if(prefix != "" && varInfo.length > 0)
			varInfo[varInfo.length - 1].suffix = prefix;
		
		return varInfo;
	};
})
(jQuery);