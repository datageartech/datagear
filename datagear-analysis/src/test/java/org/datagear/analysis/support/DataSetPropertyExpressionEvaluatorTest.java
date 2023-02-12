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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * {@linkplain DataSetPropertyExpressionEvaluator}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetPropertyExpressionEvaluatorTest
{
	private DataSetPropertyExpressionEvaluator evaluator = new DataSetPropertyExpressionEvaluator();

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

		//基本数值运算
		{
			Number result = (Number) this.evaluator.eval("(width * height)/2 + map.size + bean.d + list[1]", data);
			assertEquals(20, result.intValue());
		}
		
		//三元运算
		{
			Number result = (Number) this.evaluator.eval("width > height ? width + map.size : height + map.size", data);
			assertEquals(12, result.intValue());
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
	public void evalTest_forBigIntegerAndBigDecimal()
	{
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("width", BigInteger.valueOf(1000L));
		data.put("height", BigDecimal.valueOf(1.2D));

		Number result = (Number) this.evaluator.eval("(width * height)/2", data);

		assertEquals(600, result.intValue());
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
	public void evalTest_denied()
	{
		//方法调用
		{
			String exception = null;
			
			try
			{
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("string", "abc");

				this.evaluator.eval("string.length()", data);
			}
			catch(DataSetPropertyExpressionEvaluatorException e)
			{
				exception = e.getMessage();
			}
			
			assertNotNull(exception);
		}
		{
			String exception = null;
			
			try
			{
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("string", "abc");

				this.evaluator.eval("string.toUpperCase()", data);
			}
			catch(DataSetPropertyExpressionEvaluatorException e)
			{
				exception = e.getMessage();
			}
			
			assertNotNull(exception);
		}
		
		//类型
		{
			String exception = null;
			
			try
			{
				Map<String, Object> data = new HashMap<String, Object>();
				this.evaluator.eval("T(java.lang.Math).random()", data);
			}
			catch(DataSetPropertyExpressionEvaluatorException e)
			{
				exception = e.getMessage();
			}
			
			assertNotNull(exception);
		}
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
