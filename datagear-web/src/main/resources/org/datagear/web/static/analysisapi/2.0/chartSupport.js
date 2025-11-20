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
 * 图表支持库。
 * 全局变量名：window.chartFactory.chartSupport
 * 
 * 加载时依赖：
 *   chartFactory.js
 * 
 * 运行时依赖:
 *   jquery.js
 *   echarts.js
 */
(function(global){

var CF = global.chartFactory;
var SPT = (CF.chartSupport || (CF.chartSupport = {}));
var EU = (SPT.echartsUtil || (SPT.echartsUtil = {}));

SPT.ECHARTS_RENDERER_DEPEND =
[
	{ name: "echarts", acceptVersion: ">=5.0" }
];

//扩展图表选项名：处理图表渲染选项
SPT.PROCESS_RENDER_OPTIONS_OPTION_NAME = "processRenderOptions";

//扩展图表选项名：处理图表更新选项
SPT.PROCESS_UPDATE_OPTIONS_OPTION_NAME = "processUpdateOptions";

//在chart.liveData()中存储渲染选项的名称
SPT.RENDER_OPTIONS_LIVE_DATA_NAME = "renderOptions";

//在chart.liveData()中存储更新选项的名称
SPT.UPDATE_OPTIONS_LIVE_DATA_NAME = "updateOptions";

//图表数据属性名：原始类别
SPT.ORIGINAL_CATEGORY_PROP_NAME = "originalCategory";

//图表数据属性名：原始数据
SPT.ORIGINAL_DATA_PROP_NAME = "originalData";

//图表数据属性名：原始ID
SPT.ORIGINAL_ID_PROP_NAME = "originalId";

//图表数据属性名：原始ID
SPT.ORIGINAL_PARENT_PROP_NAME = "originalParent";

//树图虚拟根节点标识属性名
SPT.VIRTUAL_ROOT_PROP_NAME = "virtualRoot";

//图表数据属性名：原始源ID
SPT.ORIGINAL_SOURCE_ID_PROP_NAME = "originalSourceId";

//图表数据属性名：原始源名
SPT.ORIGINAL_SOURCE_NAME_PROP_NAME = "originalSourceName";

//图表数据属性名：原始目标ID
SPT.ORIGINAL_TARGET_ID_PROP_NAME = "originalTargetId";

//图表数据属性名：原始目标名
SPT.ORIGINAL_TARGET_NAME_PROP_NAME = "originalTargetName";

//图表元素属性名：ECharts主题名
EU.ELE_ATTR_ECHARTS_THEME = "dg-echarts-theme";

//图表主题中的ECharts主题名属性名
EU.THEME_PROP_ECHARTS_THEME_NAME = "DG_ECHARTS_THEME_NAME";

//注册地图状态
//键："地图名"；值：{ loadPromise: Promise }
EU.MAP_REGISTER_STATES = {};

//扩展ECharts图表选项：地图名
//默认的ECharts地图类图表配置地图名稍微麻烦，所有这里的内置图表都支持此快捷方式设置地图名选项
EU.MAP_NAME_OPTION_NAME = "mapName";

//扩展ECharts图表选项：类目轴数据排序配置选项名
//对于多数据集场景，如果采用数据先后顺序提取的轴数据，顺序可能不符合预期，需要重新排序
EU.SORT_AXIS_DATA_OPTION_NAME = "sortAxisData";

//折线图

SPT.lineRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		//是否堆叠
		stack: false,
		//是否平滑
		smooth: false,
		//是否面积
		area: false,
		//阶梯：true, false, "start", "middle", "end"
		step: false,
		//是否互换坐标轴
		interchangeAxis: false
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		render: function(chart)
		{
			var dataSetBind = chart.dataSetBindMain();
			var nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
			var valueField = chart.dataSetFieldOfSign(dataSetBind, 1);
			
			var options =
			{
				title: { text: chart.name() },
				tooltip: { trigger: "axis" },
				legend: { data: [] },
				xAxis:
				{
					name: chart.dataSetFieldAlias(dataSetBind, nameField),
					type: SPT.evalDataSetFieldAxisType(chart, nameField),
					boundaryGap: false
				},
				yAxis:
				{
					name: chart.dataSetFieldAlias(dataSetBind, valueField),
					type: SPT.evalDataSetFieldAxisType(chart, valueField)
				},
				series:
				[
					{ type: "line", data: [] }
				]
			};
			
			if(config.interchangeAxis)
			{
				let xAxisTmp = options.xAxis;
				options.xAxis = options.yAxis;
				options.yAxis = xAxisTmp;
			}
			
			options = EU.prepareRenderOptions(chart, options);
			var instance = EU.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var legendData = [];
			var series = [];
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let dataSetAlias = chart.dataSetAlias(dataSetBind);
				let result = chart.resultOf(chartResult, dataSetBind);
				
				let nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
				let categoryField = chart.dataSetFieldOfSign(dataSetBind, 2);
				
				if(categoryField)
				{
					let valueField = chart.dataSetFieldOfSign(dataSetBind, 1);
					let categoryNames = [];
					let categoryDatasMap = {};
					
					//使用{name,value:[]}格式可以更好地兼容category、value、time坐标轴类型以及tooltip、事件数据
					let fieldMap = { name: nameField, value: [nameField, valueField] };
					fieldMap = SPT.addCategoryToFieldMap(fieldMap, categoryField);
					let data = chart.resultMapDatas(result, fieldMap);
					SPT.originalDataOfResult(data, chart, result);
					SPT.splitDataByCategory(data, categoryNames, categoryDatasMap);
					
					for(let j=0; j<categoryNames.length; j++)
					{
						let categoryName = categoryNames[j];
						let legendName = SPT.legendNameForDataCategory(dataSetBinds, dataSetAlias, categoryName);
						let mySeries = { name: legendName, data: categoryDatasMap[categoryName] };
						
						this._configSingleSeries(chart, mySeries);
						legendData.push({ name: legendName });
						series.push(mySeries);
					}
				}
				else
				{
					let valueFields = chart.dataSetFieldsOfSign(dataSetBind, 1);
					
					for(let j=0; j<valueFields.length; j++)
					{
						let legendName = SPT.legendNameForDataValues(chart, dataSetBinds, dataSetBind, dataSetAlias, valueFields, j);
						//使用{name,value:[]}格式可以更好地兼容category、value、time坐标轴类型以及tooltip、事件数据
						let fieldMap = { name: nameField, value: [nameField, valueFields[j]] };
						let data = chart.resultMapDatas(result, fieldMap);
						SPT.originalDataOfResult(data, chart, result);
						let mySeries = { name: legendName, data: data };
						
						this._configSingleSeries(chart, mySeries);
						legendData.push({ name: legendName });
						series.push(mySeries);
					}
				}
			}
			
			//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
			var options = { legend: { data: legendData }, series: series };
			//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
			(config.interchangeAxis ? (options.yAxis = {}) : (options.xAxis = {}));
			
			EU.inflateUpdateAxisData(chart, options, (config.interchangeAxis ? options.yAxis : options.xAxis),
					EU.axisDataExtractors.valueElement0());
			options = EU.prepareUpdateOptions(chart, options, (options) => { EU.adaptValueArrayData(chart, options, "line"); });
			
			EU.setOptionsReplaceMerge(chart, options);
		},
		
		_configSingleSeries: function(chart, series)
		{
			series.type = "line";
			series.encode = (config.interchangeAxis ? { x: 1, y: 0 } : { x: 0, y: 1 });
			
			//折线图按数据集分组没有展示效果，所以都使用同一个堆叠
			if(config.stack)
				series.stack = "stack";
			
			if(config.smooth)
				series.smooth = true;
			
			if(config.area)
				series.areaStyle = {};
			
			if(config.step !== false)
				series.step = config.step;
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//柱状图

SPT.barRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		//是否堆叠
		stack: false,
		//是否按数据集分组堆叠
		stackGroup: true,
		//是否互换坐标轴
		interchangeAxis: false
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		render: function(chart)
		{
			var dataSetBind = chart.dataSetBindMain();
			var nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
			var valueField = chart.dataSetFieldOfSign(dataSetBind, 1);
			
			var options =
			{
				title: { text: chart.name() },
				tooltip: { trigger: "item" },
				legend: { data: [] },
				xAxis:
				{
					name: chart.dataSetFieldAlias(dataSetBind, nameField),
					type: SPT.evalDataSetFieldAxisType(chart, nameField)
				},
				yAxis:
				{
					name: chart.dataSetFieldAlias(dataSetBind, valueField),
					type: SPT.evalDataSetFieldAxisType(chart, valueField)
				},
				series:
				[
					{ type: "bar", data: [] }
				]
			};
			
			if(config.interchangeAxis)
			{
				let xAxisTmp = options.xAxis;
				options.xAxis = options.yAxis;
				options.yAxis = xAxisTmp;
			}
			
			options = EU.prepareRenderOptions(chart, options);
			var instance = EU.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var legendData = [];
			var series = [];
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let dataSetAlias = chart.dataSetAlias(dataSetBind);
				let result = chart.resultOf(chartResult, dataSetBind);
				
				let nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
				let categoryField = chart.dataSetFieldOfSign(dataSetBind, 2);
				
				if(categoryField)
				{
					let valueField = chart.dataSetFieldOfSign(dataSetBind, 1);
					let categoryNames = [];
					let categoryDatasMap = {};
					
					//使用{name,value:[]}格式可以更好地兼容category、value、time坐标轴类型以及tooltip、事件数据
					let fieldMap = { name: nameField, value: [nameField, valueField] };
					fieldMap = SPT.addCategoryToFieldMap(fieldMap, categoryField);
					let data = chart.resultMapDatas(result, fieldMap);
					SPT.originalDataOfResult(data, chart, result);
					SPT.splitDataByCategory(data, categoryNames, categoryDatasMap);
					
					for(let j=0; j<categoryNames.length; j++)
					{
						let categoryName = categoryNames[j];
						let legendName = SPT.legendNameForDataCategory(dataSetBinds, dataSetAlias, categoryName);
						let mySeries = { name: legendName, data: categoryDatasMap[categoryName] };
						
						this._configSingleSeries(chart, mySeries, dataSetAlias);
						legendData.push({ name: legendName });
						series.push(mySeries);
					}
				}
				else
				{
					let valueFields = chart.dataSetFieldsOfSign(dataSetBind, 1);
					
					for(let j=0; j<valueFields.length; j++)
					{
						let legendName = SPT.legendNameForDataValues(chart, dataSetBinds, dataSetBind, dataSetAlias, valueFields, j);
						//使用{name,value:[]}格式可以更好地兼容category、value、time坐标轴类型以及tooltip、事件数据
						let fieldMap = { name: nameField, value: [nameField, valueFields[j]] };
						let data = chart.resultMapDatas(result, fieldMap);
						SPT.originalDataOfResult(data, chart, result);
						let mySeries = { name: legendName, data: data };
						
						this._configSingleSeries(chart, mySeries, dataSetAlias);
						legendData.push({ name: legendName });
						series.push(mySeries);
					}
				}
			}
			
			var options = { legend: { data: legendData }, series: series };
			//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
			(config.interchangeAxis ? (options.yAxis = {}) : (options.xAxis = {}));
			
			EU.inflateUpdateAxisData(chart, options, (config.interchangeAxis ? options.yAxis : options.xAxis),
							EU.axisDataExtractors.valueElement0());
			options = EU.prepareUpdateOptions(chart, options, (options) => { EU.adaptValueArrayData(chart, options, "bar"); });
			
			EU.setOptionsReplaceMerge(chart, options);
		},
		
		_configSingleSeries: function(chart, series, dataSetAlias)
		{
			series.type = "bar";
			series.encode = (config.interchangeAxis ? { x: 1, y: 0 } : { x: 0, y: 1 });
			
			if(config.stack)
			{
				series.stack = (config.stackGroup ? dataSetAlias : "stack");
				series.label = { show: true };
			}
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//极坐标柱状图

SPT.barPolarRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		//是否堆叠
		stack: false,
		//是否按数据集分组堆叠
		stackGroup: true,
		//坐标类型：radius（径向）、angle（角度）
		axisType: "radius"
	},
	config);
	
	var isAngleAxis = (config.axisType == "angle");
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		render: function(chart)
		{
			var dataSetBind = chart.dataSetBindMain();
			var nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
			
			var options =
			{
				title: { text: chart.name() },
				tooltip: { trigger: "item" },
				legend: { data: [] },
				polar: { radius: "60%" },
				angleAxis: {},
				radiusAxis: {},
				series:
				[
					{ type: "bar", data: [], coordinateSystem: "polar" }
				]
			};
			
			if(isAngleAxis)
			{
				options.angleAxis =
				{
					name: chart.dataSetFieldAlias(dataSetBind, nameField),
					type: SPT.evalDataSetFieldAxisType(chart, nameField),
					data: []
				};
				
				//非类目轴（比如：time）时的特殊设置
				if(options.angleAxis.type !== "category")
				{
					//需要设置boundaryGap，不然第一个条目可能会被最后一个条目覆盖不可见
					options.angleAxis.boundaryGap = ['8%', '8%'];
				}
			}
			else
			{
				options.radiusAxis =
				{
					name: chart.dataSetFieldAlias(dataSetBind, nameField),
					type: SPT.evalDataSetFieldAxisType(chart, nameField),
					nameGap: 20,
					data: []
				};
				
				//在ECharts-5.6.0中，当radiusAxis.type="value"时，会自动转为角度柱图，
				//此时，需要强制设为"category"，并将对应数据转换为字符串，否则会出现图元混乱的情况
				if(options.radiusAxis.type === "value")
				{
					options.radiusAxis.type = "category";
					chart.liveData("dataNameToString", true);
				}
			}
			
			options = EU.prepareRenderOptions(chart, options);
			var instance = EU.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var legendData = [];
			var series = [];
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let dataSetAlias = chart.dataSetAlias(dataSetBind);
				let result = chart.resultOf(chartResult, dataSetBind);
				
				let nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
				let categoryField = chart.dataSetFieldOfSign(dataSetBind, 2);
				
				if(categoryField)
				{
					let valueField = chart.dataSetFieldOfSign(dataSetBind, 1);
					let categoryNames = [];
					let categoryDatasMap = {};
					
					//使用{name,value:[]}格式可以更好地兼容category、value、time坐标轴类型以及tooltip、事件数据
					let fieldMap = { name: nameField, value: [nameField, valueField] };
					fieldMap = SPT.addCategoryToFieldMap(fieldMap, categoryField);
					let data = chart.resultMapDatas(result, fieldMap);
					SPT.originalDataOfResult(data, chart, result);
					
					if(chart.liveData("dataNameToString"))
						SPT.convertArrayValueEleToString(data);
					
					SPT.splitDataByCategory(data, categoryNames, categoryDatasMap);
					
					for(let j=0; j<categoryNames.length; j++)
					{
						let categoryName = categoryNames[j];
						let legendName = SPT.legendNameForDataCategory(dataSetBinds, dataSetAlias, categoryName);
						let mySeries = { name: legendName, data: categoryDatasMap[categoryName] };
						
						this._configSingleSeries(chart, mySeries, dataSetAlias);
						legendData.push({ name: legendName });
						series.push(mySeries);
					}
				}
				else
				{
					let valueFields = chart.dataSetFieldsOfSign(dataSetBind, 1);
					
					for(let j=0; j<valueFields.length; j++)
					{
						let legendName = SPT.legendNameForDataValues(chart, dataSetBinds, dataSetBind, dataSetAlias, valueFields, j);
						//使用{name,value:[]}格式可以更好地兼容category、value、time坐标轴类型以及tooltip、事件数据
						let fieldMap = { name: nameField, value: [nameField, valueFields[j]] };
						let data = chart.resultMapDatas(result, fieldMap);
						SPT.originalDataOfResult(data, chart, result);
						
						if(chart.liveData("dataNameToString"))
							SPT.convertArrayValueEleToString(data);
						
						let mySeries = { name: legendName, data: data };
						
						this._configSingleSeries(chart, mySeries, dataSetAlias);
						legendData.push({ name: legendName });
						series.push(mySeries);
					}
				}
			}
			
			var options = { legend: { data: legendData }, series: series };
			//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
			(isAngleAxis ? (options.angleAxis = {}) : (options.radiusAxis = {}));
			
			EU.inflateUpdateAxisData(chart, options, (isAngleAxis ? options.angleAxis : options.radiusAxis),
							EU.axisDataExtractors.valueElement0());
			options = EU.prepareUpdateOptions(chart, options, (options) => { EU.adaptValueArrayData(chart, options, "bar"); });
			
			EU.setOptionsReplaceMerge(chart, options);
		},
		
		_configSingleSeries: function(chart, series, dataSetAlias)
		{
			series.type = "bar";
			series.coordinateSystem = "polar";
			series.encode = (isAngleAxis ? { radius: 1, angle: 0 } : { radius: 0, angle: 1 });
			
			if(config.stack)
			{
				series.stack = (config.stackGroup ? dataSetAlias : "stack");
				series.label = { show: true };
			}
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//饼图

SPT.pieRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		//是否按数据集分割系列，而非仅一个系列
		splitDataSet: false,
		//当splitDataSet=true时，各系列布局：
		//nest：嵌套；grid：网格
		seriesLayout: "nest",
		//当splitDataSet=false且数据集无category标记时，是否环形图
		ring: false,
		//当splitDataSet=false且数据集无category标记时，是否玫瑰图
		rose: false
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		render: function(chart)
		{
			var options =
			{
				title: { text: chart.name() },
				tooltip:
				{
					trigger: "item",
					formatter: function(params)
					{
						return EU.customTooltip(params, (pi) =>
						{
							let re = { value: pi.value + " ("+pi.percent+"%)" };
							return re;
						});
					}
				},
				legend: { data: [] },
				series:
				[
					{ type: "pie", data: [] }
				]
			};
			
			options = EU.prepareRenderOptions(chart, options);
			var instance = EU.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var hasCategorySign = (chart.pluginDataSigns().length > 2);
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var series = [];
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let dataSetAlias = chart.dataSetAlias(dataSetBind);
				let result = chart.resultOf(chartResult, dataSetBind);
				
				let nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
				let valueField = chart.dataSetFieldOfSign(dataSetBind, 1);
				let categoryField = (hasCategorySign ? chart.dataSetFieldOfSign(dataSetBind, 2) : null);
				
				//饼图只支持{name,value}格式的数据
				let fieldMap = { name: nameField, value: valueField };
				if(categoryField)
					fieldMap = SPT.addCategoryToFieldMap(fieldMap, categoryField);
				
				let data = chart.resultMapDatas(result, fieldMap);
				SPT.originalDataOfResult(data, chart, result);
				
				if(categoryField)
				{
					let categoryNames = [];
					let categoryDatasMap = {};
					SPT.splitDataByCategory(data, categoryNames, categoryDatasMap);
					
					for(let j=0; j<categoryNames.length; j++)
					{
						let categoryName = categoryNames[j];
						let legendName = SPT.legendNameForDataCategory(dataSetBinds, dataSetAlias, categoryName);
						let mySeries = { name: legendName, data: categoryDatasMap[categoryName] };
						
						this._configSingleSeries(chart, mySeries);
						series.push(mySeries);
					}
				}
				else if(config.splitDataSet)
				{
					let mySeries = { name: dataSetAlias, data: data };
					
					this._configSingleSeries(chart, mySeries);
					series.push(mySeries);
				}
				else
				{
					if(series.length == 0)
					{
						let mySeries = { name: dataSetAlias, data: [], radius: "60%" };
						
						if(config.ring)
							mySeries.radius = ["35%", "55%"];
						
						if(config.rose)
							mySeries.roseType = "radius";
						
						this._configSingleSeries(chart, mySeries);
						series.push(mySeries);
					}
					
					series[0].data = series[0].data.concat(data);
				}
			}
			
			var options = { legend: {}, series: series };
			
			EU.inflateUpdateAxisData(chart, options, options.legend, EU.axisDataExtractors.propertyName());
			SPT.convertDataPropValueToName(options.legend);
			this._evalSeriesLayout(chart, options);
			options = EU.prepareUpdateOptions(chart, options);
			
			EU.setOptionsReplaceMerge(chart, options);
		},
		
		_configSingleSeries: function(chart, series)
		{
			series.type = "pie";
		},
		
		_evalSeriesLayout: function(chart, options)
		{
			if(!config.splitDataSet)
				return;
			
			var series = options.series;
			var len = series.length;
			
			if(!len)
				return;
			
			if(config.seriesLayout == "nest")
			{
				let radiusMax = 80;
				let radiusInner = 0;
				//系列数=1取60，否则取30
				let radiusOuter = (len == 1 ? 60 : 30);
				let radiusStep = parseInt((radiusMax - radiusOuter)/len);
				let radiusGap = parseInt(radiusStep*4/9);
				radiusStep = radiusStep - radiusGap;
				
				for(let i=0; i<len; i++)
				{
					series[i].radius = [ radiusInner+"%", radiusOuter+"%" ];
					
					//不是最外圈系列标签显示在内部
					if(i < (len - 1))
						series[i].label = { position: "inner" };
					
					radiusInner = radiusOuter + radiusGap;
					radiusOuter = radiusInner + radiusStep;
				}
			}
			else if(config.seriesLayout == "grid")
			{
				//TODO
			}
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//仪表盘

SPT.gaugeRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		//仪表盘类型："" 基本；"ring" 得分环；"step" 阶段
		gaugeType: ""
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		render: function(chart)
		{
			var options =
			{
				title: { text: chart.name() },
				tooltip: {},
				series:
				[
					{ type: "gauge", data: [] }
				]
			};
			
			if(config.gaugeType == "ring")
			{
				let itemBorderColor = chart.themeGradualColor(0.8);
				let axisLineWidth = this._evalAxisLineWidth(chart, 12);
				
				CF.extend(options.series[0],
				{
					startAngle: 90,
					endAngle: -270,
					pointer: { show: false },
					progress:
					{
						show: true, overlap: false,
						roundCap: true, clip: false,
						itemStyle:
						{
							borderWidth: 1,
							borderColor: itemBorderColor
						}
					},
					axisLine: { lineStyle: { width: axisLineWidth } },
					splitLine: { show: false },
					axisTick: { show: false },
					axisLabel: { show: false },
					detail: { borderColor: 'auto', borderRadius: 20, borderWidth: 1 }
				});
			}
			else if(config.gaugeType == "step")
			{
				let axisLineWidth = this._evalAxisLineWidth(chart, 20);
				
				CF.extend(options.series[0],
				{
					axisLine:
					{
						lineStyle:
						{
							width: axisLineWidth,
							color: [ [0.2, '#67e0e3'], [0.8, '#37a2da'], [1, '#fd666d'] ]
						}
					},
					pointer: { itemStyle: { color: 'auto' } },
					progress: { show: false },
					axisTick: { distance: (0-axisLineWidth), length: parseInt(axisLineWidth/3), },
					splitLine: { distance: (0-axisLineWidth), length: axisLineWidth },
					axisLabel: { color: 'auto', distance: axisLineWidth + parseInt(axisLineWidth/3) },
					detail: { valueAnimation: true, color: 'auto' }
				});
			}
			
			options = EU.prepareRenderOptions(chart, options);
			var instance = EU.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var seriesName = "";
			var seriesData = [];
			var min = null;
			var max = null;
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let dataSetAlias = chart.dataSetAlias(dataSetBind);
				let result = chart.resultOf(chartResult, dataSetBind);
				
				let minField = chart.dataSetFieldOfSign(dataSetBind, 1);
				if(minField)
				{
					let minValues = chart.resultColumnArrayDatas(result, minField);
					let myMin = SPT.findNonNull(minValues);
					min = (min == null ? myMin : Math.min(min, myMin));
				}
				
				let maxField = chart.dataSetFieldOfSign(dataSetBind, 2);
				if(maxField)
				{
					let maxValues = chart.resultColumnArrayDatas(result, maxField);
					let myMax = SPT.findNonNull(maxValues);
					max = (max == null ? myMax : Math.max(max, myMax));
				}
				
				let valueFields = chart.dataSetFieldsOfSign(dataSetBind, 0);
				let valuess = chart.resultRowArrayDatas(result, valueFields);
				let originalDatas = chart.resultDatas(result);
				
				for(let j=0; j<valuess.length; j++)
				{
					let values = valuess[j];
					let originalData = originalDatas[j];
					
					for(let k=0; k<values.length; k++)
					{
						let name = chart.dataSetFieldAlias(dataSetBind, valueFields[k]);
						let data = { name: name, value: values[k] };
						SPT.originalDataOfData(data, originalData);
						
						seriesData.push(data);
					}
				}
				
				if(CF.isEmpty(seriesName))
					seriesName = dataSetAlias;
			}
			
			if(config.gaugeType == "ring")
				this._evalDataTitlePosition(chart, seriesData, "center", null, 1);
			else
				this._evalDataTitlePosition(chart, seriesData, "top");
			
			min = (min == null ? 0 : min);
			max = (max == null ? 100 : max);
			
			var options = { series: [ { type: "gauge", name: seriesName, min: min, max: max, data: seriesData } ] };
			options = EU.prepareUpdateOptions(chart, options);
			
			EU.setOptionsReplaceMerge(chart, options);
		},
		
		_evalAxisLineWidth: function(chart, divide)
		{
			var chartEle = chart.element();
			var width = chartEle.clientWidth;
			var height = chartEle.clientHeight;
			return parseInt(Math.min(width, height)/divide);
		},
		
		_evalDataTitlePosition: function(chart, seriesData, positionType, topPposition, colCount, titleHeight, detailHeight)
		{
			positionType = (positionType == null ? "center" : positionType);
			topPposition = (topPposition == null ? 50 : topPposition);
			if(colCount == null)
			{
				var len = seriesData.length;
				if(len < 3)
					colCount = len;
				else if(len%3 == 0)
					colCount = 3;
				else if(len%2 == 0)
					colCount = 2;
				else
					colCount = 3;
			}
			titleHeight = (titleHeight == null? 14 : titleHeight);
			detailHeight = (detailHeight == null ? 15 : detailHeight);
			
			var rowHeight = titleHeight + detailHeight;
			var rowCount = Math.ceil(seriesData.length/colCount);
			var colCenterIdx = colCount/2;
			var rowCenterIdx = rowCount/2;
			var xGap = 100/colCount;
			
			for(var i=0; i<seriesData.length; i++)
			{
				var row = Math.floor(i/colCount);
				var col = i%colCount;
				
				var x = parseInt((col - colCenterIdx) * xGap + xGap/2);
				var yt = (positionType == "top" ? (topPposition + row*rowHeight) : ((row - rowCenterIdx)*rowHeight));
				var yd = yt + titleHeight;
				
				seriesData[i].title = { offsetCenter: [ x+'%', yt+"%" ] };
				seriesData[i].detail = { offsetCenter: [ x+'%', yd+"%" ] };
			}
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//散点图

SPT.scatterRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		scatterType: "scatter"
	},
	config);
	
	return SPT._scatterRenderer(plugin, config);
};

SPT.scatterRippleRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		scatterType: "effectScatter"
	},
	config);
	
	return SPT._scatterRenderer(plugin, config);
};

SPT._scatterRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		//散点图类型："scatter"、"effectScatter"
		scatterType: "scatter",
		//是否互换坐标轴
		interchangeAxis: false
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		render: function(chart)
		{
			var dataSetBind = chart.dataSetBindMain();
			var nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
			var valueField = chart.dataSetFieldOfSign(dataSetBind, 1);
			
			var options =
			{
				title: { text: chart.name() },
				tooltip: { trigger: "item" },
				legend: { data: [] },
				xAxis:
				{
					name: chart.dataSetFieldAlias(dataSetBind, nameField),
					type: SPT.evalDataSetFieldAxisType(chart, nameField),
					boundaryGap: !SPT.isDataTypeNumber(nameField)
				},
				yAxis:
				{
					name: chart.dataSetFieldAlias(dataSetBind, valueField),
					type: SPT.evalDataSetFieldAxisType(chart, valueField)
				},
				series:
				[
					{ type: config.scatterType, data: [] }
				]
			};
			
			if(config.interchangeAxis)
			{
				let xAxisTmp = options.xAxis;
				options.xAxis = options.yAxis;
				options.yAxis = xAxisTmp;
			}
			
			options = EU.prepareRenderOptions(chart, options);
			var instance = EU.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var legendData = [];
			var series = [];
			
			var dataRange = { min: null, max: null };
			var symbolSizeRatio = SPT.symbolSizeRadioIfEffectScatter(config.scatterType);
			var symbolSizeMax = SPT.evalSymbolSizeMax(chart, symbolSizeRatio);
			var symbolSizeMin = SPT.evalSymbolSizeMin(chart, symbolSizeMax);
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let dataSetAlias = chart.dataSetAlias(dataSetBind);
				let result = chart.resultOf(chartResult, dataSetBind);
				
				let nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
				let categoryField = chart.dataSetFieldOfSign(dataSetBind, 2);
				
				if(categoryField)
				{
					let valueField = chart.dataSetFieldOfSign(dataSetBind, 1);
					let categoryNames = [];
					let categoryDatasMap = {};
					
					//使用{name,value:[]}格式可以更好地兼容category、value、time坐标轴类型以及tooltip、事件数据
					let fieldMap = { name: nameField, value: [nameField, valueField] };
					fieldMap = SPT.addCategoryToFieldMap(fieldMap, categoryField);
					let data = chart.resultMapDatas(result, fieldMap);
					SPT.originalDataOfResult(data, chart, result);
					SPT.evalArrayDataRange(dataRange, data, "value", 1);
					SPT.splitDataByCategory(data, categoryNames, categoryDatasMap);
					
					for(let j=0; j<categoryNames.length; j++)
					{
						let categoryName = categoryNames[j];
						let legendName = SPT.legendNameForDataCategory(dataSetBinds, dataSetAlias, categoryName);
						let mySeries = { name: legendName, data: categoryDatasMap[categoryName] };
						
						this._configSingleSeries(chart, mySeries);
						legendData.push({ name: legendName });
						series.push(mySeries);
					}
				}
				else
				{
					let valueFields = chart.dataSetFieldsOfSign(dataSetBind, 1);
					
					for(let j=0; j<valueFields.length; j++)
					{
						let legendName = SPT.legendNameForDataValues(chart, dataSetBinds, dataSetBind, dataSetAlias, valueFields, j);
						//使用{name,value:[]}格式可以更好地兼容category、value、time坐标轴类型以及tooltip、事件数据
						let fieldMap = { name: nameField, value: [nameField, valueFields[j]] };
						let data = chart.resultMapDatas(result, fieldMap);
						SPT.originalDataOfResult(data, chart, result);
						SPT.evalArrayDataRange(dataRange, data, "value", 1);
						let mySeries = { name: legendName, data: data };
						
						this._configSingleSeries(chart, mySeries);
						legendData.push({ name: legendName });
						series.push(mySeries);
					}
				}
			}
			
			SPT.evalSeriesDataValueSymbolSize(series, dataRange.min, dataRange.max, symbolSizeMax, symbolSizeMin, "value", 1);
			
			var options = { legend: { data: legendData }, series: series };
			//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
			(config.interchangeAxis ? (options.yAxis = {}) : (options.xAxis = {}));
			
			EU.inflateUpdateAxisData(chart, options, (config.interchangeAxis ? options.yAxis : options.xAxis),
							EU.axisDataExtractors.valueElement0());
			options = EU.prepareUpdateOptions(chart, options, (options) => { EU.adaptValueArrayData(chart, options, config.scatterType); });
			
			EU.setOptionsReplaceMerge(chart, options);
		},
		
		_configSingleSeries: function(chart, series)
		{
			series.type = config.scatterType;
			series.encode = (config.interchangeAxis ? { x: 1, y: 0 } : { x: 0, y: 1 });
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//坐标散点图

SPT.scatterCoordRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		scatterType: "scatter"
	},
	config);
	
	return SPT._scatterCoordRenderer(plugin, config);
};

SPT.scatterCoordRippleRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		scatterType: "effectScatter"
	},
	config);
	
	return SPT._scatterCoordRenderer(plugin, config);
};

