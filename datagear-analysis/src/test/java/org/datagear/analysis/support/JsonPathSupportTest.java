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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * {@linkplain JsonPathSupport}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class JsonPathSupportTest
{
	private JsonPathSupport support = new JsonPathSupport();

	@Test
	public void test()
	{
		Object map = testMap();
		Object list = testList();

		{
			Object v = support.resolve(map, "key1");
			assertEquals("value1", v);
		}

		{
			Object v = support.resolve(map, "$.key1");
			assertEquals("value1", v);
		}

		{
			Object v = support.resolve(list, "[1].key21");
			assertEquals("value21", v);
		}

		{
			Object v = support.resolve(list, "$[1].key21");
			assertEquals("value21", v);
		}
	}

	protected Map<String, Object> testMap()
	{
		Map<String, Object> re = new HashMap<>();

		re.put("key1", "value1");
		re.put("key2", 2);
		re.put("key3", new TestBean("id2", "name2", 2));
		re.put("key4", "value4");

		return re;
	}

	protected List<Map<String, Object>> testList()
	{
		List<Map<String, Object>> re = new ArrayList<>();

		{
			Map<String, Object> map = new HashMap<>();

			map.put("key11", "value11");
			map.put("key12", 12);
			map.put("key13", "value13");

			re.add(map);
		}

		{
			Map<String, Object> map = new HashMap<>();

			map.put("key21", "value21");
			map.put("key22", 22);
			map.put("key23", "value23");

			re.add(map);
		}

		{
			Map<String, Object> map = new HashMap<>();

			map.put("key31", "value31");
			map.put("key32", 32);
			map.put("key33", "value33");

			re.add(map);
		}

		return re;
	}

	protected static class TestBean
	{
		private String id;
		private String name;
		private int value;
		private Map<String, ?> map = Collections.emptyMap();
		private List<TestBean> list = Collections.emptyList();

		public TestBean()
		{
			super();
		}

		public TestBean(String id, String name, int value)
		{
			super();
			this.id = id;
			this.name = name;
			this.value = value;
		}

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
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

		public Map<String, ?> getMap()
		{
			return map;
		}

		public void setMap(Map<String, ?> map)
		{
			this.map = map;
		}

		public List<TestBean> getList()
		{
			return list;
		}

		public void setList(List<TestBean> list)
		{
			this.list = list;
		}
	}
}
