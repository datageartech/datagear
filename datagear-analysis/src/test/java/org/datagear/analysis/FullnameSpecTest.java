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
import static org.junit.Assert.assertThrows;

import java.util.List;

import org.junit.Test;

/**
 * {@linkplain FullnameSpec}单元测试。
 * 
 * @author datagear@163.com
 *
 */
public class FullnameSpecTest
{
	@Test
	public void toFullnameTest()
	{
		{
			String name = null;

			assertThrows(IllegalArgumentException.class, () ->
			{
				FullnameSpec.toFullname(name, null);
			});
		}

		{
			String name = "";

			assertThrows(IllegalArgumentException.class, () ->
			{
				FullnameSpec.toFullname(name, null);
			});
		}

		{
			String name = "aaa";
			String fullname = FullnameSpec.toFullname(name, null);
			assertEquals(name, fullname);
		}

		{
			String name = "bbb";
			String fullname = FullnameSpec.toFullname(name, "aaa");
			assertEquals("aaa.bbb", fullname);
		}

		{
			String name = "ccc";
			String fullname = FullnameSpec.toFullname(name, "aaa.bbb");
			assertEquals("aaa.bbb.ccc", fullname);
		}

		{
			String name = "a.a\\a\"a\'a[a]a";
			String fullname = FullnameSpec.toFullname(name, null);
			assertEquals("a\\.a\\\\a\\\"a\\\'a\\[a\\]a", fullname);
		}

		{
			String name = "b.b\\b\"b\'b[b]b";
			String fullname = FullnameSpec.toFullname(name, "a\\.a\\\\a\\\"a\\\'a\\[a\\]a");
			assertEquals("a\\.a\\\\a\\\"a\\\'a\\[a\\]a.b\\.b\\\\b\\\"b\\\'b\\[b\\]b", fullname);
		}

		{
			String name = "c.c\\c\"c\'c[c]c";
			String fullname = FullnameSpec.toFullname(name,
					"a\\.a\\\\a\\\"a\\\'a\\[a\\]a.b\\.b\\\\b\\\"b\\\'b\\[b\\]b");
			assertEquals("a\\.a\\\\a\\\"a\\\'a\\[a\\]a.b\\.b\\\\b\\\"b\\\'b\\[b\\]b.c\\.c\\\\c\\\"c\\\'c\\[c\\]c",
					fullname);
		}
	}

	@Test
	public void fromFullnameTest()
	{
		{
			String fullname = null;
			List<String> names = FullnameSpec.fromFullname(fullname);
			assertEquals(0, names.size());
		}

		{
			String fullname = "";
			List<String> names = FullnameSpec.fromFullname(fullname);
			assertEquals(0, names.size());
		}

		{
			String fullname = "...";
			List<String> names = FullnameSpec.fromFullname(fullname);
			assertEquals(0, names.size());
		}

		{
			String fullname = "aaa";
			List<String> names = FullnameSpec.fromFullname(fullname);
			assertEquals(1, names.size());
			assertEquals("aaa", names.get(0));
		}

		{
			String fullname = ".aaa";
			List<String> names = FullnameSpec.fromFullname(fullname);
			assertEquals(1, names.size());
			assertEquals("aaa", names.get(0));
		}

		{
			String fullname = "aaa.";
			List<String> names = FullnameSpec.fromFullname(fullname);
			assertEquals(1, names.size());
			assertEquals("aaa", names.get(0));
		}

		{
			String fullname = ".aaa.";
			List<String> names = FullnameSpec.fromFullname(fullname);
			assertEquals(1, names.size());
			assertEquals("aaa", names.get(0));
		}

		{
			String fullname = "aaa.bbb";
			List<String> names = FullnameSpec.fromFullname(fullname);
			assertEquals(2, names.size());
			assertEquals("aaa", names.get(0));
			assertEquals("bbb", names.get(1));
		}

		{
			String fullname = "aaa.bbb.ccc";
			List<String> names = FullnameSpec.fromFullname(fullname);
			assertEquals(3, names.size());
			assertEquals("aaa", names.get(0));
			assertEquals("bbb", names.get(1));
			assertEquals("ccc", names.get(2));
		}

		{
			String fullname = "a\\.a\\\\a\\\"a\\\'a\\[a\\]a";
			List<String> names = FullnameSpec.fromFullname(fullname);
			assertEquals(1, names.size());
			assertEquals("a.a\\a\"a\'a[a]a", names.get(0));
		}

		{
			List<String> names = FullnameSpec.fromFullname("a\\.a\\\\a\\\"a\\\'a\\[a\\]a.b\\.b\\\\b\\\"b\\\'b\\[b\\]b");
			assertEquals(2, names.size());
			assertEquals("a.a\\a\"a\'a[a]a", names.get(0));
			assertEquals("b.b\\b\"b\'b[b]b", names.get(1));
		}

		{
			List<String> names = FullnameSpec.fromFullname(
					"a\\.a\\\\a\\\"a\\\'a\\[a\\]a.b\\.b\\\\b\\\"b\\\'b\\[b\\]b.c\\.c\\\\c\\\"c\\\'c\\[c\\]c");
			assertEquals(3, names.size());
			assertEquals("a.a\\a\"a\'a[a]a", names.get(0));
			assertEquals("b.b\\b\"b\'b[b]b", names.get(1));
			assertEquals("c.c\\c\"c\'c[c]c", names.get(2));
		}
	}
}
