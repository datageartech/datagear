/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 看板。
 * 
 * @author datagear@163.com
 *
 */
public class Dashboard extends AbstractIdentifiable
{
	public static final String PROPERTY_ID = "id";
	public static final String PROPERTY_RENDER_CONTEXT = "renderContext";
	public static final String PROPERTY_WIDGET = "widget";
	public static final String PROPERTY_CHARTS = "charts";

	private RenderContext renderContext;

	private DashboardWidget<?> widget;

	private List<Chart> charts;

	public Dashboard()
	{
		super();
	}

	public Dashboard(String id, RenderContext renderContext, DashboardWidget<?> widget)
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

	public DashboardWidget<?> getWidget()
	{
		return widget;
	}

	public void setWidget(DashboardWidget<?> widget)
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
	 * 获取此看板所有无需参数的图表的数据集结果。
	 * 
	 * @return
	 * @throws DataSetException
	 */
	public Map<String, DataSetResult[]> getDataSetResults() throws DataSetException
	{
		Map<String, DataSetResult[]> resultsMap = new HashMap<>();

		if (this.charts == null || this.charts.isEmpty())
			return resultsMap;

		for (Chart chart : this.charts)
		{
			if (!chart.isReadyForDataSetResults())
				continue;

			DataSetResult[] results = chart.getDataSetResults();

			if (results != null)
				resultsMap.put(chart.getId(), results);
		}

		return resultsMap;
	}

	/**
	 * 获取此看板指定图表ID集的数据集结果。
	 * 
	 * @param chartIds
	 * @param dataSetParamValuess
	 * @return
	 * @throws DataSetException
	 */
	@SuppressWarnings("unchecked")
	public Map<String, DataSetResult[]> getDataSetResults(Set<String> chartIds,
			Map<String, Map<String, ?>> dataSetParamValuess) throws DataSetException
	{
		Map<String, DataSetResult[]> resultsMap = new HashMap<>();

		if (this.charts == null || this.charts.isEmpty())
			return resultsMap;

		for (Chart chart : this.charts)
		{
			if (!chartIds.contains(chart.getId()))
				continue;

			Map<String, ?> myParamValues = dataSetParamValuess.get(chart.getId());
			if (myParamValues == null)
				myParamValues = Collections.EMPTY_MAP;

			DataSetResult[] results = chart.getDataSetResults(myParamValues);
			resultsMap.put(chart.getId(), results);
		}

		return resultsMap;
	}
}
