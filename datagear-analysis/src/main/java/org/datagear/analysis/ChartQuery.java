/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 图表查询。
 * 
 * @author datagear@163.com
 *
 */
public class ChartQuery implements ResultDataFormatAware, Serializable
{
	private static final long serialVersionUID = 1L;

	private List<DataSetQuery> dataSetQueries = Collections.emptyList();

	/** 图表结果数格式 */
	private ResultDataFormat resultDataFormat = null;

	public ChartQuery()
	{
		super();
	}

	public ChartQuery(List<DataSetQuery> dataSetQueries)
	{
		super();
		this.dataSetQueries = dataSetQueries;
	}

	public ChartQuery(ChartQuery chartQuery)
	{
		super();
		this.dataSetQueries = chartQuery.dataSetQueries;
		this.resultDataFormat = chartQuery.resultDataFormat;
	}

	public List<DataSetQuery> getDataSetQueries()
	{
		return dataSetQueries;
	}

	public void setDataSetQueries(List<DataSetQuery> dataSetQueries)
	{
		this.dataSetQueries = dataSetQueries;
	}

	@Override
	public ResultDataFormat getResultDataFormat()
	{
		return resultDataFormat;
	}

	@Override
	public void setResultDataFormat(ResultDataFormat resultDataFormat)
	{
		this.resultDataFormat = resultDataFormat;
	}

	/**
	 * 获取指定索引的{@linkplain DataSetQuery}。
	 * <p>
	 * 此方法不会抛出索引越界异常，如果索引越界，将直接返回{@code null}。
	 * </p>
	 * 
	 * @param index
	 * @return 返回{@code null}表示无对应的{@linkplain DataSetQuery}
	 */
	public DataSetQuery getDataSetQuery(int index)
	{
		int size = (this.dataSetQueries == null ? 0 : this.dataSetQueries.size());

		if (index >= size)
			return null;

		return this.dataSetQueries.get(index);
	}

	public ChartQuery copy()
	{
		return new ChartQuery(this);
	}
}
