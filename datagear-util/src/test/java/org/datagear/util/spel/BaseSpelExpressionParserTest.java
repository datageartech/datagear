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

package org.datagear.util.spel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.SpelEvaluationException;

/**
 * {@linkplain BaseSpelExpressionParser}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class BaseSpelExpressionParserTest
{
	private BaseSpelExpressionParser parser = new BaseSpelExpressionParser();

	@Test
	public void getValueTest_default()
	{
		TestBean bean = testBean();
		TestBean[] beanArray = testBeanArray();
		List<TestBean> beanList = testBeanList();

		Map<String, Object> map = testMap();
		Map<String, Object>[] mapArray = testMapArray();
		List<Map<String, Object>> mapList = testMapList();

		{
			Object v = parser.getValue("name", bean);
			assertEquals("name1", v);
		}
		{
			Object v = parser.getValue("map['key1']", bean);
			assertEquals("value1", v);
		}
		{
			Object v = parser.getValue("map.size", bean);
			assertEquals(5, v);
		}
		{
			Object v = parser.getValue("map['size']", bean);
			assertEquals(99, v);
		}
		{
			Object v = parser.getValue("map['inexists']", bean);
			assertNull(v);
		}

		{
			Object v = parser.getValue("[1].name", beanArray);
			assertEquals("name2", v);
		}
		{
			Object v = parser.getValue("[1].map['key21']", beanArray);
			assertEquals("value21", v);
		}

		{
			Object v = parser.getValue("[2].name", beanList);
			assertEquals("name3", v);
		}
		{
			Object v = parser.getValue("[2].map['key31']", beanList);
			assertEquals("value31", v);
		}

		{
			Object v = parser.getValue("['key1']", map);
			assertEquals("value1", v);
		}
		{
			Object v = parser.getValue("['inexists']", map);
			assertNull(v);
		}
		{
			Object v = parser.getValue("['size']", map);
			assertNull(v);
		}
		{
			Object v = parser.getValue("size", map);
			assertEquals(4, v);
		}

		{
			Object v = parser.getValue("[0]['key11']", mapArray);
			assertEquals("value11", v);
		}
		{
			Object v = parser.getValue("[0]['size']", mapArray);
			assertNull(v);
		}
		{
			Object v = parser.getValue("[0].size", mapArray);
			assertEquals(3, v);
		}

		{
			Object v = parser.getValue("[1]['key21']", mapList);
			assertEquals("value21", v);
		}
		{
			Object v = parser.getValue("[0]['size']", mapList);
			assertNull(v);
		}
		{
			Object v = parser.getValue("[0].size", mapList);
			assertEquals(3, v);
		}

		// 默认非法的Map访问语法
		{
			assertThrows(SpelEvaluationException.class, () ->
			{
				Object v = parser.getValue("map.key1", bean);
				assertEquals("value1", v);
			});
		}
		{
			assertThrows(SpelEvaluationException.class, () ->
			{
				Object v = parser.getValue("key1", map);
				assertEquals("value1", v);
			});
		}
	}

	@Test
	public void getValueTest_readonlyMapKeyOnlyContext()
	{
		TestBean bean = testBean();
		Map<String, Object> map = testMap();
		Map<String, Object>[] mapArray = testMapArray();
		List<Map<String, Object>> mapList = testMapList();

		EvaluationContext context = parser.readonlyMapSimplifyContext();

		{
			Object v = parser.getValue("name", context, bean);
			assertEquals("name1", v);
		}
		{
			Object v = parser.getValue("map['key1']", context, bean);
			assertEquals("value1", v);
		}
		{
			Object v = parser.getValue("map.key1", context, bean);
			assertEquals("value1", v);
		}
		{
			Object v = parser.getValue("map['size']", context, bean);
			assertEquals(99, v);
		}
		{
			Object v = parser.getValue("map.size", context, bean);
			assertEquals(99, v);
		}
		{
			Object v = parser.getValue("map['inexists']", context, bean);
			assertNull(v);
		}

		{
			Object v = parser.getValue("['key1']", context, map);
			assertEquals("value1", v);
		}
		{
			Object v = parser.getValue("key1", context, map);
			assertEquals("value1", v);
		}
		{
			Object v = parser.getValue("['size']", context, map);
			assertNull(v);
		}
		{
			Object v = parser.getValue("size", context, map);
			assertNull(v);
		}

		{
			Object v = parser.getValue("[0]['key11']", context, mapArray);
			assertEquals("value11", v);
		}
		{
			Object v = parser.getValue("[0].key11", context, mapArray);
			assertEquals("value11", v);
		}
		{
			Object v = parser.getValue("[0]['size']", context, mapArray);
			assertNull(v);
		}
		{
			Object v = parser.getValue("[0].size", context, mapArray);
			assertNull(v);
		}

		{
			Object v = parser.getValue("[1]['key21']", context, mapList);
			assertEquals("value21", v);
		}
		{
			Object v = parser.getValue("[1].key21", context, mapList);
			assertEquals("value21", v);
		}
		{
			Object v = parser.getValue("[1]['size']", context, mapList);
			assertNull(v);
		}
		{
			Object v = parser.getValue("[1].size", context, mapList);
			assertNull(v);
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

	@SuppressWarnings("unchecked")
	protected Map<String, Object>[] testMapArray()
	{
		List<Map<String, Object>> list = testMapList();
		return list.toArray(new Map[list.size()]);
	}

	protected List<Map<String, Object>> testMapList()
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

	protected TestBean testBean()
	{
		TestBean bean = new TestBean("id1", "name1", 1);

		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		map.put("key2", 2);
		map.put("key3", new TestBean("key3-id", "key3-name", 3));
		map.put("key4", "value4");
		map.put("size", 99);

		bean.setMap(map);

		return bean;
	}

	protected TestBean[] testBeanArray()
	{
		List<TestBean> list = testBeanList();
		return list.toArray(new TestBean[list.size()]);
	}

	protected List<TestBean> testBeanList()
	{
		List<TestBean> list = new ArrayList<>();

		{
			TestBean bean = new TestBean("id1", "name1", 1);

			Map<String, Object> map = new HashMap<>();
			map.put("key11", "value11");
			map.put("key12", 12);
			map.put("key13", new TestBean("key13-id", "key13-name", 13));
			map.put("key14", "value14");

			bean.setMap(map);
			list.add(bean);
		}

		{
			TestBean bean = new TestBean("id2", "name2", 2);

			Map<String, Object> map = new HashMap<>();
			map.put("key21", "value21");
			map.put("key22", 22);
			map.put("key23", new TestBean("key23-id", "key23-name", 23));
			map.put("key24", "value24");

			bean.setMap(map);
			list.add(bean);
		}

		{
			TestBean bean = new TestBean("id3", "name3", 3);

			Map<String, Object> map = new HashMap<>();
			map.put("key31", "value31");
			map.put("key32", 32);
			map.put("key33", new TestBean("key33-id", "key33-name", 33));
			map.put("key34", "value34");

			bean.setMap(map);
			list.add(bean);
		}

		return list;
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
