/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 表元信息工具函数库。
 * 
 * 依赖:
 * jquery.js
 */
(function($, undefined)
{
	var $meta = ($.meta || ($.meta = {}));

	if(!$meta.schemaTableCache)
		$meta.schemaTableCache = {};
	
	//java.sql.Types
	$meta.typeEnum=
	{
		TINYINT: -6, SMALLINT: 5, INTEGER: 4, BIGINT: -5, REAL: 7, FLOAT: 6,
		DOUBLE: 8, DECIMAL: 3, NUMERIC: 2, BIT: -7, BOOLEAN: 16, CHAR: 1,
		VARCHAR: 12, LONGVARCHAR: -1, BINARY: -2, VARBINARY: -3, LONGVARBINARY: -4,
		DATE: 91, TIME: 92, TIME_WITH_TIMEZONE: 2013, TIMESTAMP: 93, TIMESTAMP_WITH_TIMEZONE: 2014,
		CLOB: 2005, BLOB: 2004, NCHAR: -15, NVARCHAR: -9, LONGNVARCHAR: -16, NCLOB: 2011, SQLXML: 2009
	};
	
	$.extend($meta,
	{
		/**
		 * 获取指定列。
		 * 
		 * @param table
		 * @param index 列索引、列名称
		 */
		column : function(table, index)
		{
			if(typeof(index) == "string")
				index = this.columnIndex(table, index);

			if(index < 0)
				throw new Error("No column for ["+index+"]");
			
			return table.columns[index];
		},
		
		/**
		 * 获取列索引。
		 */
		columnIndex : function(table, columnName)
		{
			var columns=table.columns;
			for(var i=0; i<columns.length; i++)
			{
				if(columns[i].name == columnName)
					return i;
			}
			
			return -1;
		},
		
		/**
		 * 获取/设置列值。
		 * 
		 * @param obj 必选，对象
		 * @param column 必选，列对象或者列名
		 * @parma value 可选，列值
		 */
		columnValue : function(obj, column, value)
		{
			if(obj == undefined || obj == null)
				throw new Error("[obj] must be defined");
			
			column = (column.name || column);
			
			var isGet = (arguments.length == 2);
			
			if(isGet)
				return obj[column];
			else
				obj[column] = value;
		},

		/**
		 * 如果列导入外键，则返回ImportKey对象，否则返回false。
		 */
		columnImportKey: function(table, column)
		{
			if(!table.importKeys)
				return false;
			
			var name = (column.name || column);
			
			for(var i=0; i<table.importKeys.length; i++)
			{
				var importKey = table.importKeys[i];
				if($.inArray(name, importKey.primaryColumnNames))
					return importKey;
			}
			
			return false;
		},
		
		/**
		 * 判断给定属性是否是ID属性。
		 */
		isIdProperty : function(model, property)
		{
			var idProperties = (model.idProperties ? model.idProperties : undefined);
			
			if(!idProperties)
				return false;
			
			for(var i=0; i< idProperties.length; i++)
			{
				if(idProperties[i] == property)
					return true;
			}
			
			return false;
		},
		
		/**
		 * 获取/设置对象ID属性值。
		 * 
		 * @param model 必选，表
		 * @param obj 必选，对象或者数组
		 * @parma id 可选
		 */
		id : function(model, obj, id)
		{
			var idProperties = model.idProperties;
			
			if(!idProperties)
				throw new Error("Table ["+model.name+"] is not entity Table");
			
			var isArray = (obj.length != undefined);
			
			if(!obj.length)
				obj = [obj];
			
			if(id == undefined)
			{
				var re = [];
				
				for(var i=0; i<idProperties.length; i++)
				{
					var propName = idProperties[i].name;
					
					for(var j=0; j<obj.length; j++)
					{
						if(!re[j])
							re[j] = {};
						
						re[j][propName] = this.propertyValue(obj[j], propName);
					}
				}
				
				return (isArray ? re : re[0]);
			}
			else
			{
				if(!id.length)
					id = [id];
				
				for(var i=0; i<idProperties.length; i++)
				{
					var propName = idProperties[i].name;
					
					for(var j=0; j<obj.length; j++)
						this.propertyValue(obj[j], propName, id[j][propName]);
				}
				
				return id;
			}
		},

		/**
		 * 创建指定表的实例对象。
		 * 
		 * @param table 表
		 * @param data 可选，待填充的实例对象
		 */
		instance : function(table, data)
		{
			data = (data || {});
			
			for(var i=0; i<table.columns.length; i++)
			{
				var column=table.columns[i];
				
				if(data[column.name] != undefined)
					continue;
				
				//如果没有默认值，明确赋值为null，避免某些页面逻辑错误（比如DataTable的cell().data()会取值为""空字符串）
				data[column.name] = (column.defaultValue != undefined ? column.defaultValue : null);
			}
			
			return data;
		},
		
		isBinaryColumn: function(column)
		{
			var type = column.type;
			
			return (type == this.typeEnum.BINARY
						|| type == this.typeEnum.VARBINARY || this.isBlobColumn(column));
		},
		
		isBlobColumn: function(column)
		{
			var type = column.type;
			
			return (type == this.typeEnum.LONGVARBINARY
						|| type == this.typeEnum.BLOB);
		},
		
		isTextColumn: function(column)
		{
			var type = column.type;
			
			return (type == this.typeEnum.CHAR || type == this.typeEnum.VARCHAR
					 || type == this.typeEnum.LONGVARCHAR || type == this.typeEnum.CLOB
					 || type == this.typeEnum.NCHAR || type == this.typeEnum.NVARCHAR
					 || type == this.typeEnum.LONGNVARCHAR|| type == this.typeEnum.NCLOB
					 || type == this.typeEnum.SQLXML);
		},
		
		isClobColumn: function(column)
		{
			var type = column.type;
			
			return (type == this.typeEnum.LONGVARCHAR
						|| type == this.typeEnum.CLOB
						|| type == this.typeEnum.LONGNVARCHAR
						|| type == this.typeEnum.SQLXML);
		},
		
		isDateColumn: function(column)
		{
			return (column.type == this.typeEnum.DATE);
		},

		isTimeColumn: function(column)
		{
			var type = column.type;
			return (type == this.typeEnum.TIME || type == this.typeEnum.TIME_WITH_TIMEZONE);
		},

		isTimestampColumn: function(column)
		{
			var type = column.type;
			return (type == this.typeEnum.TIMESTAMP || type == this.typeEnum.TIMESTAMP_WITH_TIMEZONE);
		},
		
		isBooleanColumn: function(column)
		{
			var type = column.type;
			return (type == this.typeEnum.BIT || type == this.typeEnum.BOOLEAN);
		},
		
		/**
		 * 指定列是否是必填项。
		 */
		isRequiredColumn: function(column)
		{
			return (!column.nullable && !column.autoincrement);
		},
		
		/**
		 * 将属性名按照HTML规范转义。
		 */
		escapeHtml : function(text)
		{
			if(typeof(text) != "string")
				return text;
			
			var epn = "";
			
			for(var i=0; i<text.length; i++)
			{
				var c = text.charAt(i);
				
				if(c == '<')
					epn += '&lt;';
				else if(c == '>')
					epn += '&gt;';
				else if(c == '&')
					epn += '&amp;';
				else if(c == '"')
					epn += '&quot;';
				else if(c == '\'')
					epn += '&#39;';
				else
					epn += c;
			}
			
			return epn;
		},

		/**
		 * 获取展示HTML。
		 * 
		 * @param tableOrColumn
		 * @param tagName 可选，HTML标签名
		 */
		displayInfoHtml : function(tableOrColumn, tagName)
		{
			if(!tagName)
				tagName = "span";
			
			var displayName = tableOrColumn.name;
			var displayDesc = (tableOrColumn.comment || "");
			
			var label = "<"+tagName+" class='display-info" + "' title='"+this.escapeHtml(displayDesc)+"'>"+this.escapeHtml(displayName)+"</"+tagName+">";
			
			return label;
		},
		
		/**
		 * 是否是可展示值对象。
		 */
		isShowableValue : function(value)
		{
			return $.isPlainObject(value) && value.hasOwnProperty("value") && value.hasOwnProperty("labelValue");
		},
		
		/**
		 * 构建可展示值对象。
		 */
		toShowableValue : function(value, labelValue)
		{
			return { "value" : value, "labelValue" : labelValue };
		},
		
		/**
		 * 获取可展示值对象的原始值。
		 */
		getShowableRawValue : function(value)
		{
			if(this.isShowableValue(value))
				return value.value;
			else
				return value;
		},
		
		/**
		 * 获取可展示值对象的标签值。
		 */
		getShowableLabelValue : function(value)
		{
			if(this.isShowableValue(value))
				return value.labelValue;
			else
				return undefined;
		}
	});
	
	$.extend($meta,
	{
		/**
		 * 加载表的URL。
		 * 
		 * @param schemaId
		 * @param tableName
		 * @param reload 是否让后台重新载入
		 */
		loadTableUrl : function(schemaId, tableName, reload)
		{
			var url = "";
			
			if(typeof(contextPath) != "undefined")
				url += contextPath;
			
			url = url + "/schema/" + encodeURIComponent(schemaId) +"/table/" + encodeURIComponent(tableName);
			
			if(reload)
				url = url +"?reload=1";
			
			return url;
		},
		
		/**
		 * 在指定表上执行callback操作。
		 * 
		 * @param schemaId
		 * @param tableName
		 * @param callback
		 */
		on : function(schemaId, tableName, callback)
		{
			this._on(schemaId, tableName, callback, false);
		},
		
		/**
		 * 获取指定名称的表对象。
		 * 
		 * @param schemaId
		 * @param tableName
		 */
		get : function(schemaId, tableName)
		{
			return this._getCachedTable(schemaId, tableName);
		},
		
		/**
		 * 载入指定名称的表对象。
		 * 
		 * @param schemaId
		 * @param tableName
		 * @param callback
		 */
		load : function(schemaId, tableName, callback)
		{
			this._on(schemaId, tableName, callback, true);
		},
		
		/**
		 * 在指定表上执行callback。
		 * 
		 * @param schemaId
		 * @param tableName 表名
		 * @param callback || options callback：载入回调函数，格式为：function(table){ ... }，options：ajax请求options
		 * @param reload 是否让后台重新载入
		 */
		_on : function(schemaId, tableName, callback, reload)
		{
			var table = this._getCachedTable(schemaId, tableName);
			
			if(table == null || reload)
			{
				var loadUrl = this.loadTableUrl(schemaId, tableName, reload);
				
				var _this = this;
				
				if($.isFunction(callback))
				{
					$.getJSON(loadUrl, function(table)
					{
						_this._setCachedTable(schemaId, table);
						
						if(callback != undefined)
							callback(table);
					});
				}
				else if($.isPlainObject(callback))
				{
					var options = callback;
					
					if(!options.url)
						options.url = loadUrl;
					
					if(!options.dataType)
						options.dataType = "json";
					
					var originalSuccessCallback = options.success;
					options.success = function(table, textStatus, jqXHR)
					{
						_this._setCachedTable(schemaId, table);
						
						if(originalSuccessCallback)
							originalSuccessCallback.call(this, table, textStatus, jqXHR);
					};
					
					$.ajax(options);
				}
				else
					throw new Error("Unknown function parameter type");
			}
			else
			{
				if($.isFunction(callback))
					callback(table);
				else if($.isPlainObject(callback))
					callback.success(table);
				else
					throw new Error("Unknown function parameter type");
			}
		},
		
		_getCachedTable : function(schemaId, tableName)
		{
			var tables = (this.schemaTableCache[schemaId] || (this.schemaTableCache[schemaId] = {}));
			return tables[tableName];
		},
		
		_setCachedTable : function(schemaId, table)
		{
			var tables = (this.schemaTableCache[schemaId] || (this.schemaTableCache[schemaId] = {}));
			tables[table.name] = table;
		},
	});
})
(jQuery);
