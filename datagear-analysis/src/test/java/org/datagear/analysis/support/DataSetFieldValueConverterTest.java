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
import static org.junit.Assert.assertTrue;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
			Object actual = converter.convert(value, field);
			assertEquals(value, actual);
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
			Object actual = converter.convertValue(null, DataSetField.DataType.BOOLEAN);
			assertNull(actual);
		}
		{
			Object actual = converter.convertValue("true", null);
			assertEquals("true", actual);
		}
		{
			Object actual = converter.convertValue("true", DataSetField.DataType.BOOLEAN);
			assertEquals(Boolean.TRUE, actual);
		}
		{
			Object actual = converter.convertValue(true, DataSetField.DataType.STRING);
			assertEquals("true", actual);
		}
		{
			Object actual = converter.convertValue(3, DataSetField.DataType.STRING);
			assertEquals("3", actual);
		}
		{
			Time time = new Time(System.currentTimeMillis());
			Object actual = converter.convertValue(time, DataSetField.DataType.STRING);
			assertEquals(formatDate(time, format.getTimeFormat()), actual);
		}
		{
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			Object actual = converter.convertValue(timestamp, DataSetField.DataType.STRING);
			assertEquals(formatDate(timestamp, format.getTimestampFormat()), actual);
		}
		{
			Date date = new Date();
			Object actual = converter.convertValue(date, DataSetField.DataType.STRING);
			assertEquals(formatDate(date, format.getDateFormat()), actual);
		}
		{
			int a = 3;
			Object actual = converter.convertValue(a, DataSetField.DataType.UNKNOWN);
			assertEquals(3, actual);
		}
	}

	@Test
	public void convertStringValueTest() throws Throwable
	{
		DataFormat format = new DataFormat();
		DataSetFieldValueConverter converter = new DataSetFieldValueConverter(format);

		{
			String value = "aaa";
			Object actual = converter.convertStringValue(value, DataSetField.DataType.STRING);
			assertEquals(value, actual);
		}

		{
			String value = "aaa";
			Object actual = converter.convertStringValue(value, DataSetField.DataType.UNKNOWN);
			assertEquals(value, actual);
		}

		{
			Object actual = converter.convertStringValue(null, DataSetField.DataType.NUMBER);
			assertNull(actual);
		}
		{
			Object actual = converter.convertStringValue("", DataSetField.DataType.NUMBER);
			assertNull(actual);
		}

		{
			String value = "true";
			Object actual = converter.convertStringValue(value, DataSetField.DataType.BOOLEAN);
			assertEquals(Boolean.TRUE, actual);
		}
		{
			String value = "1";
			Object actual = converter.convertStringValue(value, DataSetField.DataType.BOOLEAN);
			assertEquals(Boolean.TRUE, actual);
		}
		{
			String value = "false";
			Object actual = converter.convertStringValue(value, DataSetField.DataType.BOOLEAN);
			assertEquals(Boolean.FALSE, actual);
		}
		{
			String value = "0";
			Object actual = converter.convertStringValue(value, DataSetField.DataType.BOOLEAN);
			assertEquals(Boolean.FALSE, actual);
		}

		{
			String value = "3.2";
			Object actual = converter.convertStringValue(value, DataSetField.DataType.NUMBER);
			assertEquals(3.2d, ((Number) actual).doubleValue(), 0.1d);
		}

		{
			String value = "3";
			Object actual = converter.convertStringValue(value, DataSetField.DataType.INTEGER);
			assertEquals(3, ((Integer) actual).intValue());
		}
		{
			String value = "3.2";
			Object actual = converter.convertStringValue(value, DataSetField.DataType.INTEGER);
			assertEquals(3, ((Integer) actual).intValue());
		}

		{
			String value = "3.2";
			Object actual = converter.convertStringValue(value, DataSetField.DataType.DECIMAL);
			assertEquals(3.2d, ((Double) actual).doubleValue(), 0.1d);
		}

		{
			String value = "2022-10-24";
			Object actual = converter.convertStringValue(value, DataSetField.DataType.DATE);
			assertEquals(value, formatDate((Date) actual, format.getDateFormat()));
		}

		{
			String value = "11:32:45";
			Object actual = converter.convertStringValue(value, DataSetField.DataType.TIME);
			assertEquals(value, formatDate((Time) actual, format.getTimeFormat()));
		}

		{
			String value = "2022-10-24 11:32:45";
			Object actual = converter.convertStringValue(value, DataSetField.DataType.TIMESTAMP);
			assertEquals(value, formatDate((Timestamp) actual, format.getTimestampFormat()));
		}
	}

	@Test
	public void convertBooleanValueTest() throws Throwable
	{
		DataFormat format = new DataFormat();
		DataSetFieldValueConverter converter = new DataSetFieldValueConverter(format);

		{
			boolean value = true;
			Object actual = converter.convertBooleanValue(value, DataSetField.DataType.BOOLEAN);
			assertEquals(value, actual);
		}

		{
			boolean value = true;
			Object actual = converter.convertBooleanValue(value, DataSetField.DataType.UNKNOWN);
			assertEquals(value, actual);
		}

		{
			Object actual = converter.convertBooleanValue(null, DataSetField.DataType.BOOLEAN);
			assertNull(actual);
		}

		{
			boolean value = true;
			Object actual = converter.convertBooleanValue(value, DataSetField.DataType.STRING);
			assertEquals("true", actual);
		}
		{
			boolean value = false;
			Object actual = converter.convertBooleanValue(value, DataSetField.DataType.STRING);
			assertEquals("false", actual);
		}

		{
			boolean value = true;
			Object actual = converter.convertBooleanValue(value, DataSetField.DataType.NUMBER);
			assertEquals(1, actual);
		}
		{
			boolean value = false;
			Object actual = converter.convertBooleanValue(value, DataSetField.DataType.NUMBER);
			assertEquals(0, actual);
		}

		{
			boolean value = true;
			Object actual = converter.convertBooleanValue(value, DataSetField.DataType.INTEGER);
			assertEquals(1, actual);
		}
		{
			boolean value = false;
			Object actual = converter.convertBooleanValue(value, DataSetField.DataType.INTEGER);
			assertEquals(0, actual);
		}

		{
			boolean value = true;
			Object actual = converter.convertBooleanValue(value, DataSetField.DataType.DECIMAL);
			assertEquals(1, actual);
		}
		{
			boolean value = false;
			Object actual = converter.convertBooleanValue(value, DataSetField.DataType.DECIMAL);
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
			Object actual = converter.convertNumberValue(value, DataSetField.DataType.NUMBER);
			assertEquals(value, actual);
		}

		{
			Number value = 3;
			Object actual = converter.convertNumberValue(value, DataSetField.DataType.UNKNOWN);
			assertEquals(value, actual);
		}

		{
			Object actual = converter.convertNumberValue(null, DataSetField.DataType.NUMBER);
			assertNull(actual);
		}

		{
			Number value = 3;
			Object actual = converter.convertNumberValue(value, DataSetField.DataType.STRING);
			assertEquals("3", actual);
		}

		{
			Number value = 3;
			Object actual = converter.convertNumberValue(value, DataSetField.DataType.BOOLEAN);
			assertEquals(Boolean.TRUE, actual);
		}
		{
			Number value = 0;
			Object actual = converter.convertNumberValue(value, DataSetField.DataType.BOOLEAN);
			assertEquals(Boolean.FALSE, actual);
		}

		{
			Number value = 3;
			Object actual = converter.convertNumberValue(value, DataSetField.DataType.INTEGER);
			assertEquals(3, ((Long) actual).intValue());
		}

		{
			Number value = 3.2d;
			Object actual = converter.convertNumberValue(value, DataSetField.DataType.DECIMAL);
			assertEquals(3.2d, ((Double) actual).doubleValue(), 0.1);
		}

		{
			Number value = new Date().getTime();
			Object actual = converter.convertNumberValue(value, DataSetField.DataType.DATE);
			assertEquals(value.longValue(), ((java.sql.Date) actual).getTime());
		}

		{
			Number value = new Time(System.currentTimeMillis()).getTime();
			Object actual = converter.convertNumberValue(value, DataSetField.DataType.TIME);
			assertEquals(value.longValue(), ((Time) actual).getTime());
		}

		{
			Number value = new Timestamp(System.currentTimeMillis()).getTime();
			Object actual = converter.convertNumberValue(value, DataSetField.DataType.TIMESTAMP);
			assertEquals(value.longValue(), ((Timestamp) actual).getTime());
		}
	}

	@Test
	public void convertDateValueTest() throws Throwable
	{
		DataFormat format = new DataFormat();
		DataSetFieldValueConverter converter = new DataSetFieldValueConverter(format);

		{
			Date value = new Date();
			Object actual = converter.convertDateValue(value, DataSetField.DataType.UNKNOWN);
			assertEquals(value, actual);
		}

		{
			Object actual = converter.convertDateValue(null, DataSetField.DataType.NUMBER);
			assertNull(actual);
		}

		{
			Date value = new Date();
			Object actual = converter.convertDateValue(value, DataSetField.DataType.STRING);
			assertEquals(formatDate(value, format.getDateFormat()), actual);
		}

		{
			Date value = new Date();
			Object actual = converter.convertDateValue(value, DataSetField.DataType.NUMBER);
			assertEquals(value.getTime(), ((Long) actual).longValue());
		}

		{
			Date value = new Date();
			Object actual = converter.convertDateValue(value, DataSetField.DataType.INTEGER);
			assertEquals(value.getTime(), ((Long) actual).longValue());
		}

		{
			Date value = new Date();
			Object actual = converter.convertDateValue(value, DataSetField.DataType.DECIMAL);
			assertEquals(value.getTime(), ((Long) actual).longValue());
		}

		{
			Date value = new Date();
			Object actual = converter.convertDateValue(value, DataSetField.DataType.DATE);
			assertEquals(value.getTime(), ((java.sql.Date) actual).getTime());
		}

		{
			Date value = new Date();
			Object actual = converter.convertDateValue(value, DataSetField.DataType.TIME);
			assertEquals(value.getTime(), ((java.sql.Time) actual).getTime());
		}

		{
			Date value = new Date();
			Object actual = converter.convertDateValue(value, DataSetField.DataType.TIMESTAMP);
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
			Object actual = converter.convertTimeValue(value, DataSetField.DataType.TIME);
			assertEquals(value, actual);
		}

		{
			Time value = new Time(System.currentTimeMillis());
			Object actual = converter.convertTimeValue(value, DataSetField.DataType.UNKNOWN);
			assertEquals(value, actual);
		}

		{
			Object actual = converter.convertTimeValue(null, DataSetField.DataType.NUMBER);
			assertNull(actual);
		}

		{
			Time value = new Time(System.currentTimeMillis());
			Object actual = converter.convertTimeValue(value, DataSetField.DataType.STRING);
			assertEquals(formatDate(value, format.getTimeFormat()), actual);
		}

		{
			Time value = new Time(System.currentTimeMillis());
			Object actual = converter.convertTimeValue(value, DataSetField.DataType.NUMBER);
			assertEquals(value.getTime(), ((Long) actual).longValue());
		}

		{
			Time value = new Time(System.currentTimeMillis());
			Object actual = converter.convertTimeValue(value, DataSetField.DataType.INTEGER);
			assertEquals(value.getTime(), ((Long) actual).longValue());
		}

		{
			Time value = new Time(System.currentTimeMillis());
			Object actual = converter.convertTimeValue(value, DataSetField.DataType.DECIMAL);
			assertEquals(value.getTime(), ((Long) actual).longValue());
		}

		{
			Time value = new Time(System.currentTimeMillis());
			Object actual = converter.convertTimeValue(value, DataSetField.DataType.DATE);
			assertEquals(value.getTime(), ((java.sql.Date) actual).getTime());
		}

		{
			Time value = new Time(System.currentTimeMillis());
			Object actual = converter.convertTimeValue(value, DataSetField.DataType.TIMESTAMP);
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
			Object actual = converter.convertTimestampValue(value, DataSetField.DataType.TIMESTAMP);
			assertEquals(value, actual);
		}

		{
			Timestamp value = new Timestamp(System.currentTimeMillis());
			Object actual = converter.convertTimestampValue(value, DataSetField.DataType.UNKNOWN);
			assertEquals(value, actual);
		}

		{
			Object actual = converter.convertTimestampValue(null, DataSetField.DataType.NUMBER);
			assertNull(actual);
		}

		{
			Timestamp value = new Timestamp(System.currentTimeMillis());
			Object actual = converter.convertTimestampValue(value, DataSetField.DataType.STRING);
			assertEquals(formatDate(value, format.getTimestampFormat()), actual);
		}

		{
			Timestamp value = new Timestamp(System.currentTimeMillis());
			Object actual = converter.convertTimestampValue(value, DataSetField.DataType.NUMBER);
			assertEquals(value.getTime(), ((Long) actual).longValue());
		}

		{
			Timestamp value = new Timestamp(System.currentTimeMillis());
			Object actual = converter.convertTimestampValue(value, DataSetField.DataType.INTEGER);
			assertEquals(value.getTime(), ((Long) actual).longValue());
		}

		{
			Timestamp value = new Timestamp(System.currentTimeMillis());
			Object actual = converter.convertTimestampValue(value, DataSetField.DataType.DECIMAL);
			assertEquals(value.getTime(), ((Long) actual).longValue());
		}

		{
			Timestamp value = new Timestamp(System.currentTimeMillis());
			Object actual = converter.convertTimestampValue(value, DataSetField.DataType.DATE);
			assertEquals(value.getTime(), ((java.sql.Date) actual).getTime());
		}

		{
			Timestamp value = new Timestamp(System.currentTimeMillis());
			Object actual = converter.convertTimestampValue(value, DataSetField.DataType.TIME);
			assertEquals(value.getTime(), ((java.sql.Time) actual).getTime());
		}
	}

	@Test
	public void convertObjectValueTest() throws Throwable
	{
		DataFormat format = new DataFormat();
		DataSetFieldValueConverter converter = new DataSetFieldValueConverter(format);

		{
			Object value = Collections.singletonMap("name", "aaa");
			Object actual = converter.convertObjectValue(value, DataSetField.DataType.OBJECT);
			assertTrue(value == actual);
		}

		{
			Object value = Collections.singletonMap("name", "aaa");
			Object actual = converter.convertObjectValue(value, DataSetField.DataType.OBJECT);
			assertTrue(value == actual);
		}

		{
			Object value = Collections.singletonMap("name", "aaa");
			Object actual = converter.convertObjectValue(value, DataSetField.DataType.STRING);
			assertEquals("{\"name\":\"aaa\"}", actual);
		}
	}

	protected String formatDate(Date date, String format)
	{
		return new SimpleDateFormat(format).format(date);
	}
}
