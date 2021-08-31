/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
	}
}
