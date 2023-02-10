/*
 * Copyright 2018-2023 datagear.tech
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.ResultDataFormat;
import org.junit.Test;

/**
 * {@linkplain AbstractDataSet}单元测试用例。
 * 
 * @author datagear@163.com
 *
 */
public class AbstractDataSetTest
{
	@Test
	public void resolveResultDataTest_dataFormat_default() throws Throwable
	{
		TestAbstractDataSet dataSet = new TestAbstractDataSet();

		DataFormat dataFormat = dataSet.createDataSetPropertyValueConverter().getDataFormat();
		SimpleDateFormat dateFormat = new SimpleDateFormat(dataFormat.getDateFormat());
		SimpleDateFormat timeFormat = new SimpleDateFormat(dataFormat.getTimeFormat());
		SimpleDateFormat timestampFormat = new SimpleDateFormat(dataFormat.getTimestampFormat());

		List<Map<String, Object>> rawData = new ArrayList<Map<String, Object>>();

		Map<String, Object> raw0 = new HashMap<String, Object>();
		raw0.put("number", "4.1");
		raw0.put("date", "2021-01-01");
		raw0.put("time", "14:41:41");
		raw0.put("timestamp", "2021-01-01 14:41:41");

		Map<String, Object> raw1 = new HashMap<String, Object>();
		raw1.put("number", "4.2");
		raw1.put("date", "2021-01-02");
		raw1.put("time", "14:41:42");
		raw1.put("timestamp", "2021-01-01 14:41:42");

		Collections.addAll(rawData, raw0, raw1);

		List<DataSetProperty> properties = new ArrayList<DataSetProperty>();
		{
			DataSetProperty p0 = new DataSetProperty("number", DataSetProperty.DataType.NUMBER);
			DataSetProperty p1 = new DataSetProperty("date", DataSetProperty.DataType.DATE);
			DataSetProperty p2 = new DataSetProperty("time", DataSetProperty.DataType.TIME);
			DataSetProperty p3 = new DataSetProperty("timestamp", DataSetProperty.DataType.TIMESTAMP);

			Collections.addAll(properties, p0, p1, p2, p3);
		}

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> resultData = (List<Map<String, Object>>) dataSet.resolveResultData(rawData,
				properties, -1, null);

		assertEquals(rawData.size(), resultData.size());

		Map<String, Object> re0 = resultData.get(0);

		assertEquals(raw0.get("number"), ((Double) re0.get("number")).toString());
		assertEquals(raw0.get("date"), dateFormat.format(((Date) re0.get("date"))));
		assertEquals(raw0.get("time"), timeFormat.format(((Date) re0.get("time"))));
		assertEquals(raw0.get("timestamp"), timestampFormat.format(((Date) re0.get("timestamp"))));

		Map<String, Object> re1 = resultData.get(1);

		assertEquals(raw1.get("number"), ((Double) re1.get("number")).toString());
		assertEquals(raw1.get("date"), dateFormat.format(((Date) re1.get("date"))));
		assertEquals(raw1.get("time"), timeFormat.format(((Date) re1.get("time"))));
		assertEquals(raw1.get("timestamp"), timestampFormat.format(((Date) re1.get("timestamp"))));
	}

