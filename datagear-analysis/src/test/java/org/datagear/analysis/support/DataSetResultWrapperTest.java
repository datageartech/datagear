/*
 * Copyright 2018-present datagear.tech
 */

package org.datagear.analysis.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetField;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.ResolvedDataSetResult;
import org.junit.Test;

/**
 * {@linkplain DataSetResultWrapper}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetResultWrapperTest
{
	@Test
	public void getResultTest()
	{
		DataSetResultWrapper drw = new DataSetResultWrapper(dataSet());

		{
			DataSetResult result = drw.getResult("3");
			Object data = result.getData();

			assertTrue(data instanceof Map<?, ?>);

			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) data;

			assertEquals("dft-name", map.get("name"));
			assertEquals(3, ((Number) map.get("value")).intValue());
			assertEquals(6, ((Number) map.get("evalValue")).intValue());
		}

		{
			Map<String, Object> s = new HashMap<>();
			s.put("name", "a");
			s.put("value", 5);

			DataSetResult result = drw.getResult(s);
			Object data = result.getData();

			assertTrue(data instanceof Map<?, ?>);

			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) data;

			assertEquals("a", map.get("name"));
			assertEquals(5, ((Number) map.get("value")).intValue());
			assertEquals(10, ((Number) map.get("evalValue")).intValue());
		}

		{
			List<Map<String, Object>> s = new ArrayList<>();

			{
				Map<String, Object> se = new HashMap<>();
				se.put("name", "a");
				se.put("value", 5);

				s.add(se);
			}

			{
				Map<String, Object> se = new HashMap<>();
				se.put("name", "b");
				se.put("value", 6);

				s.add(se);
			}

			DataSetResult result = drw.getResult(s);
			Object data = result.getData();

			assertTrue(data instanceof List<?>);

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> list = (List<Map<String, Object>>) data;

			{
				Map<String, Object> map = list.get(0);

				assertEquals("a", map.get("name"));
				assertEquals(5, ((Number) map.get("value")).intValue());
				assertEquals(10, ((Number) map.get("evalValue")).intValue());
			}

			{
				Map<String, Object> map = list.get(1);

				assertEquals("b", map.get("name"));
				assertEquals(6, ((Number) map.get("value")).intValue());
				assertEquals(12, ((Number) map.get("evalValue")).intValue());
			}
		}
	}

	@Test
	public void resolveResultTest()
	{
		CsvValueDataSet dataSet = dataSet();
		dataSet.setFields(Collections.emptyList());
		DataSetResultWrapper drw = new DataSetResultWrapper(dataSet);

		{
			Map<String, Object> s = new HashMap<>();
			s.put("name", "a");
			s.put("value", 5);

			ResolvedDataSetResult resolvedResult = drw.resolveResult(s);
			DataSetResult result = resolvedResult.getResult();

			{
				Object data = result.getData();

				assertTrue(data instanceof Map<?, ?>);

				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) data;

				assertEquals("a", map.get("name"));
				assertEquals(5, ((Number) map.get("value")).intValue());
			}

			{
				List<DataSetField> fields = resolvedResult.getFields();
				assertNotNull(fields);
				assertEquals(2, fields.size());

				{
					DataSetField p = fields.get(0);
					assertEquals("name", p.getName());
					assertEquals(DataSetField.DataType.STRING, p.getType());
				}
				{
					DataSetField p = fields.get(1);
					assertEquals("value", p.getName());
					assertEquals(DataSetField.DataType.NUMBER, p.getType());
				}
			}
		}
	}

	@Test
	public void wrapDataTest()
	{
		DataSetResultWrapper drw = new DataSetResultWrapper(dataSet());

		{
			String s = "a";
			Object re = drw.wrapData(s);

			assertTrue(re instanceof Map<?, ?>);
			assertEquals(s, ((Map<?, ?>) re).get(DataSetResultWrapper.PRIMITIVE_TO_STD_DATA_PROP_NAME));
		}
		{
			byte[] s = new byte[] { 1, 2, 3 };
			Object re = drw.wrapData(s);

			assertTrue(re instanceof Map<?, ?>);
			assertEquals(s, ((Map<?, ?>) re).get(DataSetResultWrapper.PRIMITIVE_TO_STD_DATA_PROP_NAME));
		}

		{
			List<String> s = Arrays.asList("a", "b", "c");
			Object re = drw.wrapData(s);

			assertTrue(re instanceof List<?>);

			@SuppressWarnings("unchecked")
			List<Map<?, ?>> reList = (List<Map<?, ?>>) re;

			assertEquals(3, reList.size());
			assertEquals(1, reList.get(0).size());
			assertEquals("a", reList.get(0).get(DataSetResultWrapper.PRIMITIVE_TO_STD_DATA_PROP_NAME));
			assertEquals(1, reList.get(1).size());
			assertEquals("b", reList.get(1).get(DataSetResultWrapper.PRIMITIVE_TO_STD_DATA_PROP_NAME));
			assertEquals(1, reList.get(2).size());
			assertEquals("c", reList.get(2).get(DataSetResultWrapper.PRIMITIVE_TO_STD_DATA_PROP_NAME));
		}
		{
			List<Object> s = Arrays.asList(new Object(), new Object(), new Object());
			Object re = drw.wrapData(s);

			assertTrue(re == s);
		}

		{
			String[] s = new String[] { "a", "b", "c" };
			Object re = drw.wrapData(s);

			assertTrue(re instanceof List<?>);

			@SuppressWarnings("unchecked")
			List<Map<?, ?>> reList = (List<Map<?, ?>>) re;

			assertEquals(3, reList.size());
			assertEquals(1, reList.get(0).size());
			assertEquals("a", reList.get(0).get(DataSetResultWrapper.PRIMITIVE_TO_STD_DATA_PROP_NAME));
			assertEquals(1, reList.get(1).size());
			assertEquals("b", reList.get(1).get(DataSetResultWrapper.PRIMITIVE_TO_STD_DATA_PROP_NAME));
			assertEquals(1, reList.get(2).size());
			assertEquals("c", reList.get(2).get(DataSetResultWrapper.PRIMITIVE_TO_STD_DATA_PROP_NAME));
		}
		{
			Object[] s = new Object[] { new Object(), new Object(), new Object() };
			Object re = drw.wrapData(s);

			assertTrue(re == s);
		}

		{
			Object s = new Object();
			Object re = drw.wrapData(s);

			assertTrue(re == s);
		}
	}

	@Test
	public void isPrimitiveTest()
	{
		DataSetResultWrapper drw = new DataSetResultWrapper(dataSet());

		{
			assertFalse(drw.isPrimitive(null));
			assertTrue(drw.isPrimitive("test"));
			assertTrue(drw.isPrimitive(new byte[] { 1, 2 }));
			assertTrue(drw.isPrimitive(1));
			assertTrue(drw.isPrimitive(true));
			assertTrue(drw.isPrimitive('a'));
			assertFalse(drw.isPrimitive(new String[] { null, "a", "b" }));
			assertFalse(drw.isPrimitive(new Object[] { null, new Object(), "b" }));
			assertFalse(drw.isPrimitive(Arrays.asList(new String[] { null, "a", "b" })));
			assertFalse(drw.isPrimitive(Arrays.asList(new Object[] { null, new Object(), "b" })));
		}
	}

	protected CsvValueDataSet dataSet()
	{
		List<DataSetField> fields = new ArrayList<>();

		DataSetField np = new DataSetField("name", DataSetField.DataType.STRING);
		np.setDefaultValue("dft-name");

		DataSetField vp = new DataSetField("value", DataSetField.DataType.NUMBER);

		DataSetField evalp = new DataSetField("evalValue", DataSetField.DataType.NUMBER);
		evalp.setEvaluated(true);
		evalp.setExpression("value*2");

		fields.add(np);
		fields.add(vp);
		fields.add(evalp);

		CsvValueDataSet ds = new CsvValueDataSet("test", "test", fields, "");
		return ds;
	}
}
