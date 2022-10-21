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
 * 看板查询。
 * 
 * @author datagear@163.com
 *
 */
public class DashboardQuery implements ResultDataFormatAware
{
	/** 图表ID-查询映射表 */
	private Map<String, ChartQuery> chartQueries = Collections.emptyMap();

	/** 图表结果数格式 */
	private ResultDataFormat resultDataFormat = null;

	/** 单个图表查询出错时是否不抛出异常，而仅记录错误信息 */
	private boolean suppressChartError = false;

	public DashboardQuery()
	{
		super();
	}

	public DashboardQuery(Map<String, ChartQuery> chartQueries)
	{
		super();
		this.chartQueries = chartQueries;
	}

	public DashboardQuery(DashboardQuery query)
	{
		super();
		this.chartQueries = query.chartQueries;
		this.suppressChartError = query.suppressChartError;
	}

	public Map<String, ChartQuery> getChartQueries()
	{
		return chartQueries;
	}

	public void setChartQueries(Map<String, ChartQuery> chartQueries)
	{
		this.chartQueries = chartQueries;
	}

	@Override
	public ResultDataFormat getResultDataFormat()
	{
		return resultDataFormat;
	}

	@Override
	public void setResultDataFormat(ResultDataFormat resultDataFormat)
	{
		this.resultDataFormat = resultDataFormat;
	}

	public boolean isSuppressChartError()
	{
		return suppressChartError;
	}

	public void setSuppressChartError(boolean suppressChartError)
	{
		this.suppressChartError = suppressChartError;
	}

	public DashboardQuery copy()
	{
		return new DashboardQuery(this);
	}
}
