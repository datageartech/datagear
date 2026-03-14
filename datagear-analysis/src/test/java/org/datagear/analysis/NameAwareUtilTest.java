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

package org.datagear.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * {@linkplain NameAwareUtil}单元测试。
 * 
 * @author datagear@163.com
 *
 */
public class NameAwareUtilTest
{
	@Test
	public void findTest()
	{
		{
			List<TestNameObject> list = null;
			TestNameObject actual = NameAwareUtil.find(list, null);
			assertNull(actual);
		}

		{
			List<TestNameObject> list = null;
			TestNameObject actual = NameAwareUtil.find(list, "aa");
			assertNull(actual);
		}

		{
			List<TestNameObject> list = new ArrayList<>();
			list.add(new TestNameObject("aaa"));
			list.add(new TestNameObject("bbb"));
			list.add(null);
			list.add(new TestNameObject("ccc"));
			list.add(new TestNameObject(null));

			{
				TestNameObject actual = NameAwareUtil.find(list, "ccc");
				assertNotNull(actual);
				assertEquals("ccc", actual.getName());
			}
			{
				TestNameObject actual = NameAwareUtil.find(list, null);
				assertNotNull(actual);
				assertNull(actual.getName());
			}
		}
	}

	protected static class TestNameObject implements NameAware
	{
		private String name = null;

		public TestNameObject()
		{
			super();
		}

		public TestNameObject(String name)
		{
			super();
			this.name = name;
		}

		@Override
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
