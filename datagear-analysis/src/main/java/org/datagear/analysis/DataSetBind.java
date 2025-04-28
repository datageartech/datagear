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
 * 数据集绑定。
 * <p>
 * 此类描述图表关联绑定的某个{@linkplain DataSet}、及相关设置信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DataSetBind implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 数据集 */
	private DataSet dataSet;

	/** 数据集标记集合 */
	private Set<String> dataSetSigns = Collections.emptySet();

	/** 数据集字段标记映射表 */
	private Map<String, Set<String>> fieldSigns = Collections.emptyMap();

	/** 数据集别名 */
	private String alias = "";

	/** 是否附件数据集，不用作渲染图表 */
	private boolean attachment = false;

	/** 数据集查询 */
	private DataSetQuery query = null;

	/** 数据集字段别名映射表 */
	private Map<String, String> fieldAliases = Collections.emptyMap();

	/** 数据集字段排序 */
	private Map<String, ? extends Number> fieldOrders = Collections.emptyMap();

	public DataSetBind()
	{
		super();
	}

	public DataSetBind(DataSet dataSet)
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

	public Set<String> getDataSetSigns()
	{
		return dataSetSigns;
	}

	/**
	 * 设置{@linkplain #getDataSet()}的标记，
	 * <p>
	 * 其元素应是{@linkplain Chart#getPlugin()}的{@linkplain ChartPlugin#getDataSigns()}中
	 * {@linkplain DataSign#getTarget()}为{@linkplain DataSign#TARGET_DATASET}的{@linkplain DataSign#getName()}集合。
	 * </p>
	 * 
	 * @param dataSetSigns
	 */
	public void setDataSetSigns(Set<String> dataSetSigns)
	{
		this.dataSetSigns = dataSetSigns;
	}

	public Map<String, Set<String>> getFieldSigns()
	{
		return fieldSigns;
	}

	/**
	 * 设置字段标记映射表。
	 * <p>
	 * 其关键字是{@linkplain #getDataSet()}的{@linkplain DataSetField#getName()}，
	 * 值集合元素则是{@linkplain Chart#getPlugin()}的{@linkplain ChartPlugin#getDataSigns()}中
	 * {@linkplain DataSign#getTarget()}为{@linkplain DataSign#TARGET_FIELD}的{@linkplain DataSign#getName()}，
	 * 或者，是{@linkplain DataSign#getTarget()}为{@linkplain DataSign#TARGET_DATASET}的{@linkplain DataSign#getChildren()}的{@linkplain DataSign#getName()}路径，
	 * 格式为：<code>【数据集标记名】.【字段标记名】</code>，
	 * 其中，<code>【数据集标记名】</code>是{@linkplain DataSign#getTarget()}为{@linkplain DataSign#TARGET_DATASET}的{@linkplain DataSign#getName()}，
	 * <code>【字段标记名】</code>则是其{@linkplain DataSign#getChildren()}的{@linkplain DataSign#getName()}。
	 * </p>
	 * 
	 * @param fieldSigns
	 */
	public void setFieldSigns(Map<String, Set<String>> fieldSigns)
	{
		this.fieldSigns = fieldSigns;
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

	public Map<String, String> getFieldAliases()
	{
		return fieldAliases;
	}

	public void setFieldAliases(Map<String, String> fieldAliases)
	{
		this.fieldAliases = fieldAliases;
	}

	public Map<String, ? extends Number> getFieldOrders()
	{
		return fieldOrders;
	}

	public void setFieldOrders(Map<String, ? extends Number> fieldOrders)
	{
		this.fieldOrders = fieldOrders;
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
