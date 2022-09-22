/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.support.AbstractChartPluginManager;
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
		this.htmlChartPluginLoader.setTmpDirectory(FileUtil.getFile("target/tmp/", true));
	}

	@Test
	public void loadTest()
	{
		File directory = FileUtil.getFile(
				"src/test/resources/org/datagear/analysis/support/html/htmlChartPluginLoaders/plugin01");

		HtmlChartPlugin plugin = this.htmlChartPluginLoader.load(directory);
		Assert.assertEquals("plugin01", plugin.getId());
	}

	@Test
	public void loadZipTest()
	{
		File file = FileUtil.getFile(
				"src/test/resources/org/datagear/analysis/support/html/htmlChartPluginLoaders/plugin03.zip");

		HtmlChartPlugin plugin = this.htmlChartPluginLoader.loadZip(file);
		Assert.assertEquals("plugin03", plugin.getId());
	}

	@Test
	public void loadsTest()
	{
		File directory = FileUtil
				.getFile("src/test/resources/org/datagear/analysis/support/html/htmlChartPluginLoaders");

		Set<HtmlChartPlugin> plugins = this.htmlChartPluginLoader.loadAll(directory);

		List<HtmlChartPlugin> list = new ArrayList<>();
		list.addAll(plugins);

		AbstractChartPluginManager.sort(list);

		Assert.assertEquals(5, list.size());

		{
			HtmlChartPlugin plugin = list.get(0);
			Assert.assertEquals("plugin05", plugin.getId());
			StringJsChartRenderer chartRenderer = (StringJsChartRenderer)plugin.getRenderer();
			Assert.assertEquals(" { render: function(chart){ } }", chartRenderer.getContent());
		}

		{
			HtmlChartPlugin plugin = list.get(1);
			Assert.assertEquals("plugin04", plugin.getId());

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
			Assert.assertEquals("plugin02", plugin.getId());

			Map<String, String> icons = plugin.getIconResourceNames();
			Assert.assertNotNull(icons.get("light"));
			Assert.assertNotNull(icons.get("dark"));
		}

		{
			HtmlChartPlugin plugin = list.get(4);
			Assert.assertEquals("plugin01", plugin.getId());

			Map<String, String> icons = plugin.getIconResourceNames();
			Assert.assertEquals(1, icons.size());
			Assert.assertNotNull(icons.get(ChartPlugin.DEFAULT_ICON_THEME_NAME));
		}
	}
}
