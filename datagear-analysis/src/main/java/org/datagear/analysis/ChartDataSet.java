/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

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
public class ChartDataSet
{
	/** 数据集 */
	private DataSet dataSet;

	/** 数据集属性标记映射表 */
	private Map<String, Set<String>> propertySigns = Collections.emptyMap();

	/** 数据集别名 */
	private String alias = "";

	/** 是否附件数据集，不用作渲染图表 */
	private boolean attachment = false;

	/**数据集查询*/
	private DataSetQuery query = null;

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
