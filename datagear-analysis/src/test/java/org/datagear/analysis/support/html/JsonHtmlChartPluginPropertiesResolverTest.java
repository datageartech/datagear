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
	@Test
	public void resolvePropertiesTest_platformVersion() throws IOException
	{
		InputStream jsonInputStream = getClass().getClassLoader().getResourceAsStream(
				"org/datagear/analysis/support/html/JsonHtmlChartPluginPropertiesResolverTest-platformVersion.json");

		HtmlChartPlugin chartPlugin = new HtmlChartPlugin();
		JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin> resolver = new JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin>(
				chartPlugin);
		resolver.resolveProperties(jsonInputStream, IOUtil.CHARSET_UTF_8);

		assertEquals("5.2.0", chartPlugin.getPlatformVersion());
		assertEquals(HtmlChartPluginUse.NORMAL, chartPlugin.getUse());
	}

	@Test
	public void resolvePropertiesTest_apiVersion() throws IOException
	{
		InputStream jsonInputStream = getClass().getClassLoader().getResourceAsStream(
				"org/datagear/analysis/support/html/JsonHtmlChartPluginPropertiesResolverTest-apiVersion.json");

		HtmlChartPlugin chartPlugin = new HtmlChartPlugin();
		JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin> resolver = new JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin>(
				chartPlugin);
		resolver.resolveProperties(jsonInputStream, IOUtil.CHARSET_UTF_8);

		assertEquals("2.0", chartPlugin.getApiVersion());
		assertEquals(HtmlChartPluginUse.NORMAL, chartPlugin.getUse());
	}

	@Test
	public void resolvePropertiesTest_type_lib() throws IOException
	{
		InputStream jsonInputStream = getClass().getClassLoader().getResourceAsStream(
				"org/datagear/analysis/support/html/JsonHtmlChartPluginPropertiesResolverTest-use-lib.json");

		HtmlChartPlugin chartPlugin = new HtmlChartPlugin();
		JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin> resolver = new JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin>(
				chartPlugin);
		resolver.resolveProperties(jsonInputStream, IOUtil.CHARSET_UTF_8);

		assertEquals("2.0", chartPlugin.getApiVersion());
		assertEquals(HtmlChartPluginUse.LIB, chartPlugin.getUse());
	}

	@Test
	public void resolvePropertiesTest_type_normal() throws IOException
	{
		InputStream jsonInputStream = getClass().getClassLoader().getResourceAsStream(
				"org/datagear/analysis/support/html/JsonHtmlChartPluginPropertiesResolverTest-use-normal.json");

		HtmlChartPlugin chartPlugin = new HtmlChartPlugin();
		JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin> resolver = new JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin>(
				chartPlugin);
		resolver.resolveProperties(jsonInputStream, IOUtil.CHARSET_UTF_8);

		assertEquals("2.0", chartPlugin.getApiVersion());
		assertEquals(HtmlChartPluginUse.NORMAL, chartPlugin.getUse());
	}

	@Test
	public void resolvePropertiesTest_type_unknown() throws IOException
	{
		InputStream jsonInputStream = getClass().getClassLoader().getResourceAsStream(
				"org/datagear/analysis/support/html/JsonHtmlChartPluginPropertiesResolverTest-use-unknown.json");

		HtmlChartPlugin chartPlugin = new HtmlChartPlugin();
		JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin> resolver = new JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin>(
				chartPlugin);
		resolver.resolveProperties(jsonInputStream, IOUtil.CHARSET_UTF_8);

		assertEquals("2.0", chartPlugin.getApiVersion());
		assertEquals(HtmlChartPluginUse.NORMAL, chartPlugin.getUse());
	}
}
