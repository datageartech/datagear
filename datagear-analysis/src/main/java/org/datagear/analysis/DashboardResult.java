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

/**
 * 看板结果。
 * 
 * @author datagear@163.com
 *
 */
public class DashboardResult implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 图表ID-图表结果映射表 */
	private Map<String, ChartResult> chartResults = Collections.emptyMap();

	/** 图表ID-图表结果异常映射表 */
	private Map<String, ChartResultError> chartResultErrors = Collections.emptyMap();

	public DashboardResult()
	{
		super();
	}

	public DashboardResult(Map<String, ChartResult> chartResults)
	{
		super();
		this.chartResults = chartResults;
	}

	/**
	 * 获取[图表ID-图表结果]映射表。
	 * 
	 * @return
	 */
	public Map<String, ChartResult> getChartResults()
	{
		return chartResults;
	}

	public void setChartResults(Map<String, ChartResult> chartResults)
	{
		this.chartResults = chartResults;
	}

	/**
	 * 获取[图表ID-{@linkplain ChartResultError}]映射表。
	 * 
	 * @return
	 */
	public Map<String, ChartResultError> getChartResultErrors()
	{
		return chartResultErrors;
	}

	public void setChartResultErrors(Map<String, ChartResultError> chartResultErrors)
	{
		this.chartResultErrors = chartResultErrors;
	}
}
