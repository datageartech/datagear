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

var builtinOptionNames = (CF.builtinOptionNames || (CF.builtinOptionNames = {}));

//内置地图类图表的地图选项名
//默认的ECharts地图类图表配置地图名稍微麻烦，
//所有这里的内置图表都支持此快捷方式设置地图名选项
builtinOptionNames.mapName = "mapName";

//内置类目轴数据排序配置选项名
builtinOptionNames.sortAxisData = "sortAxisData";

SPT.ECHARTS_RENDERER_DEPEND =
[
	{ name: "echarts", acceptVersion: ">=5.0" },
	{ name: "chartUtil.echarts", acceptVersion: ">=1.0" }
];

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
			
			options = SPT.prepareEChartsRenderOptions(chart, options);
			var instance = chartUtil.echarts.init(chart);
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
			
			SPT.inflateEChartsUpdateAxisData(chart, options, (config.interchangeAxis ? options.yAxis : options.xAxis),
					SPT.inflateAxisDataExtractors.valueElement0());
			options = SPT.prepareEChartsUpdateOptions(chart, options, (options) => { SPT.adaptEChartsValueArrayData(chart, options, "line"); });
			
			SPT.echartsOptionsReplaceMerge(chart, options);
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
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
			
			options = SPT.prepareEChartsRenderOptions(chart, options);
			var instance = chartUtil.echarts.init(chart);
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
			
			SPT.inflateEChartsUpdateAxisData(chart, options, (config.interchangeAxis ? options.yAxis : options.xAxis),
							SPT.inflateAxisDataExtractors.valueElement0());
			options = SPT.prepareEChartsUpdateOptions(chart, options, (options) => { SPT.adaptEChartsValueArrayData(chart, options, "bar"); });
			
			SPT.echartsOptionsReplaceMerge(chart, options);
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
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
			
			options = SPT.prepareEChartsRenderOptions(chart, options);
			var instance = chartUtil.echarts.init(chart);
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
			
			SPT.inflateEChartsUpdateAxisData(chart, options, (isAngleAxis ? options.angleAxis : options.radiusAxis),
							SPT.inflateAxisDataExtractors.valueElement0());
			options = SPT.prepareEChartsUpdateOptions(chart, options, (options) => { SPT.adaptEChartsValueArrayData(chart, options, "bar"); });
			
			SPT.echartsOptionsReplaceMerge(chart, options);
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
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
						return SPT.customEChartsTooltip(params, (pi) =>
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
			
			options = SPT.prepareEChartsRenderOptions(chart, options);
			var instance = chartUtil.echarts.init(chart);
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
			
			SPT.inflateEChartsUpdateAxisData(chart, options, options.legend, SPT.inflateAxisDataExtractors.propertyName());
			SPT.convertDataPropValueToName(options.legend);
			this._evalSeriesLayout(chart, options);
			options = SPT.prepareEChartsUpdateOptions(chart, options);
			
			SPT.echartsOptionsReplaceMerge(chart, options);
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
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
			
			options = SPT.prepareEChartsRenderOptions(chart, options);
			var instance = chartUtil.echarts.init(chart);
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
			options = SPT.prepareEChartsUpdateOptions(chart, options);
			
			SPT.echartsOptionsReplaceMerge(chart, options);
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
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
			
			options = SPT.prepareEChartsRenderOptions(chart, options);
			var instance = chartUtil.echarts.init(chart);
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
			
			SPT.inflateEChartsUpdateAxisData(chart, options, (config.interchangeAxis ? options.yAxis : options.xAxis),
							SPT.inflateAxisDataExtractors.valueElement0());
			options = SPT.prepareEChartsUpdateOptions(chart, options, (options) => { SPT.adaptEChartsValueArrayData(chart, options, config.scatterType); });
			
			SPT.echartsOptionsReplaceMerge(chart, options);
		},
		
		_configSingleSeries: function(chart, series)
		{
			series.type = config.scatterType;
			series.encode = (config.interchangeAxis ? { x: 1, y: 0 } : { x: 0, y: 1 });
		}
	};
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
			
			options = SPT.prepareEChartsRenderOptions(chart, options);
			var instance = chartUtil.echarts.init(chart);
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
			
			SPT.inflateEChartsUpdateAxisData(chart, options, (config.interchangeAxis ? options.yAxis : options.xAxis),
							SPT.inflateAxisDataExtractors.valueElement0());
			options = SPT.prepareEChartsUpdateOptions(chart, options, (options) => { SPT.adaptEChartsValueArrayData(chart, options, config.scatterType); });
			
			SPT.echartsOptionsReplaceMerge(chart, options);
		},
		
		_configSingleSeries: function(chart, series, hasWeightField)
		{
			series.type = config.scatterType;
			series.encode = (config.interchangeAxis ? { x: 1, y: 0 } : { x: 0, y: 1 });
			
			if(hasWeightField)
				series.encode.tooltip = [1, 2];
		}
	};
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
			
			options = SPT.prepareEChartsRenderOptions(chart, options);
			var instance = chartUtil.echarts.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var renderOptions = chart.renderOptions();
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
			
			if(SPT.sortAxisDataOption(renderOptions))
			{
				let tmpAxisData = [];
				indicatorData.forEach((indicator) =>
				{
					tmpAxisData.push(indicator.name);
				});
				
				let tmpOptions = { tmpAxis: { data: tmpAxisData }, series: tmpSeries };
				
				SPT.sortEChartsUpdateAxisData(renderOptions, tmpOptions, tmpOptions.tmpAxis,
								true, true, SPT.inflateAxisDataExtractors.propertyName());
				
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
			options = SPT.prepareEChartsUpdateOptions(chart, options);
			
			SPT.echartsOptionsReplaceMerge(chart, options);
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
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
			
			options = SPT.prepareEChartsRenderOptions(chart, options);
			var instance = chartUtil.echarts.init(chart);
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
			options = SPT.prepareEChartsUpdateOptions(chart, options);
			
			SPT.echartsOptionsReplaceMerge(chart, options);
		}
	};
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
			
			options = SPT.prepareEChartsRenderOptions(chart, options,
				(chart, renderOptions) =>
				{
					SPT.echartsMapChartInitMap(chart, renderOptions);
				});
			
			SPT.echartsMapChartRender(chart, options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var mapSignIndex = 2;
			
			var seriesName = "";
			var seriesData = [];
			var dataRange = { min: null, max: null };
			var map = null;
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let dataSetAlias = chart.dataSetAlias(dataSetBind);
				let result = chart.resultOf(chartResult, dataSetBind);
				
				let nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
				let valueField = chart.dataSetFieldOfSign(dataSetBind, 1);
				
				if(CF.isEmpty(map))
					map = SPT.resultFirstNonEmptyValueOfSign(chart, dataSetBind, result, mapSignIndex);
				
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
			
			if(!CF.isEmpty(map))
				series[0].map = map;
			
			var options = { visualMap: visualMap, series: series };
			options = SPT.prepareEChartsUpdateOptions(chart, options);
			SPT.echartsMapChartUpdate(chart, options);
		}
	};
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
						return SPT.customEChartsTooltip(params, (pi) =>
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
			
			options = SPT.prepareEChartsRenderOptions(chart, options,
				(chart, renderOptions) =>
				{
					SPT.echartsMapChartInitMap(chart, renderOptions);
				});
			
			SPT.echartsMapChartRender(chart, options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var mapSignIndex = 5;
			
			var legendData = [];
			var series = [];
			var map = null;
			
			var dataRange = { min: null, max: null };
			var symbolSizeRatio = SPT.symbolSizeRadioIfEffectScatter(config.scatterType);
			var symbolSizeMax = SPT.evalSymbolSizeMax(chart, symbolSizeRatio);
			var symbolSizeMin = SPT.evalSymbolSizeMin(chart, symbolSizeMax);
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let dataSetAlias = chart.dataSetAlias(dataSetBind);
				let result = chart.resultOf(chartResult, dataSetBind);
				
				if(CF.isEmpty(map))
					map = SPT.resultFirstNonEmptyValueOfSign(chart, dataSetBind, result, mapSignIndex);
				
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
			
			if(!CF.isEmpty(map))
				options.geo = { map: map };
			
			options = SPT.prepareEChartsUpdateOptions(chart, options);
			SPT.echartsMapChartUpdate(chart, options);
		},
		
		_configSingleSeries: function(chart, series, hasValueField)
		{
			series.type = config.scatterType;
			series.coordinateSystem = "geo";
			//series.encode = { tooltip: (hasValueField ? [2] : []) };
		}
	};
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
						return SPT.customEChartsTooltip(params, (pi) =>
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
			
			options = SPT.prepareEChartsRenderOptions(chart, options,
				(chart, renderOptions) =>
				{
					SPT.echartsMapChartInitMap(chart, renderOptions);
				});
			
			SPT.echartsMapChartRender(chart, options);
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
			
			this._configSingleSeries(chart, options.series[0]);
			SPT.evalSeriesDataValueSymbolSize(options.series, dataRange.min, dataRange.max, symbolSizeMax, symbolSizeMin, "value", 2);
			
			options = SPT.prepareEChartsUpdateOptions(chart, options);
			SPT.echartsMapChartUpdate(chart, options);
		},
		
		_inflateCommonOptions: function(chart, chartResult, dataSetBinds, dsbIndex, options)
		{
			var mapSignIndex = 3;
			var dataSetBind = dataSetBinds[dsbIndex];
			
			if(CF.isEmpty(options.series[0].name))
				options.series[0].name = chart.dataSetAlias(dataSetBind);
			
			if(!options.geo || !options.geo.map)
			{
				let result = chart.resultOf(chartResult, dataSetBind);
				let map = SPT.resultFirstNonEmptyValueOfSign(chart, dataSetBind, result, mapSignIndex);
				
				if(!CF.isEmpty(map))
				{
					options.geo = (options.geo || {});
					options.geo.map = map;
				}
			}
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
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
			
			options = SPT.prepareEChartsRenderOptions(chart, options,
				(chart, renderOptions) =>
				{
					SPT.echartsMapChartInitMap(chart, renderOptions);
				});
			
			SPT.echartsMapChartRender(chart, options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var mapSignIndex = 3;
			
			var legendData = [];
			var series = [];
			var map = null;
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let dataSetAlias = chart.dataSetAlias(dataSetBind);
				let result = chart.resultOf(chartResult, dataSetBind);
				
				let nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
				let loField = chart.dataSetFieldOfSign(dataSetBind, 1);
				let laField = chart.dataSetFieldOfSign(dataSetBind, 2);
				
				if(CF.isEmpty(map))
					map = SPT.resultFirstNonEmptyValueOfSign(chart, dataSetBind, result, mapSignIndex);
				
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
			
			if(!CF.isEmpty(map))
				options.geo = { map: map };
			
			options = SPT.prepareEChartsUpdateOptions(chart, options);
			SPT.echartsMapChartUpdate(chart, options);
		}
	};
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
						return SPT.customEChartsTooltip(params);
					}
				},
				legend: { data: [] },
				geo: { roam: true },
				series:
				[
					{ type: "lines", data: [], coordinateSystem: "geo", polyline: false }
				]
			};
			
			options = SPT.prepareEChartsRenderOptions(chart, options,
				(chart, renderOptions) =>
				{
					SPT.echartsMapChartInitMap(chart, renderOptions);
				});
			
			SPT.echartsMapChartRender(chart, options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var mapSignIndex = 6;
			
			var legendData = [];
			var categoryNames = [];
			var categoryDatasMap = {};
			var map = null;
			
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
				
				if(CF.isEmpty(map))
					map = SPT.resultFirstNonEmptyValueOfSign(chart, dataSetBind, result, mapSignIndex);
				
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
			
			if(!CF.isEmpty(map))
				options.geo = { map: map };
			
			options = SPT.prepareEChartsUpdateOptions(chart, options);
			SPT.echartsMapChartUpdate(chart, options);
		}
	};
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
			
			options = SPT.prepareEChartsRenderOptions(chart, options,
				(chart, renderOptions) =>
				{
					SPT.echartsMapChartInitMap(chart, renderOptions);
				});
			
			SPT.echartsMapChartRender(chart, options);
		},
		
		update: function(chart, chartResult)
		{
			var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
			var mapSignIndex = 4;
			
			var seriesName = "";
			var seriesData = [];
			var dataRange = { min: null, max: null };
			var map = null;
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let dataSetBind = dataSetBinds[i];
				let dataSetAlias = chart.dataSetAlias(dataSetBind);
				let result = chart.resultOf(chartResult, dataSetBind);
				
				let nameField = chart.dataSetFieldOfSign(dataSetBind, 0);
				let longitudeField = chart.dataSetFieldOfSign(dataSetBind, 1);
				let latitudeField = chart.dataSetFieldOfSign(dataSetBind, 2);
				let valueField = chart.dataSetFieldOfSign(dataSetBind, 3);
				
				if(CF.isEmpty(map))
					map = SPT.resultFirstNonEmptyValueOfSign(chart, dataSetBind, result, mapSignIndex);
				
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
			
			if(!CF.isEmpty(map))
				options.geo = { map: map };
			
			options = SPT.prepareEChartsUpdateOptions(chart, options);
			SPT.echartsMapChartUpdate(chart, options);
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
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
			
			options = SPT.prepareEChartsRenderOptions(chart, options);
			var instance = chartUtil.echarts.init(chart);
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
			
			SPT.inflateEChartsUpdateAxisData(chart, options, options.xAxis,
							SPT.inflateAxisDataExtractors.propertyName());
			
			options = SPT.prepareEChartsUpdateOptions(chart, options);
			
			SPT.echartsOptionsReplaceMerge(chart, options);
		},
		
		_configSingleSeries: function(chart, series)
		{
			series.type = "candlestick";
			series.encode = { x: 0, y: [ 1,2,3,4 ], tooltip: [1, 2, 3, 4] };
		}
	};
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
			
			options = SPT.prepareEChartsRenderOptions(chart, options);
			var instance = chartUtil.echarts.init(chart);
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
			
			SPT.inflateEChartsUpdateAxisData(chart, options, options.xAxis,
							SPT.inflateAxisDataExtractors.valueElement(0));
			SPT.inflateEChartsUpdateAxisData(chart, options, options.yAxis,
							SPT.inflateAxisDataExtractors.valueElement(1), false);
			
			options = SPT.prepareEChartsUpdateOptions(chart, options);
			
			SPT.echartsOptionsReplaceMerge(chart, options);
		},
		
		_configSingleSeries: function(chart, series)
		{
			series.type = "heatmap";
			series.encode = { x: 0, y: 1, tooltip: 2 };
		}
	};
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
			
			options = SPT.prepareEChartsRenderOptions(chart, options);
			var instance = chartUtil.echarts.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			//树图只支持单根节点
			var singleRootNode = true;
			var singleSeries = SPT.inflateTreeNodeSingleSeries(chart, chartResult, { type: "tree" }, singleRootNode);
			var options = { series: [ singleSeries ] };
			
			this._inflateUpdateOptions(chart, options);
			options = SPT.prepareEChartsUpdateOptions(chart, options);
			
			SPT.echartsOptionsReplaceMerge(chart, options);
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
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
			
			options = SPT.prepareEChartsRenderOptions(chart, options);
			var instance = chartUtil.echarts.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var singleSeries = SPT.inflateTreeNodeSingleSeries(chart, chartResult, { type: "treemap" });
			var options = { series: [ singleSeries ] };
			options = SPT.prepareEChartsUpdateOptions(chart, options);
			
			SPT.echartsOptionsReplaceMerge(chart, options);
		}
	};
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
			
			options = SPT.prepareEChartsRenderOptions(chart, options);
			var instance = chartUtil.echarts.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var singleSeries = SPT.inflateTreeNodeSingleSeries(chart, chartResult, { type: "sunburst" });
			var options = { series: [ singleSeries ] };
			options = SPT.prepareEChartsUpdateOptions(chart, options);
			
			SPT.echartsOptionsReplaceMerge(chart, options);
		}
	};
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
						return SPT.customEChartsTooltip(params, (pi) =>
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
			
			options = SPT.prepareEChartsRenderOptions(chart, options);
			var instance = chartUtil.echarts.init(chart);
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
			options = SPT.prepareEChartsUpdateOptions(chart, options);
			SPT.echartsOptionsReplaceMerge(chart, options);
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
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
						return SPT.customEChartsTooltip(params, (pi) =>
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
			
			options = SPT.prepareEChartsRenderOptions(chart, options);
			var instance = chartUtil.echarts.init(chart);
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
			
			options = SPT.prepareEChartsUpdateOptions(chart, options);
			SPT.echartsOptionsReplaceMerge(chart, options);
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
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
			
			options = SPT.prepareEChartsRenderOptions(chart, options);
			var instance = chartUtil.echarts.init(chart);
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
			
			SPT.inflateEChartsUpdateAxisData(chart, options, (config.interchangeAxis ? options.yAxis : options.xAxis),
							{
								get: function(s)
								{
									if(s.type == "boxplot")
										return SPT.inflateAxisDataExtractors.propertyName();
									else
										return SPT.inflateAxisDataExtractors.valueElement0();
								}
							});
			options = SPT.prepareEChartsUpdateOptions(chart, options);
			
			SPT.echartsOptionsReplaceMerge(chart, options);
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
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
			
			options = SPT.prepareEChartsRenderOptions(chart, options);
			var instance = chartUtil.echarts.init(chart);
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
			options = SPT.prepareEChartsUpdateOptions(chart, options);
			
			SPT.echartsOptionsReplaceMerge(chart, options);
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
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
								borderColor: chartTheme.borderColor,
								shadowColor: chart.themeGradualColor(0.4)
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
			
			options = SPT.prepareEChartsRenderOptions(chart, options);
			var instance = chartUtil.echarts.init(chart);
			instance.setOption(options);
		},
		
		update: function(chart, chartResult)
		{
			var renderOptions = chart.renderOptions();
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
			options = SPT.prepareEChartsUpdateOptions(chart, options);
			
			SPT.echartsOptionsReplaceMerge(chart, options);
		}
	};
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
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
			
			options = SPT.prepareEChartsRenderOptions(chart, options);
			var instance = chartUtil.echarts.init(chart);
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
			options = SPT.prepareEChartsUpdateOptions(chart, options);
			
			SPT.echartsOptionsReplaceMerge(chart, options);
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
	
	SPT.inflateEChartsRendererCommonFuncs(renderer);
	return renderer;
};

