/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;

/**
 * {@linkplain CharsetTagListener}单元测试用例。
 * 
 * @author datagear@163.com
 *
 */
public class CharsetTagListenerTest
{
	@Test
	public void test() throws IOException
	{
		HtmlFilter htmlFilter = new HtmlFilter();

		{
			String html = "<html><head><meta charset=\"UTF-8\"></head><body></body></html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			CharsetTagListener tagListener = new CharsetTagListener(true);
			htmlFilter.filter(in, out, tagListener);

			assertEquals("UTF-8", tagListener.getCharset());
		}
		{
			String html = "<html><head><meta charset='UTF-8'></head><body></body></html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			CharsetTagListener tagListener = new CharsetTagListener(true);
			htmlFilter.filter(in, out, tagListener);

			assertEquals("UTF-8", tagListener.getCharset());
		}
		{
			String html = "<html><head><meta charset=UTF-8></head><body></body></html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			CharsetTagListener tagListener = new CharsetTagListener(true);
			htmlFilter.filter(in, out, tagListener);

			assertEquals("UTF-8", tagListener.getCharset());
		}
		{
			String html = "<html><head><meta charset=' UTF-8 '></head><body></body></html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			CharsetTagListener tagListener = new CharsetTagListener(true);
			htmlFilter.filter(in, out, tagListener);

			assertEquals("UTF-8", tagListener.getCharset());
		}
		{
			String html = "<html><head><META CHARSET=' UTF-8 '></head><body></body></html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			CharsetTagListener tagListener = new CharsetTagListener(true);
			htmlFilter.filter(in, out, tagListener);

			assertEquals("UTF-8", tagListener.getCharset());
		}
		{
			String html = "<html><head><meta content=\"text/html; charset=UTF-8\"></head><body></body></html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			CharsetTagListener tagListener = new CharsetTagListener(true);
			htmlFilter.filter(in, out, tagListener);

			assertEquals("UTF-8", tagListener.getCharset());
		}
		{
			String html = "<html><head><meta content=\"text/html; charset=UTF-8; p0=v0\"></head><body></body></html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			CharsetTagListener tagListener = new CharsetTagListener(true);
			htmlFilter.filter(in, out, tagListener);

			assertEquals("UTF-8", tagListener.getCharset());
		}
		{
			String html = "<html><head><meta content=\"charset=UTF-8\"></head><body></body></html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			CharsetTagListener tagListener = new CharsetTagListener(true);
			htmlFilter.filter(in, out, tagListener);

			assertEquals("UTF-8", tagListener.getCharset());
		}
		{
			String html = "<html><head><meta content=\"charset=UTF-8; text/html\"></head><body></body></html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			CharsetTagListener tagListener = new CharsetTagListener(true);
			htmlFilter.filter(in, out, tagListener);

			assertEquals("UTF-8", tagListener.getCharset());
		}
		{
			String html = "<html><head><META CONTENT=\"charset=UTF-8; text/html\"></head><body></body></html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			CharsetTagListener tagListener = new CharsetTagListener(true);
			htmlFilter.filter(in, out, tagListener);

			assertEquals("UTF-8", tagListener.getCharset());
		}
		{
			String html = "<html><head></head><body></body></html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			CharsetTagListener tagListener = new CharsetTagListener(true);
			htmlFilter.filter(in, out, tagListener);

			assertNull(tagListener.getCharset());
		}
	}
}
