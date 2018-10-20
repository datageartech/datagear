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
						
						re[j][propName] = this.propValue(obj[j], propName);
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
						this.propValue(obj[j], propName, id[j][propName]);
				}
				
				return id;
			}
		},
		
		/**
		 * 获取/设置对象属性值。
		 * 
		 * @param obj
		 * @param propName
		 * @parma propValue
		 */
		propValue : function(obj, propName, propValue)
		{
			if(obj == undefined || obj == null)
				throw new Error("[obj] must be defined");
			
			var isGet = (arguments.length == 2);
			
			var ppAry=propName.split(".");
			
			var currentObj = obj;
			
			for(var i=0; i<ppAry.length; i++)
			{
				if((currentObj == undefined || currentObj == null) && isGet)
					return undefined;

				var mySegment=ppAry[i];
				
				var javaBeanPropertyNameSegment = undefined;
				var arrayIndexSegment = undefined;

				var bli = mySegment.indexOf("[");
				
				//JavaBean
				if(bli < 0)
				{
					javaBeanPropertyNameSegment = mySegment;
				}
				//数组
				else if(bli == 0)
				{
					arrayIndexSegment = mySegment;
				}
				//JavaBean、数组组合
				else
				{
					var bri = mySegment.indexOf("]", bli + 1);
					
					javaBeanPropertyNameSegment = mySegment.substring(0, bli);
					arrayIndexSegment = mySegment.substring(bli + 1, bri);
				}
				
				var javaBeanPropertyName = undefined;
				var arrayNumberIndex = undefined;
				var arrayMapIndex = undefined;
				
				if(javaBeanPropertyNameSegment)
				{
					javaBeanPropertyName = javaBeanPropertyNameSegment;
					
					var qi = mySegment.indexOf("<");
					if(qi > 0)
						javaBeanPropertyName = javaBeanPropertyNameSegment.substring(0, qi);
				}
				
				if(arrayIndexSegment)
				{
					try
					{
						arrayNumberIndex = parseInt(arrayIndexSegment);
					}
					catch(e)
					{
						arrayNumberIndex = undefined;
						
						//TODO 解析arrayMapIndex
					}
				}
				
				if(isGet || i < ppAry.length - 1)
				{
					if(javaBeanPropertyName != undefined)
						currentObj = currentObj[javaBeanPropertyName];
					
					if(currentObj && arrayNumberIndex != undefined)
						currentObj = currentObj[arrayNumberIndex];
				}
				else
				{
					if(javaBeanPropertyName != undefined && arrayNumberIndex != undefined)
					{
						if(!currentObj[javaBeanPropertyName]);
							currentObj[javaBeanPropertyName] = [];
						
						currentObj[javaBeanPropertyName][arrayNumberIndex] = propValue;
					}
					else if(javaBeanPropertyName != undefined)
					{
						currentObj[javaBeanPropertyName] = propValue;
					}
					else if(arrayNumberIndex != undefined)
					{
						currentObj[arrayNumberIndex] = propValue;
					}
					else
						throw new Error();
				}
			}
			
			if(isGet)
				return currentObj;
		},
		
		/**
		 * 复制为可用作KEY的对象，那些不可能被作为KEY的属性值将会被删除。
		 * <p>
		 * 数据对象可能包含LOB类的数据，在get请求时可能会导致URL超长，需要把它们删除。
		 * </p>
		 * 
		 * @param model 模型
		 * @param obj 模型数据对象
		 * @param keepPropertyPath 不删除的属性路径
		 */
		/*
		copyForKey : function(model, obj, keepPropertyPath)
		{
			var properties = model.properties;
			
			if(!properties)
				return obj;
			
			if(!obj || !properties)
				return {};
			
			var keyObj = {};
			
			var accessIndex = (keepPropertyPath ? keepPropertyPath.indexOf(".") : -1);
			var reservePropName = (accessIndex > -1 ? keepPropertyPath.substring(0, accessIndex) : "");
			
			for(var i=0; i< properties.length; i++)
			{
				var property = properties[i];
				var propName = property.name;
				var propValue = obj[propName];
				
				var copy = false;
				
				if(propValue == null)//NULL属性值也拷贝，因为几乎不影响URL长度
					copy = true;
				else if(propName == reservePropName)
					copy = true;
				else if(this.isIdProperty(model, property))
					copy = true;
				else
				{
					
				}
			}
			
			return keyObj;
		},
		*/
		
		/**
		 * 获取org.datagear.model.Label的文本。
		 * 
		 * @param label
		 * @returns
		 */
		text : function(label)
		{
			if(!label)
				return "??????";
			
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
		 * 获取指定名称的Property对象。
		 * 
		 * @param model
		 * @param propName
		 */
		getProperty : function(model, propName)
		{
			var properties=model.properties;
			for(var i=0; i<properties.length; i++)
			{
				var property=properties[i];
				if(property.name == propName)
				{
					return property;
				}
			}
			
			return null;
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
			
			var segments = propertyPath.split(".");
			
			var parent = model;
			
			for(var i=0; i<segments.length; i++)
			{
				var segment = segments[i];
				
				var elIdx = segment.indexOf("[");
				
				//忽略元素片段
				if(elIdx == 0)
					continue;
				
				if(parent == null)
					throw new Error("["+propertyPath+"] is not legal property path, middle Model is null");
				
				propertyInfo.parent = parent;
				
				var propName = null;
				var propModelIndex = null;
				
				var clIdx = segment.indexOf("<");
				
				var propNameEnd = segment.length;
				if(clIdx > 0)
					propNameEnd = clIdx;
				else if(elIdx > 0)
					propNameEnd = elIdx;
				
				propName = segment.substring(0, propNameEnd);
				
				if(clIdx > 0)
				{
					var crIdx = segment.indexOf(">", clIdx + 1);
					
					if(crIdx > 0)
						propModelIndex = parseInt(segment.substring(clIdx + 1, crIdx));
				}
				
				propertyInfo.property = this.getProperty(parent, propName);
				
				if(propertyInfo.property == null)
					throw new Error("No Property named ["+propName+"] found int Model ["+model.name+"]");
				
				if(this.isConcreteProperty(propertyInfo.property))
					propertyInfo.model = propertyInfo.property.model;
				else if(propModelIndex)
					propertyInfo.model = propertyInfo.property.models[propModelIndex];
				
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
		 * 获取属性标签。
		 */
		propertyLabel : function(property)
		{
			var propName = property.name;
			var nameLabel = this.featureNameLable(property);
			
			var label = propName + (nameLabel == propName || !nameLabel ? "" : "(" + nameLabel +")");
			
			return label;
		},
		
		/**
		 * 获取属性标签HTML。
		 */
		propertyLabelHtml : function(property)
		{
			var propName = property.name;
			var nameLabel = this.featureNameLable(property);
			var isToken = this.hasFeatureToken(property);
			
			var label = "<span class='property-name"+(isToken ? " property-name-token" : "")+"'>"+propName +"</span>"
				+ (nameLabel == propName || !nameLabel ? "" : "<span class='bracket bracket-left'>(</span><span class='prop-label'>"
				+ nameLabel +"</span><span class='bracket bracket-right'>)</span>");
			
			return label;
		},
		
		/**
		 * 获取属性标签HTML。
		 */
		propertyLabelHtmlOfTitle : function(property, tagName)
		{
			var propName = property.name;
			var nameLabel = this.featureNameLable(property);
			var isToken = this.hasFeatureToken(property);
			
			var label = "<"+tagName+" class='property-name" + (isToken ? " property-name-token" : "") + "' title='"+nameLabel+"'>"+propName +"</"+tagName+">";
			
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
					
					var v=this.propValue(obj, property.name);
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
					//TODO 处理抽象属性
					var propertyModel = property.model;
					
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
		 * 获取Model或者Property的NameLabel特性。
		 */
		featureNameLable : function(modelOrProperty)
		{
			var label = "";
			
			var nameLabel = this.feature(modelOrProperty, "NameLabel");
			if(!nameLabel)
				label = modelOrProperty.name;
			else
				label = this.text(nameLabel.value);
			
			if(!label)
				label = "";
			
			return label;
		},
		
		/**
		 * 获取Model或者Property的NameLabel特性。
		 */
		featureDescLable : function(modelOrProperty)
		{
			var descLabel = this.feature(modelOrProperty, "DescLabel");
			if(!descLabel)
				return modelOrProperty.name;
			else
				return this.text(descLabel.value);
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