//主题河流图

SPT.themeRiverRender = function(chart, options)
{
	options = CF.extend(true,
	{
		dg:
		{
			//name 河流X轴坐标，通常是数值、日期
			//value 河流数值，当标记category时单选，否则可多选，每一列作为一条河流
			//category 可选，类别，不同类别绘制为不同系列
			dataSignNames: { name: "name", value: "value", category: "category" }
		}
	},
	options);
	
	var dataSignNames = options.dg.dataSignNames;
	var dataSetBind = chart.dataSetBindMain();
	var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
	
	options = SPT.inflateRenderOptions(chart,
	{
		title:
		{
	        text: chart.name()
	    },
		tooltip:
		{
			show: true,
			trigger: 'axis'
		},
		legend:
		{
			id: 0,
			//将在update中设置：
			//data
		},
		singleAxis:
		{
			id: 0,
			type: SPT.evalDataSetFieldAxisType(chart, np),
			//ECharts-5.3.2版本主题配置不起作用，所以这里配置
			"left": "10%",
            "top": "24%",
            "right": "10%",
            "bottom": "10%",
		},
		series:
		[
			//将在update中设置：
			//{}
			//设初值以免渲染报错
			{
				id: 0,
				type: "themeRiver"
			}
		]
	},
	options);
	
	chart.echartsInit(options);
};

SPT.themeRiverUpdate = function(chart, chartResult)
{
	var renderOptions= chart.renderOptions();
	var dg = renderOptions.dg;
	var dataSignNames = dg.dataSignNames;
	
	var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
	
	var legendData = [];
	var seriesData = [];
	
	for(var i=0; i<dataSetBinds.length; i++)
	{
		var dataSetBind = dataSetBinds[i];
		var dataSetAlias = chart.dataSetAlias(dataSetBind);
		var result = chart.resultOf(chartResult, dataSetBind);
		
		var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
		var cp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.category);
		
		if(cp)
		{
			var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
			
			//主题河流图只支持[ name, value, category ]格式的数据条目
			var data = chart.resultRowArrayDatas(result, [ np, vp, cp ]);
			chart.originalDataIndexes(data, dataSetBind);
			
			//为类别添加前缀，确保多数据集类别不重复
			for(var j=0; j<data.length; j++)
			{
				var myCategory = SPT.legendNameForDataCategory(dataSetBinds, dataSetAlias, data[j][2]);
				data[j][2] = myCategory;
				
				SPT.appendDistinct(legendData, myCategory);
			}
			
			SPT.appendElement(seriesData, data);
		}
		else
		{
			var vps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.value);
			
			for(var j=0; j<vps.length; j++)
			{
				var legendName = SPT.legendNameForDataValues(chart, dataSetBinds, dataSetBind, dataSetAlias, vps, j);
				//主题河流图只支持[ name, value, lengendName ]格式的数据条目
				var data = chart.resultRowArrayDatas(result, [ np, vps[j] ]);
				for(var k=0; k<data.length; k++)
					data[k].push(legendName);
				
				chart.originalDataIndexes(data, dataSetBind);
				
				SPT.appendDistinct(legendData, legendName);
				SPT.appendElement(seriesData, data);
			}
		}
	}
	
	var series =
	{
		id: 0,
		type: "themeRiver",
		data: seriesData
	};
	
	//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
	var options = { legend: { id: 0, data: legendData}, series: [ series ], singleAxis: { id: 0 } };
	
	SPT.inflateEChartsUpdateAxisData(chart, options, options.singleAxis,
					SPT.inflateAxisDataExtractors.element(0));
	
	SPT.adaptArrayPropsForUpdateOptions(options, renderOptions);
	options = chart.inflateUpdateOptions(chartResult, options);
	
	SPT.echartsOptionsReplaceMerge(chart, options);
};

SPT.themeRiverResize = function(chart)
{
	SPT.resizeChartEcharts(chart);
};

SPT.themeRiverDestroy = function(chart)
{
	SPT.destroyChartEcharts(chart);
};

SPT.themeRiverOn = function(chart, eventType, handler)
{
	SPT.bindChartEventHandlerForEcharts(chart, eventType, handler,
			SPT.themeRiverSetChartEventData);
};

SPT.themeRiverOff = function(chart, eventType, handler)
{
	chart.echartsOffEventHandler(eventType, handler);
};

SPT.themeRiverSetChartEventData = function(chart, chartEvent, echartsEventParams)
{
	var renderOptions= chart.renderOptions();
	var dg = renderOptions.dg;
	var dataSignNames = dg.dataSignNames;
	
	//TODO ECharts-5.3.2版本这里的数据与实际的事件数据不匹配，可能是BUG
	var echartsData = echartsEventParams.data;
	
	var data = undefined;
	
	if(echartsData)
	{
		data = {};
		data[dataSignNames.name] = echartsData[0];
		data[dataSignNames.value] = echartsData[1];
		data[dataSignNames.category] = echartsData[2];
	}
	
	chart.eventData(chartEvent, data);
	chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
};

//象形柱图

SPT.pictorialBarSymbolPaths=
{
	//星型
	"star" : "path://m15.5,19c-0.082,0 -0.164,-0.02 -0.239,-0.061l-5.261,-2.869l-5.261,2.869c-0.168,0.092 -0.373,0.079 -0.529,-0.032s-0.235,-0.301 -0.203,-0.49l0.958,-5.746l-3.818,-3.818c-0.132,-0.132 -0.18,-0.328 -0.123,-0.506s0.209,-0.31 0.394,-0.341l5.749,-0.958l2.386,-4.772c0.085,-0.169 0.258,-0.276 0.447,-0.276s0.363,0.107 0.447,0.276l2.386,4.772l5.749,0.958c0.185,0.031 0.337,0.162 0.394,0.341s0.01,0.374 -0.123,0.506l-3.818,3.818l0.958,5.746c0.031,0.189 -0.048,0.379 -0.203,0.49c-0.086,0.061 -0.188,0.093 -0.29,0.093z",
};

SPT.pictorialBarRender = function(chart, options)
{
	options = CF.extend(true,
	{
		dg:
		{
			//name 名称
			//value 数值，当标记category时单选，否则可多选
			//category 可选，类别，不同类别绘制为不同系列
			dataSignNames: { name: "name", value: "value", category: "category" },
			//是否横向
			horizontal: false,
			//图形类型
			symbol: "circle",
			//图形重复
			symbolRepeat: true,
			//柱条间距
			barGap: "100%"
		}
	},
	options);
	
	var dataSignNames = options.dg.dataSignNames;
	var dataSetBind = chart.dataSetBindMain();
	var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
	var vps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.value);
	
	options = SPT.inflateRenderOptions(chart,
	{
		dg:
		{
			symbolSize: (vps.length > 1 ? "100%" : "50%")
		},
		title:
		{
	        text: chart.name()
	    },
		tooltip:
		{
			trigger: "item"
		},
		legend:
		{
			id: 0,
			//将在update中设置：
			//data
		},
		xAxis:
		{
			id: 0,
			name: chart.dataSetFieldAlias(dataSetBind, np),
			nameGap: 5,
			type: SPT.evalDataSetFieldAxisType(chart, np),
			splitLine: { show: false }
		},
		yAxis:
		{
			id: 0,
			name: (vps.length == 1 ? chart.dataSetFieldAlias(dataSetBind, vps[0]) : ""),
			nameGap: 5,
			type: "value"
		},
		series:
		[
			//将在update中设置：
			//{}
			//设初值以免渲染报错
			{
				id: 0,
				type: "pictorialBar"
			}
		]
	},
	options,
	function(options)
	{
		if(options.dg.horizontal)
		{
			var xAxisTmp = options.xAxis;
			options.xAxis = options.yAxis;
			options.yAxis = xAxisTmp;
			
			//横向柱状图的yAxis.type不能为value，不然会变为竖向图形
			if(options.yAxis.type == "value")
				options.yAxis.type = "category";
		}
	});
	
	chart.echartsInit(options);
};

