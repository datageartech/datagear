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

package org.datagear.analysis.support.datasettpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * {@linkplain SqlDataSetFmkTemplateResolver}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataSetFmkTemplateResolverTest
{
	private SqlDataSetFmkTemplateResolver resolver = new SqlDataSetFmkTemplateResolver();

	@Test
	public void resolveTest()
	{
		{
			String text = "SELECT * FROM TABLE WHERE NAME = '${name}'";

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("name", "aaa");

			SqlTemplateResult actual = resolver.resolve(text, params);

			assertEquals("SELECT * FROM TABLE WHERE NAME = 'aaa'", actual.getResult());
			assertFalse(actual.isPrecompiled());
			assertTrue(actual.getParamValues().isEmpty());
		}

		// 单引号转义
		{
			String text = "SELECT * FROM TABLE WHERE NAME = '${name}'";

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("name", "a'a");

			SqlTemplateResult actual = resolver.resolve(text, params);

			assertEquals("SELECT * FROM TABLE WHERE NAME = 'a''a'", actual.getResult());
			assertFalse(actual.isPrecompiled());
			assertTrue(actual.getParamValues().isEmpty());
		}
	}

	@Test
	public void resolveTest_precompile()
	{
		// 预编译
		{
			String text = "SELECT * FROM TABLE WHERE STRING = ${pc(STRING)} AND INTEGER = ${pc(INTEGER)} AND FLOAT = ${pc(FLOAT)}" //
					+ " AND BOOLEAN = ${pc(BOOLEAN)}" //
					+ " AND STRING_ARRAY = ${pc(STRING_ARRAY)} AND STRING_ARRAY_ELE = ${pc(STRING_ARRAY[1])}" //
					+ " AND NUMBER_ARRAY = ${pc(NUMBER_ARRAY)} AND NUMBER_ARRAY_ELE = ${pc(NUMBER_ARRAY[0])}" //
					+ " AND BOOLEAN_ARRAY = ${pc(BOOLEAN_ARRAY)} AND BOOLEAN_ARRAY_ELE = ${pc(BOOLEAN_ARRAY[1])}" //
					+ " AND OBJ_ARRAY = ${pc(OBJ_ARRAY)} AND OBJ_ARRAY_ELE1 = ${pc(OBJ_ARRAY[1])} AND OBJ_ARRAY_ELE0 = ${pc(OBJ_ARRAY[0])} AND OBJ_ARRAY_ELE2 = ${pc(OBJ_ARRAY[2])}" //
					+ " AND OBJ_LIST = ${pc(OBJ_LIST)} AND OBJ_LIST_ELE1 = ${pc(OBJ_LIST[1])} AND OBJ_LIST_ELE0 = ${pc(OBJ_LIST[0])} AND OBJ_LIST_ELE2 = ${pc(OBJ_LIST[2])}" //
					+ " AND BEAN = ${pc(BEAN)} AND BEAN_P0 = ${pc(BEAN.name)} AND BEAN_P1 = ${pc(BEAN.value)} AND BEAN_P2 = ${pc(BEAN.success)}" //
					+ " AND MAP = ${pc(MAP)} AND MAP_P0 = ${pc(MAP.name)} AND MAP_P1 = ${pc(MAP.value)}";

			String expected = "SELECT * FROM TABLE WHERE STRING = ? AND INTEGER = ? AND FLOAT = ?" //
					+ " AND BOOLEAN = ?" //
					+ " AND STRING_ARRAY = ? AND STRING_ARRAY_ELE = ?" //
					+ " AND NUMBER_ARRAY = ? AND NUMBER_ARRAY_ELE = ?" //
					+ " AND BOOLEAN_ARRAY = ? AND BOOLEAN_ARRAY_ELE = ?" //
					+ " AND OBJ_ARRAY = ? AND OBJ_ARRAY_ELE1 = ? AND OBJ_ARRAY_ELE0 = ? AND OBJ_ARRAY_ELE2 = ?" //
					+ " AND OBJ_LIST = ? AND OBJ_LIST_ELE1 = ? AND OBJ_LIST_ELE0 = ? AND OBJ_LIST_ELE2 = ?" //
					+ " AND BEAN = ? AND BEAN_P0 = ? AND BEAN_P1 = ? AND BEAN_P2 = ?" //
					+ " AND MAP = ? AND MAP_P0 = ? AND MAP_P1 = ?";

			String[] STRING_ARRAY = new String[] { "aaa", "bbb", "ccc" };
			Number[] NUMBER_ARRAY = new Number[] { 5.13, 6, 7 };
			Boolean[] BOOLEAN_ARRAY = new Boolean[] { true, false, true };
			Object[] OBJ_ARRAY = new Object[] { true, "aaa", 3.26 };
			List<Object> OBJ_LIST = Arrays.asList("bbb", false, 3.36);
			BeanTest BEAN = new BeanTest("aaa", 6, true);
			Map<String, Object> MAP = new HashMap<>();
			MAP.put("name", "bbb");
			MAP.put("value", 6);

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("STRING", "a'a");
			params.put("INTEGER", 3);
			params.put("FLOAT", 3.26);
			params.put("BOOLEAN", true);
			params.put("STRING_ARRAY", STRING_ARRAY);
			params.put("NUMBER_ARRAY", NUMBER_ARRAY);
			params.put("BOOLEAN_ARRAY", BOOLEAN_ARRAY);
			params.put("OBJ_ARRAY", OBJ_ARRAY);
			params.put("OBJ_LIST", OBJ_LIST);
			params.put("BEAN", BEAN);
			params.put("MAP", MAP);

			SqlTemplateResult actual = resolver.resolve(text, params);
			List<Object> paramValues = actual.getParamValues();

			assertEquals(expected, actual.getResult());
			assertTrue(actual.isPrecompiled());
			assertFalse(paramValues.isEmpty());

			assertEquals("a'a", paramValues.get(0));
			assertEquals(Integer.valueOf(3), paramValues.get(1));
			assertEquals(Double.valueOf(3.26), paramValues.get(2));
			assertEquals(Boolean.valueOf(true), paramValues.get(3));

			assertEquals(STRING_ARRAY, paramValues.get(4));
			assertEquals(STRING_ARRAY[1], paramValues.get(5));

			assertEquals(NUMBER_ARRAY, paramValues.get(6));
			assertEquals(NUMBER_ARRAY[0], paramValues.get(7));

			assertEquals(BOOLEAN_ARRAY, paramValues.get(8));
			assertEquals(BOOLEAN_ARRAY[1], paramValues.get(9));

			assertEquals(OBJ_ARRAY, paramValues.get(10));
			assertEquals(OBJ_ARRAY[1], paramValues.get(11));
			assertEquals(OBJ_ARRAY[0], paramValues.get(12));
			assertEquals(OBJ_ARRAY[2], paramValues.get(13));

			assertEquals(OBJ_LIST, paramValues.get(14));
			assertEquals(OBJ_LIST.get(1), paramValues.get(15));
			assertEquals(OBJ_LIST.get(0), paramValues.get(16));
			assertEquals(OBJ_LIST.get(2), paramValues.get(17));

			assertEquals(BEAN, paramValues.get(18));
			assertEquals(BEAN.getName(), paramValues.get(19));
			assertEquals(BEAN.getValue(), paramValues.get(20));
			assertEquals(BEAN.isSuccess(), paramValues.get(21));

			assertEquals(MAP, paramValues.get(22));
			assertEquals(MAP.get("name"), paramValues.get(23));
			assertEquals(MAP.get("value"), paramValues.get(24));
		}
	}

	public static class BeanTest
	{
		private String name;
		private int value;
		private boolean success;

		public BeanTest()
		{
			super();
		}

		public BeanTest(String name, int value, boolean success)
		{
			super();
			this.name = name;
			this.value = value;
			this.success = success;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public int getValue()
		{
			return value;
		}

		public void setValue(int value)
		{
			this.value = value;
		}

		public boolean isSuccess()
		{
			return success;
		}

		public void setSuccess(boolean success)
		{
			this.success = success;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + (success ? 1231 : 1237);
			result = prime * result + value;
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BeanTest other = (BeanTest) obj;
			if (name == null)
			{
				if (other.name != null)
					return false;
			}
			else if (!name.equals(other.name))
				return false;
			if (success != other.success)
				return false;
			if (value != other.value)
				return false;
			return true;
		}
	}
}
