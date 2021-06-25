/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

import java.util.Collections;
import java.util.List;

/**
 * 图表结果。
 * 
 * @author datagear@163.com
 *
 */
public class ChartResult
{
	private List<DataSetResult> dataSetResults = Collections.emptyList();

	public ChartResult()
	{
		super();
	}

	public ChartResult(List<DataSetResult> dataSetResults)
	{
		super();
		this.dataSetResults = dataSetResults;
	}

	/**
	 * 获取{@linkplain DataSetResult}列表。
	 * <p>
	 * 返回列表的元素与{@linkplain ChartDefinition#getChartDataSets()}元素一一对应。
	 * </p>
	 * 
	 * @return 返回{@code null}或空列表表示无结果
	 */
	public List<DataSetResult> getDataSetResults()
	{
		return dataSetResults;
	}

	public void setDataSetResults(List<DataSetResult> dataSetResults)
	{
		this.dataSetResults = dataSetResults;
	}
}
