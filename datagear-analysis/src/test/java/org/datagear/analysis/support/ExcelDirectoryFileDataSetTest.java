/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.ResolvedDataSetResult;
import org.junit.Test;

/**
 * {@linkplain ExcelDirectoryFileDataSet}单元测试用例。
 * 
 * @author datagear@163.com
 *
 */
public class ExcelDirectoryFileDataSetTest
{
	private static final File DIRECTORY = new File("src/test/resources/org/datagear/analysis/support/");

	@Test
	public void getResultTest_xlsx()
	{
		List<DataSetProperty> properties = new ArrayList<>();
		properties.add(new DataSetProperty("name", DataSetProperty.DataType.STRING));
		properties.add(new DataSetProperty("value", DataSetProperty.DataType.NUMBER));
		properties.add(new DataSetProperty("size", DataSetProperty.DataType.NUMBER));
		properties.add(new DataSetProperty("date", DataSetProperty.DataType.DATE));

		ExcelDirectoryFileDataSet dataSet = new ExcelDirectoryFileDataSet("a", "a", properties, DIRECTORY,
				"ExcelDirectoryFileDataSetTest-0.xlsx");
		dataSet.setNameRow(1);

		@SuppressWarnings("unchecked")
		DataSetResult result = dataSet.getResult(Collections.EMPTY_MAP);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) result.getData();

