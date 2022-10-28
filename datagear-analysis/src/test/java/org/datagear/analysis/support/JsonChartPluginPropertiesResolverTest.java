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
import java.util.Locale;
import java.util.Map;

import org.datagear.analysis.Category;
import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartPluginAttribute;
import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.DataSign;
import org.datagear.analysis.Group;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.util.StringUtil;
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

	@SuppressWarnings("unchecked")
	@Test
	public void resolveChartPluginPropertiesTest() throws IOException
	{
		Locale enLocale = new Locale("en");
		Locale zhLocale = new Locale("zh");
		
		{
			InputStream jsonInputStream = getClass().getClassLoader()
					.getResourceAsStream("org/datagear/analysis/support/JsonChartPluginPropertiesResolverTest.json");

			TestChartPlugin chartPlugin = new TestChartPlugin();
			jsonChartPluginPropertiesResolver.resolveChartPluginProperties(chartPlugin, jsonInputStream, "UTF-8");

			Assert.assertEquals("pie-chart", chartPlugin.getId());
			Assert.assertNotNull(chartPlugin.getNameLabel());
			Assert.assertNotNull(chartPlugin.getDescLabel());
			Assert.assertNotNull(chartPlugin.getIconResourceNames().size() > 0);
			Assert.assertNotNull(chartPlugin.getAttributes());
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
				Assert.assertEquals("pie chart", nameLabel.getValue(enLocale));
				Assert.assertEquals("饼图中文", nameLabel.getValue(zhLocale));
			}

			{
				Label descLabel = chartPlugin.getDescLabel();
				Assert.assertEquals("饼图描述", descLabel.getValue());
				Assert.assertEquals("pie chart desc", descLabel.getValue(enLocale));
				Assert.assertEquals("饼图描述中文", descLabel.getValue(zhLocale));
			}

			{
				Map<String, String> icons = chartPlugin.getIconResourceNames();

				Assert.assertEquals("icon-0.png", icons.get("LIGHT"));
				Assert.assertEquals("icon-1.png", icons.get("DARK"));
			}

			List<ChartPluginAttribute> chartPluginAttributes = chartPlugin.getAttributes();

			{
				ChartPluginAttribute chartPluginAttribute = chartPluginAttributes.get(0);

				Assert.assertEquals("title", chartPluginAttribute.getName());
				Assert.assertEquals(ChartPluginAttribute.DataType.STRING, chartPluginAttribute.getType());

				Label nameLabel = chartPluginAttribute.getNameLabel();
				Assert.assertEquals("标题", nameLabel.getValue());
				Assert.assertEquals("title", nameLabel.getValue(enLocale));
				Assert.assertEquals("标题中文", nameLabel.getValue(zhLocale));

				Label descLabel = chartPluginAttribute.getDescLabel();
				Assert.assertEquals("标题描述", descLabel.getValue());
				Assert.assertEquals("title desc", descLabel.getValue(enLocale));
				Assert.assertEquals("标题描述中文", descLabel.getValue(zhLocale));
				
				Assert.assertFalse(chartPluginAttribute.isRequired());
				Assert.assertFalse(chartPluginAttribute.isMultiple());
				Assert.assertTrue(StringUtil.isEmpty(chartPluginAttribute.getInputType()));
				Assert.assertTrue(StringUtil.isEmpty(chartPluginAttribute.getInputPayload()));

				Group group = chartPluginAttribute.getGroup();
				Label groupNameLabel = group.getNameLabel();
				Label groupDescLabel = group.getDescLabel();

				Assert.assertEquals("group-0", group.getName());
				Assert.assertEquals(99, group.getOrder());

				Assert.assertEquals("分组-0", groupNameLabel.getValue());
				Assert.assertEquals("group-0", groupNameLabel.getValue(enLocale));
				Assert.assertEquals("分组-0-中文", groupNameLabel.getValue(zhLocale));

				Assert.assertEquals("分组-0-描述", groupDescLabel.getValue());
				Assert.assertEquals("group-0 desc", groupDescLabel.getValue(enLocale));
				Assert.assertEquals("分组-0-描述-中文", groupDescLabel.getValue(zhLocale));
				
				Map<String, ?> additions = chartPluginAttribute.getAdditions();
				Assert.assertNotNull(additions);
				Assert.assertEquals("that", additions.get("for"));
				Assert.assertEquals("title.text", additions.get("optionPath"));
				Assert.assertEquals(3, ((Number)additions.get("priority")).intValue());
			}

			{
				ChartPluginAttribute chartPluginAttribute = chartPluginAttributes.get(1);

				Assert.assertEquals("interval", chartPluginAttribute.getName());
				Assert.assertEquals(ChartPluginAttribute.DataType.NUMBER, chartPluginAttribute.getType());

				Label nameLabel = chartPluginAttribute.getNameLabel();
				Assert.assertEquals("间隔", nameLabel.getValue());
				Assert.assertEquals("interval", nameLabel.getValue(enLocale));
				Assert.assertEquals("间隔中文", nameLabel.getValue(zhLocale));

				Label descLabel = chartPluginAttribute.getDescLabel();
				Assert.assertEquals("间隔描述", descLabel.getValue());
				Assert.assertEquals("interval desc", descLabel.getValue(enLocale));
				Assert.assertEquals("间隔描述中文", descLabel.getValue(zhLocale));
				
				Assert.assertTrue(chartPluginAttribute.isRequired());
				Assert.assertTrue(chartPluginAttribute.isMultiple());
				Assert.assertEquals("mytype", chartPluginAttribute.getInputType());
				Assert.assertEquals("mypayload", chartPluginAttribute.getInputPayload());

				Group group = chartPluginAttribute.getGroup();
				Label groupNameLabel = group.getNameLabel();
				Label groupDescLabel = group.getDescLabel();

				Assert.assertEquals("group-1", group.getName());
				Assert.assertEquals(0, group.getOrder());
				Assert.assertNull(groupNameLabel);
				Assert.assertNull(groupDescLabel);
				
				Map<String, ?> additions = chartPluginAttribute.getAdditions();
				Assert.assertNull(additions);
			}
			
			{
				ChartPluginAttribute a2 = chartPluginAttributes.get(2);
				ChartPluginAttribute a3 = chartPluginAttributes.get(3);
				ChartPluginAttribute a4 = chartPluginAttributes.get(4);
				ChartPluginAttribute a5 = chartPluginAttributes.get(5);
				ChartPluginAttribute a6 = chartPluginAttributes.get(6);

				Assert.assertEquals("a2", a2.getName());
				Assert.assertEquals(ChartPluginAttribute.DataType.BOOLEAN, a2.getType());
				Assert.assertNull(a2.getGroup());
				{
					List<?> inputPayload = (List<?>)a2.getInputPayload();
					Assert.assertEquals(2, inputPayload.size());
					Map<String, ?> inputPayload0 = (Map<String, ?>)inputPayload.get(0);
					Map<String, ?> inputPayload1 = (Map<String, ?>)inputPayload.get(1);
					Assert.assertEquals("a", inputPayload0.get("name"));
					Assert.assertEquals(2, ((Number)inputPayload0.get("value")).intValue());
					Assert.assertEquals("b", inputPayload1.get("name"));
					Assert.assertEquals(3, ((Number)inputPayload1.get("value")).intValue());
				}

				Assert.assertEquals("a3", a3.getName());
				Assert.assertEquals(ChartPluginAttribute.DataType.STRING, a3.getType());
				Assert.assertNull(a3.getGroup());
				{
					List<?> inputPayload = (List<?>)a3.getInputPayload();
					Assert.assertEquals(2, inputPayload.size());
					Assert.assertEquals("a", inputPayload.get(0));
					Assert.assertEquals("b", inputPayload.get(1));
				}

				Assert.assertEquals("a4", a4.getName());
				Assert.assertEquals(ChartPluginAttribute.DataType.NUMBER, a4.getType());
				Assert.assertNull(a4.getGroup());
				{
					List<?> inputPayload = (List<?>)a4.getInputPayload();
					Assert.assertEquals(2, inputPayload.size());
					Assert.assertEquals(2, ((Number)inputPayload.get(0)).intValue());
					Assert.assertEquals(3, ((Number)inputPayload.get(1)).intValue());
				}

				Assert.assertEquals("a5", a5.getName());
				Assert.assertEquals(ChartPluginAttribute.DataType.BOOLEAN, a5.getType());
				Assert.assertNull(a5.getInputPayload());
				Assert.assertNull(a5.getGroup());

				Assert.assertEquals("a6", a6.getName());
				Assert.assertEquals("custom", a6.getType());
				Assert.assertNull(a6.getInputPayload());
				Assert.assertNull(a6.getGroup());
			}

			List<DataSign> dataSigns = chartPlugin.getDataSigns();

			{
				DataSign dataSign = dataSigns.get(0);

				Assert.assertEquals("x-value", dataSign.getName());
				Assert.assertFalse(dataSign.isRequired());
				Assert.assertFalse(dataSign.isMultiple());

				Label nameLabel = dataSign.getNameLabel();
				Assert.assertEquals("X值", nameLabel.getValue());
				Assert.assertEquals("X value", nameLabel.getValue(enLocale));
				Assert.assertEquals("X值中文", nameLabel.getValue(zhLocale));

				Label descLabel = dataSign.getDescLabel();
				Assert.assertEquals("X值描述", descLabel.getValue());
				Assert.assertEquals("X value desc", descLabel.getValue(enLocale));
				Assert.assertEquals("X值描述中文", descLabel.getValue(zhLocale));
			}

			{
				DataSign dataSign = dataSigns.get(1);

				Assert.assertEquals("y-value", dataSign.getName());
				Assert.assertTrue(dataSign.isRequired());
				Assert.assertTrue(dataSign.isMultiple());

				Label nameLabel = dataSign.getNameLabel();
				Assert.assertEquals("Y值", nameLabel.getValue());
				Assert.assertEquals("Y value", nameLabel.getValue(enLocale));
				Assert.assertEquals("Y值中文", nameLabel.getValue(zhLocale));

				Label descLabel = dataSign.getDescLabel();
				Assert.assertEquals("Y值描述", descLabel.getValue());
				Assert.assertEquals("Y value desc", descLabel.getValue(enLocale));
				Assert.assertEquals("Y值描述中文", descLabel.getValue(zhLocale));
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
		Locale enLocale = new Locale("en");
		Locale zhLocale = new Locale("zh");
		
		{
			InputStream jsonInputStream = getClass().getClassLoader()
					.getResourceAsStream(
							"org/datagear/analysis/support/JsonChartPluginPropertiesResolverTest-3.0.1.json");

			TestChartPlugin chartPlugin = new TestChartPlugin();
			jsonChartPluginPropertiesResolver.resolveChartPluginProperties(chartPlugin, jsonInputStream, "UTF-8");

			Assert.assertEquals("pie-chart", chartPlugin.getId());
			Assert.assertNotNull(chartPlugin.getNameLabel());
			Assert.assertNotNull(chartPlugin.getDescLabel());
			Assert.assertTrue(chartPlugin.getIconResourceNames().size() > 0);
			Assert.assertNotNull(chartPlugin.getAttributes());
			Assert.assertNotNull(chartPlugin.getDataSigns());
			Assert.assertEquals("0.1.0", chartPlugin.getVersion());
			Assert.assertEquals(2, chartPlugin.getOrder());
			Assert.assertNotNull(chartPlugin.getCategories());
			Assert.assertEquals(1, chartPlugin.getCategories().size());

			{
				Label nameLabel = chartPlugin.getNameLabel();
				Assert.assertEquals("饼图", nameLabel.getValue());
				Assert.assertEquals("pie chart", nameLabel.getValue(enLocale));
				Assert.assertEquals("饼图中文", nameLabel.getValue(zhLocale));
			}

			{
				Label descLabel = chartPlugin.getDescLabel();
				Assert.assertEquals("饼图描述", descLabel.getValue());
				Assert.assertEquals("pie chart desc", descLabel.getValue(enLocale));
				Assert.assertEquals("饼图描述中文", descLabel.getValue(zhLocale));
			}

			{
				Map<String, String> icons = chartPlugin.getIconResourceNames();

				Assert.assertEquals("icon-0.png", icons.get("LIGHT"));
				Assert.assertEquals("icon-1.png", icons.get("DARK"));
			}

			List<ChartPluginAttribute> chartPluginAttributes = chartPlugin.getAttributes();

			{
				ChartPluginAttribute chartPluginAttribute = chartPluginAttributes.get(0);

				Assert.assertEquals("title", chartPluginAttribute.getName());
				Assert.assertEquals(ChartPluginAttribute.DataType.STRING, chartPluginAttribute.getType());

				Label nameLabel = chartPluginAttribute.getNameLabel();
				Assert.assertEquals("标题", nameLabel.getValue());
				Assert.assertEquals("title", nameLabel.getValue(enLocale));
				Assert.assertEquals("标题中文", nameLabel.getValue(zhLocale));

				Label descLabel = chartPluginAttribute.getDescLabel();
				Assert.assertEquals("标题描述", descLabel.getValue());
				Assert.assertEquals("title desc", descLabel.getValue(enLocale));
				Assert.assertEquals("标题描述中文", descLabel.getValue(zhLocale));
			}

			{
				ChartPluginAttribute chartPluginAttribute = chartPluginAttributes.get(1);

				Assert.assertEquals("interval", chartPluginAttribute.getName());
				Assert.assertEquals(ChartPluginAttribute.DataType.NUMBER, chartPluginAttribute.getType());

				Label nameLabel = chartPluginAttribute.getNameLabel();
				Assert.assertEquals("间隔", nameLabel.getValue());
				Assert.assertEquals("interval", nameLabel.getValue(enLocale));
				Assert.assertEquals("间隔中文", nameLabel.getValue(zhLocale));

				Label descLabel = chartPluginAttribute.getDescLabel();
				Assert.assertEquals("间隔描述", descLabel.getValue());
				Assert.assertEquals("interval desc", descLabel.getValue(enLocale));
				Assert.assertEquals("间隔描述中文", descLabel.getValue(zhLocale));
			}

			List<DataSign> dataSigns = chartPlugin.getDataSigns();

			{
				DataSign dataSign = dataSigns.get(0);

				Assert.assertEquals("x-value", dataSign.getName());
				Assert.assertFalse(dataSign.isRequired());
				Assert.assertFalse(dataSign.isMultiple());

				Label nameLabel = dataSign.getNameLabel();
				Assert.assertEquals("X值", nameLabel.getValue());
				Assert.assertEquals("X value", nameLabel.getValue(enLocale));
				Assert.assertEquals("X值中文", nameLabel.getValue(zhLocale));

				Label descLabel = dataSign.getDescLabel();
				Assert.assertEquals("X值描述", descLabel.getValue());
				Assert.assertEquals("X value desc", descLabel.getValue(enLocale));
				Assert.assertEquals("X值描述中文", descLabel.getValue(zhLocale));
			}

			{
				DataSign dataSign = dataSigns.get(1);

				Assert.assertEquals("y-value", dataSign.getName());
				Assert.assertTrue(dataSign.isRequired());
				Assert.assertTrue(dataSign.isMultiple());

				Label nameLabel = dataSign.getNameLabel();
				Assert.assertEquals("Y值", nameLabel.getValue());
				Assert.assertEquals("Y value", nameLabel.getValue(enLocale));
				Assert.assertEquals("Y值中文", nameLabel.getValue(zhLocale));

				Label descLabel = dataSign.getDescLabel();
				Assert.assertEquals("Y值描述", descLabel.getValue());
				Assert.assertEquals("Y value desc", descLabel.getValue(enLocale));
				Assert.assertEquals("Y值描述中文", descLabel.getValue(zhLocale));
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
