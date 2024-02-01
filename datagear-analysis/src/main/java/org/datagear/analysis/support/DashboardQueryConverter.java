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

package org.datagear.analysis.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartDataSet;
import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.ChartQuery;
import org.datagear.analysis.DashboardQuery;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetQuery;

/**
 * 抽象{@linkplain DashboardQuery}转换器。
 * 
 * @author datagear@163.com
 *
 */
public abstract class DashboardQueryConverter
{
	private DataSetParamValueConverter dataSetParamValueConverter;

	public DashboardQueryConverter()
	{
		super();
	}

	public DashboardQueryConverter(DataSetParamValueConverter dataSetParamValueConverter)
	{
		super();
		this.dataSetParamValueConverter = dataSetParamValueConverter;
	}

	public DataSetParamValueConverter getDataSetParamValueConverter()
	{
		return dataSetParamValueConverter;
	}

	public void setDataSetParamValueConverter(DataSetParamValueConverter dataSetParamValueConverter)
	{
		this.dataSetParamValueConverter = dataSetParamValueConverter;
	}

	/**
	 * 转换{@linkplain DashboardQuery}。
	 * <p>
	 * 将{@linkplain DashboardQuery}包含的{@linkplain DataSetQuery}转换为符合{@linkplain DataSet#getParams()}类型。
	 * </p>
	 * 
	 * @param query
	 *            允许{@code null}
	 * @return
	 */
	public DashboardQuery convert(DashboardQuery query)
	{
		if (query == null)
			return new DashboardQuery();

		Map<String, ChartQuery> chartQueries = query.getChartQueries();
		Map<String, ChartQuery> chartQueriesRe = new HashMap<>(chartQueries.size());

		for (Map.Entry<String, ChartQuery> entry : chartQueries.entrySet())
		{
			String chartId = entry.getKey();
			ChartQuery chartQuery = entry.getValue();
			ChartDefinition chartDef = getChartDefinition(chartId);

			// 忽略未找到的
			if (chartDef == null)
				continue;

			ChartDataSet[] chartDataSets = chartDef.getChartDataSets();
			List<DataSetQuery> dataSetQueries = chartQuery.getDataSetQueries();

			int chartDataSetsLen = (chartDataSets == null ? 0 : chartDataSets.length);
			int dataSetQueriesLen = (dataSetQueries == null ? 0 : dataSetQueries.size());

			ChartQuery chartQueryRe = chartQuery.copy();

			// 如果没定义图表级结果数据格式，应使用看板级
			if (chartQueryRe.getResultDataFormat() == null && query.getResultDataFormat() != null)
				chartQueryRe.setResultDataFormat(query.getResultDataFormat());

			List<DataSetQuery> dataSetQueriesRe = new ArrayList<DataSetQuery>(dataSetQueriesLen);

			for (int i = 0; i < chartDataSetsLen; i++)
			{
				DataSetQuery dataSetQuery = (i >= dataSetQueriesLen ? null : dataSetQueries.get(i));
				DataSetQuery dataSetQueryRe = convertDataSetQuery(dataSetQuery, chartDataSets[i].getDataSet());

				dataSetQueriesRe.add(dataSetQueryRe);
			}

			chartQueryRe.setDataSetQueries(dataSetQueriesRe);
			chartQueriesRe.put(chartId, chartQueryRe);
		}

		DashboardQuery re = query.copy();
		re.setChartQueries(chartQueriesRe);

		return re;
	}

	/**
	 * 转换{@linkplain DataSetQuery}为符合{@linkplain DataSet#getParams()}的类型。
	 * 
	 * @param dataSetQuery
	 *            允许{@code null}
	 * @param dataSet
	 * @return
	 */
	protected DataSetQuery convertDataSetQuery(DataSetQuery dataSetQuery, DataSet dataSet)
	{
		return getDataSetParamValueConverter().convert(dataSetQuery, dataSet, true);
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