SPT._scatterCoordRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		//散点图类型："scatter"、"effectScatter"
		scatterType: "scatter",
		//是否互换坐标轴
		interchangeAxis: false
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		render: function(chart)
		{
			var dataSetBind = chart.dataSetBindMain();
			var nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
			var valueField = chart.dataSetFieldOfSign(dataSetBind, 1);
			
			var options =
			{
				title: { text: chart.name() },
				tooltip: { trigger: "item" },
				legend: { data: [] },
				xAxis:
				{
					name: chart.dataSetFieldAlias(dataSetBind, nameField),
					type: SPT.evalDataSetFieldAxisType(chart, nameField),
					boundaryGap: !SPT.isDataTypeNumber(nameField)
				},
				yAxis:
				{
					name: chart.dataSetFieldAlias(dataSetBind, valueField),
					type: SPT.evalDataSetFieldAxisType(chart, valueField)
				},
				series:
				[
					{ type: config.scatterType, data: [] }
				]
			};
			
			if(config.interchangeAxis)
			{
				let xAxisTmp = options.xAxis;
				options.xAxis = options.yAxis;
				options.yAxis = xAxisTmp;
			}
			
			options = EU.prepareRenderOptions(chart, options);
			var instance = EU.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var legendData = [];
			var series = [];
			
			var dataRange = { min: null, max: null };
			var symbolSizeRatio = SPT.symbolSizeRadioIfEffectScatter(config.scatterType);
			var symbolSizeMax = SPT.evalSymbolSizeMax(chart, symbolSizeRatio);
			var symbolSizeMin = SPT.evalSymbolSizeMin(chart, symbolSizeMax);
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let dataSetAlias = chart.dataSetAlias(dataSetBind);
				let result = chart.resultOf(chartResult, dataSetBind);
				
				let nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
				let valueField = chart.dataSetFieldOfSign(dataSetBind, 1);
				let weightField = chart.dataSetFieldOfSign(dataSetBind, 2);
				let categoryField = chart.dataSetFieldOfSign(dataSetBind, 3);
				let hasWeightField = (weightField != null);
				
				//使用{name,value:[]}格式可以更好地兼容category、value、time坐标轴类型以及tooltip、事件数据
				let fieldMap = { name: nameField, value: (hasWeightField ? [nameField, valueField, weightField] : [nameField, valueField]) };
				if(categoryField)
					fieldMap = SPT.addCategoryToFieldMap(fieldMap, categoryField);
				
				let data = chart.resultMapDatas(result, fieldMap);
				SPT.originalDataOfResult(data, chart, result);
				
				if(hasWeightField)
					SPT.evalArrayDataRange(dataRange, data, "value", 2);
				
				if(categoryField)
				{
					let categoryNames = [];
					let categoryDatasMap = {};
					
					SPT.splitDataByCategory(data, categoryNames, categoryDatasMap);
					
					for(let j=0; j<categoryNames.length; j++)
					{
						let categoryName = categoryNames[j];
						let legendName = SPT.legendNameForDataCategory(dataSetBinds, dataSetAlias, categoryName);
						let mySeries = { name: legendName, data: categoryDatasMap[categoryName] };
						
						this._configSingleSeries(chart, mySeries, hasWeightField);
						legendData.push({ name: legendName });
						series.push(mySeries);
					}
				}
				else
				{
					let mySeries = { name: dataSetAlias, data: data };
					
					this._configSingleSeries(chart, mySeries, hasWeightField);
					legendData.push({name: dataSetAlias});
					series.push(mySeries);
				}
			}
			
			SPT.evalSeriesDataValueSymbolSize(series, dataRange.min, dataRange.max, symbolSizeMax, symbolSizeMin, "value", 2);
			
			var options = { legend: { data: legendData }, series: series };
			//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
			(config.interchangeAxis ? (options.yAxis = {}) : (options.xAxis = {}));
			
			EU.inflateUpdateAxisData(chart, options, (config.interchangeAxis ? options.yAxis : options.xAxis),
							EU.axisDataExtractors.valueElement0());
			options = EU.prepareUpdateOptions(chart, options, (options) => { EU.adaptValueArrayData(chart, options, config.scatterType); });
			
			EU.setOptionsReplaceMerge(chart, options);
		},
		
		_configSingleSeries: function(chart, series, hasWeightField)
		{
			series.type = config.scatterType;
			series.encode = (config.interchangeAxis ? { x: 1, y: 0 } : { x: 0, y: 1 });
			
			if(hasWeightField)
				series.encode.tooltip = [1, 2];
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//雷达图

SPT.radarRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		//雷达图形状："polygon" 多边形；"circle" 圆形
		radarShape: "polygon",
		//默认indicator最大值
		defaultIndicatorMax: 100
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		render: function(chart)
		{
			var options =
			{
				title: { text: chart.name() },
				tooltip: { trigger: "item" },
				legend: { data: [] },
				radar:
				{
					center: ["50%", "60%"],
					radius: "70%",
					shape: config.radarShape,
					indicator: []
				},
				series:
				[
					{ type: "radar", data: [] }
				]
			};
			
			options = EU.prepareRenderOptions(chart, options);
			var instance = EU.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var renderOptions = SPT.liveDataRenderOptions(chart);
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			
			var legendData = [];
			var indicatorData = [];
			var seriesName = "";
			var seriesData = [];
			
			//临时series，series[i]表示一条雷达网，series[i].name是雷达网名称，
			//series[i].data[i].name是雷达指标名、series[i].data[i].value雷达指标值
			//这样可以使用已有的排序逻辑，从而支持sortAxisData特性
			var tmpSeries = [];
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let result = chart.resultOf(chartResult, dataSetBind);
				
				if(CF.isEmpty(seriesName))
					seriesName = chart.dataSetAlias(dataSetBind);
				
				let itemField = chart.dataSetFieldOfSign(dataSetBind, 0);
				
				//行式雷达网数据，必设置【雷达网条目名称】标记
				//一行数据表示一条雷达网，行式结构为：雷达网条目名称, [指标名, 指标值, 指标上限值]*n
				//或者
				//相同条目名的多行数据表示一条雷达网，行式结构为：雷达网条目名称, [指标名, 指标值, 指标上限值]*1
				if(itemField)
				{
					this._inflateTmpSeriesForRowMode(chart, chartResult, dataSetBind, result, indicatorData, tmpSeries);
				}
				//列式雷达网数据
				//一列【指标值】数据表示一条雷达网，列式结构为：指标名, 指标上限值, [指标值]*n，其中【指标值】列名将作为雷达网条目名称
				else
				{
					this._inflateTmpSeriesForColumnMode(chart, chartResult, dataSetBind, result, indicatorData, tmpSeries);
				}
			}
			
			indicatorData.forEach((indicator) =>
			{
				if(indicator.max == null)
					indicator.max = config.defaultIndicatorMax;
			});
			
			if(EU.sortAxisDataOption(renderOptions))
			{
				let tmpAxisData = [];
				indicatorData.forEach((indicator) =>
				{
					tmpAxisData.push(indicator.name);
				});
				
				let tmpOptions = { tmpAxis: { data: tmpAxisData }, series: tmpSeries };
				
				EU.sortUpdateAxisData(renderOptions, tmpOptions, tmpOptions.tmpAxis,
								true, true, EU.axisDataExtractors.propertyName());
				
				indicatorData.sort((a, b) =>
				{
					let ia = SPT.findInArray(tmpAxisData, a.name);
					let ib = SPT.findInArray(tmpAxisData, b.name);
					return (ia - ib);
				});
			}
			
			//将上述tmpSeries转换为雷达网数据
			for(let i=0; i<tmpSeries.length; i++)
			{
				let ts = tmpSeries[i];
				let radarData = { name: ts.name, value: [] };
				
				indicatorData.forEach((indicator) =>
				{
					let idx = SPT.findInArray(ts.data, indicator.name, "name");
					radarData.value.push(idx > -1 ? ts.data[idx].value : null);
				});
				
				SPT.originalDataOfData(radarData, SPT.originalDataOfData(ts));
				seriesData.push(radarData);
				legendData.push({ name: ts.name });
			}
			
			//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
			var series = [ { name: seriesName, type: "radar", data: seriesData } ];
			var options = { legend: { data: legendData }, radar: { indicator: indicatorData }, series: series };
			options = EU.prepareUpdateOptions(chart, options);
			
			EU.setOptionsReplaceMerge(chart, options);
		},
		
		//行式雷达网数据处理
		_inflateTmpSeriesForRowMode: function(chart, chartResult, dataSetBind, result, indicatorData, series)
		{
			var itemField = chart.dataSetFieldOfSign(dataSetBind, 0);
			var nameFields = chart.dataSetFieldsOfSign(dataSetBind, 1);
			var maxFields = chart.dataSetFieldsOfSign(dataSetBind, 3);
			
			var fields = chart.dataSetFields(dataSetBind, false);
			for(let i=0; i<nameFields.length; i++)
			{
				let nameField = nameFields[i];
				let maxField = null;
				
				if(maxFields.length > 0)
				{
					if(maxFields.length >= nameFields.length)
					{
						maxField = maxFields[i];
					}
					//查找至下一个名nameField之间的maxField
					else
					{
						let nameFieldIdx = fields.indexOf(nameField);
						let nextNameFieldIdx = ((i+1) >= nameFields.length ? fields.length : fields.indexOf(nameFields[i+1]));
						
						for(let j=0; j<maxFields.length; j++)
						{
							let myMaxFieldIdx = fields.indexOf(maxFields[j]);
							if(myMaxFieldIdx >= nameFieldIdx && myMaxFieldIdx < nextNameFieldIdx)
							{
								maxField = maxFields[j];
								break;
							}
						}
					}
				}
				
				let indicators = chart.resultMapDatas(result, (maxField == null ? { name: nameField } : { name: nameField, max: maxField }));
				
				indicators.forEach((indicator) =>
				{
					this._appendValidIndicator(indicatorData, indicator);
				});
			}
			
			if(nameFields.length == 0){}
			//多行式雷达网
			else if(nameFields.length == 1)
			{
				let valueField = chart.dataSetFieldOfSign(dataSetBind, 2);
				let categoryNames = [];
				let categoryDatasMap = {};
				
				let fieldMap = SPT.addCategoryToFieldMap({ name: nameFields, value: valueField }, itemField);
				let data = chart.resultMapDatas(result, fieldMap);
				SPT.originalDataOfResult(data, chart, result);
				SPT.splitDataByCategory(data, categoryNames, categoryDatasMap);
				
				for(let j=0; j<categoryNames.length; j++)
				{
					let categoryName = categoryNames[j];
					let categoryDatas = categoryDatasMap[categoryName];
					let mySeries = { name: categoryName, data: categoryDatas };
					let originalData = [];
					
					categoryDatas.forEach((cd) =>
					{
						let myOriginalData = SPT.originalDataOfData(cd);
						originalData.push(myOriginalData);
					});
					
					SPT.originalDataOfData(mySeries, originalData);
					
					series.push(mySeries);
				}
			}
			//单行式雷达网
			else
			{
				let iv = chart.resultColumnArrayDatas(result, itemField);
				let nv = chart.resultRowArrayDatas(result, nameFields);
				let valueFields = chart.dataSetFieldsOfSign(dataSetBind, 2);
				let vv = chart.resultRowArrayDatas(result, valueFields);
				let dataLen = Math.min(nameFields.length, valueFields.length);
				
				for(let i=0; i<iv.length; i++)
				{
					let mySeries = { name: iv[i], data: [] };
					
					for(let j=0; j<dataLen; j++)
					{
						mySeries.data.push({ name: nv[i][j], value: vv[i][j] });
					}
					
					series.push(mySeries);
				}
				
				SPT.originalDataOfResult(series, chart, result);
			}
		},
		
		_inflateTmpSeriesForColumnMode: function(chart, chartResult, dataSetBind, result, indicatorData, series)
		{
			var nameField = chart.dataSetFieldOfSign(dataSetBind, 1);
			var nv = chart.resultColumnArrayDatas(result, nameField);
			var maxField = chart.dataSetFieldOfSign(dataSetBind, 3);
			var mv = (maxField == null ? null : chart.resultColumnArrayDatas(result, maxField));
			
			for(let i=0; i<nv.length; i++)
			{
				let indicator = { name: nv[i], max: (mv == null ? null : mv[i]) };
				this._appendValidIndicator(indicatorData, indicator);
			}
			
			var valueFields = chart.dataSetFieldsOfSign(dataSetBind, 2);
			var vv = chart.resultColumnArrayDatas(result, valueFields);
			
			for(let i=0; i<valueFields.length; i++)
			{
				let name = chart.dataSetFieldAlias(dataSetBind, valueFields[i]);
				let mySeries = { name: name, data: [] };
				
				for(let j=0; j<nv.length; j++)
				{
					mySeries.data.push({ name: nv[j], value: vv[i][j] });
				}
				
				SPT.originalDataOfData(mySeries, chart.resultDatas(result));
				
				series.push(mySeries);
			}
		},
		
		_appendValidIndicator: function(indicatorData, indicator)
		{
			if(indicator && indicator.name != null)
			{
				var idx = SPT.findInArray(indicatorData, indicator.name, "name");
				
				if(idx < 0)
				{
					indicatorData.push(indicator);
				}
				else if(indicatorData[idx].max == null || indicatorData[idx].max < indicator.max)
				{
					indicatorData[idx] = indicator;
				}
			}
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//漏斗图

SPT.funnelRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		//同series[i].sort
		sort: "descending"
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		render: function(chart)
		{
			var options =
			{
				title: { text: chart.name() },
				tooltip: { trigger: "item" },
				legend: { data: [] },
				series:
				[
					{ type: "funnel", data: [] }
				]
			};
			
			options = EU.prepareRenderOptions(chart, options);
			var instance = EU.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var legendData = [];
			var seriesName = "";
			var seriesData = [];
			var dataRange = { min: 0, max: 100 };
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let dataSetAlias = chart.dataSetAlias(dataSetBind);
				let result = chart.resultOf(chartResult, dataSetBind);
				
				let nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
				let valueField = chart.dataSetFieldOfSign(dataSetBind, 1);
				
				let data = chart.resultNameValueDatas(result, nameField, valueField);
				SPT.originalDataOfResult(data, chart, result);
				
				let names = chart.resultColumnArrayDatas(result, nameField);
				
				for(let j=0; j<names.length; j++)
				{
					legendData.push({ name: names[j] });
				}
				
				if(CF.isEmpty(seriesName))
					seriesName = dataSetAlias;
				
				seriesData = seriesData.concat(data);
			}
			
			SPT.evalArrayDataRange(dataRange, seriesData, "value");
			
			var series = [ {type: "funnel", name: seriesName, min: dataRange.min, max: dataRange.max, data: seriesData, sort: config.sort } ];
			var options = { legend: { data: legendData }, series: series };
			options = EU.prepareUpdateOptions(chart, options);
			
			EU.setOptionsReplaceMerge(chart, options);
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//地图

SPT.mapRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		asyncRender: true,
		asyncUpdate: true,
		
		render: function(chart)
		{
			var options =
			{
				title: { text: chart.name() },
				tooltip: { trigger: "item" },
				visualMap:
				{
					text: ["高", "低"],
					realtime: true,
					calculable: true,
					min: 0,
					max: 100
				},
				series:
				[
					{ type: "map", data: [] }
				]
			};
			
			options = EU.prepareRenderOptions(chart, options,
				(chart, renderOptions) =>
				{
					EU.initChartMap(chart, renderOptions);
				});
			
			EU.renderMapChart(chart, options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var mapSignIndex = 2;
			
			var seriesName = "";
			var seriesData = [];
			var dataRange = { min: null, max: null };
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let dataSetAlias = chart.dataSetAlias(dataSetBind);
				let result = chart.resultOf(chartResult, dataSetBind);
				
				let nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
				let valueField = chart.dataSetFieldOfSign(dataSetBind, 1);
				
				let data = chart.resultNameValueDatas(result, nameField, valueField);
				SPT.originalDataOfResult(data, chart, result);
				SPT.evalArrayDataRange(dataRange, data, "value");
				
				if(CF.isEmpty(seriesName))
					seriesName = dataSetAlias;
				
				seriesData = seriesData.concat(data);
			}
			
			var visualMap = { min: dataRange.min, max: dataRange.max };
			SPT.trimNumberRange(visualMap);
			
			var series = [ { type: "map", name: seriesName, data: seriesData } ];
			
			//从全部结果中取，以支持在附件数据集中定义地图名
			var map = SPT.chartResultFirstNonEmptyOfSign(chart, chartResult, mapSignIndex);
			if(!CF.isEmpty(map))
				series[0].map = map;
			
			var options = { visualMap: visualMap, series: series };
			options = EU.prepareUpdateOptions(chart, options);
			EU.updateMapChart(chart, options);
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//地图散点图

SPT.mapScatterRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		scatterType: "scatter"
	},
	config);
	
	return SPT._mapScatterRenderer(plugin, config);
};

SPT.mapScatterRippleRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		scatterType: "effectScatter"
	},
	config);
	
	return SPT._mapScatterRenderer(plugin, config);
};

