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

package org.datagear.analysis.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.util.i18n.Label;
import org.junit.Test;

/**
 * {@linkplain AbstractChartPluginManager}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class AbstractChartPluginManagerTest
{
	@Test
	public void isRegisterableTest()
	{
		SimpleChartPluginManager pm = new SimpleChartPluginManager();

		{
			TestChartPlugin p = new TestChartPlugin("test0", new Label("test0"));
			p.setVersion("0.2");
			boolean registered = pm.register(p);
			assertTrue(registered);
		}

		{
			TestChartPlugin p = new TestChartPlugin("test1", new Label("test1"));
			boolean registered = pm.register(p);
			assertTrue(registered);
		}

		// 插件非法
		{
			TestChartPlugin p = new TestChartPlugin();
			assertFalse(pm.isRegisterable(p));
		}

		// 未指定版本号
		{
			TestChartPlugin p = new TestChartPlugin("test0", new Label("test0"));
			assertFalse(pm.isRegisterable(p));
		}

		// 旧版本号
		{
			TestChartPlugin p = new TestChartPlugin("test0", new Label("test0"));
			p.setVersion("0.1");
			assertFalse(pm.isRegisterable(p));
		}

		// 相同版本号
		{
			TestChartPlugin p = new TestChartPlugin("test0", new Label("test0"));
			p.setVersion("0.2");
			assertFalse(pm.isRegisterable(p));
		}

		// 新版本号
		{
			TestChartPlugin p = new TestChartPlugin("test0", new Label("test0"));
			p.setVersion("0.3");
			assertTrue(pm.isRegisterable(p));
		}

		// 都没有版本号
		{
			TestChartPlugin p = new TestChartPlugin("test1", new Label("test1"));
			assertTrue(pm.isRegisterable(p));
		}
		{
			TestChartPlugin p = new TestChartPlugin("test1", new Label("test1"));
			assertTrue(pm.isRegisterable(p));
		}
		{
			TestChartPlugin p = new TestChartPlugin("test1", new Label("test1"));
			assertTrue(pm.isRegisterable(p));
		}

		// 已注册的没有版本号
		{
			TestChartPlugin p = new TestChartPlugin("test1", new Label("test1"));
			p.setVersion("0.0.1");
			assertTrue(pm.isRegisterable(p));
		}
	}

	@Test
	public void registerChartPluginTest()
	{
		SimpleChartPluginManager pm = new SimpleChartPluginManager();

		{
			TestChartPlugin p = new TestChartPlugin("test0", new Label("test0"));
			p.setVersion("0.2");
			boolean registered = pm.registerChartPlugin(p);
			assertTrue(registered);
		}

		{
			TestChartPlugin p = new TestChartPlugin("test1", new Label("test1"));
			boolean registered = pm.registerChartPlugin(p);
			assertTrue(registered);
		}

		// 未指定版本号
		{
			TestChartPlugin p = new TestChartPlugin("test0", new Label("test0-1"));
			assertFalse(pm.registerChartPlugin(p));
			assertEquals("test0", pm.get("test0").getNameLabel().getValue());
		}

		// 旧版本号
		{
			TestChartPlugin p = new TestChartPlugin("test0", new Label("test0-2"));
			p.setVersion("0.1");
			assertFalse(pm.registerChartPlugin(p));
			assertEquals("test0", pm.get("test0").getNameLabel().getValue());
		}

		// 相同版本号
		{
			TestChartPlugin p = new TestChartPlugin("test0", new Label("test0-3"));
			p.setVersion("0.2");
			assertFalse(pm.registerChartPlugin(p));
			assertEquals("test0", pm.get("test0").getNameLabel().getValue());
		}

		// 新版本号
		{
			TestChartPlugin p = new TestChartPlugin("test0", new Label("test0-4"));
			p.setVersion("0.3");
			assertTrue(pm.registerChartPlugin(p));
			assertEquals("test0-4", pm.get("test0").getNameLabel().getValue());
		}

		// 都没有版本号
		{
			TestChartPlugin p = new TestChartPlugin("test1", new Label("test1-1"));
			assertTrue(pm.registerChartPlugin(p));
			assertEquals("test1-1", pm.get("test1").getNameLabel().getValue());
		}
		{
			TestChartPlugin p = new TestChartPlugin("test1", new Label("test1-2"));
			assertTrue(pm.registerChartPlugin(p));
			assertEquals("test1-2", pm.get("test1").getNameLabel().getValue());
		}
		{
			TestChartPlugin p = new TestChartPlugin("test1", new Label("test1-3"));
			assertTrue(pm.registerChartPlugin(p));
			assertEquals("test1-3", pm.get("test1").getNameLabel().getValue());
		}

		// 已注册的没有版本号
		{
			TestChartPlugin p = new TestChartPlugin("test1", new Label("test1-4"));
			p.setVersion("0.0.1");
			assertTrue(pm.registerChartPlugin(p));
			assertEquals("test1-4", pm.get("test1").getNameLabel().getValue());
		}
	}

	private static class TestChartPlugin extends AbstractChartPlugin
	{
		private static final long serialVersionUID = 1L;

		public TestChartPlugin()
		{
			super();
		}

		public TestChartPlugin(String id, Label nameLabel)
		{
			super(id, nameLabel);
		}

		@Override
		public Chart renderChart(ChartDefinition chartDefinition, RenderContext renderContext) throws RenderException
		{
			throw new UnsupportedOperationException();
		}
	}
}
