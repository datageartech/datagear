/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

import java.util.Collections;
import java.util.List;
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
	public static final String PROPERTY_CHART_DATASETS = "chartDataSets";
	public static final String PROPERTY_CHART_PARAM_VALUES = "paramValues";
	public static final String PROPERTY_UPDATE_INTERVAL = "updateInterval";

	public static final ChartDataSet[] EMPTY_CHART_DATA_SET = new ChartDataSet[0];

	/** 图表名称 */
	private String name;

	/** 图表数据集 */
	private ChartDataSet[] chartDataSets = EMPTY_CHART_DATA_SET;

	/** 图表参数值映射表 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> paramValues = Collections.EMPTY_MAP;

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

	public Map<String, Object> getParamValues()
	{
		return paramValues;
	}

	public void setParamValues(Map<String, Object> paramValues)
	{
		this.paramValues = paramValues;
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
	 * 获取指定索引的默认{@linkplain DataSetResult}。
	 * 
	 * @param index
	 * @return 如果{@linkplain ChartDataSet#isResultReady()}为{@code false}，将返回{@code null}。
	 * @throws DataSetException
	 */
	public DataSetResult getDataSetResult(int index) throws DataSetException
	{
		if (this.chartDataSets[index].isResultReady())
			return this.chartDataSets[index].getResult();
		else
			return null;
	}

	/**
	 * 获取默认{@linkplain DataSetResult}数组。
	 * 
	 * @return 如果{@linkplain #getChartDataSets()}指定索引的{@linkplain ChartDataSet#isResultReady()}为{@code false}，
	 *         返回数组对应元素将为{@code null}。
	 * @throws DataSetException
	 */
	public DataSetResult[] getDataSetResults() throws DataSetException
	{
		if (this.chartDataSets == null || this.chartDataSets.length == 0)
			return new DataSetResult[0];

		DataSetResult[] results = new DataSetResult[this.chartDataSets.length];

		for (int i = 0; i < this.chartDataSets.length; i++)
			results[i] = getDataSetResult(i);

		return results;
	}

	/**
	 * 获取指定索引和参数的{@linkplain DataSetResult}。
	 * 
	 * @param index
	 * @param dataSetParamValues
	 *            允许为{@code null}
	 * @return 如果{@code dataSetParamValues}为{@code null}，或者{@linkplain ChartDataSet#isResultReady(Map)}为{@code false}，将返回{@code null}。
	 * @throws DataSetException
	 */
	public DataSetResult getDataSetResult(int index, Map<String, ?> dataSetParamValues) throws DataSetException
	{
		if (dataSetParamValues == null)
			return null;
		else if (this.chartDataSets[index].isResultReady(dataSetParamValues))
			return this.chartDataSets[index].getResult(dataSetParamValues);
		else
			return null;
	}

	/**
	 * 获取指定参数的{@linkplain DataSetResult}数组。
	 * 
	 * @param dataSetParamValuess
	 *            允许为{@code null}
	 * @return 如果{@code dataSetParamValuess}指定元素为{@code null}，
	 *         或者{@linkplain #getChartDataSets()}指定索引的{@linkplain ChartDataSet#isResultReady(Map)}为{@code false}，返回数组对应元素将为{@code null}。
	 * @throws DataSetException
	 */
	public DataSetResult[] getDataSetResults(List<? extends Map<String, ?>> dataSetParamValuess) throws DataSetException
	{
		if (this.chartDataSets == null || this.chartDataSets.length == 0)
			return new DataSetResult[0];

		DataSetResult[] results = new DataSetResult[this.chartDataSets.length];

		int pvSize = (dataSetParamValuess == null ? 0 : dataSetParamValuess.size());

		for (int i = 0; i < this.chartDataSets.length; i++)
		{
			Map<String, ?> dataSetParamValues = (i >= pvSize ? null : dataSetParamValuess.get(i));
			results[i] = getDataSetResult(i, dataSetParamValues);
		}

		return results;
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
		to.setParamValues(from.paramValues);
		to.setUpdateInterval(from.updateInterval);
	}
}
