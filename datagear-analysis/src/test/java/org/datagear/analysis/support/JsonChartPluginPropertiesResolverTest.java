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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.datagear.analysis.Category;
import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.ChartPluginAttribute;
import org.datagear.analysis.ChartPluginDataSetRange;
import org.datagear.analysis.ChartPluginDataSetRange.Range;
import org.datagear.analysis.DataSign;
import org.datagear.analysis.Group;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.util.i18n.Label;
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
			jsonChartPluginPropertiesResolver.resolveChartPluginProperties(chartPlugin, jsonInputStream,
					IOUtil.CHARSET_UTF_8);

			assertEquals("pie-chart", chartPlugin.getId());
			assertNotNull(chartPlugin.getNameLabel());
			assertNotNull(chartPlugin.getDescLabel());
			assertNotNull(chartPlugin.getIconResourceNames().size() > 0);
			assertNotNull(chartPlugin.getAttributes());
			assertNotNull(chartPlugin.getDataSigns());
			assertEquals("0.1.0", chartPlugin.getVersion());
			assertEquals(2, chartPlugin.getOrder());
			assertNotNull(chartPlugin.getCategories());
			assertEquals(2, chartPlugin.getCategories().size());
			assertNotNull(chartPlugin.getCategoryOrders());
			assertEquals(2, chartPlugin.getCategoryOrders().size());

			{
				Label nameLabel = chartPlugin.getNameLabel();
				assertEquals("饼图", nameLabel.getValue());
				assertEquals("pie chart", nameLabel.getValue(enLocale));
				assertEquals("饼图中文", nameLabel.getValue(zhLocale));
			}

			{
				Label descLabel = chartPlugin.getDescLabel();
				assertEquals("饼图描述", descLabel.getValue());
				assertEquals("pie chart desc", descLabel.getValue(enLocale));
				assertEquals("饼图描述中文", descLabel.getValue(zhLocale));
			}

			{
				Map<String, String> icons = chartPlugin.getIconResourceNames();

				assertEquals("icon-0.png", icons.get("LIGHT"));
				assertEquals("icon-1.png", icons.get("DARK"));
			}

			List<DataSign> dataSigns = chartPlugin.getDataSigns();
			assertEquals(3, dataSigns.size());

			{
				DataSign dataSign = dataSigns.get(0);

				assertEquals("x-value", dataSign.getName());
				assertFalse(dataSign.isRequired());
				assertFalse(dataSign.isMultiple());

				Label nameLabel = dataSign.getNameLabel();
				assertEquals("X值", nameLabel.getValue());
				assertEquals("X value", nameLabel.getValue(enLocale));
				assertEquals("X值中文", nameLabel.getValue(zhLocale));

				Label descLabel = dataSign.getDescLabel();
				assertEquals("X值描述", descLabel.getValue());
				assertEquals("X value desc", descLabel.getValue(enLocale));
				assertEquals("X值描述中文", descLabel.getValue(zhLocale));
			}

			{
				DataSign dataSign = dataSigns.get(1);

				assertEquals("y-value", dataSign.getName());
				assertTrue(dataSign.isRequired());
				assertTrue(dataSign.isMultiple());

				Label nameLabel = dataSign.getNameLabel();
				assertEquals("Y值", nameLabel.getValue());
				assertEquals("Y value", nameLabel.getValue(enLocale));
				assertEquals("Y值中文", nameLabel.getValue(zhLocale));

				Label descLabel = dataSign.getDescLabel();
				assertEquals("Y值描述", descLabel.getValue());
				assertEquals("Y value desc", descLabel.getValue(enLocale));
				assertEquals("Y值描述中文", descLabel.getValue(zhLocale));
			}

			{
				DataSign dataSign = dataSigns.get(2);

				assertEquals("simple-value", dataSign.getName());
				assertTrue(dataSign.isRequired());
				assertFalse(dataSign.isMultiple());
				assertNull(dataSign.getNameLabel());
				assertNull(dataSign.getDescLabel());
			}

			{
				ChartPluginDataSetRange dataSetRange = chartPlugin.getDataSetRange();
				assertEquals(1, dataSetRange.getMain().getMin().intValue());
				assertNull(dataSetRange.getMain().getMax());
				assertNull(dataSetRange.getAttachment());
			}

			List<ChartPluginAttribute> chartPluginAttributes = chartPlugin.getAttributes();

			{
				ChartPluginAttribute chartPluginAttribute = chartPluginAttributes.get(0);

				assertEquals("title", chartPluginAttribute.getName());
				assertEquals(ChartPluginAttribute.DataType.STRING, chartPluginAttribute.getType());

				Label nameLabel = chartPluginAttribute.getNameLabel();
				assertEquals("标题", nameLabel.getValue());
				assertEquals("title", nameLabel.getValue(enLocale));
				assertEquals("标题中文", nameLabel.getValue(zhLocale));

				Label descLabel = chartPluginAttribute.getDescLabel();
				assertEquals("标题描述", descLabel.getValue());
				assertEquals("title desc", descLabel.getValue(enLocale));
				assertEquals("标题描述中文", descLabel.getValue(zhLocale));
				
				assertFalse(chartPluginAttribute.isRequired());
				assertTrue(StringUtil.isEmpty(chartPluginAttribute.getInputType()));
				assertTrue(StringUtil.isEmpty(chartPluginAttribute.getInputPayload()));

				Group group = chartPluginAttribute.getGroup();
				Label groupNameLabel = group.getNameLabel();
				Label groupDescLabel = group.getDescLabel();

				assertEquals("group-0", group.getName());
				assertEquals(99, group.getOrder());

				assertEquals("分组-0", groupNameLabel.getValue());
				assertEquals("group-0", groupNameLabel.getValue(enLocale));
				assertEquals("分组-0-中文", groupNameLabel.getValue(zhLocale));

				assertEquals("分组-0-描述", groupDescLabel.getValue());
				assertEquals("group-0 desc", groupDescLabel.getValue(enLocale));
				assertEquals("分组-0-描述-中文", groupDescLabel.getValue(zhLocale));
				
				Map<String, ?> additions = chartPluginAttribute.getAdditions();
				assertNotNull(additions);
				assertEquals("that", additions.get("for"));
				assertEquals("title.text", additions.get("optionPath"));
				assertEquals(3, ((Number) additions.get("priority")).intValue());
			}

			{
				ChartPluginAttribute chartPluginAttribute = chartPluginAttributes.get(1);

				assertEquals("interval", chartPluginAttribute.getName());
				assertEquals(ChartPluginAttribute.DataType.NUMBER, chartPluginAttribute.getType());

				Label nameLabel = chartPluginAttribute.getNameLabel();
				assertEquals("间隔", nameLabel.getValue());
				assertEquals("interval", nameLabel.getValue(enLocale));
				assertEquals("间隔中文", nameLabel.getValue(zhLocale));

				Label descLabel = chartPluginAttribute.getDescLabel();
				assertEquals("间隔描述", descLabel.getValue());
				assertEquals("interval desc", descLabel.getValue(enLocale));
				assertEquals("间隔描述中文", descLabel.getValue(zhLocale));
				
				assertTrue(chartPluginAttribute.isRequired());
				assertEquals("mytype", chartPluginAttribute.getInputType());
				assertEquals("mypayload", chartPluginAttribute.getInputPayload());

				Group group = chartPluginAttribute.getGroup();
				Label groupNameLabel = group.getNameLabel();
				Label groupDescLabel = group.getDescLabel();

				assertEquals("group-1", group.getName());
				assertEquals(0, group.getOrder());
				assertNull(groupNameLabel);
				assertNull(groupDescLabel);
				
				Map<String, ?> additions = chartPluginAttribute.getAdditions();
				assertNull(additions);
			}
			
			{
				ChartPluginAttribute a2 = chartPluginAttributes.get(2);
				ChartPluginAttribute a3 = chartPluginAttributes.get(3);
				ChartPluginAttribute a4 = chartPluginAttributes.get(4);
				ChartPluginAttribute a5 = chartPluginAttributes.get(5);
				ChartPluginAttribute a6 = chartPluginAttributes.get(6);
				ChartPluginAttribute a7 = chartPluginAttributes.get(7);

				assertEquals("a2", a2.getName());
				assertEquals(ChartPluginAttribute.DataType.BOOLEAN, a2.getType());
				assertNull(a2.getGroup());
				{
					List<?> inputPayload = (List<?>)a2.getInputPayload();
					assertEquals(2, inputPayload.size());
					Map<String, ?> inputPayload0 = (Map<String, ?>)inputPayload.get(0);
					Map<String, ?> inputPayload1 = (Map<String, ?>)inputPayload.get(1);
					assertEquals("a", inputPayload0.get("name"));
					assertEquals(2, ((Number) inputPayload0.get("value")).intValue());
					assertEquals("b", inputPayload1.get("name"));
					assertEquals(3, ((Number) inputPayload1.get("value")).intValue());
				}

				assertEquals("a3", a3.getName());
				assertEquals(ChartPluginAttribute.DataType.STRING, a3.getType());
				assertNull(a3.getGroup());
				{
					List<?> inputPayload = (List<?>)a3.getInputPayload();
					assertEquals(2, inputPayload.size());
					assertEquals("a", inputPayload.get(0));
					assertEquals("b", inputPayload.get(1));
				}

				assertEquals("a4", a4.getName());
				assertEquals(ChartPluginAttribute.DataType.NUMBER, a4.getType());
				assertNull(a4.getGroup());
				{
					List<?> inputPayload = (List<?>)a4.getInputPayload();
					assertEquals(2, inputPayload.size());
					assertEquals(2, ((Number) inputPayload.get(0)).intValue());
					assertEquals(3, ((Number) inputPayload.get(1)).intValue());
				}

				assertEquals("a5", a5.getName());
				assertEquals(ChartPluginAttribute.DataType.BOOLEAN, a5.getType());
				assertNull(a5.getInputPayload());
				assertNull(a5.getGroup());

				assertEquals("a6", a6.getName());
				assertEquals("custom", a6.getType());
				assertNull(a6.getInputPayload());
				assertNull(a6.getGroup());

				assertEquals("a7", a7.getName());
				assertEquals(ChartPluginAttribute.DataType.STRING, a7.getType());
				assertNull(a7.getInputPayload());
				assertNull(a7.getGroup());
			}

			{
				List<Category> categories = chartPlugin.getCategories();

				{
					Category category = categories.get(0);
					assertEquals("line", category.getName());
					assertEquals("nameLabel-line", category.getNameLabel().getValue());
					assertEquals("descLabel-line", category.getDescLabel().getValue());
					assertEquals(41, category.getOrder());
				}

				{
					Category category = categories.get(1);
					assertEquals("bar", category.getName());
					assertEquals("nameLabel-bar", category.getNameLabel().getValue());
					assertEquals("descLabel-bar", category.getDescLabel().getValue());
					assertEquals(51, category.getOrder());
				}
			}

			{
				List<Integer> categoryOrders = chartPlugin.getCategoryOrders();
				assertEquals(41, categoryOrders.get(0).intValue());
				assertEquals(51, categoryOrders.get(1).intValue());
			}
		}
	}

	@Test
	public void resolveChartPluginPropertiesTest_author_issueDate() throws IOException
	{
		InputStream jsonInputStream = getClass().getClassLoader().getResourceAsStream(
				"org/datagear/analysis/support/JsonChartPluginPropertiesResolverTest-author-issueDate.json");

		TestChartPlugin chartPlugin = new TestChartPlugin();
		jsonChartPluginPropertiesResolver.resolveChartPluginProperties(chartPlugin, jsonInputStream,
				IOUtil.CHARSET_UTF_8);

		assertEquals("author-issueDate", chartPlugin.getId());
		assertEquals("test", chartPlugin.getAuthor());
		assertEquals("2024-09-01", chartPlugin.getIssueDate());
	}

	@Test
	public void resolveChartPluginPropertiesTest_platformVersion() throws IOException
	{
		InputStream jsonInputStream = getClass().getClassLoader().getResourceAsStream(
				"org/datagear/analysis/support/JsonChartPluginPropertiesResolverTest-platformVersion.json");

		TestChartPlugin chartPlugin = new TestChartPlugin();
		jsonChartPluginPropertiesResolver.resolveChartPluginProperties(chartPlugin, jsonInputStream,
				IOUtil.CHARSET_UTF_8);

		assertEquals("5.2.0", chartPlugin.getPlatformVersion());
	}

	@Test
	public void resolveChartPluginPropertiesTest_dataSetRange() throws IOException
	{
		{
			InputStream jsonInputStream = getClass().getClassLoader()
					.getResourceAsStream(
							"org/datagear/analysis/support/JsonChartPluginPropertiesResolverTest-dataSetRange-number.json");

			TestChartPlugin chartPlugin = new TestChartPlugin();
			jsonChartPluginPropertiesResolver.resolveChartPluginProperties(chartPlugin, jsonInputStream,
					IOUtil.CHARSET_UTF_8);

			assertEquals("dataset-range-number", chartPlugin.getId());

			ChartPluginDataSetRange dsr = chartPlugin.getDataSetRange();

			assertEquals(1, dsr.getMain().getMin().intValue());
			assertNull(dsr.getMain().getMax());

			assertNull(dsr.getAttachment());
		}
		
		{
			InputStream jsonInputStream = getClass().getClassLoader()
					.getResourceAsStream(
							"org/datagear/analysis/support/JsonChartPluginPropertiesResolverTest-dataSetRange.json");

			TestChartPlugin chartPlugin = new TestChartPlugin();
			jsonChartPluginPropertiesResolver.resolveChartPluginProperties(chartPlugin, jsonInputStream,
					IOUtil.CHARSET_UTF_8);

			assertEquals("pie-chart", chartPlugin.getId());

			ChartPluginDataSetRange dsr = chartPlugin.getDataSetRange();

			assertEquals(1, dsr.getMain().getMin().intValue());
			assertEquals(2, dsr.getMain().getMax().intValue());

			assertEquals(3, dsr.getAttachment().getMin().intValue());
			assertEquals(4, dsr.getAttachment().getMax().intValue());
		}
	}

	@Test
	public void resolveChartPluginPropertiesTest_string_categories() throws IOException
	{
		{
			InputStream jsonInputStream = getClass().getClassLoader().getResourceAsStream(
					"org/datagear/analysis/support/JsonChartPluginPropertiesResolverTest-string-categories.json");

			TestChartPlugin chartPlugin = new TestChartPlugin();
			jsonChartPluginPropertiesResolver.resolveChartPluginProperties(chartPlugin, jsonInputStream,
					IOUtil.CHARSET_UTF_8);

			assertEquals("pie-chart", chartPlugin.getId());
			assertEquals(2, chartPlugin.getCategories().size());
			assertEquals(1, chartPlugin.getCategoryOrders().size());

			{
				List<Category> categories = chartPlugin.getCategories();

				{
					Category category = categories.get(0);
					assertEquals("line", category.getName());
					assertNull(category.getNameLabel());
				}

				{
					Category category = categories.get(1);
					assertEquals("bar", category.getName());
					assertNull(category.getNameLabel());
				}
			}

			{
				List<Integer> categoryOrders = chartPlugin.getCategoryOrders();
				assertEquals(41, categoryOrders.get(0).intValue());
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
			jsonChartPluginPropertiesResolver.resolveChartPluginProperties(chartPlugin, jsonInputStream,
					IOUtil.CHARSET_UTF_8);

			assertEquals("pie-chart", chartPlugin.getId());
			assertNotNull(chartPlugin.getNameLabel());
			assertNotNull(chartPlugin.getDescLabel());
			assertTrue(chartPlugin.getIconResourceNames().size() > 0);
			assertNotNull(chartPlugin.getAttributes());
			assertNotNull(chartPlugin.getDataSigns());
			assertEquals("0.1.0", chartPlugin.getVersion());
			assertEquals(2, chartPlugin.getOrder());
			assertNotNull(chartPlugin.getCategories());
			assertEquals(1, chartPlugin.getCategories().size());

			{
				Label nameLabel = chartPlugin.getNameLabel();
				assertEquals("饼图", nameLabel.getValue());
				assertEquals("pie chart", nameLabel.getValue(enLocale));
				assertEquals("饼图中文", nameLabel.getValue(zhLocale));
			}

			{
				Label descLabel = chartPlugin.getDescLabel();
				assertEquals("饼图描述", descLabel.getValue());
				assertEquals("pie chart desc", descLabel.getValue(enLocale));
				assertEquals("饼图描述中文", descLabel.getValue(zhLocale));
			}

			{
				Map<String, String> icons = chartPlugin.getIconResourceNames();

				assertEquals("icon-0.png", icons.get("LIGHT"));
				assertEquals("icon-1.png", icons.get("DARK"));
			}

			List<ChartPluginAttribute> chartPluginAttributes = chartPlugin.getAttributes();

			{
				ChartPluginAttribute chartPluginAttribute = chartPluginAttributes.get(0);

				assertEquals("title", chartPluginAttribute.getName());
				assertEquals(ChartPluginAttribute.DataType.STRING, chartPluginAttribute.getType());

				Label nameLabel = chartPluginAttribute.getNameLabel();
				assertEquals("标题", nameLabel.getValue());
				assertEquals("title", nameLabel.getValue(enLocale));
				assertEquals("标题中文", nameLabel.getValue(zhLocale));

				Label descLabel = chartPluginAttribute.getDescLabel();
				assertEquals("标题描述", descLabel.getValue());
				assertEquals("title desc", descLabel.getValue(enLocale));
				assertEquals("标题描述中文", descLabel.getValue(zhLocale));
			}

			{
				ChartPluginAttribute chartPluginAttribute = chartPluginAttributes.get(1);

				assertEquals("interval", chartPluginAttribute.getName());
				assertEquals(ChartPluginAttribute.DataType.NUMBER, chartPluginAttribute.getType());

				Label nameLabel = chartPluginAttribute.getNameLabel();
				assertEquals("间隔", nameLabel.getValue());
				assertEquals("interval", nameLabel.getValue(enLocale));
				assertEquals("间隔中文", nameLabel.getValue(zhLocale));

				Label descLabel = chartPluginAttribute.getDescLabel();
				assertEquals("间隔描述", descLabel.getValue());
				assertEquals("interval desc", descLabel.getValue(enLocale));
				assertEquals("间隔描述中文", descLabel.getValue(zhLocale));
			}

			List<DataSign> dataSigns = chartPlugin.getDataSigns();

			{
				DataSign dataSign = dataSigns.get(0);

				assertEquals("x-value", dataSign.getName());
				assertFalse(dataSign.isRequired());
				assertFalse(dataSign.isMultiple());

				Label nameLabel = dataSign.getNameLabel();
				assertEquals("X值", nameLabel.getValue());
				assertEquals("X value", nameLabel.getValue(enLocale));
				assertEquals("X值中文", nameLabel.getValue(zhLocale));

				Label descLabel = dataSign.getDescLabel();
				assertEquals("X值描述", descLabel.getValue());
				assertEquals("X value desc", descLabel.getValue(enLocale));
				assertEquals("X值描述中文", descLabel.getValue(zhLocale));
			}

			{
				DataSign dataSign = dataSigns.get(1);

				assertEquals("y-value", dataSign.getName());
				assertTrue(dataSign.isRequired());
				assertTrue(dataSign.isMultiple());

				Label nameLabel = dataSign.getNameLabel();
				assertEquals("Y值", nameLabel.getValue());
				assertEquals("Y value", nameLabel.getValue(enLocale));
				assertEquals("Y值中文", nameLabel.getValue(zhLocale));

				Label descLabel = dataSign.getDescLabel();
				assertEquals("Y值描述", descLabel.getValue());
				assertEquals("Y value desc", descLabel.getValue(enLocale));
				assertEquals("Y值描述中文", descLabel.getValue(zhLocale));
			}

			{
				List<Category> categories = chartPlugin.getCategories();

				{
					Category category = categories.get(0);
					assertEquals("line", category.getName());
					assertEquals("nameLabel", category.getNameLabel().getValue());
					assertEquals("descLabel", category.getDescLabel().getValue());
					assertEquals(41, category.getOrder());
				}
			}
		}
	}

	@Test
	public void convertToDataSetRangeTest()
	{
		{
			ChartPluginDataSetRange dsr = this.jsonChartPluginPropertiesResolver.convertToDataSetRange(null);
			assertNull(dsr);
		}

		{
			int min = 1;
			
			ChartPluginDataSetRange dsr = this.jsonChartPluginPropertiesResolver.convertToDataSetRange(min);

			assertEquals(1, dsr.getMain().getMin().intValue());
			assertNull(dsr.getMain().getMax());

			assertNull(dsr.getAttachment());
		}

		{
			Map<String, Object> main = new HashMap<String, Object>();
			main.put(ChartPluginDataSetRange.Range.PROPERTY_MIN, 1);
			main.put(ChartPluginDataSetRange.Range.PROPERTY_MAX, 2);

			ChartPluginDataSetRange dsr = this.jsonChartPluginPropertiesResolver.convertToDataSetRange(main);

			assertEquals(1, dsr.getMain().getMin().intValue());
			assertEquals(2, dsr.getMain().getMax().intValue());

			assertNull(dsr.getAttachment());
		}

		{
			Map<String, Object> main = new HashMap<String, Object>();
			main.put(ChartPluginDataSetRange.Range.PROPERTY_MIN, 1);

			ChartPluginDataSetRange dsr = this.jsonChartPluginPropertiesResolver.convertToDataSetRange(main);

			assertEquals(1, dsr.getMain().getMin().intValue());
			assertNull(dsr.getMain().getMax());

			assertNull(dsr.getAttachment());
		}

		{
			Map<String, Object> map = new HashMap<String, Object>();

			Map<String, Object> main = new HashMap<String, Object>();
			main.put(ChartPluginDataSetRange.Range.PROPERTY_MIN, 1);
			main.put(ChartPluginDataSetRange.Range.PROPERTY_MAX, 2);

			Map<String, Object> attachment = new HashMap<String, Object>();
			attachment.put(ChartPluginDataSetRange.Range.PROPERTY_MIN, 3);
			attachment.put(ChartPluginDataSetRange.Range.PROPERTY_MAX, 4);

			map.put(ChartPluginDataSetRange.PROPERTY_MAIN, main);
			map.put(ChartPluginDataSetRange.PROPERTY_ATTACHMENT, attachment);

			ChartPluginDataSetRange dsr = this.jsonChartPluginPropertiesResolver.convertToDataSetRange(map);

			assertEquals(1, dsr.getMain().getMin().intValue());
			assertEquals(2, dsr.getMain().getMax().intValue());

			assertEquals(3, dsr.getAttachment().getMin().intValue());
			assertEquals(4, dsr.getAttachment().getMax().intValue());
		}

		{
			Map<String, Object> map = new HashMap<String, Object>();

			Map<String, Object> main = new HashMap<String, Object>();
			main.put(ChartPluginDataSetRange.Range.PROPERTY_MIN, 1);
			main.put(ChartPluginDataSetRange.Range.PROPERTY_MAX, 2);

			map.put(ChartPluginDataSetRange.PROPERTY_MAIN, main);

			ChartPluginDataSetRange dsr = this.jsonChartPluginPropertiesResolver.convertToDataSetRange(map);

			assertEquals(1, dsr.getMain().getMin().intValue());
			assertEquals(2, dsr.getMain().getMax().intValue());

			assertNull(dsr.getAttachment());
		}

		{
			Map<String, Object> map = new HashMap<String, Object>();

			Map<String, Object> attachment = new HashMap<String, Object>();
			attachment.put(ChartPluginDataSetRange.Range.PROPERTY_MIN, 3);
			attachment.put(ChartPluginDataSetRange.Range.PROPERTY_MAX, 4);

			map.put(ChartPluginDataSetRange.PROPERTY_ATTACHMENT, attachment);

			ChartPluginDataSetRange dsr = this.jsonChartPluginPropertiesResolver.convertToDataSetRange(map);

			assertNull(dsr.getMain());

			assertEquals(3, dsr.getAttachment().getMin().intValue());
			assertEquals(4, dsr.getAttachment().getMax().intValue());
		}
	}

	@Test
	public void convertToRangeTest()
	{
		{
			Range r = this.jsonChartPluginPropertiesResolver.convertToRange(null);

			assertNull(r);
		}

		{
			Map<String, Object> range = new HashMap<String, Object>();

			Range r = this.jsonChartPluginPropertiesResolver.convertToRange(range);

			assertNull(r);
		}

		{
			Map<String, Object> range = new HashMap<String, Object>();
			range.put(ChartPluginDataSetRange.Range.PROPERTY_MIN, 1);

			Range r = this.jsonChartPluginPropertiesResolver.convertToRange(range);

			assertEquals(1, r.getMin().intValue());
			assertNull(r.getMax());
		}

		{
			Map<String, Object> range = new HashMap<String, Object>();
			range.put(ChartPluginDataSetRange.Range.PROPERTY_MAX, 2);

			Range r = this.jsonChartPluginPropertiesResolver.convertToRange(range);

			assertNull(r.getMin());
			assertEquals(2, r.getMax().intValue());
		}

		{
			Map<String, Object> range = new HashMap<String, Object>();
			range.put(ChartPluginDataSetRange.Range.PROPERTY_MIN, 1);
			range.put(ChartPluginDataSetRange.Range.PROPERTY_MAX, 2);

			Range r = this.jsonChartPluginPropertiesResolver.convertToRange(range);

			assertEquals(1, r.getMin().intValue());
			assertEquals(2, r.getMax().intValue());
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
		public Chart renderChart(ChartDefinition chartDefinition, RenderContext renderContext) throws RenderException
		{
			throw new UnsupportedOperationException();
		}
	}
}
