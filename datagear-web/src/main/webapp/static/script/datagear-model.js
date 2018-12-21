/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 模型工具函数库。
 * 
 * 依赖:
 * jquery.js
 */
(function($, undefined)
{
	var $model = ($.model || ($.model = {}));
	var $propertyPath = ($.propertyPath || ($.propertyPath = {}));

	/**
	 * org.datagear.model.support.PropertyPath工具函数。
	 */
	$.extend($propertyPath,
	{
		ESCAPOR : "\\",
		
		PROPERTY : ".",
		
		CONCRETE_L : "<",
		
		CONCRETE_R : ">",
		
		ELEMENT_L : "[",
		
		ELEMENT_R : "]",
		
		/**
		 * 将属性路径字符串转换为属性路径数组。
		 * 参考org.datagear.model.support.PropertyPath.parse(String)。
		 */
		arrayValueOf : function(propertyPath)
		{
			var segmentList = [];
			
			var cs = propertyPath;
			var cache = "";
			
			for(var i=0, length = cs.length; i< length; i++)
			{
				var c = cs.charAt(i);
				
				if(c == this.ELEMENT_L)
				{
					var preSegment = (segmentList.length == 0 ? null : segmentList[segmentList.length - 1]);
					
					if (preSegment != null && preSegment.elementIndex != undefined)
						throw new Error("[" + propertyPath + "] is illegal, sequential element is not allowed");
					
					var j = i + 1;
					var hasCloseChar = false;
					
					for (; j < length; j++)
					{
						var cj = cs.charAt(j);

						if (cj == this.ESCAPOR)
						{
							var cjn = ((j + 1) < length ? cs.charAt(j+1) : 0);

							if (this.isKeyword(cjn))
							{
								j = j + 1;
								cache = this.appendIgnoreBlank(cache, cjn);
							}
							else
								cache = this.appendIgnoreBlank(cache, cj);
						}
						else if (cj == this.ELEMENT_R)
						{
							hasCloseChar = true;
							break;
						}
						else
							cache = this.appendIgnoreBlank(cache, cj);
					}

					if (!hasCloseChar)
						throw new Error("[" + propertyPath + "] is illegal, '" + this.ELEMENT_R + "' required at position [" + j + "]");

					var indexStr = cache; cache="";
					var index;

					try
					{
						index = parseInt(indexStr);
					}
					catch (e)
					{
						throw new Error("[" + propertyPath + "] is illegal, [" + indexStr + "] of position [" + (i + 1) + "] is not integer");
					}

					segmentList.push({ elementIndex : index });

					i = j;
				}
				// 属性具体模型索引
				else if (c == this.CONCRETE_L)
				{
					var property = (segmentList.length == 0 ? null : segmentList[segmentList.length - 1]);
					
					if (property == null || property.propertyName == undefined)
						throw new Error("[" + propertyPath + "] is illegal, property name required before position [" + i + "]");

					var j = i + 1;
					var hasCloseChar = false;

					for (; j < length; j++)
					{
						var cj = cs.charAt(j);

						if (cj == this.ESCAPOR)
						{
							var cjn = ((j + 1) < length ? cs.charAt(j+1) : 0);

							if (this.isKeyword(cjn))
							{
								j = j + 1;
								cache = this.appendIgnoreBlank(cache, cj);
							}
							else
								cache = this.appendIgnoreBlank(cache, cj);
						}
						else if (cj == this.CONCRETE_R)
						{
							hasCloseChar = true;
							break;
						}
						else
							cache = this.appendIgnoreBlank(cache, cj);
					}

					if (!hasCloseChar)
						throw new Error("[" + propertyPath + "] is illegal, '" + this.CONCRETE_R + "' required at position [" + j + "]");

					var indexStr = cache; cache="";
					var index;

					try
					{
						index = parseInt(indexStr);
					}
					catch (e)
					{
						throw new Error("[" + propertyPath + "] is illegal, [" + indexStr + "] of position [" + (i + 1) + "] is not integer");
					}

					property.propertyModelIndex = index;

					i = j;
				}
				// 属性名
				else if (c == this.PROPERTY || i == 0)
				{
					var j = (c == this.PROPERTY ? i + 1 : i);

					for (; j < length; j++)
					{
						var cj = cs.charAt(j);

						if (cj == this.ESCAPOR)
						{
							var cjn = ((j + 1) < length ? cs.charAt(j+1) : 0);

							if (this.isKeyword(cjn))
							{
								j = j + 1;
								cache = this.appendIgnoreBlank(cache, cjn);
							}
							else
								cache = this.appendIgnoreBlank(cache, cj);
						}
						else if (cj == this.PROPERTY || cj == this.ELEMENT_L || cj == this.CONCRETE_L)
						{
							j = j - 1;
							break;
						}
						else
							cache = this.appendIgnoreBlank(cache, cj);
					}

					var propertyName = cache; cache="";

					if (propertyName == "")
						throw new Error(
								"[" + propertyPath + "] is illegal, property name character must be present at position ["
										+ (c == PROPERTY ? i + 1 : i) + "]");

					segmentList.push({ "propertyName" : propertyName});
					
					i = j;
				}
				else
					throw new Error("[" + propertyPath + "] is illegal");
			}
			
			return segmentList;
		},

		/**
		 * 在属性路径后面连接属性名称。
		 * 
		 * @param propertyPath 可选，属性路径
		 * @param propertyName 必选，属性名
		 * @param propertyConcreteIndex 可选，属性具体模型索引
		 */
		concatPropertyName : function(propertyPath, propertyName, propertyConcreteIndex)
		{
			if(arguments.length == 1)
			{
				propertyConcreteIndex = undefined;
				propertyName = propertyPath;
				propertyPath = undefined;
			}
			else if(arguments.length == 2)
			{
				if(typeof(propertyName) == "number")
				{
					propertyConcreteIndex = propertyName;
					propertyName = propertyPath;
					propertyPath = undefined;
				}
				else
				{
					propertyConcreteIndex = undefined;
				}
			}
			
			propertyName = this.escapePropertyName(propertyName);
			
			var re = "";
			
			if(propertyPath && propertyPath != "")
				re = propertyPath + this.PROPERTY + propertyName;
			else
				re = propertyName;
			
			if(propertyConcreteIndex != undefined)
				re += this.CONCRETE_L + propertyConcreteIndex + this.CONCRETE_R;
			
			return re;
		},
		
		/**
		 * 在属性路径后面连接元素索引。
		 * 
		 * @param propertyPath 可选，属性路径
		 * @param elementIndex 必选，元素索引
		 */
		concatElementIndex : function(propertyPath, elementIndex)
		{
			if(elementIndex == undefined)
			{
				elementIndex = propertyPath;
				propertyPath = undefined;
			}
			
			if(!propertyPath)
				propertyPath = "";
			
			return propertyPath + this.ELEMENT_L + elementIndex + this.ELEMENT_R;
		},
		
		/**
		 * 转义属性名使其符合PropertyPath规范。
		 * 参考org.datagear.model.support.PropertyPath.escapePropertyName(String)。
		 * 
		 * @param propertyName
		 */
		escapePropertyName : function(propertyName)
		{
			return propertyName;
			
			/* 后台dbmodel已经限定了propertyName不会包含特殊字符，不再需要此逻辑
			var epn = "";
			
			for(var i=0; i<propertyName.length; i++)
			{
				var c = propertyName.charAt(i);
				
				if(this.isKeyword(c))
					epn += this.ESCAPOR + c;
				else
					epn += c;
			}
			
			return epn;
			*/
		},
		
		/**
		 * 反转义由escapePropertyName()转义的属性名。
		 * 参考org.datagear.model.support.PropertyPath.unescapePropertyName(String)。
		 */
		unescapePropertyName : function(propertyName)
		{
			return propertyName;
			
			/* 后台dbmodel已经限定了propertyName不会包含特殊字符，不再需要此逻辑
			var pn = "";
			
			for(var i=0; i<propertyName.length; i++)
			{
				var c = propertyName.charAt(i);
				
				if(c == this.ESCAPOR)
				{
					var cin = ((i + 1) < propertyName.length ? propertyName.charAt(i + 1) : 0);
					
					if(this.isKeyword(cin))
					{
						i += 1;
						pn += cin;
					}
					else
						pn += c;
				}
				else
					pn += c;
			}
			
			return pn;
			*/
		},
		
		isKeyword : function(c)
		{
			return (c == this.PROPERTY
					|| c == this.CONCRETE_L
					|| c == this.CONCRETE_R
					|| c == this.ELEMENT_L
					|| c == this.ELEMENT_R);
		},
		
		isBeanAccessKeyword : function(c)
		{
			return (c == this.PROPERTY
					|| c == this.ELEMENT_L
					|| c == this.ELEMENT_R);
		},
		
		appendIgnoreBlank : function(cache, c)
		{
			if (this.isBlankChar(c))
				return cache;
			
			return cache += c;
		},
		
		isBlankChar : function(c)
		{
			return c == ' ' || c == '\t' || c == '\r' || c == '\n';
		}
	});
	
	$.extend($model,
	{
		/**
		 * 判断模型是否是实体模型。
		 * 
		 * @param model
		 */
		isEntityModel : function(model)
		{
			return (model.idProperties && model.idProperties.length > 0);
		},
		
		/**
		 * 判断模型是否是值模型。
		 * 
		 * @param model
		 */
		isValueModel : function(model)
		{
			return !this.isEntityModel(model);
		},
		
		/**
		 * 判断模型是否是复合模型。
		 * 
		 * @param model
		 * @returns
		 */
		isCompositeModel : function(model)
		{
			return (model.properties && model.properties.length > 0);
		},
		
		/**
		 * 判断模型是否是基本模型。
		 * 
		 * @param model
		 * @returns
		 */
		isPrimitiveModel : function(model)
		{
			return !this.isCompositeModel(model);
		},

		/**
		 * 获取指定名称的Property对象。
		 * 
		 * @param model
		 * @param propNameOrIndex 属性名称或者属性索引
		 */
		getProperty : function(model, propNameOrIndex)
		{
			//索引
			if(typeof(propNameOrIndex) == "number")
				return model.properties[propNameOrIndex];
			//属性名
			else
			{
				var index = this.getPropertyIndex(model, propNameOrIndex);
				
				if(index < 0)
					throw new Error("No property named '"+propNameOrIndex+"'");
				
				return model.properties[index];
			}
		},
		
		/**
		 * 获取属性索引。
		 */
		getPropertyIndex : function(model, propertyName)
		{
			var properties=model.properties;
			for(var i=0; i<properties.length; i++)
			{
				if(properties[i].name == propertyName)
					return i;
			}
			
			return -1;
		},
		
		/**
		 * 获取指定名称/索引的模型。
		 * 
		 * @param models
		 * @param modelNameOrIndex
		 */
		getModel : function(models, modelNameOrIndex)
		{
			if(typeof(modelNameOrIndex) == "number")
			{
				return models[modelNameOrIndex];
			}
			else
			{
				for(var i=0; i<models.length; i++)
				{
					if(models[i].name == modelName)
						return models[i];
				}
			}
			return null;
		},
		
		/**
		 * 获取模型索引。
		 * 
		 * @param models
		 * @param model
		 */
		getModelIndex : function(models, model)
		{
			for(var i=0; i<models.length; i++)
			{
				if(models[i].name == model.name)
					return i;
			}
			
			return -1;
		},
		
		/**
		 * 获取属性模型数组。
		 */
		getPropertyModels : function(property)
		{
			return property.models;
		},
		
		/**
		 * 获取属性模型索引。
		 * 
		 * @param property
		 * @param model
		 */
		getPropertyModelIndex : function(property, propertyModel)
		{
			var propertyModels = this.getPropertyModels(property);
			
			var index = this.getModelIndex(propertyModels, propertyModel);
			
			if(index < 0)
				throw new Error("Not correct property model");
			
			return index;
		},
		
		/**
		 * 获取指定索引的属性模型。
		 */
		getPropertyModelByIndex : function(property, propertyModelIndex)
		{
			var propertyModels = this.getPropertyModels(property);
			
			return propertyModels[propertyModelIndex];
		},
		
		/**
		 * 获取指定属性值对应的属性模型。
		 */
		getPropertyModelByValue : function(property, propertyValue)
		{
			//TODO 处理抽象属性
			return property.model;
		},
		
		/**
		 * 获取指定属性值对应的属性模型索引。
		 */
		getPropertyModelIndexByValue : function(property, propertyValue)
		{
			//TODO 处理抽象属性
			return 0;
		},
		
		/**
		 * 判断属性是否是抽象属性。
		 * 
		 * @param property
		 * @returns
		 */
		isAbstractedProperty : function(property)
		{
			return property.models.length > 1;
		},
		
		/**
		 * 判断属性是否是具体属性。
		 * 
		 * @param property
		 * @returns
		 */
		isConcreteProperty : function(property)
		{
			return !this.isAbstractedProperty(property);
		},
		
		/**
		 * 判断属性是否是多元属性（数组或者集合）。
		 * 
		 * @param property
		 * @returns {Boolean}
		 */
		isMultipleProperty : function(property)
		{
			return this.isCollectionProperty(property) || this.isArrayProperty(property);
		},

		/**
		 * 判断属性是否是数组属性。
		 * 
		 * @param property
		 * @returns {Boolean}
		 */
		isArrayProperty : function(property)
		{
			return property.array;
		},
		
		/**
		 * 判断属性是否是集合属性。
		 * 
		 * @param property
		 * @returns {Boolean}
		 */
		isCollectionProperty : function(property)
		{
			return property.collectionType;
		},
		
		/**
		 * 判断属性是否是私有属性，私有属性无独立生命周期。
		 * 此函数实现参考org.datagear.persistence.support.PMU.isPrivate(Model, Property, Model)。
		 * 
		 * @param model
		 * @param property
		 * @param propertyConcreteModel
		 * @returns {Boolean}
		 */
		isPrivatePropertyModel : function(model, property, propertyConcreteModel)
		{
			var relationMapper = this.featureRelationMapper(property);
			var myIndex = this.getPropertyModelIndex(property, propertyConcreteModel);
			var mapper = relationMapper.mappers[myIndex];
			
			var keyRule = mapper.propertyKeyDeleteRule;

			if (keyRule && "CASCADE" == keyRule.ruleType)
				return true;
			
			//ModelTableMapper
			if(!mapper.modelKeyColumnNames && mapper.propertyKeyColumnNames)
			{
				return mapper.primitivePropertyMapper;
			}
			//PropertyTableMapper
			else if(mapper.modelKeyColumnNames && !mapper.propertyKeyColumnNames)
			{
				return true;
			}
			//JoinTableMapper
			else if(mapper.modelKeyColumnNames && mapper.propertyKeyColumnNames)
			{
				return false;
			}
			else
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
		 * @param model 必选，模型
		 * @param obj 必选，对象或者数组
		 * @parma id 可选
		 */
		id : function(model, obj, id)
		{
			var idProperties = model.idProperties;
			
			if(!idProperties)
				throw new Error("Model ["+model.name+"] is not entity Model");
			
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
		 * 获取/设置对象属性值。
		 * 
		 * @param obj 必选，对象
		 * @param property 必选，属性对象或者属性名称名称
		 * @parma propertyValue 可选，属性值
		 */
		propertyValue : function(obj, property, propertyValue)
		{
			if(obj == undefined || obj == null)
				throw new Error("[obj] must be defined");
			
			var propertyName = (property.name ? property.name : property);
			
			var isGet = (arguments.length == 2);
			
			if(isGet)
				return obj[propertyName];
			else
				obj[propertyName] = propertyValue;
		},

		/**
		 * 获取/设置对象属性值。
		 * 
		 * @param obj 必选，对象
		 * @param propertyPath 必选，属性路径字符串
		 * @parma propertyValue 可选，属性值
		 */
		propertyPathValue : function(obj, propertyPath, propertyValue)
		{
			if(obj == undefined || obj == null)
				throw new Error("[obj] must be defined");
			
			var isGet = (arguments.length == 2);
			
			var propertyPathArray=$.propertyPath.arrayValueOf(propertyPath);
			
			var currentObj = obj;
			
			for(var i=0; i<propertyPathArray.length; i++)
			{
				var segment=propertyPathArray[i];
				
				if(isGet)
				{
					if(currentObj == undefined || currentObj == null)
						return undefined;
					
					if(segment.propertyName != undefined)
						currentObj = currentObj[segment.propertyName];
					else if(segment.elementIndex != undefined)
						currentObj = currentObj[segment.elementIndex];
					else
						throw new Error();
				}
				else
				{
					if(i == propertyPathArray.length - 1)
					{
						if(segment.propertyName != undefined)
							currentObj[segment.propertyName] = propertyValue;
						else if(segment.elementIndex != undefined)
							currentObj[segment.elementIndex] = propertyValue;
						else
							throw new Error();
					}
					
					if(i < propertyPathArray.length - 1)
					{
						var myObj = undefined;
						
						if(segment.propertyName != undefined)
							myObj = currentObj[segment.propertyName];
						else if(segment.elementIndex != undefined)
							myObj = currentObj[segment.elementIndex];
						else
							throw new Error();
						
						if(!myObj)
						{
							var nextSegment = propertyPathArray[i + 1];
							
							if(nextSegment.propertyName != undefined)
							{
								myObj = {};
								currentObj[segment.propertyName] = myObj;
							}
							else if(nextSegment.elementIndex != undefined)
							{
								myObj = [];
								currentObj[segment.elementIndex] = myObj;
							}
							else
								throw new Error();
						}
						
						currentObj = myObj;
					}
				}
			}
			
			if(isGet)
				return currentObj;
		},
		
		/**
		 * 获取org.datagear.model.Label的文本。
		 * 
		 * @param label
		 * @returns
		 */
		text : function(label)
		{
			if(!label)
				return "";
			
			//TODO 根据客户端语言，返回特定文本
			return label.value;
		},
		
		/**
		 * 创建指定模型的实例对象。
		 * 
		 * @param model 模型
		 * @param data 可选，待填充的实例对象
		 */
		instance : function(model, data)
		{
			if(!this.isCompositeModel(model))
				throw new Error();
			
			data = (data || {});
			
			var properties=model.properties;
			for(var i=0; i<properties.length; i++)
			{
				var property=properties[i];
				
				if(data[property.name] != undefined)
					continue;
				
				if(property.defaultValue != undefined)
					data[property.name] = property.defaultValue;
			}
			
			return data;
		},
		
		/**
		 * 获取结尾属性信息。
		 * 
		 * @param model
		 * @param propertyPath
		 */
		getTailPropertyInfoConcrete : function(model, propertyPath)
		{
			var propertyInfo = this.getTailPropertyInfo(model, propertyPath);
			
			if(!propertyInfo.model)
				throw new Error("The tail property 's model must be set");
			
			return propertyInfo;
		},
		
		/**
		 * 获取结尾属性信息。
		 * 
		 * @param model
		 * @param propertyPath
		 */
		getTailPropertyInfo : function(model, propertyPath)
		{
			var propertyInfo = { "parent" : parent, "property" : undefined, "model" : undefined };
			
			var propertyPathArray=$.propertyPath.arrayValueOf(propertyPath);
			
			var parent = model;
			
			for(var i=0; i<propertyPathArray.length; i++)
			{
				var segment = propertyPathArray[i];
				
				//忽略元素片段
				if(segment.elementIndex != undefined)
					continue;
				
				if(parent == null)
					throw new Error("["+propertyPath+"] is not legal property path, middle Model is null");
				
				propertyInfo.parent = parent;
				
				propertyInfo.property = this.getProperty(parent, segment.propertyName);
				
				if(propertyInfo.property == null)
					throw new Error("No Property named ["+propName+"] found int Model ["+model.name+"]");
				
				if(this.isConcreteProperty(propertyInfo.property))
					propertyInfo.model = propertyInfo.property.model;
				else if(segment.propertyModelIndex)
					propertyInfo.model = propertyInfo.property.models[propertyModelIndex];
				
				parent = propertyInfo.model;
			}
			
			if(!propertyInfo.property)
				throw new Error("["+propertyPath+"] is not JavaBean property path");
			
			return propertyInfo;
		},
		
		/**
		 * 查找org.datagear.model.features.Select选项集中特定值的选项。
		 * 
		 * @param select
		 * @param value
		 * @returns
		 */
		option : function(select, value)
		{
			var options=select.options;
			
			if(options)
			{
				for(var i=0; i<options.length; i++)
				{
					var _option=options[i];
					
					//TODO 处理对象判断
					if(_option.value == value)
						return _option;
				}
			}
			
			return null;
		},
		
		/**
		 * 查找org.datagear.model.features.Select选项集中特定值的选项索引。
		 * 
		 * @param select
		 * @param value
		 * @returns
		 */
		optionIndex : function(select, value)
		{
			var options=select.options;
			
			if(options)
			{
				for(var i=0; i<options.length; i++)
				{
					var _option=options[i];
					
					//TODO 处理对象判断
					if(_option.value == value)
						return i;
				}
			}
			
			return null;
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
				else
					epn += c;
			}
			
			return epn;
		},

		/**
		 * 获取模型/属性的展示名称。
		 */
		displayName : function(modelOrProperty)
		{
			var nameLabel = this.feature(modelOrProperty, "NameLabel");
			
			if(nameLabel && nameLabel.value)
				return this.escapeHtml(this.text(nameLabel.value));
			
			var columnName = this.feature(modelOrProperty, "ColumnName");
			
			if(columnName && columnName.value)
				return this.escapeHtml(columnName.value);
			
			var tableName = this.feature(modelOrProperty, "TableName");
			
			if(tableName && tableName.value)
				return this.escapeHtml(tableName.value);
			
			return this.escapeHtml(modelOrProperty.name);
		},
		
		/**
		 * 获取模型/属性的展示描述。
		 */
		displayDesc : function(modelOrProperty)
		{
			var descLabel = this.feature(modelOrProperty, "DescLabel");
			
			if(descLabel && descLabel.value)
				return this.escapeHtml(this.text(descLabel.value));
			else
				return "";
		},
		
		/**
		 * 获取展示HTML。
		 * 
		 * @param modelOrProperty
		 * @param tagName 可选，HTML标签名
		 */
		displayInfoHtml : function(modelOrProperty, tagName)
		{
			if(!tagName)
				tagName = "span";
			
			var displayName = this.displayName(modelOrProperty);
			var displayDesc = this.displayDesc(modelOrProperty);
			var isToken = this.hasFeatureToken(modelOrProperty);
			
			var label = "<"+tagName+" class='display-info" + (isToken ? " display-info-token" : "") + "' title='"+displayDesc+"'>"+displayName +"</"+tagName+">";
			
			return label;
		},
		
		/**
		 * 获取指定模型数据对象的表意字符串。
		 * 
		 * @param model
		 * @param obj
		 */
		token : function(model, obj, ignorePropertyNames)
		{
			if(obj == undefined || obj == null)
				return "";
			
			var re="";
			
			if(this.isPrimitiveModel(model))
			{
				if(this.isFileTypeModel(model))
				{
					var rawValue = this.getFilePropertyRawValue(obj);
					var showValue = this.getFilePropertyShowValue(obj);
					
					obj = (showValue ? showValue : rawValue);
				}
				
				re=obj+"";
			}
			else
			{
				var properties=model.properties;
				var tokenProperties = [];
				
				//先使用Token属性
				for(var i=0; i<properties.length; i++)
				{
					var property=properties[i];
					
					if(this.containsOrEquals(ignorePropertyNames, property.name))
						continue;
					
					if(this.hasFeatureToken(property))
						tokenProperties.push(property);
				}
				
				//取前5个属性
				if(tokenProperties.length == 0)
				{
					for(var i=0; i<properties.length; i++)
					{
						if(tokenProperties.length >= 5)
							break;
						
						var property=properties[i];
						
						if(this.containsOrEquals(ignorePropertyNames, property.name))
							continue;
						
						tokenProperties.push(property);
					}
				}
				
				for(var i=0; i<tokenProperties.length; i++)
				{
					var property=tokenProperties[i];
					
					var v=this.propertyValue(obj, property.name);
					var tv = this.tokenProperty(property, v);
					
					if(re != "" && tv != "")
						re+=", ";
					
					re+=tv;
				}
			}
			
			return re;
		},
		
		/**
		 * 获取指定属性数据对象的表意字符串。
		 * 
		 * @param property
		 * @param propertyValue
		 */
		tokenProperty : function(property, propertyValue)
		{
			var re = "";
			
			var v= propertyValue;
			
			if(this.isMultipleProperty(property))
			{
				if(propertyValue && propertyValue.size != undefined)
					re = propertyValue.size+"";
				else
					re="0";
			}
			else if(v == undefined || v == null)
			{
				re = "";
			}
			else
			{
				if(this.hasFeatureSelect(property))
				{
					var option=this.option(this.featureSelect(property), v);
					
					re = this.text(option.label);
				}
				else
				{
					var propertyModel = this.getPropertyModelByValue(property, propertyValue);
					
					var ignorePropertyName = this.findMappedByWith(property, propertyModel);
					re = this.token(propertyModel, v, ignorePropertyName);
				}
			}
			
			if(re.length > 100)
				return re.substr(0, 97) + "...";
			else
				return re;
		},
		
		/**
		 * 构建文件属性详细值对象。
		 */
		toFilePropertyDetailValue : function(value, showValue)
		{
			return { "value" : value, "showValue" : showValue };
		},
		
		/**
		 * 是否是文件属性详细值对象。
		 */
		isFilePropertyDetailValue : function(value)
		{
			return $.isPlainObject(value) && value.hasOwnProperty("value") && value.hasOwnProperty("showValue");
		},
		
		/**
		 * 获取原始文件属性值。
		 */
		getFilePropertyRawValue : function(value)
		{
			if(this.isFilePropertyDetailValue(value))
				return value.value;
			else
				return value;
		},
		
		/**
		 * 获取显示文件属性值。
		 */
		getFilePropertyShowValue : function(value)
		{
			if(this.isFilePropertyDetailValue(value))
				return value.showValue;
			else
				return undefined;
		},

		/**
		 * 判断org.datagear.model.Featured对象是否有指定特性。
		 * 
		 * @param featured
		 * @param featureName
		 */
		hasFeature : function(featured, featureName)
		{
			var features = featured.features;
			
			return !!features[featureName];
		},
		
		/**
		 * 获取/设置org.datagear.model.Featured对象的指定特性。
		 * 
		 * @param featured
		 * @param featureName
		 * @param featureValue
		 */
		feature : function(featured, featureName, featureValue)
		{
			var features = featured.features;
			
			if(featureValue == undefined)
				return features[featureName];
			else
			{
				features[featureName] = featureValue;
				return featureValue;
			}
		},
		
		/**
		 * 获取org.datagear.model.MapFeature的值。
		 * 
		 * @param mapFeature
		 * @param key
		 */
		getMapFeatureValue : function(mapFeature, key)
		{
			if(!mapFeature)
				return undefined;
			
			var re = (mapFeature.mapValues ? mapFeature.mapValues[key] : null);
			
			if(re == null || re == undefined)
				re = mapFeature.value;
			
			return re;
		},
		
		/**
		 * 判断是否有org.datagear.model.features.Select特性。
		 */
		hasFeatureSelect : function(property)
		{
			return this.hasFeature(property, "Select");
		},
		
		/**
		 * 获取org.datagear.model.features.Select特性。
		 */
		featureSelect : function(property)
		{
			return this.feature(property, "Select");
		},
		
		/**
		 * 判断是否有org.datagear.model.features.Token特性。
		 */
		hasFeatureToken : function(property)
		{
			return this.hasFeature(property, "Token");
		},
		
		/**
		 * 判断是否有org.datagear.model.features.NotReadable特性。
		 */
		hasFeatureNotReadable : function(property)
		{
			return this.hasFeature(property, "NotReadable");
		},
		
		/**
		 * 判断是否有org.datagear.model.features.NotEditable特性。
		 */
		hasFeatureNotEditable : function(property)
		{
			return this.hasFeature(property, "NotEditable");
		},
		
		/**
		 * 判断是否有org.datagear.model.features.MaxLength特性。
		 */
		hasFeatureMaxLength : function(property)
		{
			return this.hasFeature(property, "MaxLength");
		},
		
		/**
		 * 获取org.datagear.model.features.MaxLength特性。
		 */
		featureMaxLength : function(property)
		{
			return this.feature(property, "MaxLength");
		},

		/**
		 * 判断是否有org.datagear.model.features.NotNull特性。
		 */
		hasFeatureNotNull : function(property)
		{
			return this.hasFeature(property, "NotNull");
		},

		/**
		 * 判断是否有org.datagear.persistence.features.AutoGenerated特性。
		 */
		hasFeatureAutoGenerated : function(property)
		{
			return this.hasFeature(property, "AutoGenerated");
		},
		
		/**
		 * 判断是否有org.datagear.persistence.features.ValueGenerator特性。
		 */
		hasFeatureValueGenerator : function(property)
		{
			return this.hasFeature(property, "ValueGenerator");
		},

		/**
		 * 获取org.datagear.model.features.MaxLength特性。
		 */
		featureRelationMapper : function(property)
		{
			return this.feature(property, "RelationMapper");
		},
		
		/**
		 * 获取模型对应的表名称。
		 */
		featureTableName : function(model)
		{
			var tableNameFeature = this.feature(model, "TableName");
			
			return tableNameFeature.value;
		},
		
		/**
		 * 获取属性模型的JDBC类型值，如果没有，则返回undefined。
		 */
		featureJdbcTypeValue : function(property, propertyModelIndex)
		{
			var jdbcTypeFeature = this.feature(property, "JdbcType");
			
			return this.getMapFeatureValue(jdbcTypeFeature, propertyModelIndex);
		},
		
		/**
		 * 查找MappedBy目标或者源属性名，如果没有，此方法将返回undefined。
		 */
		findMappedByWith : function(property, propertyModel)
		{
			var propertyModelIndex = this.getPropertyModelIndex(property, propertyModel);
			
			//有MappedBy特性
			var mappedBy = this.feature(property, "MappedBy");
			var mappedByTarget = (mappedBy ? this.getMapFeatureValue(mappedBy, propertyModelIndex) : null);
			if(mappedByTarget)
				return mappedByTarget;
			
			var relationMapper = this.featureRelationMapper(property);
			var mapper = relationMapper.mappers[propertyModelIndex];
			
			return mapper.mappedBySource;
		},
		
		/**
		 * 判断模型类型是否是File。
		 */
		isFileTypeModel : function(model)
		{
			return "File" == model.type || "java.io.File" == model.type;
		},
		
		/**
		 * 获取模型类型。
		 */
		getModelType : function(model)
		{
			return model.type;
		},
		
		/**
		 * 是否包含或等于给定值。
		 */
		containsOrEquals : function(arrayOrValue, value)
		{
			if(!arrayOrValue)
				return false;
			
			if(value == arrayOrValue)
				return true;
			
			for(var i=0; i<arrayOrValue.length; i++)
			{
				if(value == arrayOrValue[i])
					return true;
			}
			
			return false;
		}
	});
})
(jQuery);
