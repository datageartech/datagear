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
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.datagear.analysis.DataSign;
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
		assertEquals(HtmlChartPluginUsage.NORMAL, chartPlugin.getUsage());
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
		assertEquals(HtmlChartPluginUsage.NORMAL, chartPlugin.getUsage());
	}

	@Test
	public void resolvePropertiesTest_type_lib() throws IOException
	{
		InputStream jsonInputStream = getClass().getClassLoader().getResourceAsStream(
				"org/datagear/analysis/support/html/JsonHtmlChartPluginPropertiesResolverTest-usage-lib.json");

		HtmlChartPlugin chartPlugin = new HtmlChartPlugin();
		JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin> resolver = new JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin>(
				chartPlugin);
		resolver.resolveProperties(jsonInputStream, IOUtil.CHARSET_UTF_8);

		assertEquals("2.0", chartPlugin.getApiVersion());
		assertEquals(HtmlChartPluginUsage.LIB, chartPlugin.getUsage());
	}

	@Test
	public void resolvePropertiesTest_type_normal() throws IOException
	{
		InputStream jsonInputStream = getClass().getClassLoader().getResourceAsStream(
				"org/datagear/analysis/support/html/JsonHtmlChartPluginPropertiesResolverTest-usage-normal.json");

		HtmlChartPlugin chartPlugin = new HtmlChartPlugin();
		JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin> resolver = new JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin>(
				chartPlugin);
		resolver.resolveProperties(jsonInputStream, IOUtil.CHARSET_UTF_8);

		assertEquals("2.0", chartPlugin.getApiVersion());
		assertEquals(HtmlChartPluginUsage.NORMAL, chartPlugin.getUsage());
	}

	@Test
	public void resolvePropertiesTest_type_unknown() throws IOException
	{
		InputStream jsonInputStream = getClass().getClassLoader().getResourceAsStream(
				"org/datagear/analysis/support/html/JsonHtmlChartPluginPropertiesResolverTest-usage-unknown.json");

		HtmlChartPlugin chartPlugin = new HtmlChartPlugin();
		JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin> resolver = new JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin>(
				chartPlugin);
		resolver.resolveProperties(jsonInputStream, IOUtil.CHARSET_UTF_8);

		assertEquals("2.0", chartPlugin.getApiVersion());
		assertEquals(HtmlChartPluginUsage.NORMAL, chartPlugin.getUsage());
	}

	@Test
	public void convertToDataSignTest() throws IOException
	{
		HtmlChartPlugin chartPlugin = new HtmlChartPlugin();
		JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin> resolver = new JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin>(
				chartPlugin);

		{
			chartPlugin.setApiVersion(DashboardApiVersion.V1);

			Map<String, Object> map = new HashMap<>();
			map.put("name", "3.5");

			DataSign dataSign = resolver.convertToDataSign(map, null);
			assertEquals("3.5", dataSign.getName());
		}

		{
			chartPlugin.setApiVersion(DashboardApiVersion.V2);

			Map<String, Object> map = new HashMap<>();
			map.put("name", "3.5");

			assertThrows(IllegalArgumentException.class, () ->
			{
				DataSign dataSign = resolver.convertToDataSign(map, null);
				assertEquals("3.5", dataSign.getName());
			});
		}

		{
			chartPlugin.setApiVersion(DashboardApiVersion.V2);

			Map<String, Object> map = new HashMap<>();
			map.put("name", "a.b");

			assertThrows(IllegalArgumentException.class, () ->
			{
				DataSign dataSign = resolver.convertToDataSign(map, null);
				assertEquals("a.b", dataSign.getName());
			});
		}

		{
			chartPlugin.setApiVersion(DashboardApiVersion.V2);

			Map<String, Object> map = new HashMap<>();
			map.put("name", "abc");

			DataSign dataSign = resolver.convertToDataSign(map, null);
			assertEquals("abc", dataSign.getName());
		}

		{
			chartPlugin.setApiVersion(DashboardApiVersion.V2);

			Map<String, Object> map = new HashMap<>();
			map.put("name", "_123");

			DataSign dataSign = resolver.convertToDataSign(map, null);
			assertEquals("_123", dataSign.getName());
		}

		{
			chartPlugin.setApiVersion(DashboardApiVersion.V2);

			Map<String, Object> map = new HashMap<>();
			map.put("name", "$a123");

			DataSign dataSign = resolver.convertToDataSign(map, null);
			assertEquals("$a123", dataSign.getName());
		}
	}
}
