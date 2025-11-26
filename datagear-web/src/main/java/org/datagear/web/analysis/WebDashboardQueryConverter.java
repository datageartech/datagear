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

package org.datagear.web.analysis;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.ChartQuery;
import org.datagear.analysis.DashboardQuery;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.support.DashboardQueryConverter;
import org.datagear.analysis.support.DataSetParamValueConverter;
import org.datagear.management.domain.User;

/**
 * Web环境的{@linkplain DashboardQuery}转换器。
 * 
 * @author datagear@163.com
 *
 */
public class WebDashboardQueryConverter
{
	private DashboardQueryConverter dashboardQueryConverter;

	public WebDashboardQueryConverter()
	{
		super();
	}

	public WebDashboardQueryConverter(DashboardQueryConverter dashboardQueryConverter)
	{
		super();
		this.dashboardQueryConverter = dashboardQueryConverter;
	}

	public DashboardQueryConverter getDashboardQueryConverter()
	{
		return dashboardQueryConverter;
	}

	public void setDashboardQueryConverter(DashboardQueryConverter dashboardQueryConverter)
	{
		this.dashboardQueryConverter = dashboardQueryConverter;
	}

	/**
	 * 转换{@linkplain DashboardQuery}。
	 * <p>
	 * 将{@linkplain DashboardQuery}包含的{@linkplain DataSetQuery}转换为符合{@linkplain DataSet#getParams()}类型。
	 * </p>
	 * 
	 * @param query
	 *            允许{@code null}
	 * @param chartDefs
	 *            {@linkplain DashboardQuery#getChartQueries()}的图表ID-{@linkplain ChartDefinition}映射表
	 * @param user
	 * @return
	 */
	public DashboardQuery convert(DashboardQuery query, Map<String, ? extends ChartDefinition> chartDefs, User user)
	{
		AnalysisUser analysisUser = toAnalysisUser(user);
		return convert(query, chartDefs, analysisUser);
	}

	/**
	 * 转换{@linkplain DashboardQuery}。
	 * <p>
	 * 将{@linkplain DashboardQuery}包含的{@linkplain DataSetQuery}转换为符合{@linkplain DataSet#getParams()}类型。
	 * </p>
	 * 
	 * @param query
	 *            允许{@code null}
	 * @param chartDefs
	 *            {@linkplain DashboardQuery#getChartQueries()}的图表ID-{@linkplain ChartDefinition}映射表
	 * @param analysisUser
	 * @return
	 */
	public DashboardQuery convert(DashboardQuery query, Map<String, ? extends ChartDefinition> chartDefs,
			AnalysisUser analysisUser)
	{
		DashboardQuery re = this.dashboardQueryConverter.convert(query, chartDefs);
		inflateAnalysisUser(re, analysisUser);

		return re;
	}

	/**
	 * 转换{@linkplain DataSetQuery}。
	 * 
	 * @param dataSetQuery
	 * @param dataSet
	 * @param user
	 * @return
	 */
	public DataSetQuery convert(DataSetQuery dataSetQuery, DataSet dataSet, User user)
	{
		AnalysisUser analysisUser = toAnalysisUser(user);
		return convert(dataSetQuery, dataSet, analysisUser);
	}

	/**
	 * 转换{@linkplain DataSetQuery}。
	 * 
	 * @param dataSetQuery
	 * @param dataSet
	 * @param analysisUser
	 * @return
	 */
	public DataSetQuery convert(DataSetQuery dataSetQuery, DataSet dataSet, AnalysisUser analysisUser)
	{
		DataSetQuery re = getDataSetParamValueConverter().convert(dataSetQuery, dataSet);
		inflateAnalysisUser(re, analysisUser);

		return re;
	}

	/**
	 * 转换{@linkplain DataSetQuery}。
	 * 
	 * @param dataSetQuery
	 * @param dataSet
	 * @return
	 */
	public DataSetQuery convert(DataSetQuery dataSetQuery, DataSet dataSet)
	{
		return getDataSetParamValueConverter().convert(dataSetQuery, dataSet);
	}

	/**
	 * 转换为{@linkplain DataSetQuery}。
	 * 
	 * @param paramValues
	 * @param dataSetParams
	 * @param user
	 * @return
	 */
	public DataSetQuery convert(Map<String, ?> paramValues, Collection<? extends DataSetParam> dataSetParams, User user)
	{
		AnalysisUser analysisUser = toAnalysisUser(user);
		return convert(paramValues, dataSetParams, analysisUser);
	}

	/**
	 * 转换为{@linkplain DataSetQuery}。
	 * 
	 * @param paramValues
	 * @param dataSetParams
	 * @param analysisUser
	 * @return
	 */
	public DataSetQuery convert(Map<String, ?> paramValues, Collection<? extends DataSetParam> dataSetParams,
			AnalysisUser analysisUser)
	{
		Map<String, ?> converted = convert(paramValues, dataSetParams);
		DataSetQuery re = DataSetQuery.valueOf(converted);
		inflateAnalysisUser(re, analysisUser);

		return re;
	}

	/**
	 * 转换参数。
	 * 
	 * @param paramValues
	 * @param dataSetParams
	 * @return
	 */
	public Map<String, ?> convert(Map<String, ?> paramValues, Collection<? extends DataSetParam> dataSetParams)
	{
		return getDataSetParamValueConverter().convert(paramValues, dataSetParams);
	}

	/**
	 * 将{@linkplain AnalysisUser}填充至{@linkplain DashboardQuery}包含的所有{@linkplain DataSetQuery}。
	 * 
	 * @param query
	 * @param analysisUser
	 */
	public void inflateAnalysisUser(DashboardQuery query, AnalysisUser analysisUser)
	{
		List<String> analysisRoleNames = analysisUser.getEnabledRoleNames();

		Map<String, ChartQuery> chartQueries = query.getChartQueries();

		for (Map.Entry<String, ChartQuery> entry : chartQueries.entrySet())
		{
			ChartQuery chartQuery = entry.getValue();
			List<DataSetQuery> dataSetQueries = chartQuery.getDataSetQueries();

			for (DataSetQuery dataSetQuery : dataSetQueries)
			{
				analysisUser.setParamValue(dataSetQuery, analysisRoleNames);
			}
		}
	}

	/**
	 * 将{@linkplain AnalysisUser}填充至{@linkplain DataSetQuery}。
	 * 
	 * @param query
	 * @param user
	 */
	public void inflateAnalysisUser(DataSetQuery query, User user)
	{
		AnalysisUser analysisUser = toAnalysisUser(user);
		inflateAnalysisUser(query, analysisUser);
	}

	/**
	 * 将{@linkplain AnalysisUser}填充至{@linkplain DataSetQuery}。
	 * 
	 * @param query
	 * @param analysisUser
	 */
	public void inflateAnalysisUser(DataSetQuery query, AnalysisUser analysisUser)
	{
		List<String> analysisRoleNames = analysisUser.getEnabledRoleNames();
		analysisUser.setParamValue(query, analysisRoleNames);
	}

	/**
	 * 将{@linkplain User}转换为{@linkplain AnalysisUser}。
	 * 
	 * @param user
	 * @return
	 */
	public AnalysisUser toAnalysisUser(User user)
	{
		return new AnalysisUser(user);
	}

	protected DataSetParamValueConverter getDataSetParamValueConverter()
	{
		return getDashboardQueryConverter().getDataSetParamValueConverter();
	}
}
