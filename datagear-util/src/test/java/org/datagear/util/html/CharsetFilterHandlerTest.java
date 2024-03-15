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

package org.datagear.util.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;

/**
 * {@linkplain CharsetFilterHandler}单元测试用例。
 * 
 * @author datagear@163.com
 *
 */
public class CharsetFilterHandlerTest
{
	@Test
	public void test() throws IOException
	{
		HtmlFilter htmlFilter = new HtmlFilter();

		{
			String html = "<html><head><meta charset=\"UTF-8\"></head><body></body></html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			CharsetFilterHandler tagListener = new CharsetFilterHandler(out, true);
			htmlFilter.filter(in, tagListener);

			assertEquals("UTF-8", tagListener.getCharset());
		}
		{
			String html = "<html><head><meta charset='UTF-8'></head><body></body></html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			CharsetFilterHandler tagListener = new CharsetFilterHandler(out, true);
			htmlFilter.filter(in, tagListener);

			assertEquals("UTF-8", tagListener.getCharset());
		}
		{
			String html = "<html><head><meta charset=UTF-8></head><body></body></html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			CharsetFilterHandler tagListener = new CharsetFilterHandler(out, true);
			htmlFilter.filter(in, tagListener);

			assertEquals("UTF-8", tagListener.getCharset());
		}
		{
			String html = "<html><head><meta charset=' UTF-8 '></head><body></body></html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			CharsetFilterHandler tagListener = new CharsetFilterHandler(out, true);
			htmlFilter.filter(in, tagListener);

			assertEquals("UTF-8", tagListener.getCharset());
		}
		{
			String html = "<html><head><META CHARSET=' UTF-8 '></head><body></body></html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			CharsetFilterHandler tagListener = new CharsetFilterHandler(out, true);
			htmlFilter.filter(in, tagListener);

			assertEquals("UTF-8", tagListener.getCharset());
		}
		{
			String html = "<html><head><meta content=\"text/html; charset=UTF-8\"></head><body></body></html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			CharsetFilterHandler tagListener = new CharsetFilterHandler(out, true);
			htmlFilter.filter(in, tagListener);

			assertEquals("UTF-8", tagListener.getCharset());
		}
		{
			String html = "<html><head><meta content=\"text/html; charset=UTF-8; p0=v0\"></head><body></body></html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			CharsetFilterHandler tagListener = new CharsetFilterHandler(out, true);
			htmlFilter.filter(in, tagListener);

			assertEquals("UTF-8", tagListener.getCharset());
		}
		{
			String html = "<html><head><meta content=\"charset=UTF-8\"></head><body></body></html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			CharsetFilterHandler tagListener = new CharsetFilterHandler(out, true);
			htmlFilter.filter(in, tagListener);

			assertEquals("UTF-8", tagListener.getCharset());
		}
		{
			String html = "<html><head><meta content=\"charset=UTF-8; text/html\"></head><body></body></html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			CharsetFilterHandler tagListener = new CharsetFilterHandler(out, true);
			htmlFilter.filter(in, tagListener);

			assertEquals("UTF-8", tagListener.getCharset());
		}
		{
			String html = "<html><head><META CONTENT=\"charset=UTF-8; text/html\"></head><body></body></html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			CharsetFilterHandler tagListener = new CharsetFilterHandler(out, true);
			htmlFilter.filter(in, tagListener);

			assertEquals("UTF-8", tagListener.getCharset());
		}
		{
			String html = "<html><head></head><body></body></html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			CharsetFilterHandler tagListener = new CharsetFilterHandler(out, true);
			htmlFilter.filter(in, tagListener);

			assertNull(tagListener.getCharset());
		}
	}
}
