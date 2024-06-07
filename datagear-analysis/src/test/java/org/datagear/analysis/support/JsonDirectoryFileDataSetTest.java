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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetField;
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
		List<DataSetField> fields = new ArrayList<>();
		fields.add(new DataSetField("name", DataSetField.DataType.STRING));
		fields.add(new DataSetField("value", DataSetField.DataType.NUMBER));
		fields.add(new DataSetField("尺寸", DataSetField.DataType.NUMBER));
		fields.add(new DataSetField("date", DataSetField.DataType.STRING));

		JsonDirectoryFileDataSet dataSet = new JsonDirectoryFileDataSet("a", "a", fields, DIRECTORY,
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
		List<DataSetField> fields = new ArrayList<>();
		fields.add(new DataSetField("name", DataSetField.DataType.STRING));
		fields.add(new DataSetField("value", DataSetField.DataType.NUMBER));
		fields.add(new DataSetField("尺寸", DataSetField.DataType.NUMBER));
		fields.add(new DataSetField("date", DataSetField.DataType.DATE));

		JsonDirectoryFileDataSet dataSet = new JsonDirectoryFileDataSet("a", "a", fields, DIRECTORY,
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
		List<DataSetField> fields = result.getFields();
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) result.getResult().getData();

		{
			assertEquals(4, fields.size());

			{
				DataSetField property = fields.get(0);
				assertEquals("name", property.getName());
				assertEquals(DataSetField.DataType.STRING, property.getType());
			}

			{
				DataSetField property = fields.get(1);
				assertEquals("value", property.getName());
				assertEquals(DataSetField.DataType.NUMBER, property.getType());
			}

			{
				DataSetField property = fields.get(2);
				assertEquals("尺寸", property.getName());
				assertEquals(DataSetField.DataType.NUMBER, property.getType());
			}

			{
				DataSetField property = fields.get(3);
				assertEquals("date", property.getName());
				assertEquals(DataSetField.DataType.STRING, property.getType());
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
