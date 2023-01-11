/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util;

import static org.junit.Assert.assertEquals;

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
}
