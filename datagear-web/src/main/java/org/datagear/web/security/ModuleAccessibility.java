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

package org.datagear.web.security;

import java.io.Serializable;

/**
 * 模块可访问性。
 * 
 * @author datagear@163.com
 *
 */
public class ModuleAccessibility implements Serializable
{
	private static final long serialVersionUID = 1L;

	private boolean schemaAccessible = false;

	private boolean schemaOperator = false;

	private boolean analysisProjectAccessible = false;

	private boolean analysisProjectOperator = false;

	private boolean dataSetAccessible = false;

	private boolean dataSetOperator = false;

	private boolean chartAccessible = false;

	private boolean chartOperator = false;

	private boolean dashboardAccessible = false;

	private boolean dashboardOperator = false;

	public ModuleAccessibility()
	{
		super();
	}

	public ModuleAccessibility(boolean schemaAccessible, boolean schemaOperator, boolean analysisProjectAccessible,
			boolean analysisProjectOperator, boolean dataSetAccessible, boolean dataSetOperator,
			boolean chartAccessible, boolean chartOperator, boolean dashboardAccessible, boolean dashboardOperator)
	{
		super();
		this.schemaAccessible = schemaAccessible;
		this.schemaOperator = schemaOperator;
		this.analysisProjectAccessible = analysisProjectAccessible;
		this.analysisProjectOperator = analysisProjectOperator;
		this.dataSetAccessible = dataSetAccessible;
		this.dataSetOperator = dataSetOperator;
		this.chartAccessible = chartAccessible;
		this.chartOperator = chartOperator;
		this.dashboardAccessible = dashboardAccessible;
		this.dashboardOperator = dashboardOperator;
	}

	public boolean isSchemaAccessible()
	{
		return schemaAccessible;
	}

	public void setSchemaAccessible(boolean schemaAccessible)
	{
		this.schemaAccessible = schemaAccessible;
	}

	public boolean isSchemaOperator()
	{
		return schemaOperator;
	}

	public void setSchemaOperator(boolean schemaOperator)
	{
		this.schemaOperator = schemaOperator;
	}

	public boolean isAnalysisProjectAccessible()
	{
		return analysisProjectAccessible;
	}

	public void setAnalysisProjectAccessible(boolean analysisProjectAccessible)
	{
		this.analysisProjectAccessible = analysisProjectAccessible;
	}

	public boolean isAnalysisProjectOperator()
	{
		return analysisProjectOperator;
	}

	public void setAnalysisProjectOperator(boolean analysisProjectOperator)
	{
		this.analysisProjectOperator = analysisProjectOperator;
	}

	public boolean isDataSetAccessible()
	{
		return dataSetAccessible;
	}

	public void setDataSetAccessible(boolean dataSetAccessible)
	{
		this.dataSetAccessible = dataSetAccessible;
	}

	public boolean isDataSetOperator()
	{
		return dataSetOperator;
	}

	public void setDataSetOperator(boolean dataSetOperator)
	{
		this.dataSetOperator = dataSetOperator;
	}

	public boolean isChartAccessible()
	{
		return chartAccessible;
	}

	public void setChartAccessible(boolean chartAccessible)
	{
		this.chartAccessible = chartAccessible;
	}

	public boolean isChartOperator()
	{
		return chartOperator;
	}

	public void setChartOperator(boolean chartOperator)
	{
		this.chartOperator = chartOperator;
	}

	public boolean isDashboardAccessible()
	{
		return dashboardAccessible;
	}

	public void setDashboardAccessible(boolean dashboardAccessible)
	{
		this.dashboardAccessible = dashboardAccessible;
	}

	public boolean isDashboardOperator()
	{
		return dashboardOperator;
	}

	public void setDashboardOperator(boolean dashboardOperator)
	{
		this.dashboardOperator = dashboardOperator;
	}
}
