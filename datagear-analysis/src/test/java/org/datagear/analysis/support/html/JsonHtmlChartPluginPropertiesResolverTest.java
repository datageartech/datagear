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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.datagear.util.IOUtil;
import org.junit.Test;

/**
 * {@linkplain JsonHtmlChartPluginPropertiesResolver}单元测试类
 * 
 * @author datagear@163.com
 *
 */
public class JsonHtmlChartPluginPropertiesResolverTest
{
	private JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin> resolver = new JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin>();

	@Test
	public void resolveChartPluginPropertiesTest_platformVersion() throws IOException
	{
		InputStream jsonInputStream = getClass().getClassLoader().getResourceAsStream(
				"org/datagear/analysis/support/html/JsonHtmlChartPluginPropertiesResolverTest-platformVersion.json");

		HtmlChartPlugin chartPlugin = new HtmlChartPlugin();
		resolver.resolveChartPluginProperties(chartPlugin, jsonInputStream, IOUtil.CHARSET_UTF_8);

		assertEquals("5.2.0", chartPlugin.getPlatformVersion());
	}

	@Test
	public void resolveChartPluginPropertiesTest_apiVersion() throws IOException
	{
		InputStream jsonInputStream = getClass().getClassLoader().getResourceAsStream(
				"org/datagear/analysis/support/html/JsonHtmlChartPluginPropertiesResolverTest-apiVersion.json");

		HtmlChartPlugin chartPlugin = new HtmlChartPlugin();
		resolver.resolveChartPluginProperties(chartPlugin, jsonInputStream, IOUtil.CHARSET_UTF_8);

		assertEquals(">=1.0", chartPlugin.getApiVersion());
	}
}