	@Test
	public void resolveResultDataTest_dataFormat_custom() throws Throwable
	{
		TestAbstractDataSet dataSet = new TestAbstractDataSet();

		DataFormat dataFormat = new DataFormat();
		dataFormat.setDateFormat("yyyy年MM月dd日");
		dataFormat.setTimeFormat("HH时mm分ss秒");
		dataFormat.setTimestampFormat("yyyy年MM月dd日HH时mm分ss秒");
		dataFormat.setNumberFormat("#.##");

		dataSet.setDataFormat(dataFormat);

		SimpleDateFormat dateFormat = new SimpleDateFormat(dataFormat.getDateFormat());
		SimpleDateFormat timeFormat = new SimpleDateFormat(dataFormat.getTimeFormat());
		SimpleDateFormat timestampFormat = new SimpleDateFormat(dataFormat.getTimestampFormat());

		List<Map<String, Object>> rawData = new ArrayList<Map<String, Object>>();

		Map<String, Object> raw0 = new HashMap<String, Object>();
		raw0.put("number", "4.1");
		raw0.put("date", "2021年01月01日");
		raw0.put("time", "14时41分41秒");
		raw0.put("timestamp", "2021年01月01日14时41分41秒");

		Map<String, Object> raw1 = new HashMap<String, Object>();
		raw1.put("number", "4.2");
		raw1.put("date", "2021年01月02日");
		raw1.put("time", "14时41分42秒");
		raw1.put("timestamp", "2021年01月01日14时41分42秒");

		Collections.addAll(rawData, raw0, raw1);

		List<DataSetProperty> properties = new ArrayList<DataSetProperty>();
		{
			DataSetProperty p0 = new DataSetProperty("number", DataSetProperty.DataType.NUMBER);
			DataSetProperty p1 = new DataSetProperty("date", DataSetProperty.DataType.DATE);
			DataSetProperty p2 = new DataSetProperty("time", DataSetProperty.DataType.TIME);
			DataSetProperty p3 = new DataSetProperty("timestamp", DataSetProperty.DataType.TIMESTAMP);

			Collections.addAll(properties, p0, p1, p2, p3);
		}

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> resultData = (List<Map<String, Object>>) dataSet.resolveResultData(rawData,
				properties, -1, null);

		assertEquals(rawData.size(), resultData.size());

		Map<String, Object> re0 = resultData.get(0);

		assertEquals(raw0.get("number"), ((Double) re0.get("number")).toString());
		assertEquals(raw0.get("date"), dateFormat.format(((Date) re0.get("date"))));
		assertEquals(raw0.get("time"), timeFormat.format(((Date) re0.get("time"))));
		assertEquals(raw0.get("timestamp"), timestampFormat.format(((Date) re0.get("timestamp"))));

		Map<String, Object> re1 = resultData.get(1);

		assertEquals(raw1.get("number"), ((Double) re1.get("number")).toString());
		assertEquals(raw1.get("date"), dateFormat.format(((Date) re1.get("date"))));
		assertEquals(raw1.get("time"), timeFormat.format(((Date) re1.get("time"))));
		assertEquals(raw1.get("timestamp"), timestampFormat.format(((Date) re1.get("timestamp"))));
	}

