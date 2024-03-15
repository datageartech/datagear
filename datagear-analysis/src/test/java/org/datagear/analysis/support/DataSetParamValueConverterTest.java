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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetQuery;
import org.junit.Test;

/**
 * {@linkplain DataSetParamValueConverter}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetParamValueConverterTest
{
	protected DataSet dataSet;

	public DataSetParamValueConverterTest()
	{
		super();

		List<DataSetProperty> properties = new ArrayList<>();
		properties.add(new DataSetProperty("name", DataSetProperty.DataType.STRING));

		List<DataSetParam> params = new ArrayList<>();
		params.add(new DataSetParam("name", DataSetParam.DataType.STRING, true));
		params.add(new DataSetParam("size", DataSetParam.DataType.NUMBER, true));
		params.add(new DataSetParam("enable", DataSetParam.DataType.BOOLEAN, true));

		CsvValueDataSet dataSet = new CsvValueDataSet("test", "test", properties, "name \n aaa");
		dataSet.setParams(params);
		dataSet.setNameRow(1);

		this.dataSet = dataSet;
	}

	@Test
	public void convertTest_DataSetQuery_DataSet_boolean()
	{
		DataSetParamValueConverter converter = new DataSetParamValueConverter();

		{
			DataSetQuery actual = converter.convert(null, dataSet, false);
			assertNull(actual);
		}

		{
			DataSetQuery actual = converter.convert(null, dataSet, true);
			assertNotNull(actual);
			assertTrue(actual.getParamValues().isEmpty());
		}

		{
			DataSetQuery query = new DataSetQuery();
			query.setParamValue("name", "aaa");
			query.setParamValue("size", "3");
			query.setParamValue("enable", "true");
			query.setParamValue("other0", 5);
			query.setParamValue("other1", "false");

			DataSetQuery actual = converter.convert(query, dataSet, true);

			assertEquals("aaa", actual.getParamValue("name"));
			assertEquals(3, ((Integer) actual.getParamValue("size")).intValue());
			assertEquals(true, ((Boolean) actual.getParamValue("enable")).booleanValue());
			assertEquals(5, ((Integer) actual.getParamValue("other0")).intValue());
			assertEquals("false", actual.getParamValue("other1"));
		}

		{
			DataSetQuery query = new DataSetQuery();
			query.setParamValue("name", new String[] { "aaa", "bbb" });
			query.setParamValue("size", new String[] { "3", "4" });
			query.setParamValue("enable", new String[] { "true", "false" });
			query.setParamValue("other0", new int[] { 5, 6 });
			query.setParamValue("other1", new boolean[] { false, false });

			DataSetQuery actual = converter.convert(query, dataSet, true);
			Object[] actualName = actual.getParamValue("name");
			Object[] actualSize = actual.getParamValue("size");
			Object[] actualEnable = actual.getParamValue("enable");
			int[] actualOther0 = actual.getParamValue("other0");
			boolean[] actualOther1 = actual.getParamValue("other1");

			assertEquals(2, actualName.length);
			assertEquals("aaa", actualName[0]);
			assertEquals("bbb", actualName[1]);

			assertEquals(2, actualSize.length);
			assertEquals(3, ((Integer) actualSize[0]).intValue());
			assertEquals(4, ((Integer) actualSize[1]).intValue());

			assertEquals(2, actualEnable.length);
			assertEquals(true, ((Boolean) actualEnable[0]).booleanValue());
			assertEquals(false, ((Boolean) actualEnable[1]).booleanValue());

			assertEquals(2, actualOther0.length);
			assertEquals(5, actualOther0[0]);
			assertEquals(6, actualOther0[1]);

			assertEquals(2, actualOther1.length);
			assertEquals(false, actualOther1[0]);
			assertEquals(false, actualOther1[1]);
		}
	}
}