SPT.pictorialBarUpdate = function(chart, chartResult)
{
	var renderOptions= chart.renderOptions();
	var dg = renderOptions.dg;
	var dataSignNames = dg.dataSignNames;
	
	var symbol = dg.symbol;
	if(SPT.pictorialBarSymbolPaths[symbol])
		symbol = SPT.pictorialBarSymbolPaths[symbol];
	
	var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
	
	var legendData = [];
	var series = [];
	
	for(var i=0; i<dataSetBinds.length; i++)
	{
		var dataSetBind = dataSetBinds[i];
		var dataSetAlias = chart.dataSetAlias(dataSetBind);
		var result = chart.resultOf(chartResult, dataSetBind);
		
		var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
		var cp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.category);
		
		if(cp)
		{
			var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
			
			var categoryNames = [];
			var categoryDatasMap = {};
			
			//使用{value: [name,value]}格式可以更好地兼容category、value、time坐标轴类型
			var propertyMap = { "value": [np, vp] }; 
			propertyMap = SPT.addCategoryToFieldMap(propertyMap, cp);
			var data = chart.resultMapDatas(result, propertyMap);
			chart.originalDataIndexes(data, dataSetBind);
			SPT.splitDataByCategory(data, categoryNames, categoryDatasMap);
			
			for(var j=0; j<categoryNames.length; j++)
			{
				var categoryName = categoryNames[j];
				var legendName = SPT.legendNameForDataCategory(dataSetBinds, dataSetAlias, categoryName);
				var mySeries =
				{
					id: series.length, type: "pictorialBar", name: legendName, data: categoryDatasMap[categoryName],
					symbol: symbol,
					symbolSize: dg.symbolSize, symbolRepeat: dg.symbolRepeat,
					barGap: dg.barGap
				};
				
				if(dg.horizontal)
				{
					mySeries.encode = { x: 1, y: 0 };
				}
				
				legendData.push(legendName);
				series.push(mySeries);
			}
		}
		else
		{
			var vps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.value);
			
			for(var j=0; j<vps.length; j++)
			{
				var legendName = SPT.legendNameForDataValues(chart, dataSetBinds, dataSetBind, dataSetAlias, vps, j);
				
				//使用{value: [name,value]}格式可以更好地兼容category、value、time坐标轴类型
				var vpsMy = [np, vps[j]];
				var data = chart.resultValueDatas(result, vpsMy);
				
				chart.originalDataIndexes(data, dataSetBind);
				
				var mySeries =
				{
					id: series.length, type: "pictorialBar", name: legendName, data: data,
					symbol: symbol,
					symbolSize: dg.symbolSize, symbolRepeat: dg.symbolRepeat,
					barGap: dg.barGap
				};
				
				if(dg.horizontal)
				{
					mySeries.encode = { x: 1, y: 0 };
				}
				
				legendData.push(legendName);
				series.push(mySeries);
			}
		}
	}
	
	var options = { legend: {id: 0, data: legendData}, series: series };
	
	//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
	if(dg.horizontal)
		options.yAxis = { id: 0 };
	else
		options.xAxis = { id: 0 };
	
	SPT.inflateEChartsUpdateAxisData(chart, options, (dg.horizontal ? options.yAxis : options.xAxis),
					SPT.inflateAxisDataExtractors.valueElement(0));
	
	SPT.adaptArrayPropsForUpdateOptions(options, renderOptions);
	
	options = chart.inflateUpdateOptions(chartResult, options, function(options)
	{
		SPT.adaptEChartsValueArrayData(chart, options, "pictorialBar");
	});
	
	SPT.echartsOptionsReplaceMerge(chart, options);
};

SPT.pictorialBarResize = function(chart)
{
	SPT.resizeChartEcharts(chart);
};

SPT.pictorialBarDestroy = function(chart)
{
	SPT.destroyChartEcharts(chart);
};

SPT.pictorialBarOn = function(chart, eventType, handler)
{
	SPT.bindChartEventHandlerForEcharts(chart, eventType, handler,
			SPT.pictorialBarSetChartEventData);
};

SPT.pictorialBarOff = function(chart, eventType, handler)
{
	chart.echartsOffEventHandler(eventType, handler);
};

SPT.pictorialBarSetChartEventData = function(chart, chartEvent, echartsEventParams)
{
	var renderOptions= chart.renderOptions();
	var dg = renderOptions.dg;
	var dataSignNames = dg.dataSignNames;
	
	var echartsData = echartsEventParams.data;
	var data = SPT.extractNameValueStyleObj(echartsData, dataSignNames.name, dataSignNames.value);
	data[dataSignNames.category] = SPT.categoryValueOfData(echartsData);
	
	chart.eventData(chartEvent, data);
	chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
};

//象形进度柱图

SPT.pictorialBarProgressRender = function(chart, options)
{
	options = CF.extend(true,
	{
		dg:
		{
			//name 名称，必选，单选
			//value 值，必选，单选
			//max 最大值，可选，单选，默认：100
			dataSignNames: { name: "name", value: "value", max: "max" },
			//是否横向
			horizontal: false,
			//图形类型
			symbol: "rect",
			//图形尺寸
			symbolSize: ["100%", "100%"],
			//图形重复
			symbolRepeat: false,
			//背景图形重复
			symbolRepeatForBg: false,
			//图形间距
			symbolMargin: 0,
			//柱条间距
			barGap: "-100%",
			//最大值
			max: 100,
		}
	},
	options);
	
	var dataSignNames = options.dg.dataSignNames;
	var dataSetBind = chart.dataSetBindMain();
	var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
	var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
	
	options = SPT.inflateRenderOptions(chart,
	{
		title:
		{
	        text: chart.name()
	    },
		tooltip:
		{
			trigger: "item"
		},
		legend:
		{
			id: 0,
			//将在update中设置：
			//data
		},
		xAxis:
		{
			id: 0,
			name: chart.dataSetFieldAlias(dataSetBind, np),
			nameGap: 5,
			type: SPT.evalDataSetFieldAxisType(chart, np),
			splitLine: { show: false }
		},
		yAxis:
		{
			id: 0,
			name: chart.dataSetFieldAlias(dataSetBind, vp),
			nameGap: 5,
			type: "value",
			splitLine: { show: false }
		},
		series:
		[
			//将在update中设置：
			//{}
			//设初值以免渲染报错
			{
				id: 0,
				type: "pictorialBar"
			}
		]
	},
	options,
	function(options)
	{
		if(options.dg.horizontal)
		{
			var xAxisTmp = options.xAxis;
			options.xAxis = options.yAxis;
			options.yAxis = xAxisTmp;
			
			//横向柱状图的yAxis.type不能为value，不然会变为竖向图形
			if(options.yAxis.type == "value")
				options.yAxis.type = "category";
		}
	});
	
	chart.echartsInit(options);
};

SPT.pictorialBarProgressUpdate = function(chart, chartResult)
{
	var renderOptions= chart.renderOptions();
	var dg = renderOptions.dg;
	var dataSignNames = dg.dataSignNames;
	
	var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
	
	var seriesName = "";
	var seriesData = [];
	var maxValue = null;
	
	for(var i=0; i<dataSetBinds.length; i++)
	{
		var dataSetBind = dataSetBinds[i];
		var dataSetAlias = chart.dataSetAlias(dataSetBind);
		var result = chart.resultOf(chartResult, dataSetBind);
		
		var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
		var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
		
		//使用{value: [name,value]}格式可以更好地兼容category、value、time坐标轴类型
		var data = chart.resultValueDatas(result, [np, vp]);
		
		chart.originalDataIndexes(data, dataSetBind);
		
		//取任一不为空的地图名列值
		if(maxValue == null)
			maxValue = SPT.resultFirstNonEmptyValueOfSign(chart, dataSetBind, result, dataSignNames.max);
		
		seriesData = seriesData.concat(data);
		
		if(!seriesName)
			seriesName = dataSetAlias;
	}
	
	maxValue = (maxValue == null ? dg.max : maxValue);
	
	var symbol = dg.symbol;
	if(SPT.pictorialBarSymbolPaths[symbol])
		symbol = SPT.pictorialBarSymbolPaths[symbol];
	
	var series =
	[
		{
			id: 0,
			type: "pictorialBar",
			name: seriesName,
			data: seriesData,
			symbol: symbol,
			symbolSize: dg.symbolSize,
			symbolRepeat: dg.symbolRepeat,
			barGap: dg.barGap,
			symbolBoundingData: maxValue,
			symbolClip: true,
			symbolMargin: dg.symbolMargin,
			z: 10
		},
		{
			id: 1,
			type: "pictorialBar",
			name: seriesName+"-background",
			data: seriesData,
			symbol: symbol,
			symbolSize: dg.symbolSize,
			symbolRepeat: dg.symbolRepeatForBg,
			barGap: dg.barGap,
			symbolBoundingData: maxValue,
			symbolClip: false,
			symbolMargin: dg.symbolMargin,
			z: 1,
			animationDuration: 0,
			itemStyle:{ color: chart.themeGradualColor(0.2) },
			silent: true
		}
	];
	
	if(dg.horizontal)
	{
		series[0].encode = { x: 1, y: 0 };
		series[1].encode = { x: 1, y: 0 };
	}
	
	var options = { legend: {id: 0, data: [ seriesName ]}, series: series };
	
	//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
	if(dg.horizontal)
	{
		options.xAxis = { id: 0, max: maxValue };
		options.yAxis = { id: 0 };
	}
	else
	{
		options.xAxis = { id: 0 };
		options.yAxis = { id: 0, max: maxValue };
	}
	
	SPT.inflateEChartsUpdateAxisData(chart, options, (dg.horizontal ? options.yAxis : options.xAxis),
					SPT.inflateAxisDataExtractors.valueElement(0));
	
	SPT.adaptArrayPropsForUpdateOptions(options, renderOptions);
	
	options = chart.inflateUpdateOptions(chartResult, options, function(options)
	{
		SPT.adaptEChartsValueArrayData(chart, options, "pictorialBar");
	});
	
	SPT.echartsOptionsReplaceMerge(chart, options);
};

SPT.pictorialBarProgressResize = function(chart)
{
	SPT.resizeChartEcharts(chart);
};

SPT.pictorialBarProgressDestroy = function(chart)
{
	SPT.destroyChartEcharts(chart);
};

SPT.pictorialBarProgressOn = function(chart, eventType, handler)
{
	SPT.bindChartEventHandlerForEcharts(chart, eventType, handler,
			SPT.pictorialBarProgressSetChartEventData);
};

SPT.pictorialBarProgressOff = function(chart, eventType, handler)
{
	chart.echartsOffEventHandler(eventType, handler);
};

SPT.pictorialBarProgressSetChartEventData = function(chart, chartEvent, echartsEventParams)
{
	var renderOptions= chart.renderOptions();
	var dg = renderOptions.dg;
	var dataSignNames = dg.dataSignNames;
	
	var echartsData = echartsEventParams.data;
	var data = SPT.extractNameValueStyleObj(echartsData, dataSignNames.name, dataSignNames.value);
	
	chart.eventData(chartEvent, data);
	chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
};

//表格

