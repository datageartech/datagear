/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.AbstractIdentifiable;
import org.datagear.analysis.Chart;
import org.datagear.analysis.Dashboard;
import org.datagear.analysis.DashboardWidget;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetFactory;
import org.datagear.analysis.DataSetParamValues;
import org.datagear.analysis.RenderContext;

/**
 * 抽象{@linkplain Dashboard}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDashboard extends AbstractIdentifiable implements Dashboard
{
	private DashboardWidget<?> widget;

	private RenderContext renderContext;

	private List<Chart> charts;

	public AbstractDashboard()
	{
		super();
	}

	@SuppressWarnings("unchecked")
	public AbstractDashboard(String id, DashboardWidget<?> widget, RenderContext renderContext,
			List<? extends Chart> charts)
	{
		super(id);
		this.widget = widget;
		this.renderContext = renderContext;
		this.charts = (List<Chart>) charts;
	}

	@Override
	public DashboardWidget<?> getWidget()
	{
		return widget;
	}

	public void setWidget(DashboardWidget<?> widget)
	{
		this.widget = widget;
	}

	@Override
	public RenderContext getRenderContext()
	{
		return this.renderContext;
	}

	public void setRenderContext(RenderContext renderContext)
	{
		this.renderContext = renderContext;
	}

	@Override
	public List<? extends Chart> getCharts()
	{
		return charts;
	}

	@SuppressWarnings("unchecked")
	public void setCharts(List<? extends Chart> charts)
	{
		this.charts = (List<Chart>) charts;
	}

	@Override
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
	 * 获取此看板的所有数据集。
	 * 
	 * @param dataSetParamValues
	 * @return
	 * @throws DataSetException
	 */
	public Map<String, DataSet[]> getDataSets(DataSetParamValues dataSetParamValues) throws DataSetException
	{
		Map<String, DataSet[]> dataSetsMap = new HashMap<String, DataSet[]>();

		if (this.charts == null || this.charts.isEmpty())
			return dataSetsMap;

		for (Chart chart : this.charts)
		{
			DataSetFactory[] dataSetFactories = chart.getDataSetFactories();

			if (dataSetFactories == null || dataSetFactories.length == 0)
				continue;

			DataSet[] dataSets = new DataSet[dataSetFactories.length];

			for (int i = 0; i < dataSetFactories.length; i++)
			{
				DataSet dataSet = dataSetFactories[i].getDataSet(dataSetParamValues);
				dataSets[i] = dataSet;
			}

			dataSetsMap.put(chart.getId(), dataSets);
		}

		return dataSetsMap;
	}
}
