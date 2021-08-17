/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 看板。
 * 
 * @author datagear@163.com
 *
 */
public class Dashboard extends AbstractIdentifiable implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_ID = "id";
	public static final String PROPERTY_RENDER_CONTEXT = "renderContext";
	public static final String PROPERTY_WIDGET = "widget";
	public static final String PROPERTY_CHARTS = "charts";

	private transient RenderContext renderContext;

	private DashboardWidget widget;

	private List<Chart> charts = null;

	public Dashboard()
	{
		super();
	}

	public Dashboard(String id, RenderContext renderContext, DashboardWidget widget)
	{
		super(id);
		this.renderContext = renderContext;
		this.widget = widget;
	}

	public RenderContext getRenderContext()
	{
		return renderContext;
	}

	public void setRenderContext(RenderContext renderContext)
	{
		this.renderContext = renderContext;
	}

	public DashboardWidget getWidget()
	{
		return widget;
	}

	public void setWidget(DashboardWidget widget)
	{
		this.widget = widget;
	}

	/**
	 * 是否包含图表。
	 * 
	 * @return
	 */
	public boolean hasChart()
	{
		return (this.charts != null && !this.charts.isEmpty());
	}

	public List<Chart> getCharts()
	{
		return charts;
	}

	public void setCharts(List<Chart> charts)
	{
		this.charts = charts;
	}

	/**
	 * 获取指定ID的{@linkplain Chart}。
	 * 
	 * @param id
	 * @return 返回{@code null}表示没有找到
	 */
	public Chart getChart(String id)
	{
		if (this.charts == null)
			return null;

		for (Chart chart : this.charts)
		{
			if (chart.getId().equals(id))
				return chart;
		}

		return null;
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
			Chart chart = getChart(chartId);

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
}
