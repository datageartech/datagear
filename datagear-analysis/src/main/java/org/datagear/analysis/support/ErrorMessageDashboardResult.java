/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.datagear.analysis.ChartResultError;
import org.datagear.analysis.DashboardResult;

/**
 * 错误消息的{@linkplain DashboardResult}。
 * <p>
 * 它的{@linkplain #getChartResultErrors()}始终返回空映射表，
 * {@linkplain #getChartResultErrorMessages()}则是由{@linkplain DashboardResult#getChartResultErrors()}转换而得。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ErrorMessageDashboardResult extends DashboardResult
{
	private Map<String, ChartResultErrorMessage> chartResultErrorMessages = Collections.emptyMap();

	public ErrorMessageDashboardResult(DashboardResult result)
	{
		super.setChartResults(result.getChartResults());
		setChartResultErrorMessages(toChartResultErrorMessages(result.getChartResultErrors(), false));
	}

	public ErrorMessageDashboardResult(DashboardResult result, boolean rootCauseMessage)
	{
		super.setChartResults(result.getChartResults());
		setChartResultErrorMessages(toChartResultErrorMessages(result.getChartResultErrors(), rootCauseMessage));
	}

	public Map<String, ChartResultErrorMessage> getChartResultErrorMessages()
	{
		return chartResultErrorMessages;
	}

	protected void setChartResultErrorMessages(Map<String, ChartResultErrorMessage> chartResultErrorMessages)
	{
		this.chartResultErrorMessages = chartResultErrorMessages;
	}

	@Override
	public Map<String, ChartResultError> getChartResultErrors()
	{
		return Collections.emptyMap();
	}

	@Override
	public void setChartResultErrors(Map<String, ChartResultError> chartResultErrors)
	{
		throw new UnsupportedOperationException();
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
