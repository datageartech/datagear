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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetField;
import org.datagear.analysis.support.DataSetFieldExpEvaluator.ValueSetter;
import org.junit.Test;

/**
 * {@linkplain DataSetFieldExpEvaluator}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetFieldExpEvaluatorTest
{
	private DataSetFieldExpEvaluator evaluator = new DataSetFieldExpEvaluator();

	@Test
	public void evalTest() throws Throwable
	{
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("width", 3);
		data.put("height", 6);
		data.put("bean", new ExpBean());
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("size", 6);
		map.put("age", 12);
		map.put("weight", 5);
		data.put("map", map);
		data.put("list", Arrays.asList(1,3,5));
		data.put("BigInteger", BigInteger.valueOf(1000L));
		data.put("BigDecimal", BigDecimal.valueOf(1.2D));
		data.put("string", "abc");
		data.put("中文关键字", 6);
		data.put("special']name", 6);
		data.put("special\"]name", 6);

		// 基本数值运算，简单Map访问方式
		{
			Number result = (Number) this.evaluator.eval("(width * height)/2 + map.size + bean.d + list[1] + 5%2",
					data);
			assertEquals(21, result.intValue());
		}
		
		// 基本数值运算，标准Map访问方式
		{
			Number result = (Number) this.evaluator
					.eval("(['width'] * height)/2 + map[\"size\"] + bean.d + list[1] + 5%2",
					data);
			assertEquals(21, result.intValue());
		}

		//三元运算
		{
			Number result = (Number) this.evaluator.eval("width > height ? width + map.size : height + map.size", data);
			assertEquals(12, result.intValue());
		}
		{
			Number result = (Number) this.evaluator.eval("width == null ?  0 : width", data);
			assertEquals(3, result.intValue());
		}

		// BigInteger、BigDecimal
		{
			Number result = (Number) this.evaluator.eval("(BigInteger * BigDecimal)/2 + width - 1", data);
			assertEquals(602, result.intValue());
		}

		// 混合
		{
			Number result = (Number) this.evaluator.eval(
					"(width * height)/2 + map.size + bean.d + list[1] + (width > height ? width + map.size : height + map.size) - 1",
					data);
			assertEquals(31, result.intValue());
		}

		// 字符串合并
		{
			String result = (String) this.evaluator.eval("string + '-suffix'", data);
			assertEquals("abc-suffix", result);
		}

		// 特殊字符
		{
			Number result = (Number) this.evaluator.eval("(['中文关键字'] + 2) * 3", data);
			assertEquals(24, result.intValue());
		}
		{
			Number result = (Number) this.evaluator.eval("([\"special']name\"] + 2) * 3", data);
			assertEquals(24, result.intValue());
		}
		{
			Number result = (Number) this.evaluator.eval("(['special\"]name'] + 2) * 3", data);
			assertEquals(24, result.intValue());
		}

		// 字面值
		{
			String result = (String) this.evaluator.eval("'abc'", data);
			assertEquals("abc", result);
		}
		{
			String result = (String) this.evaluator.eval("\"abc\"", data);
			assertEquals("abc", result);
		}
		{
			Integer result = (Integer) this.evaluator.eval("3", data);
			assertEquals(3, result.intValue());
		}
		{
			Number result = (Number) this.evaluator.eval("3.25", data);
			assertEquals(3.25d, result.doubleValue(), 0.001d);
		}
		{
			Boolean result = (Boolean) this.evaluator.eval("true", data);
			assertTrue(result.booleanValue());
		}
		{
			Boolean result = (Boolean) this.evaluator.eval("false", data);
			assertFalse(result.booleanValue());
		}
		{
			Object result = this.evaluator.eval("null", data);
			assertNull(result);
		}
	}

	@Test
	public void evalTest_forMapSize() throws Throwable
	{
		//map.size应取"size"关键字的值
		{
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("width", 3);
			data.put("height", 6);
			data.put("bean", new ExpBean());
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("size", 6);
			map.put("age", 12);
			map.put("weight", 5);
			data.put("map", map);
	
			Number result = (Number) this.evaluator.eval("(width * height)/2 + map.size + bean.d", data);
	
			assertEquals(17, result.intValue());
		}
		
		//Map只允许访问关键字的值，不允许访问Map本身的方法（比如"map.size"不允许访问Map的大小）
		{
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("width", 3);
			data.put("height", 6);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("age", 12);
			data.put("map", map);
			
			{
				{
					Number result = (Number)this.evaluator.eval("width", data);
					assertEquals(3, result.intValue());
				}
				{
					Object result = this.evaluator.eval("size", data);
					assertNull(result);
				}
			}
			
			{
				{
					Number result = (Number)this.evaluator.eval("map.age", data);
					assertEquals(12, result.intValue());
				}
				{
					Object result = this.evaluator.eval("map.size", data);
					assertNull(result);
				}
			}
		}
	}
	
	@Test
	public void evalTest_forStringLength()
	{
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("string", "abc");

		Integer length = (Integer)this.evaluator.eval("string.length", data);
		assertEquals(3, length.intValue());
	}
	
	@Test
	public void evalTest_specialStringKey()
	{
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("width", 3);
		data.put("height", 6);
		data.put("height_1", 1);
		data.put("height-2", 1);
		data.put("中文关键字", 6);
		data.put("中文对象", new ExpBean());
		data.put("special'name", 6);
		data.put("special\"name", 6);
		data.put("special'\"name", 6);

		// 特殊字符关键字只能使用标准Map访问语法
		{
			Number result = (Number) this.evaluator.eval("(['中文关键字'] + ['中文对象'].d) * width * height_1 * ['height-2']",
					data);
			assertEquals(24, result.intValue());
		}
		{
			Number result = (Number) this.evaluator.eval("([\"special'name\"] + 2) * 3", data);
			assertEquals(24, result.intValue());
		}
		{
			Number result = (Number) this.evaluator.eval("(['special\"name'] + 2) * 3", data);
			assertEquals(24, result.intValue());
		}

		// 同时包含单引号、双引号的无法处理
		{
			String exception = null;

			try
			{
				this.evaluator.eval("([\"special'\"name\"] + 2) * 3", data);
			}
			catch (DataSetFieldExpEvaluatorException e)
			{
				exception = e.getMessage();
			}

			assertNotNull(exception);
		}

		// 同时包含单引号、双引号的无法处理
		{
			String exception = null;

			try
			{
				this.evaluator.eval("(['special\\'\"name'] + 2) * 3", data);
			}
			catch (DataSetFieldExpEvaluatorException e)
			{
				exception = e.getMessage();
			}

			assertNotNull(exception);
		}

		// 特殊字符关键字无法使用简单Map访问语法
		{
			String exception = null;
			
			try
			{
				this.evaluator.eval("(中文关键字 + 中文对象.d) * width * height-2", data);
			}
			catch (DataSetFieldExpEvaluatorException e)
			{
				exception = e.getMessage();
			}
			
			assertNotNull(exception);
		}
	}

	@Test
	public void evalTest_illegalSyntax()
	{
		// 禁止的语法：数据写入
		{
			String exception = null;

			Map<String, Object> data = new HashMap<String, Object>();

			try
			{
				this.evaluator.eval("age = 33", data);
			}
			catch (DataSetFieldExpEvaluatorException e)
			{
				exception = e.getMessage();
			}

			assertTrue(data.isEmpty());
			assertNotNull(exception);
		}
		{
			String exception = null;

			Map<String, Object> data = new HashMap<String, Object>();

			try
			{
				this.evaluator.eval("['age'] = 33", data);
			}
			catch (DataSetFieldExpEvaluatorException e)
			{
				exception = e.getMessage();
			}

			assertTrue(data.isEmpty());
			assertNotNull(exception);
		}
		{
			String exception = null;

			Map<String, Object> data = new HashMap<String, Object>();
			Map<String, Object> map = new HashMap<String, Object>();
			data.put("map", map);

			try
			{
				this.evaluator.eval("map.age = 33", data);
			}
			catch (DataSetFieldExpEvaluatorException e)
			{
				exception = e.getMessage();
			}

			assertTrue(map.isEmpty());
			assertNotNull(exception);
		}
		{
			String exception = null;

			Map<String, Object> data = new HashMap<String, Object>();
			Map<String, Object> map = new HashMap<String, Object>();
			data.put("map", map);

			try
			{
				this.evaluator.eval("['map']['age'] = 33", data);
			}
			catch (DataSetFieldExpEvaluatorException e)
			{
				exception = e.getMessage();
			}

			assertTrue(map.isEmpty());
			assertNotNull(exception);
		}
		{
			String exception = null;

			Map<String, Object> data = new HashMap<String, Object>();
			ExpBean bean = new ExpBean();
			data.put("bean", bean);

			try
			{
				this.evaluator.eval("bean.d = 33", data);
			}
			catch (DataSetFieldExpEvaluatorException e)
			{
				exception = e.getMessage();
			}

			assertNotEquals(33, bean.d);
			assertNotNull(exception);
		}
		
		// 禁止的语法：方法调用
		{
			String exception = null;

			Map<String, Object> data = new HashMap<String, Object>();
			data.put("string", "abc");

			try
			{
				this.evaluator.eval("size()", data);
			}
			catch (DataSetFieldExpEvaluatorException e)
			{
				exception = e.getMessage();
			}

			assertNotNull(exception);
		}
		{
			String exception = null;

			Map<String, Object> data = new HashMap<String, Object>();
			data.put("string", "abc");

			try
			{
				this.evaluator.eval("string.length()", data);
			}
			catch(DataSetFieldExpEvaluatorException e)
			{
				exception = e.getMessage();
			}
			
			assertNotNull(exception);
		}
		{
			String exception = null;

			Map<String, Object> data = new HashMap<String, Object>();
			data.put("string", "abc");

			try
			{
				this.evaluator.eval("string.toUpperCase()", data);
			}
			catch(DataSetFieldExpEvaluatorException e)
			{
				exception = e.getMessage();
			}
			
			assertNotNull(exception);
		}
		
		// 禁止的语法：类型
		{
			String exception = null;

			Map<String, Object> data = new HashMap<String, Object>();
			
			try
			{
				this.evaluator.eval("T(java.lang.Math).random()", data);
			}
			catch(DataSetFieldExpEvaluatorException e)
			{
				exception = e.getMessage();
			}
			
			assertNotNull(exception);
		}
		
		// 禁止的语法：创建
		{
			String exception = null;

			Map<String, Object> data = new HashMap<String, Object>();
			
			try
			{
				this.evaluator.eval("new java.lang.String()", data);
			}
			catch(DataSetFieldExpEvaluatorException e)
			{
				exception = e.getMessage();
			}
			
			assertNotNull(exception);
		}
		{
			String exception = null;

			Map<String, Object> data = new HashMap<String, Object>();

			try
			{
				this.evaluator.eval("{1,2,3}", data);
			}
			catch (DataSetFieldExpEvaluatorException e)
			{
				exception = e.getMessage();
			}

			assertNotNull(exception);
		}
		{
			String exception = null;

			Map<String, Object> data = new HashMap<String, Object>();

			try
			{
				this.evaluator.eval("{a:1,b:2,c:3}", data);
			}
			catch (DataSetFieldExpEvaluatorException e)
			{
				exception = e.getMessage();
			}

			assertNotNull(exception);
		}

		// 禁止的语法：bean引用
		{
			String exception = null;

			Map<String, Object> data = new HashMap<String, Object>();

			try
			{
				this.evaluator.eval("@bean", data);
			}
			catch (DataSetFieldExpEvaluatorException e)
			{
				exception = e.getMessage();
			}

			assertNotNull(exception);
		}

		// 禁止的语法
		{
			String exception = null;

			Map<String, Object> data = new HashMap<String, Object>();
			data.put("width", 3);

			try
			{
				this.evaluator.eval("width > 0 ? new java.lang.String('b') : {1,2,3}", data);
			}
			catch (DataSetFieldExpEvaluatorException e)
			{
				exception = e.getMessage();
			}

			assertNotNull(exception);
		}
	}

	@Test
	public void evalTest_List_DataSetField()
	{
		List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("v0", 2);
		data.put("v1", 3);
		data.put("v2", 4);

		Collections.addAll(datas, data);

		List<DataSetField> fields = new ArrayList<DataSetField>();
		{
			DataSetField p0 = new DataSetField("v0", DataSetField.DataType.INTEGER);
			DataSetField p1 = new DataSetField("v1", DataSetField.DataType.INTEGER);
			DataSetField p2 = new DataSetField("v2", DataSetField.DataType.INTEGER);
			DataSetField p3 = new DataSetField("v3", DataSetField.DataType.INTEGER);

			p1.setEvaluated(true);
			p1.setExpression("v0 + v1 + v2");

			p2.setEvaluated(true);
			p2.setExpression("v0 + v1");

			p3.setEvaluated(true);
			p3.setExpression("v0 + v1 + v2");

			Collections.addAll(fields, p0, p1, p2, p3);
		}

		this.evaluator.eval(fields, datas, new ValueSetter<Map<String, Object>>()
		{
			@Override
			public void set(DataSetField field, int fieldIndex, Map<String, Object> data, Object value)
			{
				data.put(field.getName(), value);
			}
		});

		assertEquals(2, ((Number) data.get("v0")).intValue());
		assertEquals(9, ((Number) data.get("v1")).intValue());
		assertEquals(11, ((Number) data.get("v2")).intValue());
		assertEquals(22, ((Number) data.get("v3")).intValue());
	}

	@Test
	public void evalTest_List_DataSetField_noExpression()
	{
		List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("v0", 2);
		data.put("s0", "aaa");

		Collections.addAll(datas, data);

		List<DataSetField> fields = new ArrayList<DataSetField>();
		{
			DataSetField p0 = new DataSetField("v0", DataSetField.DataType.INTEGER);
			DataSetField p1 = new DataSetField("s0", DataSetField.DataType.STRING);

			Collections.addAll(fields, p0, p1);
		}

		boolean evaled = this.evaluator.eval(fields, datas, new ValueSetter<Map<String, Object>>()
		{
			@Override
			public void set(DataSetField field, int fieldIndex, Map<String, Object> data, Object value)
			{
				data.put(field.getName(), value);
			}
		});

		assertFalse(evaled);
	}

	protected static class ExpBean
	{
		private int d = 2;
		private int m = 3;

		public ExpBean()
		{
			super();
		}

		public int getD()
		{
			return d;
		}

		public void setD(int d)
		{
			this.d = d;
		}

		public int getM()
		{
			return m;
		}

		public void setM(int m)
		{
			this.m = m;
		}
	}
}