SPT._mapScatterRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		//散点图类型："scatter"、"effectScatter"
		scatterType: "scatter",
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		asyncRender: true,
		asyncUpdate: true,
		
		render: function(chart)
		{
			var options =
			{
				title: { text: chart.name() },
				tooltip:
				{
					trigger: "item",
					//ECharts-6.0设置“series.encode = { tooltip: (hasValueField ? [2] : []) }”时，
					//无值时仍会显示经度，所以这里自定义了formatter选项
					formatter: function(params)
					{
						return EU.customTooltip(params, (pi) =>
						{
							let re = {};
							
							if(pi.value)
								re.value = (pi.value.length >= 3 ? pi.value[2] : "");
							
							return re;
						});
					}
				},
				legend: { data: [] },
				geo: { roam: true },
				series:
				[
					{ type: config.scatterType, coordinateSystem: "geo", data: [] }
				]
			};
			
			options = EU.prepareRenderOptions(chart, options,
				(chart, renderOptions) =>
				{
					EU.initChartMap(chart, renderOptions);
				});
			
			EU.renderMapChart(chart, options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var mapSignIndex = 5;
			
			var legendData = [];
			var series = [];
			
			var dataRange = { min: null, max: null };
			var symbolSizeRatio = SPT.symbolSizeRadioIfEffectScatter(config.scatterType);
			var symbolSizeMax = SPT.evalSymbolSizeMax(chart, symbolSizeRatio);
			var symbolSizeMin = SPT.evalSymbolSizeMin(chart, symbolSizeMax);
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let dataSetAlias = chart.dataSetAlias(dataSetBind);
				let result = chart.resultOf(chartResult, dataSetBind);
				
				let nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
				let loField = chart.dataSetFieldOfSign(dataSetBind, 1);
				let laField = chart.dataSetFieldOfSign(dataSetBind, 2);
				let valueField = chart.dataSetFieldOfSign(dataSetBind, 3);
				let categoryField = chart.dataSetFieldOfSign(dataSetBind, 4);
				let hasValueField = (valueField != null);
				
				let fieldMap = { name: nameField, value: (hasValueField ? [loField, laField, valueField] : [loField, laField]) };
				
				if(categoryField)
					fieldMap = SPT.addCategoryToFieldMap(fieldMap, categoryField);
				
				let data = chart.resultMapDatas(result, fieldMap);
				SPT.originalDataOfResult(data, chart, result);
				
				if(hasValueField)
					SPT.evalArrayDataRange(dataRange, data, "value", 2);
				
				if(categoryField)
				{
					let categoryNames = [];
					let categoryDatasMap = {};
					
					SPT.splitDataByCategory(data, categoryNames, categoryDatasMap);
					
					for(let j=0; j<categoryNames.length; j++)
					{
						let categoryName = categoryNames[j];
						let legendName = SPT.legendNameForDataCategory(dataSetBinds, dataSetAlias, categoryName);
						let mySeries = { name: legendName, data: categoryDatasMap[categoryName] };
						
						this._configSingleSeries(chart, mySeries, hasValueField);
						legendData.push({ name: legendName });
						series.push(mySeries);
					}
				}
				else
				{
					let mySeries = { name: dataSetAlias, data: data };
					
					this._configSingleSeries(chart, mySeries, hasValueField);
					legendData.push({ name: dataSetAlias });
					series.push(mySeries);
				}
			}
			
			SPT.evalSeriesDataValueSymbolSize(series, dataRange.min, dataRange.max, symbolSizeMax, symbolSizeMin, "value", 2);
			
			var options = { legend: { data: legendData }, series: series };
			
			//从全部结果中取，以支持在附件数据集中定义地图名
			var map = SPT.chartResultFirstNonEmptyOfSign(chart, chartResult, mapSignIndex);
			if(!CF.isEmpty(map))
				options.geo = { map: map };
			
			options = EU.prepareUpdateOptions(chart, options);
			EU.updateMapChart(chart, options);
		},
		
		_configSingleSeries: function(chart, series, hasValueField)
		{
			series.type = config.scatterType;
			series.coordinateSystem = "geo";
			//series.encode = { tooltip: (hasValueField ? [2] : []) };
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//地图关系图

SPT.mapGraphRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		asyncRender: true,
		asyncUpdate: true,
		
		render: function(chart)
		{
			var options =
			{
				title: { text: chart.name() },
				tooltip:
				{
					trigger: "item",
					//ECharts-6.0默认tooltip显示有缺陷，所以这里自定义了formatter选项
					formatter: function(params)
					{
						return EU.customTooltip(params, (pi) =>
						{
							let re = {};
							
							if(pi.dataType == "edge" && pi.data)
							{
								re.name = pi.data[SPT.ORIGINAL_SOURCE_NAME_PROP_NAME] + " > " + pi.data[SPT.ORIGINAL_TARGET_NAME_PROP_NAME];
							}
							else if(pi.dataType == "node" && pi.value)
							{
								re.value = (pi.value.length >= 3 ? pi.value[2] : "");
							}
							
							return re;
						});
					}
				},
				legend: { data: [] },
				geo: { roam: true },
				series:
				[
					{ type: "graph", coordinateSystem: "geo", layout: "none", data: [] }
				]
			};
			
			options = EU.prepareRenderOptions(chart, options,
				(chart, renderOptions) =>
				{
					EU.initChartMap(chart, renderOptions);
				});
			
			EU.renderMapChart(chart, options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var mapSignIndex = 3;
			
			var options =
			{
				legend: { data: [] },
				series: [{ name: "", categories: [], data: [], links: [] }]
			};
			
			var dataRange = { min: null, max: null };
			var symbolSizeMax = SPT.evalSymbolSizeMax(chart);
			var symbolSizeMin = SPT.evalSymbolSizeMin(chart, symbolSizeMax);
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				
				//节点数据集
				if(chart.isDataSetSigned(dataSetBind, 0))
				{
					this._inflateOptionsForNode(chart, chartResult, dataSetBinds, i, options, dataRange);
				}
				//合并数据集
				else if(chart.isDataSetSigned(dataSetBind, 2))
				{
					this._inflateOptionsForJoin(chart, chartResult, dataSetBinds, i, options, dataRange);
				}
				
				this._inflateCommonOptions(chart, chartResult, dataSetBinds, i, options);
			}
			
			//应在所有节点填充完成后再处理关系
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				
				//关系数据集
				if(chart.isDataSetSigned(dataSetBind, 1))
				{
					this._inflateOptionsForLink(chart, chartResult, dataSetBinds, i, options);
				}
				
				this._inflateCommonOptions(chart, chartResult, dataSetBinds, i, options);
			}
			
			this._configSingleSeries(chart, options.series[0]);
			SPT.evalSeriesDataValueSymbolSize(options.series, dataRange.min, dataRange.max, symbolSizeMax, symbolSizeMin, "value", 2);
			
			//从全部结果中取，以支持在附件数据集中定义地图名
			var map = SPT.chartResultFirstNonEmptyOfSign(chart, chartResult, mapSignIndex);
			if(!CF.isEmpty(map))
				options.geo = { map: map };
			
			options = EU.prepareUpdateOptions(chart, options);
			EU.updateMapChart(chart, options);
		},
		
		_inflateCommonOptions: function(chart, chartResult, dataSetBinds, dsbIndex, options)
		{
			var dataSetBind = dataSetBinds[dsbIndex];
			
			if(CF.isEmpty(options.series[0].name))
				options.series[0].name = chart.dataSetAlias(dataSetBind);
		},
		
		_inflateOptionsForNode: function(chart, chartResult, dataSetBinds, dsbIndex, options, dataRange)
		{
			var legendData = options.legend.data;
			var seriesCategories = options.series[0].categories;
			var seriesData = options.series[0].data;
			
			var dataSetBind = dataSetBinds[dsbIndex];
			var dataSetAlias = chart.dataSetAlias(dataSetBind);
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var idField = chart.dataSetFieldOfSign(dataSetBind, [0, 0]);
			var nameField = chart.dataSetFieldOfSign(dataSetBind, [0, 1]);
			var loField = chart.dataSetFieldOfSign(dataSetBind, [0, 2]);
			var laField = chart.dataSetFieldOfSign(dataSetBind, [0, 3]);
			var valueField = chart.dataSetFieldOfSign(dataSetBind, [0, 4]);
			var categoryField = chart.dataSetFieldOfSign(dataSetBind, [0, 5]);
			
			var data = chart.resultDatas(result);
			
			for(let i=0; i<data.length; i++)
			{
				let dataj = data[i];
				let node =
				{
					name: chart.resultDataRowCell(dataj, nameField),
					value: [ chart.resultDataRowCell(dataj, loField), chart.resultDataRowCell(dataj, laField) ]
				};
				
				node[SPT.ORIGINAL_ID_PROP_NAME] = chart.resultDataRowCell(dataj, idField);
				SPT.originalDataOfData(node, dataj);
				
				if(valueField)
				{
					let v = chart.resultDataRowCell(dataj, valueField);
					node.value.push(v);
					
					dataRange.min = (dataRange.min == null ? v : Math.min(dataRange.min, v));
					dataRange.max = (dataRange.max == null ? v : Math.max(dataRange.max, v));
				}
				
				let category = (categoryField ? chart.resultDataRowCell(dataj, categoryField) : dataSetAlias);
				node[SPT.ORIGINAL_CATEGORY_PROP_NAME] = category;
				node.category = SPT.appendDistinct(seriesCategories, {name: category}, "name");
				SPT.appendDistinct(legendData, {name: category}, "name");
				
				this._appendNode(seriesData, node);
			}
		},
		
		_inflateOptionsForLink: function(chart, chartResult, dataSetBinds, dsbIndex, options)
		{
			var seriesData = options.series[0].data;
			var seriesLinks = options.series[0].links;
			
			var dataSetBind = dataSetBinds[dsbIndex];
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var sourceField = chart.dataSetFieldOfSign(dataSetBind, [1, 0]);
			var targetField = chart.dataSetFieldOfSign(dataSetBind, [1, 1]);
			var fieldMap = { source: sourceField, target: targetField };
			
			var data = chart.resultMapDatas(result, fieldMap);
			
			for(let i=0; i<data.length; i++)
			{
				let di = data[i];
				let srcIdx = SPT.findInArray(seriesData, di.source, SPT.ORIGINAL_ID_PROP_NAME);
				let srcNode = (srcIdx >= 0 ? seriesData[srcIdx] : null);
				let tgtIdx = SPT.findInArray(seriesData, di.target, SPT.ORIGINAL_ID_PROP_NAME);
				let tgtNode = (tgtIdx >= 0 ? seriesData[tgtIdx] : null);
				
				if(srcNode != null && tgtNode != null)
				{
					//如果使用id值作为关系标识，对于数值型id，echarts会误当做数据索引；
					//如果使用名称作为关系标识，在连接线上的tooltip只会显示索引数值不友好，所有这里使用索引号同时自定义tooltip
					let link = { source: srcIdx, target: tgtIdx };
					this._inflateLinkOriginalInfo(link, srcNode, tgtNode);
					SPT.originalDataOfData(link, di);
					
					seriesLinks.push(link);
				}
			}
		},
		
		_inflateOptionsForJoin: function(chart, chartResult, dataSetBinds, dsbIndex, options, dataRange)
		{
			var legendData = options.legend.data;
			var seriesCategories = options.series[0].categories;
			var seriesData = options.series[0].data;
			var seriesLinks = options.series[0].links;
			
			var dataSetBind = dataSetBinds[dsbIndex];
			var dataSetAlias = chart.dataSetAlias(dataSetBind);
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var srcIdField = chart.dataSetFieldOfSign(dataSetBind, [2, 0]);
			var srcNameField = chart.dataSetFieldOfSign(dataSetBind, [2, 1]);
			var srcLoField = chart.dataSetFieldOfSign(dataSetBind, [2, 2]);
			var srcLaField = chart.dataSetFieldOfSign(dataSetBind, [2, 3]);
			var srcValueField = chart.dataSetFieldOfSign(dataSetBind, [2, 4]);
			var srcCategoryField = chart.dataSetFieldOfSign(dataSetBind, [2, 5]);
			var tgtIdField = chart.dataSetFieldOfSign(dataSetBind, [2, 6]);
			var tgtNameField = chart.dataSetFieldOfSign(dataSetBind, [2, 7]);
			var tgtLoField = chart.dataSetFieldOfSign(dataSetBind, [2, 8]);
			var tgtLaField = chart.dataSetFieldOfSign(dataSetBind, [2, 9]);
			var tgtValueField = chart.dataSetFieldOfSign(dataSetBind, [2, 10]);
			var tgtCategoryField = chart.dataSetFieldOfSign(dataSetBind, [2, 11]);
			
			var data = chart.resultDatas(result);
			
			for(let i=0; i<data.length; i++)
			{
				let dataj = data[i];
				let sd =
				{
					name: chart.resultDataRowCell(dataj, srcNameField),
					value: [ chart.resultDataRowCell(dataj, srcLoField), chart.resultDataRowCell(dataj, srcLaField) ]
				};
				let td =
				{
					name: chart.resultDataRowCell(dataj, tgtNameField),
					value: [ chart.resultDataRowCell(dataj, tgtLoField), chart.resultDataRowCell(dataj, tgtLaField) ]
				};
				
				sd[SPT.ORIGINAL_ID_PROP_NAME] = chart.resultDataRowCell(dataj, srcIdField);
				td[SPT.ORIGINAL_ID_PROP_NAME] = chart.resultDataRowCell(dataj, tgtIdField);
				
				SPT.originalDataOfData(sd, dataj);
				SPT.originalDataOfData(td, dataj);
				
				if(srcValueField)
				{
					let sv = chart.resultDataRowCell(dataj, srcValueField);
					sd.value.push(sv);
					
					dataRange.min = (dataRange.min == null ? sv : Math.min(dataRange.min, sv));
					dataRange.max = (dataRange.max == null ? sv : Math.max(dataRange.max, sv));
				}
				
				let srcCategory = (srcCategoryField ? chart.resultDataRowCell(dataj, srcCategoryField) : dataSetAlias);
				sd[SPT.ORIGINAL_CATEGORY_PROP_NAME] = srcCategory;
				sd.category = SPT.appendDistinct(seriesCategories, {name: srcCategory}, "name");
				SPT.appendDistinct(legendData, {name: srcCategory}, "name");
				
				if(tgtValueField)
				{
					let tv = chart.resultDataRowCell(dataj, tgtValueField);
					td.value.push(tv);
					
					dataRange.min = (dataRange.min == null ? tv : Math.min(dataRange.min, tv));
					dataRange.max = (dataRange.max == null ? tv : Math.max(dataRange.max, tv));
				}
				
				let tgtCategory = (tgtCategoryField ? chart.resultDataRowCell(dataj, tgtCategoryField) : dataSetAlias);
				td[SPT.ORIGINAL_CATEGORY_PROP_NAME] = tgtCategory;
				td.category = SPT.appendDistinct(seriesCategories, {name: tgtCategory}, "name");
				SPT.appendDistinct(legendData, {name: tgtCategory}, "name");
				
				let srcIdx = this._appendNode(seriesData, sd);
				let tgtIdx = this._appendNode(seriesData, td);
				
				//如果使用id值作为关系标识，对于数值型id，echarts会误当做数据索引；
				//如果使用名称作为关系标识，在连接线上的tooltip只会显示索引数值不友好，所有这里使用索引号同时自定义tooltip
				let link = { source: srcIdx, target: tgtIdx };
				this._inflateLinkOriginalInfo(link, sd, td);
				SPT.originalDataOfData(link, dataj);
				seriesLinks.push(link);
			}
		},
		
		_inflateLinkOriginalInfo: function(link, sourceNode, targetNode)
		{
			link[SPT.ORIGINAL_SOURCE_ID_PROP_NAME] = sourceNode[SPT.ORIGINAL_ID_PROP_NAME];
			link[SPT.ORIGINAL_SOURCE_NAME_PROP_NAME] = sourceNode.name;
			link[SPT.ORIGINAL_TARGET_ID_PROP_NAME] = targetNode[SPT.ORIGINAL_ID_PROP_NAME];
			link[SPT.ORIGINAL_TARGET_NAME_PROP_NAME] = targetNode.name;
		},
		
		_appendNode: function(seriesData, node)
		{
			return SPT.appendDistinct(seriesData, node, SPT.ORIGINAL_ID_PROP_NAME);
		},
		
		_configSingleSeries: function(chart, series)
		{
			series.type = "graph";
			series.coordinateSystem = "geo";
			
			//ECharts-6.0中这样设置仍不会显示第三个数值，所以上面自定义了tooltip.formatter选项
			//series.encode = { tooltip: (hasValueField ? [0, 1, 2] : [0, 1]) };
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//地图路径图

SPT.mapLinesRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		asyncRender: true,
		asyncUpdate: true,
		
		render: function(chart)
		{
			var options =
			{
				title: { text: chart.name() },
				legend: { data: [] },
				geo: { roam: true },
				series:
				[
					{ type: "lines", data: [], coordinateSystem: "geo", polyline: true }
				]
			};
			
			options = EU.prepareRenderOptions(chart, options,
				(chart, renderOptions) =>
				{
					EU.initChartMap(chart, renderOptions);
				});
			
			EU.renderMapChart(chart, options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var mapSignIndex = 3;
			
			var legendData = [];
			var series = [];
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let dataSetAlias = chart.dataSetAlias(dataSetBind);
				let result = chart.resultOf(chartResult, dataSetBind);
				
				let nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
				let loField = chart.dataSetFieldOfSign(dataSetBind, 1);
				let laField = chart.dataSetFieldOfSign(dataSetBind, 2);
				
				let data = null;
				
				//同名称的是一条路径
				if(nameField)
				{
					data = chart.resultNameValueDatas(result, nameField, [loField, laField]);
					let originalDatas = chart.resultDatas(result);
					let names = [];
					let coordsInfos = {};
					
					for(let j=0; j<data.length; j++)
					{
						let dj = data[j];
						let name = dj.name;
						let coordsInfo = coordsInfos[name];
						
						if(!coordsInfo)
						{
							names.push(name);
							coordsInfo = { coords: [], originalData: [] };
							coordsInfos[name] = coordsInfo;
						}
						
						coordsInfo.coords.push(dj.value);
						coordsInfo.originalData.push(originalDatas[j]);
					}
					
					data = [];
					
					for(let j=0; j<names.length; j++)
					{
						let name = names[j];
						data[j] = { name: name, coords: coordsInfos[name].coords };
						SPT.originalDataOfData(data[j], coordsInfos[name].originalData);
					}
				}
				//整个数据集是一条路径
				else
				{
					data = chart.resultRowArrayDatas(result, [loField, laField]);
					data = [ { name: dataSetAlias, coords: data } ];
					SPT.originalDataOfData(data[0], chart.resultDatas(result));
				}
				
				legendData.push({name: dataSetAlias});
				series.push({ name: dataSetAlias, data: data, type: "lines", coordinateSystem: "geo", polyline: true });
			}
			
			var options = { legend: {data: legendData}, series: series };
			
			//从全部结果中取，以支持在附件数据集中定义地图名
			var map = SPT.chartResultFirstNonEmptyOfSign(chart, chartResult, mapSignIndex);
			if(!CF.isEmpty(map))
				options.geo = { map: map };
			
			options = EU.prepareUpdateOptions(chart, options);
			EU.updateMapChart(chart, options);
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//地图飞线图

SPT.mapFlylineRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		asyncRender: true,
		asyncUpdate: true,
		
		render: function(chart)
		{
			var options =
			{
				title: { text: chart.name() },
				tooltip:
				{
					trigger: "item",
					//ECharts-6.0中默认提示信息太简单，所以这里自定义了formatter选项
					formatter: function(params)
					{
						return EU.customTooltip(params);
					}
				},
				legend: { data: [] },
				geo: { roam: true },
				series:
				[
					{ type: "lines", data: [], coordinateSystem: "geo", polyline: false }
				]
			};
			
			options = EU.prepareRenderOptions(chart, options,
				(chart, renderOptions) =>
				{
					EU.initChartMap(chart, renderOptions);
				});
			
			EU.renderMapChart(chart, options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var mapSignIndex = 6;
			
			var legendData = [];
			var categoryNames = [];
			var categoryDatasMap = {};
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let dataSetAlias = chart.dataSetAlias(dataSetBind);
				let result = chart.resultOf(chartResult, dataSetBind);
				
				var nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
				var coordFields = [
							chart.dataSetFieldOfSign(dataSetBind, 1),
							chart.dataSetFieldOfSign(dataSetBind, 2),
							chart.dataSetFieldOfSign(dataSetBind, 3),
							chart.dataSetFieldOfSign(dataSetBind, 4),
						];
				var categoryField = chart.dataSetFieldOfSign(dataSetBind, 5);
				
				var fieldMap = { "name": nameField, "coords": coordFields };
				
				if(categoryField)
					fieldMap = SPT.addCategoryToFieldMap(fieldMap, categoryField);
				
				let data = chart.resultMapDatas(result, fieldMap);
				
				for(let j=0; j<data.length; j++)
				{
					let coords = data[j].coords;
					data[j].coords = [[coords[0], coords[1]], [coords[2], coords[3]]];
				}
				
				SPT.originalDataOfResult(data, chart, result);
				
				if(categoryField)
					SPT.splitDataByCategory(data, categoryNames, categoryDatasMap);
				else
					SPT.appendCategoryNameAndData(categoryNames, categoryDatasMap, dataSetAlias, data);
			}
			
			var series = [];
			
			for(let i=0; i<categoryNames.length; i++)
			{
				let categoryName = categoryNames[i];
				legendData.push({ name: categoryName });
				series[i] =
				{
					name: categoryName,
					data: categoryDatasMap[categoryName],
					type: "lines",
					coordinateSystem: "geo",
					polyline: false,
					effect:
					{
						show: true,
						symbol: "arrow",
						symbolSize: 8,
						trailLength: 0
					},
					lineStyle:
					{
						curveness: 0.2
					}
				};
			}
			
			var options = { legend: {data: legendData}, series: series };
			
			//从全部结果中取，以支持在附件数据集中定义地图名
			var map = SPT.chartResultFirstNonEmptyOfSign(chart, chartResult, mapSignIndex);
			if(!CF.isEmpty(map))
				options.geo = { map: map };
			
			options = EU.prepareUpdateOptions(chart, options);
			EU.updateMapChart(chart, options);
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//地图热力图

SPT.mapHeatmapRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		asyncRender: true,
		asyncUpdate: true,
		
		render: function(chart)
		{
			var options =
			{
				title: { text: chart.name() },
				geo: { roam: true },
				visualMap: { show: false },
				series:
				[
					{ type: "heatmap", data: [], coordinateSystem: "geo", pointSize: 5, blurSize: 6 }
				]
			};
			
			options = EU.prepareRenderOptions(chart, options,
				(chart, renderOptions) =>
				{
					EU.initChartMap(chart, renderOptions);
				});
			
			EU.renderMapChart(chart, options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var mapSignIndex = 4;
			
			var seriesName = "";
			var seriesData = [];
			var dataRange = { min: null, max: null };
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let dataSetAlias = chart.dataSetAlias(dataSetBind);
				let result = chart.resultOf(chartResult, dataSetBind);
				
				let nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
				let longitudeField = chart.dataSetFieldOfSign(dataSetBind, 1);
				let latitudeField = chart.dataSetFieldOfSign(dataSetBind, 2);
				let valueField = chart.dataSetFieldOfSign(dataSetBind, 3);
				
				let data = chart.resultNameValueDatas(result, nameField, [ longitudeField, latitudeField, valueField ]);
				SPT.originalDataOfResult(data, chart, result);
				
				SPT.evalArrayDataRange(dataRange, data, "value", 2);
				
				seriesData = seriesData.concat(data);
				
				if(CF.isEmpty(seriesName))
					seriesName = dataSetAlias;
			}
			
			var pointSize = this._evalPointSize(chart);
			var blurSize = parseInt(pointSize*1.2);
			
			var series =
			[{
				name: seriesName,
				data: seriesData,
				type: "heatmap",
				coordinateSystem: "geo",
				pointSize: pointSize,
				blurSize: blurSize
			}];
			
			var options = { visualMap: { min: dataRange.min, max: dataRange.max }, series: series };
			SPT.trimNumberRange(options.visualMap);
			
			//从全部结果中取，以支持在附件数据集中定义地图名
			var map = SPT.chartResultFirstNonEmptyOfSign(chart, chartResult, mapSignIndex);
			if(!CF.isEmpty(map))
				options.geo = { map: map };
			
			options = EU.prepareUpdateOptions(chart, options);
			EU.updateMapChart(chart, options);
		},
		
		_evalPointSize: function(chart)
		{
			//根据图表元素尺寸自动计算
			var chartEle = chart.element();
			var pointSize = parseInt(Math.min(chartEle.clientWidth, chartEle.clientHeight)/60);
			
			if(pointSize < 1)
				pointSize = 1;
				
			return pointSize;
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//K线图

SPT.candlestickRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		render: function(chart)
		{
			var dataSetBind = chart.dataSetBindMain();
			var nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
			
			var options =
			{
				title: { text: chart.name() },
				tooltip: { trigger: "item" },
				xAxis:
				{
					name: chart.dataSetFieldAlias(dataSetBind, nameField),
					nameGap: 5,
					type: SPT.evalDataSetFieldAxisType(chart, nameField),
					boundaryGap: true,
					splitLine: { show:false },
					data: []
				},
				yAxis:
				{
					name: "", nameGap: 5, type: "value"
				},
				series:
				[
					{ type: "candlestick", data: [] }
				]
			};
			
			//非类目轴需要设置，不然图形会贴边
			if(options.xAxis.type != "category")
				options.xAxis.boundaryGap = [ "12%", "12%" ];
			
			options = EU.prepareRenderOptions(chart, options);
			var instance = EU.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			
			var series = [];
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let dataSetAlias = chart.dataSetAlias(dataSetBind);
				let result = chart.resultOf(chartResult, dataSetBind);
				
				let nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
				let valueFields =
				[
					nameField,
					chart.dataSetFieldOfSign(dataSetBind, 1),
					chart.dataSetFieldOfSign(dataSetBind, 2),
					chart.dataSetFieldOfSign(dataSetBind, 3),
					chart.dataSetFieldOfSign(dataSetBind, 4)
				];
				
				let data = chart.resultNameValueDatas(result, nameField, valueFields);
				SPT.originalDataOfResult(data, chart, result);
				
				let mySeries = {name: dataSetAlias, data: data };
				this._configSingleSeries(chart, mySeries);
				series.push(mySeries);
			}
			
			//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
			var options = { series: series, xAxis: {} };
			
			EU.inflateUpdateAxisData(chart, options, options.xAxis,
							EU.axisDataExtractors.propertyName());
			
			options = EU.prepareUpdateOptions(chart, options);
			
			EU.setOptionsReplaceMerge(chart, options);
		},
		
		_configSingleSeries: function(chart, series)
		{
			series.type = "candlestick";
			series.encode = { x: 0, y: [ 1,2,3,4 ], tooltip: [1, 2, 3, 4] };
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//热力图

SPT.heatmapRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		render: function(chart)
		{
			var dataSetBind = chart.dataSetBindMain();
			var xField = chart.dataSetFieldOfSign(dataSetBind, 1);
			var yField = chart.dataSetFieldOfSign(dataSetBind, 2);
			
			var chartEle = chart.element();
			var vmItemWidth = parseInt(chartEle.clientHeight/20);
			
			var options =
			{
				title: { text: chart.name() },
				tooltip: { trigger: "item" },
				grid: { bottom: vmItemWidth + 20 },
				legend: { show: false },
				xAxis:
				{
					name: chart.dataSetFieldAlias(dataSetBind, xField),
					nameGap: 5,
					type: SPT.evalDataSetFieldAxisType(chart, xField),
					splitArea: { show: true },
					data: []
				},
				yAxis:
				{
					name: chart.dataSetFieldAlias(dataSetBind, yField),
					nameGap: 5,
					type: SPT.evalDataSetFieldAxisType(chart, yField),
					splitArea: { show: true },
					data: []
				},
				visualMap:
				{
					text: ["高", "低"],
					realtime: true,
					calculable: true,
					orient: "horizontal",
			        left: "center",
			        itemWidth: vmItemWidth,
			        itemHeight: parseInt(chartEle.clientWidth/8),
			        bottom: 0,
			        min: 0,
					max: 100
				},
				series: [{ type: "heatmap", data: [] }]
			};
			
			options = EU.prepareRenderOptions(chart, options);
			var instance = EU.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			
			var seriesName = "";
			var seriesData = [];
			var dataRange = { min: null, max: null };
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let dataSetAlias = chart.dataSetAlias(dataSetBind);
				let result = chart.resultOf(chartResult, dataSetBind);
				
				let nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
				let valueFields =
				[
					chart.dataSetFieldOfSign(dataSetBind, 1),
					chart.dataSetFieldOfSign(dataSetBind, 2),
					chart.dataSetFieldOfSign(dataSetBind, 3)
				];
				
				let data = chart.resultNameValueDatas(result, nameField, valueFields);
				SPT.originalDataOfResult(data, chart, result);
				
				SPT.evalArrayDataRange(dataRange, data, "value", 2);
				
				seriesData = seriesData.concat(data);
				
				if(CF.isEmpty(seriesName))
					seriesName = dataSetAlias;
			}
			
			var mySeries = { name: seriesName, data: seriesData };
			this._configSingleSeries(chart, mySeries);
			
			//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
			var options =
			{
				xAxis: {}, yAxis: {},
				visualMap: {min: dataRange.min, max: dataRange.max},
				series: [ mySeries ]
			};
			
			SPT.trimNumberRange(options.visualMap);
			
			EU.inflateUpdateAxisData(chart, options, options.xAxis,
							EU.axisDataExtractors.valueElement(0));
			EU.inflateUpdateAxisData(chart, options, options.yAxis,
							EU.axisDataExtractors.valueElement(1), false);
			
			options = EU.prepareUpdateOptions(chart, options);
			
			EU.setOptionsReplaceMerge(chart, options);
		},
		
		_configSingleSeries: function(chart, series)
		{
			series.type = "heatmap";
			series.encode = { x: 0, y: 1, tooltip: 2 };
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//树图

SPT.treeRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		//同series[i].orient
		orient: "LR",
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		render: function(chart)
		{
			var options =
			{
				title: { text: chart.name() },
				tooltip: { trigger: "item" },
				series: [{ type: "tree", data: [] }]
			};
			
			options = EU.prepareRenderOptions(chart, options);
			var instance = EU.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			//树图只支持单根节点
			var singleRootNode = true;
			var singleSeries = SPT.inflateTreeNodeSingleSeries(chart, chartResult, { type: "tree" }, singleRootNode);
			var options = { series: [ singleSeries ] };
			
			this._inflateUpdateOptions(chart, options);
			options = EU.prepareUpdateOptions(chart, options);
			
			EU.setOptionsReplaceMerge(chart, options);
		},
		
		_inflateUpdateOptions: function(chart, updateOptions)
		{
			var label;
            var leaves;
            var left;
            var right;
            var top;
            var bottom;
			
			if(config.orient == "TB")
			{
				label = { position: "left", verticalAlign: "middle", align: "right" };
	            leaves = { label: { position: "right", verticalAlign: "middle", align: "left" } };
	            left = "12%";
	            right= "12%";
	            top = "16%";
	            bottom = "16%";
			}
			else if(config.orient == "RL")
			{
				label = { position: "right", verticalAlign: "middle", align: "left" };
	            leaves = { label: { position: "left", verticalAlign: "middle", align: "right" } };
	            left = "16%";
	            right = "16%";
	            top = "12%";
	            bottom = "12%";
			}
			else if(config.orient == "BT")
			{
				label = { position: "left", verticalAlign: "middle", align: "right" };
	            leaves = { label: { position: "right", verticalAlign: "middle", align: "left" } };
	            left = "12%";
	            right = "12%";
	            top = "16%";
	            bottom = "16%";
			}
			//LR
			else
			{
				label = { position: "left", verticalAlign: "middle", align: "right" };
	            leaves = { label: { position: "right", verticalAlign: "middle", align: "left" } };
	            left = "16%";
	            right = "16%";
	            top = "12%";
	            bottom = "12%";
			}
			
			var series0 = updateOptions.series[0];
			series0.orient = config.orient;
			series0.label = label;
            series0.leaves = leaves;
            series0.left = left;
            series0.right = right;
            series0.top = top;
            series0.bottom = bottom;
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//矩形树图

SPT.treemapRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		render: function(chart)
		{
			var options =
			{
				title: { text: chart.name() },
				tooltip: { trigger: "item" },
				series: [{ type: "treemap", data: [] }]
			};
			
			options = EU.prepareRenderOptions(chart, options);
			var instance = EU.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var singleSeries = SPT.inflateTreeNodeSingleSeries(chart, chartResult, { type: "treemap" });
			var options = { series: [ singleSeries ] };
			options = EU.prepareUpdateOptions(chart, options);
			
			EU.setOptionsReplaceMerge(chart, options);
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//旭日图

SPT.sunburstRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		render: function(chart)
		{
			var options =
			{
				title: { text: chart.name() },
				tooltip: { trigger: "item" },
				series: [{ type: "sunburst", data: [] }]
			};
			
			options = EU.prepareRenderOptions(chart, options);
			var instance = EU.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var singleSeries = SPT.inflateTreeNodeSingleSeries(chart, chartResult, { type: "sunburst" });
			var options = { series: [ singleSeries ] };
			options = EU.prepareUpdateOptions(chart, options);
			
			EU.setOptionsReplaceMerge(chart, options);
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

SPT.inflateTreeNodeSingleSeries = function(chart, chartResult, singleSeries, singleRootNode, dataSignIndexes)
{
	singleSeries = (singleSeries || {});
	singleRootNode = (singleRootNode == null ? false : singleRootNode);
	dataSignIndexes = (dataSignIndexes || { id: 0, name: 1, parent: 2, value: 3 });
	
	var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
	
	var seriesName = "";
	var seriesData = [];
	
	for(let i=0; i<dataSetBinds.length; i++)
	{
		let dataSetBind = dataSetBinds[i];
		let dataSetAlias = chart.dataSetAlias(dataSetBind);
		let result = chart.resultOf(chartResult, dataSetBind);
		
		if(CF.isEmpty(seriesName))
			seriesName = dataSetAlias;
		
		let idField = chart.dataSetFieldOfSign(dataSetBind, dataSignIndexes.id);
		let nameField = chart.dataSetFieldOfSign(dataSetBind, dataSignIndexes.name);
		let parentField = chart.dataSetFieldOfSign(dataSetBind, dataSignIndexes.parent);
		let valueField = chart.dataSetFieldOfSign(dataSetBind, dataSignIndexes.value);
		
		let data = chart.resultDatas(result);
		
		for(let j=0; j<data.length; j++)
		{
			let dataj = data[j];
			let node = {};
			
			node.name = chart.resultDataRowCell(dataj, nameField);
			node[SPT.ORIGINAL_ID_PROP_NAME] = chart.resultDataRowCell(dataj, idField);
			node[SPT.ORIGINAL_PARENT_PROP_NAME] = chart.resultDataRowCell(dataj, parentField);
			
			if(valueField)
			{
				node.value = chart.resultDataRowCell(dataj, valueField);
				SPT.treeNodeEvalValueMark(node);
			}
			
			SPT.originalDataOfData(node, dataj);
			
			//处理seriesData中可能的node子节点
			for(let k=0; k<seriesData.length; k++)
			{
				if(seriesData[k] == null)
					continue;
				
				if(SPT.treeAppendNode(node, seriesData[k]))
				{
					seriesData[k] = null;
				}
			}
			
			let addedAsChild = false;
			
			//将node添加至seriesData中可能的父节点中
			for(let k=0; k<seriesData.length; k++)
			{
				if(seriesData[k] == null)
					continue;
				
				if(SPT.treeAppendNode(seriesData[k], node))
				{
					addedAsChild = true;
					break;
				}
			}
			
			if(!addedAsChild)
				seriesData.push(node);
		}
	}
	
	seriesData = seriesData.filter((di) => { return (di != null);  });
	
	if(singleRootNode && seriesData.length > 1)
	{
		let rootNode = { name: "", children: seriesData };
		SPT.treeNodeEvalValueMark(rootNode);
		rootNode[SPT.VIRTUAL_ROOT_PROP_NAME] = true;
		SPT.treeEvalNodeValue(rootNode);
		
		seriesData = [ rootNode ];
	}
	
	singleSeries.name = seriesName;
	singleSeries.data = seriesData;
	
	return singleSeries;
};

SPT.treeNodeEvalValueMark = function(node)
{
	//标识节点值需要动态计算
	if(node.value == null || node.value == 0)
		node._evalValue = true;
};

SPT.treeAppendNode = function(treeNode, node)
{
	if(!treeNode)
		return false;
	
	if(node[SPT.ORIGINAL_PARENT_PROP_NAME] === treeNode[SPT.ORIGINAL_ID_PROP_NAME])
	{
		if(!treeNode.children)
			treeNode.children = [];
		
		treeNode.children.push(node);
		SPT.treeEvalNodeValue(treeNode);
		
		return true;
	}
	
	if(treeNode.children)
	{
		for(let i=0; i<treeNode.children.length; i++)
		{
			let child = treeNode.children[i];
			
			if(SPT.treeAppendNode(child, node))
			{
				SPT.treeEvalNodeValue(treeNode);
				return true;
			}
		}
	}
	
	return false;
};

SPT.treeEvalNodeValue = function(treeNode)
{
	if(treeNode._evalValue !== true)
		return;
	
	let value = 0;
	
	if(treeNode.children != null)
	{
		for(let i=0; i<treeNode.children.length; i++)
		{
			let child = treeNode.children[i];
			SPT.treeEvalNodeValue(child);
			
			if(child.value != null && CF.isNumber(child.value))
				value += child.value;
		}
	}
	
	treeNode.value = value;
};

//桑基图

SPT.sankeyRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		//同series[i].orient
		orient: "horizontal"
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		render: function(chart)
		{
			var options =
			{
				title: { text: chart.name() },
				tooltip:
				{
					trigger: "item",
					//ECharts-6.0默认tooltip显示有缺陷，所以这里自定义了formatter选项
					formatter: function(params)
					{
						return EU.customTooltip(params, (pi) =>
						{
							let re = {};
							
							if(pi.dataType == "edge" && pi.data)
							{
								re.name = pi.data[SPT.ORIGINAL_SOURCE_NAME_PROP_NAME] + " - " + pi.data[SPT.ORIGINAL_TARGET_NAME_PROP_NAME];
							}
							else if(pi.dataType == "node" && pi.value != null)
							{
								re.value = pi.value;
							}
							
							return re;
						});
					}
				},
				series:
				[
					{ type: "sankey", data: [], links: [] }
				]
			};
			
			options = EU.prepareRenderOptions(chart, options);
			var instance = EU.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			
			var options =
			{
				series: [{ name: "", data: [], links: [] }]
			};
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				
				//节点数据集
				if(chart.isDataSetSigned(dataSetBind, 0))
				{
					this._inflateOptionsForNode(chart, chartResult, dataSetBinds, i, options);
				}
				//合并数据集
				else if(chart.isDataSetSigned(dataSetBind, 2))
				{
					this._inflateOptionsForJoin(chart, chartResult, dataSetBinds, i, options);
				}
				
				this._inflateCommonOptions(chart, chartResult, dataSetBinds, i, options);
			}
			
			//应在所有节点填充完成后再处理关系
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				
				//关系数据集
				if(chart.isDataSetSigned(dataSetBind, 1))
				{
					this._inflateOptionsForLink(chart, chartResult, dataSetBinds, i, options);
				}
				
				this._inflateCommonOptions(chart, chartResult, dataSetBinds, i, options);
			}
			
			this._configSingleSeries(chart, options.series[0]);
			this._inflateUpdateOptions(chart, options);
			options = EU.prepareUpdateOptions(chart, options);
			EU.setOptionsReplaceMerge(chart, options);
		},
		
		_inflateUpdateOptions: function(chart, updateOptions)
		{
			var series0 = updateOptions.series[0];
			series0.orient = config.orient;
			
			if(series0.orient == "horizontal")
			{
				series0.left = "16%";
	            series0.right = "16%";
	            series0.top = "12%";
	            series0.bottom = "12%";
			}
			else if(series0.orient == "vertical")
			{
				series0.label = { position: "top" };
	            series0.left = "12%";
	            series0.right = "12%";
	            series0.top = "16%";
	            series0.bottom = "16%";
			}
			
			//自适应条目宽度和间隔
			var chartEle = chart.element();
			var width = chartEle.clientWidth;
			var height = chartEle.clientHeight;
			
			var totalWidth = (series0.orient == "vertical" ? height : width);
			nodeWidth = parseInt(totalWidth * 5/100);
			nodeWidth = (nodeWidth < 4 ? 4: nodeWidth);
			series0.nodeWidth = nodeWidth;
			
			var totalWidth = (series0.orient == "vertical" ? width : height);
			nodeGap = parseInt(totalWidth * 2/100);
			nodeGap = (nodeWidth < 1 ? 1: nodeGap);
			series0.nodeGap = nodeGap;
		},
		
		_inflateCommonOptions: function(chart, chartResult, dataSetBinds, dsbIndex, options)
		{
			var dataSetBind = dataSetBinds[dsbIndex];
			
			if(CF.isEmpty(options.series[0].name))
				options.series[0].name = chart.dataSetAlias(dataSetBind);
		},
		
		_inflateOptionsForNode: function(chart, chartResult, dataSetBinds, dsbIndex, options)
		{
			var seriesData = options.series[0].data;
			
			var dataSetBind = dataSetBinds[dsbIndex];
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var idField = chart.dataSetFieldOfSign(dataSetBind, [0, 0]);
			var nameField = chart.dataSetFieldOfSign(dataSetBind, [0, 1]);
			var valueField = chart.dataSetFieldOfSign(dataSetBind, [0, 2]);
			
			var data = chart.resultDatas(result);
			
			for(let i=0; i<data.length; i++)
			{
				let dataj = data[i];
				let node = { name: chart.resultDataRowCell(dataj, nameField) };
				node[SPT.ORIGINAL_ID_PROP_NAME] = chart.resultDataRowCell(dataj, idField);
				SPT.originalDataOfData(node, dataj);
				
				if(valueField)
					node.value = chart.resultDataRowCell(dataj, valueField);
				
				this._appendNode(seriesData, node);
			}
		},
		
		_inflateOptionsForLink: function(chart, chartResult, dataSetBinds, dsbIndex, options)
		{
			var seriesData = options.series[0].data;
			var seriesLinks = options.series[0].links;
			
			var dataSetBind = dataSetBinds[dsbIndex];
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var sourceField = chart.dataSetFieldOfSign(dataSetBind, [1, 0]);
			var targetField = chart.dataSetFieldOfSign(dataSetBind, [1, 1]);
			var valueField = chart.dataSetFieldOfSign(dataSetBind, [1, 2]);
			var fieldMap = { source: sourceField, target: targetField, value: valueField };
			
			var data = chart.resultMapDatas(result, fieldMap);
			
			for(let i=0; i<data.length; i++)
			{
				let di = data[i];
				let srcIdx = SPT.findInArray(seriesData, di.source, SPT.ORIGINAL_ID_PROP_NAME);
				let srcNode = (srcIdx >= 0 ? seriesData[srcIdx] : null);
				let tgtIdx = SPT.findInArray(seriesData, di.target, SPT.ORIGINAL_ID_PROP_NAME);
				let tgtNode = (tgtIdx >= 0 ? seriesData[tgtIdx] : null);
				
				if(srcNode != null && tgtNode != null)
				{
					//如果使用id值作为关系标识，对于数值型id，echarts会误当做数据索引；
					//如果使用名称作为关系标识，在连接线上的tooltip只会显示索引数值不友好，所有这里使用索引号同时自定义tooltip
					let link = { source: srcIdx, target: tgtIdx, value: di.value };
					this._inflateLinkOriginalInfo(link, srcNode, tgtNode);
					SPT.originalDataOfData(link, di);
					
					seriesLinks.push(link);
				}
			}
		},
		
		_inflateOptionsForJoin: function(chart, chartResult, dataSetBinds, dsbIndex, options)
		{
			var seriesData = options.series[0].data;
			var seriesLinks = options.series[0].links;
			
			var dataSetBind = dataSetBinds[dsbIndex];
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var srcIdField = chart.dataSetFieldOfSign(dataSetBind, [2, 0]);
			var srcNameField = chart.dataSetFieldOfSign(dataSetBind, [2, 1]);
			var srcValueField = chart.dataSetFieldOfSign(dataSetBind, [2, 2]);
			var tgtIdField = chart.dataSetFieldOfSign(dataSetBind, [2, 3]);
			var tgtNameField = chart.dataSetFieldOfSign(dataSetBind, [2, 4]);
			var tgtValueField = chart.dataSetFieldOfSign(dataSetBind, [2, 5]);
			var valueField = chart.dataSetFieldOfSign(dataSetBind, [2, 6]);
			
			var data = chart.resultDatas(result);
			
			for(let i=0; i<data.length; i++)
			{
				let dataj = data[i];
				let sd = { name: chart.resultDataRowCell(dataj, srcNameField) };
				let td = { name: chart.resultDataRowCell(dataj, tgtNameField) };
				
				sd[SPT.ORIGINAL_ID_PROP_NAME] = chart.resultDataRowCell(dataj, srcIdField);
				td[SPT.ORIGINAL_ID_PROP_NAME] = chart.resultDataRowCell(dataj, tgtIdField);
				
				SPT.originalDataOfData(sd, dataj);
				SPT.originalDataOfData(td, dataj);
				
				if(srcValueField)
					sd.value = chart.resultDataRowCell(dataj, srcValueField);
				
				if(tgtValueField)
					td.value = chart.resultDataRowCell(dataj, tgtValueField);
				
				let srcIdx = this._appendNode(seriesData, sd);
				let tgtIdx = this._appendNode(seriesData, td);
				
				//如果使用id值作为关系标识，对于数值型id，echarts会误当做数据索引；
				//如果使用名称作为关系标识，在连接线上的tooltip只会显示索引数值不友好，所有这里使用索引号同时自定义tooltip
				let link = { source: srcIdx, target: tgtIdx, value: chart.resultDataRowCell(dataj, valueField) };
				this._inflateLinkOriginalInfo(link, sd, td);
				SPT.originalDataOfData(link, dataj);
				seriesLinks.push(link);
			}
		},
		
		_inflateLinkOriginalInfo: function(link, sourceNode, targetNode)
		{
			link[SPT.ORIGINAL_SOURCE_ID_PROP_NAME] = sourceNode[SPT.ORIGINAL_ID_PROP_NAME];
			link[SPT.ORIGINAL_SOURCE_NAME_PROP_NAME] = sourceNode.name;
			link[SPT.ORIGINAL_TARGET_ID_PROP_NAME] = targetNode[SPT.ORIGINAL_ID_PROP_NAME];
			link[SPT.ORIGINAL_TARGET_NAME_PROP_NAME] = targetNode.name;
		},
		
		_appendNode: function(seriesData, node)
		{
			return SPT.appendDistinct(seriesData, node, SPT.ORIGINAL_ID_PROP_NAME);
		},
		
		_configSingleSeries: function(chart, series)
		{
			series.type = "sankey";
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//关系图

SPT.graphRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		//同series[i].layout
		layout: "force"
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		
		render: function(chart)
		{
			var options =
			{
				title: { text: chart.name() },
				tooltip:
				{
					trigger: "item",
					//ECharts-6.0默认tooltip显示有缺陷，所以这里自定义了formatter选项
					formatter: function(params)
					{
						return EU.customTooltip(params, (pi) =>
						{
							let re = {};
							
							if(pi.dataType == "edge" && pi.data)
							{
								re.name = pi.data[SPT.ORIGINAL_SOURCE_NAME_PROP_NAME] + " > " + pi.data[SPT.ORIGINAL_TARGET_NAME_PROP_NAME];
							}
							else if(pi.dataType == "node" && pi.value != null)
							{
								re.value = pi.value;
							}
							
							return re;
						});
					}
				},
				legend: { data: [] },
				series:
				[
					{ type: "graph", data: [] }
				]
			};
			
			options = EU.prepareRenderOptions(chart, options);
			var instance = EU.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			
			var options =
			{
				legend: { data: [] },
				series: [{ name: "", categories: [], data: [], links: [] }]
			};
			
			var dataRange = { min: null, max: null };
			var symbolSizeMax = SPT.evalSymbolSizeMax(chart);
			var symbolSizeMin = SPT.evalSymbolSizeMin(chart, symbolSizeMax);
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				
				//节点数据集
				if(chart.isDataSetSigned(dataSetBind, 0))
				{
					this._inflateOptionsForNode(chart, chartResult, dataSetBinds, i, options, dataRange);
				}
				//合并数据集
				else if(chart.isDataSetSigned(dataSetBind, 2))
				{
					this._inflateOptionsForJoin(chart, chartResult, dataSetBinds, i, options, dataRange);
				}
				
				this._inflateCommonOptions(chart, chartResult, dataSetBinds, i, options);
			}
			
			//应在所有节点填充完成后再处理关系
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				
				//关系数据集
				if(chart.isDataSetSigned(dataSetBind, 1))
				{
					this._inflateOptionsForLink(chart, chartResult, dataSetBinds, i, options);
				}
				
				this._inflateCommonOptions(chart, chartResult, dataSetBinds, i, options);
			}
			
			this._configSingleSeries(chart, options.series[0], symbolSizeMax);
			SPT.evalSeriesDataValueSymbolSize(options.series, dataRange.min, dataRange.max, symbolSizeMax, symbolSizeMin, "value");
			
			options = EU.prepareUpdateOptions(chart, options);
			EU.setOptionsReplaceMerge(chart, options);
		},
		
		_inflateCommonOptions: function(chart, chartResult, dataSetBinds, dsbIndex, options)
		{
			var dataSetBind = dataSetBinds[dsbIndex];
			
			if(CF.isEmpty(options.series[0].name))
				options.series[0].name = chart.dataSetAlias(dataSetBind);
		},
		
		_inflateOptionsForNode: function(chart, chartResult, dataSetBinds, dsbIndex, options, dataRange)
		{
			var legendData = options.legend.data;
			var seriesCategories = options.series[0].categories;
			var seriesData = options.series[0].data;
			
			var dataSetBind = dataSetBinds[dsbIndex];
			var dataSetAlias = chart.dataSetAlias(dataSetBind);
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var idField = chart.dataSetFieldOfSign(dataSetBind, [0, 0]);
			var nameField = chart.dataSetFieldOfSign(dataSetBind, [0, 1]);
			var valueField = chart.dataSetFieldOfSign(dataSetBind, [0, 2]);
			var categoryField = chart.dataSetFieldOfSign(dataSetBind, [0, 3]);
			
			var data = chart.resultDatas(result);
			
			for(let i=0; i<data.length; i++)
			{
				let dataj = data[i];
				let node = { name: chart.resultDataRowCell(dataj, nameField) };
				
				node[SPT.ORIGINAL_ID_PROP_NAME] = chart.resultDataRowCell(dataj, idField);
				SPT.originalDataOfData(node, dataj);
				
				if(valueField)
				{
					let v = chart.resultDataRowCell(dataj, valueField);
					node.value = v;
					
					dataRange.min = (dataRange.min == null ? v : Math.min(dataRange.min, v));
					dataRange.max = (dataRange.max == null ? v : Math.max(dataRange.max, v));
				}
				
				let category = (categoryField ? chart.resultDataRowCell(dataj, categoryField) : dataSetAlias);
				node[SPT.ORIGINAL_CATEGORY_PROP_NAME] = category;
				node.category = SPT.appendDistinct(seriesCategories, {name: category}, "name");
				SPT.appendDistinct(legendData, {name: category}, "name");
				
				this._appendNode(seriesData, node);
			}
		},
		
		_inflateOptionsForLink: function(chart, chartResult, dataSetBinds, dsbIndex, options)
		{
			var seriesData = options.series[0].data;
			var seriesLinks = options.series[0].links;
			
			var dataSetBind = dataSetBinds[dsbIndex];
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var sourceField = chart.dataSetFieldOfSign(dataSetBind, [1, 0]);
			var targetField = chart.dataSetFieldOfSign(dataSetBind, [1, 1]);
			var fieldMap = { source: sourceField, target: targetField };
			
			var data = chart.resultMapDatas(result, fieldMap);
			
			for(let i=0; i<data.length; i++)
			{
				let di = data[i];
				let srcIdx = SPT.findInArray(seriesData, di.source, SPT.ORIGINAL_ID_PROP_NAME);
				let srcNode = (srcIdx >= 0 ? seriesData[srcIdx] : null);
				let tgtIdx = SPT.findInArray(seriesData, di.target, SPT.ORIGINAL_ID_PROP_NAME);
				let tgtNode = (tgtIdx >= 0 ? seriesData[tgtIdx] : null);
				
				if(srcNode != null && tgtNode != null)
				{
					//如果使用id值作为关系标识，对于数值型id，echarts会误当做数据索引；
					//如果使用名称作为关系标识，在连接线上的tooltip只会显示索引数值不友好，所有这里使用索引号同时自定义tooltip
					let link = { source: srcIdx, target: tgtIdx };
					this._inflateLinkOriginalInfo(link, srcNode, tgtNode);
					SPT.originalDataOfData(link, di);
					
					seriesLinks.push(link);
				}
			}
		},
		
		_inflateOptionsForJoin: function(chart, chartResult, dataSetBinds, dsbIndex, options, dataRange)
		{
			var legendData = options.legend.data;
			var seriesCategories = options.series[0].categories;
			var seriesData = options.series[0].data;
			var seriesLinks = options.series[0].links;
			
			var dataSetBind = dataSetBinds[dsbIndex];
			var dataSetAlias = chart.dataSetAlias(dataSetBind);
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var srcIdField = chart.dataSetFieldOfSign(dataSetBind, [2, 0]);
			var srcNameField = chart.dataSetFieldOfSign(dataSetBind, [2, 1]);
			var srcValueField = chart.dataSetFieldOfSign(dataSetBind, [2, 2]);
			var srcCategoryField = chart.dataSetFieldOfSign(dataSetBind, [2, 3]);
			var tgtIdField = chart.dataSetFieldOfSign(dataSetBind, [2, 4]);
			var tgtNameField = chart.dataSetFieldOfSign(dataSetBind, [2, 5]);
			var tgtValueField = chart.dataSetFieldOfSign(dataSetBind, [2, 6]);
			var tgtCategoryField = chart.dataSetFieldOfSign(dataSetBind, [2, 7]);
			
			var data = chart.resultDatas(result);
			
			for(let i=0; i<data.length; i++)
			{
				let dataj = data[i];
				let sd = { name: chart.resultDataRowCell(dataj, srcNameField) };
				let td = { name: chart.resultDataRowCell(dataj, tgtNameField) };
				
				sd[SPT.ORIGINAL_ID_PROP_NAME] = chart.resultDataRowCell(dataj, srcIdField);
				td[SPT.ORIGINAL_ID_PROP_NAME] = chart.resultDataRowCell(dataj, tgtIdField);
				
				SPT.originalDataOfData(sd, dataj);
				SPT.originalDataOfData(td, dataj);
				
				if(srcValueField)
				{
					let sv = chart.resultDataRowCell(dataj, srcValueField);
					sd.value = sv;
					
					dataRange.min = (dataRange.min == null ? sv : Math.min(dataRange.min, sv));
					dataRange.max = (dataRange.max == null ? sv : Math.max(dataRange.max, sv));
				}
				
				let srcCategory = (srcCategoryField ? chart.resultDataRowCell(dataj, srcCategoryField) : dataSetAlias);
				sd[SPT.ORIGINAL_CATEGORY_PROP_NAME] = srcCategory;
				sd.category = SPT.appendDistinct(seriesCategories, {name: srcCategory}, "name");
				SPT.appendDistinct(legendData, {name: srcCategory}, "name");
				
				if(tgtValueField)
				{
					let tv = chart.resultDataRowCell(dataj, tgtValueField);
					td.value = tv;
					
					dataRange.min = (dataRange.min == null ? tv : Math.min(dataRange.min, tv));
					dataRange.max = (dataRange.max == null ? tv : Math.max(dataRange.max, tv));
				}
				
				let tgtCategory = (tgtCategoryField ? chart.resultDataRowCell(dataj, tgtCategoryField) : dataSetAlias);
				td[SPT.ORIGINAL_CATEGORY_PROP_NAME] = tgtCategory;
				td.category = SPT.appendDistinct(seriesCategories, {name: tgtCategory}, "name");
				SPT.appendDistinct(legendData, {name: tgtCategory}, "name");
				
				let srcIdx = this._appendNode(seriesData, sd);
				let tgtIdx = this._appendNode(seriesData, td);
				
				//如果使用id值作为关系标识，对于数值型id，echarts会误当做数据索引；
				//如果使用名称作为关系标识，在连接线上的tooltip只会显示索引数值不友好，所有这里使用索引号同时自定义tooltip
				let link = { source: srcIdx, target: tgtIdx };
				this._inflateLinkOriginalInfo(link, sd, td);
				SPT.originalDataOfData(link, dataj);
				seriesLinks.push(link);
			}
		},
		
		_inflateLinkOriginalInfo: function(link, sourceNode, targetNode)
		{
			link[SPT.ORIGINAL_SOURCE_ID_PROP_NAME] = sourceNode[SPT.ORIGINAL_ID_PROP_NAME];
			link[SPT.ORIGINAL_SOURCE_NAME_PROP_NAME] = sourceNode.name;
			link[SPT.ORIGINAL_TARGET_ID_PROP_NAME] = targetNode[SPT.ORIGINAL_ID_PROP_NAME];
			link[SPT.ORIGINAL_TARGET_NAME_PROP_NAME] = targetNode.name;
		},
		
		_appendNode: function(seriesData, node)
		{
			return SPT.appendDistinct(seriesData, node, SPT.ORIGINAL_ID_PROP_NAME);
		},
		
		_configSingleSeries: function(chart, series, symbolSizeMax)
		{
			series.type = "graph";
			series.layout = config.layout;
			
			if(series.layout == "force")
			{
				series.draggable = true;
				series.force = {};
				//自动计算散点间距
				series.force.edgeLength = parseInt(symbolSizeMax*1.5);
				//自动计算散点稀疏度
				series.force.repulsion = parseInt(symbolSizeMax*2);
			}
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//箱型图

SPT.boxplotRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		//散点图类型："scatter"、"effectScatter"
		scatterType: "scatter",
		//是否互换坐标轴
		interchangeAxis: false
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		render: function(chart)
		{
			var dataSetBind = chart.dataSetBindMain();
			var nameField = chart.dataSetFieldOfSign(dataSetBind, [0, 0]);
			
			var options =
			{
				title: { text: chart.name() },
				tooltip: { trigger: "item" },
				legend: { data: [] },
				xAxis:
				{
					name: chart.dataSetFieldAlias(dataSetBind, nameField),
					type: SPT.evalDataSetFieldAxisType(chart, nameField),
					boundaryGap: true,
					splitLine: { show: false }
				},
				yAxis:
				{
					name: "",
					type: "value"
				},
				series:
				[
					{ type: "boxplot", data: [] }
				]
			};
			
			//非类目轴需要设置，不然图形会贴边
			if(options.xAxis.type != "category")
				options.xAxis.boundaryGap = [ "12%", "12%" ];
			
			if(config.interchangeAxis)
			{
				let xAxisTmp = options.xAxis;
				options.xAxis = options.yAxis;
				options.yAxis = xAxisTmp;
			}
			
			options = EU.prepareRenderOptions(chart, options);
			var instance = EU.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			
			var options = { legend: { data: [] }, series: [] };
			//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
			(config.interchangeAxis ? (options.yAxis = {}) : (options.xAxis = {}));
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				
				//箱形数据集
				if(chart.isDataSetSigned(dataSetBind, 0))
				{
					this._inflateOptionsForBoxplot(chart, chartResult, dataSetBinds, i, options);
				}
				//异常值数据集
				else if(chart.isDataSetSigned(dataSetBind, 1))
				{
					this._inflateOptionsForOutlier(chart, chartResult, dataSetBinds, i, options);
				}
			}
			
			EU.inflateUpdateAxisData(chart, options, (config.interchangeAxis ? options.yAxis : options.xAxis),
							{
								get: function(s)
								{
									if(s.type == "boxplot")
										return EU.axisDataExtractors.propertyName();
									else
										return EU.axisDataExtractors.valueElement0();
								}
							});
			options = EU.prepareUpdateOptions(chart, options);
			
			EU.setOptionsReplaceMerge(chart, options);
		},
		
		_inflateOptionsForBoxplot: function(chart, chartResult, dataSetBinds, dsbIndex, options)
		{
			var legendData = options.legend.data;
			var series = options.series;
			
			var dataSetBind = dataSetBinds[dsbIndex];
			var dataSetAlias = chart.dataSetAlias(dataSetBind);
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var nameField = chart.dataSetFieldOfSign(dataSetBind, [0, 0]);
			var valueFields =
			[
				nameField,
				chart.dataSetFieldOfSign(dataSetBind, [0, 1]),
				chart.dataSetFieldOfSign(dataSetBind, [0, 2]),
				chart.dataSetFieldOfSign(dataSetBind, [0, 3]),
				chart.dataSetFieldOfSign(dataSetBind, [0, 4]),
				chart.dataSetFieldOfSign(dataSetBind, [0, 5])
			];
			var categoryField = chart.dataSetFieldOfSign(dataSetBind, [0, 6]);
			
			var fieldMap = { name: nameField, value: valueFields };
			if(categoryField)
				fieldMap = SPT.addCategoryToFieldMap(fieldMap, categoryField);
			
			var data = chart.resultMapDatas(result, fieldMap);
			SPT.originalDataOfResult(data, chart, result);
			
			if(categoryField)
			{
				var categoryNames = [];
				var categoryDatasMap = {};
				SPT.splitDataByCategory(data, categoryNames, categoryDatasMap);
				
				for(var j=0; j<categoryNames.length; j++)
				{
					var categoryName = categoryNames[j];
					var legendName = SPT.legendNameForDataCategory(dataSetBinds, dataSetAlias, categoryName);
					var mySeries = { name: legendName, data: categoryDatasMap[categoryName] };
					
					this._configSingleSeriesForBoxplot(chart, mySeries);
					legendData.push({ name: legendName });
					series.push(mySeries);
				}
			}
			else
			{
				let mySeries = { name: dataSetAlias, data: data };
				
				this._configSingleSeriesForBoxplot(chart, mySeries);
				legendData.push({ name: dataSetAlias });
				series.push(mySeries);
			}
		},
		
		_configSingleSeriesForBoxplot: function(chart, series)
		{
			series.type = "boxplot";
			series.encode = (config.interchangeAxis ? { x: [1, 2, 3,4 ,5], y: 0 } : { x: 0, y: [1, 2, 3, 4, 5] });
		},
		
		_inflateOptionsForOutlier: function(chart, chartResult, dataSetBinds, dsbIndex, options)
		{
			var legendData = options.legend.data;
			var series = options.series;
			
			var dataRange = { min: 1, max: 1 };
			var symbolSizeMax = SPT.evalSymbolSizeMax(chart);
			var symbolSizeMin = SPT.evalSymbolSizeMin(chart, symbolSizeMax);
			
			let dataSetBind = dataSetBinds[dsbIndex];
			let dataSetAlias = chart.dataSetAlias(dataSetBind);
			let result = chart.resultOf(chartResult, dataSetBind);
			
			let nameField = chart.dataSetFieldOfSign(dataSetBind, [1, 0]);
			let categoryField = chart.dataSetFieldOfSign(dataSetBind, [1, 2]);
			
			if(categoryField)
			{
				let valueField = chart.dataSetFieldOfSign(dataSetBind, [1, 1]);
				let categoryNames = [];
				let categoryDatasMap = {};
				
				//使用{name,value:[]}格式可以更好地兼容category、value、time坐标轴类型以及tooltip、事件数据
				let fieldMap = { name: nameField, value: [nameField, valueField] };
				fieldMap = SPT.addCategoryToFieldMap(fieldMap, categoryField);
				let data = chart.resultMapDatas(result, fieldMap);
				SPT.originalDataOfResult(data, chart, result);
				SPT.evalDataValueSymbolSize(data, dataRange.min, dataRange.max, symbolSizeMax, symbolSizeMin, "_inexists");
				SPT.splitDataByCategory(data, categoryNames, categoryDatasMap);
				
				for(let j=0; j<categoryNames.length; j++)
				{
					let categoryName = categoryNames[j];
					let legendName = SPT.legendNameForDataCategory(dataSetBinds, dataSetAlias, categoryName);
					let mySeries = { name: legendName, data: categoryDatasMap[categoryName] };
					
					this._configSingleSeriesForOutlier(chart, mySeries);
					legendData.push({ name: legendName });
					series.push(mySeries);
				}
			}
			else
			{
				let valueFields = chart.dataSetFieldsOfSign(dataSetBind, [1, 1]);
				
				for(let j=0; j<valueFields.length; j++)
				{
					let legendName = SPT.legendNameForDataValues(chart, dataSetBinds, dataSetBind, dataSetAlias, valueFields, j);
					//使用{name,value:[]}格式可以更好地兼容category、value、time坐标轴类型以及tooltip、事件数据
					let fieldMap = { name: nameField, value: [nameField, valueFields[j]] };
					let data = chart.resultMapDatas(result, fieldMap);
					SPT.originalDataOfResult(data, chart, result);
					SPT.evalDataValueSymbolSize(data, dataRange.min, dataRange.max, symbolSizeMax, symbolSizeMin, "_inexists");
					let mySeries = { name: legendName, data: data };
					
					this._configSingleSeriesForOutlier(chart, mySeries);
					legendData.push({ name: legendName });
					series.push(mySeries);
				}
			}
		},
		
		_configSingleSeriesForOutlier: function(chart, series)
		{
			series.type = config.scatterType;
			series.encode = (config.interchangeAxis ? { x: 1, y: 0 } : { x: 0, y: 1 });
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//词云图

SPT.wordcloudRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
	},
	config);
	
	var renderer =
	{
		depend:
		{
			name: "echarts-wordcloud",
			version: "2.1.0",
			acceptVersion: ">=2.0",
			source: "lib/echarts-wordcloud-2.1.0/echarts-wordcloud.min.js",
			depend: SPT.ECHARTS_RENDERER_DEPEND
		},
		render: function(chart)
		{
			var chartEle = chart.element();
			//不支持在echarts主题中设置样式，只能在这里设置
			var chartTheme = chart.theme();
			
			//自适应字体大小
			var baseSize = Math.min(chartEle.clientWidth, chartEle.clientHeight);
			var sizeRange = [parseInt(baseSize * 1/40), parseInt(baseSize * 1/8)];
			sizeRange[0] = (sizeRange[0] < 6 ? 6: sizeRange[0]);
			sizeRange[1] = (sizeRange[1] < 12 ? 12: sizeRange[1]);
			
			var options =
			{
				title: { text: chart.name() },
				tooltip: { trigger: "item" },
				series:
				[
					{
						type: "wordCloud", shape: "circle", data: [],
						textStyle: { color: chartTheme.color },
						sizeRange: sizeRange,
						emphasis:
						{
							focus: "self",
							textStyle:
							{
								"fontWeight": "bold"
							}
						}
					}
				]
			};
			
			options = EU.prepareRenderOptions(chart, options);
			var instance = EU.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			
			var seriesName = "";
			var seriesData = [];
			var dataRange = { min: null, max: null };
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let result = chart.resultOf(chartResult, dataSetBind);
				
				if(CF.isEmpty(seriesName))
					seriesName = chart.dataSetAlias(dataSetBind);
				
				let nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
				let valueField = chart.dataSetFieldOfSign(dataSetBind, 1);
				
				let data = chart.resultNameValueDatas(result, nameField, valueField);
				SPT.originalDataOfResult(data, chart, result);
				SPT.evalArrayDataRange(dataRange, data, "value");
				
				seriesData = seriesData.concat(data);
			}
			
			//映射颜色值
			this._inflateSeriesDataTextStyle(chart, seriesData, dataRange);
			
			var options = { series: [ { type: "wordCloud", name: seriesName, data: seriesData } ] };
			options = EU.prepareUpdateOptions(chart, options);
			
			EU.setOptionsReplaceMerge(chart, options);
		},
		
		_inflateSeriesDataTextStyle: function(chart, seriesData, dataRange)
		{
			dataRange.min = (dataRange.min >= dataRange.max ? dataRange.max - 1 : dataRange.min);
			
			var chartTheme = chart.theme();
			var colorRange = chartTheme.graphRangeColors;
			var colorGradients = [];
			
			for(let i=0; i<colorRange.length; i++)
			{
				let fromColor = colorRange[i];
				let toColor = ((i+1) < colorRange.length ? colorRange[i+1] : null);
				
				if(!toColor)
					break;
				
				colorGradients = colorGradients.concat(CF.evalGradualColors(fromColor, toColor, 5));
			}
			
			for(let i=0; i<seriesData.length; i++)
			{
				let colorIndex = parseInt((seriesData[i].value-dataRange.min)/(dataRange.max-dataRange.min) * (colorGradients.length-1));
				seriesData[i].textStyle = { "color": colorGradients[colorIndex] };
			}
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//水球图

SPT.liquidfillRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		//同series[i].shape
		shape: "circle"
	},
	config);
	
	var renderer =
	{
		depend:
		{
			name: "echarts-liquidfill",
			acceptVersion: ">=3.0",
			version: "3.1.0",
			source: "lib/echarts-liquidfill-3.1.0/echarts-liquidfill.min.js",
			depend: SPT.ECHARTS_RENDERER_DEPEND
		},
		render: function(chart)
		{
			var chartEle = chart.element();
			//不支持在echarts主题中设置样式，只能在这里设置
			var chartTheme = chart.theme();
			
			//自适应字体大小
			var baseSize = Math.min(chartEle.clientWidth, chartEle.clientHeight);
			var fontSize = parseInt(baseSize * 8/50);
			fontSize = (fontSize < 6 ? 6: fontSize);
			
			var options =
			{
				//如果仅有一个波浪数据，则自动复制扩充至这些个波浪数据
				autoInflateWave: 3,
				
				title: { text: chart.name() },
				tooltip: { trigger: "item" },
				series:
				[
					{
						type: "liquidFill", shape: config.shape, radius: "75%", data: [],
						color: ['#294D99', '#156ACF', '#1598ED', '#45BDFF'],
						backgroundStyle: { color: "transparent" },
						outline:
						{
							itemStyle:
							{
								borderColor: chart.themeGradualColor(0.3),
								shadowColor: chart.themeGradualColor(0.3)
							}
						},
						label:
						{
							color: chartTheme.color,
							fontSize: fontSize,
							//当series.data为空时（图表渲染时），label会显示"series***"异常内容，所以这里重新处理
							formatter: function(param)
							{
								var value = (param && param.data != null ? param.data.value : null);
								value = (value != null ? value : (param && param.value != null ? param.value : null));
								
								if(value == null)
									return "";
								
								//此处逻辑参考自echarts-liquidfill.js
								value = 100 * value;
								return (isNaN(value) ? "" : value.toFixed(0) + "%");
							}
						}
					}
				]
			};
			
			options = EU.prepareRenderOptions(chart, options);
			var instance = EU.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var renderOptions = SPT.liveDataRenderOptions(chart);
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			
			var seriesName = "";
			var seriesData = [];
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let result = chart.resultOf(chartResult, dataSetBind);
				
				if(CF.isEmpty(seriesName))
					seriesName = chart.dataSetAlias(dataSetBind);
				
				let nameFields = chart.dataSetFieldsOfSign(dataSetBind, 0);
				let valueFields = chart.dataSetFieldsOfSign(dataSetBind, 1);
				let noNameField = CF.isEmpty(nameFields);
				
				if(!noNameField && nameFields.length != valueFields.length)
					throw new Error("The [name] sign column must be one-to-one with [value] sign column");
				
				let originalDatas = chart.resultDatas(result);
				
				if(noNameField)
				{
					let ras = chart.resultRowArrayDatas(result, valueFields);
					for(let j=0; j<ras.length; j++)
					{
						let ra = ras[j];
						for(let k=0; k<ra.length; k++)
						{
							let sv = { name: chart.dataSetFieldAlias(dataSetBind, valueFields[k]), value: ra[k] };
							SPT.originalDataOfData(sv, originalDatas[j]);
							
							seriesData.push(sv);
						}
					}
				}
				else
				{
					let namess = chart.resultRowArrayDatas(result, nameFields);
					let valuess = chart.resultRowArrayDatas(result, valueFields);
					
					for(let j=0; j<namess.length; j++)
					{
						let names = namess[j];
						let values = valuess[j];
						
						for(let k=0; k<names.length; k++)
						{
							let sv = { name: names[k], value: values[k] };
							SPT.originalDataOfData(sv, originalDatas[j]);
							
							seriesData.push(sv);
						}
					}
				}
			}
			
			//如果仅有一个波浪，则自动扩充
			if(seriesData.length == 1 && renderOptions.autoInflateWave > 1)
			{
				for(var i=1; i<renderOptions.autoInflateWave; i++)
				{
					var inflateValue = CF.extend(true, {}, seriesData[0]);
					seriesData.push(inflateValue);
				}
			}
			
			var options = { series: [ { type: "liquidFill", name: seriesName, data: seriesData, shape: config.shape } ] };
			options = EU.prepareUpdateOptions(chart, options);
			
			EU.setOptionsReplaceMerge(chart, options);
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//平行坐标系

SPT.parallelRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		//同parallel.layout
		parallelLayout: "horizontal",
		//是否平滑
		smooth: false
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		render: function(chart)
		{
			var options =
			{
				title: { text: chart.name() },
				tooltip: { trigger: "item" },
				legend: { data: [] },
				parallel: { layout: config.parallelLayout },
				parallelAxis:  [],
				series:
				[
					{ type: "parallel", data: [] }
				]
			};
			
			options = EU.prepareRenderOptions(chart, options);
			var instance = EU.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var parallelAxis = this._evalParallelAxis(chart, dataSetBinds);
			var valuePropertyNamess = this._evalValueFieldNamess(chart, dataSetBinds, parallelAxis);
			var categoryNames = [];
			var categoryDatasMap = {};
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let dataSetAlias = chart.dataSetAlias(dataSetBind);
				let result = chart.resultOf(chartResult, dataSetBind);
				
				let fieldMap = { name: chart.dataSetFieldOfSign(dataSetBind, 0), value: valuePropertyNamess[i] };
				let categoryField = chart.dataSetFieldOfSign(dataSetBind, 2);
				
				if(categoryField)
					fieldMap = SPT.addCategoryToFieldMap(fieldMap, categoryField);
				
				let data = chart.resultMapDatas(result, fieldMap);
				SPT.originalDataOfResult(data, chart, result);
				
				if(categoryField)
					SPT.splitDataByCategory(data, categoryNames, categoryDatasMap);
				else
					SPT.appendCategoryNameAndData(categoryNames, categoryDatasMap, dataSetAlias, data);
				
				//设置每个坐标系的min、max、data
				for(let j=0; j<data.length; j++)
				{
					let vs = (data[j].value || []);
					
					for(let k=0; k<parallelAxis.length; k++)
					{
						let paxis = parallelAxis[k];
						let pv = vs[k];
						
						if(paxis.type == "category")
						{
							paxis.data = (paxis.data || (paxis.data = []));
							if(pv != null)
								SPT.appendDistinct(paxis.data, pv);
						}
						else
						{
							if(pv != null)
							{
								//设置min、max，不然当多系列时不能自动识别，可能导致某些线飞离
								if(paxis.min == null)
									paxis.min = pv;
								else if(paxis.min > pv)
									paxis.min = pv;
								
								if(paxis.max == null)
									paxis.max = pv;
								else if(paxis.max < pv)
									paxis.max = pv;
							}
						}
					}
				}
			}
			
			var legendData = [];
			var series = [];
			
			for(let i=0; i<categoryNames.length; i++)
			{
				let categoryName = categoryNames[i];
				legendData.push({ name: categoryName });
				let mySeries = { name: categoryName, data: categoryDatasMap[categoryName] };
				series.push(mySeries);
				this._configSingleSeries(chart, mySeries);
			}
			
			var options = { legend: { data: legendData }, parallelAxis: parallelAxis, series: series };
			this._trimAxisMinMax(options);
			options = EU.prepareUpdateOptions(chart, options);
			
			EU.setOptionsReplaceMerge(chart, options);
		},
		
		_configSingleSeries: function(chart, series)
		{
			series.type = "parallel";
			
			if(config.smooth)
				series.smooth = true;
		},
		
		_evalParallelAxis: function(chart, dataSetBinds)
		{
			var parallelAxis = [];
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let valueFields = chart.dataSetFieldsOfSign(dataSetBind, 1);
				
				for(let j=0; j<valueFields.length; j++)
				{
					let valueField = valueFields[j];
					//使用alias而非name作为坐标轴名，因为alias是可编辑得，使得用户可以自定义坐标轴
					let axisName = chart.dataSetFieldAlias(dataSetBind, valueField);
					
					if(SPT.findInArray(parallelAxis, axisName, "name") < 0)
					{
						let axis =
						{
							name: axisName,
							type: SPT.evalDataSetFieldAxisType(chart, valueField)
						};
						
						parallelAxis.push(axis);
					}
				}
			}
			
			for(let i=0; i<parallelAxis.length; i++)
				parallelAxis[i].dim = i;
			
			return parallelAxis;
		},
		
		_evalValueFieldNamess: function(chart, dataSetBinds, parallelAxis)
		{
			var valueFieldNamess = [];
			
			//平行坐标系的series[i].value数组需要与坐标系个数对齐，不存在的槽位需使用占位字段名使得后续取值为null
			var placeholderName = (this._placeholderName != null ? this._placeholderName
							: (this._placeholderName = CF.uid()+"FieldNamePlaceholder"));
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let valueFieldNames = [];
				let dataSetBind = dataSetBinds[i];
				let valueFields = chart.dataSetFieldsOfSign(dataSetBind, 1);
				
				for(let j=0; j<parallelAxis.length; j++)
				{
					let idx = SPT.findInArray(valueFields, parallelAxis[j].name,
								function(valueField)
								{
									return chart.dataSetFieldAlias(dataSetBind, valueField);
								});
					
					valueFieldNames[j] = (idx < 0 ? placeholderName : valueFields[idx].name);
				}
				
				valueFieldNamess[i] = valueFieldNames;
			}
			
			return valueFieldNamess;
		},
		
		_trimAxisMinMax: function(options)
		{
			var parallelAxis = (options.parallelAxis || []);
			var series = (options.series || []);
			
			for(let i=0; i<parallelAxis.length; i++)
			{
				let pa = parallelAxis[i];
				
				//单系列ECharts会自动计算min、max，这里不必设置
				if(series.length < 2)
				{
					pa.min = undefined;
					pa.max = undefined;
				}
				//多系列ECharts不会自动计算，需要手动计算
				else
				{
					SPT.trimNumberRange(pa);
				}
			}
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//主题河流图

SPT.themeRiverRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		render: function(chart)
		{
			var dataSetBind = chart.dataSetBindMain();
			var nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
			
			var options =
			{
				title: { text: chart.name() },
				tooltip: { trigger: "axis" },
				legend: { data: [] },
				singleAxis:
				{
					name: chart.dataSetFieldAlias(dataSetBind, nameField),
					type: SPT.evalDataSetFieldAxisType(chart, nameField),
					data: [],
					//ECharts-6.0版本主题配置不起作用，所以这里配置
					left: "10%",
		            top: "20%",
		            right: "10%",
		            bottom: "10%"
				},
				series: [ { type: "themeRiver", data: [] } ]
			};
			
			options = EU.prepareRenderOptions(chart, options);
			var instance = EU.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var legendData = [];
			var seriesName = "";
			var seriesData = [];
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let dataSetAlias = chart.dataSetAlias(dataSetBind);
				let result = chart.resultOf(chartResult, dataSetBind);
				
				if(CF.isEmpty(seriesName))
					seriesName = dataSetAlias;
				
				let nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
				let categoryField = chart.dataSetFieldOfSign(dataSetBind, 2);
				
				if(categoryField)
				{
					let valueField = chart.dataSetFieldOfSign(dataSetBind, 1);
					
					//主题河流图只支持[name, value, category]格式的数据条目
					let data = chart.resultRowArrayDatas(result, [ nameField, valueField, categoryField ]);
					//即使这里设置了originalData，由于其结构是数组，在绑定了click事件处理函数后，事件对象的data中仍会丢失originalData，暂时无解决办法
					SPT.originalDataOfResult(data, chart, result);
					
					//为类别添加前缀，确保多数据集类别不重复
					for(let j=0; j<data.length; j++)
					{
						let myCategory = SPT.legendNameForDataCategory(dataSetBinds, dataSetAlias, data[j][2]);
						data[j][2] = myCategory;
						
						SPT.appendDistinct(legendData, { name: myCategory });
					}
					
					SPT.appendElement(seriesData, data);
				}
				else
				{
					let valueFields = chart.dataSetFieldsOfSign(dataSetBind, 1);
					
					for(let j=0; j<valueFields.length; j++)
					{
						let legendName = SPT.legendNameForDataValues(chart, dataSetBinds, dataSetBind, dataSetAlias, valueFields, j);
						
						//主题河流图只支持[name, value, lengendName]格式的数据条目
						let data = chart.resultRowArrayDatas(result, [ nameField, valueFields[j] ]);
						for(let k=0; k<data.length; k++)
							data[k].push(legendName);
						
						//即使这里设置了originalData，由于其结构是数组，在绑定了click事件处理函数后，事件对象的data中仍会丢失originalData，暂时无解决办法
						SPT.originalDataOfResult(data, chart, result);
						
						SPT.appendDistinct(legendData, { name: legendName });
						SPT.appendElement(seriesData, data);
					}
				}
			}
			
			var singleSeries = { name: seriesName, type: "themeRiver", data: seriesData };
			//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
			var options = { legend: { data: legendData }, series: [ singleSeries ], singleAxis: {} };
			EU.inflateUpdateAxisData(chart, options, options.singleAxis, EU.axisDataExtractors.valueElement0());
			options = EU.prepareUpdateOptions(chart, options);
			
			EU.setOptionsReplaceMerge(chart, options);
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//象形柱图

SPT.pictorialBarSymbolPaths=
{
	//星型
	"star" : "path://m15.5,19c-0.082,0 -0.164,-0.02 -0.239,-0.061l-5.261,-2.869l-5.261,2.869c-0.168,0.092 -0.373,0.079 -0.529,-0.032s-0.235,-0.301 -0.203,-0.49l0.958,-5.746l-3.818,-3.818c-0.132,-0.132 -0.18,-0.328 -0.123,-0.506s0.209,-0.31 0.394,-0.341l5.749,-0.958l2.386,-4.772c0.085,-0.169 0.258,-0.276 0.447,-0.276s0.363,0.107 0.447,0.276l2.386,4.772l5.749,0.958c0.185,0.031 0.337,0.162 0.394,0.341s0.01,0.374 -0.123,0.506l-3.818,3.818l0.958,5.746c0.031,0.189 -0.048,0.379 -0.203,0.49c-0.086,0.061 -0.188,0.093 -0.29,0.093z",
};

SPT.pictorialBarRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		//图形类型
		symbol: "circle",
		//是否互换坐标轴
		interchangeAxis: false
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		render: function(chart)
		{
			var dataSetBind = chart.dataSetBindMain();
			var nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
			var valueField = chart.dataSetFieldOfSign(dataSetBind, 1);
			
			var options =
			{
				title: { text: chart.name() },
				tooltip: { trigger: "item" },
				legend: { data: [] },
				xAxis:
				{
					name: chart.dataSetFieldAlias(dataSetBind, nameField),
					type: SPT.evalDataSetFieldAxisType(chart, nameField),
					splitLine: { show: false }
				},
				yAxis:
				{
					name: chart.dataSetFieldAlias(dataSetBind, valueField),
					type: SPT.evalDataSetFieldAxisType(chart, valueField)
				},
				series:
				[
					{ type: "pictorialBar", data: [] }
				]
			};
			
			if(config.interchangeAxis)
			{
				let xAxisTmp = options.xAxis;
				options.xAxis = options.yAxis;
				options.yAxis = xAxisTmp;
			}
			
			options = EU.prepareRenderOptions(chart, options);
			var instance = EU.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var legendData = [];
			var series = [];
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let dataSetAlias = chart.dataSetAlias(dataSetBind);
				let result = chart.resultOf(chartResult, dataSetBind);
				
				let nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
				let categoryField = chart.dataSetFieldOfSign(dataSetBind, 2);
				
				if(categoryField)
				{
					let valueField = chart.dataSetFieldOfSign(dataSetBind, 1);
					let categoryNames = [];
					let categoryDatasMap = {};
					
					//使用{name,value:[]}格式可以更好地兼容category、value、time坐标轴类型以及tooltip、事件数据
					let fieldMap = { name: nameField, value: [nameField, valueField] };
					fieldMap = SPT.addCategoryToFieldMap(fieldMap, categoryField);
					let data = chart.resultMapDatas(result, fieldMap);
					SPT.originalDataOfResult(data, chart, result);
					SPT.splitDataByCategory(data, categoryNames, categoryDatasMap);
					
					for(let j=0; j<categoryNames.length; j++)
					{
						let categoryName = categoryNames[j];
						let legendName = SPT.legendNameForDataCategory(dataSetBinds, dataSetAlias, categoryName);
						let mySeries = { name: legendName, data: categoryDatasMap[categoryName] };
						
						this._configSingleSeries(chart, mySeries);
						legendData.push({ name: legendName });
						series.push(mySeries);
					}
				}
				else
				{
					let valueFields = chart.dataSetFieldsOfSign(dataSetBind, 1);
					
					for(let j=0; j<valueFields.length; j++)
					{
						let legendName = SPT.legendNameForDataValues(chart, dataSetBinds, dataSetBind, dataSetAlias, valueFields, j);
						//使用{name,value:[]}格式可以更好地兼容category、value、time坐标轴类型以及tooltip、事件数据
						let fieldMap = { name: nameField, value: [nameField, valueFields[j]] };
						let data = chart.resultMapDatas(result, fieldMap);
						SPT.originalDataOfResult(data, chart, result);
						let mySeries = { name: legendName, data: data };
						
						this._configSingleSeries(chart, mySeries);
						legendData.push({ name: legendName });
						series.push(mySeries);
					}
				}
			}
			
			var options = { legend: { data: legendData }, series: series };
			//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
			(config.interchangeAxis ? (options.yAxis = {}) : (options.xAxis = {}));
			
			this._inflateUpdateOptions(chart, options);
			EU.inflateUpdateAxisData(chart, options, (config.interchangeAxis ? options.yAxis : options.xAxis),
							EU.axisDataExtractors.valueElement0());
			options = EU.prepareUpdateOptions(chart, options, (options) => { EU.adaptValueArrayData(chart, options, "pictorialBar"); });
			
			EU.setOptionsReplaceMerge(chart, options);
		},
		
		_inflateUpdateOptions: function(chart, options)
		{
			var series = options.series;
			var symbolSize = (series.length > 1 ? "100%" : "50%");
			var barGap = (series.length > 1 ? "50%" : "100%");
			
			for(let i=0; i<series.length; i++)
			{
				series[i].symbolSize = symbolSize;
				series[i].barGap = barGap;
			}
		},
		
		_configSingleSeries: function(chart, series)
		{
			var symbol = config.symbol;
			if(SPT.pictorialBarSymbolPaths[symbol])
				symbol = SPT.pictorialBarSymbolPaths[symbol];
			
			series.type = "pictorialBar";
			series.encode = (config.interchangeAxis ? { x: 1, y: 0 } : { x: 0, y: 1 });
			series.symbol = symbol;
			series.symbolRepeat = true;
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//象形进度柱图

SPT.pictorialBarProgressRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		//是否互换坐标轴
		interchangeAxis: false,
		//最大值
		max: 100,
		//系列配置
		series: {}
	},
	config);
	
	var renderer =
	{
		depend: SPT.ECHARTS_RENDERER_DEPEND,
		render: function(chart)
		{
			var dataSetBind = chart.dataSetBindMain();
			var nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
			var valueField = chart.dataSetFieldOfSign(dataSetBind, 1);
			
			var options =
			{
				title: { text: chart.name() },
				tooltip: { trigger: "item" },
				legend: { data: [] },
				xAxis:
				{
					name: chart.dataSetFieldAlias(dataSetBind, nameField),
					type: SPT.evalDataSetFieldAxisType(chart, nameField),
					splitLine: { show: false }
				},
				yAxis:
				{
					name: chart.dataSetFieldAlias(dataSetBind, valueField),
					type: SPT.evalDataSetFieldAxisType(chart, valueField),
					splitLine: { show: false }
				},
				series:
				[
					{ type: "pictorialBar", data: [] }
				]
			};
			
			if(config.interchangeAxis)
			{
				let xAxisTmp = options.xAxis;
				options.xAxis = options.yAxis;
				options.yAxis = xAxisTmp;
			}
			
			options = EU.prepareRenderOptions(chart, options);
			var instance = EU.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var legendData = [];
			var seriesName = "";
			var seriesData = [];
			var maxValue = null;
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let dataSetAlias = chart.dataSetAlias(dataSetBind);
				let result = chart.resultOf(chartResult, dataSetBind);
				
				if(CF.isEmpty(seriesName))
					seriesName = dataSetAlias;
				
				let nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
				let valueField = chart.dataSetFieldOfSign(dataSetBind, 1);
				let maxField = chart.dataSetFieldOfSign(dataSetBind, 2);
				
				//使用{name,value:[]}格式可以更好地兼容category、value、time坐标轴类型以及tooltip、事件数据
				let fieldMap = { name: nameField, value: [nameField, valueField] };
				let data = chart.resultMapDatas(result, fieldMap);
				SPT.originalDataOfResult(data, chart, result);
				
				seriesData = seriesData.concat(data);
				
				if(maxField != null)
				{
					let maxValues = chart.resultColumnArrayDatas(result, maxField);
					for(let j=0; j<maxValues.length; j++)
					{
						let mvj = maxValues[j];
						maxValue = (maxValue == null ? mvj : (mvj == null ? maxValue : Math.max(maxValue, mvj)));
					}
				}
			}
			
			maxValue = (maxValue == null ? config.max : maxValue);
			
			let series0 = { name: seriesName, data: seriesData, symbolClip: true, z: 10 };
			this._configSingleSeries(chart, series0, config.series0, maxValue);
			legendData.push({ name: seriesName });
			
			let series1 =
			{
				name: seriesName+"-background", data: seriesData, symbolClip: false, z: 1,
				animationDuration: 0, itemStyle:{ color: chart.themeGradualColor(0.2) }, silent: true
			};
			this._configSingleSeries(chart, series1, config.series1, maxValue);
			
			var options = { legend: { data: legendData }, series: [ series0, series1 ] };
			//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
			if(config.interchangeAxis)
			{
				options.xAxis = { max: maxValue };
				options.yAxis = {};
			}
			else
			{
				options.xAxis = {};
				options.yAxis = { max: maxValue };
			}
			
			EU.inflateUpdateAxisData(chart, options, (config.interchangeAxis ? options.yAxis : options.xAxis),
							EU.axisDataExtractors.valueElement0());
			options = EU.prepareUpdateOptions(chart, options, (options) => { EU.adaptValueArrayData(chart, options, "pictorialBar"); });
			
			EU.setOptionsReplaceMerge(chart, options);
		},
		
		_configSingleSeries: function(chart, series, seriesTemplate, maxValue)
		{
			series = CF.extend(true, series, seriesTemplate);
			
			if(SPT.pictorialBarSymbolPaths[series.symbol])
				series.symbol = SPT.pictorialBarSymbolPaths[series.symbol];
			
			series.type = "pictorialBar";
			series.encode = (config.interchangeAxis ? { x: 1, y: 0 } : { x: 0, y: 1 });
			series.barGap = "-100%";
			series.symbolBoundingData = maxValue;
		}
	};
	
	EU.inflateRendererCommons(renderer);
	return renderer;
};

