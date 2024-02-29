/*
 * Copyright 2018-2024 datagear.tech
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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginResource;
import org.datagear.analysis.support.FileChartPluginResource;
import org.datagear.analysis.support.ZipEntryChartPluginResource;
import org.datagear.util.FileUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@linkplain HtmlChartPluginLoader}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartPluginLoaderTest
{
	private HtmlChartPluginLoader htmlChartPluginLoader;

	public HtmlChartPluginLoaderTest()
	{
		super();
		this.htmlChartPluginLoader = new HtmlChartPluginLoader();
	}

	@Test
	public void loadTest()
	{
		File directory = FileUtil.getFile(
				"src/test/resources/org/datagear/analysis/support/html/htmlChartPluginLoaders/plugin01");

		HtmlChartPlugin plugin = this.htmlChartPluginLoader.load(directory);
		Assert.assertEquals("plugin01", plugin.getId());
		assertEquals("icon-01.png", plugin.getIconResourceName(ChartPlugin.DEFAULT_ICON_THEME_NAME));

		assertTrue(plugin.getRenderer() instanceof StringJsChartRenderer);
		StringJsChartRenderer renderer = (StringJsChartRenderer) plugin.getRenderer();
		assertEquals(JsChartRenderer.CODE_TYPE_OBJECT, renderer.getCodeType());
		assertEquals("{}", renderer.getCodeValue().trim());

		List<ChartPluginResource> resources = plugin.getResources();
		assertEquals(3, resources.size());

		Collections.sort(resources, new Comparator<ChartPluginResource>()
		{
			@Override
			public int compare(ChartPluginResource o1, ChartPluginResource o2)
			{
				return o1.getName().compareTo(o2.getName());
			}
		});

		assertTrue(resources.get(0) instanceof FileChartPluginResource);
		assertEquals("icon-01.png", resources.get(0).getName());

		assertTrue(resources.get(1) instanceof FileChartPluginResource);
		assertEquals("icons/icon-02.png", resources.get(1).getName());

		assertTrue(resources.get(2) instanceof FileChartPluginResource);
		assertEquals("plugin.json", resources.get(2).getName());
	}

	@Test
	public void loadZipTest()
	{
		File file = FileUtil.getFile(
				"src/test/resources/org/datagear/analysis/support/html/htmlChartPluginLoaders/plugin03.zip");

		HtmlChartPlugin plugin = this.htmlChartPluginLoader.loadZip(file);
		Assert.assertEquals("plugin03", plugin.getId());

		assertTrue(plugin.getRenderer() instanceof StringJsChartRenderer);
		StringJsChartRenderer renderer = (StringJsChartRenderer) plugin.getRenderer();
		assertEquals(JsChartRenderer.CODE_TYPE_OBJECT, renderer.getCodeType());
		assertEquals("{}", renderer.getCodeValue().trim());

		List<ChartPluginResource> resources = plugin.getResources();
		assertEquals(3, resources.size());

		Collections.sort(resources, new Comparator<ChartPluginResource>()
		{
			@Override
			public int compare(ChartPluginResource o1, ChartPluginResource o2)
			{
				return o1.getName().compareTo(o2.getName());
			}
		});

		assertTrue(resources.get(0) instanceof ZipEntryChartPluginResource);
		assertEquals("icon-01.png", resources.get(0).getName());
		assertEquals(file, ((ZipEntryChartPluginResource) resources.get(0)).getZipFile());

		assertTrue(resources.get(1) instanceof ZipEntryChartPluginResource);
		assertEquals("icons/icon-02.png", resources.get(1).getName());
		assertEquals(file, ((ZipEntryChartPluginResource) resources.get(1)).getZipFile());

		assertTrue(resources.get(2) instanceof ZipEntryChartPluginResource);
		assertEquals("plugin.json", resources.get(2).getName());
		assertEquals(file, ((ZipEntryChartPluginResource) resources.get(2)).getZipFile());
	}

	@Test
	public void loadsTest()
	{
		File directory = FileUtil
				.getFile("src/test/resources/org/datagear/analysis/support/html/htmlChartPluginLoaders");

		Set<HtmlChartPlugin> plugins = this.htmlChartPluginLoader.loadAll(directory);

		List<HtmlChartPlugin> list = new ArrayList<>();
		list.addAll(plugins);

		Assert.assertEquals(6, list.size());

		Collections.sort(list, new Comparator<HtmlChartPlugin>()
		{
			@Override
			public int compare(HtmlChartPlugin o1, HtmlChartPlugin o2)
			{
				return o1.getId().compareTo(o2.getId());
			}
		});

		{
			HtmlChartPlugin plugin = list.get(0);
			Assert.assertEquals("plugin01", plugin.getId());

			Map<String, String> icons = plugin.getIconResourceNames();
			Assert.assertEquals(1, icons.size());
			Assert.assertNotNull(icons.get(ChartPlugin.DEFAULT_ICON_THEME_NAME));
		}

		{
			HtmlChartPlugin plugin = list.get(1);
			Assert.assertEquals("plugin02", plugin.getId());

			Map<String, String> icons = plugin.getIconResourceNames();
			Assert.assertNotNull(icons.get("light"));
			Assert.assertNotNull(icons.get("dark"));
		}

		{
			HtmlChartPlugin plugin = list.get(2);
			Assert.assertEquals("plugin03", plugin.getId());

			Map<String, String> icons = plugin.getIconResourceNames();
			Assert.assertNotNull(icons.get("light"));
			Assert.assertNotNull(icons.get("dark"));
		}

		{
			HtmlChartPlugin plugin = list.get(3);
			Assert.assertEquals("plugin04", plugin.getId());

			Map<String, String> icons = plugin.getIconResourceNames();
			Assert.assertNotNull(icons.get("light"));
			Assert.assertNotNull(icons.get("dark"));
		}

		{
			HtmlChartPlugin plugin = list.get(4);
			Assert.assertEquals("plugin05", plugin.getId());
			StringJsChartRenderer chartRenderer = (StringJsChartRenderer) plugin.getRenderer();
			assertEquals(JsChartRenderer.CODE_TYPE_OBJECT, chartRenderer.getCodeType());
			Assert.assertEquals(" { render: function(chart){ } }", chartRenderer.getCodeValue());
		}

		{
			HtmlChartPlugin plugin = list.get(5);
			Assert.assertEquals("plugin06", plugin.getId());
			StringJsChartRenderer chartRenderer = (StringJsChartRenderer) plugin.getRenderer();
			assertEquals(JsChartRenderer.CODE_TYPE_INVOKE, chartRenderer.getCodeType());
			Assert.assertTrue(chartRenderer.getCodeValue().contains("(function(localPlugin)"));
		}
	}
}