SPT.tableRender = function(chart, options)
{
	options = CF.extend(true,
	{
		dg:
		{
			dataSignNames: { column: "column" }
		}
	},
	options);
	
	var dataSignNames = options.dg.dataSignNames;
	var chartEle = chart.elementJquery();
	chartEle.addClass("dg-chart-table");
	var isV1 = SPT.tableIsV1();
	chartEle.addClass(isV1 ? "dg-table-v1" : "dg-table-v2");
	
	var columns = SPT.tableGetFieldColumns(chart, dataSignNames.column);
	
	if(columns.length == 0)
		throw new Error("Column required for rendering table in chart '"+chart.name()+"'");
	
	options = SPT.inflateRenderOptions(chart,
	{
		//标题配置
		title:
		{
			show: true,
			text: chart.name()
		},
		//标题样式，格式为：{ color:'red', 'background-color':'blue' }
		titleStyle: undefined,
		//表格样式，格式为：
		//{
		//	table: {...},
		//	head: { row: {...}, cell: {...} },
		//	body:
		//	{
		//		row: {...}, rowOdd: {}, rowEven: {}, rowHover: {...}, rowSelected: {...},
		//		cell: {...}, cellOdd: {}, cellEven: {}, cellHover: {...}, cellSelected: {...}
		//	}
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
		//是否表格文本不换行
		enableWrapText: false,
		
		//DataTable配置项
		"columns": columns,
		"data" : [],
		"ordering": false,
		"scrollX": true,
		"scrollY": undefined,
		"autoWidth": true,
        "scrollCollapse": false,
		"pagingType": "full_numbers",
		"lengthMenu": [],
		"pageLength": 50,
		"select" : { style : 'os' },
		"searching" : false,
		"language":
	    {
			"emptyTable": "",
			"zeroRecords": "",
			"search": "搜索",
			"lengthMenu": "每页_MENU_条",
			"info": "共_TOTAL_条，当前_START_-_END_条",
			"infoEmpty": "无数据",
			"infoFiltered": "_TOTAL_条",
			"loadingRecords": "加载中...",
			"paginate":
			{
				"first": "首页",
				"last": "尾页",
				"next": "下一页",
				"previous": "上一页"
			},
			select:
			{
				"rows": ""
			}
		}
	},
	options, null, function(options)
	{
		SPT.tableRenderProcessOptions(chart, options);
	});
	
	//填充options.columns的render函数
	for(var i=0; i<options.columns.length; i++)
	{
		var column = options.columns[i];
		
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
	
	SPT.tableThemeStyleSheet(chart, options);
	
	var carousel = SPT.carouselOption(options);
	
	if(carousel.enable)
		chartEle.addClass("dg-chart-table-carousel");
	
	if(!options.title.show)
		chartEle.addClass("dg-hide-title");
	
	if(options.enableWrapText)
		chartEle.addClass("dg-text-nowrap");
	
	var eleWrapper = (isV1 ? chartEle : $("<div class='dg-chart-ele-wrapper' />").appendTo(chartEle));
	
	var chartTitle = $("<div class='dg-chart-table-title' />").html(options.title.text).appendTo(eleWrapper);
	if(options.titleStyle)
		chart.elementStyle(chartTitle, options.titleStyle);
	
	var chartContent = $("<div class='dg-chart-table-content' />").appendTo(eleWrapper);
	
	if(isV1)
	{
		chartContent.css("top", (options.title.show ? chartTitle.outerHeight(true) : 0));
	}
	
	var table = $("<table width='100%' class='"+(options.disableStripe ? "" : " stripe ")+(options.disableHover ? "" : " hover ")+"'></table>")
					.appendTo(chartContent);
	var tableId = chart.id()+"-table";
	table.attr("id", tableId);
	
	table.dataTable(options);
	
	var dataTable = table.DataTable();
	
	if(options.scrollY == null && isV1)
	{
		SPT.tableEvalBodyHeightV1(chart, chartContent, dataTable);
	}
	
	if(carousel.enable && carousel.hideVerticalScrollbar != false)
	{
		var tableBody = SPT.tableGetScrollBody(chart, chartContent);
		tableBody.css("overflow-y", "hidden");
	}
	
	if(carousel.enable)
	{
		$(dataTable.table().body()).on("mouseenter", "tr", function()
		{
			if(carousel.pauseOnHover)
				SPT.tableStopCarousel(chart);
		})
		.on("mouseleave", "tr", function()
		{
			if(carousel.pauseOnHover)
				SPT.tableStartCarousel(chart);
		});
	}
	
	chart.internal(dataTable);
};

SPT.tableUpdate = function(chart, chartResult)
{
	var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
	
	var updateOptions = { data: [] };
	
	for(var i=0; i<dataSetBinds.length; i++)
	{
		var dataSetBind = dataSetBinds[i];
		
		var result = chart.resultOf(chartResult, dataSetBind);
		var resultDatas = chart.resultDatas(result);
		
		//复制，避免污染原始数据
		for(var j=0; j<resultDatas.length; j++)
		{
			var data = CF.extend({}, resultDatas[j]);
			chart.originalDataIndex(data, dataSetBind, j);
			updateOptions.data.push(data);
		}
	}
	
	SPT.tableStopCarousel(chart);
	
	updateOptions = chart.inflateUpdateOptions(chartResult, updateOptions);
	SPT.tableUpdateInternalData(chart, chartResult, updateOptions);
};

SPT.tableResize = function(chart)
{
	var renderOptions= chart.renderOptions();
	var chartContent = SPT.tableGetChartContent(chart);
	var dataTable = chart.internal();
	
	if(renderOptions.scrollY == null && SPT.tableIsV1())
	{
		SPT.tableEvalBodyHeightV1(chart, chartContent, dataTable);
	}
	
	SPT.tableAdjustColumn(dataTable);
};

SPT.tableDestroy = function(chart)
{
	var chartEle = chart.elementJquery();
	
	SPT.tableStopCarousel(chart);
	chartEle.removeClass("dg-chart-table dg-table-v1 dg-table-v2 dg-hide-title dg-text-nowrap dg-chart-table-carousel");
	chartEle.removeClass(chart.liveData(CF.builtinPropName("TableChartLocalStyleName")));
	$(".dg-chart-table-title", chartEle).remove();
	$(".dg-chart-table-content", chartEle).remove();
	$(".dg-chart-ele-wrapper", chartEle).remove();
};

SPT.tableOn = function(chart, eventType, handler)
{
	var handlerDelegation = function(htmlEvent)
	{
		var rowElement = this;
		var chartEvent = SPT.chartEventForHtml(chart, eventType, htmlEvent);
		SPT.tableSetChartEventData(chart, chartEvent, htmlEvent, rowElement);
		
		chart.callEventHandler(handler, chartEvent);
	};
	
	chart.registerEventHandlerDelegation(eventType, handler, handlerDelegation);
	$(chart.internal().table().body()).on(eventType, "tr", handlerDelegation);
};

SPT.tableOff = function(chart, eventType, handler)
{
	var $tableBody = $(chart.internal().table().body());
	
	chart.removeEventHandlerDelegation(eventType, handler, function(et, eh, ehd)
	{
		$tableBody.off(et, "tr", ehd);
	});
};

SPT.tableSetChartEventData = function(chart, chartEvent, htmlEvent, rowElement)
{
	var renderOptions= chart.renderOptions();
	var dg = renderOptions.dg;
	var dataSignNames = dg.dataSignNames;
	
	var dataTable = chart.internal();
	
	var chartData = dataTable.row(rowElement).data();
	
	var data = {};
	
	if(chartData)
	{
		var columnData = [];
		var columnDataSrc = dataTable.columns().dataSrc();
		
		for(var i=0; i<columnDataSrc.length; i++)
			columnData[i] = chartData[columnDataSrc[i]];
		
		data[dataSignNames.column] = columnData;
	}
	
	chart.eventData(chartEvent, data);
	chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(chartData));
};

SPT.tableRenderProcessOptions = function(chart, options)
{
	SPT.tableRenderProcessServerSidePaging(chart, options);
	SPT.tableRenderProcessCarousel(chart, options)
	
	//必须明确设置paging=false，因为底层表格组件的paging默认值为true
	options.paging = (options.paging != null ? options.paging : false);
	
	//开启分页后，默认开启info
	options.info = (options.info != null ? options.info : options.paging);
	
	if(options.paging)
	{
		options.lengthMenu = (options.lengthMenu == null || options.lengthMenu.length == 0 ? [ 10, 25, 50, 75, 100 ] : options.lengthMenu);
		//如果有50，则取50，以兼容旧版逻辑
		options.pageLength = (CF.indexInArray(options.lengthMenu, 50) >= 0 ? 50 : options.lengthMenu[0]);
	}
	
	if(SPT.tableIsV1())
	{
		if(options.dom == null)
		{
			options.dom = "t";
			options.dom = (options.paging ? (options.dom + "ilpr") : options.dom);
			options.dom = (options.searching ? ("f" + options.dom) : options.dom);
			options.dom = (options.buttons ? ("B" + options.dom) : options.dom);
		}
	}
	else
	{
		var dftLayout =
		{
			topStart: (options.buttons ? "buttons" : null),
			topEnd: (options.searching ? "search" : null),
			bottomStart: (options.info ? "info" : null),
			bottomEnd: (options.paging ? ["pageLength", "paging"] : null)
		};
		
		options.layout = (options.layout  == null ? dftLayout : options.layout);
	}
};

/**
 * 表格处理carousel选项，格式为：
 * {
 *   carousel: ...
 * }
 */
SPT.tableRenderProcessCarousel = function(chart, options)
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
	
	var carousel = SPT.carouselOption(options);
	
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
	
	SPT.carouselOption(options, carouselObj);
};

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
SPT.tableRenderProcessServerSidePaging = function(chart, options)
{
	var serverSidePaging = SPT.serverSidePagingOption(options);
	
	if(!serverSidePaging)
		return;
	
	options.serverSide = true;
	options.paging = true;
	
	//这里需禁用轮播，详细参考SPT.tableStartCarousel()函数
	SPT.carouselOption(options, false);
	
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
		
		//由图表API触发，此时已获取到数据，不应再执行chart.refreshData()函数
		if(refreshInfo != null)
		{
			chart.liveData("serverSidePagingRefreshInfo", null);
			
			if(chart.isActive())
				SPT.tableUpdateInternalData(chart, refreshInfo.chartResult, refreshInfo.updateOptions);
		}
		else
		{
			serverSidePaging.param(data, chart);
			
			if(chart.isActive())
				chart.refreshData();
		}
	};
	SPT.updateInternalOption(options, function(updateOptions, chart, chartResult)
	{
		var ajaxInfos = (chart.liveData("serverSidePagingAjaxInfos") || []);
		
		//由表格内部操作触发
		if(ajaxInfos.length > 0)
		{
			for(var i=0; i<ajaxInfos.length; i++)
			{
				var ajaxInfo = ajaxInfos[i];
				var recordsTotal = SPT.tableGetRecordsTotal(updateOptions, chart, chartResult, serverSidePaging);
				
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
		//由图表API触发，比如：参数表单提交、chart.refreshData()
		else
		{
			var pagingState = (serverSidePaging.state == null ? null : serverSidePaging.state(chart));
			if(pagingState != null)
				SPT.tableUpdatePagingState(chart, pagingState);
			
			var refreshInfo = { updateOptions: updateOptions, chartResult: chartResult };
			chart.liveData("serverSidePagingRefreshInfo", refreshInfo);
			
			var drawPagingArg = (serverSidePaging.drawPagingArg == null ? false : serverSidePaging.drawPagingArg);
			if(CF.isFunction(drawPagingArg))
				drawPagingArg = serverSidePaging.drawPagingArg(chart);
			
			chart.internal().draw(drawPagingArg);
		}
	});
};

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
SPT.tableUpdatePagingState = function(chart, state)
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
};

SPT.tableGetRecordsTotal = function(updateOptions, chart, chartResult, serverSidePaging)
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
};

SPT.tableGetChartContent = function(chart)
{
	//图表的数据透视表功能也采用的是DataTable组件，可能会与表格图表处在同一个图表div内，
	//因此，获取图表表格的DOM操作都应限定在".dg-chart-table-content"内
	
	return $(".dg-chart-table-content", chart.element());
};

