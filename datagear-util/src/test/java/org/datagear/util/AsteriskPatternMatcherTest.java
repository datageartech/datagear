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

package org.datagear.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * {@linkplain AsteriskPatternMatcher}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class AsteriskPatternMatcherTest
{
	@Test
	public void test()
	{
		AsteriskPatternMatcher matcher = new AsteriskPatternMatcher();

		{
			String pattern = null;

			assertTrue(matcher.matches(pattern, null));
			assertFalse(matcher.matches(pattern, ""));
			assertFalse(matcher.matches(pattern, "abc"));
		}

		{
			String pattern = "";

			assertTrue(matcher.matches(pattern, ""));
			assertFalse(matcher.matches(pattern, null));
			assertFalse(matcher.matches(pattern, "abc"));
		}

		{
			String pattern = null;

			assertTrue(matcher.matches(pattern, null));
			assertFalse(matcher.matches(pattern, ""));
			assertFalse(matcher.matches(pattern, "abc"));
		}

		{
			assertTrue(matcher.matches(null, null));
			assertFalse(matcher.matches("", null));
			assertFalse(matcher.matches("abc", null));
		}

		{
			String pattern = "*";

			assertTrue(matcher.matches(pattern, ""));
			assertTrue(matcher.matches(pattern, "abc"));
		}

		{
			String pattern = "abc*";

			assertTrue(matcher.matches(pattern, "abc"));
			assertTrue(matcher.matches(pattern, "abcdef"));
			assertFalse(matcher.matches(pattern, ""));
			assertFalse(matcher.matches(pattern, "defabcghi"));
			assertFalse(matcher.matches(pattern, "def"));
		}

		{
			String pattern = "*abc";

			assertTrue(matcher.matches(pattern, "abc"));
			assertTrue(matcher.matches(pattern, "defabc"));
			assertFalse(matcher.matches(pattern, ""));
			assertFalse(matcher.matches(pattern, "abcdef"));
			assertFalse(matcher.matches(pattern, "def"));
		}

		{
			String pattern = "*abc*";

			assertTrue(matcher.matches(pattern, "abc"));
			assertTrue(matcher.matches(pattern, "abcdef"));
			assertTrue(matcher.matches(pattern, "defabc"));
			assertTrue(matcher.matches(pattern, "defabcghi"));
			assertFalse(matcher.matches(pattern, ""));
			assertFalse(matcher.matches(pattern, "def"));
		}

		{
			String pattern = "abc";

			assertTrue(matcher.matches(pattern, "abc"));
			assertFalse(matcher.matches(pattern, "abcdef"));
			assertFalse(matcher.matches(pattern, "defabc"));
		}

		{
			String pattern = "abc*ghi";

			assertTrue(matcher.matches(pattern, "abcghi"));
			assertTrue(matcher.matches(pattern, "abcdefghi"));
			assertFalse(matcher.matches(pattern, ""));
			assertFalse(matcher.matches(pattern, "def"));
			assertFalse(matcher.matches(pattern, "abcdefghijkl"));
			assertFalse(matcher.matches(pattern, "jklabcdefghi"));
			assertFalse(matcher.matches(pattern, "jklabcdefghijkl"));
		}

		{
			String pattern = "192.168.1.1*";

			assertTrue(matcher.matches(pattern, "192.168.1.1:3306/dg_test"));
			assertFalse(matcher.matches(pattern, "jdbc:mysql://192.168.1.1:3306/dg_test"));
		}

		{
			String pattern = "*192.168.1.1";

			assertTrue(matcher.matches(pattern, "jdbc:mysql://192.168.1.1"));
			assertFalse(matcher.matches(pattern, "jdbc:mysql://192.168.1.1:3306/dg_test"));
		}

		{
			String pattern = "*192.168.1.1*";

			assertTrue(matcher.matches(pattern, "jdbc:mysql://192.168.1.1:3306/dg_test"));
			assertFalse(matcher.matches(pattern, "jdbc:mysql://192.168.1.2:3306/dg_test"));
		}

		{
			String pattern = "abc*";

			assertFalse(matcher.matches(pattern, "Abc"));
			assertFalse(matcher.matches(pattern, "ABCd"));
		}

		{
			String pattern = "";

			assertTrue(matcher.matches(pattern, ""));
			assertFalse(matcher.matches(pattern, "a"));
			assertFalse(matcher.matches(pattern, "abc"));
		}
	}

	@Test
	public void test_ignoreCase_true()
	{
		AsteriskPatternMatcher matcher = new AsteriskPatternMatcher();
		matcher.setIgnoreCase(true);

		{
			String pattern = "abc*";

			assertTrue(matcher.matches(pattern, "Abc"));
			assertTrue(matcher.matches(pattern, "ABCd"));
		}

		{
			String pattern = "*abc";

			assertTrue(matcher.matches(pattern, "aBc"));
			assertTrue(matcher.matches(pattern, "defAbc"));
		}

		{
			String pattern = "abc";

			assertTrue(matcher.matches(pattern, "abc"));
			assertTrue(matcher.matches(pattern, "aBc"));
			assertFalse(matcher.matches(pattern, "abcdef"));
			assertFalse(matcher.matches(pattern, "defabc"));
		}
	}
}
