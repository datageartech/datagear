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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * {@linkplain StringUtil}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class StringUtilTest
{
	@Test
	public void splitTest()
	{
		{
			String s = "abc,def";
			String[] actual = StringUtil.split(s, ",", true);

			assertEquals(2, actual.length);
			assertEquals("abc", actual[0]);
			assertEquals("def", actual[1]);
		}
		{
			String s = "ab,cd,ef";
			String[] actual = StringUtil.split(s, ",", true);

			assertEquals(3, actual.length);
			assertEquals("ab", actual[0]);
			assertEquals("cd", actual[1]);
			assertEquals("ef", actual[2]);
		}
		{
			String s = "abc.def";
			String[] actual = StringUtil.split(s, ".", true);

			assertEquals(2, actual.length);
			assertEquals("abc", actual[0]);
			assertEquals("def", actual[1]);
		}
		{
			String s = "ab.cd.ef";
			String[] actual = StringUtil.split(s, ".", true);

			assertEquals(3, actual.length);
			assertEquals("ab", actual[0]);
			assertEquals("cd", actual[1]);
			assertEquals("ef", actual[2]);
		}

		// 以分隔符开头
		{
			String s = ",abc";
			String[] actual = StringUtil.split(s, ",", true);

			assertEquals(1, actual.length);
			assertEquals("abc", actual[0]);
		}
		{
			String s = ",ab,c";
			String[] actual = StringUtil.split(s, ",", true);

			assertEquals(2, actual.length);
			assertEquals("ab", actual[0]);
			assertEquals("c", actual[1]);
		}

		// 以分隔符结尾
		{
			String s = "abc,";
			String[] actual = StringUtil.split(s, ",", true);

			assertEquals(1, actual.length);
			assertEquals("abc", actual[0]);
		}
		{
			String s = "ab,c,";
			String[] actual = StringUtil.split(s, ",", true);

			assertEquals(2, actual.length);
			assertEquals("ab", actual[0]);
			assertEquals("c", actual[1]);
		}

		// 连续分隔符
		{
			String s = "abc,,def";
			String[] actual = StringUtil.split(s, ",", true);

			assertEquals(2, actual.length);
			assertEquals("abc", actual[0]);
			assertEquals("def", actual[1]);
		}

		// 无分隔符
		{
			String s = "abcdef";
			String[] actual = StringUtil.split(s, ",", true);

			assertEquals(1, actual.length);
			assertEquals(s, actual[0]);
		}
	}

	@Test
	public void escapeHtmlTest()
	{
		{
			String  s ="abc<>\"&def";
			String actual = StringUtil.escapeHtml(s);
			assertEquals("abc&lt;&gt;&quot;&amp;def", actual);
		}
	}
	
	@Test
	public void maskTest()
	{
		{
			String s = null;
			String actual = StringUtil.mask(s, 2, 2, 2);
			assertEquals("**", actual);
		}

		{
			String s = "";
			String actual = StringUtil.mask(s, 2, 2, 2);
			assertEquals("**", actual);
		}

		{
			String s = "a";
			String actual = StringUtil.mask(s, 2, 2, 2);
			assertEquals("a**", actual);
		}

		{
			String s = "ab";
			String actual = StringUtil.mask(s, 2, 2, 2);
			assertEquals("ab**", actual);
		}

		{
			String s = "abc";
			String actual = StringUtil.mask(s, 2, 2, 2);
			assertEquals("ab**c", actual);
		}

		{
			String s = "abcd";
			String actual = StringUtil.mask(s, 2, 2, 2);
			assertEquals("ab**cd", actual);
		}

		{
			String s = "abcdefghi";
			String actual = StringUtil.mask(s, 2, 2, 2);
			assertEquals("ab**hi", actual);
		}

		//

		{
			String s = null;
			String actual = StringUtil.mask(s, 2, 0, 2);
			assertEquals("**", actual);
		}

		{
			String s = "";
			String actual = StringUtil.mask(s, 2, 0, 2);
			assertEquals("**", actual);
		}

		{
			String s = "a";
			String actual = StringUtil.mask(s, 2, 0, 2);
			assertEquals("a**", actual);
		}

		{
			String s = "ab";
			String actual = StringUtil.mask(s, 2, 0, 2);
			assertEquals("ab**", actual);
		}

		{
			String s = "abc";
			String actual = StringUtil.mask(s, 2, 0, 2);
			assertEquals("ab**", actual);
		}

		{
			String s = "abcd";
			String actual = StringUtil.mask(s, 2, 0, 2);
			assertEquals("ab**", actual);
		}

		{
			String s = "abcdefghi";
			String actual = StringUtil.mask(s, 2, 0, 2);
			assertEquals("ab**", actual);
		}

		//
		{
			String s = null;
			String actual = StringUtil.mask(s, 0, 2, 2);
			assertEquals("**", actual);
		}

		{
			String s = "";
			String actual = StringUtil.mask(s, 0, 2, 2);
			assertEquals("**", actual);
		}

		{
			String s = "a";
			String actual = StringUtil.mask(s, 0, 2, 2);
			assertEquals("**a", actual);
		}

		{
			String s = "ab";
			String actual = StringUtil.mask(s, 0, 2, 2);
			assertEquals("**ab", actual);
		}

		{
			String s = "abc";
			String actual = StringUtil.mask(s, 0, 2, 2);
			assertEquals("**bc", actual);
		}

		{
			String s = "abcd";
			String actual = StringUtil.mask(s, 0, 2, 2);
			assertEquals("**cd", actual);
		}

		{
			String s = "abcdefghi";
			String actual = StringUtil.mask(s, 0, 2, 2);
			assertEquals("**hi", actual);
		}

		//

		{
			String s = null;
			String actual = StringUtil.mask(s, 2, 2, 0);
			assertEquals("", actual);
		}

		{
			String s = "";
			String actual = StringUtil.mask(s, 2, 2, 0);
			assertEquals("", actual);
		}

		{
			String s = "a";
			String actual = StringUtil.mask(s, 2, 2, 0);
			assertEquals("a", actual);
		}

		{
			String s = "ab";
			String actual = StringUtil.mask(s, 2, 2, 0);
			assertEquals("ab", actual);
		}

		{
			String s = "abc";
			String actual = StringUtil.mask(s, 2, 2, 0);
			assertEquals("abc", actual);
		}

		{
			String s = "abcd";
			String actual = StringUtil.mask(s, 2, 2, 0);
			assertEquals("abcd", actual);
		}

		{
			String s = "abcdefghi";
			String actual = StringUtil.mask(s, 2, 2, 0);
			assertEquals("abhi", actual);
		}
	}

	@Test
	public void toBooleanTest_String() throws Exception
	{
		{
			String v = "true";
			boolean actual = StringUtil.toBoolean(v);
			assertTrue(actual);
		}
		{
			String v = "TRUE";
			boolean actual = StringUtil.toBoolean(v);
			assertTrue(actual);
		}
		{
			String v = "1";
			boolean actual = StringUtil.toBoolean(v);
			assertTrue(actual);
		}
		{
			String v = "y";
			boolean actual = StringUtil.toBoolean(v);
			assertTrue(actual);
		}
		{
			String v = "Y";
			boolean actual = StringUtil.toBoolean(v);
			assertTrue(actual);
		}
		{
			String v = "yes";
			boolean actual = StringUtil.toBoolean(v);
			assertTrue(actual);
		}
		{
			String v = "YES";
			boolean actual = StringUtil.toBoolean(v);
			assertTrue(actual);
		}
		{
			String v = "on";
			boolean actual = StringUtil.toBoolean(v);
			assertTrue(actual);
		}
		{
			String v = "ON";
			boolean actual = StringUtil.toBoolean(v);
			assertTrue(actual);
		}
		{
			String v = "是";
			boolean actual = StringUtil.toBoolean(v);
			assertTrue(actual);
		}

		{
			String v = null;
			boolean actual = StringUtil.toBoolean(v);
			assertFalse(actual);
		}

		{
			String v = "";
			boolean actual = StringUtil.toBoolean(v);
			assertFalse(actual);
		}

		{
			String v = "2";
			boolean actual = StringUtil.toBoolean(v);
			assertFalse(actual);
		}
	}

	@Test
	public void toBooleanTest_Number() throws Exception
	{
		{
			int v = 1;
			boolean actual = StringUtil.toBoolean(v);
			assertTrue(actual);
		}
		{
			int v = 2;
			boolean actual = StringUtil.toBoolean(v);
			assertTrue(actual);
		}

		{
			Integer v = null;
			boolean actual = StringUtil.toBoolean(v);
			assertFalse(actual);
		}
		{
			int v = 0;
			boolean actual = StringUtil.toBoolean(v);
			assertFalse(actual);
		}
		{
			int v = -1;
			boolean actual = StringUtil.toBoolean(v);
			assertFalse(actual);
		}
	}

	@Test
	public void encodeURLTest() throws Exception
	{
		{
			String s = "/a/b/c";
			String actual = StringUtil.encodeURL(s, IOUtil.CHARSET_UTF_8);

			assertEquals("%2Fa%2Fb%2Fc", actual);
		}

		{
			String s = "/a/b/ c";
			String actual = StringUtil.encodeURL(s, IOUtil.CHARSET_UTF_8);

			assertEquals("%2Fa%2Fb%2F+c", actual);
		}

		{
			String s = "/a/b/c?param=1";
			String actual = StringUtil.encodeURL(s, IOUtil.CHARSET_UTF_8);

			assertEquals("%2Fa%2Fb%2Fc%3Fparam%3D1", actual);
		}

		{
			String s = "中文";
			String actual = StringUtil.encodeURL(s, IOUtil.CHARSET_UTF_8);

			assertEquals("%E4%B8%AD%E6%96%87", actual);
		}
	}

	@Test
	public void decodeURLTest() throws Exception
	{
		{
			String s = "%2Fa%2Fb%2Fc";
			String actual = StringUtil.decodeURL(s, IOUtil.CHARSET_UTF_8);

			assertEquals("/a/b/c", actual);
		}

		{
			String s = "%2Fa%2Fb%2F+c";
			String actual = StringUtil.decodeURL(s, IOUtil.CHARSET_UTF_8);

			assertEquals("/a/b/ c", actual);
		}

		{
			String s = "%2Fa%2Fb%2Fc%3Fparam%3D1";
			String actual = StringUtil.decodeURL(s, IOUtil.CHARSET_UTF_8);

			assertEquals("/a/b/c?param=1", actual);
		}

		{
			String s = "%E4%B8%AD%E6%96%87";
			String actual = StringUtil.decodeURL(s, IOUtil.CHARSET_UTF_8);

			assertEquals("中文", actual);
		}
	}

	@Test
	public void encodePathURLTest() throws Exception
	{
		{
			String url = "abc";
			String actual = StringUtil.encodePathURL(url, IOUtil.CHARSET_UTF_8);
			assertEquals(url, actual);
		}

		{
			String url = "abc/def/ghi";
			String actual = StringUtil.encodePathURL(url, IOUtil.CHARSET_UTF_8);
			assertEquals(url, actual);
		}

		{
			String url = "abc/中 文/?/ghi";
			String actual = StringUtil.encodePathURL(url, IOUtil.CHARSET_UTF_8);
			assertEquals("abc/%E4%B8%AD+%E6%96%87/%3F/ghi", actual);
			assertEquals(url, StringUtil.decodeURL(actual, IOUtil.CHARSET_UTF_8));
		}

		{
			String url = "/abc//中 文/?/ghi//";
			String actual = StringUtil.encodePathURL(url, IOUtil.CHARSET_UTF_8);
			assertEquals("/abc//%E4%B8%AD+%E6%96%87/%3F/ghi//", actual);
			assertEquals(url, StringUtil.decodeURL(actual, IOUtil.CHARSET_UTF_8));
		}
	}

	@Test
	public void toAsciiURITest() throws Exception
	{
		{
			String uri = "http://abc.def/def/ghi?param1=a&param2=b#jkl";
			String actual = StringUtil.toAsciiURI(uri);
			assertEquals(uri, actual);
		}

		{
			String uri = "中文一/ghi?param1=中文二&param2=中文三#中文四";
			String actual = StringUtil.toAsciiURI(uri);
			assertEquals(
					"%E4%B8%AD%E6%96%87%E4%B8%80/ghi?param1=%E4%B8%AD%E6%96%87%E4%BA%8C&param2=%E4%B8%AD%E6%96%87%E4%B8%89#%E4%B8%AD%E6%96%87%E5%9B%9B",
					actual);
		}

		{
			String uri = "http://中文.def.com/中文一/ghi?param1=中文二&param2=b#中文三";
			String actual = StringUtil.toAsciiURI(uri);
			assertEquals(
					"http://%E4%B8%AD%E6%96%87.def.com/%E4%B8%AD%E6%96%87%E4%B8%80/ghi?param1=%E4%B8%AD%E6%96%87%E4%BA%8C&param2=b#%E4%B8%AD%E6%96%87%E4%B8%89",
					actual);
		}
	}
}