//表格

SPT.tableRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
	},
	config);
	
	var renderer =
	{
		depend:
		{
			name: "DataTable",
			version: "2.3.1",
			acceptVersion: ">=2.0",
			source:
			[
				"lib/DataTables-2.3.1/datatables.min.css",
				"lib/DataTables-2.3.1/datatables.min.js"
			],
			depend: { name: "jQuery", acceptVersion: ">=1.8" }
		},
		render: function(chart)
		{
			var chartEle = jQuery(chart.element());
			chartEle.addClass("dg-chart-table");
			
			var columns = this._getFieldColumns(chart);
			
			if(columns.length == 0)
				throw new Error("Column required for rendering table in chart '"+chart.name()+"'");
			
			var options =
			{
				//标题配置
				title:
				{
					show: true,
					text: chart.name(),
					//样式，格式支持："color:red;background-color:blue;"、{ color:'red', 'background-color':'blue' }
					style: undefined
				},
				
				//表格样式，每一项都支持CSS字符串或CSS对象：
				//{
				//	table: ...,
				//	head: { row: ... },
				//	body:
				//	{
				//		row: ..., rowOdd: ..., rowEven: ..., rowHover: ..., rowSelected: ...
				//	},
				//	foot: { row: ... }
				//}
				tableStyle: undefined,
				//自定义单元格渲染函数，格式为：function(value, name, rowIndex, columnIndex, row, meta){ return ...; }
				renderCell: undefined,
				//轮播，格式可以为：true、false、轮播interval数值、轮播interval返回函数、{...}
				carousel: undefined,
				//是否禁用条纹样式效果
				disableStripe: false,
				//是否禁用悬浮样式效果
				disableHover: false,
				//是否禁用文本不换行
				disableWrapText: false,
				
				//DataTable配置项
				columns: columns,
				data: [],
				ordering: false,
				scrollX: true,
				scrollY: undefined,
				autoWidth: true,
		        scrollCollapse: false,
				pagingType: "full_numbers",
				lengthMenu: [],
				pageLength: 50,
				select : { style : "single", toggleable: false },
				searching : false,
				language:
			    {
					emptyTable: "",
					zeroRecords: "",
					search: "搜索",
					lengthMenu: "每页_MENU_条",
					info: "共_TOTAL_条，当前_START_-_END_条",
					infoEmpty: "无数据",
					infoFiltered: "_TOTAL_条",
					loadingRecords: "加载中...",
					paginate:
					{
						first: "首页",
						last: "尾页",
						next: "下一页",
						previous: "上一页"
					},
					select:
					{
						rows: ""
					}
				}
			};
			
			chart.inflateOptions(options);
			this._processRenderOptions(chart, options);
			SPT.processRenderOptions(chart, options);
			
			this._themeStyleSheet(chart, options);
			
			var carousel = this._carouselOption(options);
			
			if(carousel.enable)
				chartEle.addClass("dg-chart-table-carousel");
			
			if(!options.disableWrapText)
				chartEle.addClass("dg-text-nowrap");
			
			var containerEle = jQuery("<div class='dg-chart-container' />").appendTo(chartEle);
			var chartTitle = jQuery("<div class='dg-chart-table-title' />").appendTo(containerEle);
			var chartContent = jQuery("<div class='dg-chart-table-content' />").appendTo(containerEle);
			var table = jQuery("<table width='100%' class='"+(options.disableStripe ? "" : " stripe ")+(options.disableHover ? "" : " hover ")+"'></table>")
							.attr("id", "table"+chart.id()).appendTo(chartContent);
			
			this._renderTitleIfSet(chart, options, chartEle, chartTitle);
			
			table.dataTable(options);
			var dataTable = table.DataTable();
			
			if(carousel.enable && carousel.hideVerticalScrollbar != false)
			{
				var tableBody = this._getScrollBody(chart, chartContent);
				tableBody.css("overflow-y", "hidden");
			}
			
			if(carousel.enable)
			{
				jQuery(dataTable.table().body()).on("mouseenter", "tr", () =>
				{
					if(carousel.pauseOnHover)
						this._stopCarousel(chart);
				})
				.on("mouseleave", "tr", () =>
				{
					if(carousel.pauseOnHover)
						this._startCarousel(chart);
				});
			}
			
			chart.internal(dataTable);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var options = { data: [] };
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let result = chart.resultOf(chartResult, dataSetBind);
				let resultDatas = chart.resultDatas(result);
				
				for(let j=0; j<resultDatas.length; j++)
				{
					//这里直接使用原始数据，所以不需要设置SPT.originalDataOfData()，也无需使用深度复制后的数据
					var data = resultDatas[j];
					options.data.push(data);
				}
			}
			
			this._stopCarousel(chart);
			
			options = chart.inflateOptions(options);
			SPT.processUpdateOptions(chart, options);
			
			this._renderTitleIfSet(chart, options);
			this._updateInternalData(chart, chartResult, options);
		},
		
		resize: function(chart)
		{
			var dataTable = chart.internal();
			this._adjustColumn(dataTable);
		},
		
		destroy: function(chart)
		{
			var chartEle = jQuery(chart.element());
			
			this._stopCarousel(chart);
			chart.internal().destroy(true);
			chartEle.removeClass("dg-chart-table dg-hide-title dg-text-nowrap dg-chart-table-carousel");
			chartEle.removeClass(chart.liveData(CF.builtinPropName("TableChartLocalStyleName")));
			$(".dg-chart-container", chartEle).remove();
		},
		
		on: function(chart, type, handler)
		{
			var internal = chart.internal();
			var delegate;
			
			var actualType = SPT.actualEventTypeForData(type);
			if(this._isSupportEventTypeForData(actualType))
			{
				type = actualType;
				delegate = function(e, dt, type, indexes)
				{
					//暂时仅支持行级事件
					if (type !== "row")
						return;
					
					if(CF.isEmpty(indexes))
						return;
					
					var data = [];
					
					for(let i=0; i<indexes.length; i++)
						data.push(dt.row(indexes[i]).data());
					
					//单选应仅设置单行数据
					var dtInit = dt.init();
					if(dtInit.select === "single" || (dtInit.select && dtInit.select.style === "single"))
						data = data[0];
					
					SPT.eventData(e, data);
					return SPT.invokeEventHandler(chart, handler, arguments);
				};
			}
			else
			{
				delegate = function()
				{
					return SPT.invokeEventHandler(chart, handler, arguments);
				};
			}
			
			chart.registerEventHandlerDelegate(type, handler, delegate);
			internal.on(type, delegate);
		},
		
		off: function(chart, type, handler)
		{
			var internal = chart.internal();
			type = SPT.actualEventTypeForData(type, type);
			
			var delegates = chart.removeEventHandlerDelegate((d) =>
			{
				return SPT.eventHandlerDelegateFilter(d, type, handler);
			});
			
			delegates.forEach((d) =>
			{
				internal.off(d.type, d.delegate);
			});
		},
		
		additions:
		{
			defaultLinkEventType: "select.data"
		},
		
		_isSupportEventTypeForData: function(type)
		{
			return ("select" == type || "deselect" == type);
		},
		
		_renderTitleIfSet: function(chart, options, chartEle, titleEle)
		{
			var title = (options ? options.title : undefined);
			
			if(title == null)
				return;
			
			chartEle = (chartEle == null ? jQuery(chart.element()) : jQuery(chartEle));
			titleEle = (titleEle == null ? jQuery(".dg-chart-table-title", chartEle) : jQuery(titleEle));
			
			if(title.show !== undefined)
			{
				if(!title.show)
					chartEle.addClass("dg-hide-title");
				else
					chartEle.removeClass("dg-hide-title");
			}
			
			if(title.text !== undefined)
				titleEle.html(title.text != null ? title.text : "");
			
			if(title.style !== undefined)
				CF.eleStyle(titleEle[0], title.style);
		},
		
		_getFieldColumns: function(chart)
		{
			var columns = [];
			
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart);
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let fields = chart.dataSetFieldsOfSign(dataSetBind, 0);
				if(!fields || fields.length == 0)
					fields = chart.dataSetFields(dataSetBind);
				
				for(let j=0; j<fields.length; j++)
				{
					let field = fields[j];
					let colIdx = SPT.findInArray(columns, field.name, "name");
					
					if(colIdx < 0)
					{
						let column =
						{
							title: chart.dataSetFieldAlias(dataSetBind, field),
							name: field.name,
							data: field.name,
							defaultContent: "",
							orderable: true,
							searchable: false,
							//需后续完善
							render: undefined
						};
						
						columns.push(column);
					}
				}
			}
			
			return columns;
		},
		
		_processRenderOptions: function(chart, options)
		{
			this._processServerSidePagingOptions(chart, options);
			this._processCarouselOptions(chart, options)
			
			//必须明确设置paging=false，因为底层表格组件的paging默认值为true
			options.paging = (options.paging != null ? options.paging : false);
			
			//开启分页后，默认开启info
			options.info = (options.info != null ? options.info : options.paging);
			
			if(options.paging)
			{
				options.lengthMenu = (options.lengthMenu == null || options.lengthMenu.length == 0 ? [ 10, 25, 50, 75, 100 ] : options.lengthMenu);
				//如果有50，则取50
				options.pageLength = (CF.indexInArray(options.lengthMenu, 50) >= 0 ? 50 : options.lengthMenu[0]);
			}
			
			var dftLayout =
			{
				topStart: (options.buttons ? "buttons" : null),
				topEnd: (options.searching ? "search" : null),
				bottomStart: (options.info ? "info" : null),
				bottomEnd: (options.paging ? ["pageLength", "paging"] : null)
			};
			
			options.layout = (options.layout  == null ? dftLayout : options.layout);
			
			//填充options.columns的render函数
			for(let i=0; i<options.columns.length; i++)
			{
				let column = options.columns[i];
				
				//DataTables-1.10.18是允许column.data为""的，升级至1.11.3后则会有一个警告弹出框，
				//这里设置defaultContent可以解决此问题
				if(CF.isEmpty(column.data) && column.defaultContent == null)
					column.defaultContent = "";
				
				if(column.render == null)
				{
					column.render = function(value, type, row, meta)
					{
						//单元格展示绘制
						if(type == "display")
						{
							if(options.renderCell)
							{
								var rowIndex = meta.row;
								var columnIndex = meta.col;
								var name = options.columns[columnIndex].data;
								
								return options.renderCell(value, name, rowIndex, columnIndex, row, meta);
							}
							else
								return CF.escapeHtml(value);
						}
						//其他绘制，比如排序
						else
							return value;
					};
				}
			}
		},
		
		/**
		 * 表格处理carousel选项，格式为：
		 * {
		 *   carousel: ...
		 * }
		 */
		_processCarouselOptions: function(chart, options)
		{
			//标准轮播格式
			var carouselObj =
			{
				//是否开启，true 开启；false 禁用；"auto" 只有在行溢出时才开启
				enable: false,
				//滚动间隔毫秒数，或者返回间隔毫秒数的函数：
				//currentRow 当前可见行
				//visibleHeight 当前可见行的剩余可见高度
				//height 当前可见行高度
				//function(currentRow, visibleHeight, height){ return ...; }
				interval: 50,
				//滚动跨度像素数，或者返回跨度像素数的函数：
				//function(currentRow, visibleHeight, height){ return ...; }
				span: 1,
				//是否在鼠标悬停时暂停轮播
				pauseOnHover: true,
				//是否隐藏纵向滚动条
				hideVerticalScrollbar: true,
				//溢出删除个数，小于这个数的轮播溢出行数，不会执行删除操作
				overflowCount: 2
			};
			
			var carousel = this._carouselOption(options);
			
			if(carousel == null)
			{
				
			}
			//true、false、"auto"
			else if(carousel === true || carousel === false || CF.isString(carousel))
			{
				carouselObj.enable = carousel;
			}
			//间隔数值、函数
			else if(CF.isNumber(carousel) || CF.isFunction(carousel))
			{
				carouselObj.enable = true;
				carouselObj.interval = carousel;
			}
			//轮播对象
			else
			{
				carouselObj = CF.extend(true, carouselObj, carousel);
			}
			
			this._carouselOption(options, carouselObj);
		},
		
		/**
		 * 表格处理serverSidePaging选项，格式为：
		 * {
		 *   serverSidePaging:
		 *   {
		 *      //必填，将data中的分页查询信息设置为图表数据集参数
		 *      param: function(data, chart){ ... },
		 *      //可选（与totalFieldName、totalValue三选一），数据集附加数据中总记录数关键字
		 *      totalAdditionName: "...",
		 *      //可选（与totalAdditionName、totalValue三选一），附件数据集中总记录数字段名
		 *      totalFieldName: "...",
		 *      //可选（与totalFieldName、totalAdditionName三选一），附件数据集中总记录数字段名
		 *      totalValue: function(chart){ return 数值; },
		 *      //可选，根据图表数据集参数设置表格分页状态，或者返回要设置的状态数据（参考SPT.tableUpdatePagingState()函数），
		 *      //如果不设置，使用图表参数面板的查询信息不会同步显示到表格中
		 *      state: function(chart){ ... },
		 *      //可选，触发表格draw()函数时的paging参数
		 *      drawPagingArg: ...、function(chart){ return ...; },
		 *   }
		 * }
		 */
		_processServerSidePagingOptions: function(chart, options)
		{
			var serverSidePaging = this._serverSidePagingOption(options);
			
			if(!serverSidePaging)
				return;
			
			options.serverSide = true;
			options.paging = true;
			
			//这里需禁用轮播，详细参考SPT.tableStartCarousel()函数
			this._carouselOption(options, false);
			
			var thisRenderer = this;
			
			options.ajax = function(data, callback, settings)
			{
				var ajaxInfos = chart.liveData("serverSidePagingAjaxInfos");
				if(ajaxInfos == null)
				{
					ajaxInfos = [];
					chart.liveData("serverSidePagingAjaxInfos", ajaxInfos);
				}
				
				ajaxInfos.push({ data: data, callback: callback, settings: settings });
				
				var refreshInfo = chart.liveData("serverSidePagingRefreshInfo");
				
				//由图表API触发，此时已获取到数据，不应再执行chart.refresh()函数
				if(refreshInfo != null)
				{
					chart.liveData("serverSidePagingRefreshInfo", null);
					
					if(chart.isActive())
						thisRenderer._updateInternalData(chart, refreshInfo.chartResult, refreshInfo.updateOptions);
				}
				else
				{
					serverSidePaging.param(data, chart);
					
					if(chart.isActive())
						chart.refresh();
				}
			};
			
			this._updateInternalOption(options, function(updateOptions, chart, chartResult)
			{
				var ajaxInfos = (chart.liveData("serverSidePagingAjaxInfos") || []);
				
				//由表格内部操作触发
				if(ajaxInfos.length > 0)
				{
					for(var i=0; i<ajaxInfos.length; i++)
					{
						var ajaxInfo = ajaxInfos[i];
						var recordsTotal = thisRenderer._getRecordsTotal(updateOptions, chart, chartResult, serverSidePaging);
						
						var pagingData =
						{
							draw: (ajaxInfo.data ? ajaxInfo.data.draw : undefined),
							recordsTotal: recordsTotal,
							recordsFiltered: recordsTotal,
							data: updateOptions.data
						};
						
						ajaxInfo.callback(pagingData);
					}
					
					chart.liveData("serverSidePagingAjaxInfos", []);
				}
				//由图表API触发，比如：参数表单提交、chart.refresh()
				else
				{
					var pagingState = (serverSidePaging.state == null ? null : serverSidePaging.state(chart));
					if(pagingState != null)
						thisRenderer._updatePagingState(chart, pagingState);
					
					var refreshInfo = { updateOptions: updateOptions, chartResult: chartResult };
					chart.liveData("serverSidePagingRefreshInfo", refreshInfo);
					
					var drawPagingArg = (serverSidePaging.drawPagingArg == null ? false : serverSidePaging.drawPagingArg);
					if(CF.isFunction(drawPagingArg))
						drawPagingArg = serverSidePaging.drawPagingArg(chart);
					
					chart.internal().draw(drawPagingArg);
				}
			});
		},
		
		/**
		 * 表格更新分页状态（不应刷新数据），state格式为：
		 * {
		 *   //可选，页大小
		 *   length: 数值,
		 *   //可选，页码（以0开始）
		 *   page: 数值,
		 *   //可选，页数据起始索引
		 *   start: 数值,
		 *   //可选，搜索关键字
		 *   searchValue: "...",
		 *   //可选，排序
		 *   order: "..."
		 * }
		 */
		_updatePagingState: function(chart, state)
		{
			if(!state)
				return;
			
			var dataTable = chart.internal();
			
			var pageLength = (state.length == null ? null : parseInt(state.length));
			var pagePage = (state.page == null ? null : parseInt(state.page));
			var pageStart = (state.start == null ? null : parseInt(state.start));
			
			if(pageLength != null)
				dataTable.page.len(pageLength);
			
			if(pagePage != null)
			{
				dataTable.page(pagePage);
			}
			else if(pageStart != null)
			{
				pageLength = (pageLength == null ? dataTable.page.info() : pageLength);
				pagePage = parseInt(pageStart/pageLength);
				dataTable.page(pagePage);
			}
			
			if(state.searchValue !== undefined)
			{
				dataTable.search(state.searchValue == null ? "" : state.searchValue);
			}
			
			if(state.order !== undefined)
			{
				dataTable.order(state.order);
			}
		},
		
		_getRecordsTotal: function(updateOptions, chart, chartResult, serverSidePaging)
		{
			var recordsTotal = null;
			
			if(serverSidePaging.totalValue != null)
			{
				recordsTotal = serverSidePaging.totalValue(chart);
			}
			else
			{
				var dsbs = chart.dataSetBinds();
				
				for(var i=0; i<dsbs.length; i++)
				{
					var result = chart.resultOf(chartResult, dsbs[i]);
					
					if(serverSidePaging.totalAdditionName != null)
					{
						recordsTotal = chart.resultAddition(result, serverSidePaging.totalAdditionName);
					}
					
					if(recordsTotal == null && serverSidePaging.totalFieldName != null
						&& chart.dataSetField(dsbs[i], serverSidePaging.totalFieldName) != null)
					{
						var colValues = chart.resultColumnArrayDatas(result, serverSidePaging.totalFieldName);
						recordsTotal = SPT.findNonNull(colValues);
					}
					
					if(recordsTotal != null)
						break;
				}
			}
			
			if(recordsTotal == null)
				recordsTotal = (updateOptions.data ? updateOptions.data.length : 0);
			
			return recordsTotal;
		},
		
		_getChartContent: function(chart)
		{
			return jQuery(".dg-chart-table-content", chart.element());
		},
		
		_getScrollHead: function(chart, $chartContent)
		{
			return jQuery(".dt-scroll-head", $chartContent);
		},
		
		_getScrollBody: function(chart, $chartContent)
		{
			return jQuery(".dt-scroll-body", $chartContent);
		},
		
		_themeStyleSheet: function(chart, options)
		{
			var name = CF.builtinPropName("TableChart");
			var isLocalStyle = (options.tableStyle != null);
			var forceUpdate = false;
			
			if(isLocalStyle)
			{
				//这里不应使用随机数，因为在图表多次destroy再init后，会导致残留无法销毁的样式表DOM
				name = "tableStyle" + chart.id();
				//需强制为每次都更新样式表，因为绑定的图表主题可能是全局主题
				forceUpdate = true;
				
				jQuery(chart.element()).addClass(name);
				chart.liveData(CF.builtinPropName("TableChartLocalStyleName"), name);
			}
			
			chart.themeStyleSheet(name, () =>
			{
				var theme = chart.theme();
				
				var rowBgColor ="rgba(0,0,0,0)";
				var rowOddBgColor = chart.themeGradualColor(0.5) + "0F";
				var borderColor = chart.themeGradualColor(0.4);
				
				var tableStyle =
				{
					table: {},
					head:
					{
						row:
						{
							"color": theme.color,
							//必须设置背景色，不然会是组件默认背景色无法适配图表主题
							"background-color": rowBgColor
						}
					},
					body:
					{
						row:
						{
							"color": theme.color,
							//必须设置背景色，不然会是组件默认背景色无法适配图表主题
							"background-color": rowBgColor
						},
						rowOdd:
						{
							//必须设置背景色，不然会是组件默认背景色无法适配图表主题
							"background-color": rowOddBgColor
						},
						rowEven:
						{
							//必须设置背景色，不然会是组件默认背景色无法适配图表主题
							"background-color": rowBgColor
						},
						rowHover:
						{
							"background-color": chart.themeGradualColor(0.1)
						},
						rowSelected:
						{
							"color": theme.color,
							"background-color": chart.themeGradualColor(0.3)
						}
					},
					foot:
					{
						row:
						{
							"color": theme.color,
							//必须设置背景色，不然会是组件默认背景色无法适配图表主题
							"background-color": rowBgColor
						}
					},
				};
				
				if(isLocalStyle)
				{
					let optionTableStyle = CF.extend(true, {}, options.tableStyle);
					this._trimTableStyleOption(optionTableStyle);
					tableStyle = CF.extend(true, tableStyle, optionTableStyle);
				}
				
				//样式应加".dg-chart-table-content"限定
				var qualifier = (isLocalStyle ? "." + name : "") + " .dg-chart-table-content";
				
				var css=
				[
					{
						name: (isLocalStyle ? "." + name : "") + " .dg-chart-table-title",
						value: { "font-size": CF.toCssFontSize(theme.titleTheme.fontSize) }
					},
					{
						name: qualifier + " table.dataTable",
						value: CF.styleString(tableStyle.table)
					},
					{
						name: qualifier + " table.dataTable thead tr",
						value: CF.styleString(tableStyle.head.row)
					},
					{
						name: qualifier + " table.dataTable tfoot tr",
						value: CF.styleString(tableStyle.foot.row)
					},
					{
						name: qualifier + " table.dataTable tbody tr",
						value: CF.styleString(tableStyle.body.row)
					},
					{
						name: qualifier + " table.dataTable.stripe>tbody>tr:nth-child(odd)",
						value: CF.styleString(tableStyle.body.rowOdd)
					},
					{
						name: qualifier + " table.dataTable.stripe>tbody>tr:nth-child(even)",
						value: CF.styleString(tableStyle.body.rowEven)
					},
					{
						name:
						[
							qualifier + " table.dataTable.hover tbody tr:hover",
							qualifier + " table.dataTable.hover.stripe>tbody>tr:nth-child(odd):hover",
							qualifier + " table.dataTable.hover.stripe>tbody>tr:nth-child(even):hover"
						],
						value: CF.styleString(tableStyle.body.rowHover)
					},
					{
						name:
						[
							qualifier + " table.dataTable tbody tr.selected",
							qualifier + " table.dataTable.hover tbody tr:hover.selected",
							qualifier + " table.dataTable.stripe>tbody>tr:nth-child(odd).selected",
							qualifier + " table.dataTable.display>tbody>tr:nth-child(odd).selected",
							qualifier + " table.dataTable.stripe>tbody>tr:nth-child(even).selected",
							qualifier + " table.dataTable.display>tbody>tr:nth-child(even).selected",
							qualifier + " table.dataTable.hover tbody tr:hover.selected",
							qualifier + " table.dataTable.hover.stripe>tbody>tr:nth-child(odd):hover.selected",
							qualifier + " table.dataTable.hover.stripe>tbody>tr:nth-child(even):hover.selected"
						],
						value: CF.styleString(tableStyle.body.rowSelected)
					},
					{
						name: qualifier + " .dt-container .dt-length select",
						value: { color: theme.color }
					},
					{
						name: qualifier + " .dt-container .dt-length select option",
						value: { color: theme.color, "background-color": chart.themeGradualColor(0) }
					},
					{
						name: qualifier + " .dt-container .dt-scroll-body",
						value: { color: theme.color }
					},
					{
						name:
						[
							qualifier + " table.dataTable>thead>tr>th",
							qualifier + " table.dataTable>thead>tr>td"
						],
						value: { "border-bottom-color": chart.themeGradualColor(0) }
					},
					{
						name: qualifier + " table.dataTable.dtfc-scrolling-left tr>.dtfc-fixed-left::after",
						value: { "box-shadow": "inset 10px 0 8px -8px " + chart.themeGradualColor(0.2) }
					},
					{
						name: qualifier + " table.dataTable.dtfc-scrolling-right tr>.dtfc-fixed-right::after",
						value: { "box-shadow": "inset -10px 0 8px -8px " + chart.themeGradualColor(0.2) }
					},
					{
						name:
						[
							qualifier + " div.dt-container .dt-paging .dt-paging-button.current",
							qualifier + " div.dt-container .dt-paging .dt-paging-button.current:hover",
							qualifier + " div.dt-container .dt-input"
						],
						value: { "border-color": borderColor }
					},
					{
						name:
						[
							qualifier + " div.dt-container div.dt-buttons>.dt-button",
							qualifier + " div.dt-container div.dt-buttons>div.dt-button-split .dt-button"
						],
						value: { "border-color": borderColor }
					},
					{
						name:
						[
							qualifier + " div.dt-container div.dt-buttons>.dt-button:focus:not(.disabled)",
							qualifier + " div.dt-container div.dt-buttons>div.dt-button-split .dt-button:focus:not(.disabled)"
						],
						value: { "outline": "2px solid " + borderColor }
					}
				];
				
				if(!isLocalStyle)
				{
					css.push(
					{
						name: " .dg-chart-table-title",
						value: { "color": theme.titleTheme.color, "background-color": theme.titleTheme.backgroundColor }
					});
				}
				
				return css;
			},
			forceUpdate);
		},
		
		_trimTableStyleOption: function(tableStyle)
		{
			if(tableStyle == null)
				return;
			
			if(CF.isString(tableStyle.table))
				tableStyle.table = CF.styleStringToObj(tableStyle.table);
			
			if(tableStyle.head && CF.isString(tableStyle.head.row))
				tableStyle.head.row = CF.styleStringToObj(tableStyle.head.row);
			
			if(tableStyle.body && CF.isString(tableStyle.body.row))
				tableStyle.body.row = CF.styleStringToObj(tableStyle.body.row);
			
			if(tableStyle.body && CF.isString(tableStyle.body.rowOdd))
				tableStyle.body.rowOdd = CF.styleStringToObj(tableStyle.body.rowOdd);
			
			if(tableStyle.body && CF.isString(tableStyle.body.rowEven))
				tableStyle.body.rowEven = CF.styleStringToObj(tableStyle.body.rowEven);
			
			if(tableStyle.body && CF.isString(tableStyle.body.rowHover))
				tableStyle.body.rowHover = CF.styleStringToObj(tableStyle.body.rowHover);
			
			if(tableStyle.body && CF.isString(tableStyle.body.rowSelected))
				tableStyle.body.rowSelected = CF.styleStringToObj(tableStyle.body.rowSelected);
			
			if(tableStyle.foot && CF.isString(tableStyle.foot.row))
				tableStyle.foot.row = CF.styleStringToObj(tableStyle.foot.row);
			
			if(tableStyle.body && tableStyle.body.row)
			{
				if(tableStyle.body.rowOdd == null)
					tableStyle.body.rowOdd = CF.extend(true, {}, tableStyle.body.row);
				
				if(tableStyle.body.rowEven == null)
					tableStyle.body.rowEven = CF.extend(true, {}, tableStyle.body.row);
			}
		},
		
		_updateInternalData: function(chart, chartResult, updateOptions)
		{
			var renderOptions = SPT.liveDataRenderOptions(chart);
			
			//自定义更新底层组件数据，当启用serverSide后，需要自定义调用其ajax配置项的callback更新数据，而非这里
			//格式为：function(updateOptions, chart, chartResult){ ... }
			var updateInternal = this._updateInternalOption(renderOptions);
			if(updateInternal)
			{
				updateInternal.call(renderOptions, updateOptions, chart, chartResult);
				return;
			}
			
			var dataTable = chart.internal();
			var rows = dataTable.rows();
			var datas = updateOptions.data;
			var removeRowIndexes = [];
			var startRowIndex = 0;
			var dataIndex = 0;
			
			rows.every(function(rowIndex)
			{
				if(rowIndex < startRowIndex)
					return;
				
				if(dataIndex >= datas.length)
					removeRowIndexes.push(rowIndex);
				else
					this.data(datas[dataIndex]);
				
				dataIndex++;
			});
			
			for(; dataIndex<datas.length; dataIndex++)
				dataTable.row.add(datas[dataIndex]);
			
			dataTable.rows(removeRowIndexes).remove();
			
			dataTable.draw();
			this._adjustColumn(dataTable);
			
			if(this._carouselOption(renderOptions).enable)
			{
				let chartEle = jQuery(chart.element());
				chartEle.data("tableCarouselPrepared", false);
				this._startCarousel(chart);
			}
		},
		
		/**
		 * 调整图表表格。
		 * 当表格隐藏显示、位置调整、数据变更后，可能会出现表头、固定列错位的情况，需要重新调整。
		 */
		_adjustColumn: function(dataTable)
		{
			dataTable.columns.adjust();
			
			var initOptions = dataTable.init();
			
			if(initOptions.fixedHeader)
				dataTable.fixedHeader.adjust();
		},
		
		_prepareCarousel: function(chart)
		{
			var renderOptions = SPT.liveDataRenderOptions(chart);
			
			//此时需禁用轮播功能，不然dataTable.draw()会导致死循环
			if(renderOptions.serverSide == true || this._serverSidePagingOption(renderOptions) != null)
				return;
			
			var chartContent = this._getChartContent(chart);
			var dataTable = chart.internal();
			var rowIndexes = dataTable.rows().indexes();
			var rowCount = rowIndexes.length;
			
			//空表格
			if(rowCount == 0)
				return;
			
			var scrollBody = this._getScrollBody(chart, chartContent);
			var scrollTable = jQuery(".dataTable", scrollBody);
			
			var scrollBodyHeight = scrollBody.height();
			
			while(true)
			{
				let scrollTableHeight = scrollTable.height();
				
				//表格高度至少为容器高度两倍，保证滚动平滑
				if(scrollTableHeight >= scrollBodyHeight*2)
					break;
				
				//必须成倍添加数据，避免出现轮播次序混乱
				for(let i=0; i<rowCount; i++)
				{
					let addData = dataTable.row(rowIndexes[i]).data();
					dataTable.row.add(addData);
				}
				
				dataTable.draw();
			}
		},
		
		_startCarousel: function(chart)
		{
			var renderOptions = SPT.liveDataRenderOptions(chart);
			
			//此时需禁用轮播功能，不然dataTable.draw()会导致死循环
			if(renderOptions.serverSide == true || this._serverSidePagingOption(renderOptions) != null)
				return;
			
			var carousel = this._carouselOption(renderOptions);
			var chartEle = jQuery(chart.element());
			var chartContent = this._getChartContent(chart);
			var dataTable = chart.internal();
			var rowCount = dataTable.rows().indexes().length;
			
			var scrollBody = this._getScrollBody(chart, chartContent);
			var scrollTable = jQuery(".dataTable", scrollBody);
			
			//空表格，或者，"auto"且行数未溢出时不轮播
			if(rowCount == 0
				|| (carousel.enable == "auto" && (scrollTable.height() <= scrollBody.height())))
			{
				scrollTable.css("margin-top", "0px");
				return;
			}
			
			this._stopCarousel(chart);
			chartEle.data("tableCarouselStatus", "start");
			
			this._handleCarousel(chart, renderOptions, chartEle, dataTable, scrollBody, scrollTable);
		},
		
		_stopCarousel: function(chart)
		{
			var chartEle = jQuery(chart.element());
			chartEle.data("tableCarouselStatus", "stop");
			this._carouselIntervalId(chart, null);
		},
		
		_handleCarousel: function(chart, renderOptions, chartEle, dataTable, scrollBody, scrollTable)
		{
			if(chartEle.data("tableCarouselStatus") == "stop")
				return;
			
			var carousel = this._carouselOption(renderOptions);
			var doCarousel = true;
			
			//元素隐藏时会因为高度计算有问题导致浏览器卡死，所以隐藏式不实际执行轮播
			if(scrollBody.is(":hidden"))
				doCarousel = false;
			
			if(doCarousel)
			{
				if(chartEle.data("tableCarouselPrepared") != true)
				{
					this._prepareCarousel(chart);
					chartEle.data("tableCarouselPrepared", true)
				}
				
				//不采用设置滚动高度的方式（scrollBody.scrollTop()），因为会出现影响整个页面滚动高度的情况
				let scrollTop = parseInt(scrollTable.css("margin-top"));
				scrollTop = (Math.abs(scrollTop) || 0);
				
				let tableBody = dataTable.table().body();
				let currentRow = undefined;
				let currentRowHeight = undefined;
				let currentRowVisibleHeight = undefined;
				
				let offset = 0;
				let removeRowIndexes = [];
				let addRowDatas = [];
				let doRemove = false;
				let $checkRow = jQuery("> tr:first", tableBody);
				let tmpOffset = 0;
				
				while(true)
				{
					currentRow = $checkRow[0];
					currentRowHeight = $checkRow.outerHeight(true);
					currentRowVisibleHeight = currentRowHeight;
					
					if($checkRow.length == 0 || removeRowIndexes.length >= carousel.overflowCount)
					{
						offset += tmpOffset;
						doRemove = true;
						break;
					}
					
					tmpOffset += currentRowHeight;
					
					if(scrollTop < tmpOffset)
					{
						currentRowVisibleHeight = tmpOffset - scrollTop;
						break;
					}
					
					let dtRow = dataTable.row($checkRow);
					removeRowIndexes.push(dtRow.index());
					addRowDatas.push(dtRow.data());
					$checkRow = $checkRow.next();
				}
				
				let needDraw = false;
				
				if(doRemove && removeRowIndexes.length > 0)
				{
					dataTable.rows(removeRowIndexes).remove();
					scrollTop = scrollTop - offset;
					needDraw = true;
				}
				
				if(doRemove && addRowDatas.length > 0)
				{
					dataTable.rows.add(addRowDatas);
					needDraw = true;
				}
				
				if(needDraw)
					dataTable.draw();
				
				let span = (CF.isFunction(carousel.span) ?
						carousel.span(currentRow, currentRowVisibleHeight, currentRowHeight) : carousel.span);
				
				scrollTable.css("margin-top", (0 - (scrollTop + span))+"px");
			}
			
			var interval = null;
			
			if(!CF.isFunction(carousel.interval))
			{
				interval = carousel.interval;
			}
			else
			{
				if(doCarousel)
				{
					interval = carousel.interval(currentRow, currentRowVisibleHeight, currentRowHeight);
				}
				else
				{
					//没有执行轮播时，无法执行interval函数，所以采用默认处理间隔（同轮播默认间隔）
					interval = 50;
				}
			}
			
			var intervalId = setTimeout(() =>
			{
				this._handleCarousel(chart, renderOptions, chartEle, dataTable, scrollBody, scrollTable);
			},
			interval);
			
			this._carouselIntervalId(chart, intervalId);
		},
		
		_carouselIntervalId: function(chart, intervalId)
		{
			var chartEle = jQuery(chart.element());
			var curIntervalId = chartEle.data("tableCarouselIntervalId");
			
			if(intervalId === undefined)
				return curIntervalId;
			
			if(curIntervalId != null)
				clearInterval(curIntervalId);
			
			chartEle.data("tableCarouselIntervalId", intervalId);
		},
		
		_serverSidePagingOption: function(options, value)
		{
			return CF.optionValue(options, "serverSidePaging", value);
		},
		
		_updateInternalOption: function(options, value)
		{
			return CF.optionValue(options, "updateInternal", value);
		},
		
		_carouselOption: function(options, value)
		{
			return CF.optionValue(options, "carousel", value);
		}
	};
	
	return renderer;
};

