/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetResult;
import org.junit.Test;

/**
 * {@linkplain CsvValueDataSet}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class CsvValueDataSetTest
{
	@Test
	public void getResultTest_hasParam()
	{
		List<DataSetProperty> properties = new ArrayList<>();
		properties.add(new DataSetProperty("name", DataSetProperty.DataType.STRING));
		properties.add(new DataSetProperty("value", DataSetProperty.DataType.NUMBER));
		properties.add(new DataSetProperty("size", DataSetProperty.DataType.NUMBER));

		List<DataSetParam> params = new ArrayList<>();
		params.add(new DataSetParam("size", DataSetParam.DataType.NUMBER, true));

		CsvValueDataSet dataSet = new CsvValueDataSet("a", "a", properties, "name, value, size \n aaa, 11, ${size}");
		dataSet.setParams(params);
		dataSet.setNameRow(1);

		Map<String, Object> paramValues = new HashMap<>();
		paramValues.put("size", 12);

		DataSetResult result = dataSet.getResult(paramValues);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) result.getData();

		{
			assertEquals(1, data.size());

			{
				Map<String, Object> row = data.get(0);

				assertEquals("aaa", row.get("name"));
				assertEquals(11, ((Number) row.get("value")).intValue());
				assertEquals(12, ((Number) row.get("size")).intValue());
			}
		}
	}

	@Test
	public void getResultTest_hasParam_convertPropertyValue()
	{
		List<DataSetProperty> properties = new ArrayList<>();
		properties.add(new DataSetProperty("name", DataSetProperty.DataType.STRING));
		properties.add(new DataSetProperty("value", DataSetProperty.DataType.NUMBER));
		properties.add(new DataSetProperty("size", DataSetProperty.DataType.STRING));

		List<DataSetParam> params = new ArrayList<>();
		params.add(new DataSetParam("size", DataSetParam.DataType.NUMBER, true));

		CsvValueDataSet dataSet = new CsvValueDataSet("a", "a", properties, "name, value, size \n aaa, 11, ${size}");
		dataSet.setParams(params);
		dataSet.setNameRow(1);

		Map<String, Object> paramValues = new HashMap<>();
		paramValues.put("size", 12);

		DataSetResult result = dataSet.getResult(paramValues);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) result.getData();

		{
			assertEquals(1, data.size());

			{
				Map<String, Object> row = data.get(0);

				assertEquals("aaa", row.get("name"));
				assertEquals(11, ((Number) row.get("value")).intValue());
				assertEquals("12", row.get("size"));
			}
		}
	}

	@Test
	public void resolveTest_hasParam()
	{
		List<DataSetParam> params = new ArrayList<>();
		params.add(new DataSetParam("size", DataSetParam.DataType.NUMBER, true));

		CsvValueDataSet dataSet = new CsvValueDataSet("a", "a", "name, value, size \n aaa, 11, ${size}");
		dataSet.setParams(params);
		dataSet.setNameRow(1);

		Map<String, Object> paramValues = new HashMap<>();
		paramValues.put("size", 12);

		TemplateResolvedDataSetResult result = dataSet.resolve(paramValues, null);
		List<DataSetProperty> properties = result.getProperties();
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) result.getResult().getData();

		assertEquals("name, value, size \n aaa, 11, 12", result.getTemplateResult());

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
				assertEquals("size", property.getName());
				assertEquals(DataSetProperty.DataType.NUMBER, property.getType());
			}
		}

		{
			assertEquals(1, data.size());

			{
				Map<String, Object> row = data.get(0);

				assertEquals("aaa", row.get("name"));
				assertEquals(11, ((Number) row.get("value")).intValue());
				assertEquals(12, ((Number) row.get("size")).intValue());
			}
		}
	}
}
