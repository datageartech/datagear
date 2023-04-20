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

	private boolean analysisProjectAccessible = false;

	private boolean dataSetAccessible = false;

	private boolean chartAccessible = false;

	private boolean dashboardAccessible = false;

	public ModuleAccessibility()
	{
		super();
	}

	public ModuleAccessibility(boolean schemaAccessible, boolean analysisProjectAccessible, boolean dataSetAccessible,
			boolean chartAccessible, boolean dashboardAccessible)
	{
		super();
		this.schemaAccessible = schemaAccessible;
		this.analysisProjectAccessible = analysisProjectAccessible;
		this.dataSetAccessible = dataSetAccessible;
		this.chartAccessible = chartAccessible;
		this.dashboardAccessible = dashboardAccessible;
	}

	public boolean isSchemaAccessible()
	{
		return schemaAccessible;
	}

	public void setSchemaAccessible(boolean schemaAccessible)
	{
		this.schemaAccessible = schemaAccessible;
	}

	public boolean isAnalysisProjectAccessible()
	{
		return analysisProjectAccessible;
	}

	public void setAnalysisProjectAccessible(boolean analysisProjectAccessible)
	{
		this.analysisProjectAccessible = analysisProjectAccessible;
	}

	public boolean isDataSetAccessible()
	{
		return dataSetAccessible;
	}

	public void setDataSetAccessible(boolean dataSetAccessible)
	{
		this.dataSetAccessible = dataSetAccessible;
	}

	public boolean isChartAccessible()
	{
		return chartAccessible;
	}

	public void setChartAccessible(boolean chartAccessible)
	{
		this.chartAccessible = chartAccessible;
	}

	public boolean isDashboardAccessible()
	{
		return dashboardAccessible;
	}

	public void setDashboardAccessible(boolean dashboardAccessible)
	{
		this.dashboardAccessible = dashboardAccessible;
	}
}