//标签卡

SPT.labelRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		//是否标签值在标签名之前展示
		valueFirst: false,
		//是否隐藏标签名
		hideName: false
	},
	config);
	
	var renderer =
	{
		render: function(chart)
		{
			var options =
			{
				//布局，"flex-around" 居中间隔；"flex-start" 左对齐；"flex-end" 右对齐；"flex-center" 居中；"flex-between" 贴边间隔；""、null 无
				layout: "flex-around",
				//标签名、标签值是否都行内显示
				inline: false,
				//是否标签值在标签名之前展示
				valueFirst: config.valueFirst,
				//是否隐藏标签名
				hideName: config.hideName,
				//容器元素样式，格式允许：CSS字符串、CSS对象
				containerStyle: null,
				//标签条目元素公用css样式，格式允许：CSS字符串、CSS对象
				itemStyle: null,
				 //标签名元素公用css样式，格式允许：CSS字符串、CSS对象
				nameStyle: null,
				//标签值元素公用css样式，格式允许：CSS字符串、CSS对象
				valueStyle: null,
				//容器元素类名
				containerClass: null,
				//标签条目元素类名
				itemClass: null,
				//标签名元素类名
				nameClass: null,
				//标签值元素类名
				valueClass: null,
				//标签卡数据，元素结构:
				//{
				//  //可选，标签名，默认为选项值
				//  name: "...",
				//  //标签值
				//  value: ...,
				//  //标签条目元素css样式，格式允许：CSS字符串、CSS对象
				//  itemStyle: null,
				//  //标签名元素css样式，格式允许：CSS字符串、CSS对象
				//  nameStyle: null,
				//  //标签值元素css样式，格式允许：CSS字符串、CSS对象
				//  valueStyle: null,
				//  //标签条目元素类名
				//  itemClass: null,
				//  //标签名元素类名
				//  nameClass: null,
				//  //标签值元素类名
				//  valueClass: null
				//}
				data: []
			};
			
			var chartEle = chart.element();
			CF.eleAddClass(chartEle, "dg-chart-label");
			var containerEle = CF.eleCreate("div", "dg-chart-container");
			CF.eleAppend(chartEle, containerEle);
			chart.internal(containerEle);
			
			chart.inflateOptions(options);
			SPT.processRenderOptions(chart, options);
			
			if(!CF.isEmpty(options.layout))
				CF.eleAddClass(containerEle, "dg-chart-label-layout-"+options.layout);
			
			if(options.hideName)
				CF.eleAddClass(containerEle, "dg-chart-label-hide-name");
			
			if(options.inline)
				CF.eleAddClass(containerEle, "dg-chart-label-item-inline");
			
			if(!CF.isEmpty(options.containerStyle))
				CF.eleStyle(containerEle, options.containerStyle);
			
			if(!CF.isEmpty(options.containerClass))
				CF.eleAddClass(containerEle, options.containerClass);
			
			//此时不应绘制数据
			//this._drawDataOptions(chart, options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var options = { data: [] };
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let result = chart.resultOf(chartResult, dataSetBind);
				let resultDatas = chart.resultDatas(result);
				
				let nameFields = chart.dataSetFieldsOfSign(dataSetBind, 0);
				let valueFields = chart.dataSetFieldsOfSign(dataSetBind, 1);
				let hasNameField = (nameFields.length > 0);
				
				if(hasNameField && nameFields.length != valueFields.length)
					throw new Error("the [name] sign columns must be one-to-one with [value] sign columns");
				
				let namess = (hasNameField ? chart.resultRowArrayDatas(result, nameFields) : []);
				let valuess = chart.resultRowArrayDatas(result, valueFields);
				
				let vfNames = [];
				if(!hasNameField)
				{
					for(let j=0; j<valueFields.length; j++)
						vfNames[j] = chart.dataSetFieldAlias(dataSetBind, valueFields[j]);
				}
				
				for(let j=0; j<valuess.length; j++)
				{
					let values = valuess[j];
					let names = (hasNameField ? namess[j] : vfNames);
					
					for(let k=0; k<names.length; k++)
					{
						let di = { name: names[k], value: values[k] };
						SPT.originalDataOfData(di, resultDatas[j]);
						options.data.push(di);
					}
				}
			}
			
			options = chart.inflateOptions(options);
			SPT.processUpdateOptions(chart, options);
			
			this._drawDataOptions(chart, options);
		},
		
		destroy: function(chart)
		{
			var chartEle = chart.element();
			var internal = chart.internal();
			CF.eleRemove(internal);
			CF.eleRemoveClass(chartEle, "dg-chart-label");
		},
		
		on: function(chart, type, handler)
		{
			var internal = chart.internal();
			var delegate;
			
			var actualType = SPT.actualEventTypeForData(type);
			if(actualType != null)
			{
				let bindDataName = this._itemBindDataName();
				type = actualType;
				delegate = function(e)
				{
					var item = CF.eleAncestorOfSelector(e.target, ".dg-chart-label-item");
					if(item != null)
					{
						let data = CF.eleData(item, bindDataName);
						SPT.eventData(e, data);
						return SPT.invokeEventHandler(chart, handler, arguments);
					}
				};
			}
			else
			{
				delegate = function()
				{
					return SPT.invokeEventHandler(chart, handler, arguments);
				};
			}
			
			chart.registerEventHandlerDelegate(type, handler, delegate);
			internal.addEventListener(type, delegate);
		},
		
		off: function(chart, type, handler)
		{
			var internal = chart.internal();
			type = SPT.actualEventTypeForData(type, type);
			
			var delegates = chart.removeEventHandlerDelegate((d) =>
			{
				return SPT.eventHandlerDelegateFilter(d, type, handler);
			});
			
			delegates.forEach((d) =>
			{
				internal.removeEventListener(d.type, d.delegate);
			});
		},
		
		additions:
		{
			defaultLinkEventType: "click.data"
		},
		
		_drawDataOptions: function(chart, options)
		{
			var internal = chart.internal();
			var bindDataName = this._itemBindDataName();
			
			var data = (options.data || []);
			var itemEles = CF.elesOfSelector(".dg-chart-label-item", internal);
			var length = Math.max(data.length, itemEles.length);
			var valueFirst = config.valueFirst;
			
			for(let i=0; i<length; i++)
			{
				let di = data[i];
				let itemEle = itemEles[i];
				
				if(di == null && itemEle == null)
					continue;
				
				if(di == null)
				{
					CF.eleRemove(itemEle);
					continue;
				}
				
				if(itemEle == null)
				{
					itemEle = CF.eleCreate("div", "dg-chart-label-item");
					CF.eleAppend(internal, itemEle);
					CF.eleAppend(itemEle, CF.eleCreate("div", (valueFirst ? "dg-chart-label-value" : "dg-chart-label-name")));
					CF.eleAppend(itemEle, CF.eleCreate("div", (valueFirst ? "dg-chart-label-name" : "dg-chart-label-value")));
				}
				
				let nameEle = CF.eleOfSelector(".dg-chart-label-name", itemEle);
				let valueEle = CF.eleOfSelector(".dg-chart-label-value", itemEle);
				
				CF.eleAttr(itemEle, "class", "dg-chart-label-item" + (CF.isEmpty(options.itemClass) ? "" : " "+options.itemClass)
					+ (CF.isEmpty(di.itemClass) ? "" : " "+di.itemClass));
				CF.eleAttr(nameEle, "class", "dg-chart-label-name" + (CF.isEmpty(options.nameClass) ? "" : " "+options.nameClass)
					+ (CF.isEmpty(di.nameClass) ? "" : " "+di.nameClass));
				CF.eleAttr(valueEle, "class", "dg-chart-label-value" + (CF.isEmpty(options.valueClass) ? "" : " "+options.valueClass)
					+ (CF.isEmpty(di.valueClass) ? "" : " "+di.valueClass));
				
				CF.eleStyle(itemEle, options.itemStyle, di.itemStyle);
				CF.eleStyle(nameEle, options.nameStyle, di.nameStyle);
				CF.eleStyle(valueEle, options.valueStyle, di.valueStyle);
				
				CF.eleHtml(nameEle, (di.name == null ? "" : di.name));
				CF.eleHtml(valueEle, (di.value == null ? "" : di.value));
				CF.eleData(itemEle, bindDataName, di);
			}
		},
		
		_itemBindDataName: function()
		{
			return (this.__itemBindDataName != null ? this.__itemBindDataName
							: (this.__itemBindDataName = CF.uid()+"LabelItemBindData"));
		}
	};
	
	return renderer;
};

