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

package org.datagear.analysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.util.Global;

/**
 * 图表定义。
 * 
 * @author datagear@163.com
 *
 */
public class ChartDefinition extends AbstractIdentifiable implements ResultDataFormatAware, Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * 内置图表属性名前缀。
	 * <p>
	 * 所有内置属性名都应以此作为前缀，避免名称冲突。
	 * </p>
	 * <p>
	 * 注意：谨慎重构此常量值，因为它可能已被用于系统已创建的图表中，重构它将导致这些图表逻辑出错。
	 * </p>
	 */
	public static final String BUILTIN_ATTR_PREFIX = Global.NAME_SHORT_UCUS;

	public static final String PROPERTY_ID = "id";
	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_DATA_SET_BINDS = "dataSetBinds";
	public static final String PROPERTY_ATTR_VALUES = "attrValues";
	public static final String PROPERTY_UPDATE_INTERVAL = "updateInterval";

	public static final DataSetBind[] EMPTY_DATA_SET_BINDS = new DataSetBind[0];

	/** 图表名称 */
	private String name;

	/** 图表数据集 */
	private DataSetBind[] dataSetBinds = EMPTY_DATA_SET_BINDS;

	/** 图表属性值映射表 */
	private Map<String, Object> attrValues = null;

	/** 图表更新间隔毫秒数 */
	private int updateInterval = -1;

	/**结果数据格式*/
	private ResultDataFormat resultDataFormat = null;
	
	public ChartDefinition()
	{
		super();
	}

	public ChartDefinition(String id, String name, DataSetBind[] dataSetBinds)
	{
		super(id);
		this.name = name;
		this.dataSetBinds = dataSetBinds;
	}

	public ChartDefinition(ChartDefinition chartDefinition)
	{
		this(chartDefinition.getId(), chartDefinition.name, chartDefinition.dataSetBinds);
		this.attrValues = chartDefinition.attrValues;
		this.updateInterval = chartDefinition.updateInterval;
		this.resultDataFormat = chartDefinition.resultDataFormat;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public DataSetBind[] getDataSetBinds()
	{
		return dataSetBinds;
	}

	public void setDataSetBinds(DataSetBind[] dataSetBinds)
	{
		this.dataSetBinds = dataSetBinds;
	}

	/**
	 * 获取图表属性值映射表。
	 * 
	 * @return 可能返回{@code null}
	 */
	public Map<String, Object> getAttrValues()
	{
		return attrValues;
	}

	/**
	 * 设置图表属性值映射表。
	 * 
	 * @param attrValues
	 */
	public void setAttrValues(Map<String, Object> attrValues)
	{
		this.attrValues = attrValues;
	}

	/**
	 * 设置属性值。
	 * 
	 * @param name
	 * @param value
	 */
	public void setAttrValue(String name, Object value)
	{
		if (this.attrValues == null || this.attrValues == Collections.EMPTY_MAP)
			this.attrValues = new HashMap<>();

		this.attrValues.put(name, value);
	}

	/**
	 * 获取属性值。
	 * 
	 * @param name
	 * @return 返回{@code null}表示没有。
	 */
	public Object getAttrValue(String name)
	{
		if (this.attrValues == null)
			return null;

		return this.attrValues.get(name);
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
	 * 获取图表数据集结果数据格式。
	 * 
	 * @return
	 */
	@Override
	public ResultDataFormat getResultDataFormat()
	{
		return resultDataFormat;
	}

	/**
	 * 设置图表数据集结果数据格式。
	 * 
	 * @param dataFormat
	 */
	@Override
	public void setResultDataFormat(ResultDataFormat resultDataFormat)
	{
		this.resultDataFormat = resultDataFormat;
	}
	
	/**
	 * 获取{@linkplain ChartResult}。
	 * <p>
	 * 如果{@linkplain ChartQuery#getDataSetQuery(int)}为{@code null}，此方法将使用{@linkplain DataSetBind#getQuery()}。
	 * </p>
	 * 
	 * @param query
	 * @return
	 * @throws DataSetException
	 */
	public ChartResult getResult(ChartQuery query) throws DataSetException
	{
		if (this.dataSetBinds == null || this.dataSetBinds.length == 0)
			return new ChartResult(Collections.emptyList());

		ChartResult chartResult = new ChartResult();

		List<DataSetResult> dataSetResults = new ArrayList<DataSetResult>(this.dataSetBinds.length);

		for (int i = 0; i < this.dataSetBinds.length; i++)
		{
			DataSetBind dataSetBind = this.dataSetBinds[i];
			DataSetQuery dataSetQuery = getDataSetQuery(query, dataSetBind, i);
			DataSetResult dataSetResult = dataSetBind.getResult(dataSetQuery);

			dataSetResults.add(dataSetResult);
		}

		chartResult.setDataSetResults(dataSetResults);

		return chartResult;
	}

	/**
	 * 获取指定{@linkplain DataSetQuery}。
	 * 
	 * @param chartQuery
	 * @param dataSetBind
	 * @param dataSetBindIdx
	 * @return
	 */
	protected DataSetQuery getDataSetQuery(ChartQuery chartQuery, DataSetBind dataSetBind, int dataSetBindIdx)
	{
		DataSetQuery dataSetQuery = chartQuery.getDataSetQuery(dataSetBindIdx);

		if (dataSetQuery == null)
			dataSetQuery = dataSetBind.getQuery();

		if (dataSetQuery == null)
			dataSetQuery = DataSetQuery.valueOf();

		if (dataSetQuery.getResultDataFormat() == null)
		{
			ResultDataFormat cqrds = chartQuery.getResultDataFormat();
			ResultDataFormat crds = getResultDataFormat();

			if (cqrds != null || crds != null)
			{
				dataSetQuery = dataSetQuery.copy();
				dataSetQuery.setResultDataFormat(cqrds);

				if (dataSetQuery.getResultDataFormat() == null)
					dataSetQuery.setResultDataFormat(crds);
			}
		}

		return dataSetQuery;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [id=" + getId() + ", name=" + name + "]";
	}
}
