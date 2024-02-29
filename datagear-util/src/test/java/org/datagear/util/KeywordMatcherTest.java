/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.datagear.util.KeywordMatcher.MatchValue;
import org.junit.Test;

/**
 * {@linkplain KeywordMatcher}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class KeywordMatcherTest
{
	@Test
	public void test()
	{
		KeywordMatcher matcher = new KeywordMatcher();

		MatchValue<TestBean> mv = new MatchValue<KeywordMatcherTest.TestBean>()
		{
			@Override
			public String[] get(TestBean t)
			{
				return new String[] { t.getName() };
			}
		};
		
		{
			String pattern = "abc%";
			List<TestBean> list = Arrays.asList(new TestBean("Abc"), new TestBean("ABCd"), new TestBean("deabc"),
					new TestBean("ghi"));
			List<TestBean> actual = matcher.match(list, pattern, mv);

			assertEquals(2, actual.size());
			assertEquals("Abc", actual.get(0).getName());
			assertEquals("ABCd", actual.get(1).getName());
		}

		{
			String pattern = "%abc";
			List<TestBean> list = Arrays.asList(new TestBean("aBc"), new TestBean("defAbc"), new TestBean("abcde"),
					new TestBean("ghi"));
			List<TestBean> actual = matcher.match(list, pattern, mv);

			assertEquals(2, actual.size());
			assertEquals("aBc", actual.get(0).getName());
			assertEquals("defAbc", actual.get(1).getName());
		}

		{
			String pattern = "abc";
			List<TestBean> list = Arrays.asList(new TestBean("abc"), new TestBean("aBc"), new TestBean("abcdef"),
					new TestBean("defabc"), new TestBean("ghi"));
			List<TestBean> actual = matcher.match(list, pattern, mv);

			assertEquals(4, actual.size());
			assertEquals("abc", actual.get(0).getName());
			assertEquals("aBc", actual.get(1).getName());
			assertEquals("abcdef", actual.get(2).getName());
			assertEquals("defabc", actual.get(3).getName());
		}
	}

	protected static class TestBean
	{
		private String name;

		public TestBean(String name)
		{
			super();
			this.name = name;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}
	}
}
