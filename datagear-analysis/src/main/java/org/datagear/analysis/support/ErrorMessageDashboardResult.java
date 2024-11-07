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

package org.datagear.analysis.support;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.datagear.analysis.ChartResult;
import org.datagear.analysis.ChartResultError;
import org.datagear.analysis.DashboardResult;

/**
 * 封装错误消息的看板结果。
 * 
 * @author datagear@163.com
 *
 */
public class ErrorMessageDashboardResult implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 图表ID-图表结果映射表 */
	private Map<String, ChartResult> chartResults = Collections.emptyMap();

	/** 图表ID-错误消息映射表 */
	private Map<String, ChartResultErrorMessage> chartErrors = Collections.emptyMap();

	public ErrorMessageDashboardResult(DashboardResult result)
	{
		this(result, false);
	}

	public ErrorMessageDashboardResult(DashboardResult result, boolean rootCauseMessage)
	{
		this.chartResults = result.getChartResults();
		this.chartErrors = toChartResultErrorMessages(result.getChartResultErrors(), rootCauseMessage);
	}

	public Map<String, ChartResult> getChartResults()
	{
		return chartResults;
	}

	public void setChartResults(Map<String, ChartResult> chartResults)
	{
		this.chartResults = chartResults;
	}

	public Map<String, ChartResultErrorMessage> getChartErrors()
	{
		return chartErrors;
	}

	public void setChartErrors(Map<String, ChartResultErrorMessage> chartErrors)
	{
		this.chartErrors = chartErrors;
	}

	protected Map<String, ChartResultErrorMessage> toChartResultErrorMessages(
			Map<String, ChartResultError> chartResultErrors, boolean rootCauseMessage)
	{
		if (chartResultErrors == null)
			return Collections.emptyMap();

		Map<String, ChartResultErrorMessage> re = new HashMap<String, ChartResultErrorMessage>(
				chartResultErrors.size());

		for (Map.Entry<String, ChartResultError> entry : chartResultErrors.entrySet())
		{
			ChartResultErrorMessage em = new ChartResultErrorMessage(entry.getValue(), rootCauseMessage);
			re.put(entry.getKey(), em);
		}

		return re;
	}
}
