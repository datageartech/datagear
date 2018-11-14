/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 模型缓存。
 * 
 * 依赖:
 * jquery.js
 * datagear-model.js
 */
(function($, undefined)
{
	var $model = ($.model || ($.model = {}));
	
	if(!$model.schemaModels)
		$model.schemaModels = {};
	
	$.extend($model,
	{
		/**
		 * 加载模型的URL。
		 * 
		 * @param schemaId
		 * @param tableName
		 * @param reload 是否让后台重新载入
		 */
		loadModelUrl : function(schemaId, tableName, reload)
		{
			var url = "";
			
			if(typeof(contextPath) == "string")
				url = contextPath;
			
			url = url + "/schema/" + encodeURIComponent(schemaId) +"/model/" + encodeURIComponent(tableName);
			
			if(reload)
				url = url +"?reload=1";
			
			return url;
		},
		
		/**
		 * 在指定模型上执行callback操作。
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
		 * 获取指定名称的模型对象。
		 * 
		 * @param schemaId
		 * @param tableName
		 */
		get : function(schemaId, tableName)
		{
			return this._getCacheModel(schemaId, tableName);
		},
		
		/**
		 * 载入指定名称的模型对象。
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
		 * 在指定模型上执行callback。
		 * 
		 * @param schemaId
		 * @param tableName 表名
		 * @param callback || options callback：载入回调函数，格式为：function(model){ ... }，options：ajax请求options
		 * @param reload 是否让后台重新载入
		 */
		_on : function(schemaId, tableName, callback, reload)
		{
			var model = this._getCacheModel(schemaId, tableName);
			
			if(model == null || reload)
			{
				var loadUrl = this.loadModelUrl(schemaId, tableName, reload);
				
				var _this = this;
				
				if($.isFunction(callback))
				{
					$.getJSON(loadUrl, function(model)
					{
						_this._setCacheModel(schemaId, model);
						
						if(callback != undefined)
							callback(model);
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
					options.success = function(model, textStatus, jqXHR)
					{
						_this._setCacheModel(schemaId, model);
						
						if(originalSuccessCallback)
							originalSuccessCallback.call(this, model, textStatus, jqXHR);
					};
					
					$.ajax(options);
				}
				else
					throw new Error("Unknown function parameter type");
			}
			else
			{
				if($.isFunction(callback))
					callback(model);
				else if($.isPlainObject(callback))
					callback.success(model);
				else
					throw new Error("Unknown function parameter type");
			}
		},
		
		_getCacheModel : function(schemaId, tableName)
		{
			var models = (this.schemaModels[schemaId] || (this.schemaModels[schemaId] = {}));
			
			return models[tableName];
		},
		
		_setCacheModel : function(schemaId, model)
		{
			var models = (this.schemaModels[schemaId] || (this.schemaModels[schemaId] = {}));
			
			this._setCacheModelWithProperties(models, model, {});
		},
		
		_setCacheModelWithProperties : function(modelCache, model, myPutContext)
		{
			if(!model.properties)
				return;
			
			if(myPutContext[model.name])
				return;
			
			myPutContext[model.name] = true;
			
			var tableName = $.model.featureTableName(model);
			
			if(!tableName)
				return;
			
			modelCache[tableName] = model;
			
			var properties = model.properties;
			for(var i=0; i<properties.length; i++)
			{
				var propertyModels = properties[i].models;
				for(var j=0; j<propertyModels.length; j++)
				{
					this._setCacheModelWithProperties(modelCache, propertyModels[j], myPutContext);
				}
			}
		}
	});
})
(jQuery);