SPT.tableGetFieldColumns = function(chart, columnDataSignName)
{
	var columns = [];
	
	var dataSetBinds = SPT.dataSetBindsMainFetched(chart);
	
	for(var i=0; i<dataSetBinds.length; i++)
	{
		var dataSetBind = dataSetBinds[i];
		
		var fields = chart.dataSetFieldsOfSign(dataSetBind, columnDataSignName);
		if(!fields || fields.length == 0)
			fields = chart.dataSetFields(dataSetBind);
		
		for(var j=0; j<fields.length; j++)
		{
			var field = fields[j];
			var colIdx = SPT.findInArray(columns, field.name, "name");
			
			if(colIdx < 0)
			{
				var column =
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
};

SPT.tableInternalVersion = function()
{
	if(typeof(DataTable) != "undefined")
		return (DataTable.version || "");
	else
		return "";
};

SPT.tableIsV1 = function()
{
	var v = SPT.tableInternalVersion();
	return (v != null && /^1\./.test(v));
};

SPT.tableGetScrollHead = function(chart, $chartContent)
{
	if(SPT.tableIsV1())
		return $(".dataTables_scrollHead", $chartContent);
	else
		return $(".dt-scroll-head", $chartContent);
};

SPT.tableGetScrollBody = function(chart, $chartContent)
{
	if(SPT.tableIsV1())
		return $(".dataTables_scrollBody", $chartContent);
	else
		return $(".dt-scroll-body", $chartContent);
};

SPT.tableThemeStyleSheet = function(chart, options)
{
	var isV1 = SPT.tableIsV1();
	var name = CF.builtinPropName("TableChart");
	var isLocalStyle = (options.tableStyle != null);
	var forceUpdate = false;
	
	if(isLocalStyle)
	{
		//这里不应使用随机数，因为在图表多次destroy再init后，会导致残留无法销毁的样式表DOM
		name = "tableStyle" + chart.id();
		//需强制为每次都更新样式表，因为绑定的图表主题可能是全局主题
		forceUpdate = true;
		
		chart.elementJquery().addClass(name);
		chart.liveData(CF.builtinPropName("TableChartLocalStyleName"), name);
	}
	
	chart.themeStyleSheet(name, function()
	{
		var theme = chart.theme();
		
		//使用实际背景色可以避免当backgroundColor是透明时，设置固定列后横向滚动时固定列无法遮挡其他滚动列的问题
		var rowBgColor = theme.actualBackgroundColor;
		var rowOddBgColor = chart.themeGradualColor(0);
		
		//V2版本通过透明度控制奇偶行颜色，这样可以适配任意背景，不过仍会出现固定列后横向滚动无法遮挡的问题（用户可以自定义颜色解决）
		if(!isV1)
		{
			rowBgColor ="rgba(0,0,0,0)";
			rowOddBgColor = chart.themeGradualColor(0.5) + "09";
		}
		
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
				},
				cell: {}
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
					"background-color": chart.themeGradualColor(0.2)
				},
				rowSelected:
				{
					"color": theme.highlightTheme.color,
					"background-color": theme.highlightTheme.backgroundColor
				},
				cell: {},
				cellOdd: {},
				cellEven: {},
				cellHover: {},
				cellSelected:
				{
					"color": theme.highlightTheme.color
				}
			}
		};
		
		if(isLocalStyle)
		{
			var optionTableStyle = options.tableStyle;
			tableStyle = CF.extend(true, tableStyle, optionTableStyle);
		}
		
		//DataTable-1.11.3内置表头背景CSS添加了"!important"，这里也必须添加才能起作用
		SPT.tableCopyStyleBackground(tableStyle.head.row, tableStyle.head.row, true, true);
		
		//DataTable-1.11.3的固定列采用的sticky特性，导致单元格必须设置背景不然会变透明
		SPT.tableCopyStyleBackground(tableStyle.head.row, tableStyle.head.cell, false, true);
		SPT.tableCopyStyleBackground(tableStyle.body.row, tableStyle.body.cell);
		SPT.tableCopyStyleBackground(tableStyle.body.rowOdd, tableStyle.body.cellOdd);
		SPT.tableCopyStyleBackground(tableStyle.body.rowEven, tableStyle.body.cellEven);
		SPT.tableCopyStyleBackground(tableStyle.body.rowHover, tableStyle.body.cellHover);
		SPT.tableCopyStyleBackground(tableStyle.body.rowSelected, tableStyle.body.cellSelected);
		
		var headColor = (tableStyle.head.cell.color ? tableStyle.head.cell.color : tableStyle.head.row.color);
		
		//样式要加".dg-chart-table-content"限定，因为图表的数据透视表功能也采用的是DataTable组件，可能会处在同一个表格图表div内
		var qualifier = (isLocalStyle ? "." + name : "") + " .dg-chart-table-content";
		var qualifierV1 = (isLocalStyle ? "." + name : "") + ".dg-table-v1 .dg-chart-table-content";
		var qualifierV2 = (isLocalStyle ? "." + name : "") + ".dg-table-v2 .dg-chart-table-content";
		
		var css=
		[
			{
				name: (isLocalStyle ? "." + name : "") + " .dg-chart-table-title",
				value:
				{
					"font-size": CF.toCssFontSize(theme.titleTheme.fontSize)
				}
			},
			{
				name: qualifier + " table.dataTable",
				value: chart.styleString(tableStyle.table)
			},
			{
				name: qualifier + " table.dataTable thead tr",
				value: chart.styleString(tableStyle.head.row)
			},
			{
				name:
				[
					qualifier + " table.dataTable thead tr th",
					qualifier + " table.dataTable thead tr td"
				],
				value: chart.styleString(tableStyle.head.cell)
			},
			{
				name: qualifier + " table.dataTable tbody tr",
				value: chart.styleString(tableStyle.body.row)
			},
			{
				name: qualifier + " table.dataTable tbody tr td",
				value: chart.styleString(tableStyle.body.cell)
			},
			{
				name:
				[
					qualifierV1 + " table.dataTable.stripe tbody tr.odd",
					qualifierV2 + " table.dataTable.stripe>tbody>tr:nth-child(odd)"
				],
				value: chart.styleString(tableStyle.body.rowOdd)
			},
			{
				name:
				[
					qualifierV1 + " table.dataTable.stripe tbody tr.odd td",
					qualifierV2 + " table.dataTable.stripe>tbody>tr:nth-child(odd)>*"
				],
				value: chart.styleString(tableStyle.body.cellOdd)
			},
			{
				name:
				[
					qualifierV1 + " table.dataTable.stripe tbody tr.even",
					qualifierV2 + " table.dataTable.stripe>tbody>tr:nth-child(even)"
				],
				value: chart.styleString(tableStyle.body.rowEven)
			},
			{
				name:
				[
					qualifierV1 + " table.dataTable.stripe tbody tr.even td",
					qualifierV2 + " table.dataTable.stripe>tbody>tr:nth-child(even)>*"
				],
				value: chart.styleString(tableStyle.body.cellEven)
			},
			{
				name:
				[
					qualifier + " table.dataTable.hover tbody tr:hover",
					qualifierV1 + " table.dataTable.hover.stripe tbody tr.odd:hover",
					qualifierV1 + " table.dataTable.hover.stripe tbody tr.even:hover",
					qualifierV2 + " table.dataTable.hover.stripe>tbody>tr:nth-child(odd):hover",
					qualifierV2 + " table.dataTable.hover.stripe>tbody>tr:nth-child(even):hover"
				],
				value: chart.styleString(tableStyle.body.rowHover)
			},
			{
				name:
				[
					qualifier + " table.dataTable.hover tbody tr:hover td",
					qualifierV1 + " table.dataTable.hover.stripe tbody tr.odd:hover td",
					qualifierV1 + " table.dataTable.hover.stripe tbody tr.even:hover td",
					qualifierV2 + " table.dataTable.hover.stripe>tbody>tr:nth-child(odd):hover>*",
					qualifierV2 + " table.dataTable.hover.stripe>tbody>tr:nth-child(even):hover>*",
				],
				value: chart.styleString(tableStyle.body.cellHover)
			},
			{
				name:
				[
					qualifier + " table.dataTable tbody tr.selected",
					qualifier + " table.dataTable.hover tbody tr:hover.selected",
					qualifierV1 + " table.dataTable.stripe tbody tr.odd.selected",
					qualifierV1 + " table.dataTable.stripe tbody tr.even.selected",
					qualifierV2 + " table.dataTable.stripe>tbody>tr:nth-child(odd).selected",
					qualifierV2 + " table.dataTable.display>tbody>tr:nth-child(odd).selected",
					qualifierV2 + " table.dataTable.stripe>tbody>tr:nth-child(even).selected",
					qualifierV2 + " table.dataTable.display>tbody>tr:nth-child(even).selected",
					
					qualifier + " table.dataTable.hover tbody tr:hover.selected",
					qualifierV1 + " table.dataTable.hover.stripe tbody tr.odd:hover.selected",
					qualifierV1 + " table.dataTable.hover.stripe tbody tr.even:hover.selected",
					qualifierV2 + " table.dataTable.hover.stripe>tbody>tr:nth-child(odd):hover.selected",
					qualifierV2 + " table.dataTable.hover.stripe>tbody>tr:nth-child(even):hover.selected"
				],
				value: chart.styleString(tableStyle.body.rowSelected)
			},
			{
				name:
				[
					qualifier + " table.dataTable tbody tr.selected td",
					qualifier + " table.dataTable.stripe tbody tr.odd.selected td",
					qualifier + " table.dataTable.stripe tbody tr.even.selected td",
					qualifier + " table.dataTable.hover tbody tr:hover.selected td",
					qualifierV2 + " table.dataTable>tbody>tr.selected>*",
					qualifierV2 + " table.dataTable.stripe>tbody>tr:nth-child(odd).selected>*",
					qualifierV2 + " table.dataTable.display>tbody>tr:nth-child(odd).selected>*",
					qualifierV2 + " table.dataTable.stripe>tbody>tr:nth-child(even).selected>*",
					qualifierV2 + " table.dataTable.display>tbody>tr:nth-child(even).selected>*",
					
					qualifier + " table.dataTable.hover tbody tr:hover.selected td",
					qualifierV1 + " table.dataTable.hover.stripe tbody tr.odd:hover.selected td",
					qualifierV1 + " table.dataTable.hover.stripe tbody tr.even:hover.selected td",
					qualifierV2 + " table.dataTable.hover.stripe>tbody>tr:nth-child(odd):hover.selected>*",
					qualifierV2 + " table.dataTable.hover.stripe>tbody>tr:nth-child(even):hover.selected>*",
				],
				value: chart.styleString(tableStyle.body.cellSelected)
			},
			{
				name: qualifierV1 + " table.dataTable thead th.sorting div.DataTables_sort_wrapper span",
				value:
				{
					"background": headColor
				}
			},
			{
				name: qualifierV1 + " table.dataTable thead th.sorting_asc div.DataTables_sort_wrapper span",
				value:
				{
					"border-bottom-color": headColor,
					"background": "none"
				}
			},
			{
				name: qualifierV1 + " table.dataTable thead th.sorting_desc div.DataTables_sort_wrapper span",
				value:
				{
					"border-top-color": headColor,
					"background": "none"
				}
			},
			{
				name:
				[
					qualifierV1 + " .dataTables_wrapper .dataTables_length select",
					qualifierV2 + " .dt-container .dt-length select"
				],
				value:
				{
					color: theme.color
				}
			},
			{
				name:
				[
					qualifierV1 + " .dataTables_wrapper .dataTables_length select option",
					qualifierV2 + " .dt-container .dt-length select option"
				],
				value:
				{
					color: theme.color,
					"background-color": chart.themeGradualColor(0)
				}
			},
			{
				name: qualifierV2 + " .dt-container .dt-scroll-body",
				value:
				{
					color: theme.color
				}
			},
			{
				name:
				[
					qualifierV2 + " table.dataTable>thead>tr>th",
					qualifierV2 + " table.dataTable>thead>tr>td"
				],
				value:
				{
					"border-bottom-color": chart.themeGradualColor(0)
				}
			},
			{
				name: qualifierV2 + " table.dataTable.dtfc-scrolling-left tr>.dtfc-fixed-left::after",
				value:
				{
					"box-shadow": "inset 10px 0 8px -8px " + chart.themeGradualColor(0.2)
				}
			},
			{
				name: qualifierV2 + " table.dataTable.dtfc-scrolling-right tr>.dtfc-fixed-right::after",
				value:
				{
					"box-shadow": "inset -10px 0 8px -8px " + chart.themeGradualColor(0.2)
				}
			},
			{
				name:
				[
					qualifierV1 + " .dataTables_wrapper .dataTables_filter input",
					qualifierV2 + " div.dt-container .dt-paging .dt-paging-button.current",
					qualifierV2 + " div.dt-container .dt-paging .dt-paging-button.current:hover",
					qualifierV2 + " div.dt-container .dt-input"
				],
				value:
				{
					"border-color": theme.borderColor
				}
			},
			{
				name:
				[
					qualifierV2 + " div.dt-container div.dt-buttons>.dt-button",
					qualifierV2 + " div.dt-container div.dt-buttons>div.dt-button-split .dt-button"
				],
				value:
				{
					"border-color": theme.borderColor
				}
			},
			{
				name:
				[
					qualifierV2 + " div.dt-container div.dt-buttons>.dt-button:focus:not(.disabled)",
					qualifierV2 + " div.dt-container div.dt-buttons>div.dt-button-split .dt-button:focus:not(.disabled)"
				],
				value:
				{
					"outline": "2px solid " + theme.borderColor
				}
			}
		];
		
		if(!isLocalStyle)
		{
			css.push(
			{
				name: " .dg-chart-table-title",
				value:
				{
					"color": theme.titleTheme.color,
					"background-color": theme.titleTheme.backgroundColor
				}
			});
		}
		
		return css;
	},
	forceUpdate);
};

SPT.tableCopyStyleBackground = function(from, to, force, important)
{
	force = (force == null ? false : force);
	important = (important == null ? false : important);
	
	if(from["background-color"] && (force || !to["background-color"]))
		to["background-color"] = (important ? SPT.cssValueImportant(from["background-color"]) : from["background-color"]);
	
	if(from["background"] && (force || !to["background"]))
		to["background"] = (important ? SPT.cssValueImportant(from["background"]) : from["background"]);
};

SPT.tableEvalBodyHeightV1 = function(chart, $chartContent, dataTable)
{
	var chartContentHeight = $chartContent.height();
	var container = $(dataTable.table().container());
	var containerHeight = container.outerHeight(true);
	var tableHeader = SPT.tableGetScrollHead(chart, $chartContent);
	var tableHeaderHeight = tableHeader.outerHeight(true);
	var tableBody = SPT.tableGetScrollBody(chart, $chartContent);
	var fixedColumnContainer = tableBody.closest(".DTFC_ScrollWrapper");
	var tableBodyHeight = chartContentHeight - tableHeaderHeight;
	tableBody.css("height", tableBodyHeight);
	tableBody.css("max-height", tableBodyHeight);
	fixedColumnContainer.css("height", tableBody.parent().height());
	
	containerHeight = container.outerHeight(true);
	
	//如果表格容器高度不等于图表内容限高，则重新设置
	if(containerHeight - chartContentHeight != 0)
	{
		tableBodyHeight = tableBodyHeight - (containerHeight - chartContentHeight);
		tableBody.css("height", tableBodyHeight);
		tableBody.css("max-height", tableBodyHeight);
		fixedColumnContainer.css("height", tableBody.parent().height());
	}
};

SPT.tableUpdateInternalData = function(chart, chartResult, updateOptions)
{
	var renderOptions = chart.renderOptions();
	
	//自定义更新底层组件数据，当启用serverSide后，需要自定义调用其ajax配置项的callback更新数据，而非这里
	//格式为：function(updateOptions, chart, chartResult){ ... }
	var updateInternal = SPT.updateInternalOption(renderOptions);
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
	SPT.tableAdjustColumn(dataTable);
	
	if(SPT.carouselOption(renderOptions).enable)
	{
		var chartEle = chart.elementJquery();
		chartEle.data("tableCarouselPrepared", false);
		SPT.tableStartCarousel(chart);
	}
};

/**
 * 调整图表表格。
 * 当表格隐藏显示、位置调整、数据变更后，可能会出现表头、固定列错位的情况，需要重新调整。
 */
SPT.tableAdjustColumn = function(dataTable)
{
	dataTable.columns.adjust();
	
	var initOptions = dataTable.init();
	
	if(initOptions.fixedHeader)
		dataTable.fixedHeader.adjust();
	
	/*
	if(initOptions.fixedColumns)
		dataTable.fixedColumns.relayout();
	*/
};

/**
 * 表格准备轮播。
 */
SPT.tablePrepareCarousel = function(chart)
{
	var renderOptions = chart.renderOptions();
	
	//此时需禁用轮播功能，不然dataTable.draw()会导致死循环
	if(renderOptions.serverSide == true || SPT.serverSidePagingOption(renderOptions) != null)
		return;
	
	var chartContent = SPT.tableGetChartContent(chart);
	var dataTable = chart.internal();
	var rowIndexes = dataTable.rows().indexes();
	var rowCount = rowIndexes.length;
	
	//空表格
	if(rowCount == 0)
		return;
	
	var scrollBody = SPT.tableGetScrollBody(chart, chartContent);
	var scrollTable = $(".dataTable", scrollBody);
	
	var scrollBodyHeight = scrollBody.height();
	
	while(true)
	{
		var scrollTableHeight = scrollTable.height();
		
		//表格高度至少为容器高度两倍，保证滚动平滑
		if(scrollTableHeight >= scrollBodyHeight*2)
			break;
		
		//必须成倍添加数据，避免出现轮播次序混乱
		for(var i=0; i<rowCount; i++)
		{
			var addData = dataTable.row(rowIndexes[i]).data();
			dataTable.row.add(addData);
		}
		
		dataTable.draw();
	}
};

/**
 * 表格开始轮播。
 */