		{
			assertEquals(3, data.size());

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

			{
				Map<String, Object> row = data.get(0);

				assertEquals("aaa", row.get("name"));
				assertEquals(15, ((Number) row.get("value")).intValue());
				assertEquals(16, ((Number) row.get("size")).intValue());
				assertEquals("2020-08-01", dateFormat.format((Date) row.get("date")));
			}

			{
				Map<String, Object> row = data.get(1);

				assertEquals("bbb", row.get("name"));
				assertEquals(25, ((Number) row.get("value")).intValue());
				assertEquals(26, ((Number) row.get("size")).intValue());
				assertEquals("2020-08-02", dateFormat.format((Date) row.get("date")));
			}

			{
				Map<String, Object> row = data.get(2);

				assertEquals("ccc", row.get("name"));
				assertEquals(35, ((Number) row.get("value")).intValue());
				assertEquals(36, ((Number) row.get("size")).intValue());
				assertEquals("2020-08-03", dateFormat.format((Date) row.get("date")));
			}
		}
	}

	@Test
	public void getResultTest_xlsx_convertPropertyValue()
	{
		List<DataSetProperty> properties = new ArrayList<>();
		properties.add(new DataSetProperty("name", DataSetProperty.DataType.STRING));
		properties.add(new DataSetProperty("value", DataSetProperty.DataType.NUMBER));
		properties.add(new DataSetProperty("size", DataSetProperty.DataType.STRING));
		properties.add(new DataSetProperty("date", DataSetProperty.DataType.STRING));

		ExcelDirectoryFileDataSet dataSet = new ExcelDirectoryFileDataSet("a", "a", properties, DIRECTORY,
				"ExcelDirectoryFileDataSetTest-0.xlsx");
		dataSet.setNameRow(1);

		@SuppressWarnings("unchecked")
		DataSetResult result = dataSet.getResult(Collections.EMPTY_MAP);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) result.getData();

		{
			assertEquals(3, data.size());

			{
				Map<String, Object> row = data.get(0);

				assertEquals("aaa", row.get("name"));
				assertEquals(15, ((Number) row.get("value")).intValue());
				assertEquals("16", row.get("size"));
				assertEquals("2020-08-01", row.get("date"));
			}

			{
				Map<String, Object> row = data.get(1);

				assertEquals("bbb", row.get("name"));
				assertEquals(25, ((Number) row.get("value")).intValue());
				assertEquals("26", row.get("size"));
				assertEquals("2020-08-02", row.get("date"));
			}

			{
				Map<String, Object> row = data.get(2);

				assertEquals("ccc", row.get("name"));
				assertEquals(35, ((Number) row.get("value")).intValue());
				assertEquals("36", row.get("size"));
				assertEquals("2020-08-03", row.get("date"));
			}
		}
	}

	@Test
	public void resolveTest_xlsx()
	{
		ExcelDirectoryFileDataSet dataSet = new ExcelDirectoryFileDataSet("a", "a", DIRECTORY,
				"ExcelDirectoryFileDataSetTest-0.xlsx");
		dataSet.setNameRow(1);

		ResolvedDataSetResult resolvedResult = dataSet.resolve(new HashMap<>());

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) resolvedResult.getResult().getData();
		List<DataSetProperty> properties = resolvedResult.getProperties();

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
				assertEquals(DataSetProperty.DataType.DECIMAL, property.getType());
			}

			{
				DataSetProperty property = properties.get(2);
				assertEquals("size", property.getName());
				assertEquals(DataSetProperty.DataType.DECIMAL, property.getType());
			}

			{
				DataSetProperty property = properties.get(3);
				assertEquals("date", property.getName());
				assertEquals(DataSetProperty.DataType.DATE, property.getType());
			}
		}

		{
			assertEquals(3, data.size());

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

			{
				Map<String, Object> row = data.get(0);

				assertEquals("aaa", row.get("name"));
				assertEquals(15, ((Number) row.get("value")).intValue());
				assertEquals(16, ((Number) row.get("size")).intValue());
				assertEquals("2020-08-01", dateFormat.format((Date) row.get("date")));
			}

			{
				Map<String, Object> row = data.get(1);

				assertEquals("bbb", row.get("name"));
				assertEquals(25, ((Number) row.get("value")).intValue());
				assertEquals(26, ((Number) row.get("size")).intValue());
				assertEquals("2020-08-02", dateFormat.format((Date) row.get("date")));
			}

			{
				Map<String, Object> row = data.get(2);

				assertEquals("ccc", row.get("name"));
				assertEquals(35, ((Number) row.get("value")).intValue());
				assertEquals(36, ((Number) row.get("size")).intValue());
				assertEquals("2020-08-03", dateFormat.format((Date) row.get("date")));
			}
		}
	}

	@Test
	public void resolveTest_xls()
	{
		ExcelDirectoryFileDataSet dataSet = new ExcelDirectoryFileDataSet("a", "a", DIRECTORY,
				"ExcelDirectoryFileDataSetTest-1.xls");
		dataSet.setNameRow(1);

		ResolvedDataSetResult resolvedResult = dataSet.resolve(new HashMap<>());

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) resolvedResult.getResult().getData();
		List<DataSetProperty> properties = resolvedResult.getProperties();

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
				assertEquals(DataSetProperty.DataType.DECIMAL, property.getType());
			}

			{
				DataSetProperty property = properties.get(2);
				assertEquals("size", property.getName());
				assertEquals(DataSetProperty.DataType.DECIMAL, property.getType());
			}

			{
				DataSetProperty property = properties.get(3);
				assertEquals("date", property.getName());
				assertEquals(DataSetProperty.DataType.DATE, property.getType());
			}
		}

		{
			assertEquals(3, data.size());

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

			{
				Map<String, Object> row = data.get(0);

				assertEquals("aaa", row.get("name"));
				assertEquals(15, ((Number) row.get("value")).intValue());
				assertEquals(16, ((Number) row.get("size")).intValue());
				assertEquals("2020-08-01", dateFormat.format((Date) row.get("date")));
			}

			{
				Map<String, Object> row = data.get(1);

				assertEquals("bbb", row.get("name"));
				assertEquals(25, ((Number) row.get("value")).intValue());
				assertEquals(26, ((Number) row.get("size")).intValue());
				assertEquals("2020-08-02", dateFormat.format((Date) row.get("date")));
			}

			{
				Map<String, Object> row = data.get(2);

				assertEquals("ccc", row.get("name"));
				assertEquals(35, ((Number) row.get("value")).intValue());
				assertEquals(36, ((Number) row.get("size")).intValue());
				assertEquals("2020-08-03", dateFormat.format((Date) row.get("date")));
			}
		}
	}

	@Test
	public void resolveTest_dataRowColumnExp()
	{
		ExcelDirectoryFileDataSet dataSet = new ExcelDirectoryFileDataSet("a", "a", DIRECTORY,
				"ExcelDirectoryFileDataSetTest-0.xlsx");
		dataSet.setNameRow(1);
		dataSet.setDataRowExp("2,3-");
		dataSet.setDataColumnExp("A,C-");

		ResolvedDataSetResult resolvedResult = dataSet.resolve(new HashMap<>());

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) resolvedResult.getResult().getData();
		List<DataSetProperty> properties = resolvedResult.getProperties();

		{
			assertEquals(3, properties.size());

			{
				DataSetProperty property = properties.get(0);
				assertEquals("name", property.getName());
				assertEquals(DataSetProperty.DataType.STRING, property.getType());
			}

			{
				DataSetProperty property = properties.get(1);
				assertEquals("size", property.getName());
				assertEquals(DataSetProperty.DataType.DECIMAL, property.getType());
			}

			{
				DataSetProperty property = properties.get(2);
				assertEquals("date", property.getName());
				assertEquals(DataSetProperty.DataType.DATE, property.getType());
			}
		}

		{
			assertEquals(3, data.size());

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

			{
				Map<String, Object> row = data.get(0);

				assertEquals("aaa", row.get("name"));
				assertEquals(16, ((Number) row.get("size")).intValue());
				assertEquals("2020-08-01", dateFormat.format((Date) row.get("date")));
			}

			{
				Map<String, Object> row = data.get(1);

				assertEquals("bbb", row.get("name"));
				assertEquals(26, ((Number) row.get("size")).intValue());
				assertEquals("2020-08-02", dateFormat.format((Date) row.get("date")));
			}

			{
				Map<String, Object> row = data.get(2);

				assertEquals("ccc", row.get("name"));
				assertEquals(36, ((Number) row.get("size")).intValue());
				assertEquals("2020-08-03", dateFormat.format((Date) row.get("date")));
			}
		}
	}
}
