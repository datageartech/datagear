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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.util.FileUtil;
import org.junit.Test;

/**
 * {@linkplain CsvDirectoryFileDataSet}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDirectoryFileDataSetTest
{
	private static final File DIRECTORY = FileUtil.getFile("src/test/resources/org/datagear/analysis/support/");

	@Test
	public void getResultTest()
	{
		List<DataSetProperty> properties = new ArrayList<>();
		properties.add(new DataSetProperty("name", DataSetProperty.DataType.STRING));
		properties.add(new DataSetProperty("value", DataSetProperty.DataType.NUMBER));
		properties.add(new DataSetProperty("尺寸", DataSetProperty.DataType.NUMBER));

		CsvDirectoryFileDataSet dataSet = new CsvDirectoryFileDataSet("a", "a", properties, DIRECTORY,
				"CsvDirectoryFileDataSetTest-0.csv");
		dataSet.setNameRow(1);

		DataSetResult result = dataSet.getResult(new DataSetQuery());
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) result.getData();

		{
			assertEquals(3, data.size());

			{
				Map<String, Object> row = data.get(0);

				assertEquals("aaa", row.get("name"));
				assertEquals(11, ((Number) row.get("value")).intValue());
				assertEquals(12, ((Number) row.get("尺寸")).intValue());
			}

			{
				Map<String, Object> row = data.get(1);

				assertEquals("bbb", row.get("name"));
				assertEquals(21, ((Number) row.get("value")).intValue());
				assertEquals(22, ((Number) row.get("尺寸")).intValue());
			}

			{
				Map<String, Object> row = data.get(2);

				assertEquals("ccc", row.get("name"));
				assertEquals(31, ((Number) row.get("value")).intValue());
				assertEquals(32, ((Number) row.get("尺寸")).intValue());
			}
		}
	}

	@Test
	public void resolveTest()
	{
		CsvDirectoryFileDataSet dataSet = new CsvDirectoryFileDataSet("a", "a", DIRECTORY,
				"CsvDirectoryFileDataSetTest-0.csv");
		dataSet.setNameRow(1);

		ResolvedDataSetResult result = dataSet.resolve(new DataSetQuery());
		List<DataSetProperty> properties = result.getProperties();
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) result.getResult().getData();

		{
			assertEquals(3, properties.size());

			{
				DataSetProperty property = properties.get(0);
				assertEquals("name", property.getName());
				assertEquals(DataSetProperty.DataType.STRING, property.getType());
			}

			{
				DataSetProperty property = properties.get(1);
				assertEquals("value", property.getName());
				assertEquals(DataSetProperty.DataType.NUMBER, property.getType());
			}

			{
				DataSetProperty property = properties.get(2);
				assertEquals("尺寸", property.getName());
				assertEquals(DataSetProperty.DataType.NUMBER, property.getType());
			}
		}

		{
			assertEquals(3, data.size());

			{
				Map<String, Object> row = data.get(0);

				assertEquals("aaa", row.get("name"));
				assertEquals(11, ((Number) row.get("value")).intValue());
				assertEquals(12, ((Number) row.get("尺寸")).intValue());
			}

			{
				Map<String, Object> row = data.get(1);

				assertEquals("bbb", row.get("name"));
				assertEquals(21, ((Number) row.get("value")).intValue());
				assertEquals(22, ((Number) row.get("尺寸")).intValue());
			}

			{
				Map<String, Object> row = data.get(2);

				assertEquals("ccc", row.get("name"));
				assertEquals(31, ((Number) row.get("value")).intValue());
				assertEquals(32, ((Number) row.get("尺寸")).intValue());
			}
		}
	}

	@Test
	public void resolveTest_noNameRow()
	{
		CsvDirectoryFileDataSet dataSet = new CsvDirectoryFileDataSet("a", "a", DIRECTORY,
				"CsvDirectoryFileDataSetTest-0.csv");

		ResolvedDataSetResult result = dataSet.resolve(new DataSetQuery());
		List<DataSetProperty> properties = result.getProperties();
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) result.getResult().getData();

		{
			assertEquals(3, properties.size());

			{
				DataSetProperty property = properties.get(0);
				assertEquals("1", property.getName());
				assertEquals(DataSetProperty.DataType.STRING, property.getType());
			}

			{
				DataSetProperty property = properties.get(1);
				assertEquals("2", property.getName());
				assertEquals(DataSetProperty.DataType.STRING, property.getType());
			}

			{
				DataSetProperty property = properties.get(2);
				assertEquals("3", property.getName());
				assertEquals(DataSetProperty.DataType.STRING, property.getType());
			}
		}

		{
			assertEquals(4, data.size());

			{
				Map<String, Object> row = data.get(0);

				assertEquals("name", row.get("1"));
				assertEquals("value", row.get("2"));
				assertEquals("尺寸", row.get("3"));
			}

			{
				Map<String, Object> row = data.get(1);

				assertEquals("aaa", row.get("1"));
				assertEquals("11", row.get("2"));
				assertEquals("12", row.get("3"));
			}

			{
				Map<String, Object> row = data.get(2);

				assertEquals("bbb", row.get("1"));
				assertEquals("21", row.get("2"));
				assertEquals("22", row.get("3"));
			}

			{
				Map<String, Object> row = data.get(3);

				assertEquals("ccc", row.get("1"));
				assertEquals("31", row.get("2"));
				assertEquals("32", row.get("3"));
			}
		}
	}
}
