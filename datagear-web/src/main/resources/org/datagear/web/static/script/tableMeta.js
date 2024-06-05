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
 * 表元信息工具函数库。
 * 
 * 依赖:
 * jquery.js
 * util.js
 */
(function($, undefined)
{
	var tableMeta = ($.tableMeta || ($.tableMeta = {}));
	tableMeta.dtbsSourceTableCache = (tableMeta.dtbsSourceTableCache || (tableMeta.dtbsSourceTableCache = {}));
	
	//PersistenceSupport.supportsSqlType支持的SQL类型
	tableMeta.Types=
	{
		TINYINT: -6, SMALLINT: 5, INTEGER: 4, BIGINT: -5, REAL: 7, FLOAT: 6,
		DOUBLE: 8, DECIMAL: 3, NUMERIC: 2, BIT: -7, BOOLEAN: 16, CHAR: 1,
		VARCHAR: 12, LONGVARCHAR: -1, BINARY: -2, VARBINARY: -3, LONGVARBINARY: -4,
		DATE: 91, TIME: 92, TIME_WITH_TIMEZONE: 2013, TIMESTAMP: 93, TIMESTAMP_WITH_TIMEZONE: 2014,
		CLOB: 2005, BLOB: 2004, NCHAR: -15, NVARCHAR: -9, LONGNVARCHAR: -16, NCLOB: 2011, SQLXML: 2009
	};
	
	$.extend(tableMeta,
	{
		/**
		 * 获取指定列/列数组。
		 * 
		 * @param table
		 * @param index 列索引、列名称、数组
		 */
		column : function(table, index)
		{
			var isArray = $.isArray(index);
			
			var re = [];
			var indexes = (isArray ? index : [index]);
			
			for(var i=0; i<indexes.length; i++)
			{
				index = indexes[i];
				
				if(typeof(index) == "string")
					index = this.columnIndex(table, index);
	
				if(index < 0)
					throw new Error("No column for ["+index+"]");
				
				re.push(table.columns[index]);
			}
			
			return (isArray ? re : re[0]);
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
				if($.inArray(name, importKey.columnNames) > -1)
					return importKey;
			}
			
			return false;
		},
		
		/**
		 * 获取导入键的本表对象。
		 * 
		 * @param importKey
		 * @param primaryObj 主表对象
		 */
		fromImportKeyPrimary: function(importKey, primaryObj)
		{
			var re = {};
			
			var primaryNames = importKey.primaryColumnNames;
			var myNames = importKey.columnNames;
			
			for(var i=0; i<primaryNames.length; i++)
			{
				var value = primaryObj[primaryNames[i]];
				
				//在某些情况（比如先将主表以大写命名语句创建加载至系统缓存，之后又以小写命名语句重新创建主表和外键表，而不刷新主表），
				//会出现primaryNames与primaryObj属性名大小写不一致的情况，所以这里如果没取到，再使用忽略大小写的方式重试一次
				if(value === undefined)
				{
					for(var p in primaryObj)
					{
						if(p.toLowerCase() == primaryNames[i].toLowerCase())
						{
							value = primaryObj[p];
							break;
						}
					}
				}
				
				if(value == undefined)
				{
					value = null;
				}
				
				re[myNames[i]] = value;
			}
			
			return re;
		},
		
		/**
		 * 获取导入键的主表对象。
		 * 
		 * @param importKey
		 * @param obj 本表对象
		 */
		toImportKeyPrimary: function(importKey, obj)
		{
			var re = {};
			
			var myNames = importKey.columnNames;
			var primaryNames = importKey.primaryColumnNames;
			
			for(var i=0; i<myNames.length; i++)
			{
				var value = obj[myNames[i]];
				if(value == undefined)
					value = null;
				
				re[primaryNames[i]] = value;
			}
			
			return re;
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
				data[column.name] = null;
				
				//不设置默认值了，因为默认值可能仅是数据库级的标识，比如Mysql的"CURRENT_TIMESTAMP"
				//data[column.name] = (column.defaultValue != undefined ? column.defaultValue : null);
			}
			
			return data;
		},
		
		/**
		 * 尽量获取能够唯一确定记录的数据对象。
		 */
		uniqueRecordData: function(table, row)
		{
			var columns;
			
			if(table.primaryKey)
				columns = this.column(table, table.primaryKey.columnNames);
			else if(table.uniqueKeys && table.uniqueKeys.length > 0)
				columns = this.column(table, table.uniqueKeys[0].columnNames);
			else
			{
				columns = [];
				var Types = this.Types;
				for(var i=0; i<table.columns.length; i++)
				{
					var column = table.columns[i];
					var type = column.type;
					//与DefaultPersistenceManager.getColumnsMaybeUniqueRecord(Table)保持一致
					if (Types.BIGINT == type || Types.BIT == type || Types.BOOLEAN == type || Types.CHAR == type
							|| Types.DATE == type || Types.DECIMAL == type || Types.DOUBLE == type || Types.FLOAT == type
							|| Types.BINARY == type || Types.VARBINARY == type || Types.INTEGER == type || Types.NULL == type
							|| Types.NUMERIC == type || Types.REAL == type || Types.SMALLINT == type || Types.TIME == type
							|| Types.TIME_WITH_TIMEZONE == type || Types.TIMESTAMP == type
							|| Types.TIMESTAMP_WITH_TIMEZONE == type || Types.TINYINT == type || Types.VARCHAR == type)
						columns.push(column);
				}
			}
			
			var re = [];
			
			var rows = ($.isArray(row) ? row : [row]);
			for(var i=0; i<rows.length; i++)
			{
				var data = {};
				var myRow = rows[i];
				for(var j=0; j<columns.length; j++)
				{
					var name = columns[j].name;
					data[name] = (myRow[name] == null ? null : myRow[name]);
				}
				
				re.push(data);
			}
			
			return ($.isArray(row) ? re : re[0]);
		},
		
		isBinaryColumnValueHex: function(value)
		{
			value = this.valueOfLabeledValue(value);
			return (value ? value.indexOf(this.binaryColumnValueHexPrefix) == 0 : false);
		},
		
		binaryColumnValueHexPrefix: "hex:",
		
		isBinaryColumnValueBase64: function(value)
		{
			value = this.valueOfLabeledValue(value);
			return (value ? value.indexOf(this.binaryColumnValueBase64Prefix) == 0 : false);
		},
		
		binaryColumnValueBase64Prefix: "base64:",
		
		isBinaryColumnValueFile: function(value)
		{
			value = this.valueOfLabeledValue(value);
			return (value ? value.indexOf(this.binaryColumnValueFilePrefix) == 0 : false);
		},
		
		binaryColumnValueFileContent: function(value)
		{
			if(!this.isBinaryColumnValueFile(value))
				return value;
			
			value = this.valueOfLabeledValue(value);
			return value.substr(this.binaryColumnValueFilePrefix.length);
		},
		
		binaryColumnValueFilePrefix: "file:",
		
		/**
		 * 是否支持指定列的持久化操作，参考PersistenceSupport.supportsSqlType()。
		 */
		supportsColumn: function(column)
		{
			var type = column.type;
			
			for(var p in this.Types)
			{
				if(this.Types[p] == type)
					return true;
			}
			
			return false;
		},
		
		isNumberColumn: function(column)
		{
			var Types = this.Types;
			var sqlType = column.type;
			
			switch (sqlType)
			{
				case Types.TINYINT:
				case Types.SMALLINT:
				case Types.INTEGER:
				case Types.BIGINT:
				case Types.REAL:
				case Types.FLOAT:
				case Types.DOUBLE:
				case Types.DECIMAL:
				case Types.NUMERIC:
					return true;
				default:
					return false;
			}
		},
		
		isBinaryColumn: function(column)
		{
			var type = column.type;
			
			return (type == this.Types.BINARY || type == this.Types.VARBINARY
						|| type == this.Types.LONGVARBINARY || type == this.Types.BLOB);
		},
		
		isTextColumn: function(column)
		{
			var type = column.type;
			
			return (type == this.Types.CHAR || type == this.Types.VARCHAR
					 || type == this.Types.LONGVARCHAR || type == this.Types.CLOB
					 || type == this.Types.NCHAR || type == this.Types.NVARCHAR
					 || type == this.Types.LONGNVARCHAR|| type == this.Types.NCLOB
					 || type == this.Types.SQLXML);
		},
		
		isClobColumn: function(column)
		{
			var type = column.type;
			
			return (type == this.Types.LONGVARCHAR
						|| type == this.Types.CLOB
						|| type == this.Types.LONGNVARCHAR
						|| type == this.Types.NCLOB);
		},
		
		isSqlxmlColumn: function(column)
		{
			var type = column.type;
			return (type == this.Types.SQLXML);
		},
		
		isDateColumn: function(column)
		{
			return (column.type == this.Types.DATE);
		},

		isTimeColumn: function(column)
		{
			var type = column.type;
			return (type == this.Types.TIME || type == this.Types.TIME_WITH_TIMEZONE);
		},

		isTimestampColumn: function(column)
		{
			var type = column.type;
			return (type == this.Types.TIMESTAMP || type == this.Types.TIMESTAMP_WITH_TIMEZONE);
		},
		
		isBooleanColumn: function(column)
		{
			var type = column.type;
			return (type == this.Types.BIT || type == this.Types.BOOLEAN);
		},
		
		/**
		 * 指定列是否是必填项。
		 */
		isRequiredColumn: function(column)
		{
			return (!column.nullable && !column.autoincrement);
		},
		
		/**
		 * 是否支持关键字查询的列。
		 */
		isKeywordSearchColumn: function(column)
		{
			return (column.searchableType == "ONLY_LIKE" || column.searchableType == "ALL"
						|| this.isNumberColumn(column));
		},
		
		/**
		 * 获取展示HTML。
		 * 
		 * @param tableOrColumn
		 * @param tagName 可选，HTML标签名
		 * @param className 可选，自定义样式类名
		 */
		displayInfoHtml : function(tableOrColumn, tagName, className)
		{
			tagName = (tagName || "span");
			return "<"+tagName+" class='display-info " + (className ? className : "") + "' title='"+$.escapeHtml(tableOrColumn.comment || "")+"'>"
						+$.escapeHtml(tableOrColumn.name)+"</"+tagName+">";
		},
		
		/**
		 * 是否是标签值对象：{value: ..., label: "..."}。
		 */
		isLabeledValue : function(value)
		{
			return $.isPlainObject(value) && value.hasOwnProperty("value") && value.hasOwnProperty("label");
		},
		
		/**
		 * 构建标签值对象。
		 */
		toLabeledValue : function(value, label)
		{
			return { "value" : value, "label" : label };
		},
		
		/**
		 * 获取标签值对象的值。
		 */
		valueOfLabeledValue : function(value)
		{
			return (this.isLabeledValue(value) ? value.value : value);
		},
		
		/**
		 * 获取标签值对象的标签。
		 */
		labelOfLabeledValue : function(value)
		{
			return (this.isLabeledValue(value) ? value.label : undefined);
		},
		
		/**
		 * 移除对象/数组的标签值对象特性。
		 */
		removeLabeledValueFeature : function(data)
		{
			if(!data)
				return;
			
			var datas = ($.isArray(data) ? data : [data]);
			
			for(var i=0; i<datas.length; i++)
			{
				var ele = datas[i];
				for(var p in ele)
				{
					var v = ele[p];
					var vv = this.valueOfLabeledValue(v);
					if(vv !== v)
						ele[p] = vv;
				}
			}
			
			return data;
		}
	});
	
	$.extend(tableMeta,
	{
		/**
		 * 加载表的URL。
		 * 
		 * @param dtbsSourceId
		 * @param tableName
		 * @param reload 是否让后台重新载入
		 */
		loadTableUrl : function(dtbsSourceId, tableName, reload)
		{
			var url = "";
			
			if(typeof(contextPath) != "undefined")
				url += contextPath;
			
			url = url + "/dtbsSource/" + encodeURIComponent(dtbsSourceId) +"/table/" + encodeURIComponent(tableName);
			
			if(reload)
				url = url +"?reload=1";
			
			return url;
		},
		
		/**
		 * 在指定表上执行callback操作。
		 * 
		 * @param dtbsSourceId
		 * @param tableName
		 * @param callback
		 */
		on : function(dtbsSourceId, tableName, callback)
		{
			this._on(dtbsSourceId, tableName, callback, false);
		},
		
		/**
		 * 获取指定名称的表对象。
		 * 
		 * @param dtbsSourceId
		 * @param tableName
		 */
		get : function(dtbsSourceId, tableName)
		{
			return this._getCachedTable(dtbsSourceId, tableName);
		},
		
		/**
		 * 载入指定名称的表对象。
		 * 
		 * @param dtbsSourceId
		 * @param tableName
		 * @param callback
		 */
		load : function(dtbsSourceId, tableName, callback)
		{
			this._on(dtbsSourceId, tableName, callback, true);
		},
		
		/**
		 * 在指定表上执行callback。
		 * 
		 * @param dtbsSourceId
		 * @param tableName 表名
		 * @param callback || options callback：载入回调函数，格式为：function(table){ ... }，options：ajax请求options
		 * @param reload 是否让后台重新载入
		 */
		_on : function(dtbsSourceId, tableName, callback, reload)
		{
			var table = this._getCachedTable(dtbsSourceId, tableName);
			
			if(table == null || reload)
			{
				var loadUrl = this.loadTableUrl(dtbsSourceId, tableName, reload);
				
				var _this = this;
				
				if($.isFunction(callback))
				{
					$.getJSON(loadUrl, function(table)
					{
						_this._inflateColumnInfo(table);
						_this._setCachedTable(dtbsSourceId, table);
						
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
						_this._inflateColumnInfo(table);
						_this._setCachedTable(dtbsSourceId, table);
						
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
		
		_getCachedTable : function(dtbsSourceId, tableName)
		{
			var tables = (this.dtbsSourceTableCache[dtbsSourceId] || (this.dtbsSourceTableCache[dtbsSourceId] = {}));
			return tables[tableName];
		},
		
		_setCachedTable : function(dtbsSourceId, table)
		{
			var tables = (this.dtbsSourceTableCache[dtbsSourceId] || (this.dtbsSourceTableCache[dtbsSourceId] = {}));
			tables[table.name] = table;
		},
		
		_inflateColumnInfo: function(table)
		{
			var columns = (table.columns || []);
			$.each(columns, function(i, column)
			{
				column.isRequired = $.tableMeta.isRequiredColumn(column);
				column.isSupported = $.tableMeta.supportsColumn(column);
				column.isRenderAsTextarea = ($.tableMeta.isClobColumn(column) ||
											(column.size && column.size > $.tableMeta.columnAsTextareaLength
												&& $.tableMeta.isTextColumn(column)));
				column.isImportKey = $.tableMeta.columnImportKey(table, column);
				column.isBinary = $.tableMeta.isBinaryColumn(column);
				
				if($.tableMeta.isDateColumn(column))
					column.isDate = true;
				else if($.tableMeta.isTimeColumn(column))
					column.isTime = true;
				else if($.tableMeta.isTimestampColumn(column))
					column.isTimestamp = true;
			});
		},
		
		columnAsTextareaLength : 101,
	});
})
(jQuery);
