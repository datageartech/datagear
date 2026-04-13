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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

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
			String name = "a.a\\a";
			String fullname = FullnameSpec.toFullname(name, null);
			assertEquals("a\\.a\\\\a", fullname);
		}
	}

	@Test
	public void isTopFullnameTest()
	{
		{
			String fullname = "aaa";
			assertTrue(FullnameSpec.isTopFullname(fullname, "aaa"));
		}

		{
			String fullname = "aaa.bbb";
			assertFalse(FullnameSpec.isTopFullname(fullname, "aaa"));
		}

		{
			String fullname = "aaa\\.bbb";
			assertTrue(FullnameSpec.isTopFullname(fullname, "aaa.bbb"));
		}
	}
}
