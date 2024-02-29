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

package org.datagear.analysis.support;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.support.CsvValueDataSet.CsvValueDataSetResource;
import org.junit.Test;

/**
 * {@linkplain AbstractCsvDataSet}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class AbstractCsvDataSetTest
{
	@Test
	public void resolvePropertiesTest() throws Throwable
	{
		AbstractCsvDataSet<CsvValueDataSetResource> ds = new CsvValueDataSet();

		List<String> propertyNames = Arrays.asList("name", "value");

		// 全部是数值
		{
			List<Map<String, String>> data = new ArrayList<>();

			{
				Map<String, String> row = new HashMap<>();
				row.put("name", "a");
				row.put("value", "1");
				data.add(row);
			}
			{
				Map<String, String> row = new HashMap<>();
				row.put("name", "b");
				row.put("value", "2");
				data.add(row);
			}
			{
				Map<String, String> row = new HashMap<>();
				row.put("name", "c");
				row.put("value", "3");
				data.add(row);
			}

			List<DataSetProperty> properties = ds.resolveProperties(propertyNames, data);

			assertEquals(2, properties.size());
			assertEquals(DataSetProperty.DataType.STRING, properties.get(0).getType());
			assertEquals(DataSetProperty.DataType.NUMBER, properties.get(1).getType());
		}

		// 全部是空
		{
			List<Map<String, String>> data = new ArrayList<>();

			{
				Map<String, String> row = new HashMap<>();
				row.put("name", "a");
				row.put("value", "");
				data.add(row);
			}
			{
				Map<String, String> row = new HashMap<>();
				row.put("name", "b");
				row.put("value", null);
				data.add(row);
			}
			{
				Map<String, String> row = new HashMap<>();
				row.put("name", "c");
				row.put("value", "");
				data.add(row);
			}

			List<DataSetProperty> properties = ds.resolveProperties(propertyNames, data);

			assertEquals(2, properties.size());
			assertEquals(DataSetProperty.DataType.STRING, properties.get(0).getType());
			assertEquals(DataSetProperty.DataType.STRING, properties.get(1).getType());
		}

		// 除了空之外都是数值
		{
			List<Map<String, String>> data = new ArrayList<>();

			{
				Map<String, String> row = new HashMap<>();
				row.put("name", "a");
				row.put("value", "");
				data.add(row);
			}
			{
				Map<String, String> row = new HashMap<>();
				row.put("name", "b");
				row.put("value", null);
				data.add(row);
			}
			{
				Map<String, String> row = new HashMap<>();
				row.put("name", "c");
				row.put("value", "3");
				data.add(row);
			}

			List<DataSetProperty> properties = ds.resolveProperties(propertyNames, data);

			assertEquals(2, properties.size());
			assertEquals(DataSetProperty.DataType.STRING, properties.get(0).getType());
			assertEquals(DataSetProperty.DataType.NUMBER, properties.get(1).getType());
		}

		// 除了空之外都是字符串
		{
			List<Map<String, String>> data = new ArrayList<>();

			{
				Map<String, String> row = new HashMap<>();
				row.put("name", "a");
				row.put("value", "");
				data.add(row);
			}
			{
				Map<String, String> row = new HashMap<>();
				row.put("name", "b");
				row.put("value", null);
				data.add(row);
			}
			{
				Map<String, String> row = new HashMap<>();
				row.put("name", "c");
				row.put("value", "c");
				data.add(row);
			}

			List<DataSetProperty> properties = ds.resolveProperties(propertyNames, data);

			assertEquals(2, properties.size());
			assertEquals(DataSetProperty.DataType.STRING, properties.get(0).getType());
			assertEquals(DataSetProperty.DataType.STRING, properties.get(1).getType());
		}
	}
}
