/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
	public void resolveResultDataTest_defaultDataFormat() throws Throwable
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
				properties, null);

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
	public void resolveResultDataTest_customDataFormat() throws Throwable
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
				properties, null);

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

	private static class TestAbstractDataSet extends AbstractDataSet
	{
		@Override
		public DataSetResult getResult(DataSetQuery query) throws DataSetException
		{
			throw new UnsupportedOperationException();
		}
	}
}
