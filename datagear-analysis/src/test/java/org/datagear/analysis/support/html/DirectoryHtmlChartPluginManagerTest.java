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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginResource;
import org.datagear.analysis.support.ZipEntryChartPluginResource;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.LastModifiedService;
import org.datagear.util.SimpleLastModifiedService;
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
		LastModifiedService lastModifiedService = new SimpleLastModifiedService();

		DirectoryHtmlChartPluginManager directoryHtmlChartPluginManager = new DirectoryHtmlChartPluginManager(
				managerDirectory, htmlChartPluginLoader);
		directoryHtmlChartPluginManager.setTmpDirectory(tmpDirectory);
		directoryHtmlChartPluginManager.setLastModifiedService(lastModifiedService);

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

		long lastModified1 = directoryHtmlChartPluginManager.getLastModified();
		long lastModified2 = directoryHtmlChartPluginManager.getLastModified();
		long lastModified3 = directoryHtmlChartPluginManager.getLastModified();

		ChartPlugin plugin = directoryHtmlChartPluginManager.get("test");

		assertNotNull(plugin);
		assertEquals("0.1.0", plugin.getVersion());

		{
			Set<HtmlChartPlugin> uploaded = directoryHtmlChartPluginManager.upload(uploadDirectory);
			plugin = directoryHtmlChartPluginManager.get("test");

			assertEquals(1, uploaded.size());
			assertNotNull(plugin);
			assertEquals("0.1.1", plugin.getVersion());

			List<ChartPluginResource> resources = plugin.getResources();
			assertEquals(1, resources.size());

			Collections.sort(resources, new Comparator<ChartPluginResource>()
			{
				@Override
				public int compare(ChartPluginResource o1, ChartPluginResource o2)
				{
					return o1.getName().compareTo(o2.getName());
				}
			});

			assertTrue(resources.get(0) instanceof ZipEntryChartPluginResource);
			assertEquals("plugin.json", resources.get(0).getName());
			assertEquals(FileUtil.getFile(managerDirectory, "plugin.zip"),
					((ZipEntryChartPluginResource) resources.get(0)).getZipFile());

			lastModified2 = directoryHtmlChartPluginManager.getLastModified();
			assertTrue(lastModified2 != lastModified1);
		}

		{
			Set<HtmlChartPlugin> uploaded = directoryHtmlChartPluginManager.upload(uploadDirectory);
			plugin = directoryHtmlChartPluginManager.get("test");

			assertEquals(0, uploaded.size());

			assertNotNull(plugin);
			assertEquals("0.1.1", plugin.getVersion());

			lastModified3 = directoryHtmlChartPluginManager.getLastModified();
			assertTrue(lastModified3 != lastModified1);
			assertTrue(lastModified3 == lastModified2);
		}
	}
}
