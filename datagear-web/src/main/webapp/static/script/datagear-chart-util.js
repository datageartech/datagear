/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 图表工具集：window.chartUtil。
 * 
 * 依赖:
 * echarts.js
 */
(function(window)
{
	var util = (window.chartUtil || (window.chartUtil = {}));
	util.echarts = (util.echarts || {});
	util.dataset = (util.dataset || {});
	
	/**
	 * 获取图表主题。
	 */
	util.chartTheme = function(chart)
	{
		return this.renderContextAttr(chart, "chartTheme");
	};
	
	/**
	 * 获取/设置图表渲染上下文的属性值。
	 * 
	 * @param chart
	 * @param attrName
	 * @param attrValue 可选，要设置的属性值
	 */
	util.renderContextAttr = function(chart, attrName, attrValue)
	{
		var renderContext = chart.renderContext;
		
		if(attrValue == undefined)
			return renderContext.attributes[attrName];
		else
			return renderContext.attributes[attrName] = attrValue;
	};
	
	/**
	 * 初始化Echarts对象。
	 * 
	 * @param par 图表对象或者HTML元素ID
	 */
	util.echarts.init = function(par)
	{
		var elementId = (par.elementId || par);
		
		var echartsObj = echarts.init(document.getElementById(elementId));
		
		return echartsObj;
	};
	
	/**
	 * 获取数据集数组的第一个元素，没有则返回undefined。
	 * 
	 * @param dataSets
	 */
	util.dataset.first = function(dataSets)
	{
		return (dataSets && dataSets.length > 0 ? dataSets[0] : undefined);
	};

	/**
	 * 获取指定名称的列元信息对象，没有则返回undefined。
	 * 
	 * @param dataSet
	 * @param name
	 */
	util.dataset.columnMetaByName = function(dataSet, name)
	{
		var columnMetas = dataSet.meta.columnMetas;
		
		for(var i=0; i<columnMetas.length; i++)
		{
			if(columnMetas[i].name === name)
				return columnMetas[i];
		}
		
		return undefined;
	};
	
	/**
	 * 获取第一个维度列元信息对象，没有则返回undefined。
	 * 
	 * @param dataSet
	 */
	util.dataset.columnMetaByDimension = function(dataSet)
	{
		var columnMetas = dataSet.meta.columnMetas;
		
		for(var i=0; i<columnMetas.length; i++)
		{
			if(columnMetas[i].dataCategory === "DIMENSION")
				return columnMetas[i];
		}
		
		return undefined;
	};
	
	/**
	 * 获取维度列元信息对象数组。
	 * 
	 * @param dataSet
	 */
	util.dataset.columnMetasByDimension = function(dataSet)
	{
		var re = [];
		
		var columnMetas = dataSet.meta.columnMetas;
		
		for(var i=0; i<columnMetas.length; i++)
		{
			if(columnMetas[i].dataCategory === "DIMENSION")
				re.push(columnMetas[i]);
		}
	};

	/**
	 * 获取第一个量度列元信息对象。
	 * 
	 * @param dataSet
	 */
	util.dataset.columnMetaByScalar = function(dataSet)
	{
		var columnMetas = dataSet.meta.columnMetas;
		
		for(var i=0; i<columnMetas.length; i++)
		{
			if(columnMetas[i].dataCategory === "SCALAR")
				return columnMetas[i];
		}
		
		return undefined;
	};
	
	/**
	 * 获取量度列元信息对象数组。
	 * 
	 * @param dataSet
	 */
	util.dataset.columnMetasByScalar = function(dataSet)
	{
		var re = [];
		
		var columnMetas = dataSet.meta.columnMetas;
		
		for(var i=0; i<columnMetas.length; i++)
		{
			if(columnMetas[i].dataCategory === "SCALAR")
				re.push(columnMetas[i]);
		}
	};
	
	/**
	 * 获取列值数组。
	 * 
	 * @param dataSet
	 * @param columnMeta 列元信息对象、列名、列元信息对象数组、列名数组
	 */
	util.dataset.columnValues = function(dataSet, columnMeta)
	{
		var re = [];
		
		if(columnMeta == null)
			return re;
		
		var datas = dataSet.datas;
		
		if(columnMeta.length > 0)
		{
			for(var i=0; i<columnMeta.length; i++)
			{
				var myValues = [];
				
				var cm = columnMeta[i];
				
				var name = (cm.name || cm);
				
				for(var j=0; j< datas.length; j++)
					myValues[j] = datas[j][name];
				
				re[i] = myValues;
			}
		}
		else
		{
			var name = (columnMeta.name || columnMeta);
			
			for(var i=0; i< datas.length; i++)
				re[i] = datas[i][name];
		}
		
		return re;
	};
})
(window);