	@Test
	public void resolveResultDataTest_resultDataFormat() throws Throwable
	{
		TestAbstractDataSet dataSet = new TestAbstractDataSet();

		List<Map<String, Object>> rawData = new ArrayList<Map<String, Object>>();

		Map<String, Object> raw0 = new HashMap<String, Object>();
		raw0.put("number", "4.1");
		raw0.put("date", "2021-01-01");
		raw0.put("time", "14:41:41");
		raw0.put("timestamp", "2021-01-01 14:41:41");

		Map<String, Object> raw1 = new HashMap<String, Object>();
		raw1.put("number", "4.2");
		raw1.put("date", "2021-01-02");
		raw1.put("time", "14:41:42");
		raw1.put("timestamp", "2021-01-01 14:41:42");

		Collections.addAll(rawData, raw0, raw1);

		List<DataSetProperty> properties = new ArrayList<DataSetProperty>();
		{
			DataSetProperty p0 = new DataSetProperty("number", DataSetProperty.DataType.NUMBER);
			DataSetProperty p1 = new DataSetProperty("date", DataSetProperty.DataType.DATE);
			DataSetProperty p2 = new DataSetProperty("time", DataSetProperty.DataType.TIME);
			DataSetProperty p3 = new DataSetProperty("timestamp", DataSetProperty.DataType.TIMESTAMP);

			Collections.addAll(properties, p0, p1, p2, p3);
		}

		// 字符串
		{
			ResultDataFormat resultDataFormat = new ResultDataFormat();
			resultDataFormat.setDateFormat("yyyy年MM月dd日");
			resultDataFormat.setTimeFormat("HH时mm分ss秒");
			resultDataFormat.setTimestampFormat("yyyy年MM月dd日HH时mm分ss秒");

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> resultData = (List<Map<String, Object>>) dataSet.resolveResultData(rawData,
					properties, -1, resultDataFormat);

			assertEquals(rawData.size(), resultData.size());

			Map<String, Object> re0 = resultData.get(0);

			assertEquals(raw0.get("number"), ((Double) re0.get("number")).toString());
			assertEquals("2021年01月01日", re0.get("date"));
			assertEquals("14时41分41秒", re0.get("time"));
			assertEquals("2021年01月01日14时41分41秒", re0.get("timestamp"));

			Map<String, Object> re1 = resultData.get(1);

			assertEquals(raw1.get("number"), ((Double) re1.get("number")).toString());
			assertEquals("2021年01月02日", re1.get("date"));
			assertEquals("14时41分42秒", re1.get("time"));
			assertEquals("2021年01月01日14时41分42秒", re1.get("timestamp"));
		}

		// 数值
		{
			ResultDataFormat resultDataFormat = new ResultDataFormat();
			resultDataFormat.setDateType(ResultDataFormat.TYPE_NUMBER);
			resultDataFormat.setTimeType(ResultDataFormat.TYPE_NUMBER);
			resultDataFormat.setTimestampType(ResultDataFormat.TYPE_NUMBER);

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> resultData = (List<Map<String, Object>>) dataSet.resolveResultData(rawData,
					properties, -1, resultDataFormat);

			assertEquals(rawData.size(), resultData.size());

			Map<String, Object> re0 = resultData.get(0);

			assertEquals(raw0.get("number"), ((Double) re0.get("number")).toString());
			assertEquals(1609430400000L, re0.get("date"));
			assertEquals(24101000L, re0.get("time"));
			assertEquals(1609483301000L, re0.get("timestamp"));

			Map<String, Object> re1 = resultData.get(1);

			assertEquals(raw1.get("number"), ((Double) re1.get("number")).toString());
			assertEquals(1609516800000L, re1.get("date"));
			assertEquals(24102000L, re1.get("time"));
			assertEquals(1609483302000L, re1.get("timestamp"));
		}

		// 无
		{
			ResultDataFormat resultDataFormat = new ResultDataFormat();
			resultDataFormat.setDateType(ResultDataFormat.TYPE_NONE);
			resultDataFormat.setTimeType(ResultDataFormat.TYPE_NONE);
			resultDataFormat.setTimestampType(ResultDataFormat.TYPE_NONE);

			DataFormat dataFormat = dataSet.createDataSetPropertyValueConverter().getDataFormat();
			SimpleDateFormat dateFormat = new SimpleDateFormat(dataFormat.getDateFormat());
			SimpleDateFormat timeFormat = new SimpleDateFormat(dataFormat.getTimeFormat());
			SimpleDateFormat timestampFormat = new SimpleDateFormat(dataFormat.getTimestampFormat());

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> resultData = (List<Map<String, Object>>) dataSet.resolveResultData(rawData,
					properties, -1, resultDataFormat);

			assertEquals(rawData.size(), resultData.size());

			Map<String, Object> re0 = resultData.get(0);

			assertEquals(raw0.get("number"), ((Double) re0.get("number")).toString());
			assertEquals(raw0.get("date"), dateFormat.format(((Date) re0.get("date"))));
			assertEquals(raw0.get("time"), timeFormat.format(((Date) re0.get("time"))));
			assertEquals(raw0.get("timestamp"), timestampFormat.format(((Date) re0.get("timestamp"))));

			Map<String, Object> re1 = resultData.get(1);

			assertEquals(raw1.get("number"), ((Double) re1.get("number")).toString());
			assertEquals(raw1.get("date"), dateFormat.format(((Date) re1.get("date"))));
			assertEquals(raw1.get("time"), timeFormat.format(((Date) re1.get("time"))));
			assertEquals(raw1.get("timestamp"), timestampFormat.format(((Date) re1.get("timestamp"))));
		}

		// 无
		{
			ResultDataFormat resultDataFormat = new ResultDataFormat();
			resultDataFormat.setDateType(ResultDataFormat.TYPE_NONE);
			resultDataFormat.setTimeType(ResultDataFormat.TYPE_NONE);
			resultDataFormat.setTimestampType(ResultDataFormat.TYPE_NONE);

			DataFormat dataFormat = dataSet.createDataSetPropertyValueConverter().getDataFormat();
			SimpleDateFormat dateFormat = new SimpleDateFormat(dataFormat.getDateFormat());
			SimpleDateFormat timeFormat = new SimpleDateFormat(dataFormat.getTimeFormat());
			SimpleDateFormat timestampFormat = new SimpleDateFormat(dataFormat.getTimestampFormat());

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> resultData = (List<Map<String, Object>>) dataSet.resolveResultData(rawData,
					properties, -1, resultDataFormat);

			assertEquals(rawData.size(), resultData.size());

			Map<String, Object> re0 = resultData.get(0);

			assertEquals(raw0.get("number"), ((Double) re0.get("number")).toString());
			assertEquals(raw0.get("date"), dateFormat.format(((Date) re0.get("date"))));
			assertEquals(raw0.get("time"), timeFormat.format(((Date) re0.get("time"))));
			assertEquals(raw0.get("timestamp"), timestampFormat.format(((Date) re0.get("timestamp"))));

			Map<String, Object> re1 = resultData.get(1);

			assertEquals(raw1.get("number"), ((Double) re1.get("number")).toString());
			assertEquals(raw1.get("date"), dateFormat.format(((Date) re1.get("date"))));
			assertEquals(raw1.get("time"), timeFormat.format(((Date) re1.get("time"))));
			assertEquals(raw1.get("timestamp"), timestampFormat.format(((Date) re1.get("timestamp"))));
		}
	}

