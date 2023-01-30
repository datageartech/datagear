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

package org.datagear.analysis.support.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.InputStream;
import java.util.Set;

import org.datagear.analysis.ChartPlugin;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.junit.Test;

/**
 * {@linkplain DirectoryHtmlChartPluginManager}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class DirectoryHtmlChartPluginManagerTest
{
	public DirectoryHtmlChartPluginManagerTest()
	{
		super();
	}

	@Test
	public void uploadTest() throws Exception
	{
		File root = FileUtil.getFile("target/DirectoryHtmlChartPluginManagerTest/uploadTest/", true);
		File tmpDirectory = FileUtil.getFile("target/tmp/", true);
		File managerDirectory = FileUtil.getFile(root, "manager/", true);
		File uploadDirectory = FileUtil.getFile(root, "upload/", true);

		HtmlChartPluginLoader htmlChartPluginLoader = new HtmlChartPluginLoader();
		htmlChartPluginLoader.setTmpDirectory(tmpDirectory);

		DirectoryHtmlChartPluginManager directoryHtmlChartPluginManager = new DirectoryHtmlChartPluginManager(
				managerDirectory, htmlChartPluginLoader);
		directoryHtmlChartPluginManager.setTmpDirectory(tmpDirectory);

		FileUtil.clearDirectory(managerDirectory);
		FileUtil.clearDirectory(uploadDirectory);

		try (InputStream in = DirectoryHtmlChartPluginManagerTest.class.getClassLoader().getResourceAsStream(
				"org/datagear/analysis/support/html/directoryHtmlChartPluginManagerTest/plugin.current.zip"))
		{
			IOUtil.write(in, FileUtil.getFile(managerDirectory, "plugin.zip"));
		}

		try (InputStream in = DirectoryHtmlChartPluginManagerTest.class.getClassLoader().getResourceAsStream(
				"org/datagear/analysis/support/html/directoryHtmlChartPluginManagerTest/plugin.upload.zip"))
		{
			IOUtil.write(in, FileUtil.getFile(uploadDirectory, "plugin.zip"));
		}

		directoryHtmlChartPluginManager.init();

		ChartPlugin plugin = directoryHtmlChartPluginManager.get("test");

		assertNotNull(plugin);
		assertEquals("0.1.0", plugin.getVersion());

		{
			Set<HtmlChartPlugin> uploaded = directoryHtmlChartPluginManager.upload(uploadDirectory);
			plugin = directoryHtmlChartPluginManager.get("test");

			assertEquals(1, uploaded.size());
			assertNotNull(plugin);
			assertEquals("0.1.1", plugin.getVersion());
		}

		{
			Set<HtmlChartPlugin> uploaded = directoryHtmlChartPluginManager.upload(uploadDirectory);
			plugin = directoryHtmlChartPluginManager.get("test");

			assertEquals(0, uploaded.size());

			assertNotNull(plugin);
			assertEquals("0.1.1", plugin.getVersion());
		}
	}
}
