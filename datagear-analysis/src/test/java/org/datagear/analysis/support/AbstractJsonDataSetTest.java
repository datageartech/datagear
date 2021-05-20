/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetQuery;
import org.junit.Test;

/**
 * {@linkplain AbstractJsonDataSet}单元测试用例。
 * 
 * @author datagear@163.com
 *
 */
public class AbstractJsonDataSetTest
{
	@Test
	public void resolveTest_dataJsonPath()
	{
		String jsonString = "{ path0: { path1: [ { path2: [ { name:'aaa', value: 11, size: 12 } ] } ] } }";

		JsonValueDataSet dataSet = new JsonValueDataSet(JsonValueDataSet.class.getSimpleName(),
				JsonValueDataSet.class.getSimpleName(), jsonString);

		dataSet.setDataJsonPath("path0.path1[0].path2");

		TemplateResolvedDataSetResult result = dataSet.resolve(new DataSetQuery());
		List<DataSetProperty> properties = result.getProperties();
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) result.getResult().getData();

		assertEquals(jsonString, result.getTemplateResult());

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
