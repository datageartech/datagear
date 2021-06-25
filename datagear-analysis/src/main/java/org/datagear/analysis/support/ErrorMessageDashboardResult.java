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
		setChartResultErrorMessages(toChartResultErrorMessages(result.getChartResultErrors()));
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
			Map<String, ChartResultError> chartResultErrors)
	{
		if (chartResultErrors == null)
			return Collections.emptyMap();

		Map<String, ChartResultErrorMessage> re = new HashMap<String, ErrorMessageDashboardResult.ChartResultErrorMessage>(
				chartResultErrors.size());

		for (Map.Entry<String, ChartResultError> entry : chartResultErrors.entrySet())
		{
			ChartResultErrorMessage em = new ChartResultErrorMessage(entry.getValue());
			re.put(entry.getKey(), em);
		}

		return re;
	}

	public static class ChartResultErrorMessage
	{
		/** 错误类型 */
		private String type = "";

		/** 错误消息 */
		private String message = "";

		public ChartResultErrorMessage()
		{
			super();
		}

		public ChartResultErrorMessage(String type, String message)
		{
			super();
			this.type = type;
			this.message = message;
		}

		public ChartResultErrorMessage(ChartResultError error)
		{
			super();
			Throwable throwable = error.getThrowable();

			if (throwable != null)
			{
				this.type = throwable.getClass().getSimpleName();
				this.message = throwable.getMessage();
			}
		}

		public String getType()
		{
			return type;
		}

		public void setType(String type)
		{
			this.type = type;
		}

		public String getMessage()
		{
			return message;
		}

		public void setMessage(String message)
		{
			this.message = message;
		}
	}
}
