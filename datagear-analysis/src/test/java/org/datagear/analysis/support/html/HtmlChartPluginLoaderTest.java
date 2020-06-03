/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.datagear.analysis.Icon;
import org.datagear.analysis.RenderStyle;
import org.datagear.analysis.support.AbstractChartPluginManager;
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
	private HtmlChartPluginLoader htmlChartPluginLoader = new HtmlChartPluginLoader();

	@Test
	public void loadTest()
	{
		File directory = new File(
				"src/test/resources/org/datagear/analysis/support/html/htmlChartPluginLoaders/plugin01");

		HtmlChartPlugin plugin = this.htmlChartPluginLoader.load(directory);
		Assert.assertEquals("plugin01", plugin.getId());
	}

	@Test
	public void loadZipTest()
	{
		File file = new File(
				"src/test/resources/org/datagear/analysis/support/html/htmlChartPluginLoaders/plugin03.zip");

		HtmlChartPlugin plugin = this.htmlChartPluginLoader.loadZip(file);
		Assert.assertEquals("plugin03", plugin.getId());
	}

	@Test
	public void loadsTest()
	{
		File directory = new File("src/test/resources/org/datagear/analysis/support/html/htmlChartPluginLoaders");

		Set<HtmlChartPlugin> plugins = this.htmlChartPluginLoader.loads(directory);

		List<HtmlChartPlugin> list = new ArrayList<>();
		list.addAll(plugins);

		AbstractChartPluginManager.sort(list);

		Assert.assertEquals(4, list.size());

		{
			HtmlChartPlugin plugin = list.get(0);
			Assert.assertEquals("plugin04", plugin.getId());

			Map<RenderStyle, Icon> icons = plugin.getIcons();
			Assert.assertNotNull(icons.get(RenderStyle.LIGHT));
			Assert.assertNotNull(icons.get(RenderStyle.DARK));
		}

		{
			HtmlChartPlugin plugin = list.get(1);
			Assert.assertEquals("plugin03", plugin.getId());

			Map<RenderStyle, Icon> icons = plugin.getIcons();
			Assert.assertNotNull(icons.get(RenderStyle.LIGHT));
			Assert.assertNotNull(icons.get(RenderStyle.DARK));
		}

		{
			HtmlChartPlugin plugin = list.get(2);
			Assert.assertEquals("plugin02", plugin.getId());

			Map<RenderStyle, Icon> icons = plugin.getIcons();
			Assert.assertNotNull(icons.get(RenderStyle.LIGHT));
			Assert.assertNotNull(icons.get(RenderStyle.DARK));
		}

		{
			HtmlChartPlugin plugin = list.get(3);
			Assert.assertEquals("plugin01", plugin.getId());

			Map<RenderStyle, Icon> icons = plugin.getIcons();
			Assert.assertNotNull(icons.get(RenderStyle.LIGHT));
			Assert.assertNotNull(icons.get(RenderStyle.DARK));
		}
	}
}
