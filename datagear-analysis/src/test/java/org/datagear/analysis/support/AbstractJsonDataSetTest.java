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

import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetField;
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
		List<DataSetField> fields = result.getFields();
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) result.getResult().getData();

		assertEquals(jsonString, result.getTemplateResult());

		{
			assertEquals(3, fields.size());

			{
				DataSetField field = fields.get(0);
				assertEquals("name", field.getName());
				assertEquals(DataSetField.DataType.STRING, field.getType());
			}

			{
				DataSetField field = fields.get(1);
				assertEquals("value", field.getName());
				assertEquals(DataSetField.DataType.NUMBER, field.getType());
			}

			{
				DataSetField field = fields.get(2);
				assertEquals("size", field.getName());
				assertEquals(DataSetField.DataType.NUMBER, field.getType());
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
