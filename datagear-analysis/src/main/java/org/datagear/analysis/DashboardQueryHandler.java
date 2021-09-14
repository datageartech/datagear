/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

import java.util.HashMap;
import java.util.Map;

/**
 * {@linkplain DashboardQuery}处理器。
 * 
 * @author datagear@163.com
 *
 */
public abstract class DashboardQueryHandler
{
	public DashboardQueryHandler()
	{
		super();
	}

	/**
	 * 获取{@linkplain DashboardResult}。
	 * 
	 * @param query
	 * @return
	 * @throws DataSetException
	 */
	public DashboardResult getResult(DashboardQuery query) throws DataSetException
	{
		Map<String, ChartQuery> chartQueries = query.getChartQueries();
		boolean suppressChartError = query.isSuppressChartError();

		Map<String, ChartResult> chartResults = new HashMap<String, ChartResult>(chartQueries.size());
		Map<String, ChartResultError> chartResultErrors = new HashMap<String, ChartResultError>();

		for (Map.Entry<String, ChartQuery> entry : chartQueries.entrySet())
		{
			String chartId = entry.getKey();
			ChartQuery chartQuery = entry.getValue();
			ChartDefinition chart = getChartDefinition(chartId);

			if (chart == null)
				throw new IllegalArgumentException("Chart '" + chartId + "' not found");

			if (chartQuery.getResultDataFormat() == null && query.getResultDataFormat() != null)
			{
				chartQuery = chartQuery.copy();
				chartQuery.setResultDataFormat(query.getResultDataFormat());
			}

			ChartResult chartResult = null;

			if (suppressChartError)
			{
				try
				{
					chartResult = chart.getResult(chartQuery);
					chartResults.put(chartId, chartResult);
				}
				catch (Throwable t)
				{
					chartResultErrors.put(chartId, new ChartResultError(t));
				}
			}
			else
			{
				chartResult = chart.getResult(chartQuery);
				chartResults.put(chartId, chartResult);
			}
		}

		DashboardResult dashboardResult = new DashboardResult(chartResults);
		dashboardResult.setChartResultErrors(chartResultErrors);

		return dashboardResult;
	}
	
	/**
	 * 获取指定图表ID对应的{@linkplain ChartDefinition}。
	 * 
	 * @param chartId
	 *            {@linkplain Chart#getId()}
	 * @return 允许返回{@code null}
	 */
	protected abstract ChartDefinition getChartDefinition(String chartId);
}