//下拉框

SPT.selectRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
		//是否多选
		multiple: false
	},
	config);
	
	var renderer =
	{
		render: function(chart)
		{
			var options =
			{
				//下拉框ID
				id: null,
				//下拉框名称
				name: null,
				//是否多选
				multiple: config.multiple,
				//可见选项数目
				size: null,
				//默认选中项：null：默认；数值或其数组，选中指定索引的选项
				selected: null,
				//前置添加的条目项，格式同data元素，或者其数组，通常用于添加默认选中项
				prepend: null,
				//下拉框是否填满父元素，"auto" 当是内联框时填满；true 是；false 否
				fillParent: "auto",
				//select框元素css样式，格式允许：CSS字符串、CSS对象
				selectStyle: null,
				//option选项元素公用css样式，格式允许：CSS字符串、CSS对象
				itemStyle: null,
				//select框元素类名
				selectClass: null,
				//option选项元素类名
				itemClass: null,
				//下拉框数据，元素格式为：
				//{
				//  //选项名，可选，默认为选项值
				//	name: "...",
				//	//选项值
				//	value: ...,
				//	//是否选中，可选，默认为：false
				//	selected: true 或 false,
				//	//可选，选项css样式，格式允许：CSS字符串、CSS对象
				//	itemStyle: null,
				//	//option选项元素类名
				//	itemClass: null
				//}
				data: []
			};
			
			var chartEle = chart.element();
			CF.eleAddClass(chartEle, "dg-chart-select");
			var selectEle = CF.eleCreate("select", "dg-chart-select-select");
			CF.eleAppend(chartEle, selectEle);
			chart.internal(selectEle);
			
			chart.inflateOptions(options);
			SPT.processRenderOptions(chart, options);
			
			var isDropdown = (!options.multiple && (options.size == null || options.size <= 1));
			
			if(isDropdown)
				CF.eleAddClass(chartEle, "dg-chart-select-dropdown");
			
			if(!CF.isEmpty(options.id))
				CF.eleAttr(selectEle, "id", options.id);
			
			if(!CF.isEmpty(options.name))
				CF.eleAttr(selectEle, "name", options.name);
			
			if(options.multiple)
				CF.eleAttr(selectEle, "multiple", "multiple");
			
			if(options.size != null)
				CF.eleAttr(selectEle, "size", options.size);
			
			if(options.fillParent === true || (options.fillParent == "auto" && !isDropdown))
				CF.eleAddClass(selectEle, "dg-chart-select-full");
			
			if(!CF.isEmpty(options.selectStyle))
				CF.eleStyle(selectEle, options.selectStyle);
			
			if(!CF.isEmpty(options.selectClass))
				CF.eleAddClass(selectEle, options.selectClass);
			
			this._themeStyleSheet(chart);
			//此时不应绘制数据
		},
		
		update: function(chart, chartResult)
		{
			var renderOptions = SPT.liveDataRenderOptions(chart);
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var options = { data: [] };
			
			if(renderOptions.prepend != null)
			{
				var prepend = renderOptions.prepend;
				prepend = (CF.isArray(prepend) ? prepend : [ prepend ]);
				options.data  = CF.extend(true, options.data, prepend);
			}
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let result = chart.resultOf(chartResult, dataSetBind);
				let resultDatas = chart.resultDatas(result);
				
				let nameFields = chart.dataSetFieldsOfSign(dataSetBind, 0);
				let valueFields = chart.dataSetFieldsOfSign(dataSetBind, 1);
				let hasNameField = (nameFields.length > 0);
				
				if(hasNameField && nameFields.length != valueFields.length)
					throw new Error("the [name] sign columns must be one-to-one with [value] sign columns");
				
				let namess = (hasNameField ? chart.resultRowArrayDatas(result, nameFields) : []);
				let valuess = chart.resultRowArrayDatas(result, valueFields);
				
				for(let j=0; j<valuess.length; j++)
				{
					let values = valuess[j];
					let names = (hasNameField ? namess[j] : values);
					
					for(let k=0; k<names.length; k++)
					{
						let di = { name: names[k], value: values[k] };
						SPT.originalDataOfData(di, resultDatas[j]);
						options.data.push(di);
					}
				}
			}
			
			options = chart.inflateOptions(options);
			SPT.processUpdateOptions(chart, options);
			
			this._drawDataOptions(chart, options);
		},
		
		destroy: function(chart)
		{
			var chartEle = chart.element();
			var internal = chart.internal();
			CF.eleRemove(internal);
			CF.eleRemoveClass(chartEle, "dg-chart-select");
		},
		
		on: function(chart, type, handler)
		{
			var internal = chart.internal();
			var delegate;
			
			var actualType = SPT.actualEventTypeForData(type);
			if(actualType != null)
			{
				let bindDataName = this._itemBindDataName();
				type = actualType;
				delegate = function(e)
				{
					var selectedItems = Array.from(internal.selectedOptions);
					
					if(selectedItems.length > 0)
					{
						let data = [];
						selectedItems.forEach((item) =>
						{
							let di = CF.eleData(item, bindDataName);
							data.push(di);
						});
						
						if(!CF.eleAttr(internal, "multiple"))
							data = data[0];
						
						SPT.eventData(e, data);
						return SPT.invokeEventHandler(chart, handler, arguments);
					}
				};
			}
			else
			{
				delegate = function()
				{
					return SPT.invokeEventHandler(chart, handler, arguments);
				};
			}
			
			chart.registerEventHandlerDelegate(type, handler, delegate);
			internal.addEventListener(type, delegate);
		},
		
		off: function(chart, type, handler)
		{
			var internal = chart.internal();
			type = SPT.actualEventTypeForData(type, type);
			
			var delegates = chart.removeEventHandlerDelegate((d) =>
			{
				return SPT.eventHandlerDelegateFilter(d, type, handler);
			});
			
			delegates.forEach((d) =>
			{
				internal.removeEventListener(d.type, d.delegate);
			});
		},
		
		additions:
		{
			defaultLinkEventType: "change.data"
		},
		
		_themeStyleSheet: function(chart)
		{
			chart.themeStyleSheet(CF.builtinPropName("SelectChart"), function()
			{
				var theme = chart.theme();
				
				var css=
				[
					{
						name: " .dg-chart-select-select",
						value:
						{
							"color": theme.color,
							"background-color": theme.backgroundColor,
							"border-color": chart.themeGradualColor(0.4),
							"font-size": CF.toCssFontSize(theme.fontSize)
						}
					},
					{
						name: " .dg-chart-select-select option",
						value:
						{
							"font-size": CF.toCssFontSize(theme.fontSize)
						}
					},
					{
						name: ".dg-chart-select-dropdown .dg-chart-select-select option",
						value:
						{
							"color": theme.color,
							"background-color": chart.themeGradualColor(0)
						}
					}
				];
				
				return css;
			});
		},
		
		_drawDataOptions: function(chart, options)
		{
			var renderOptions = SPT.liveDataRenderOptions(chart);
			var internal = chart.internal();
			var bindDataName = this._itemBindDataName();
			
			var data = (options.data || []);
			var itemEles = CF.elesOfSelector("option", internal);
			var length = Math.max(data.length, itemEles.length);
			
			var selected = renderOptions.selected;
			if(selected != null && !CF.isArray(selected))
				selected = [ selected ];
			
			for(let i=0; i<length; i++)
			{
				let di = data[i];
				let itemEle = itemEles[i];
				
				if(di == null && itemEle == null)
					continue;
				
				if(di == null)
				{
					CF.eleRemove(itemEle);
					continue;
				}
				
				if(itemEle == null)
				{
					itemEle = CF.eleCreate("option");
					CF.eleAppend(internal, itemEle);
				}
				
				CF.eleAttr(itemEle, "class", (CF.isEmpty(options.itemClass) ? "" : " "+options.itemClass)
					+ (CF.isEmpty(di.itemClass) ? "" : " "+di.itemClass));
				CF.eleStyle(itemEle, options.itemStyle, di.itemStyle);
				
				CF.eleAttr(itemEle, "value", di.value);
				CF.eleHtml(itemEle, (!CF.isEmpty(di.name) ? di.name : di.value));
				
				if(di.selected || (selected != null && CF.indexInArray(selected, i) > -1))
					CF.eleAttr(itemEle, "selected", "selected");
				
				CF.eleData(itemEle, bindDataName, di);
			}
		},
		
		_itemBindDataName: function()
		{
			return (this.__itemBindDataName != null ? this.__itemBindDataName
							: (this.__itemBindDataName = CF.uid()+"SelectItemBindData"));
		}
	};
	
	return renderer;
};

//自定义

SPT.customRenderer = function(plugin, config)
{
	config = CF.extend(true,
	{
	},
	config);
	
	var renderer =
	{
		asyncRender: function(chart)
		{
			var renderer = this._getCustomRenderer(chart, true);
			
			if(!renderer || renderer.asyncRender == null)
				return false;
			
			if(CF.isFunction(renderer.asyncRender))
				return renderer.asyncRender(chart);
			else
				return (renderer.asyncRender == true);
		},
		
		render: function(chart)
		{
			var renderer = this._getCustomRenderer(chart, true);
			
			if(renderer && renderer.render != null)
				renderer.render(chart);
			else
				this._rawRender(chart);
		},
		
		asyncUpdate: function(chart, chartResult)
		{
			var renderer = this._getCustomRenderer(chart, true);
			
			if(!renderer || renderer.renderer == null)
				return false;
			
			if(CF.isFunction(renderer.asyncUpdate))
				return renderer.asyncUpdate(chart, chartResult);
			else
				return (renderer.asyncUpdate == true);
		},
		
		update: function(chart, chartResult)
		{
			var renderer = this._getCustomRenderer(chart, true);
			
			if(renderer && renderer.update != null)
				renderer.update(chart, chartResult);
			else
				this._rawUpdate(chart, chartResult);
		},
		
		destroy: function(chart)
		{
			var renderer = this._getCustomRenderer(chart, true);
			
			if(renderer && renderer.destroy != null)
				renderer.destroy(chart);
			else
				this._rawDestroy(chart);
		},
		
		on: function(chart, type, handler)
		{
			var renderer = this._getCustomRenderer(chart);
			
			if(renderer.on != null)
				renderer.on(chart, type, handler);
			else
				throw new Error("chart renderer 's [on] rqeuired");
		},
		
		off: function(chart, type, handler)
		{
			var renderer = this._getCustomRenderer(chart);
			
			if(renderer.off != null)
				renderer.off(chart, type, handler);
			else
				throw new Error("chart renderer 's [off] rqeuired");
		},
		
		resize: function(chart)
		{
			var renderer = this._getCustomRenderer(chart, true);
			
			//即使customRenderer未定义，resize操作也可以不抛出异常，因为不影响主体功能
			if(renderer && renderer.resize != null)
				renderer.resize(chart);
		},
		
		additions: function(chart)
		{
			var re = null;
			
			var renderer = this._getCustomRenderer(chart, true);
			
			if(renderer)
			{
				if(renderer.additions)
					re = (CF.isFunction(renderer.additions) ? renderer.additions(chart) : renderer.additions);
				else
					re = null;
			}
			
			return re;
		},
		
		_rawRender: function(chart)
		{
			var chartEle = chart.element();
			CF.eleAddClass(chartEle, "dg-chart-rawdata");
			
			var containerEle = CF.eleCreate("div", "dg-chart-container");
			CF.eleAppend(chartEle, containerEle);
			
			var titleEle = CF.eleCreate("div", "dg-chart-rawdata-title");
			CF.eleHtml(titleEle, chart.name());
			CF.eleAppend(containerEle, titleEle);
			
			var contentEle = CF.eleCreate("div", "dg-chart-rawdata-content");
			CF.eleAppend(containerEle, contentEle);
			
			chart.internal(containerEle);
		},
		
		_rawUpdate: function(chart, chartResult)
		{
			var internal = chart.internal();
			var contentEle = CF.eleOfSelector(".dg-chart-rawdata-content", internal);
			
			CF.eleEmpty(contentEle);
			
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let dataSetAlias = chart.dataSetAlias(dataSetBind);
				let result = chart.resultOf(chartResult, dataSetBind);
				let datas = chart.resultDatas(result);
				
				let dsEle = CF.eleCreate("div", "dg-chart-rawdata-dst");
				CF.eleAppend(contentEle, dsEle);
				
				let nameEle = CF.eleCreate("div", "dg-chart-rawdata-dst-name");
				CF.eleHtml(nameEle, dataSetAlias);
				CF.eleAppend(dsEle, nameEle);
				
				let dataEle = CF.eleCreate("div", "dg-chart-rawdata-dst-data");
				CF.eleAppend(dsEle, dataEle);
				
				for(let j=0; j<datas.length; j++)
				{
					let di = CF.toJsonString(datas[j]);
					
					let itemEle = CF.eleCreate("div", "dg-chart-rawdata-dst-data-item");
					CF.eleText(itemEle, di);
					CF.eleAppend(dataEle, itemEle);
				}
			}
		},
		
		_rawDestroy: function(chart)
		{
			var chartEle = chart.element();
			var internal = chart.internal();
			
			CF.eleRemoveClass(chartEle, "dg-chart-rawdata");
			CF.eleRemove(internal);
		},
		
		_getCustomRenderer: function(chart, nullable)
		{
			nullable = (nullable == null ? false : nullable);
			
			var renderer = chart.renderer();
			
			if(renderer == null && !nullable)
				throw new Error("chart renderer required");
			
			return renderer;
		}
	};
	
	return renderer;
};