	@Test
	public void convertRawDataToResultTest_expression() throws Throwable
	{
		TestAbstractDataSet dataSet = new TestAbstractDataSet();

		List<Map<String, Object>> rawData = new ArrayList<Map<String, Object>>();

		Map<String, Object> raw0 = new HashMap<String, Object>();
		raw0.put("v0", "2");
		raw0.put("v1", "6");
		raw0.put("s0", "aaa");

		Collections.addAll(rawData, raw0);

		List<DataSetProperty> properties = new ArrayList<DataSetProperty>();
		{
			DataSetProperty p0 = new DataSetProperty("v0", DataSetProperty.DataType.INTEGER);
			DataSetProperty p1 = new DataSetProperty("v1", DataSetProperty.DataType.NUMBER);
			DataSetProperty p2 = new DataSetProperty("s0", DataSetProperty.DataType.STRING);

			DataSetProperty avg = new DataSetProperty("avg", DataSetProperty.DataType.DECIMAL);
			avg.setEvaluated(true);
			avg.setExpression("(v0 + v1)/2");

			DataSetProperty suffix = new DataSetProperty("suffix", DataSetProperty.DataType.STRING);
			suffix.setEvaluated(true);
			suffix.setExpression("s0 + '-sufix'");

			Collections.addAll(properties, p0, p1, p2, avg, suffix);
		}

		List<Map<String, Object>> resultData = dataSet.convertRawDataToResult(rawData, properties, -1, null);

		assertEquals(rawData.size(), resultData.size());

		{
			Map<String, Object> re0 = resultData.get(0);

			assertEquals(raw0.get("v0"), ((Number) re0.get("v0")).toString());
			assertEquals(raw0.get("v1"), ((Number) re0.get("v1")).toString());
			assertEquals(4, ((Number) re0.get("avg")).intValue());
			assertEquals("aaa-sufix", re0.get("suffix"));
		}
	}

	private static class TestAbstractDataSet extends AbstractDataSet
	{
		@Override
		public DataSetResult getResult(DataSetQuery query) throws DataSetException
		{
			throw new UnsupportedOperationException();
		}
	}
}
