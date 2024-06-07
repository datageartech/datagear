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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetField;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.ResolvedDataSetResult;
import org.junit.Test;

/**
 * {@linkplain AbstractResolvableDataSet}单元测试用例。
 * 
 * @author datagear@163.com
 *
 */
public class AbstractResolvableDataSetTest
{
	@Test
	public void mergeDataSetFieldsTest()
	{
		TestAbstractResolvableDataSet dataSet = new TestAbstractResolvableDataSet();

		DataSetField idMerged = new DataSetField("id", DataSetField.DataType.INTEGER);
		idMerged.setLabel("Label-id");
		idMerged.setDefaultValue("41");
		DataSetField nameMerged = new DataSetField("name", DataSetField.DataType.STRING);
		nameMerged.setLabel("Label-name");
		DataSetField dateMerged = new DataSetField("date", DataSetField.DataType.DATE);
		nameMerged.setLabel("Label-date");
		DataSetField valueMerged = new DataSetField("value", DataSetField.DataType.NUMBER);
		valueMerged.setLabel("Label-value");

		List<DataSetField> fieldsMerged = Arrays.asList(idMerged, nameMerged, dateMerged, valueMerged);

		DataSetField id = new DataSetField("id", DataSetField.DataType.UNKNOWN);
		DataSetField date = new DataSetField("date", DataSetField.DataType.UNKNOWN);
		DataSetField name = new DataSetField("name", DataSetField.DataType.UNKNOWN);

		List<DataSetField> fields = new ArrayList<DataSetField>();
		Collections.addAll(fields, id, name, date);

		fields = dataSet.mergeFields(fields, fieldsMerged);

		assertEquals(4, fields.size());

		DataSetField p0 = fields.get(0);
		DataSetField p1 = fields.get(1);
		DataSetField p2 = fields.get(2);
		DataSetField p3 = fields.get(3);

		assertEquals(idMerged.getName(), p0.getName());
		assertEquals(idMerged.getType(), p0.getType());
		assertEquals(idMerged.getLabel(), p0.getLabel());
		assertEquals(idMerged.getDefaultValue(), p0.getDefaultValue());

		assertEquals(nameMerged.getName(), p1.getName());
		assertEquals(nameMerged.getType(), p1.getType());
		assertEquals(nameMerged.getLabel(), p1.getLabel());

		assertEquals(dateMerged.getName(), p2.getName());
		assertEquals(dateMerged.getType(), p2.getType());
		assertEquals(dateMerged.getLabel(), p2.getLabel());

		assertEquals(valueMerged.getName(), p3.getName());
		assertEquals(valueMerged.getType(), p3.getType());
		assertEquals(valueMerged.getLabel(), p3.getLabel());
	}

	private static class TestAbstractResolvableDataSet extends AbstractResolvableDataSet
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected ResolvedDataSetResult resolveResult(DataSetQuery query, boolean resolveFields)
				throws DataSetException
		{
			throw new UnsupportedOperationException();
		}
	}
}