SPT.tableStartCarousel = function(chart)
{
	var renderOptions = chart.renderOptions();
	
	//此时需禁用轮播功能，不然dataTable.draw()会导致死循环
	if(renderOptions.serverSide == true || SPT.serverSidePagingOption(renderOptions) != null)
		return;
	
	var carousel = SPT.carouselOption(renderOptions);
	var chartEle = chart.elementJquery();
	var chartContent = SPT.tableGetChartContent(chart);
	var dataTable = chart.internal();
	var rowCount = dataTable.rows().indexes().length;
	
	var scrollBody = SPT.tableGetScrollBody(chart, chartContent);
	var scrollTable = $(".dataTable", scrollBody);
	
	//空表格，或者，"auto"且行数未溢出时不轮播
	if(rowCount == 0
		|| (carousel.enable == "auto" && (scrollTable.height() <= scrollBody.height())))
	{
		scrollTable.css("margin-top", "0px");
		return;
	}
	
	SPT.tableStopCarousel(chart);
	chartEle.data("tableCarouselStatus", "start");
	
	SPT.tableHandleCarousel(chart, renderOptions, chartEle, dataTable, scrollBody, scrollTable);
};

/**
 * 表格停止轮播。
 */
SPT.tableStopCarousel = function(chart)
{
	var chartEle = chart.elementJquery();
	chartEle.data("tableCarouselStatus", "stop");
	SPT.tableCarouselIntervalId(chart, null);
};

SPT.tableHandleCarousel = function(chart, renderOptions, chartEle, dataTable, scrollBody, scrollTable)
{
	if(chartEle.data("tableCarouselStatus") == "stop")
		return;
	
	var carousel = SPT.carouselOption(renderOptions);
	var doCarousel = true;
	
	//元素隐藏时会因为高度计算有问题导致浏览器卡死，所以隐藏式不实际执行轮播
	if(scrollBody.is(":hidden"))
		doCarousel = false;
	
	if(doCarousel)
	{
		if(chartEle.data("tableCarouselPrepared") != true)
		{
			SPT.tablePrepareCarousel(chart);
			chartEle.data("tableCarouselPrepared", true)
		}
		
		//不采用设置滚动高度的方式（scrollBody.scrollTop()），因为会出现影响整个页面滚动高度的情况
		var scrollTop = parseInt(scrollTable.css("margin-top"));
		scrollTop = (Math.abs(scrollTop) || 0);
		
		var tableBody = dataTable.table().body();
		var currentRow = undefined;
		var currentRowHeight = undefined;
		var currentRowVisibleHeight = undefined;
		
		var offset = 0;
		var removeRowIndexes = [];
		var addRowDatas = [];
		var doRemove = false;
		var $checkRow = $("> tr:first", tableBody);
		var tmpOffset = 0;
		
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
			
			var dtRow = dataTable.row($checkRow);
			removeRowIndexes.push(dtRow.index());
			addRowDatas.push(dtRow.data());
			$checkRow = $checkRow.next();
		}
		
		var needDraw = false;
		
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
		
		var span = (CF.isFunction(carousel.span) ?
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
	
	var intervalId = setTimeout(function()
	{
		SPT.tableHandleCarousel(chart, renderOptions, chartEle, dataTable, scrollBody, scrollTable);
	},
	interval);
	
	SPT.tableCarouselIntervalId(chart, intervalId);
};

/**
 * 获取、设置表格轮播定时执行ID
 * 
 * @param chart
 * @param intervalId 要设置的定时执行ID，为null则清除
 */
SPT.tableCarouselIntervalId = function(chart, intervalId)
{
	var chartEle = chart.elementJquery();
	
	var curIntervalId = chartEle.data("tableCarouselIntervalId");
	
	if(intervalId === undefined)
		return curIntervalId;
	
	if(curIntervalId != null)
		clearInterval(curIntervalId);
	
	chartEle.data("tableCarouselIntervalId", intervalId);
};

//标签卡

SPT.labelRender = function(chart, options)
{
	options = CF.extend(true,
	{
		dg:
		{
			//name 可选，名称
			//value 数值
			dataSignNames: { name: "name", value: "value" }
		}
	},
	options);
	
	var chartEle = chart.elementJquery();
	chartEle.addClass("dg-chart-label");
	
	options = SPT.inflateRenderOptions(chart,
	{
		//将在update中设置：
		//标签卡数据：
		// data:
		// [
		//	  {
		//	    //可选，标签名，默认为选项值
		//	    name: "...",
		//	    //标签值
		//	    value: ...,
		//	    //可选，标签条目元素css样式
		//	    itemStyle: { ... },
		//	    //可选，标签名元素css样式
		//	    nameStyle: { ... },
		//	    //可选，标签值元素css样式
		//	    valueStyle: { ... }
		//	  },
		//	  ...
		// ]
		
		//标签条目、标签名、标签值是否都行内显示
		inline: false,
		//是否以flex布局展示标签
		//弹性布局：true 是、居中间隔；false 否；"around" 居中间隔；"start" 左对齐；"end" 右对齐；"center" 居中；"between" 贴边间隔； 
		flex: false,
		//是否标签值在标签名之前展示
		valueFirst: false,
		//是否隐藏标签名
		hideName: false,
		//标签条目元素公用css样式，格式为：{ ... }
		itemStyle: undefined,
		 //标签名元素公用css样式，格式为：{ ... }
		nameStyle: undefined,
		//标签值元素公用css样式，格式为：{ ... }
		valueStyle: undefined
	},
	options);
	
	if(options.inline == true)
		chartEle.addClass("dg-chart-label-inline");
	
	if(options.hideName == true)
		chartEle.addClass("dg-hide-name");
	
	if(options.flex != null && options.flex != false)
	{
		chartEle.addClass("dg-chart-label-flex");
		
		if(options.flex == "start")
			chartEle.addClass("dg-chart-label-flex-start");
		else if(options.flex == "end")
			chartEle.addClass("dg-chart-label-flex-end");
		else if(options.flex == "center")
			chartEle.addClass("dg-chart-label-flex-center");
		else if(options.flex == "between")
			chartEle.addClass("dg-chart-label-flex-between");
		else
			chartEle.addClass("dg-chart-label-flex-around");
	}
	
	chart.internal(chart.element());
};

SPT.labelUpdate = function(chart, chartResult)
{
	var renderOptions = chart.renderOptions();
	var dg = renderOptions.dg;
	var dataSignNames = dg.dataSignNames;
	var valueFirst = renderOptions.valueFirst;
	
	var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
	
	var $parent = $(chart.internal());
	
	$(".dg-chart-label-item", $parent).addClass("dg-chart-label-item-pending");
	
	var updateOptions = { data: [] };
	
	for(var i=0; i<dataSetBinds.length; i++)
	{
		var dataSetBind = dataSetBinds[i];
		
		var result = chart.resultOf(chartResult, dataSetBind);
		
		var nps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.name);
		var vps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.value);
		var hasNps = (nps && nps.length > 0);
		
		if(hasNps && nps.length != vps.length)
			throw new Error("The ["+dataSignNames.name+"] sign column must be "
					+"one-to-one with ["+dataSignNames.value+"] sign column");
		
		var namess = (hasNps ? chart.resultRowArrayDatas(result, nps) : []);
		var valuess = chart.resultRowArrayDatas(result, vps);
		
		var vpNames = [];
		if(!hasNps)
		{
			for(var j=0; j<vps.length; j++)
				vpNames[j] = chart.dataSetFieldAlias(dataSetBind, vps[j]);
		}
		
		for(var j=0; j<valuess.length; j++)
		{
			var values = valuess[j];
			var names = (hasNps ? namess[j] : vpNames);
			
			for(var k=0; k<names.length; k++)
			{
				var sv = { name: names[k], value: values[k] };
				chart.originalDataIndex(sv, dataSetBind, j);
				
				updateOptions.data.push(sv);
			}
		}
	}
	
	updateOptions = chart.inflateUpdateOptions(chartResult, updateOptions);
	
	for(var i=0; i<updateOptions.data.length; i++)
	{
		var labelData = updateOptions.data[i];
		
		var cssName = "dg-chart-label-item-"+i;
		
		var $label = $("."+ cssName, $parent);
		var $labelName = null;
		var $labelValue = null;
		
		if($label.length == 0)
		{
			$label = $("<div class='dg-chart-label-item "+cssName+"'></div>").appendTo($parent);
			
			if(valueFirst)
			{
				$labelValue = $("<div class='label-value'></div>").appendTo($label);
				$labelName = $("<div class='label-name'></div>").appendTo($label);
			}
			else
			{
				$labelName = $("<div class='label-name'></div>").appendTo($label);
				$labelValue = $("<div class='label-value'></div>").appendTo($label);
			}
		}
		else
		{
			$labelName = $(".label-name", $label);
			$labelValue = $(".label-value", $label);
			
			$label.removeClass("dg-chart-label-item-pending");
		}
		
		$labelName.html(labelData.name);
		$labelValue.html(labelData.value);
		$label.data("_dgChartLabelChartData", labelData);
		
		var itemStyle = SPT.evalLocalPlainObj(labelData.itemStyle, renderOptions.itemStyle);
		if(itemStyle)
			chart.elementStyle($label, itemStyle);
		
		var nameStyle = SPT.evalLocalPlainObj(labelData.nameStyle, renderOptions.nameStyle);
		if(nameStyle)
			chart.elementStyle($labelName, nameStyle);
		
		var valueStyle = SPT.evalLocalPlainObj(labelData.valueStyle, renderOptions.valueStyle);
		if(valueStyle)
			chart.elementStyle($labelValue, valueStyle);
	}
	
	$(".dg-chart-label-item-pending", $parent).remove();
};

SPT.labelResize = function(chart)
{
	
};

SPT.labelDestroy = function(chart)
{
	var chartEle = chart.elementJquery();
	chartEle.removeClass("dg-chart-label dg-chart-label-inline dg-hide-name dg-chart-label-flex "
							+"dg-chart-label-flex-around dg-chart-label-flex-start dg-chart-label-flex-end "
							+"dg-chart-label-flex-center dg-chart-label-flex-between");
	$(".dg-chart-label-item", chart.internal()).remove();
};

SPT.labelOn = function(chart, eventType, handler)
{
	var handlerDelegation = function(htmlEvent)
	{
		var $label = $(this);
		var chartEvent = SPT.chartEventForHtml(chart, eventType, htmlEvent);
		SPT.labelSetChartEventData(chart, chartEvent, htmlEvent, $label);
		
		chart.callEventHandler(handler, chartEvent);
	};
	
	chart.registerEventHandlerDelegation(eventType, handler, handlerDelegation);
	$(chart.internal()).on(eventType, ".dg-chart-label-item", handlerDelegation);
};

SPT.labelOff = function(chart, eventType, handler)
{
	var internal = $(chart.internal());
	
	chart.removeEventHandlerDelegation(eventType, handler, function(et, eh, ehd)
	{
		internal.off(et, ".dg-chart-label-item", ehd);
	});
};

SPT.labelSetChartEventData = function(chart, chartEvent, htmlEvent, $label)
{
	var renderOptions= chart.renderOptions();
	var dg = renderOptions.dg;
	var dataSignNames = dg.dataSignNames;
	
	var chartData = $label.data("_dgChartLabelChartData");
	
	var data = {};
	
	if(chartData)
	{
		data[dataSignNames.name] = chartData.name;
		data[dataSignNames.value] = chartData.value;
	}
	
	chart.eventData(chartEvent, data);
	chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(chartData));
};

//下拉框

SPT.selectRender = function(chart, options)
{
	options = CF.extend(true,
	{
		dg:
		{
			//name 可选，名称
			//value 数值
			dataSignNames: { name: "name", value: "value" }
		}
	},
	options);
	
	var chartEle = chart.elementJquery();
	chartEle.addClass("dg-chart-select");
	
	options = SPT.inflateRenderOptions(chart,
	{
		//将在update中设置：
		//下拉框数据：
		// data:
		// [
		//	  {
		//	    //选项名，可选，默认为选项值
		//	    name: "...",
		//	    //选项值
		//	    value: ...,
		//	    //是否选中，可选，默认为：false
		//	    selected: true 或 false,
		//	    //选项css样式，可选
		//	    itemStyle: { ... }
		//	  },
		//	  ...
		// ]
		
		//下拉框ID
		id: undefined,
		//下拉框名称
		name: undefined,
		//是否多选
		multiple: false,
		//可见选项数目
		size: undefined,
		//默认选中项：null：默认；数值或其数组：选中指定索引的选项；
		selected: undefined,
		//前置添加的条目项，格式同data元素，或者其数组，通常用于添加默认选中项
		prepend: undefined,
		//下拉框是否填满父元素，"auto" 当是内联框时填满；true 是；false 否
		fillParent: "auto",
		//select框css样式，格式为：{ ... }
		selectStyle: undefined,
		//option选项公用css样式，格式为：{ ... }
		itemStyle: undefined
	},
	options);
	
	SPT.selectThemeStyleSheet(chart);
	
	var isDropdown = (!options.multiple && (options.size == null || options.size <= 1));
	
	if(isDropdown)
		chartEle.addClass("dg-chart-select-dropdown");
	
	var $select = $("<select class='dg-chart-select-select' />").appendTo(chartEle);
	
	if(options.id)
		$select.attr("id", options.id);
	if(options.name)
		$select.attr("name", options.name);
	if(options.multiple)
		$select.attr("multiple", "multiple");
	if(options.size != null)
		$select.attr("size", options.size);
	if(options.fillParent === true || (options.fillParent == "auto" && !isDropdown))
		$select.addClass("dg-fill-parent");
	if(options.selectStyle)
		chart.elementStyle($select, options.selectStyle);
	
	chart.internal($select[0]);
};

