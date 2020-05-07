/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */

package org.datagear.analysis;

import java.util.HashMap;
import java.util.Map;

/**
 * 图表定义。
 * 
 * @author datagear@163.com
 *
 */
public class ChartDefinition extends AbstractIdentifiable
{
	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_CHARTDATASETS = "chartDataSets";
	public static final String PROPERTY_CHART_PARAM_VALUES = "chartParamValues";
	public static final String PROPERTY_DATA_SET_PARAM_VALUES = "dataSetParamValues";
	public static final String PROPERTY_UPDATE_INTERVAL = "updateInterval";

	public static final ChartDataSet[] EMPTY_CHART_DATA_SET = new ChartDataSet[0];

	/** 图表名称 */
	private String name;

	/** 图表数据集 */
	private ChartDataSet[] chartDataSets = EMPTY_CHART_DATA_SET;

	/** 图表参数值映射表 */
	private Map<String, Object> chartParamValues = new HashMap<>();

	/** 数据集参数值映射表 */
	private Map<String, Object> dataSetParamValues = new HashMap<>();

	/** 图表更新间隔毫秒数 */
	private int updateInterval = -1;

	public ChartDefinition()
	{
		super();
	}

	public ChartDefinition(String id, String name, ChartDataSet[] chartDataSets)
	{
		super(id);
		this.name = name;
		this.chartDataSets = chartDataSets;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public ChartDataSet[] getChartDataSets()
	{
		return chartDataSets;
	}

	public void setChartDataSets(ChartDataSet[] chartDataSets)
	{
		this.chartDataSets = chartDataSets;
	}

	public Map<String, Object> getChartParamValues()
	{
		return chartParamValues;
	}

	public void setChartParamValues(Map<String, Object> chartParamValues)
	{
		this.chartParamValues = chartParamValues;
	}

	public Map<String, Object> getDataSetParamValues()
	{
		return dataSetParamValues;
	}

	public void setDataSetParamValues(Map<String, Object> dataSetParamValues)
	{
		this.dataSetParamValues = dataSetParamValues;
	}

	/**
	 * 获取图表更新间隔毫秒数。
	 * 
	 * @return {@code <0}：不更新；0 ：实时更新；{@code >0}：间隔更新毫秒数
	 */
	public int getUpdateInterval()
	{
		return updateInterval;
	}

	public void setUpdateInterval(int updateInterval)
	{
		this.updateInterval = updateInterval;
	}

	/**
	 * 获取图表参数值。
	 * 
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getChartParamValue(String name)
	{
		return (T) this.chartParamValues.get(name);
	}

	/**
	 * 设置图表参数值。
	 * 
	 * @param name
	 * @param value
	 */
	public void setChartParamValue(String name, Object value)
	{
		this.chartParamValues.put(name, value);
	}

	/**
	 * 获取图表数据集参数值。
	 * 
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getDataSetParamValue(String name)
	{
		return (T) this.dataSetParamValues.get(name);
	}

	/**
	 * 设置图表数据集参数值。
	 * 
	 * @param name
	 * @param value
	 */
	public void setDataSetParamValue(String name, Object value)
	{
		this.dataSetParamValues.put(name, value);
	}

	@Override
	public String toString()
	{
		return "ChartDefinition [id=" + getId() + ", name=" + name + "]";
	}

	/**
	 * 拷贝属性。
	 * 
	 * @param from
	 * @param to
	 */
	public static void copy(ChartDefinition from, ChartDefinition to)
	{
		to.setId(from.getId());
		to.setName(from.name);
		to.setChartDataSets(from.chartDataSets);
		to.setChartParamValues(from.chartParamValues);
		to.setDataSetParamValues(from.dataSetParamValues);
		to.setUpdateInterval(from.updateInterval);
	}
}
