/*
 * Copyright (c) 2018 by datagear.org.
 */

/**
 * 数据库JDBC连接URL构建工具。
 * 
 * 依赖:
 * jquery.js
 */

(function($, undefined)
{
	var schemaUrlBuilder = ($.schemaUrlBuilder || ($.schemaUrlBuilder={}));
	var builders = (schemaUrlBuilder.builders || (schemaUrlBuilder.builders={}));
	
	schemaUrlBuilder.TEMPLATE_HOST="{host}";
	schemaUrlBuilder.TEMPLATE_PORT="{port}";
	schemaUrlBuilder.TEMPLATE_NAME="{name}";
	
	/**
	 * 列出所有构建器信息。
	 */
	schemaUrlBuilder.list = function()
	{
		var infoArray = [];
		
		var builders = $.schemaUrlBuilder.builders;
		
		for(var dbType in builders)
		{
			var builder = builders[dbType];
			
			infoArray.push({ "dbType" : dbType, "dbName" : (builder.dbName || dbType), "order" : builder.order });
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
	schemaUrlBuilder.build = function(dbType, value)
	{
		var builder = $.schemaUrlBuilder.builders[dbType];
		
		if(!builder)
			return "";
		
		if(builder.build) 
			return builder.build(value);
		else if(builder.template)
			return $.schemaUrlBuilder._resolveUrl(builder.template, value);
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
	schemaUrlBuilder.extract = function(url)
	{
		var builders = $.schemaUrlBuilder.builders;
		
		for(var dbType in builders)
		{
			var builder = builders[dbType];
			
			var value = null;
			
			if(builder.extract) 
				value = builder.extract(url);
			else if(builder.template)
				value = $.schemaUrlBuilder._resolveValue(builder.template, url);
			
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
	schemaUrlBuilder.defaultValue = function(dbType)
	{
		var builder = $.schemaUrlBuilder.builders[dbType];
		
		if(!builder)
			return {};
		
		return builder.defaultValue;
	};
	
	/**
	 * 是否包含指定数据库类型标识的构建器。
	 * 
	 * @param dbType 数据库类型标识
	 */
	schemaUrlBuilder.contains = function(dbType)
	{
		return ($.schemaUrlBuilder.builders[dbType] != undefined);
	};
	
	/**
	 * 添加一个构建器。
	 * 
	 * @param dbType 数据库类型标识
	 * @param builder 构建器，可以有两种格式：
	 * 1.
	 * {
	 *   //必选，模板
	 *   template : "...{host}...{port}...{name}...",
	 *   
	 *   //可选，默认值
	 *   defaultValue : { host : "...", port : "...", name : "" },
	 *   
	 *   //可选，展示排序
	 *   order : 9
	 * }
	 * 
	 * 2.
	 * {
	 *   //必选，由{ host : "...", port : "...", name : "" }值对象构建URL的函数
	 *   build : function(value){ ... },
	 *   
	 *   //必选，由URL构建{ host : "...", port : "...", name : "" }值对象的函数
	 *   extract : function(url){ ... },
	 *   
	 *   //可选，默认值
	 *   defaultValue : { host : "...", port : "...", name : "" },
	 *   
	 *   //可选，展示排序
	 *   order : 9
	 * }
	 */
	schemaUrlBuilder.add = function(dbType, builder)
	{
		$.schemaUrlBuilder.builders[dbType] = builder;
	};
	
	/**
	 * 删除所有自定义构建器。
	 */
	schemaUrlBuilder.removeCustom = function()
	{
		var builders = $.schemaUrlBuilder.builders;
		
		var removed = [];
		
		for(var dbType in builders)
		{
			var remove = true;
			
			var builtInDbTypes = $.schemaUrlBuilder.builtInDbTypes;
			if(builtInDbTypes)
			{
				for(var i = 0; i<builtInDbTypes.length; i++)
				{
					if(dbType == builtInDbTypes[i])
					{
						remove = false;
						break;
					}
				}
			}
			
			if(remove)
				removed.push(dbType);
		}
		
		for(var i=0; i< removed.length; i++)
			delete builders[removed[i]];
	};
	
	/**
	 * 由数据库URL模板解析URL。
	 * 
	 * @param template 数据库URL模板
	 * @param value 要替换的值
	 */
	schemaUrlBuilder._resolveUrl = function(template, value)
	{
		return template.replace($.schemaUrlBuilder.TEMPLATE_HOST, value.host)
			.replace($.schemaUrlBuilder.TEMPLATE_PORT, value.port)
			.replace($.schemaUrlBuilder.TEMPLATE_NAME, value.name);
	};
	
	/**
	 * 由数据库URL解析URL值对象。
	 * 
	 * @param template 数据库URL模板
	 * @param url 数据库URL
	 */
	schemaUrlBuilder._resolveValue = function(template, url)
	{
		if(!url)
			return null;
		
		var varInfo = null;
		
		var varInfos = ($.schemaUrlBuilder.templateVarInfos || ($.schemaUrlBuilder.templateVarInfos = {}));
		varInfo = varInfos[template];
		
		if(!varInfo)
		{
			varInfo = $.schemaUrlBuilder._resolveVarInfo(template);
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
	schemaUrlBuilder._resolveVarInfo = function(template)
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
	
	/**
	 * 将已添加的构建起记录为内置构建器。
	 */
	schemaUrlBuilder._asBuiltInDbTypes = function()
	{
		var builtInDbTypes = [];
		
		var builders = $.schemaUrlBuilder.builders;
		
		for(var dbType in builders)
			builtInDbTypes.push(dbType);
		
		$.schemaUrlBuilder.builtInDbTypes = builtInDbTypes;
	};
	
	schemaUrlBuilder.add("MySQL",
	{
		template : "jdbc:mysql://{host}:{port}/{name}",
		defaultValue :
		{
			host : "",
			port : 3306,
			name : ""
		},
		order : 0
	});
	
	schemaUrlBuilder.add("PostgreSQL",
	{
		template : "jdbc:postgresql://{host}:{port}/{name}",
		defaultValue :
		{
			host : "",
			port : 5432,
			name : ""
		},
		order : 1
	});
	
	schemaUrlBuilder.add("Oracle",
	{
		template : "jdbc:oracle:thin:@{host}:{port}/{name}",
		defaultValue :
		{
			host : "",
			port : 1521,
			name : ""
		},
		order : 2
	});
	
	schemaUrlBuilder.add("SQL Server",
	{
		template : "jdbc:sqlserver://{host}:{port};DatabaseName={name}",
		defaultValue :
		{
			host : "",
			port : 1433,
			name : ""
		},
		order : 3
	});

	schemaUrlBuilder.add("DB2",
	{
		template : "jdbc:db2://{host}:{port}/{name}",
		defaultValue :
		{
			host : "",
			port : 50000,
			name : ""
		},
		order : 4
	});
	
	schemaUrlBuilder.add("Sybase",
	{
		template : "jdbc:sybase:Tds:{host}:{port}/{name}",
		defaultValue :
		{
			host : "",
			port : 5000,
			name : ""
		},
		order : 5
	});
	
	schemaUrlBuilder._asBuiltInDbTypes();
})
(jQuery);

/**
 * $.schemaUrlBuilder.add的简写函数，便于自定义调用。
 * 
 * @param dbType
 * @param builder
 * @returns
 */
function addDbURLBuilder(dbType, builder)
{
	$.schemaUrlBuilder.add(dbType, builder)
}