SPT.selectUpdate = function(chart, chartResult)
{
	var renderOptions = chart.renderOptions();
	var dg = renderOptions.dg;
	var dataSignNames = dg.dataSignNames;
	
	var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
	
	var $select = $(chart.internal());
	
	$select.empty();
	
	var selected = renderOptions.selected;
	
	if(selected != null && typeof(selected) == "number")
		selected = [ selected ];
	
	var updateOptions = { data: [] };
	
	for(var i=0; i<dataSetBinds.length; i++)
	{
		var dataSetBind = dataSetBinds[i];
		
		var result = chart.resultOf(chartResult, dataSetBind);
		
		var nps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.name);
		var vps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.value);
		var hasNps = (nps && nps.length > 0);
		
		if(hasNps && nps.length != vps.length)
			throw new Error("The ["+dataSignNames.name+"] sign column must be "
					+"one-to-one with ["+dataSignNames.value+"] sign column");
		
		var namess = (hasNps ? chart.resultRowArrayDatas(result, nps) : []);
		var valuess = chart.resultRowArrayDatas(result, vps);
		
		for(var j=0; j<valuess.length; j++)
		{
			var values = valuess[j];
			var names = (hasNps ? namess[j] : values);
			
			for(var k=0; k<names.length; k++)
			{
				var sv = { name: names[k], value: values[k] };
				chart.originalDataIndex(sv, dataSetBind, j);
				
				updateOptions.data.push(sv);
			}
		}
	}
	
	updateOptions = chart.inflateUpdateOptions(chartResult, updateOptions);
	var data = updateOptions.data;
	
	if(renderOptions.prepend)
	{
		var newData = (CF.isArray(renderOptions.prepend) ? renderOptions.prepend : [ renderOptions.prepend ]);
		data = newData.concat(data);
	}
	
	for(var i=0; i<data.length; i++)
	{
		var optData = data[i];
		
		var $opt = $("<option />").attr("value", optData.value)
			.html(optData.name ? optData.name : optData.value).appendTo($select);
		
		if(optData.selected || (selected != null && CF.indexInArray(selected, i) > -1))
			$opt.attr("selected", "selected");
		
		$opt.data("_dgChartSelectOptionChartData", optData);
		
		var itemStyle = SPT.evalLocalPlainObj(optData.itemStyle, renderOptions.itemStyle);
		if(itemStyle)
			chart.elementStyle($opt, itemStyle);
	}
};

SPT.selectResize = function(chart)
{
	
};

SPT.selectDestroy = function(chart)
{
	var chartEle = chart.elementJquery();
	
	chartEle.removeClass("dg-chart-select dg-chart-select-dropdown dg-chart-beautify-scrollbar");
	
	$(chart.internal()).remove();
};

SPT.selectOn = function(chart, eventType, handler)
{
	var handlerDelegation = function(htmlEvent)
	{
		var $select = $(this);
		var chartEvent = SPT.chartEventForHtml(chart, eventType, htmlEvent);
		SPT.selectSetChartEventData(chart, chartEvent, htmlEvent, $select);
		
		chart.callEventHandler(handler, chartEvent);
	};
	
	chart.registerEventHandlerDelegation(eventType, handler, handlerDelegation);
	$(chart.internal()).on(eventType, handlerDelegation);
};

SPT.selectOff = function(chart, eventType, handler)
{
	var internal = $(chart.internal());
	
	chart.removeEventHandlerDelegation(eventType, handler, function(et, eh, ehd)
	{
		internal.off(et, ehd);
	});
};

SPT.selectSetChartEventData = function(chart, chartEvent, htmlEvent, $select)
{
	var renderOptions = chart.renderOptions();
	var dg = renderOptions.dg;
	var dataSignNames = dg.dataSignNames;
	
	var $selectedOptions = $("option:selected", $select);
	var chartData = [];
	var data = [];
	
	for(var i=0; i<$selectedOptions.length; i++)
	{
		chartData.push($($selectedOptions[i]).data("_dgChartSelectOptionChartData"));
		
		var datai = (data[i] = {});
		datai[dataSignNames.name] = chartData[i].name;
		datai[dataSignNames.value] = chartData[i].value;
	}
	
	//单选
	if(!renderOptions.multiple)
	{
		chartData = (chartData.length > 0 ? chartData[0] : null);
		data = (data.length > 0 ? data[0] : null);
	}
	
	chart.eventData(chartEvent, data);
	chart.eventOriginalDataIndex(chartEvent, (renderOptions.multiple ? chart.originalDataIndexes(chartData) : chart.originalDataIndex(chartData)));
};

SPT.selectThemeStyleSheet = function(chart)
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
					"border-color": theme.borderColor,
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
					"background-color": chart.themeGradualColor(0.1)
				}
			}
		];
		
		return css;
	});
};

//原始数据

SPT.rawDataRender = function(chart)
{
	var ele = chart.elementJquery();
	ele.addClass("dg-chart-rawdata");
	
	$("<div class='dg-chart-rawdata-title' />").text(chart.name()).appendTo(ele);
	$("<div class='dg-chart-rawdata-content' />").appendTo(ele);
};

SPT.rawDataUpdate = function(chart, chartResult)
{
	var ele = chart.elementJquery();
	var $content = $("> .dg-chart-rawdata-content", ele);
	$(".dg-chart-rawdata-ds", $content).remove();
	
	var dataSetBinds = SPT.dataSetBindsMainFetched(chart, chartResult);
	
	for(var i=0; i<dataSetBinds.length; i++)
	{
		var dataSetBind = dataSetBinds[i];
		var dataSetAlias = chart.dataSetAlias(dataSetBind);
		var result = chart.resultOf(chartResult, dataSetBind);
		var datas = chart.resultDatas(result);
		
		var $ds = $("<div class='dg-chart-rawdata-ds' />").appendTo($content);
		$("<div class='dg-chart-rawdata-ds-name' />").text(dataSetAlias).appendTo($ds);
		var $dsd = $("<div class='dg-chart-rawdata-ds-data' />").appendTo($ds);
		
		for(var j=0; j<datas.length; j++)
		{
			var di = CF.toJsonString(datas[j]);
			$("<div class='dg-chart-rawdata-ds-data-item' />").text(di).appendTo($dsd);
		}
	}
};

SPT.rawDataDestroy = function(chart)
{
	var ele = chart.elementJquery();
	
	ele.removeClass("dg-chart-rawdata");
	$("> .dg-chart-rawdata-title", ele).remove();
	$("> .dg-chart-rawdata-content", ele).remove();
};

SPT.rawDataAdditions = { supportIgnoreFetch: true };

SPT.rawDataResize = function(chart){};
SPT.rawDataOn = function(chart, eventType, handler){};
SPT.rawDataOff = function(chart, eventType, handler){};

//自定义

SPT.customAsyncRender = function(chart)
{
	var customRenderer = SPT.customGetCustomRenderer(chart, true);
	
	if(!customRenderer || customRenderer.asyncRender == null)
		return false;
	
	if(typeof(customRenderer.asyncRender) == "function")
		return customRenderer.asyncRender(chart);
	
	return (customRenderer.asyncRender == true);
};

SPT.customRender = function(chart)
{
	var customRenderer = SPT.customGetCustomRenderer(chart, true);
	
	//如果未定义，则采用默认方式，避免空白页，又可以让用户浏览和调试数据
	if(!customRenderer)
	{
		SPT.rawDataRender(chart);
	}
	else
	{
		customRenderer.render(chart);
	}
};

SPT.customAsyncUpdate = function(chart, chartResult)
{
	var customRenderer = SPT.customGetCustomRenderer(chart, true);
	
	if(!customRenderer || customRenderer.asyncUpdate == null)
		return false;
	
	if(typeof(customRenderer.asyncUpdate) == "function")
		return customRenderer.asyncUpdate(chart, chartResult);
	
	return (customRenderer.asyncUpdate == true);
};

SPT.customUpdate = function(chart, chartResult)
{
	var customRenderer = SPT.customGetCustomRenderer(chart, true);
	
	//如果未定义，则采用默认方式，避免空白页，又可以让用户浏览和调试数据
	if(!customRenderer)
	{
		SPT.rawDataUpdate(chart, chartResult);
	}
	else
	{
		customRenderer.update(chart, chartResult);
	}
};

SPT.customResize = function(chart)
{
	var customRenderer = SPT.customGetCustomRenderer(chart, true);
	
	//即使customRenderer未定义，resize操作也可以不抛出异常，因为不影响主体功能
	
	if(customRenderer && customRenderer.resize)
		customRenderer.resize(chart);
};

SPT.customDestroy = function(chart)
{
	var customRenderer = SPT.customGetCustomRenderer(chart, true);
	
	if(!customRenderer)
	{
		SPT.rawDataDestroy(chart);
	}
	else if(customRenderer.destroy)
	{
		customRenderer.destroy(chart);
	}
};

SPT.customOn = function(chart, eventType, handler)
{
	var customRenderer = SPT.customGetCustomRenderer(chart);
	
	if(customRenderer.on)
		customRenderer.on(chart, eventType, handler);
	else
		throw new Error("chart renderer 's [on] rqeuired");
};

SPT.customOff = function(chart, eventType, handler)
{
	var customRenderer = SPT.customGetCustomRenderer(chart);
	
	if(customRenderer.off)
		customRenderer.off(chart, eventType, handler);
	else
		throw new Error("chart renderer 's [off] rqeuired");
};

SPT.customAdditions = function(chart)
{
	var re = null;
	
	var customRenderer = SPT.customGetCustomRenderer(chart, true);
	
	if(customRenderer)
	{
		if(customRenderer.additions)
			re = (CF.isFunction(customRenderer.additions) ? customRenderer.additions(chart) : customRenderer.additions);
		else
			re = null;
	}
	else
	{
		re = SPT.rawDataAdditions;
	}
	
	return re;
};

SPT.customGetCustomRenderer = function(chart, nullable)
{
	nullable = (nullable == null ? false : nullable);
	
	var renderer = chart.renderer();
	
	if(renderer == null && !nullable)
		throw new Error("chart renderer required");
	
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
 * 准备ECharts图表渲染选项。
 * 
 * @param chart
 * @param renderOptions
 * @param beforeProcessHandler
 * @returns 一个新的图表渲染选项
 */
SPT.prepareEChartsRenderOptions = function(chart, renderOptions, beforeProcessHandler)
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
	
	SPT.setEChartsOptionsCmpId(renderOptions);
	chart.processRenderOptions(renderOptions);
	//应再次检查和设置组件ID，因为上述函数可能会增加新组件
	SPT.setEChartsOptionsCmpId(renderOptions);
	
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
SPT.prepareEChartsUpdateOptions = function(chart, updateOptions, beforeProcessHandler)
{
	var options = chart.options();
	updateOptions = SPT.trimArrayPropsForMerge(updateOptions, options);
	options = SPT.trimArrayPropsForMerge(options, updateOptions);
	updateOptions = chart.inflateOptions(updateOptions, options);
	
	if(beforeProcessHandler)
		beforeProcessHandler(updateOptions, chart);
	
	SPT.setEChartsOptionsCmpId(updateOptions);
	chart.processUpdateOptions(updateOptions);
	//应再次检查和设置组件ID，因为上述函数可能会增加新组件
	SPT.setEChartsOptionsCmpId(updateOptions);
	
	return updateOptions;
};

//设置ECharts选项中的组件ID，必须指定id且不能重复，因为更新操作采用的是replaceMerge模式，必须有对应id
SPT.setEChartsOptionsCmpId = function(options)
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

/**
 * 填充图表渲染options。
 * 注意： defaultOptions、builtinOptions，以及afterMergeHandlerFirst处理后的渲染options中，
 *		 不应设置会在update函数中有设置的项（对于基本类型，不应出现，也不要将值设置为undefined、null，可能会影响图表内部逻辑；对于数组类型，可以不出现，也可以设置为：[]），
 *		 因为update函数中调用的inflateUpdateOptions函数会把这里的设置高优先级深度合并。
 *
 * @param chart
 * @param defaultOptions 默认options，优先级最低
 * @param builtinOptions 内置options，优先级高于defaultOptions
 * @param afterMergeHandlerFirst 可选，由defaultOptions、builtinOptions合并后的新渲染options处理函数，格式为：function(renderOptions, chart){ ... }
 * @param beforeProcessHandler 可选
 * @param seriesFirstAsTemplate 可选，是否使用defaultOptions和builtinOptions合并后的series[0]作为series后续元素的模板，true 是；false 否。默认值为：true 
 * @returns 一个新的图表渲染options
 */
SPT.inflateRenderOptions = function(chart, defaultOptions, builtinOptions,
											afterMergeHandlerFirst, beforeProcessHandler, seriesFirstAsTemplate)
{
	if(arguments.length == 4)
	{
		// (chart, defaultOptions, builtinOptions, seriesFirstAsTemplate)
		if(afterMergeHandlerFirst === true || afterMergeHandlerFirst === false)
		{
			seriesFirstAsTemplate = afterMergeHandlerFirst;
			afterMergeHandlerFirst = null;
		}
	}
	else if(arguments.length == 5)
	{
		// (chart, defaultOptions, builtinOptions, afterMergeHandlerFirst, seriesFirstAsTemplate)
		if(beforeProcessHandler === true || beforeProcessHandler === false)
		{
			seriesFirstAsTemplate = beforeProcessHandler;
			beforeProcessHandler = null;
		}
	}
	
	seriesFirstAsTemplate = (seriesFirstAsTemplate == null ? true : seriesFirstAsTemplate);
	
	var renderOptions = CF.extend(true, {}, defaultOptions, builtinOptions);
	
	if(afterMergeHandlerFirst != null)
		afterMergeHandlerFirst(renderOptions, chart);
	
	var newBeforeProcessHandler = beforeProcessHandler;
	
	//使用series[0]作为series后续元素的模板，避免"dg-chart-options"中必须为series每个元素设置type等基础信息
	if(seriesFirstAsTemplate)
	{
		var series0 = (renderOptions.series && renderOptions.series[0] ?
						CF.extend(true, {}, renderOptions.series[0]) : null);
		
		if(series0)
		{
			newBeforeProcessHandler = function(renderOptions, chart)
			{
				var series = renderOptions.series;
				
				for(var i=1; i<series.length; i++)
					series[i] = CF.extend(true, {}, series0, series[i]);
				
				//必须指定id且不能重复，因为更新操作采用的是replaceMerge模式，必须有对应id
				for(var i=0; i<series.length; i++)
					series[i].id = i;
				
				if(beforeProcessHandler)
					beforeProcessHandler(renderOptions, chart);
			};
		}
	}
	
	return chart.inflateRenderOptions(renderOptions, newBeforeProcessHandler);
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
		for(var i=0; i<eles.length; i++)
			array.push(eles[i]);
	}
	else
		array.push(eles);
};

