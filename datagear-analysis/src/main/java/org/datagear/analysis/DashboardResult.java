/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

import java.util.Collections;
import java.util.Map;

/**
 * 看板结果。
 * 
 * @author datagear@163.com
 *
 */
public class DashboardResult
{
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
