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

import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.analysis.support.html.HtmlChartPluginLoadException;
import org.datagear.analysis.support.html.HtmlChartPluginLoadedProcessor;

/**
 * 将{@linkplain HtmlChartPlugin#getApiVersion()}默认设置为{@linkplain DashboardApiVersion#V1}的处理器。
 * <p>
 * 对于旧版的图表插件，需要将其设置为仅支持旧版的API版本，以解决在系统升级新版后的兼容问题。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ApiVersionHtmlChartPluginLoadedProcessor implements HtmlChartPluginLoadedProcessor
{
	@Override
	public void process(HtmlChartPlugin plugin) throws HtmlChartPluginLoadException
	{
		plugin.setApiVersion(DashboardApiVersion.trimVersion(plugin.getApiVersion()));
	}
}
