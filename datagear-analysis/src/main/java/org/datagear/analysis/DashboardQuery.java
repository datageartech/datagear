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

package org.datagear.analysis;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * 看板查询。
 * 
 * @author datagear@163.com
 *
 */
public class DashboardQuery implements ResultDataFormatAware, Serializable
{
	private static final long serialVersionUID = 1L;

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
		this.resultDataFormat = query.resultDataFormat;
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
