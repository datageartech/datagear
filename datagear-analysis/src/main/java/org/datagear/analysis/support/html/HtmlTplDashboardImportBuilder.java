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

import java.util.List;

/**
 * {@linkplain HtmlTplDashboardImport}列表构建器。
 * 
 * @author datagear@163.com
 *
 */
public interface HtmlTplDashboardImportBuilder
{
	/**
	 * 构建{@linkplain HtmlTplDashboardImport}列表。
	 * 
	 * @param renderContext
	 * @param dashboard
	 * @return
	 */
	List<HtmlTplDashboardImport> build(HtmlTplDashboardRenderContext renderContext, HtmlTplDashboard dashboard);
}
