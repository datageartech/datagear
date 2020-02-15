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
	public static final String PROPERTY_PROPERTIES = "properties";
	public static final String PROPERTY_PARAMS = "params";
	public static final String PROPERTY_UPDATE_INTERVAL = "updateInterval";

	public static final ChartDataSet[] EMPTY_CHART_DATA_SET = new ChartDataSet[0];

	/** 图表名称 */
	private String name;

	/** 图表数据集 */
	private ChartDataSet[] chartDataSets = EMPTY_CHART_DATA_SET;

	/** 图表属性映射表 */
	private Map<String, Object> properties = new HashMap<String, Object>();

	/** 图表数据集参数映射表 */
	private Map<String, Object> params = new HashMap<String, Object>();

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

	public Map<String, Object> getProperties()
	{
		return properties;
	}

	public void setProperties(Map<String, Object> properties)
	{
		this.properties = properties;
	}

	public Map<String, Object> getParams()
	{
		return params;
	}

	public void setParams(Map<String, Object> params)
	{
		this.params = params;
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
	 * 获取图表属性。
	 * 
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getProperty(String name)
	{
		return (T) this.properties.get(name);
	}

	/**
	 * 设置图表属性。
	 * 
	 * @param name
	 * @param value
	 */
	public void setProperty(String name, Object value)
	{
		this.properties.put(name, value);
	}

	/**
	 * 获取图表数据集参数。
	 * 
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getParam(String name)
	{
		return (T) this.params.get(name);
	}

	/**
	 * 设置图表数据集参数。
	 * 
	 * @param name
	 * @param value
	 */
	public void setParam(String name, Object value)
	{
		this.params.put(name, value);
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
		to.setProperties(from.properties);
		to.setParams(from.params);
		to.setUpdateInterval(from.updateInterval);
	}
}
