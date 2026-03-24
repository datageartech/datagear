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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetField;
import org.junit.Test;

/**
 * {@linkplain DataSetFieldValueConverter}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetFieldValueConverterTest
{
	public DataSetFieldValueConverterTest()
	{
		super();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void convertTest_Map_Collection()
	{
		List<DataSetField> fields = Arrays.asList(new DataSetField("name", DataSetField.DataType.STRING),
				new DataSetField("value", DataSetField.DataType.INTEGER),
				new DataSetField("obj", DataSetField.DataType.OBJECT),
				new DataSetField("sizes", DataSetField.DataType.INTEGER),
				new DataSetField("values", DataSetField.DataType.NUMBER),
				new DataSetField("types", DataSetField.DataType.STRING));

		{
			DataSetField field = fields.get(2);
			field.setFields(new ArrayList<>());

			DataSetField f0 = new DataSetField("f0", DataSetField.DataType.INTEGER);
			DataSetField f1 = new DataSetField("f1", DataSetField.DataType.INTEGER);
			DataSetField f2 = new DataSetField("f2", DataSetField.DataType.INTEGER);

			Collections.addAll(field.getFields(), f0, f1, f2);
		}
		{
			DataSetField field = fields.get(3);
			field.setArray(true);
		}
		{
			DataSetField field = fields.get(4);
			field.setArray(true);
		}
		{
			DataSetField field = fields.get(5);
			field.setArray(true);
		}

		DataFormat format = new DataFormat();

		{
			DataSetFieldValueConverter converterStrict = new DataSetFieldValueConverter(format);
			converterStrict.setStrictForMap(true);

			Map<String, Object> value = new HashMap<>();
			value.put("name", "aaa");
			value.put("value", "3");
			Map<String, Object> vobj = new HashMap<>();
			value.put("obj", vobj);
			value.put("inexists", "nonono");
			value.put("sizes", "[1, 2, 3]");
			value.put("values", " [1.5, 3.6] ");
			value.put("types", " ['a','b'] ");

			vobj.put("f0", 3);
			vobj.put("f1", "4");
			vobj.put("f2", "5");

			Map<String, ?> actual = converterStrict.convert(value, fields);
			Map<String, ?> actualObj = (Map<String, ?>) actual.get("obj");
			List<Integer> sizesActual = (List<Integer>) actual.get("sizes");
			List<Number> valuesActual = (List<Number>) actual.get("values");
			List<String> typesActual = (List<String>) actual.get("types");

			assertEquals(6, actual.size());
			assertEquals("aaa", actual.get("name"));
			assertEquals(3, actual.get("value"));

			assertEquals(3, sizesActual.size());
			assertEquals(1, sizesActual.get(0).intValue());
			assertEquals(2, sizesActual.get(1).intValue());
			assertEquals(3, sizesActual.get(2).intValue());

			assertEquals(2, valuesActual.size());
			assertEquals(1.5d, valuesActual.get(0).doubleValue(), 0.1d);
			assertEquals(3.6d, valuesActual.get(1).doubleValue(), 0.1d);

			assertEquals(1, typesActual.size());
			assertEquals(" ['a','b'] ", typesActual.get(0));

			assertEquals(3, actualObj.get("f0"));
			assertEquals(4, actualObj.get("f1"));
			assertEquals(5, actualObj.get("f2"));
		}

		{
			DataSetFieldValueConverter converterStrict = new DataSetFieldValueConverter(format);
			converterStrict.setStrictForMap(true);

			Map<String, Object> value = new HashMap<>();
			value.put("name", "aaa");
			value.put("value", "3");
			value.put("obj", null);
			value.put("inexists", "nonono");

			Map<String, ?> actual = converterStrict.convert(value, fields);

			assertEquals(6, actual.size());
			assertEquals("aaa", actual.get("name"));
			assertEquals(3, actual.get("value"));
			assertEquals(null, actual.get("obj"));
		}

		{
			DataSetFieldValueConverter converterStrict = new DataSetFieldValueConverter(format);
			converterStrict.setStrictForMap(false);

			Map<String, Object> value = new HashMap<>();
			value.put("name", "aaa");
			value.put("value", "3");
			value.put("obj", null);
			value.put("inexists", "nonono");

			Map<String, ?> actual = converterStrict.convert(value, fields);

			assertEquals(7, actual.size());
			assertEquals("aaa", actual.get("name"));
			assertEquals(3, actual.get("value"));
			assertEquals(null, actual.get("obj"));
			assertEquals("nonono", actual.get("inexists"));
		}
	}

	@Test
	public void convertTest_Object_DataSetField()
	{
		DataFormat format = new DataFormat();
		DataSetFieldValueConverter converter = new DataSetFieldValueConverter(format);
		
		{
			String value = "3";
			DataSetField field = new DataSetField("field", DataSetField.DataType.INTEGER);
			Object actual = converter.convert(value, field);
			assertEquals(3, ((Integer) actual).intValue());
		}

		{
			Object value = Collections.singletonMap("name", "aaa");
			DataSetField field = new DataSetField("field", DataSetField.DataType.OBJECT);
			field.setFields(Arrays.asList(new DataSetField("name", DataSetField.DataType.STRING)));
			@SuppressWarnings("unchecked")
			Map<String, ?> actual = (Map<String, ?>) converter.convert(value, field);

			assertEquals(1, actual.size());
			assertEquals("aaa", actual.get("name"));
		}

		{
			Object value = Collections.singletonMap("name", "aaa");
			DataSetField field = new DataSetField("field", DataSetField.DataType.STRING);
			Object actual = converter.convert(value, field);
			assertEquals("{\"name\":\"aaa\"}", actual);
		}

		{
			DataSetField field = new DataSetField("field", DataSetField.DataType.INTEGER);
			field.setArray(true);

			{
				String[] value = new String[] { "3", "4", "5" };

				Object[] actual = (Object[]) converter.convert(value, field);
				assertEquals(3, actual.length);
				assertEquals(3, ((Integer) actual[0]).intValue());
				assertEquals(4, ((Integer) actual[1]).intValue());
				assertEquals(5, ((Integer) actual[2]).intValue());
			}
			
			{
				List<String> value = Arrays.asList("3", "4", "5");

				List<?> actual = (List<?>) converter.convert(value, field);
				assertEquals(3, actual.size());
				assertEquals(3, ((Integer) actual.get(0)).intValue());
				assertEquals(4, ((Integer) actual.get(1)).intValue());
				assertEquals(5, ((Integer) actual.get(2)).intValue());
			}
			
			{
				String value = "3";

				List<?> actual = (List<?>) converter.convert(value, field);
				assertEquals(1, actual.size());
				assertEquals(3, ((Integer) actual.get(0)).intValue());
			}
		}

		{
			DataSetField field = new DataSetField("field", DataSetField.DataType.INTEGER);
			field.setArray(false);

			{
				String[] value = new String[] { "3", "4", "5" };

				Object actual = converter.convert(value, field);
				assertEquals(3, ((Integer) actual).intValue());
			}

			{
				List<String> value = Arrays.asList("3", "4", "5");

				Object actual = converter.convert(value, field);
				assertEquals(3, ((Integer) actual).intValue());
			}

			{
				String value = "3";

				Object actual = converter.convert(value, field);
				assertEquals(3, ((Integer) actual).intValue());
			}
		}
	}

	@Test
	public void convertValueTest()
	{
		DataFormat format = new DataFormat();
		DataSetFieldValueConverter converter = new DataSetFieldValueConverter(format);

		{
			Object actual = converter.convertValue(null, mockFieldForType(DataSetField.DataType.BOOLEAN));
			assertNull(actual);
		}
		{
			Object actual = converter.convertValue("true", null);
			assertEquals("true", actual);
		}
		{
			Object actual = converter.convertValue("true", mockFieldForType(DataSetField.DataType.BOOLEAN));
			assertEquals(Boolean.TRUE, actual);
		}
		{
			Object actual = converter.convertValue(true, mockFieldForType(DataSetField.DataType.STRING));
			assertEquals("true", actual);
		}
		{
			Object actual = converter.convertValue(3, mockFieldForType(DataSetField.DataType.STRING));
			assertEquals("3", actual);
		}
		{
			Time time = new Time(System.currentTimeMillis());
			Object actual = converter.convertValue(time, mockFieldForType(DataSetField.DataType.STRING));
			assertEquals(formatDate(time, format.getTimeFormat()), actual);
		}
		{
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			Object actual = converter.convertValue(timestamp, mockFieldForType(DataSetField.DataType.STRING));
			assertEquals(formatDate(timestamp, format.getTimestampFormat()), actual);
		}
		{
			Date date = new Date();
			Object actual = converter.convertValue(date, mockFieldForType(DataSetField.DataType.STRING));
			assertEquals(formatDate(date, format.getDateFormat()), actual);
		}
		{
			Map<String, ?> value = Collections.singletonMap("name", "aaa");
			Object actual = converter.convertValue(value, mockFieldForType(DataSetField.DataType.UNKNOWN));
			assertTrue(value == actual);
		}
	}

	@Test
	public void convertStringValueTest() throws Throwable
	{
		DataFormat format = new DataFormat();
		DataSetFieldValueConverter converter = new DataSetFieldValueConverter(format);

		{
			String value = "aaa";
			Object actual = converter.convertStringValue(value, mockFieldForType(DataSetField.DataType.STRING));
			assertEquals(value, actual);
		}

		{
			Object actual = converter.convertStringValue(null, mockFieldForType(DataSetField.DataType.NUMBER));
			assertNull(actual);
		}
		{
			Object actual = converter.convertStringValue("", mockFieldForType(DataSetField.DataType.NUMBER));
			assertNull(actual);
		}

		{
			String value = "true";
			Object actual = converter.convertStringValue(value, mockFieldForType(DataSetField.DataType.BOOLEAN));
			assertEquals(Boolean.TRUE, actual);
		}
		{
			String value = "1";
			Object actual = converter.convertStringValue(value, mockFieldForType(DataSetField.DataType.BOOLEAN));
			assertEquals(Boolean.TRUE, actual);
		}
		{
			String value = "false";
			Object actual = converter.convertStringValue(value, mockFieldForType(DataSetField.DataType.BOOLEAN));
			assertEquals(Boolean.FALSE, actual);
		}
		{
			String value = "0";
			Object actual = converter.convertStringValue(value, mockFieldForType(DataSetField.DataType.BOOLEAN));
			assertEquals(Boolean.FALSE, actual);
		}

		{
			String value = "3.2";
			Object actual = converter.convertStringValue(value, mockFieldForType(DataSetField.DataType.NUMBER));
			assertEquals(3.2d, ((Number) actual).doubleValue(), 0.1d);
		}

		{
			String value = "3";
			Object actual = converter.convertStringValue(value, mockFieldForType(DataSetField.DataType.INTEGER));
			assertEquals(3, ((Integer) actual).intValue());
		}
		{
			String value = "3.2";
			Object actual = converter.convertStringValue(value, mockFieldForType(DataSetField.DataType.INTEGER));
			assertEquals(3, ((Integer) actual).intValue());
		}

		{
			String value = "3.2";
			Object actual = converter.convertStringValue(value, mockFieldForType(DataSetField.DataType.NUMBER));
			assertEquals(3.2d, ((Number) actual).doubleValue(), 0.1d);
		}

		{
			String value = "2022-10-24";
			Object actual = converter.convertStringValue(value, mockFieldForType(DataSetField.DataType.DATE));
			assertEquals(value, formatDate((Date) actual, format.getDateFormat()));
		}

		{
			String value = "11:32:45";
			Object actual = converter.convertStringValue(value, mockFieldForType(DataSetField.DataType.TIME));
			assertEquals(value, formatDate((Time) actual, format.getTimeFormat()));
		}

		{
			String value = "2022-10-24 11:32:45";
			Object actual = converter.convertStringValue(value, mockFieldForType(DataSetField.DataType.TIMESTAMP));
			assertEquals(value, formatDate((Timestamp) actual, format.getTimestampFormat()));
		}

		{
			String value = "{name: 'aaa', value: 'vvv'}";
			@SuppressWarnings("unchecked")
			Map<String, ?> actual = (Map<String, ?>)converter.convertStringValue(value, mockFieldForType(DataSetField.DataType.OBJECT));
			assertEquals("aaa", actual.get("name"));
			assertEquals("vvv", actual.get("value"));
		}

		{
			String value = "['a', 'b', 'c']";
			List<?> actual = (List<?>) converter.convertStringValue(value, mockFieldForType(DataSetField.DataType.OBJECT));
			assertEquals("a", actual.get(0));
			assertEquals("b", actual.get(1));
			assertEquals("c", actual.get(2));
		}

		{
			String value = "invalid";
			assertThrows(DataValueConvertionException.class, () ->
			{
				converter.convertStringValue(value, mockFieldForType(DataSetField.DataType.OBJECT));
			});
		}
	}

	@Test
	public void convertBooleanValueTest() throws Throwable
	{
		DataFormat format = new DataFormat();
		DataSetFieldValueConverter converter = new DataSetFieldValueConverter(format);

		{
			boolean value = true;
			Object actual = converter.convertBooleanValue(value, mockFieldForType(DataSetField.DataType.BOOLEAN));
			assertEquals(value, actual);
		}

		{
			Object actual = converter.convertBooleanValue(null, mockFieldForType(DataSetField.DataType.BOOLEAN));
			assertNull(actual);
		}

		{
			boolean value = true;
			Object actual = converter.convertBooleanValue(value, mockFieldForType(DataSetField.DataType.STRING));
			assertEquals("true", actual);
		}
		{
			boolean value = false;
			Object actual = converter.convertBooleanValue(value, mockFieldForType(DataSetField.DataType.STRING));
			assertEquals("false", actual);
		}

		{
			boolean value = true;
			Object actual = converter.convertBooleanValue(value, mockFieldForType(DataSetField.DataType.NUMBER));
			assertEquals(1, actual);
		}
		{
			boolean value = false;
			Object actual = converter.convertBooleanValue(value, mockFieldForType(DataSetField.DataType.NUMBER));
			assertEquals(0, actual);
		}

		{
			boolean value = true;
			Object actual = converter.convertBooleanValue(value, mockFieldForType(DataSetField.DataType.INTEGER));
			assertEquals(1, actual);
		}
		{
			boolean value = false;
			Object actual = converter.convertBooleanValue(value, mockFieldForType(DataSetField.DataType.INTEGER));
			assertEquals(0, actual);
		}

		{
			boolean value = true;
			Object actual = converter.convertBooleanValue(value, mockFieldForType(DataSetField.DataType.NUMBER));
			assertEquals(1, actual);
		}
		{
			boolean value = false;
			Object actual = converter.convertBooleanValue(value, mockFieldForType(DataSetField.DataType.NUMBER));
			assertEquals(0, actual);
		}
	}

	@Test
	public void convertNumberValueTest() throws Throwable
	{
		DataFormat format = new DataFormat();
		DataSetFieldValueConverter converter = new DataSetFieldValueConverter(format);

		{
			Number value = 3;
			Object actual = converter.convertNumberValue(value, mockFieldForType(DataSetField.DataType.NUMBER));
			assertEquals(value, actual);
		}

		{
			Object actual = converter.convertNumberValue(null, mockFieldForType(DataSetField.DataType.NUMBER));
			assertNull(actual);
		}

		{
			Number value = 3;
			Object actual = converter.convertNumberValue(value, mockFieldForType(DataSetField.DataType.STRING));
			assertEquals("3", actual);
		}

		{
			Number value = 3;
			Object actual = converter.convertNumberValue(value, mockFieldForType(DataSetField.DataType.BOOLEAN));
			assertEquals(Boolean.TRUE, actual);
		}
		{
			Number value = 0;
			Object actual = converter.convertNumberValue(value, mockFieldForType(DataSetField.DataType.BOOLEAN));
			assertEquals(Boolean.FALSE, actual);
		}

		{
			Number value = 3;
			Object actual = converter.convertNumberValue(value, mockFieldForType(DataSetField.DataType.INTEGER));
			assertEquals(3, ((Integer) actual).intValue());
		}

		{
			Number value = 3.2d;
			Object actual = converter.convertNumberValue(value, mockFieldForType(DataSetField.DataType.NUMBER));
			assertEquals(3.2d, ((Number) actual).doubleValue(), 0.1);
		}

		{
			Number value = new Date().getTime();
			Object actual = converter.convertNumberValue(value, mockFieldForType(DataSetField.DataType.DATE));
			assertEquals(value.longValue(), ((java.sql.Date) actual).getTime());
		}

		{
			Number value = new Time(System.currentTimeMillis()).getTime();
			Object actual = converter.convertNumberValue(value, mockFieldForType(DataSetField.DataType.TIME));
			assertEquals(value.longValue(), ((Time) actual).getTime());
		}

		{
			Number value = new Timestamp(System.currentTimeMillis()).getTime();
			Object actual = converter.convertNumberValue(value, mockFieldForType(DataSetField.DataType.TIMESTAMP));
			assertEquals(value.longValue(), ((Timestamp) actual).getTime());
		}
	}

	@Test
	public void convertDateValueTest() throws Throwable
	{
		DataFormat format = new DataFormat();
		DataSetFieldValueConverter converter = new DataSetFieldValueConverter(format);

		{
			Object actual = converter.convertDateValue(null, mockFieldForType(DataSetField.DataType.NUMBER));
			assertNull(actual);
		}

		{
			Date value = new Date();
			Object actual = converter.convertDateValue(value, mockFieldForType(DataSetField.DataType.STRING));
			assertEquals(formatDate(value, format.getDateFormat()), actual);
		}

		{
			Date value = new Date();
			Object actual = converter.convertDateValue(value, mockFieldForType(DataSetField.DataType.NUMBER));
			assertEquals(value.getTime(), ((Long) actual).longValue());
		}

		{
			Date value = new Date();
			Object actual = converter.convertDateValue(value, mockFieldForType(DataSetField.DataType.INTEGER));
			assertEquals(value.getTime(), ((Long) actual).longValue());
		}

		{
			Date value = new Date();
			Object actual = converter.convertDateValue(value, mockFieldForType(DataSetField.DataType.NUMBER));
			assertEquals(value.getTime(), ((Long) actual).longValue());
		}

		{
			Date value = new Date();
			Object actual = converter.convertDateValue(value, mockFieldForType(DataSetField.DataType.DATE));
			assertEquals(value.getTime(), ((java.sql.Date) actual).getTime());
		}

		{
			Date value = new Date();
			Object actual = converter.convertDateValue(value, mockFieldForType(DataSetField.DataType.TIME));
			assertEquals(value.getTime(), ((java.sql.Time) actual).getTime());
		}

		{
			Date value = new Date();
			Object actual = converter.convertDateValue(value, mockFieldForType(DataSetField.DataType.TIMESTAMP));
			assertEquals(value.getTime(), ((java.sql.Timestamp) actual).getTime());
		}
	}

	@Test
	public void convertTimeValueTest() throws Throwable
	{
		DataFormat format = new DataFormat();
		DataSetFieldValueConverter converter = new DataSetFieldValueConverter(format);

		{
			Time value = new Time(System.currentTimeMillis());
			Object actual = converter.convertTimeValue(value, mockFieldForType(DataSetField.DataType.TIME));
			assertEquals(value, actual);
		}

		{
			Object actual = converter.convertTimeValue(null, mockFieldForType(DataSetField.DataType.NUMBER));
			assertNull(actual);
		}

		{
			Time value = new Time(System.currentTimeMillis());
			Object actual = converter.convertTimeValue(value, mockFieldForType(DataSetField.DataType.STRING));
			assertEquals(formatDate(value, format.getTimeFormat()), actual);
		}

		{
			Time value = new Time(System.currentTimeMillis());
			Object actual = converter.convertTimeValue(value, mockFieldForType(DataSetField.DataType.NUMBER));
			assertEquals(value.getTime(), ((Long) actual).longValue());
		}

		{
			Time value = new Time(System.currentTimeMillis());
			Object actual = converter.convertTimeValue(value, mockFieldForType(DataSetField.DataType.INTEGER));
			assertEquals(value.getTime(), ((Long) actual).longValue());
		}

		{
			Time value = new Time(System.currentTimeMillis());
			Object actual = converter.convertTimeValue(value, mockFieldForType(DataSetField.DataType.NUMBER));
			assertEquals(value.getTime(), ((Long) actual).longValue());
		}

		{
			Time value = new Time(System.currentTimeMillis());
			Object actual = converter.convertTimeValue(value, mockFieldForType(DataSetField.DataType.DATE));
			assertEquals(value.getTime(), ((java.sql.Date) actual).getTime());
		}

		{
			Time value = new Time(System.currentTimeMillis());
			Object actual = converter.convertTimeValue(value, mockFieldForType(DataSetField.DataType.TIMESTAMP));
			assertEquals(value.getTime(), ((java.sql.Timestamp) actual).getTime());
		}
	}

	@Test
	public void convertTimestampValueTest() throws Throwable
	{
		DataFormat format = new DataFormat();
		DataSetFieldValueConverter converter = new DataSetFieldValueConverter(format);

		{
			Timestamp value = new Timestamp(System.currentTimeMillis());
			Object actual = converter.convertTimestampValue(value, mockFieldForType(DataSetField.DataType.TIMESTAMP));
			assertEquals(value, actual);
		}

		{
			Object actual = converter.convertTimestampValue(null, mockFieldForType(DataSetField.DataType.NUMBER));
			assertNull(actual);
		}

		{
			Timestamp value = new Timestamp(System.currentTimeMillis());
			Object actual = converter.convertTimestampValue(value, mockFieldForType(DataSetField.DataType.STRING));
			assertEquals(formatDate(value, format.getTimestampFormat()), actual);
		}

		{
			Timestamp value = new Timestamp(System.currentTimeMillis());
			Object actual = converter.convertTimestampValue(value, mockFieldForType(DataSetField.DataType.NUMBER));
			assertEquals(value.getTime(), ((Long) actual).longValue());
		}

		{
			Timestamp value = new Timestamp(System.currentTimeMillis());
			Object actual = converter.convertTimestampValue(value, mockFieldForType(DataSetField.DataType.INTEGER));
			assertEquals(value.getTime(), ((Long) actual).longValue());
		}

		{
			Timestamp value = new Timestamp(System.currentTimeMillis());
			Object actual = converter.convertTimestampValue(value, mockFieldForType(DataSetField.DataType.NUMBER));
			assertEquals(value.getTime(), ((Long) actual).longValue());
		}

		{
			Timestamp value = new Timestamp(System.currentTimeMillis());
			Object actual = converter.convertTimestampValue(value, mockFieldForType(DataSetField.DataType.DATE));
			assertEquals(value.getTime(), ((java.sql.Date) actual).getTime());
		}

		{
			Timestamp value = new Timestamp(System.currentTimeMillis());
			Object actual = converter.convertTimestampValue(value, mockFieldForType(DataSetField.DataType.TIME));
			assertEquals(value.getTime(), ((java.sql.Time) actual).getTime());
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void convertMapValueTest() throws Throwable
	{
		DataFormat format = new DataFormat();

		DataSetField field = mockFieldForType(DataSetField.DataType.OBJECT);
		field.setFields(Arrays.asList(new DataSetField("name", DataSetField.DataType.STRING),
				new DataSetField("value", DataSetField.DataType.INTEGER)));

		{
			DataSetFieldValueConverter converter = new DataSetFieldValueConverter(format);
			converter.setStrictForMap(true);

			{
				Map<String, Object> value = new HashMap<>();
				value.put("name", "aaa");
				value.put("value", "3");
				value.put("inexists", "inexists");

				Map<String, ?> actual = (Map<String, ?>) converter.convertMapValue(value, field);

				assertEquals(2, actual.size());
				assertEquals("aaa", actual.get("name"));
				assertEquals(3, actual.get("value"));
			}

			{
				Map<?, ?> value = Collections.singletonMap("name", "aaa");
				Object actual = converter.convertMapValue(value, mockFieldForType(DataSetField.DataType.STRING));
				assertEquals("{\"name\":\"aaa\"}", actual);
			}

			{
				Map<?, ?> value = Collections.singletonMap("name", "aaa");
				assertThrows(DataValueConvertionException.class, () ->
				{
					converter.convertMapValue(value, mockFieldForType(DataSetField.DataType.NUMBER));
				});
			}
		}

		{
			DataSetFieldValueConverter converter = new DataSetFieldValueConverter(format);
			converter.setStrictForMap(false);

			{
				Map<String, Object> value = new HashMap<>();
				value.put("name", "aaa");
				value.put("value", "3");
				value.put("inexists", "nonono");

				Map<String, ?> actual = (Map<String, ?>) converter.convertMapValue(value, field);

				assertEquals(3, actual.size());
				assertEquals("aaa", actual.get("name"));
				assertEquals(3, actual.get("value"));
				assertEquals("nonono", actual.get("inexists"));
			}

			{
				Map<?, ?> value = Collections.singletonMap("name", "aaa");
				Object actual = converter.convertMapValue(value, mockFieldForType(DataSetField.DataType.STRING));
				assertEquals("{\"name\":\"aaa\"}", actual);
			}

			{
				Map<?, ?> value = Collections.singletonMap("name", "aaa");
				assertThrows(DataValueConvertionException.class, () ->
				{
					converter.convertMapValue(value, mockFieldForType(DataSetField.DataType.NUMBER));
				});
			}
		}
	}

	protected String formatDate(Date date, String format)
	{
		return new SimpleDateFormat(format).format(date);
	}

	protected DataSetField mockFieldForType(String type)
	{
		return new DataSetField("mock-name", type);
	}
}
