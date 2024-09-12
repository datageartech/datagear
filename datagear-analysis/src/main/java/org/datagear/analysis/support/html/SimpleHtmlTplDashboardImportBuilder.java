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

package org.datagear.analysis.support.html;

import java.util.Collections;
import java.util.List;

/**
 * 简单{@linkplain HtmlTplDashboardImportBuilder}。
 * 
 * @author datagear@163.com
 *
 */
public class SimpleHtmlTplDashboardImportBuilder implements HtmlTplDashboardImportBuilder
{
	private List<HtmlTplDashboardImport> dashboardImports = Collections.emptyList();
	
	public SimpleHtmlTplDashboardImportBuilder()
	{
		super();
	}

	public SimpleHtmlTplDashboardImportBuilder(List<HtmlTplDashboardImport> dashboardImports)
	{
		super();
		this.dashboardImports = dashboardImports;
	}

	public List<HtmlTplDashboardImport> getDashboardImports()
	{
		return dashboardImports;
	}

	public void setDashboardImports(List<HtmlTplDashboardImport> dashboardImports)
	{
		this.dashboardImports = dashboardImports;
	}

	@Override
	public List<HtmlTplDashboardImport> build(HtmlTplDashboardRenderContext renderContext, HtmlTplDashboard dashboard)
	{
		return this.dashboardImports;
	}
}