/**
 * 为源数组追加不重复的元素。
 * 
 * @param array 待追加数组
 * @param eles 追加元素、数组，可以是基本类型、对象类型
 * @param propertyName 可选，当是对象类型时，用于指定判断重复的属性名
 * @returns 追加的或重复元素的索引、或者索引数组
 */
SPT.appendDistinct = function(array, eles, propertyName)
{
	return SPT.appendDistinctQuick(array, eles, {}, propertyName);
};

/**
 * 为数组追加不重复的元素。
 * 
 * @param array 待追加数组
 * @param eles 追加元素、数组，可以是基本类型、对象类型
 * @param indexCache 索引缓存对象，用于存储arry中对应主键元素的索引，初始应为null、{}，格式为：{ 主键：索引数值 }
 * @param propertyName 可选，当是对象类型时，用于指定判断重复的属性名
 * @returns 追加的或重复元素的索引、或者索引数组
 */
SPT.appendDistinctQuick = function(array, eles, indexCache, propertyName)
{
	indexCache = (indexCache == null ? {} : indexCache);
	
	var isArray = CF.isArray(eles);
	
	if(!isArray)
		eles = [ eles ];
	
	var indexes = [];
	
	for(var i=0; i<eles.length; i++)
	{
		var key = (propertyName != null && eles[i] ? eles[i][propertyName] : eles[i]);
		var keyIdx = indexCache[key];
		
		if(keyIdx != null && keyIdx > -1)
		{
			indexes[i] = keyIdx;
		}
		else
		{
			keyIdx = SPT.findInArray(array, key, propertyName);
			
			if(keyIdx != null && keyIdx > -1)
			{
				indexes[i] = keyIdx;
				indexCache[key] = keyIdx;
			}
			else
			{
				array.push(eles[i]);
				indexCache[key] = array.length - 1;
				indexes[i] = array.length - 1;
			}
		}
	}
	
	return (isArray ? indexes : indexes[0]);
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
		if(array[i] != null && array[i] != "")
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

/**
 * 销毁图表的echarts对象。
 */
SPT.destroyChartEcharts = function(chart)
{
	var internal = chart.internal();
	if(internal && !internal.isDisposed())
		internal.dispose();
};

/**
 * 调整图表的echarts尺寸。
 */
SPT.resizeChartEcharts = function(chart)
{
	var internal = chart.internal();
	if(internal)
		internal.resize();
};

SPT.bindChartEventHandlerForEcharts = function(chart, eventType, eventHanlder, chartEventDataSetter)
{
	var hanlderDelegation = function(params)
	{
		var chartEvent = chart.eventNew(eventType, params);
		chartEventDataSetter(chart, chartEvent, params);
		chart.callEventHandler(eventHanlder, chartEvent);
	};
	
	chart.registerEventHandlerDelegation(eventType, eventHanlder, hanlderDelegation);
	chart.internal().on(eventType, "series", hanlderDelegation);
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
SPT.resultFirstNonEmptyValueOfSign = function(chart, dataSetBind, result, dataSign)
{
	var field = chart.dataSetFieldOfSign(dataSetBind, dataSign);
	
	if(field)
	{
		var values = chart.resultColumnArrayDatas(result, field);
		return SPT.findNonEmpty(values);
	}
	
	return null;
};

//初始化ECharts地图类图表的地图选项
SPT.echartsMapChartInitMap = function(chart, options)
{
	var map = CF.builtinOptionValue(options, builtinOptionNames.mapName);
	
	//必须设置初始map，不然渲染会报错
	if(CF.isEmpty(map))
		map = SPT.defaultMapName();
	
	//不应替换原始地图名
	var coverOriginalMap = false;
	SPT.echartsSetMapOption(options, map, coverOriginalMap);
};

//渲染ECharts地图类图表
SPT.echartsMapChartRender = function(chart, options)
{
	var maps = SPT.echartsGetMapsDistinct(options);
	chartUtil.echarts.registerMap(chart, maps, () =>
	{
		var instance = chartUtil.echarts.init(chart);
		instance.setOption(options);
		chart.statusRendered(true);
	});
};

//更新ECharts地图类图表
SPT.echartsMapChartUpdate = function(chart, updateOptions)
{
	var renderOptions = chart.renderOptions();
	var renderMaps = SPT.echartsGetMapsDistinct(renderOptions);
	var updateMaps = SPT.echartsGetMapsDistinct(updateOptions);
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
		SPT.echartsResetMapSettings(updateOptions);
	
	var maps = SPT.echartsGetMapsDistinct(updateOptions);
	chartUtil.echarts.registerMap(chart, maps, () =>
	{
		SPT.echartsOptionsReplaceMerge(chart, updateOptions);
		chart.statusUpdated(true);
	});
};

//仅提取ECharts地图类图表选项中的非空地图名信息，并且保持原结构
SPT.echartsGetMapOptions = function(echartsOptions)
{
	var re = {};
	
	var geo = echartsOptions.geo;
	var series = echartsOptions.series;
	
	if(geo)
	{
		if(CF.isArray(geo))
		{
			re.geo = [];
			
			for(var i=0; i<geo.length; i++)
			{
				re.geo[i] = (geo[i].map ? { map: geo[i].map } : {});
			}
		}
		else
		{
			re.geo = (geo.map ? { map: geo.map } : {});
		}
	}
	
	if(series)
	{
		if(CF.isArray(series))
		{
			re.series = [];
			
			for(var i=0; i<series.length; i++)
			{
				re.series[i] = (series[i].type == "map" && series[i].map ? { map: series[i].map } : {});
			}
		}
		else
		{
			re.series = (series.type == "map" && series.map ? { map: series.map } : {});
		}
	}
	
	return re;
};

//仅提取ECharts地图类图表选项中的不重复地图名信息
SPT.echartsGetMapsDistinct = function(echartsOptions)
{
	var re = [];
	
	var maps = [];
	var geo = echartsOptions.geo;
	var series = echartsOptions.series;
	
	if(geo)
	{
		if(CF.isArray(geo))
		{
			for(var i=0; i<geo.length; i++)
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
			for(var i=0; i<series.length; i++)
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
SPT.echartsSetMapOption = function(echartsOptions, map, force)
{
	var geo = echartsOptions.geo;
	var series = echartsOptions.series;
	
	if(geo)
	{
		if(CF.isArray(geo))
		{
			for(var i=0; i<geo.length; i++)
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
			for(var i=0; i<series.length; i++)
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
SPT.echartsResetMapSettings = function(echartsOptions)
{
	var geo = echartsOptions.geo;
	var series = echartsOptions.series;
	
	if(geo)
	{
		if(CF.isArray(geo))
		{
			for(var i=0; i<geo.length; i++)
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
			for(var i=0; i<series.length; i++)
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
SPT.adaptEChartsValueArrayData = function(chart, options, originalSeriesType, nameIndex, valueIndex)
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

/**
 * 从obj中提取名值对象。
 * 
 * @param obj 待提取的对象，格式为：{name: ..., value: ...}、{ value: [..., ...] }
 * @param nameProperty
 * @param valueProperty
 * @param nameIndex 可选，当obj.value是数组时，名在值数组对象的索引，默认为：0
 * @param valueIndex 可选，当obj.value是数组时，值在值数组对象的索引，默认为：1
 * @returns { nameProperty: ...,  valueProperty: ...}
 */
SPT.extractNameValueStyleObj = function(obj, nameProperty, valueProperty, nameIndex, valueIndex)
{
	nameIndex = (nameIndex == null ? 0 : nameIndex);
	valueIndex = (valueIndex == null ? 1 : valueIndex);
	
	var re = undefined;
	
	if(obj)
	{
		re = {};
		
		var name = obj.name;
		var value = obj.value;
		
		//{ value: [..., ...] }
		if(CF.isArray(value))
		{
			name = value[nameIndex];
			value = value[valueIndex];
		}
		
		re[nameProperty] = name;
		re[valueProperty] = value;
	}
	
	return re;
};

SPT.evalLocalPlainObj = function(localPlainObj, publicPlainObj)
{
	var re = null;
	
	if(localPlainObj && publicPlainObj)
		re = CF.extend({}, publicPlainObj, localPlainObj);
	else if(publicPlainObj)
		re = publicPlainObj;
	else if(localPlainObj)
		re = localPlainObj;
	
	return re;
};

SPT.cssValueImportant = function(cssValue)
{
	if(!cssValue)
		return cssValue;
	
	cssValue = (typeof(cssValue) == "string" ? cssValue : cssValue.toString());
	
	if(cssValue.indexOf("!important") < 0)
		cssValue += " !important";
	
	return cssValue;
};

SPT.toLegalStyleNameObj = function(obj)
{
	if(!obj)
		return obj;
	
	var re = {};
	
	for(var p in obj)
	{
		var name = CF.toLegalStyleName(p);
		var value = obj[p];
		
		re[name] = value;
	}
	
	return re;
};

SPT.echartsOptionsReplaceMerge = function(chart, options, replaceMerge)
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

SPT.chartEventForHtml = function(chart, type, htmlEvent)
{
	var event = chart.eventNew(type, htmlEvent);
	return event;
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

/**
 * 从updateOptions.series[i].data[i]提取轴数据，并设置为updateAxis.data轴数据。
 * 
 * @param chart
 * @param updateOptions 更新选项，格式应为：{ series: [ { data: [ ... ] } ] }
 * @param updateAxis 要填充轴数据的更新的轴对象，格式应为：{ data: [ { value: ... }、基本类型, ...], ... }
 * @param valueExtractor 轴数据值提取器，格式同SPT.sortEChartsUpdateAxisData的valueExtractor参数
 * @param sortSeriesData 可选，是否排序系列数据，默认值为：true。
 */
SPT.inflateEChartsUpdateAxisData = function(chart, updateOptions, updateAxis, valueExtractor, sortSeriesData)
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
	
	SPT.sortEChartsUpdateAxisData(chart.renderOptions(), updateOptions, updateAxis, true, sortSeriesData, valueExtractors);
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
SPT.sortEChartsUpdateAxisData = function(renderOptions, updateOptions, updateAxis,
				sortAxisData, sortSeriesData, valueExtractor)
{
	if(!sortAxisData && !sortSeriesData)
		return;
	
	var sortHandler = SPT.sortAxisDataOption(renderOptions);
	
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

SPT.sortAxisDataOption = function(options)
{
	var value = CF.builtinOptionValue(options, builtinOptionNames.sortAxisData);
	return value;
};

SPT.serverSidePagingOption = function(options, value)
{
	return CF.optionValue(options, "serverSidePaging", value);
};

SPT.updateInternalOption = function(options, value)
{
	return CF.optionValue(options, "updateInternal", value);
};

SPT.carouselOption = function(options, value)
{
	return CF.optionValue(options, "carousel", value);
};

SPT.inflateAxisDataExtractors =
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

/**
 * 获取默认地图名。
 * 地图类图表需要默认地图执行render初始渲染。
 * 注意：返回的默认地图名应是在dashboardFactory.js中dftBuiltinChartMaps的其中之一。
 */
SPT.defaultMapName = function()
{
	//默认中国地图，这里应使用"china"，因为echarts内部只对"china"地图名的地图才会自动绘制右下角的南海诸岛缩略图
	return "china";
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

SPT.adaptArrayPropsForUpdateOptions = function(updateOptions, renderOptions)
{
	for(var name in updateOptions)
	{
		var renderValue = renderOptions[name];
		var updateValue = updateOptions[name];
		var isRenderArray = CF.isArray(renderValue);
		var isUpdateArray = CF.isArray(updateValue);
		
		//如果渲染选项是数组，更新选项不是，应把更新选项包裹为数组
		if(isRenderArray && !isUpdateArray)
		{
			updateValue = [ updateValue ];
		}
		
		updateOptions[name] = updateValue;
	}
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

SPT.inflateEChartsRendererCommonFuncs = function(renderer)
{
	renderer.destroy = function(chart)
	{
		chartUtil.echarts.dispose(chart);
	};
	
	renderer.resize = function(chart)
	{
		chartUtil.echarts.resize(chart);
	},
	
	renderer.on = function(chart, eventType, handler)
	{
		chartUtil.echarts.on(chart, eventType, handler);
	},
	
	renderer.off = function(chart, eventType, handler)
	{
		chartUtil.echarts.off(chart, eventType, handler);
	};
	
	return renderer;		
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

SPT.customEChartsTooltip = function(params, extractor)
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

//---------------------------------------------------------
//    公用函数结束
//---------------------------------------------------------

})(this);