//---------------------------------------------------------
//    公用函数开始
//---------------------------------------------------------

//org.datagear.analysis.DataSetField.DataType
SPT.DataSetFieldDataType =
{
	STRING: "STRING",
	BOOLEAN: "BOOLEAN",
	NUMBER: "NUMBER",
	INTEGER: "INTEGER",
	DECIMAL: "DECIMAL",
	DATE: "DATE",
	TIME: "TIME",
	TIMESTAMP: "TIMESTAMP",
	UNKNOWN: "UNKNOWN"
};

//org.datagear.analysis.ResultDataFormat.TYPE_*
SPT.ResultDataFormatType =
{
	//TYPE_NUMBER
	NUMBER: "NUMBER",
	//TYPE_STRING
	STRING: "STRING"
};

/**
 * 计算指定数据集字段的坐标轴类型。
 */
SPT.evalDataSetFieldAxisType = function(chart, dataSetField)
{
	var type = "category";
	
	if(SPT.isDataTypeNumber(dataSetField))
	{
		type = "value";
	}
	else if(SPT.isDataTypeAboutDate(dataSetField))
	{
		var resultDataFormat = chart.resultDataFormat();
		if(!resultDataFormat)
		{
			let dashboard = chart.dashboard();
			resultDataFormat = (dashboard ? dashboard.resultDataFormat() : null);
		}
		
		if(resultDataFormat)
		{
			if(SPT.isDataTypeDate(dataSetField)
				&& resultDataFormat.dateType == SPT.ResultDataFormatType.NUMBER)
			{
				type = "time";
			}
			else if(SPT.isDataTypeTime(dataSetField)
				&& resultDataFormat.timeType == SPT.ResultDataFormatType.NUMBER)
			{
				type = "time";
			}
			else if(SPT.isDataTypeTimestamp(dataSetField)
				&& resultDataFormat.timestampType == SPT.ResultDataFormatType.NUMBER)
			{
				type = "time";
			}
		}
	}
	
	return type;
};

/**
 * 指定数据集字段数据是否字符串类型。
 */
SPT.isDataTypeString = function(dataSetField)
{
	var dataType = (dataSetField ? (dataSetField.type || dataSetField) : "");
	return (dataType == SPT.DataSetFieldDataType.STRING);
};

/**
 * 指定数据集字段数据是否数值类型。
 */
SPT.isDataTypeNumber = function(dataSetField)
{
	var dataType = (dataSetField ? (dataSetField.type || dataSetField) : "");
	return (dataType == SPT.DataSetFieldDataType.NUMBER
			|| dataType == SPT.DataSetFieldDataType.INTEGER
			|| dataType == SPT.DataSetFieldDataType.DECIMAL);
};

/**
 * 指定数据集字段数据是否日期、时间、时间戳类型。
 */
SPT.isDataTypeAboutDate = function(dataSetField)
{
	var dataType = (dataSetField ? (dataSetField.type || dataSetField) : "");
	return (dataType == SPT.DataSetFieldDataType.DATE
			|| dataType == SPT.DataSetFieldDataType.TIME
			|| dataType == SPT.DataSetFieldDataType.TIMESTAMP);
};

/**
 * 指定数据集字段数据是否日期类型。
 */
SPT.isDataTypeDate = function(dataSetField)
{
	var dataType = (dataSetField ? (dataSetField.type || dataSetField) : "");
	return (dataType == SPT.DataSetFieldDataType.DATE);
};

/**
 * 指定数据集字段数据是否时间类型。
 */
SPT.isDataTypeTime = function(dataSetField)
{
	var dataType = (dataSetField ? (dataSetField.type || dataSetField) : "");
	return (dataType == SPT.DataSetFieldDataType.TIME);
};

/**
 * 指定数据集字段数据是否时间戳类型。
 */
SPT.isDataTypeTimestamp = function(dataSetField)
{
	var dataType = (dataSetField ? (dataSetField.type || dataSetField) : "");
	return (dataType == SPT.DataSetFieldDataType.TIMESTAMP);
};

/**
 * 为数组追加单个元素、数组
 */
SPT.appendElement = function(array, eles)
{
	if(CF.isArray(eles))
	{
		for(let i=0; i<eles.length; i++)
		{
			array.push(eles[i]);
		}
	}
	else
	{
		array.push(eles);
	}
};

/**
 * 为源数组追加不重复的元素。
 * 
 * @param array 待追加数组
 * @param ele 要追加的元素、数组，可以是基本类型、对象类型
 * @param propName 可选，当是对象类型时，用于指定判断重复的属性名
 * @returns 追加的或重复元素的索引、或者索引数组
 */
SPT.appendDistinct = function(array, ele, propName)
{
	if(CF.isArray(ele))
	{
		return SPT.appendDistinctQuick(array, ele, {}, propName);
	}
	else
	{
		let key = (propName != null && ele != null ? ele[propName] : ele);
		let keyIdx = SPT.findInArray(array, key, propName);
		
		if(keyIdx < 0)
		{
			array.push(ele);
			keyIdx = array.length - 1;
		}
		
		return keyIdx;
	}
};

/**
 * 为数组追加不重复的元素。
 * 
 * @param array 待追加数组
 * @param eles 追加元素、数组，可以是基本类型、对象类型
 * @param indexCache 索引缓存对象，用于存储arry中对应主键元素的索引，初始应为null、{}，格式为：{ 主键：索引数值 }
 * @param propName 可选，当是对象类型时，用于指定判断重复的属性名
 * @returns 追加的或重复元素的索引、或者索引数组
 */
SPT.appendDistinctQuick = function(array, eles, indexCache, propName)
{
	indexCache = (indexCache == null ? {} : indexCache);
	
	var isArray = CF.isArray(eles);
	
	if(!isArray)
		eles = [ eles ];
	
	var re = [];
	
	for(let i=0; i<eles.length; i++)
	{
		let ele = eles[i];
		let key = (propName != null && ele != null ? ele[propName] : ele);
		let keyIdx = indexCache[key];
		
		if(keyIdx != null && keyIdx > -1)
		{
			re[i] = keyIdx;
		}
		else
		{
			keyIdx = SPT.findInArray(array, key, propName);
			
			if(keyIdx > -1)
			{
				re[i] = keyIdx;
				indexCache[key] = keyIdx;
			}
			else
			{
				array.push(ele);
				indexCache[key] = array.length - 1;
				re[i] = array.length - 1;
			}
		}
	}
	
	return (isArray ? re : re[0]);
};

/**
 * 在数组中查找元素，返回其索引
 * 
 * @param array
 * @param value
 * @param propertyName 当数组元素是对象类型时，用于指定判断属性名，格式为："..."、function(ele){ return ... }
 * @returns 索引数值，-1 表示没有找到
 */
SPT.findInArray = function(array, value, propertyName)
{
	var isPnFunction = (propertyName && CF.isFunction(propertyName));
	
	for(var i=0; i<array.length; i++)
	{
		var ae = array[i];
		
		if(propertyName != null)
		{
			if(isPnFunction)
				ae = (ae ? propertyName(ae) : null);
			else
				ae = (ae ? ae[propertyName] : null);
		}
		
		if(ae == value)
			return i;
	}
	
	return -1;
};

/**
 * 查找数组中第一个不为null的元素值，如果未找到，则返回undefined。
 */
SPT.findNonNull = function(array)
{
	if(!array)
		return undefined;
	
	for(var i=0; i<array.length; i++)
	{
		if(array[i] != null)
			return array[i];
	}
	
	return undefined;
};

/**
 * 查找数组中第一个不为空的元素值，如果未找到，则返回undefined。
 */
SPT.findNonEmpty = function(array)
{
	if(!array)
		return undefined;
	
	for(var i=0; i<array.length; i++)
	{
		if(!CF.isEmpty(array[i]))
			return array[i];
	}
	
	return undefined;
};

/**
 * 校正obj.min、obj.max值，使得obj.min始终小于obj.max且都不为null。
 */
SPT.trimNumberRange = function(obj, defaultMin, defaultMax)
{
	if(defaultMin == null)
		defaultMin = 0;
	if(defaultMax == null)
		defaultMax = 100;
	
	if(obj.min == null && obj.max == null)
	{
		obj.min = defaultMin;
		obj.max = defaultMax;
	}
	else if(obj.min == null)
	{
		obj.min = obj.max - Math.abs(obj.max)/2;
	}
	else if(obj.max == null)
	{
		obj.max = obj.min + Math.abs(obj.min)/2;
	}
	
	if(obj.min == obj.max)
		obj.max = obj.min + Math.abs(obj.min)/2;
	else if(obj.min > obj.max)
	{
		var min = obj.min;
		obj.min = obj.max;
		obj.max = min;
	}
	
	return obj;
};

//计算图例名
SPT.legendNameForDataValues = function(chart, dataSetBinds, dataSetBind, dataSetAlias,
												valueProperties, valuePropertyIdx)
{
	var legendName = dataSetAlias;
	
	if(dataSetBinds.length > 1 && valueProperties.length > 1)
	{
		legendName = dataSetAlias +"-" + chart.dataSetFieldAlias(dataSetBind, valueProperties[valuePropertyIdx]);
	}
	else if(valueProperties.length > 1)
	{
		legendName = chart.dataSetFieldAlias(dataSetBind, valueProperties[valuePropertyIdx]);
	}
	
	return legendName;
};

SPT.symbolSizeRadioIfEffectScatter = function(scatterType)
{
	//涟漪效果会是散点显得很大，所以这里稍作调整
	return (scatterType == "effectScatter" ? 0.06 : null);
};

/**
 * 计算最大图元记号尺寸
 * 
 * @param chart
 * @param ratio 可选，自动获取的比率
 */
SPT.evalSymbolSizeMax = function(chart, ratio)
{
	ratio = (ratio == null ? 0.08 : ratio);
	
	//根据图表元素尺寸自动计算
	var chartEle = chart.element();
	var symbolSizeMax = parseInt(Math.min(chartEle.clientWidth, chartEle.clientHeight)*ratio);
	
	return symbolSizeMax;
};

/**
 * 计算最小图元记号尺寸
 * 
 * @param chart
 * @param symbolSizeMax
 * @param ratio 可选，自动获取的比率
 */
SPT.evalSymbolSizeMin = function(chart, symbolSizeMax, ratio)
{
	ratio = (ratio == null ? 0.2 : ratio);
	
	var symbolSizeMin = parseInt(symbolSizeMax * ratio);
	
	if(symbolSizeMin < 6)
		symbolSizeMin = 6;
	
	return symbolSizeMin;
};

//计算数值的图元记号尺寸
SPT.evalValueSymbolSize = function(value, minValue, maxValue, symbolSizeMax, symbolSizeMin)
{
	if(symbolSizeMin == null)
		symbolSizeMin = 4;
	
	if(value == null || minValue == null || maxValue == null)
		return symbolSizeMin;
	
	if((maxValue-minValue) <= 0)
		return symbolSizeMin;
	
	var size = parseInt((value-minValue)/(maxValue-minValue)*(symbolSizeMax-symbolSizeMin)) + symbolSizeMin;
	return size;
};

/**
 * 计算系列数据数值的图元记号尺寸
 * 
 * @param series 系列对象：{ data: [ {value: ...}, ... ] }、或其数组
 */
SPT.evalSeriesDataValueSymbolSize = function(series, minValue, maxValue, symbolSizeMax, symbolSizeMin,
		valuePropertyName, valueElementIndex)
{
	if(series == null)
		return;
	
	if(valuePropertyName == null)
		valuePropertyName = "value";
	
	if(!CF.isArray(series))
		series = [ series ];
	
	for(var i=0; i<series.length; i++)
	{
		SPT.evalDataValueSymbolSize(series[i].data, minValue, maxValue, symbolSizeMax, symbolSizeMin,
					valuePropertyName, valueElementIndex);
	}
};

/**
 * 计算系列数据数值的图元记号尺寸
 * 
 * @param data 数据对象：{value: ...}、或其数组
 */
SPT.evalDataValueSymbolSize = function(data, minValue, maxValue, symbolSizeMax, symbolSizeMin,
		valuePropertyName, valueElementIndex)
{
	if(data == null)
		return;
	
	if(valuePropertyName == null)
		valuePropertyName = "value";
	
	if(!CF.isArray(data))
		data = [ data ];
	
	for(var i=0; i<data.length; i++)
	{
		var obj = 	data[i];
		var value = obj[valuePropertyName];
		
		if(value != null && valueElementIndex != null)
			value = value[valueElementIndex];
		
		obj.symbolSize = SPT.evalValueSymbolSize(value, minValue, maxValue, symbolSizeMax, symbolSizeMin);
	}
};

SPT.appendCategoryNameAndData = function(categoryNames, categoryDatasMap, categoryName, categoryData)
{
	SPT.appendDistinct(categoryNames, categoryName);
	
	var categoryDatas = (categoryDatasMap[categoryName] || (categoryDatasMap[categoryName] = []));
	SPT.appendElement(categoryDatas, categoryData);
};

SPT.splitDataByCategory = function(data, categoryNames, categoryDatasMap, defaultCategoryName, categoryPropName)
{
	defaultCategoryName = (defaultCategoryName == null ? "" : defaultCategoryName);
	
	for(let i=0; i<data.length; i++)
	{
		let di = data[i];
		let categoryName = (di == null ? null : SPT.categoryValueOfData(di, categoryPropName));
		categoryName = (categoryName == null ? defaultCategoryName : categoryName);
		
		SPT.appendCategoryNameAndData(categoryNames, categoryDatasMap, categoryName, di);
	}
};

/**
 * 从数据集结果中读取第一个不为空的数据标记数据值。
 */
SPT.dstResultFirstNonEmptyOfSign = function(chart, dataSetBind, result, dataSign)
{
	var field = chart.dataSetFieldOfSign(dataSetBind, dataSign);
	
	if(field)
	{
		let datas = chart.resultDatas(result);
		
		for(let i=0; i<datas.length; i++)
		{
			let value = chart.resultDataRowCell(datas[i], field);
			
			if(!CF.isEmpty(value))
				return value;
		}
	}
	
	return null;
};

/**
 * 从图表结果中读取第一个不为空的数据标记数据值。
 */
SPT.chartResultFirstNonEmptyOfSign = function(chart, chartResult, dataSign)
{
	var dsbs = chart.dataSetBinds();
	
	for(let i=0; i<dsbs.length; i++)
	{
		let dsb = dsbs[i];
		let result = chart.resultOf(chartResult, dsb);
		let value = SPT.dstResultFirstNonEmptyOfSign(chart, dsb, result, dataSign);
		
		if(!CF.isEmpty(value))
			return value;
	}
	
	return null;
};

SPT.addCategoryToFieldMap = function(fieldMap, categoryField, categoryName)
{
	categoryName = (categoryName == null ? SPT.ORIGINAL_CATEGORY_PROP_NAME : categoryName);
	fieldMap[categoryName] = categoryField;
	return fieldMap;
};

SPT.categoryValueOfData = function(data, categoryPopName)
{
	if(data == null)
		return undefined;
	
	categoryPopName = (categoryPopName == null ? SPT.ORIGINAL_CATEGORY_PROP_NAME : categoryPopName);
	return data[categoryPopName];
};

SPT.legendNameForDataCategory = function(dataSetBinds, dataSetAlias, categoryName)
{
	return (dataSetBinds.length > 1 ? dataSetAlias +"-" + categoryName : categoryName);
};

//计算数组数据最小/最大值
//range 待填充的最小/最大值对象，格式为：{ min: 数值, max: 数值 }
//data 数组
//propertyName0 可选，当data[i]是对象或数组时，取值属性
//propertyName1 可选，当data[i][propertyName0]是对象或数组时，取值属性
SPT.evalArrayDataRange = function(range, data, propertyName0, propertyName1)
{
	if(data == null)
		return range;
	
	for(let i=0; i<data.length; i++)
	{
		let val = (data[i] == null ? null : data[i]);
		
		if(propertyName0 != null)
			val = (val == null ? null : val[propertyName0]);
		
		if(propertyName1 != null)
			val = (val == null ? null : val[propertyName1]);
		
		if(val != null)
		{
			range.min = (range.min == null ? val : Math.min(range.min, val));
			range.max = (range.max == null ? val : Math.max(range.max, val));
		}
	}
	
	return range;
};

//返回targetObj的浅复制对象，如果某个属性值在baseObj中是数组而在targetObj不是数组，返回对象会将其转为数组
SPT.trimArrayPropsForMerge = function(targetObj, baseObj)
{
	var targetRe = {};
	
	for(let name in targetObj)
	{
		let value = targetObj[name];
		let baseValue = (baseObj == null ? null : baseObj[name]);
		
		if(value != null && !CF.isArray(value) && CF.isArray(baseValue))
		{
			targetRe[name] = [ value ];
		}
		else
		{
			targetRe[name] = value;
		}
	}
	
	return targetRe;
};

SPT.dataSetBindsMainFetched = function(chart, chartResult)
{
	var dsbs = chart.dataSetBindsMain();
	return chart.dataSetBindsFetched(dsbs, chartResult);
};

SPT.originalDataOfData = function(data, originalData)
{
	if(originalData === undefined)
	{
		return (data == null ? null : data[SPT.ORIGINAL_DATA_PROP_NAME]);
	}
	else
	{
		data[SPT.ORIGINAL_DATA_PROP_NAME] = originalData;
	}
};

SPT.originalDataOfDatas = function(datas, originalDatas)
{
	if(datas == null || originalDatas == null)
		return;
	
	var len = Math.min(datas.length, originalDatas.length);
	
	for(let i=0; i<len; i++)
	{
		SPT.originalDataOfData(datas[i], originalDatas[i]);
	}
};

SPT.originalDataOfResult = function(datas, chart, result)
{
	SPT.originalDataOfDatas(datas, chart.resultDatas(result));
};

SPT.convertArrayValueEleToString = function(array, index)
{
	index = (index == null ? 0 : index);
	
	if(array == null)
		return;
	
	for(let i=0; i<array.length; i++)
	{
		let v = (array[i] == null ? null : array[i].value);
		let vi = (v == null ? null : v[index]);
		
		if(vi != null && !CF.isString(vi))
			v[index] = vi + "";
	}
};

//转换dataObj.data[i]元素{value: ...}为{name: ...}
SPT.convertDataPropValueToName = function(dataObj)
{
	if(dataObj == null || dataObj.data == null)
		return;
	
	for(let i=0; i<dataObj.data.length; i++)
	{
		let di = dataObj.data[i];
		dataObj.data[i] = (di == null ? null : { name: di.value });
	}
};

//调用chart.options()中的processRenderOptions选项函数处理图表渲染选项，格式为：function(renderOptions, chart){ ... }
SPT.processRenderOptions = function(chart, renderOptions, set)
{
	set = (set == null ? true : set);
	
	var options = chart.options();
	var handler = CF.optionValue(options, SPT.PROCESS_RENDER_OPTIONS_OPTION_NAME);
	
	if(handler)
	{
		handler.call(options, renderOptions, chart);
	}
	
	if(set)
	{
		SPT.liveDataRenderOptions(chart, renderOptions);
	}
	
	return renderOptions;
};

//获取/设置图表渲染选项
SPT.liveDataRenderOptions = function(chart, renderOptions)
{
	if(renderOptions === undefined)
		return chart.liveData(SPT.RENDER_OPTIONS_LIVE_DATA_NAME);
	else
		chart.liveData(SPT.RENDER_OPTIONS_LIVE_DATA_NAME, renderOptions);
};

//调用chart.options()中的processUpdateOptions选项函数处理图表更新选项，格式为：function(updateOptions, chart){ ... }
SPT.processUpdateOptions = function(chart, updateOptions, set)
{
	set = (set == null ? true : set);
	
	var options = chart.options();
	var handler = CF.optionValue(options, SPT.PROCESS_UPDATE_OPTIONS_OPTION_NAME);
	
	if(handler)
	{
		handler.call(options, updateOptions, chart);
	}
	
	if(set)
	{
		SPT.liveDataUpdateOptions(chart, updateOptions);
	}
	
	return updateOptions;
};

//获取/设置图表更新选项
SPT.liveDataUpdateOptions = function(chart, updateOptions)
{
	if(updateOptions === undefined)
		return chart.liveData(SPT.UPDATE_OPTIONS_LIVE_DATA_NAME);
	else
		chart.liveData(SPT.UPDATE_OPTIONS_LIVE_DATA_NAME, updateOptions);
};

SPT.EVENT_TYPE_FOR_DATA_SUFFIX = ".data";

//是否是扩展的数据的事件类型（以'.data'结尾）
SPT.isEventTypeForData = function(type)
{
	if(type == null || !CF.isString(type))
		return false;
	
	return type.endsWith(SPT.EVENT_TYPE_FOR_DATA_SUFFIX);
};

//获取扩展数据事件类型的实际事件类型，不是则返回null或者typeIfNot
SPT.actualEventTypeForData = function(type, typeIfNot)
{
	typeIfNot = (typeIfNot == null ? null : typeIfNot);
	
	if(!SPT.isEventTypeForData(type))
		return typeIfNot;
	
	return type.substring(0, type.length - SPT.EVENT_TYPE_FOR_DATA_SUFFIX.length);
};

SPT.eventHandlerDelegateFilter = function(delegateObj, type, handler)
{
	return (delegateObj.type === type && delegateObj.handler === handler);
};

//调用扩展数据事件类型处理函数，函数内的this将指向chart
SPT.invokeEventHandler = function(chart, handler, args)
{
	handler.apply(chart, args);
};

//获取/设置事件对象的"data"属性值
SPT.eventData = function(e, data)
{
	if(data === undefined)
		return e.data;
	else
		e.data = data;
};

//获取全局ECharts对象
EU._echarts = function()
{
	return global.echarts;
};

/**
 * 将图表初始化为ECharts图表。
 * 此函数会自动将EU.themeName()函数、EU.themeNameOfChartTheme()函数返回的主题名应用至初始化的ECharts图表主题。
 * 此函数会自动调用chart.internal()将初始化的ECharts实例对象设置为图表底层组件。
 * 
 * @param chart 图表
 * @param opts 可选，同echarts.init()函数的opts附加参数
 * @returns ECharts实例
 */
EU.init = function(chart, opts)
{
	var themeName = EU.themeName(chart);
	
	if(!themeName)
		themeName = EU.themeNameOfChartTheme(chart);
	
	var instance = EU._echarts().init(chart.element(), themeName, opts);
	chart.internal(instance);
	
	return instance;
};

/**
 * 获取在图表元素上（优先）或者<body>元素上通过"dg-echarts-theme"属性定义的ECharts主题名
 * 
 * @param chart 图表
 * @returns ECharts主题名
 */
EU.themeName = function(chart)
{
	var themeName = CF.eleAttr(chart.element(), CF.elementAttrConst.ECHARTS_THEME);
	
	if(!themeName)
		themeName = CF.eleAttr(document.body, CF.elementAttrConst.ECHARTS_THEME);
	
	return themeName;
};

/**
 * 获取根据图表主题生成（只第一次生成）的ECharts主题名。
 * 
 * @param chart 图表
 * @returns ECharts主题名，对应的主题由图表主题生成且已经注册至ECharts
 */
EU.themeNameOfChartTheme = function(chart)
{
	var theme = chart.theme();
	var themeName = theme[EU.THEME_PROP_ECHARTS_THEME_NAME];
	
	if(!themeName)
	{
		themeName = (theme[EU.THEME_PROP_ECHARTS_THEME_NAME] = CF.uid());
		
		var echartsTheme = EU._buildEchartsTheme(chart);
		EU._echarts().registerTheme(themeName, echartsTheme);
	}
	
    return themeName;
};

/**
 * 为图表注册指定名称的地图（GeoJSON、SVG）至ECharts，并在注册完成后执行回调函数。
 * 如果地图未加载，将在加载后再注册。
 * 注意：如果在图表渲染器的render()、update()函数中调用此函数，应该首先设置渲染器的asyncRender、asyncUpdate，
 * 并在complete中调用chart.statusRendered(true)、chart.statusUpdated(true)。
 * 
 * @param chart 图表
 * @param name 地图名称、地图名称数组
 * @param complete 可选，注册完成后（无论是否成功）的回调函数，格式为：function(){ ... }
 */
