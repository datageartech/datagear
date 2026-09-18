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

		{
			String pattern = "asterisk:";

			assertTrue(matcher.matches(pattern, ""));
			assertFalse(matcher.matches(pattern, "a"));
			assertFalse(matcher.matches(pattern, "abc"));
		}
		{
			String pattern = "asterisk:abc*";

			assertTrue(matcher.matches(pattern, "abc"));
			assertTrue(matcher.matches(pattern, "abcdef"));
			assertFalse(matcher.matches(pattern, "ac"));
		}

		{
			String pattern = "regex:";

			assertTrue(matcher.matches(pattern, ""));
			assertFalse(matcher.matches(pattern, "a"));
			assertFalse(matcher.matches(pattern, "abc"));
		}
		{
			String pattern = "regex:^abc$";

			assertTrue(matcher.matches(pattern, "abc"));
			assertFalse(matcher.matches(pattern, "abcdef"));
			assertFalse(matcher.matches(pattern, "ac"));
		}
		{
			String pattern = "regex:^abc\\d+$";

			assertTrue(matcher.matches(pattern, "abc1"));
			assertFalse(matcher.matches(pattern, "Abc1"));
			assertFalse(matcher.matches(pattern, "abcdef"));
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

		{
			String pattern = "asterisk:abc";

			assertTrue(matcher.matches(pattern, "abc"));
			assertTrue(matcher.matches(pattern, "aBc"));
			assertFalse(matcher.matches(pattern, "abcdef"));
			assertFalse(matcher.matches(pattern, "defabc"));
		}

		{
			String pattern = "regex:^abc\\d+$";

			assertTrue(matcher.matches(pattern, "abc1"));
			assertTrue(matcher.matches(pattern, "Abc1"));
			assertFalse(matcher.matches(pattern, "abcdef"));
		}
	}

	@Test
	public void test_regex_url_no_azAZ_encoded()
	{
		AsteriskPatternMatcher matcher = new AsteriskPatternMatcher(true);

		// URL中a-z、A-Z字符的编码值
		String pattern = "regex:^.*%(4[1-9A-Fa-f]|5[0-9Aa]|6[1-9A-Fa-f]|7[0-9Aa]).*$";

		assertFalse(matcher.matches(pattern, "abc1"));
		assertFalse(matcher.matches(pattern, "%00"));
		assertFalse(matcher.matches(pattern, "%01"));
		assertFalse(matcher.matches(pattern, "%10"));
		assertFalse(matcher.matches(pattern, "%11"));
		assertFalse(matcher.matches(pattern, "%20"));
		assertFalse(matcher.matches(pattern, "%21"));
		assertFalse(matcher.matches(pattern, "%30"));
		assertFalse(matcher.matches(pattern, "%31"));
		assertFalse(matcher.matches(pattern, "%40"));
		assertFalse(matcher.matches(pattern, "%80"));

		assertTrue(matcher.matches(pattern, "%41"));
		assertTrue(matcher.matches(pattern, "%49"));
		assertTrue(matcher.matches(pattern, "%4A"));
		assertTrue(matcher.matches(pattern, "%4F"));
		assertTrue(matcher.matches(pattern, "%4a"));
		assertTrue(matcher.matches(pattern, "%4f"));

		assertTrue(matcher.matches(pattern, "%50"));
		assertTrue(matcher.matches(pattern, "%59"));
		assertTrue(matcher.matches(pattern, "%5A"));
		assertTrue(matcher.matches(pattern, "%5a"));
		assertFalse(matcher.matches(pattern, "%5B"));
		assertFalse(matcher.matches(pattern, "%5b"));

		assertTrue(matcher.matches(pattern, "%61"));
		assertTrue(matcher.matches(pattern, "%69"));
		assertTrue(matcher.matches(pattern, "%6A"));
		assertTrue(matcher.matches(pattern, "%6F"));
		assertTrue(matcher.matches(pattern, "%6a"));
		assertTrue(matcher.matches(pattern, "%6f"));

		assertTrue(matcher.matches(pattern, "%70"));
		assertTrue(matcher.matches(pattern, "%79"));
		assertTrue(matcher.matches(pattern, "%7A"));
		assertTrue(matcher.matches(pattern, "%7a"));
		assertFalse(matcher.matches(pattern, "%7B"));
		assertFalse(matcher.matches(pattern, "%7b"));

		assertTrue(matcher.matches(pattern, "%50%51"));
		assertTrue(matcher.matches(pattern, "abc_%50%51"));
		assertTrue(matcher.matches(pattern, "%50%51_abc"));

		assertTrue(matcher.matches(pattern, "%50_%51"));
		assertTrue(matcher.matches(pattern, "abc_%50_%51"));
		assertTrue(matcher.matches(pattern, "%50_%51_abc"));

		assertFalse(matcher.matches(pattern, "%80%81"));
		assertFalse(matcher.matches(pattern, "abc_%80%81"));
		assertFalse(matcher.matches(pattern, "%80%81_abc"));
	}
}
