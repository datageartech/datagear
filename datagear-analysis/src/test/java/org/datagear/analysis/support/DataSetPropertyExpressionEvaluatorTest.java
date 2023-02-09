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
	public void doEvalSingleTest() throws Throwable
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

		Number result = (Number) this.evaluator.doEvalSingle("(width * height)/2 + map.size + bean.d", data);

		assertEquals(17, result.intValue());
	}
	
	@Test
	public void doEvalSingleTest_mapKeyFirst() throws Throwable
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

			Number result = (Number) this.evaluator.doEvalSingle("(width * height)/2 + map.size + bean.d", data);

			assertEquals(17, result.intValue());
		}
		
		//map.size应取map的大小
		{
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("width", 3);
			data.put("height", 6);
			data.put("bean", new ExpBean());
			Map<String, Object> map = new HashMap<String, Object>();
			//map.put("size", 6);
			map.put("age", 12);
			map.put("weight", 5);
			data.put("map", map);
	
			Number result = (Number) this.evaluator.doEvalSingle("(width * height)/2 + map.size + bean.d", data);
	
			assertEquals(13, result.intValue());
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