EU.registerMap = function(chart, name, complete)
{
	name = (CF.isArray(name) ? name : [ name ]);
	
	var echarts = EU._echarts();
	
	var needLoads = [];
	
	for(let i=0; i<name.length; i++)
	{
		if(echarts.getMap(name[i]) == null)
			needLoads.push(name[i]);
	}
	
	if(needLoads.length == 0)
	{
		if(complete != null)
			complete();
		
		return;
	}
	
	var loadPromises = [];
	
	for(let i=0; i<needLoads.length; i++)
	{
		let myName = name[i];
		let state = EU.MAP_REGISTER_STATES[myName];
		
		if(state == null)
		{
			let mapUrl = chart.mapURL(myName);
			state =
			{
				loadPromise: new Promise(function(resolve, reject)
				{
					fetch(mapUrl).then((response) =>
					{
						if(!response.ok)
							throw new Error(response.statusText ? response.statusText : response.status+"");
						
						let headers = response.headers;
						let contentType = (headers.get("Content-Type") || "");
						//是否SVG地图
						let isSvg = (/svg/i.test(contentType) || /(\.svg$)|(\.svg[\?\#])/i.test(mapUrl));
						
						if(isSvg)
						{
							response.text().then((svgText) =>
							{
								echarts.registerMap(myName, {svg: svgText});
								resolve();
							});
						}
						else
						{
							response.json().then((geoJSON) =>
							{
								echarts.registerMap(myName, {geoJSON: geoJSON});
								resolve();
							});
						}
					})
					.catch((e) =>
					{
						EU.MAP_REGISTER_STATES[myName] = null;
						reject();
					});
				})
			};
			
			EU.MAP_REGISTER_STATES[myName] = state;
		}
		
		loadPromises.push(state.loadPromise);
	}
	
	Promise.all(loadPromises).finally(function()
	{
		if(complete != null)
			complete();
	});
};

/**
 * 释放ECharts图表实例。
 * 
 * @param chart
 */
EU.dispose = function(chart)
{
	var internal = chart.internal();
	
	if(internal && !internal.isDisposed())
		internal.dispose();
};

/**
 * 调整ECharts图表尺寸。
 * 
 * @param chart
 */
EU.resize = function(chart)
{
	var internal = chart.internal();
	
	if(internal)
		internal.resize();
};

/**
 * 由图表主题构建ECharts主题。
 * 
 * @param chart 图表
 */
EU._buildEchartsTheme = function(chart)
{
	var borderColor = chart.themeGradualColor(0.3);
	var axisColor = chart.themeGradualColor(0.7);
	var axisScaleLineColor = chart.themeGradualColor(0.35);
	var areaColor0 = chart.themeGradualColor(0.1);
	var areaBorderColor0 = chart.themeGradualColor(0.3);
	var areaColor1 = chart.themeGradualColor(0.25);
	var areaBorderColor1 = chart.themeGradualColor(0.5);
	var shadowColor = chart.themeGradualColor(0.9);
	var emptyAreaColor = chart.themeGradualColor(0);
	var emptyBorderColor = areaColor0;
	
	var chartTheme = chart.theme();
	
	var theme =
	{
		"color" : chartTheme.graphColors,
		"backgroundColor" : chartTheme.backgroundColor,
		"textStyle" : {},
		"title" : {
	        "left" : "center",
	        //6.0版本标题默认top有变动，需要明确设置为0才能兼容旧版
	        "top": 0,
			"textStyle" : { "color" : chartTheme.titleTheme.color },
			"subtextStyle" : { "color" : chartTheme.titleTheme.color },
			"backgroundColor" : chartTheme.titleTheme.backgroundColor
		},
		"line" : {
			"itemStyle" : { "borderWidth" : 2 },
			"lineStyle" : { "width" : 2 },
			"label": { "color": chartTheme.color },
			"symbol" : "circle",
			"symbolSize" : 8,
			"smooth" : false,
			"emphasis" : { "lineStyle" : { "width" : 4 } }
		},
		"radar" : {
			"name" : { "textStyle" : { "color" : chartTheme.legendTheme.color } },
			"axisLine" : { "lineStyle" : { "color" : areaBorderColor0 } },
			"splitLine" : { "lineStyle" : { "color" : areaBorderColor0 } },
			"splitArea" : { "areaStyle" : { "color" : [ areaColor0, chartTheme.backgroundColor ] } },
			"itemStyle" : { "borderWidth" : 1 },
			"lineStyle" : { "width" : 2 },
			"emphasis" : { "lineStyle" : { "width" : 4, "shadowBlur" : 5, "shadowOffsetX" : 0, "shadowColor" : shadowColor } },
			"symbolSize" : 6,
			"symbol" : "circle",
			"smooth" : false
		},
		"bar": {
			"itemStyle" : { "borderWidth" : 0, "borderColor" : borderColor },
			"label": { "color": chartTheme.color },
			"emphasis" : {
				"itemStyle" : {
					"borderWidth" : 0, "borderColor" : borderColor,
					"shadowBlur" : 10, "shadowOffsetX" : 0, "shadowColor" : shadowColor, "shadowOffsetY" : 0
				}
			}
		},
		"pie" : {
			"itemStyle" : { "borderWidth" : 0, "borderColor" : borderColor },
			"label": { "color": chartTheme.color },
			"emphasis" : {
				"itemStyle": {
					"shadowBlur" : 10, "shadowOffsetX" : 0, "shadowColor" : shadowColor,
					"borderWidth" : 0, "borderColor" : borderColor
				}
			},
			"emptyCircleStyle": { "color": emptyAreaColor, "borderColor": emptyBorderColor }
		},
		"scatter" : {
			"itemStyle" : { "borderWidth" : 0, "borderColor" : borderColor, "shadowBlur" : 0, "shadowColor" : shadowColor },
			"label": { "color": chartTheme.color },
			"emphasis" : {
				"itemStyle" : {
					"borderWidth" : 0, "borderColor" : borderColor, "shadowBlur" : 10,
					"shadowOffsetX" : 0, "shadowColor" : shadowColor
				}
			}
		},
		"effectScatter": {
			"itemStyle" : { "borderWidth" : 0, "borderColor" : borderColor, "shadowBlur" : 0, "shadowColor" : shadowColor },
			"emphasis" : {
				"itemStyle" : {
					"borderWidth" : 0, "borderColor" : borderColor, "shadowBlur" : 10, "shadowOffsetX" : 0,
					"shadowColor" : shadowColor
				}
			}
		},
		"boxplot" : {
			"itemStyle" : { "color": "transparent" },
			"emphasis" : { "itemStyle" : { "color": "transparent" } }
		},
		"parallel" : {
			"left": "10%", "top": "20%", "right": "10%", "bottom": "10%",
			"lineStyle" : { "width": 2, "shadowBlur" : 0, "shadowColor" : shadowColor },
			"emphasis" : { "lineStyle" : { "shadowBlur" : 4, "shadowOffsetX" : 0, "shadowColor" : shadowColor } }
		},
		"sankey" : {
			"label": { "color": chartTheme.color },
			"itemStyle" : { "borderWidth" : 0, "borderColor" : borderColor },
			"lineStyle": { "color": areaColor1, "opacity": 1 },
			"emphasis" : {
				"itemStyle" : { "borderWidth" : 0, "borderColor" : borderColor },
				"lineStyle": { "color": axisColor, "opacity": 0.6 },
				"focus": "adjacency"
			}
		},
		"funnel" : {
			"left": "10%", "top": "20%", "right": "10%", "bottom": "10%",
            "minSize": "0%", "maxSize": "100%",
			"label" : { "color" : chartTheme.color, "show": true, "position": "inside" },
			"itemStyle" : { "borderColor" : borderColor, "borderWidth" : 0 },
			"emphasis" : {
				"label" : { "fontSize" : 20 },
				"itemStyle" : {
					"shadowBlur" : 10, "shadowOffsetX" : 0, "shadowColor" : shadowColor, "borderWidth" : 0,
					"borderColor" : borderColor
				}
			}
		},
		"gauge" : {
			"title" : { "color" : chartTheme.legendTheme.color },
			"detail": { "color": chartTheme.legendTheme.color },
			"progress": { "show": true, "roundCap": true },
			"axisLine": { "show": true,
				"lineStyle": { "color" : [ [ 1, areaColor0 ] ] }
	        },
			"axisLabel": { "color" : axisColor },
			"splitLine": { "lineStyle": { "color": chartTheme.actualBackgroundColor } },
			"axisTick": { "lineStyle": { "color": chartTheme.actualBackgroundColor } },
			"itemStyle" : { "borderColor" : borderColor, "borderWidth" : 0 },
			"emphasis" : {
				"itemStyle" : {
					"shadowBlur" : 10, "shadowOffsetX" : 0, "shadowColor" : shadowColor,
					"borderWidth" : 0, "borderColor" : borderColor
				}
			}
		},
		"candlestick" : {
			"itemStyle" : { "borderWidth" : 1 },
			"emphasis" : { "itemStyle" : { "shadowBlur" : 10, "shadowOffsetX" : 0, "shadowColor" : shadowColor } }
		},
		"heatmap": {
			"label": { "show": true },
			"emphasis" : { "itemStyle" : { "shadowBlur" : 5 } }
		},
		"tree": {
			"expandAndCollapse": true,
			"label": { "color": chartTheme.color },
			"itemStyle": { "color": chartTheme.color },
			"lineStyle": { "color": areaBorderColor0 },
			"emphasis" : { "itemStyle" : { "shadowBlur" : 10, "shadowOffsetX" : 0, "shadowColor" : shadowColor } }
		},
		"treemap": {
			//ECharts-6.0中默认的left/top/width/height没有起作用，需要这里明确设置
			"left": "center", "top": "middle", "width": "80%", "height": "80%",
			"itemStyle" : { "borderWidth": 0.5, "borderColor": chartTheme.backgroundColor },
			"emphasis" : {
				"itemStyle" : {
					"shadowBlur" : 10, "shadowOffsetX" : 0, "shadowColor" : shadowColor,
					"borderWidth" : 0, "borderColor" : borderColor
				}
			},
			"breadcrumb": {
				//ECharts-6.0中默认的top没有起作用，需要这里明确设置
				"top": "bottom",
				"itemStyle": {
					"color": chartTheme.backgroundColor, "borderColor": borderColor,
					"shadowBlur": 0, "textStyle": { color: chartTheme.color }
				}
			}
		},
		"sunburst": {
			"itemStyle" : { "borderWidth" : 1, "borderColor" : chartTheme.backgroundColor },
			"emphasis" : { "itemStyle" : { "shadowBlur" : 10, "shadowColor" : shadowColor, "borderColor" : borderColor } }
		},
		"graph" :
		{
			"left": "10%", "right": "10%", "top": "20%", "bottom": "10%", "roam": true,
			"itemStyle" : { "borderWidth" : 0, "borderColor" : borderColor, "shadowBlur" : 2, "shadowColor" : shadowColor },
			"lineStyle" : { "width": 2, "color": "source", "curveness": 0.3 },
			"label" : { "color" : chartTheme.color },
			"emphasis" : {
				"itemStyle" : {
					"borderWidth" : 0, "borderColor" : borderColor, "shadowBlur" : 10,
					"shadowOffsetX" : 0, "shadowColor" : shadowColor
				},
				"lineStyle" : { "width": 5 },
				"focus": "adjacency",
				"legendHoverLink": true,
				"label": { "position": "right" }
			}
		},
		"map" : {
			"roam" : true,
			"itemStyle" : { "areaColor" : areaBorderColor0, "borderColor" : areaBorderColor1, "borderWidth" : 0.5 },
			"label" : { "show": true, "color" : chartTheme.color },
			"emphasis" : {
				"label": { "color" : chartTheme.highlightTheme.color },
				"itemStyle": {
					"areaColor" : chartTheme.highlightTheme.backgroundColor,
					"borderColor" : chart.themeGradualColor(chartTheme.highlightTheme, 0.3),
					"borderWidth" : 1
				}
			}
		},
		"lines":
		{
			"lineStyle": { "width": 2 },
			"emphasis": { "lineStyle": { "width": 5, "shadowBlur" : 5, "shadowOffsetX" : 0, "shadowColor" : shadowColor } }
		},
		"geo" : {
			"itemStyle" : { "areaColor" : areaBorderColor0, "borderColor" : areaBorderColor1, "borderWidth" : 0.5 },
			"label" : { "color" : chartTheme.color },
			"emphasis" : {
				"label": { "color" : chartTheme.highlightTheme.color },
				"itemStyle": {
					"areaColor" : chartTheme.highlightTheme.backgroundColor,
					"borderColor" : chart.themeGradualColor(chartTheme.highlightTheme, 0.3),
					"borderWidth" : 1
				}
			}
		},
		"themeRiver":
		{
			/*ECharts-6.0版本这里定位配置不起作用*/
			"left": "10%", "top": "20%", "right": "10%", "bottom": "10%",
			"label": { "show": true },
			"emphasis": { "itemStyle": { "shadowBlur": 10, "shadowColor": shadowColor } }
		},
		"categoryAxis" : {
			"axisLine" : { "show" : true, "lineStyle" : { "color" : axisColor } },
			"axisTick" : { "show" : true, "lineStyle" : { "color" : axisColor } },
			"axisLabel" : { "show" : true, "textStyle" : { "color" : axisColor } },
			"splitLine" : { "show" : true, "lineStyle" : { "type" : "dotted", "color" : [ axisScaleLineColor ] } },
			"splitArea" : { "show" : false, "areaStyle" : { "color" : [ axisScaleLineColor ] } }
		},
		"valueAxis" : {
			"axisLine" : { "show" : true, "lineStyle" : { "color" : axisColor } },
			"axisTick" : { "show" : true, "lineStyle" : { "color" : axisColor } },
			"axisLabel" : { "show" : true, "textStyle" : { "color" : axisColor } },
			"splitLine" : { "show" : true, "lineStyle" : { "type" : "dotted", "color" : [ axisScaleLineColor ] } },
			"splitArea" : { "show" : false, "areaStyle" : { "color" : [ axisScaleLineColor ] } }
		},
		"logAxis" : {
			"axisLine" : { "show" : true, "lineStyle" : { "color" : axisColor } },
			"axisTick" : { "show" : true, "lineStyle" : { "color" : axisColor } },
			"axisLabel" : { "show" : true, "textStyle" : { "color" : axisColor } },
			"splitLine" : { "show" : true, "lineStyle" : { "type" : "dotted", "color" : [ axisScaleLineColor ] } },
			"splitArea" : { "show" : false, "areaStyle" : { "color" : [ axisScaleLineColor ] } }
		},
		"timeAxis" : {
			"axisLine" : { "show" : true, "lineStyle" : { "color" : axisColor } },
			"axisTick" : { "show" : true, "lineStyle" : { "color" : axisColor } },
			"axisLabel" : { "show" : true, "textStyle" : { "color" : axisColor } },
			"splitLine" : { "show" : true, "lineStyle" : { "type" : "dotted", "color" : [ axisScaleLineColor ] } },
			"splitArea" : { "show" : false, "areaStyle" : { "color" : [ axisScaleLineColor ] } }
		},
		"singleAxis": { "left": "10%", "top": "20%", "right": "10%", "bottom": "10%" },
		"toolbox" : {
			"iconStyle" : { "borderColor" : borderColor },
			"emphasis" : { "iconStyle" : { "borderColor" : axisColor } }
		},
		"grid": { "left": "10%", "right": "10%", "top": "20%", "bottom": "10%", "containLabel": true },
		"legend" : {
			"orient": "horizontal",
			"top": 25,
			"textStyle" : { "color" : chartTheme.legendTheme.color },
			"inactiveColor" : axisScaleLineColor,
			"inactiveBorderColor": axisColor,
			"backgroundColor" : chartTheme.legendTheme.backgroundColor
		},
		"tooltip" : {
			"backgroundColor" : chartTheme.tooltipTheme.backgroundColor,
			"borderColor" : chart.themeGradualColor(chartTheme.tooltipTheme, 0.3),
			"borderWidth" : 1,
			"textStyle" : { color: chartTheme.tooltipTheme.color },
			"axisPointer" : {
				"lineStyle" : { "color" : axisColor, "width" : 1 },
				"crossStyle" : { "color" : axisColor, "width" : 1 }
			}
		},
		"timeline" : {
			"lineStyle" : { "color" : axisColor, "width" : 1 },
			"itemStyle" : { "color" : chartTheme.color, "borderWidth" : 1 },
			"controlStyle" : { "color" : chartTheme.color, "borderColor" : borderColor, "borderWidth" : 0.5 },
			"checkpointStyle" : {
				"color" : chartTheme.highlightTheme.backgroundColor,
				"borderColor" : chart.themeGradualColor(chartTheme.tooltipTheme, 0.3)
			},
			"label" : { "color" : axisColor },
			"emphasis" : {
				"itemStyle" : { "color" : chartTheme.color },
				"controlStyle" : { "color" : chartTheme.color, "borderColor" : borderColor, "borderWidth" : 0.5 },
				"label" : { "color" : chartTheme.color }
			}
		},
		"visualMap" : {
			"inRange" : { "color" : chartTheme.graphRangeColors },
			"outOfRange" : { "color" : emptyAreaColor },
			"backgroundColor" : "transparent",
			"textStyle" : { "color" : axisColor }
		},
		"dataZoom" : {
			"backgroundColor" : "transparent",
			"dataBackgroundColor" : axisScaleLineColor,
			"fillerColor" : axisScaleLineColor,
			"handleColor" : axisScaleLineColor,
			"handleSize" : "100%",
			"textStyle" : { "color" : axisColor }
		},
		"markPoint" : {
			"label" : { "color" : axisColor },
			"emphasis" : { "label" : { "color" : axisColor } }
		}
	};
	
	//不能在上述theme中直接设置fontSize，因为即时值为null，仍然会改变默认字体
	
	if(chartTheme.fontSize)
	{
		theme.textStyle = (theme.textStyle || {});
		theme.textStyle.fontSize = chartTheme.fontSize;
		
		theme.categoryAxis.axisLabel.textStyle.fontSize = chartTheme.fontSize;
		theme.valueAxis.axisLabel.textStyle.fontSize = chartTheme.fontSize;
		theme.logAxis.axisLabel.textStyle.fontSize = chartTheme.fontSize;
		theme.timeAxis.axisLabel.textStyle.fontSize = chartTheme.fontSize;
		theme.gauge.title.fontSize = chartTheme.fontSize;
		theme.gauge.detail.fontSize = chartTheme.fontSize;
		theme.gauge.axisLabel.fontSize = chartTheme.fontSize;
		theme.sankey.label.fontSize = chartTheme.fontSize;
		theme.themeRiver.label.fontSize = chartTheme.fontSize;
	}
	
	if(chartTheme.titleTheme.fontSize)
		theme.title.textStyle.fontSize = chartTheme.titleTheme.fontSize;
	
	if(chartTheme.legendTheme.fontSize)
		theme.legend.textStyle.fontSize = chartTheme.legendTheme.fontSize;
	
	if(chartTheme.tooltipTheme.fontSize)
		theme.tooltip.textStyle.fontSize = chartTheme.tooltipTheme.fontSize;
	
	return theme;
};

/**
 * 自定义ECharts的tooltip
 */
EU.customTooltip = function(params, extractor)
{
	var html = "";
	
	let datas = [];
	let title = "";
	
	// "axis"触发时
	if(CF.isArray(params))
	{
		for(let i=0; i<params.length; i++)
		{
			let pi = params[i];
			let data = (extractor == null ? {} : (extractor(pi) || {}));
			
			if(CF.isEmpty(title))
			{
				if(data.title != null)
					title = data.title;
				else
					title = (pi.axisValueLabel || pi.axisValue || pi.name);
			}
			
			if(data.color == null)
				data.color = (pi.color || "");
			
			if(data.name == null)
				data.name = (pi.seriesName || "");
			
			if(data.value == null)
				data.value = (pi.value || "");
			
			datas.push(data);
		}
	}
	//"item"触发时
	else
	{
		let data = (extractor == null ? {} : (extractor(params) || {}));
		title = (data.title != null ? data.title : (params.seriesName || ""));
		
		if(data.color == null)
			data.color = (params.color || "");
		
		if(data.name == null)
			data.name = (params.name || "");
		
		if(data.value == null)
			data.value = (params.value || "");
		
		datas.push(data);
	}
	
	html += "<div style='display:flex;flex-direction:column;gap:6px;'>";
	html += 	"<div>"+title+"</div>";
	
	for(let i=0; i<datas.length; i++)
	{
		let di = datas[i];
		let vs = (CF.isArray(di.value) ? di.value : (CF.isEmpty(di.value) ? [] : [ di.value ]));
		
		html +=	"<div style='display:flex;flex-direction:row;align-items:center;justify-content:space-between;gap:20px;'>";
		html +=		"<div style='display:flex;flex-direction:row;align-items:center;gap:5px;'>";
		
		if(!CF.isEmpty(di.color))
		{
			html +=		"<div style='width:10px;height:10px;border-radius:10px;background:"+di.color+"'></div>";
		}
		
		html +=			"<div>"+di.name+"</div>";
		html +=		"</div>";
		html +=		"<div style='display:flex;flex-direction:row;align-items:center;gap:12px;font-weight:bold;'>";
		
		for(let j=0; j<vs.length; j++)
		{
			html +=		"<div>"+vs[j]+"</div>";
		}
		
		html +=		"</div>";
		html +=	"</div>";
	}
	
	html += "</div>";
	
	return html;
};

/**
 * 准备ECharts图表渲染选项。
 * 
 * @param chart
 * @param renderOptions
 * @param beforeProcessHandler
 * @returns 一个新的图表渲染选项
 */
EU.prepareRenderOptions = function(chart, renderOptions, beforeProcessHandler)
{
	var options = chart.options();
	renderOptions = SPT.trimArrayPropsForMerge(renderOptions, options);
	options = SPT.trimArrayPropsForMerge(options, renderOptions);
	renderOptions = chart.inflateOptions(renderOptions, options);
	
	//使用series[0]作为series后续元素的模板，避免"dg-chart-options"中必须为series每个元素设置type等基础信息
	var series = renderOptions.series;
	if(series && series.length > 1)
	{
		var series0 = CF.extend(true, {}, series[0]);
		
		for(let i=1; i<series.length; i++)
			series[i] = CF.extend(true, {}, series0, series[i]);
	}
	
	if(beforeProcessHandler)
		beforeProcessHandler(chart, renderOptions);
	
	EU.setOptionsComponentId(renderOptions);
	SPT.processRenderOptions(chart, renderOptions);
	//应再次检查和设置组件ID，因为上述函数可能会增加新组件
	EU.setOptionsComponentId(renderOptions);
	
	return renderOptions;
};

/**
 * 准备ECharts图表更新选项。
 * 
 * @param chart
 * @param updateOptions
 * @param beforeProcessHandler
 * @returns 一个新的图表更新选项
 */
EU.prepareUpdateOptions = function(chart, updateOptions, beforeProcessHandler)
{
	var options = chart.options();
	updateOptions = SPT.trimArrayPropsForMerge(updateOptions, options);
	options = SPT.trimArrayPropsForMerge(options, updateOptions);
	updateOptions = chart.inflateOptions(updateOptions, options);
	
	if(beforeProcessHandler)
		beforeProcessHandler(updateOptions, chart);
	
	EU.setOptionsComponentId(updateOptions);
	SPT.processUpdateOptions(chart, updateOptions);
	//应再次检查和设置组件ID，因为上述函数可能会增加新组件
	EU.setOptionsComponentId(updateOptions);
	
	return updateOptions;
};

//设置ECharts选项中的组件ID，必须指定id且不能重复，因为更新操作采用的是replaceMerge模式，必须有对应id
EU.setOptionsComponentId = function(options)
{
	for(let name in options)
	{
		let value = options[name];
		
		if(value == null)
			continue;
		
		if(CF.isArray(value))
		{
			for(let i=0; i<value.length; i++)
			{
				let vi = value[i];
				
				if(vi != null && CF.isPlainObject(vi) && vi.id === undefined)
				{
					vi.id = i;
				}
			}
		}
		else if(CF.isPlainObject(value) && value.id === undefined)
		{
			value.id = 0;
		}
	}
};

//初始化ECharts地图类图表的地图选项
EU.initChartMap = function(chart, options)
{
	var map = CF.optionValue(options, EU.MAP_NAME_OPTION_NAME);
	
	//必须设置初始map，不然渲染会报错
	if(CF.isEmpty(map))
		map = EU.defaultMapName();
	
	//不应替换原始地图名
	var coverOriginalMap = false;
	EU.setMapOption(options, map, coverOriginalMap);
};

/**
 * 获取默认地图名。
 * 地图类图表需要默认地图执行render初始渲染。
 * 注意：返回的默认地图名应是在dashboardFactory.registerBuiltinMap()中注册的。
 */
EU.defaultMapName = function()
{
	//默认中国地图，这里应使用"china"，因为echarts内部只对"china"地图名的地图才会自动绘制右下角的南海诸岛缩略图
	return "china";
};

//渲染ECharts地图类图表
EU.renderMapChart = function(chart, options)
{
	var maps = EU.getDistinctMaps(options);
	EU.registerMap(chart, maps, () =>
	{
		var instance = EU.init(chart);
		instance.setOption(options);
		chart.statusRendered(true);
	});
};

//更新ECharts地图类图表
EU.updateMapChart = function(chart, updateOptions)
{
	var renderOptions = SPT.liveDataRenderOptions(chart);
	var renderMaps = EU.getDistinctMaps(renderOptions);
	var updateMaps = EU.getDistinctMaps(updateOptions);
	var mapChanged = (renderMaps.length !== updateMaps.length);
	
	if(!mapChanged)
	{
		for(let i=0; i<renderMaps.length; i++)
		{
			if(renderMaps[i] != updateMaps[i])
			{
				mapChanged = true;
				break;
			}
		}
	}
	
	if(mapChanged)
		EU.resetMapOptions(updateOptions);
	
	var maps = EU.getDistinctMaps(updateOptions);
	EU.registerMap(chart, maps, () =>
	{
		EU.setOptionsReplaceMerge(chart, updateOptions);
		chart.statusUpdated(true);
	});
};

//仅提取ECharts地图类图表选项中的不重复地图名信息
EU.getDistinctMaps = function(options)
{
	var re = [];
	
	var maps = [];
	var geo = options.geo;
	var series = options.series;
	
	if(geo)
	{
		if(CF.isArray(geo))
		{
			for(let i=0; i<geo.length; i++)
			{
				if(geo[i].map)
				{
					maps.push(geo[i].map);
				}
			}
		}
		else
		{
			if(geo.map)
			{
				maps.push(geo.map);
			}
		}
	}
	
	if(series)
	{
		if(CF.isArray(series))
		{
			for(let i=0; i<series.length; i++)
			{
				if(series[i].type == "map" && series[i].map)
				{
					maps.push(series[i].map);
				}
			}
		}
		else
		{
			if(series.type == "map" && series.map)
			{
				maps.push(series.map);
			}
		}
	}
	
	SPT.appendDistinct(re, maps);
	
	return re;
};

//设置ECharts地图类图表选项中的地图名
EU.setMapOption = function(options, map, force)
{
	var geo = options.geo;
	var series = options.series;
	
	if(geo)
	{
		if(CF.isArray(geo))
		{
			for(let i=0; i<geo.length; i++)
			{
				if(geo[i].map == null || force)
				{
					geo[i].map = map;
				}
			}
		}
		else
		{
			if(geo.map == null || force)
			{
				geo.map = map;
			}
		}
	}
	
	if(series)
	{
		if(CF.isArray(series))
		{
			for(let i=0; i<series.length; i++)
			{
				if(series[i].type == "map" && (series[i].map == null || force))
				{
					series[i].map = map;
				}
			}
		}
		else
		{
			if(series.type == "map" && (series.map == null || force))
			{
				series.map = map;
			}
		}
	}
};

//重置ECharts地图类图表的中心位置、缩放比例
EU.resetMapOptions = function(options)
{
	var geo = options.geo;
	var series = options.series;
	
	if(geo)
	{
		if(CF.isArray(geo))
		{
			for(let i=0; i<geo.length; i++)
			{
				geo[i].center = null;
				geo[i].zoom = 1;
			}
		}
		else
		{
			geo.center = null;
			geo.zoom = 1;
		}
	}
	
	if(series)
	{
		if(CF.isArray(series))
		{
			for(let i=0; i<series.length; i++)
			{
				if(series[i].type == "map")
				{
					series[i].center = null;
					series[i].zoom = 1;
				}
			}
		}
		else
		{
			if(series.type == "map")
			{
				series.center = null;
				series.zoom = 1;
			}
		}
	}
};

/**
 * 将值数组对象（{value: [name, value]}）格式的options.series[i].data元素适配为与options.series[i].type匹配的格式。
 * 比如，对于"pie"的type，应适配为名值对象：{ name: name, value: value }格式，图表才能正确显示。
 * 如果originalSeriesType与options.series[i].type相同，则不进行处理。
 * 
 * 某些内置图表允许修改series[i].type来自定义系列的类型，而不同类型的数据格式规范不同，所以需要适配。
 * 
 * @param chart
 * @param options
 * @param originalSeriesType
 * @param nameIndex 可选，name在值数组对象的索引，默认为：0
 * @param valueIndex 可选，value在值数组对象的索引，默认为：1
 */
EU.adaptValueArrayData = function(chart, options, originalSeriesType, nameIndex, valueIndex)
{
	nameIndex = (nameIndex == null ? 0 : nameIndex);
	valueIndex = (valueIndex == null ? 1 : valueIndex);
	
	var series = (options.series || []);
	
	for(let i=0; i<series.length; i++)
	{
		let si = series[i];
		let type = si.type;
		
		if(type === originalSeriesType)
			continue;
		
		let data = (si.data || []);
		
		//这些图表不支持值数组对象格式的数据，支持名值格式的数据，因此需要适配
		if(type == "pie" || type == "funnel" || type == "map"
			|| type == "wordCloud" || type == "liquidFill")
		{
			data.forEach((di) =>
			{
				let value = (di == null ? null : di.value);
				
				if(value != null)
				{
					di.name = value[nameIndex];
					di.value = value[valueIndex];
				}
			});
			
			//需同时删除encode
			si.encode = null;
		}
	}
};

EU.inflateRendererCommons = function(renderer)
{
	renderer.destroy = function(chart)
	{
		EU.dispose(chart);
	};
	
	renderer.resize = function(chart)
	{
		EU.resize(chart);
	},
	
	/**
	 * 绑定事件处理函数。
	 * 
	 * @param chart
	 * @param type 事件类型，支持格式：
	 * 						1、"click"、"mouseenter"、"click.data"、"mouseenter.data"等事件类型字符串
	 * 						2、{ name: "...", query: ... }，其中name表示事件类型，比如"click"、"mousemove"，query表示过滤条件，同ECharts的on函数的query参数
	 * @param handler 事件处理函数，格式为：function(...){ ... }，参数与底层ECharts相同，函数内部this指向chart
	 */
	renderer.on = function(chart, type, handler)
	{
		type = this._toEventTypeObj(type);
		
		var internal = chart.internal();
		var delegate = function()
		{
			return SPT.invokeEventHandler(chart, handler, arguments);
		};
		
		chart.registerEventHandlerDelegate(type, handler, delegate);
		
		if(type.query == null)
		{
			internal.on(type.name, delegate);
		}
		else
		{
			internal.on(type.name, type.query, delegate);
		}
	},
	
	renderer.off = function(chart, type, handler)
	{
		var internal = chart.internal();
		type = this._toEventTypeObj(type);
		
		var delegates = chart.removeEventHandlerDelegate((d) =>
		{
			return (d.type.name === type.name && d.handler === handler);
		});
		
		delegates.forEach((d) =>
		{
			internal.off(d.type.name, d.delegate);
		});
	};
	
	renderer._toEventTypeObj = function(type)
	{
		var re;
		
		var actualType = SPT.actualEventTypeForData(type);
		if(actualType != null)
			re = { name: actualType, query: "series" };
		else
			re = (CF.isString(type) ? { name: type } : type);
		
		return re;
	};
	
	return renderer;		
};

EU.setOptionsReplaceMerge = function(chart, options, replaceMerge)
{
	if(replaceMerge == null)
	{
		replaceMerge = [];
		
		for(var p in options)
		{
			replaceMerge.push(p);
		}
	}
	
	var opts =
	{
		replaceMerge: replaceMerge
	};
	
	//对于忽略全部数据集的场景，某些图表（饼图/地图散点图等）不会清空画布，需要添加占位系列或数据
	if(options.series != null)
	{
		let series = options.series;
		
		if(series.length == 0)
			series.push({ id: 0 });
		
		for(let i=0; i<series.length; i++)
		{
			if(series[i].data === undefined)
				series[i].data = [];
		}
	}
	
	var internal = chart.internal();
	internal.setOption(options, opts);
};

/**
 * 从updateOptions.series[i].data[i]提取轴数据，并设置为updateAxis.data轴数据。
 * 
 * @param chart
 * @param updateOptions 更新选项，格式应为：{ series: [ { data: [ ... ] } ] }
 * @param updateAxis 要填充轴数据的更新的轴对象，格式应为：{ data: [ { value: ... }、基本类型, ...], ... }
 * @param valueExtractor 轴数据值提取器，格式同EU.sortUpdateAxisData的valueExtractor参数
 * @param sortSeriesData 可选，是否排序系列数据，默认值为：true。
 */
EU.inflateUpdateAxisData = function(chart, updateOptions, updateAxis, valueExtractor, sortSeriesData)
{
	sortSeriesData = (sortSeriesData == null ? true : sortSeriesData);
	
	var axisData = [];
	var indexCache = {};
	var isValueExtractorFunc = CF.isFunction(valueExtractor);
	var isValueExtractorAry = (!isValueExtractorFunc && CF.isArray(valueExtractor));
	var valueExtractors = [];
	
	var series = (updateOptions.series || []);
	
	series.forEach(function(s, i)
	{
		var data = (s.data || []);
		var myData = [];
		var myValueExtractor = null;
		
		if(isValueExtractorFunc)
			myValueExtractor = valueExtractor;
		else if(isValueExtractorAry)
			myValueExtractor = valueExtractor[i];
		else
			myValueExtractor = valueExtractor.get(s);
		
		data.forEach(function(d)
		{
			let v = myValueExtractor(d, s);
			//转换为更规范更易于扩展的{ value: ... }格式
			v = (v != null && v.value !== undefined ? v : { value: v });
			myData.push(v);
		});
		
		valueExtractors.push(myValueExtractor);
		SPT.appendDistinctQuick(axisData, myData, indexCache, "value");
	});
	
	updateAxis.data = axisData;
	
	EU.sortUpdateAxisData(SPT.liveDataRenderOptions(chart), updateOptions, updateAxis, true, sortSeriesData, valueExtractors);
};

/**
 * 依据renderOptions中的排序配置对updateAxis.data、updateOptions.series[i].data进行排序。
 * 
 * @param renderOptions 渲染选项，其中的sortAxisData配置项用于控制数据排序方式，格式为：
 *						"asc"、"ASC"：升序；
 *						"desc"、"DESC"：降序；
 *						自定义排序函数：function(a, b){}；
 *						null、false、其他：不排序；
 *						注意：ECharts对于轴type为"value"、"time"的，仅设置"desc"是无效的，需要把轴type改为"category"
 * @param updateOptions 要排序的更新选项，格式应为：{ series: [ { data: [ ... ] } ] }
 * @param updateAxis 要排序的轴对象，格式应为：{ data: [ { value: ... }、基本类型, ...], ... }
 * @param sortAxisData 是否对updateAxis.data进行排序
 * @param sortSeriesData 是否对updateOptions.series[i].data按照updateAxis.data的顺序重排
 * @param valueExtractor 可选，当sortSeriesData为true时，updateOptions.series[i].data[i]的排序值提取器，格式为：
 *						function(dataEle, seriesEle){}
 *						或者
 *						{ get: function(seriesEle){ return 轴数据值提取器函数对象; } }
 *						或者
 *						[ ... ]
 *						其元素索引与updateOptions.series[i]索引对应
 */
EU.sortUpdateAxisData = function(renderOptions, updateOptions, updateAxis, sortAxisData, sortSeriesData, valueExtractor)
{
	if(!sortAxisData && !sortSeriesData)
		return;
	
	var sortHandler = EU.sortAxisDataOption(renderOptions);
	
	if(sortHandler == null)
		return;
	
	if(CF.isString(sortHandler))
	{
		sortHandler = sortHandler.toLowerCase();
		
		if(sortHandler == "asc")
		{
			sortHandler = function(a, b)
			{
				a = (a != null && a.value !== undefined ? a.value : a);
				b = (b != null && b.value !== undefined ? b.value : b);
				
				if(a == b)
					return 0;
				else
					return (a < b ? -1 : 1);
			};
		}
		else if(sortHandler == "desc")
		{
			sortHandler = function(a, b)
			{
				a = (a != null && a.value !== undefined ? a.value : a);
				b = (b != null && b.value !== undefined ? b.value : b);
				
				if(a == b)
					return 0;
				else
					return (a > b ? -1 : 1);
			};
		}
	}
	else if(CF.isFunction(sortHandler))
	{
		let originalSortHandler = sortHandler;
		sortHandler = function(a, b)
		{
			a = (a != null && a.value !== undefined ? a.value : a);
			b = (b != null && b.value !== undefined ? b.value : b);
			
			return originalSortHandler(a, b);
		};
	}
	
	if(CF.isFunction(sortHandler))
	{
		let axisData = updateAxis.data;
		let isValueExtractorFunc = CF.isFunction(valueExtractor);
		let isValueExtractorAry = (!isValueExtractorFunc && CF.isArray(valueExtractor));
		
		axisData.sort(sortHandler);
		
		if(sortSeriesData)
		{
			let indexCache = {};
			
			axisData.forEach(function(a, i)
			{
				a = (a != null && a.value !== undefined ? a.value : a);
				indexCache[a] = i;
			});
			
			let series = (updateOptions.series || []);
			
			series.forEach(function(s, i)
			{
				let data = (s.data || []);
				let myValueExtractor = null;
				
				if(isValueExtractorFunc)
					myValueExtractor = valueExtractor;
				else if(isValueExtractorAry)
					myValueExtractor = valueExtractor[i];
				else
					myValueExtractor = valueExtractor.get(s);
				
				data.sort(function(da, db)
				{
					let va = myValueExtractor(da, s);
					let vb = myValueExtractor(db, s);
					let ia = indexCache[va];
					let ib = indexCache[vb];
					
					if(ia == ib)
						return 0;
					else
						return (ia < ib ? -1 : 1);
				});
			});
		}
	}
};

EU.sortAxisDataOption = function(options)
{
	return CF.optionValue(options, EU.SORT_AXIS_DATA_OPTION_NAME);
};

EU.axisDataExtractors =
{
	propertyName: function()
	{
		return this.property("name");
	},
	property: function(name)
	{
		var extractor = function(de)
		{
			return (de ? de[name] : null);
		};
		
		return extractor;
	},
	element: function(idx)
	{
		var extractor = function(de)
		{
			return (de ? de[idx] : null);
		};
		
		return extractor;
	},
	valueElement0: function()
	{
		return this.valueElement(0);
	},
	valueElement: function(idx)
	{
		var extractor = function(de)
		{
			return (de && de.value != null ? de.value[idx] : null);
		};
		
		return extractor;
	}
};

//---------------------------------------------------------
//    公用函数结束
//---------------------------------------------------------

})(this);