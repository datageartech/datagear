/*
 * Copyright 2018-2023 datagear.tech
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

			// 如果没定义图表级结果数据格式，应使用看板级
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
