/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.util.FileUtil;
import org.junit.Test;

/**
 * {@linkplain JsonDirectoryFileDataSet}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class JsonDirectoryFileDataSetTest
{
	private static final File DIRECTORY = FileUtil.getFile("src/test/resources/org/datagear/analysis/support/");

	@Test
	public void getResultTest()
	{
		List<DataSetProperty> properties = new ArrayList<>();
		properties.add(new DataSetProperty("name", DataSetProperty.DataType.STRING));
		properties.add(new DataSetProperty("value", DataSetProperty.DataType.NUMBER));
		properties.add(new DataSetProperty("尺寸", DataSetProperty.DataType.NUMBER));
		properties.add(new DataSetProperty("date", DataSetProperty.DataType.STRING));

		JsonDirectoryFileDataSet dataSet = new JsonDirectoryFileDataSet("a", "a", properties, DIRECTORY,
				"JsonDirectoryFileDataSetTest-0.json");

		DataSetResult result = dataSet.getResult(DataSetQuery.valueOf());
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) result.getData();

		{
			assertEquals(3, data.size());

			{
				Map<String, Object> row = data.get(0);

				assertEquals("aaa", row.get("name"));
				assertEquals(11, ((Number) row.get("value")).intValue());
				assertEquals(12, ((Number) row.get("尺寸")).intValue());
				assertEquals("2020-08-01", row.get("date"));
			}

			{
				Map<String, Object> row = data.get(1);

				assertEquals("bbb", row.get("name"));
				assertEquals(21, ((Number) row.get("value")).intValue());
				assertEquals(22, ((Number) row.get("尺寸")).intValue());
				assertEquals("2020-08-02", row.get("date"));
			}

			{
				Map<String, Object> row = data.get(2);

				assertEquals("ccc", row.get("name"));
				assertEquals(31, ((Number) row.get("value")).intValue());
				assertEquals(32, ((Number) row.get("尺寸")).intValue());
				assertEquals("2020-08-03", row.get("date"));
			}
		}
	}

	@Test
	public void getResultTest_convertPropertyValue()
	{
		List<DataSetProperty> properties = new ArrayList<>();
		properties.add(new DataSetProperty("name", DataSetProperty.DataType.STRING));
		properties.add(new DataSetProperty("value", DataSetProperty.DataType.NUMBER));
		properties.add(new DataSetProperty("尺寸", DataSetProperty.DataType.NUMBER));
		properties.add(new DataSetProperty("date", DataSetProperty.DataType.DATE));

		JsonDirectoryFileDataSet dataSet = new JsonDirectoryFileDataSet("a", "a", properties, DIRECTORY,
				"JsonDirectoryFileDataSetTest-0.json");

		DataSetResult result = dataSet.getResult(DataSetQuery.valueOf());
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) result.getData();

		{
			assertEquals(3, data.size());

			SimpleDateFormat dateFormat = new SimpleDateFormat(DataFormat.DEFAULT_DATE_FORMAT);

			{
				Map<String, Object> row = data.get(0);

				assertEquals("aaa", row.get("name"));
				assertEquals(11, ((Number) row.get("value")).intValue());
				assertEquals(12, ((Number) row.get("尺寸")).intValue());
				assertEquals("2020-08-01", dateFormat.format(((Date) row.get("date"))));
			}

			{
				Map<String, Object> row = data.get(1);

				assertEquals("bbb", row.get("name"));
				assertEquals(21, ((Number) row.get("value")).intValue());
				assertEquals(22, ((Number) row.get("尺寸")).intValue());
				assertEquals("2020-08-02", dateFormat.format(((Date) row.get("date"))));
			}

			{
				Map<String, Object> row = data.get(2);

				assertEquals("ccc", row.get("name"));
				assertEquals(31, ((Number) row.get("value")).intValue());
				assertEquals(32, ((Number) row.get("尺寸")).intValue());
				assertEquals("2020-08-03", dateFormat.format(((Date) row.get("date"))));
			}
		}
	}

	@Test
	public void resolveTest()
	{
		JsonDirectoryFileDataSet dataSet = new JsonDirectoryFileDataSet("a", "a", DIRECTORY,
				"JsonDirectoryFileDataSetTest-0.json");

		ResolvedDataSetResult result = dataSet.resolve(DataSetQuery.valueOf());
		List<DataSetProperty> properties = result.getProperties();
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) result.getResult().getData();

		{
			assertEquals(4, properties.size());

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

			{
				DataSetProperty property = properties.get(3);
				assertEquals("date", property.getName());
				assertEquals(DataSetProperty.DataType.STRING, property.getType());
			}
		}

		{
			assertEquals(3, data.size());

			{
				Map<String, Object> row = data.get(0);

				assertEquals("aaa", row.get("name"));
				assertEquals(11, ((Number) row.get("value")).intValue());
				assertEquals(12, ((Number) row.get("尺寸")).intValue());
				assertEquals("2020-08-01", row.get("date"));
			}

			{
				Map<String, Object> row = data.get(1);

				assertEquals("bbb", row.get("name"));
				assertEquals(21, ((Number) row.get("value")).intValue());
				assertEquals(22, ((Number) row.get("尺寸")).intValue());
				assertEquals("2020-08-02", row.get("date"));
			}

			{
				Map<String, Object> row = data.get(2);

				assertEquals("ccc", row.get("name"));
				assertEquals(31, ((Number) row.get("value")).intValue());
				assertEquals(32, ((Number) row.get("尺寸")).intValue());
				assertEquals("2020-08-03", row.get("date"));
			}
		}
	}
}
