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
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * 图表数据集。
 * <p>
 * 此类描述图表关联的某个{@linkplain DataSet}、及相关设置信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartDataSet implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 数据集 */
	private DataSet dataSet;

	/** 数据集属性标记映射表 */
	private Map<String, Set<String>> propertySigns = Collections.emptyMap();

	/** 数据集别名 */
	private String alias = "";

	/** 是否附件数据集，不用作渲染图表 */
	private boolean attachment = false;

	/** 数据集查询 */
	private DataSetQuery query = null;

	/** 数据集属性别名映射表 */
	private Map<String, String> propertyAliases = Collections.emptyMap();

	/** 数据集属性排序 */
	private Map<String, ? extends Number> propertyOrders = Collections.emptyMap();

	public ChartDataSet()
	{
		super();
	}

	public ChartDataSet(DataSet dataSet)
	{
		super();
		this.dataSet = dataSet;
	}

	public DataSet getDataSet()
	{
		return dataSet;
	}

	public void setDataSet(DataSet dataSet)
	{
		this.dataSet = dataSet;
	}

	/**
	 * 获取数据集属性标记映射表，其关键字是{@linkplain #getDataSet()}的{@linkplain DataSetProperty#getName()}、
	 * 值则{@linkplain Chart#getPlugin()}的{@linkplain ChartPlugin#getDataSigns()}的{@linkplain DataSign#getName()}集合。
	 * 
	 * @return
	 */
	public Map<String, Set<String>> getPropertySigns()
	{
		return propertySigns;
	}

	public void setPropertySigns(Map<String, Set<String>> propertySigns)
	{
		this.propertySigns = propertySigns;
	}

	/**
	 * 获取数据集别名。
	 * <p>
	 * 一个图表可能多次包含同一个数据集，此别名可在图表展示时用于区分显示。
	 * </p>
	 * 
	 * @return 返回{@code null}或空表示无别名
	 */
	public String getAlias()
	{
		return alias;
	}

	public void setAlias(String alias)
	{
		this.alias = alias;
	}

	/**
	 * 是否作为附件。
	 * <p>
	 * 附件数据集不用作渲染图表，不需设置数据标记，仅作为图表的附件，通常用于扩展图表功能。
	 * </p>
	 * 
	 * @return
	 */
	public boolean isAttachment()
	{
		return attachment;
	}

	public void setAttachment(boolean attachment)
	{
		this.attachment = attachment;
	}

	public DataSetQuery getQuery()
	{
		return query;
	}

	public void setQuery(DataSetQuery query)
	{
		this.query = query;
	}

	public Map<String, String> getPropertyAliases()
	{
		return propertyAliases;
	}

	public void setPropertyAliases(Map<String, String> propertyAliases)
	{
		this.propertyAliases = propertyAliases;
	}

	public Map<String, ? extends Number> getPropertyOrders()
	{
		return propertyOrders;
	}

	public void setPropertyOrders(Map<String, ? extends Number> propertyOrders)
	{
		this.propertyOrders = propertyOrders;
	}

	/**
	 * 使用{@linkplain #getQuery()}获取{@linkplain #getDataSet()}的{@linkplain DataSetResult}。
	 * 
	 * @return
	 * @throws DataSetException
	 */
	public DataSetResult getResult() throws DataSetException
	{
		return this.dataSet.getResult(this.query);
	}

	/**
	 * 获取{@linkplain #getDataSet()}的{@linkplain DataSetResult}。
	 * 
	 * @param query
	 * @return
	 * @throws DataSetException
	 */
	public DataSetResult getResult(DataSetQuery query) throws DataSetException
	{
		return this.dataSet.getResult(query);
	}
}
