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
import java.util.List;

/**
 * 图表结果。
 * 
 * @author datagear@163.com
 *
 */
public class ChartResult implements Serializable
{
	private static final long serialVersionUID = 1L;

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
	 * 返回列表的元素与{@linkplain ChartDefinition#getDataSetBinds()}元素一一对应。
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
