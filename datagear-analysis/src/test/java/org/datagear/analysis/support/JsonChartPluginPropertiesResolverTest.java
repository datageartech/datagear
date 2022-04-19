/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.Category;
import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.ChartParam;
import org.datagear.analysis.DataSign;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.util.i18n.Label;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@linkplain JsonChartPluginPropertiesResolver}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class JsonChartPluginPropertiesResolverTest
{
	private JsonChartPluginPropertiesResolver jsonChartPluginPropertiesResolver = new JsonChartPluginPropertiesResolver();

	@Test
	public void resolveChartPluginPropertiesTest() throws IOException
	{
		{
			InputStream jsonInputStream = getClass().getClassLoader()
					.getResourceAsStream("org/datagear/analysis/support/JsonChartPluginPropertiesResolverTest.json");

			TestChartPlugin chartPlugin = new TestChartPlugin();
			jsonChartPluginPropertiesResolver.resolveChartPluginProperties(chartPlugin, jsonInputStream, "UTF-8");

			Assert.assertEquals("pie-chart", chartPlugin.getId());
			Assert.assertNotNull(chartPlugin.getNameLabel());
			Assert.assertNotNull(chartPlugin.getDescLabel());
			Assert.assertNotNull(chartPlugin.getManualLabel());
			Assert.assertNotNull(chartPlugin.getIcons());
			Assert.assertNotNull(chartPlugin.getChartParams());
			Assert.assertNotNull(chartPlugin.getDataSigns());
			Assert.assertEquals("0.1.0", chartPlugin.getVersion());
			Assert.assertEquals(2, chartPlugin.getOrder());
			Assert.assertNotNull(chartPlugin.getCategories());
			Assert.assertEquals(2, chartPlugin.getCategories().size());
			Assert.assertNotNull(chartPlugin.getCategoryOrders());
			Assert.assertEquals(2, chartPlugin.getCategoryOrders().size());

			{
				Label nameLabel = chartPlugin.getNameLabel();
				Assert.assertEquals("饼图", nameLabel.getValue());
				Assert.assertEquals("pie chart", nameLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("饼图中文", nameLabel.getValue(Label.toLocale("zh")));
			}

			{
				Label descLabel = chartPlugin.getDescLabel();
				Assert.assertEquals("饼图描述", descLabel.getValue());
				Assert.assertEquals("pie chart desc", descLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("饼图描述中文", descLabel.getValue(Label.toLocale("zh")));
			}

			{
				Label manualLabel = chartPlugin.getManualLabel();
				Assert.assertEquals("饼图指南", manualLabel.getValue());
				Assert.assertEquals("pie chart manual", manualLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("饼图指南中文", manualLabel.getValue(Label.toLocale("zh")));
			}

			{
				@SuppressWarnings({ "unchecked", "rawtypes" })
				Map<String, LocationIcon> icons = (Map) chartPlugin.getIcons();

				Assert.assertEquals("icon-0.png", icons.get("LIGHT").getLocation());
				Assert.assertEquals("icon-1.png", icons.get("DARK").getLocation());
			}

			List<ChartParam> chartParams = chartPlugin.getChartParams();

			{
				ChartParam chartParam = chartParams.get(0);

				Assert.assertEquals("title", chartParam.getName());
				Assert.assertEquals(ChartParam.DataType.STRING, chartParam.getType());

				Label nameLabel = chartParam.getNameLabel();
				Assert.assertEquals("标题", nameLabel.getValue());
				Assert.assertEquals("title", nameLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("标题中文", nameLabel.getValue(Label.toLocale("zh")));

				Label descLabel = chartParam.getDescLabel();
				Assert.assertEquals("标题描述", descLabel.getValue());
				Assert.assertEquals("title desc", descLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("标题描述中文", descLabel.getValue(Label.toLocale("zh")));
			}

			{
				ChartParam chartParam = chartParams.get(1);

				Assert.assertEquals("interval", chartParam.getName());
				Assert.assertEquals(ChartParam.DataType.NUMBER, chartParam.getType());

				Label nameLabel = chartParam.getNameLabel();
				Assert.assertEquals("间隔", nameLabel.getValue());
				Assert.assertEquals("interval", nameLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("间隔中文", nameLabel.getValue(Label.toLocale("zh")));

				Label descLabel = chartParam.getDescLabel();
				Assert.assertEquals("间隔描述", descLabel.getValue());
				Assert.assertEquals("interval desc", descLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("间隔描述中文", descLabel.getValue(Label.toLocale("zh")));
			}

			List<DataSign> dataSigns = chartPlugin.getDataSigns();

			{
				DataSign dataSign = dataSigns.get(0);

				Assert.assertEquals("x-value", dataSign.getName());
				Assert.assertFalse(dataSign.isRequired());
				Assert.assertFalse(dataSign.isMultiple());

				Label nameLabel = dataSign.getNameLabel();
				Assert.assertEquals("X值", nameLabel.getValue());
				Assert.assertEquals("X value", nameLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("X值中文", nameLabel.getValue(Label.toLocale("zh")));

				Label descLabel = dataSign.getDescLabel();
				Assert.assertEquals("X值描述", descLabel.getValue());
				Assert.assertEquals("X value desc", descLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("X值描述中文", descLabel.getValue(Label.toLocale("zh")));
			}

			{
				DataSign dataSign = dataSigns.get(1);

				Assert.assertEquals("y-value", dataSign.getName());
				Assert.assertTrue(dataSign.isRequired());
				Assert.assertTrue(dataSign.isMultiple());

				Label nameLabel = dataSign.getNameLabel();
				Assert.assertEquals("Y值", nameLabel.getValue());
				Assert.assertEquals("Y value", nameLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("Y值中文", nameLabel.getValue(Label.toLocale("zh")));

				Label descLabel = dataSign.getDescLabel();
				Assert.assertEquals("Y值描述", descLabel.getValue());
				Assert.assertEquals("Y value desc", descLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("Y值描述中文", descLabel.getValue(Label.toLocale("zh")));
			}

			{
				List<Category> categories = chartPlugin.getCategories();

				{
					Category category = categories.get(0);
					Assert.assertEquals("line", category.getName());
					Assert.assertEquals("nameLabel-line", category.getNameLabel().getValue());
					Assert.assertEquals("descLabel-line", category.getDescLabel().getValue());
					Assert.assertEquals(41, category.getOrder());
				}

				{
					Category category = categories.get(1);
					Assert.assertEquals("bar", category.getName());
					Assert.assertEquals("nameLabel-bar", category.getNameLabel().getValue());
					Assert.assertEquals("descLabel-bar", category.getDescLabel().getValue());
					Assert.assertEquals(51, category.getOrder());
				}
			}

			{
				List<Integer> categoryOrders = chartPlugin.getCategoryOrders();
				Assert.assertEquals(41, categoryOrders.get(0).intValue());
				Assert.assertEquals(51, categoryOrders.get(1).intValue());
			}
		}
	}

	@Test
	public void resolveChartPluginPropertiesTest_string_categories() throws IOException
	{
		{
			InputStream jsonInputStream = getClass().getClassLoader()
					.getResourceAsStream(
							"org/datagear/analysis/support/JsonChartPluginPropertiesResolverTest-string-categories.json");

			TestChartPlugin chartPlugin = new TestChartPlugin();
			jsonChartPluginPropertiesResolver.resolveChartPluginProperties(chartPlugin, jsonInputStream, "UTF-8");

			Assert.assertEquals("pie-chart", chartPlugin.getId());
			Assert.assertEquals(2, chartPlugin.getCategories().size());
			Assert.assertEquals(1, chartPlugin.getCategoryOrders().size());

			{
				List<Category> categories = chartPlugin.getCategories();

				{
					Category category = categories.get(0);
					Assert.assertEquals("line", category.getName());
					Assert.assertNull(category.getNameLabel());
				}

				{
					Category category = categories.get(1);
					Assert.assertEquals("bar", category.getName());
					Assert.assertNull(category.getNameLabel());
				}
			}

			{
				List<Integer> categoryOrders = chartPlugin.getCategoryOrders();
				Assert.assertEquals(41, categoryOrders.get(0).intValue());
			}
		}
	}

	@Test
	public void resolveChartPluginPropertiesTest_3_0_1() throws IOException
	{
		{
			InputStream jsonInputStream = getClass().getClassLoader()
					.getResourceAsStream(
							"org/datagear/analysis/support/JsonChartPluginPropertiesResolverTest-3.0.1.json");

			TestChartPlugin chartPlugin = new TestChartPlugin();
			jsonChartPluginPropertiesResolver.resolveChartPluginProperties(chartPlugin, jsonInputStream, "UTF-8");

			Assert.assertEquals("pie-chart", chartPlugin.getId());
			Assert.assertNotNull(chartPlugin.getNameLabel());
			Assert.assertNotNull(chartPlugin.getDescLabel());
			Assert.assertNotNull(chartPlugin.getManualLabel());
			Assert.assertNotNull(chartPlugin.getIcons());
			Assert.assertNotNull(chartPlugin.getChartParams());
			Assert.assertNotNull(chartPlugin.getDataSigns());
			Assert.assertEquals("0.1.0", chartPlugin.getVersion());
			Assert.assertEquals(2, chartPlugin.getOrder());
			Assert.assertNotNull(chartPlugin.getCategories());
			Assert.assertEquals(1, chartPlugin.getCategories().size());

			{
				Label nameLabel = chartPlugin.getNameLabel();
				Assert.assertEquals("饼图", nameLabel.getValue());
				Assert.assertEquals("pie chart", nameLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("饼图中文", nameLabel.getValue(Label.toLocale("zh")));
			}

			{
				Label descLabel = chartPlugin.getDescLabel();
				Assert.assertEquals("饼图描述", descLabel.getValue());
				Assert.assertEquals("pie chart desc", descLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("饼图描述中文", descLabel.getValue(Label.toLocale("zh")));
			}

			{
				Label manualLabel = chartPlugin.getManualLabel();
				Assert.assertEquals("饼图指南", manualLabel.getValue());
				Assert.assertEquals("pie chart manual", manualLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("饼图指南中文", manualLabel.getValue(Label.toLocale("zh")));
			}

			{
				@SuppressWarnings({ "unchecked", "rawtypes" })
				Map<String, LocationIcon> icons = (Map) chartPlugin.getIcons();

				Assert.assertEquals("icon-0.png", icons.get("LIGHT").getLocation());
				Assert.assertEquals("icon-1.png", icons.get("DARK").getLocation());
			}

			List<ChartParam> chartParams = chartPlugin.getChartParams();

			{
				ChartParam chartParam = chartParams.get(0);

				Assert.assertEquals("title", chartParam.getName());
				Assert.assertEquals(ChartParam.DataType.STRING, chartParam.getType());

				Label nameLabel = chartParam.getNameLabel();
				Assert.assertEquals("标题", nameLabel.getValue());
				Assert.assertEquals("title", nameLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("标题中文", nameLabel.getValue(Label.toLocale("zh")));

				Label descLabel = chartParam.getDescLabel();
				Assert.assertEquals("标题描述", descLabel.getValue());
				Assert.assertEquals("title desc", descLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("标题描述中文", descLabel.getValue(Label.toLocale("zh")));
			}

			{
				ChartParam chartParam = chartParams.get(1);

				Assert.assertEquals("interval", chartParam.getName());
				Assert.assertEquals(ChartParam.DataType.NUMBER, chartParam.getType());

				Label nameLabel = chartParam.getNameLabel();
				Assert.assertEquals("间隔", nameLabel.getValue());
				Assert.assertEquals("interval", nameLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("间隔中文", nameLabel.getValue(Label.toLocale("zh")));

				Label descLabel = chartParam.getDescLabel();
				Assert.assertEquals("间隔描述", descLabel.getValue());
				Assert.assertEquals("interval desc", descLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("间隔描述中文", descLabel.getValue(Label.toLocale("zh")));
			}

			List<DataSign> dataSigns = chartPlugin.getDataSigns();

			{
				DataSign dataSign = dataSigns.get(0);

				Assert.assertEquals("x-value", dataSign.getName());
				Assert.assertFalse(dataSign.isRequired());
				Assert.assertFalse(dataSign.isMultiple());

				Label nameLabel = dataSign.getNameLabel();
				Assert.assertEquals("X值", nameLabel.getValue());
				Assert.assertEquals("X value", nameLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("X值中文", nameLabel.getValue(Label.toLocale("zh")));

				Label descLabel = dataSign.getDescLabel();
				Assert.assertEquals("X值描述", descLabel.getValue());
				Assert.assertEquals("X value desc", descLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("X值描述中文", descLabel.getValue(Label.toLocale("zh")));
			}

			{
				DataSign dataSign = dataSigns.get(1);

				Assert.assertEquals("y-value", dataSign.getName());
				Assert.assertTrue(dataSign.isRequired());
				Assert.assertTrue(dataSign.isMultiple());

				Label nameLabel = dataSign.getNameLabel();
				Assert.assertEquals("Y值", nameLabel.getValue());
				Assert.assertEquals("Y value", nameLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("Y值中文", nameLabel.getValue(Label.toLocale("zh")));

				Label descLabel = dataSign.getDescLabel();
				Assert.assertEquals("Y值描述", descLabel.getValue());
				Assert.assertEquals("Y value desc", descLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("Y值描述中文", descLabel.getValue(Label.toLocale("zh")));
			}

			{
				List<Category> categories = chartPlugin.getCategories();

				{
					Category category = categories.get(0);
					Assert.assertEquals("line", category.getName());
					Assert.assertEquals("nameLabel", category.getNameLabel().getValue());
					Assert.assertEquals("descLabel", category.getDescLabel().getValue());
					Assert.assertEquals(41, category.getOrder());
				}
			}
		}
	}

	private static class TestChartPlugin extends AbstractChartPlugin
	{
		private static final long serialVersionUID = 1L;

		public TestChartPlugin()
		{
			super();
		}

		@Override
		public Chart renderChart(RenderContext renderContext, ChartDefinition chartDefinition) throws RenderException
		{
			throw new UnsupportedOperationException();
		}
	}
}
