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
import org.datagear.analysis.ChartPluginAttributeForm;
import org.datagear.analysis.ChartPluginDataSetRange;
import org.datagear.analysis.ChartPluginDataSetRange.Range;
import org.datagear.analysis.DataSign;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.form.FormProperty;
import org.datagear.analysis.form.FormPropertyGroup;
import org.datagear.analysis.form.InputFormProperty;
import org.datagear.analysis.form.ObjectFormProperty;
import org.datagear.analysis.form.PropertyType;
import org.datagear.analysis.support.JsonChartPluginPropertiesResolver.Group;
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
	@SuppressWarnings({ "unchecked", "deprecation" })
	@Test
	public void resolvePropertiesTest() throws IOException
	{
		Locale enLocale = new Locale("en");
		Locale zhLocale = new Locale("zh");

		{
			InputStream jsonInputStream = getClass().getClassLoader()
					.getResourceAsStream("org/datagear/analysis/support/JsonChartPluginPropertiesResolverTest.json");

			TestChartPlugin chartPlugin = new TestChartPlugin();
			JsonChartPluginPropertiesResolver<TestChartPlugin> resolver = new JsonChartPluginPropertiesResolver<TestChartPlugin>(
					chartPlugin);
			resolver.resolveProperties(jsonInputStream, IOUtil.CHARSET_UTF_8);

			assertEquals("pie-chart", chartPlugin.getId());
			assertNotNull(chartPlugin.getNameLabel());
			assertNotNull(chartPlugin.getDescLabel());
			assertNotNull(chartPlugin.getIconResourceNames().size() > 0);
			assertNotNull(chartPlugin.getAttributeForm());
			assertNotNull(chartPlugin.getDataSigns());
			assertEquals("0.1.0", chartPlugin.getVersion());
			assertEquals(2, chartPlugin.getOrder());
			assertNotNull(chartPlugin.getCategories());
			assertEquals(2, chartPlugin.getCategories().size());
			assertNotNull(chartPlugin.getCategoryOrders());
			assertEquals(2, chartPlugin.getCategoryOrders().size());
			assertEquals("test", chartPlugin.getAuthor());
			assertEquals("2024-09-01", chartPlugin.getIssueDate());

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
			assertEquals(7, dataSigns.size());

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

				Map<String, ?> additions = dataSign.getAdditions();
				assertNotNull(additions);
				assertEquals("field", additions.get("for"));
				assertEquals("x-val", additions.get("name"));
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
				assertNull(dataSign.getAdditions());
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
				DataSign dataSign = dataSigns.get(3);

				assertEquals("sign-special-char-.#%*()", dataSign.getName());
			}

			{
				DataSign dataSign = dataSigns.get(4);

				assertEquals("signWithFieldMatcher", dataSign.getName());
				assertEquals("primitive", dataSign.getFieldMatcher());
			}

			{
				DataSign dataSign = dataSigns.get(5);
				List<DataSign> children0 = dataSign.getChildren();

				assertEquals("nestDataSetSign", dataSign.getName());
				assertEquals(1, dataSign.getTargets().length);
				assertEquals(DataSign.TARGET_DATASET, dataSign.getTargets()[0]);
				assertEquals(2, children0.size());
				
				{
					DataSign child = children0.get(0);
					List<DataSign> children1 = child.getChildren();

					assertEquals("s0", child.getName());
					assertEquals(1, child.getTargets().length);
					assertEquals(DataSign.TARGET_FIELD, child.getTargets()[0]);
					assertEquals(2, children1.size());

					{
						DataSign child1 = children1.get(0);
						List<DataSign> children2 = child1.getChildren();

						assertEquals("s00", child1.getName());
						assertEquals(1, child1.getTargets().length);
						assertEquals(DataSign.TARGET_FIELD, child1.getTargets()[0]);
						assertEquals(1, children2.size());

						{
							DataSign child2 = children2.get(0);
							assertEquals("s000", child2.getName());
							assertEquals(1, child2.getTargets().length);
							assertEquals(DataSign.TARGET_FIELD, child2.getTargets()[0]);
						}
					}
					{
						DataSign child1 = children1.get(1);
						assertEquals("s01", child1.getName());
						assertEquals(1, child1.getTargets().length);
						assertEquals(DataSign.TARGET_FIELD, child1.getTargets()[0]);
						assertEquals("fm01", child1.getFieldMatcher());
					}
				}

				{
					DataSign child = children0.get(1);
					assertEquals("s1", child.getName());
					assertEquals(1, child.getTargets().length);
					assertEquals(DataSign.TARGET_FIELD, child.getTargets()[0]);
				}
			}

			{
				DataSign dataSign = dataSigns.get(6);
				List<DataSign> children0 = dataSign.getChildren();

				assertEquals("nestFieldSign", dataSign.getName());
				assertEquals(1, dataSign.getTargets().length);
				assertEquals(DataSign.TARGET_FIELD, dataSign.getTargets()[0]);
				assertEquals(2, children0.size());

				{
					DataSign child = children0.get(0);
					List<DataSign> children1 = child.getChildren();

					assertEquals("s0", child.getName());
					assertEquals(1, child.getTargets().length);
					assertEquals(DataSign.TARGET_FIELD, child.getTargets()[0]);
					assertEquals(2, children1.size());

					{
						DataSign child1 = children1.get(0);
						List<DataSign> children2 = child1.getChildren();

						assertEquals("s00", child1.getName());
						assertEquals(1, child1.getTargets().length);
						assertEquals(DataSign.TARGET_FIELD, child1.getTargets()[0]);
						assertEquals(1, children2.size());

						{
							DataSign child2 = children2.get(0);
							assertEquals("s000", child2.getName());
							assertEquals(1, child2.getTargets().length);
							assertEquals(DataSign.TARGET_FIELD, child2.getTargets()[0]);
							assertEquals("fm000", child2.getFieldMatcher());
						}
					}
					{
						DataSign child1 = children1.get(1);
						assertEquals("s01", child1.getName());
						assertEquals(1, child1.getTargets().length);
						assertEquals(DataSign.TARGET_FIELD, child1.getTargets()[0]);
					}
				}

				{
					DataSign child = children0.get(1);
					assertEquals("s1", child.getName());
					assertEquals(1, child.getTargets().length);
					assertEquals(DataSign.TARGET_FIELD, child.getTargets()[0]);
				}
			}

			{
				ChartPluginDataSetRange dataSetRange = chartPlugin.getDataSetRange();
				assertEquals(1, dataSetRange.getMain().getMin().intValue());
				assertNull(dataSetRange.getMain().getMax());
				assertNull(dataSetRange.getAttachment());
			}

			ChartPluginAttributeForm attributeForm = chartPlugin.getAttributeForm();
			List<FormProperty> formProperties = attributeForm.getProperties();
			List<FormPropertyGroup> propertyGroups = attributeForm.getGroups();
			
			assertNotNull(formProperties);
			assertNotNull(propertyGroups);
			assertEquals(2, propertyGroups.size());
			
			{
				{
					FormPropertyGroup group = propertyGroups.get(0);
					Label groupNameLabel = group.getNameLabel();
					Label groupDescLabel = group.getDescLabel();
					List<String> names = group.getNames();

					assertEquals("分组-0", groupNameLabel.getValue());
					assertEquals("group-0", groupNameLabel.getValue(enLocale));
					assertEquals("分组-0-中文", groupNameLabel.getValue(zhLocale));

					assertEquals("分组-0-描述", groupDescLabel.getValue());
					assertEquals("group-0 desc", groupDescLabel.getValue(enLocale));
					assertEquals("分组-0-描述-中文", groupDescLabel.getValue(zhLocale));

					assertEquals(2, names.size());
					assertEquals("title", names.get(0));
					assertEquals("interval", names.get(1));
				}

				{
					FormPropertyGroup group = propertyGroups.get(1);
					Label groupNameLabel = group.getNameLabel();
					Label groupDescLabel = group.getDescLabel();
					List<String> names = group.getNames();

					assertEquals("分组-1", groupNameLabel.getValue());
					assertNull(groupDescLabel);

					assertEquals(3, names.size());
					assertEquals("a2", names.get(0));
					assertEquals("a3", names.get(1));
					assertEquals("a4", names.get(2));
				}
			}

			{
				FormProperty prop0 = formProperties.get(0);

				assertTrue(prop0 instanceof InputFormProperty);

				InputFormProperty a0 = (InputFormProperty) formProperties.get(0);

				assertEquals("title", a0.getName());
				assertEquals(PropertyType.STRING, a0.getType());

				Label nameLabel = a0.getNameLabel();
				assertEquals("标题", nameLabel.getValue());
				assertEquals("title", nameLabel.getValue(enLocale));
				assertEquals("标题中文", nameLabel.getValue(zhLocale));

				Label descLabel = a0.getDescLabel();
				assertEquals("标题描述", descLabel.getValue());
				assertEquals("title desc", descLabel.getValue(enLocale));
				assertEquals("标题描述中文", descLabel.getValue(zhLocale));

				assertFalse(a0.isRequired());
				assertTrue(StringUtil.isEmpty(a0.getInputType()));
				assertTrue(StringUtil.isEmpty(a0.getInputPayload()));

				Map<String, ?> additions = a0.getAdditions();
				assertNotNull(additions);
				assertEquals("that", additions.get("for"));
				assertEquals("title.text", additions.get("optionPath"));
				assertEquals(3, ((Number) additions.get("priority")).intValue());
			}

			{
				FormProperty prop1 = formProperties.get(0);

				assertTrue(prop1 instanceof InputFormProperty);

				InputFormProperty a1 = (InputFormProperty) formProperties.get(1);

				assertEquals("interval", a1.getName());
				assertEquals(PropertyType.NUMBER, a1.getType());

				Label nameLabel = a1.getNameLabel();
				assertEquals("间隔", nameLabel.getValue());
				assertEquals("interval", nameLabel.getValue(enLocale));
				assertEquals("间隔中文", nameLabel.getValue(zhLocale));

				Label descLabel = a1.getDescLabel();
				assertEquals("间隔描述", descLabel.getValue());
				assertEquals("interval desc", descLabel.getValue(enLocale));
				assertEquals("间隔描述中文", descLabel.getValue(zhLocale));

				assertTrue(a1.isRequired());
				assertEquals("mytype", a1.getInputType());
				assertEquals("mypayload", a1.getInputPayload());
			}

			{
				InputFormProperty a2 = (InputFormProperty) formProperties.get(2);
				InputFormProperty a3 = (InputFormProperty) formProperties.get(3);
				InputFormProperty a4 = (InputFormProperty) formProperties.get(4);
				InputFormProperty a5 = (InputFormProperty) formProperties.get(5);
				InputFormProperty a6 = (InputFormProperty) formProperties.get(6);
				InputFormProperty a7 = (InputFormProperty) formProperties.get(7);

				assertEquals("a2", a2.getName());
				assertEquals(PropertyType.BOOLEAN, a2.getType());
				assertNull(a2.getAdditions() == null ? null
						: a2.getAdditions().get(JsonChartPluginPropertiesResolver.INPUT_PROPERTY_ADDITION_OLD_GROUP));
				{
					List<?> inputPayload = (List<?>) a2.getInputPayload();
					assertEquals(2, inputPayload.size());
					Map<String, ?> inputPayload0 = (Map<String, ?>) inputPayload.get(0);
					Map<String, ?> inputPayload1 = (Map<String, ?>) inputPayload.get(1);
					assertEquals("a", inputPayload0.get("name"));
					assertEquals(2, ((Number) inputPayload0.get("value")).intValue());
					assertEquals("b", inputPayload1.get("name"));
					assertEquals(3, ((Number) inputPayload1.get("value")).intValue());
				}

				assertEquals("a3", a3.getName());
				assertEquals(PropertyType.STRING, a3.getType());
				assertEquals("a", a3.getDefaultValue());
				{
					List<?> inputPayload = (List<?>) a3.getInputPayload();
					assertEquals(2, inputPayload.size());
					assertEquals("a", inputPayload.get(0));
					assertEquals("b", inputPayload.get(1));
				}

				assertEquals("a4", a4.getName());
				assertEquals(PropertyType.NUMBER, a4.getType());
				assertEquals(2, a4.getDefaultValue());
				{
					List<?> inputPayload = (List<?>) a4.getInputPayload();
					assertEquals(2, inputPayload.size());
					assertEquals(2, ((Number) inputPayload.get(0)).intValue());
					assertEquals(3, ((Number) inputPayload.get(1)).intValue());
				}

				assertEquals("a5", a5.getName());
				assertEquals(PropertyType.BOOLEAN, a5.getType());
				assertNull(a5.getInputPayload());
				assertEquals(false, a5.getDefaultValue());

				assertEquals("a6", a6.getName());
				assertEquals(PropertyType.STRING, a6.getType());
				assertNull(a6.getInputPayload());

				assertEquals("a7", a7.getName());
				assertEquals(PropertyType.STRING, a7.getType());
				assertNull(a7.getInputPayload());
			}

			{
				ObjectFormProperty a8 = (ObjectFormProperty) formProperties.get(8);

				assertEquals("a8", a8.getName());
				assertEquals(PropertyType.OBJECT, a8.getType());
				assertEquals("对象-a8", a8.getNameLabel().getValue());
				assertEquals("对象-a8-描述", a8.getDescLabel().getValue());

				List<FormProperty> a8Chilren = a8.getProperties();

				assertEquals(4, a8Chilren.size());
				assertEquals("a8.1", a8Chilren.get(0).getName());
				assertEquals(PropertyType.STRING, a8Chilren.get(0).getType());
				assertEquals("a8.2", a8Chilren.get(1).getName());
				assertEquals("a8.3", a8Chilren.get(2).getName());
				assertEquals(PropertyType.OBJECT, a8Chilren.get(2).getType());
				assertEquals("a8.4", a8Chilren.get(3).getName());

				{
					List<FormProperty> a8_3Chilren = ((ObjectFormProperty) a8Chilren.get(2)).getProperties();

					assertEquals(2, a8_3Chilren.size());
					assertEquals("a8.3.1", a8_3Chilren.get(0).getName());
					assertEquals(PropertyType.STRING, a8_3Chilren.get(0).getType());
				}

				List<FormPropertyGroup> groups = a8.getGroups();

				assertEquals(2, groups.size());

				{
					FormPropertyGroup group = groups.get(0);
					List<String> names = group.getNames();

					assertEquals("a8-分组-1", group.getNameLabel().getValue());
					assertEquals("a8-分组-1-描述", group.getDescLabel().getValue());
					assertEquals(2, names.size());
					assertEquals("a8.2", names.get(0));
					assertEquals("a8.3", names.get(1));
					assertEquals("a8.1 == 'ok'", group.getAdditions().get("displayIf"));
				}

				{
					FormPropertyGroup group = groups.get(1);
					List<String> names = group.getNames();

					assertEquals("a8-分组-2", group.getNameLabel().getValue());
					assertEquals(1, names.size());
					assertEquals("a8.4", names.get(0));
				}
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

			{
				Map<String, ?> additions = chartPlugin.getAdditions();
				assertNotNull(additions);
				assertEquals("aaa", additions.get("name"));
				assertEquals(3, ((Number) additions.get("value")).intValue());
			}
		}
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	@Test
	public void resolvePropertiesTest_5_5_0() throws IOException
	{
		Locale enLocale = new Locale("en");
		Locale zhLocale = new Locale("zh");
		
		{
			InputStream jsonInputStream = getClass().getClassLoader()
					.getResourceAsStream(
							"org/datagear/analysis/support/JsonChartPluginPropertiesResolverTest-5.5.0.json");

			TestChartPlugin chartPlugin = new TestChartPlugin();
			JsonChartPluginPropertiesResolver<TestChartPlugin> resolver = new JsonChartPluginPropertiesResolver<TestChartPlugin>(
					chartPlugin);
			resolver.resolveProperties(jsonInputStream, IOUtil.CHARSET_UTF_8);

			assertEquals("pie-chart", chartPlugin.getId());
			assertNotNull(chartPlugin.getNameLabel());
			assertNotNull(chartPlugin.getDescLabel());
			assertNotNull(chartPlugin.getIconResourceNames().size() > 0);
			assertNotNull(chartPlugin.getAttributeForm());
			assertNotNull(chartPlugin.getDataSigns());
			assertEquals("0.1.0", chartPlugin.getVersion());
			assertEquals(2, chartPlugin.getOrder());
			assertNotNull(chartPlugin.getCategories());
			assertEquals(2, chartPlugin.getCategories().size());
			assertNotNull(chartPlugin.getCategoryOrders());
			assertEquals(2, chartPlugin.getCategoryOrders().size());
			assertEquals("test", chartPlugin.getAuthor());
			assertEquals("2024-09-01", chartPlugin.getIssueDate());

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

				Map<String, ?> additions = dataSign.getAdditions();
				assertNotNull(additions);
				assertEquals("field", additions.get("for"));
				assertEquals("x-val", additions.get("name"));
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
				assertNull(dataSign.getAdditions());
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

			ChartPluginAttributeForm attributeForm = chartPlugin.getAttributeForm();
			List<FormProperty> formProperties = attributeForm.getProperties();

			{
				FormProperty prop0 = formProperties.get(0);

				assertTrue(prop0 instanceof InputFormProperty);

				InputFormProperty a0 = (InputFormProperty) formProperties.get(0);

				assertEquals("title", a0.getName());
				assertEquals(PropertyType.STRING, a0.getType());

				Label nameLabel = a0.getNameLabel();
				assertEquals("标题", nameLabel.getValue());
				assertEquals("title", nameLabel.getValue(enLocale));
				assertEquals("标题中文", nameLabel.getValue(zhLocale));

				Label descLabel = a0.getDescLabel();
				assertEquals("标题描述", descLabel.getValue());
				assertEquals("title desc", descLabel.getValue(enLocale));
				assertEquals("标题描述中文", descLabel.getValue(zhLocale));
				
				assertFalse(a0.isRequired());
				assertTrue(StringUtil.isEmpty(a0.getInputType()));
				assertTrue(StringUtil.isEmpty(a0.getInputPayload()));

				Group group = (Group) a0.getAdditions()
						.get(JsonChartPluginPropertiesResolver.INPUT_PROPERTY_ADDITION_OLD_GROUP);
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
				
				Map<String, ?> additions = a0.getAdditions();
				assertNotNull(additions);
				assertEquals("that", additions.get("for"));
				assertEquals("title.text", additions.get("optionPath"));
				assertEquals(3, ((Number) additions.get("priority")).intValue());
			}

			{
				FormProperty prop1 = formProperties.get(0);

				assertTrue(prop1 instanceof InputFormProperty);

				InputFormProperty a1 = (InputFormProperty) formProperties.get(1);

				assertEquals("interval", a1.getName());
				assertEquals(PropertyType.NUMBER, a1.getType());

				Label nameLabel = a1.getNameLabel();
				assertEquals("间隔", nameLabel.getValue());
				assertEquals("interval", nameLabel.getValue(enLocale));
				assertEquals("间隔中文", nameLabel.getValue(zhLocale));

				Label descLabel = a1.getDescLabel();
				assertEquals("间隔描述", descLabel.getValue());
				assertEquals("interval desc", descLabel.getValue(enLocale));
				assertEquals("间隔描述中文", descLabel.getValue(zhLocale));
				
				assertTrue(a1.isRequired());
				assertEquals("mytype", a1.getInputType());
				assertEquals("mypayload", a1.getInputPayload());

				Group group = (Group) a1.getAdditions()
						.get(JsonChartPluginPropertiesResolver.INPUT_PROPERTY_ADDITION_OLD_GROUP);
				Label groupNameLabel = group.getNameLabel();
				Label groupDescLabel = group.getDescLabel();

				assertEquals("group-1", group.getName());
				assertEquals(0, group.getOrder());
				assertNull(groupNameLabel);
				assertNull(groupDescLabel);
			}
			
			{
				InputFormProperty a2 = (InputFormProperty) formProperties.get(2);
				InputFormProperty a3 = (InputFormProperty) formProperties.get(3);
				InputFormProperty a4 = (InputFormProperty) formProperties.get(4);
				InputFormProperty a5 = (InputFormProperty) formProperties.get(5);
				InputFormProperty a6 = (InputFormProperty) formProperties.get(6);
				InputFormProperty a7 = (InputFormProperty) formProperties.get(7);

				assertEquals("a2", a2.getName());
				assertEquals(PropertyType.BOOLEAN, a2.getType());
				assertNull(a2.getAdditions() == null ? null
						: a2.getAdditions().get(JsonChartPluginPropertiesResolver.INPUT_PROPERTY_ADDITION_OLD_GROUP));
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
				assertEquals(PropertyType.STRING, a3.getType());
				assertEquals("a", a3.getDefaultValue());
				{
					List<?> inputPayload = (List<?>)a3.getInputPayload();
					assertEquals(2, inputPayload.size());
					assertEquals("a", inputPayload.get(0));
					assertEquals("b", inputPayload.get(1));
				}

				assertEquals("a4", a4.getName());
				assertEquals(PropertyType.NUMBER, a4.getType());
				assertEquals(2, a4.getDefaultValue());
				{
					List<?> inputPayload = (List<?>)a4.getInputPayload();
					assertEquals(2, inputPayload.size());
					assertEquals(2, ((Number) inputPayload.get(0)).intValue());
					assertEquals(3, ((Number) inputPayload.get(1)).intValue());
				}

				assertEquals("a5", a5.getName());
				assertEquals(PropertyType.BOOLEAN, a5.getType());
				assertNull(a5.getInputPayload());
				assertEquals(false, a5.getDefaultValue());

				assertEquals("a6", a6.getName());
				assertEquals(PropertyType.STRING, a6.getType());
				assertNull(a6.getInputPayload());

				assertEquals("a7", a7.getName());
				assertEquals(PropertyType.STRING, a7.getType());
				assertNull(a7.getInputPayload());
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

			{
				Map<String, ?> additions = chartPlugin.getAdditions();
				assertNotNull(additions);
				assertEquals("aaa", additions.get("name"));
				assertEquals(3, ((Number) additions.get("value")).intValue());
			}
		}
	}

	@Test
	public void resolvePropertiesTest_author_issueDate() throws IOException
	{
		InputStream jsonInputStream = getClass().getClassLoader().getResourceAsStream(
				"org/datagear/analysis/support/JsonChartPluginPropertiesResolverTest-author-issueDate.json");

		TestChartPlugin chartPlugin = new TestChartPlugin();
		JsonChartPluginPropertiesResolver<TestChartPlugin> resolver = new JsonChartPluginPropertiesResolver<TestChartPlugin>(
				chartPlugin);
		resolver.resolveProperties(jsonInputStream,
				IOUtil.CHARSET_UTF_8);

		assertEquals("author-issueDate", chartPlugin.getId());
		assertEquals("test", chartPlugin.getAuthor());
		assertEquals("2024-09-01", chartPlugin.getIssueDate());
	}

	@Test
	public void resolvePropertiesTest_contact() throws IOException
	{
		InputStream jsonInputStream = getClass().getClassLoader().getResourceAsStream(
				"org/datagear/analysis/support/JsonChartPluginPropertiesResolverTest-contact.json");

		TestChartPlugin chartPlugin = new TestChartPlugin();
		JsonChartPluginPropertiesResolver<TestChartPlugin> resolver = new JsonChartPluginPropertiesResolver<TestChartPlugin>(
				chartPlugin);
		resolver.resolveProperties(jsonInputStream,
				IOUtil.CHARSET_UTF_8);

		assertEquals("contact", chartPlugin.getId());
		assertEquals("test", chartPlugin.getAuthor());
		assertEquals("test@dgtest.com", chartPlugin.getContact());
		assertEquals("2024-09-01", chartPlugin.getIssueDate());
	}

	@Test
	public void resolvePropertiesTest_dataSetRange() throws IOException
	{
		{
			InputStream jsonInputStream = getClass().getClassLoader()
					.getResourceAsStream(
							"org/datagear/analysis/support/JsonChartPluginPropertiesResolverTest-dataSetRange-number.json");

			TestChartPlugin chartPlugin = new TestChartPlugin();
			JsonChartPluginPropertiesResolver<TestChartPlugin> resolver = new JsonChartPluginPropertiesResolver<TestChartPlugin>(
					chartPlugin);
			resolver.resolveProperties(jsonInputStream,
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
			JsonChartPluginPropertiesResolver<TestChartPlugin> resolver = new JsonChartPluginPropertiesResolver<TestChartPlugin>(
					chartPlugin);
			resolver.resolveProperties(jsonInputStream,
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
	public void resolvePropertiesTest_string_categories() throws IOException
	{
		{
			InputStream jsonInputStream = getClass().getClassLoader().getResourceAsStream(
					"org/datagear/analysis/support/JsonChartPluginPropertiesResolverTest-string-categories.json");

			TestChartPlugin chartPlugin = new TestChartPlugin();
			JsonChartPluginPropertiesResolver<TestChartPlugin> resolver = new JsonChartPluginPropertiesResolver<TestChartPlugin>(
					chartPlugin);
			resolver.resolveProperties(jsonInputStream,
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
	public void resolvePropertiesTest_3_0_1() throws IOException
	{
		Locale enLocale = new Locale("en");
		Locale zhLocale = new Locale("zh");
		
		{
			InputStream jsonInputStream = getClass().getClassLoader()
					.getResourceAsStream(
							"org/datagear/analysis/support/JsonChartPluginPropertiesResolverTest-3.0.1.json");

			TestChartPlugin chartPlugin = new TestChartPlugin();
			JsonChartPluginPropertiesResolver<TestChartPlugin> resolver = new JsonChartPluginPropertiesResolver<TestChartPlugin>(
					chartPlugin);
			resolver.resolveProperties(jsonInputStream,
					IOUtil.CHARSET_UTF_8);

			assertEquals("pie-chart", chartPlugin.getId());
			assertNotNull(chartPlugin.getNameLabel());
			assertNotNull(chartPlugin.getDescLabel());
			assertTrue(chartPlugin.getIconResourceNames().size() > 0);
			assertNotNull(chartPlugin.getAttributeForm());
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

			ChartPluginAttributeForm attributeForm = chartPlugin.getAttributeForm();
			List<FormProperty> formProperties = attributeForm.getProperties();

			{
				InputFormProperty prop = (InputFormProperty) formProperties
						.get(0);

				assertEquals("title", prop.getName());
				assertEquals(PropertyType.STRING, prop.getType());

				Label nameLabel = prop.getNameLabel();
				assertEquals("标题", nameLabel.getValue());
				assertEquals("title", nameLabel.getValue(enLocale));
				assertEquals("标题中文", nameLabel.getValue(zhLocale));

				Label descLabel = prop.getDescLabel();
				assertEquals("标题描述", descLabel.getValue());
				assertEquals("title desc", descLabel.getValue(enLocale));
				assertEquals("标题描述中文", descLabel.getValue(zhLocale));
			}

			{
				InputFormProperty prop = (InputFormProperty) formProperties
						.get(1);

				assertEquals("interval", prop.getName());
				assertEquals(PropertyType.NUMBER, prop.getType());

				Label nameLabel = prop.getNameLabel();
				assertEquals("间隔", nameLabel.getValue());
				assertEquals("interval", nameLabel.getValue(enLocale));
				assertEquals("间隔中文", nameLabel.getValue(zhLocale));

				Label descLabel = prop.getDescLabel();
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
	public void resolvePropertiesTest_5_4_0_dataSetSign() throws IOException
	{
		{
			InputStream jsonInputStream = getClass().getClassLoader().getResourceAsStream(
					"org/datagear/analysis/support/JsonChartPluginPropertiesResolverTest-5.4.0-dataSetSign.json");

			TestChartPlugin chartPlugin = new TestChartPlugin();
			JsonChartPluginPropertiesResolver<TestChartPlugin> resolver = new JsonChartPluginPropertiesResolver<TestChartPlugin>(
					chartPlugin);
			resolver.resolveProperties(jsonInputStream,
					IOUtil.CHARSET_UTF_8);

			assertEquals("dataSetSign", chartPlugin.getId());

			{
				Label nameLabel = chartPlugin.getNameLabel();
				assertEquals("数据集标记", nameLabel.getValue());
			}

			List<DataSign> dataSigns = chartPlugin.getDataSigns();

			{
				DataSign dataSign = dataSigns.get(0);
				String[] targets = dataSign.getTargets();

				assertEquals("field-01", dataSign.getName());
				assertEquals(1, targets.length);
				assertEquals(DataSign.TARGET_FIELD, targets[0]);
				assertTrue(dataSign.isRequired());
				assertFalse(dataSign.isMultiple());
				assertNull(dataSign.getChildren());
				assertNull(dataSign.getNameLabel());
			}

			{
				DataSign dataSign = dataSigns.get(1);
				String[] targets = dataSign.getTargets();

				assertEquals("dataSet-01", dataSign.getName());
				assertEquals(1, targets.length);
				assertEquals(DataSign.TARGET_DATASET, targets[0]);
				assertFalse(dataSign.isRequired());
				assertTrue(dataSign.isMultiple());
				assertNull(dataSign.getNameLabel());

				List<DataSign> children = dataSign.getChildren();
				assertNotNull(children);
				assertEquals(5, children.size());

				{
					DataSign ds = children.get(0);
					String[] dsTargets = ds.getTargets();

					assertEquals("name", ds.getName());
					assertEquals(1, dsTargets.length);
					assertEquals(DataSign.TARGET_FIELD, dsTargets[0]);
					assertFalse(ds.isRequired());
					assertTrue(ds.isMultiple());
					assertEquals("数据集标记01-名称", ds.getNameLabel().getValue());
					assertEquals("数据集标记01-名称-描述", ds.getDescLabel().getValue());
					assertNull(ds.getChildren());

					Map<String, ?> additions = ds.getAdditions();
					assertEquals("v0", additions.get("a0"));
					assertEquals(3, ((Integer) additions.get("a1")).intValue());
				}

				{
					DataSign ds = children.get(1);
					String[] dsTargets = ds.getTargets();

					assertEquals("value", ds.getName());
					assertEquals(1, dsTargets.length);
					assertEquals(DataSign.TARGET_FIELD, dsTargets[0]);
					assertTrue(ds.isRequired());
					assertFalse(ds.isMultiple());
					assertEquals("数据集标记01-值", ds.getNameLabel().getValue());
				}

				{
					DataSign ds = children.get(2);
					String[] dsTargets = ds.getTargets();

					assertEquals("size", ds.getName());
					assertEquals(1, dsTargets.length);
					assertEquals("unknown", dsTargets[0]);
					assertTrue(ds.isRequired());
					assertFalse(ds.isMultiple());
					assertEquals("数据集标记01-尺寸", ds.getNameLabel().getValue());
				}

				{
					DataSign ds = children.get(3);
					String[] dsTargets = ds.getTargets();

					assertEquals("range", ds.getName());
					assertEquals(2, dsTargets.length);
					assertEquals("aaa", dsTargets[0]);
					assertEquals("bbb", dsTargets[1]);
					assertTrue(ds.isRequired());
					assertFalse(ds.isMultiple());
					assertEquals("数据集标记01-范围", ds.getNameLabel().getValue());
				}

				{
					DataSign ds = children.get(4);
					String[] dsTargets = ds.getTargets();
					List<DataSign> subSigns = ds.getChildren();

					assertEquals("subSigns", ds.getName());
					assertEquals(1, dsTargets.length);
					assertEquals(DataSign.TARGET_FIELD, dsTargets[0]);
					assertTrue(ds.isRequired());
					assertFalse(ds.isMultiple());
					assertEquals(2, subSigns.size());

					{
						DataSign sub = subSigns.get(0);
						String[] subTargets = ds.getTargets();

						assertEquals("aaa", sub.getName());
						assertEquals(1, subTargets.length);
						assertEquals(DataSign.TARGET_FIELD, subTargets[0]);
						assertTrue(sub.isRequired());
						assertFalse(sub.isMultiple());
					}

					{
						DataSign sub = subSigns.get(1);
						String[] subTargets = ds.getTargets();

						assertEquals("bbb", sub.getName());
						assertEquals(1, subTargets.length);
						assertEquals(DataSign.TARGET_FIELD, subTargets[0]);
						assertTrue(sub.isRequired());
						assertFalse(sub.isMultiple());
					}
				}
			}

			{
				DataSign dataSign = dataSigns.get(2);
				String[] targets = dataSign.getTargets();

				assertEquals("value", dataSign.getName());
				assertEquals(1, targets.length);
				assertEquals(DataSign.TARGET_FIELD, targets[0]);
				assertTrue(dataSign.isRequired());
				assertFalse(dataSign.isMultiple());
				assertNull(dataSign.getChildren());
				assertNull(dataSign.getNameLabel());
			}

			{
				DataSign dataSign = dataSigns.get(3);
				String[] targets = dataSign.getTargets();

				assertEquals("dataSet-02", dataSign.getName());
				assertEquals(3, targets.length);
				assertEquals(DataSign.TARGET_FIELD, targets[0]);
				assertEquals(DataSign.TARGET_DATASET, targets[1]);
				assertEquals("unknown", targets[2]);
				assertTrue(dataSign.isRequired());
				assertFalse(dataSign.isMultiple());
				assertNull(dataSign.getNameLabel());
				assertNull(dataSign.getChildren());
			}
		}
	}

	@Test
	public void convertToDataSetRangeTest()
	{
		TestChartPlugin chartPlugin = new TestChartPlugin();
		JsonChartPluginPropertiesResolver<TestChartPlugin> resolver = new JsonChartPluginPropertiesResolver<TestChartPlugin>(
				chartPlugin);

		{
			ChartPluginDataSetRange dsr = resolver.convertToDataSetRange(null);
			assertNull(dsr);
		}

		{
			int min = 1;
			
			ChartPluginDataSetRange dsr = resolver.convertToDataSetRange(min);

			assertEquals(1, dsr.getMain().getMin().intValue());
			assertNull(dsr.getMain().getMax());

			assertNull(dsr.getAttachment());
		}

		{
			Map<String, Object> main = new HashMap<String, Object>();
			main.put(ChartPluginDataSetRange.Range.PROPERTY_MIN, 1);
			main.put(ChartPluginDataSetRange.Range.PROPERTY_MAX, 2);

			ChartPluginDataSetRange dsr = resolver.convertToDataSetRange(main);

			assertEquals(1, dsr.getMain().getMin().intValue());
			assertEquals(2, dsr.getMain().getMax().intValue());

			assertNull(dsr.getAttachment());
		}

		{
			Map<String, Object> main = new HashMap<String, Object>();
			main.put(ChartPluginDataSetRange.Range.PROPERTY_MIN, 1);

			ChartPluginDataSetRange dsr = resolver.convertToDataSetRange(main);

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

			ChartPluginDataSetRange dsr = resolver.convertToDataSetRange(map);

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

			ChartPluginDataSetRange dsr = resolver.convertToDataSetRange(map);

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

			ChartPluginDataSetRange dsr = resolver.convertToDataSetRange(map);

			assertNull(dsr.getMain());

			assertEquals(3, dsr.getAttachment().getMin().intValue());
			assertEquals(4, dsr.getAttachment().getMax().intValue());
		}

		{
			{
				String value = "none";
				ChartPluginDataSetRange dsr = resolver.convertToDataSetRange(value);

				assertEquals(0, dsr.getMain().getMin().intValue());
				assertEquals(0, dsr.getMain().getMax().intValue());
				assertEquals(0, dsr.getAttachment().getMin().intValue());
				assertEquals(0, dsr.getAttachment().getMax().intValue());
			}
			{
				String value = "None";
				ChartPluginDataSetRange dsr = resolver.convertToDataSetRange(value);

				assertEquals(0, dsr.getMain().getMin().intValue());
				assertEquals(0, dsr.getMain().getMax().intValue());
				assertEquals(0, dsr.getAttachment().getMin().intValue());
				assertEquals(0, dsr.getAttachment().getMax().intValue());
			}
		}
	}

	@Test
	public void convertToRangeTest()
	{
		TestChartPlugin chartPlugin = new TestChartPlugin();
		JsonChartPluginPropertiesResolver<TestChartPlugin> resolver = new JsonChartPluginPropertiesResolver<TestChartPlugin>(
				chartPlugin);

		{
			Range r = resolver.convertToRange(null);

			assertNull(r);
		}

		{
			Map<String, Object> range = new HashMap<String, Object>();

			Range r = resolver.convertToRange(range);

			assertNull(r);
		}

		{
			Map<String, Object> range = new HashMap<String, Object>();
			range.put(ChartPluginDataSetRange.Range.PROPERTY_MIN, 1);

			Range r = resolver.convertToRange(range);

			assertEquals(1, r.getMin().intValue());
			assertNull(r.getMax());
		}

		{
			Map<String, Object> range = new HashMap<String, Object>();
			range.put(ChartPluginDataSetRange.Range.PROPERTY_MAX, 2);

			Range r = resolver.convertToRange(range);

			assertNull(r.getMin());
			assertEquals(2, r.getMax().intValue());
		}

		{
			Map<String, Object> range = new HashMap<String, Object>();
			range.put(ChartPluginDataSetRange.Range.PROPERTY_MIN, 1);
			range.put(ChartPluginDataSetRange.Range.PROPERTY_MAX, 2);

			Range r = resolver.convertToRange(range);

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
